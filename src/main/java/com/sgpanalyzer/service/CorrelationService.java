package com.sgpanalyzer.service;

import com.sgpanalyzer.exception.ResourceNotFoundException;
import com.sgpanalyzer.model.dto.BestParlayResponse;
import com.sgpanalyzer.model.dto.CorrelationLookupResponse;
import com.sgpanalyzer.model.dto.TeamCorrelationMatrixResponse;
import com.sgpanalyzer.model.entity.GameLog;
import com.sgpanalyzer.model.entity.PlayerCorrelation;
import com.sgpanalyzer.model.entity.PlayerStatistic;
import com.sgpanalyzer.model.enums.Market;
import com.sgpanalyzer.repository.GameLogRepository;
import com.sgpanalyzer.repository.PlayerCorrelationRepository;
import com.sgpanalyzer.repository.PlayerStatisticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorrelationService {

    private static final int MIN_SAMPLE_SIZE = 5;

    private final PlayerCorrelationRepository correlationRepository;
    private final PlayerStatisticRepository statisticRepository;
    private final GameLogRepository gameLogRepository;
    private final CacheManager cacheManager;

    /**
     * Looks up the precomputed correlation between two players for given markets.
     * Results are cached in Redis with a 1-hour TTL.
     */
    @Cacheable(value = "correlations", key = "#playerA + ':' + #playerB + ':' + #marketA + ':' + #marketB")
    public CorrelationLookupResponse getCorrelation(String playerA, String playerB,
                                                     Market marketA, Market marketB) {
        // Normalize ordering so (A,B) == (B,A) lookups hit the same cache/DB row
        String pA = playerA.compareTo(playerB) <= 0 ? playerA : playerB;
        String pB = playerA.compareTo(playerB) <= 0 ? playerB : playerA;
        Market mA = playerA.compareTo(playerB) <= 0 ? marketA : marketB;
        Market mB = playerA.compareTo(playerB) <= 0 ? marketB : marketA;

        PlayerCorrelation correlation = correlationRepository
                .findByPlayerAAndPlayerBAndMarketAAndMarketB(pA, pB, mA, mB)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No correlation data found for " + playerA + " / " + playerB));

        return CorrelationLookupResponse.builder()
                .playerA(correlation.getPlayerA())
                .playerB(correlation.getPlayerB())
                .correlation(correlation.getCorrelation())
                .sampleSize(correlation.getSampleSize())
                .build();
    }

    /**
     * Builds the full correlation matrix for all players on a given team.
     */
    public TeamCorrelationMatrixResponse getTeamCorrelationMatrix(String team) {
        List<PlayerStatistic> teamPlayers = statisticRepository.findByTeam(team.toUpperCase());
        if (teamPlayers.isEmpty()) {
            throw new ResourceNotFoundException("No player data found for team: " + team);
        }

        // Get distinct player names
        List<String> playerNames = teamPlayers.stream()
                .map(PlayerStatistic::getPlayerName)
                .distinct()
                .sorted()
                .toList();

        int n = playerNames.size();
        double[][] matrix = new double[n][n];

        // Diagonal = 1.0 (a player is perfectly correlated with themselves)
        for (int i = 0; i < n; i++) {
            matrix[i][i] = 1.0;
        }

        // Fill off-diagonal from stored correlations
        List<PlayerCorrelation> correlations = correlationRepository.findAllByPlayersIn(playerNames);
        for (PlayerCorrelation pc : correlations) {
            int i = playerNames.indexOf(pc.getPlayerA());
            int j = playerNames.indexOf(pc.getPlayerB());
            if (i >= 0 && j >= 0) {
                matrix[i][j] = pc.getCorrelation();
                matrix[j][i] = pc.getCorrelation();
            }
        }

        return TeamCorrelationMatrixResponse.builder()
                .team(team.toUpperCase())
                .players(playerNames)
                .matrix(matrix)
                .build();
    }

    /**
     * Returns the top-N most strongly correlated player pairs.
     */
    public List<BestParlayResponse> getBestCorrelatedParlays(String team, int limit) {
        // TODO: Step 1 — If team is specified, filter correlations by team players
        //  Otherwise, query across all stored correlations

        // TODO: Step 2 — Sort by absolute correlation descending, take top N

        // TODO: Step 3 — Map each PlayerCorrelation to BestParlayResponse
        //  legs = [playerA + " " + marketA, playerB + " " + marketB]
        //  correlation = pc.getCorrelation()

        List<PlayerCorrelation> topCorrelations = correlationRepository.findTopCorrelated(limit);

        return topCorrelations.stream()
                .map(pc -> BestParlayResponse.builder()
                        .legs(List.of(
                                pc.getPlayerA() + " " + pc.getMarketA(),
                                pc.getPlayerB() + " " + pc.getMarketB()))
                        .correlation(pc.getCorrelation())
                        .build())
                .toList();
    }

    /**
     * Rebuilds all pairwise correlations from raw game log data.
     * Called by the nightly scheduled job.
     */
    @Transactional
    public void rebuildCorrelations() {
        log.info("Starting correlation rebuild...");

        List<GameLog> allLogs = gameLogRepository.findAll();

        // Step 1: Group game logs by (playerName, market) → Map<"player|market", Map<gameId, value>>
        Map<String, Map<String, Double>> playerMarketGames = new HashMap<>();
        Map<String, String> playerTeams = new HashMap<>();

        for (GameLog gl : allLogs) {
            String key = gl.getPlayerName() + "|" + gl.getMarket().name();
            playerMarketGames
                    .computeIfAbsent(key, k -> new LinkedHashMap<>())
                    .put(gl.getGameId(), gl.getStatValue());
            playerTeams.put(gl.getPlayerName(), gl.getTeam());
        }

        List<String> keys = new ArrayList<>(playerMarketGames.keySet());
        int correlationsComputed = 0;

        // Step 2: For each pair, compute Pearson correlation on shared games
        PearsonsCorrelation pearson = new PearsonsCorrelation();

        for (int i = 0; i < keys.size(); i++) {
            for (int j = i + 1; j < keys.size(); j++) {
                String keyA = keys.get(i);
                String keyB = keys.get(j);

                Map<String, Double> gamesA = playerMarketGames.get(keyA);
                Map<String, Double> gamesB = playerMarketGames.get(keyB);

                // Find shared games (inner join on gameId)
                List<String> sharedGames = gamesA.keySet().stream()
                        .filter(gamesB::containsKey)
                        .toList();

                if (sharedGames.size() < MIN_SAMPLE_SIZE) {
                    continue;
                }

                double[] valuesA = sharedGames.stream().mapToDouble(gamesA::get).toArray();
                double[] valuesB = sharedGames.stream().mapToDouble(gamesB::get).toArray();

                double correlation = pearson.correlation(valuesA, valuesB);

                // Skip if NaN (happens when one array has zero variance)
                if (Double.isNaN(correlation)) {
                    continue;
                }

                // Parse player and market from keys
                String[] partsA = keyA.split("\\|");
                String[] partsB = keyB.split("\\|");
                String playerA = partsA[0];
                Market marketA = Market.valueOf(partsA[1]);
                String playerB = partsB[0];
                Market marketB = Market.valueOf(partsB[1]);

                // Normalize ordering: alphabetical by player name
                if (playerA.compareTo(playerB) > 0) {
                    String tmpP = playerA; playerA = playerB; playerB = tmpP;
                    Market tmpM = marketA; marketA = marketB; marketB = tmpM;
                }

                // Upsert correlation
                PlayerCorrelation existing = correlationRepository
                        .findByPlayerAAndPlayerBAndMarketAAndMarketB(playerA, playerB, marketA, marketB)
                        .orElse(null);

                if (existing != null) {
                    existing.setCorrelation(correlation);
                    existing.setSampleSize(sharedGames.size());
                    existing.setComputedAt(Instant.now());
                    correlationRepository.save(existing);
                } else {
                    correlationRepository.save(PlayerCorrelation.builder()
                            .playerA(playerA)
                            .playerB(playerB)
                            .marketA(marketA)
                            .marketB(marketB)
                            .correlation(correlation)
                            .sampleSize(sharedGames.size())
                            .computedAt(Instant.now())
                            .build());
                }
                correlationsComputed++;
            }
        }

        // Step 3: Update player_statistics (mean, stddev per player per market)
        int statsComputed = 0;
        for (Map.Entry<String, Map<String, Double>> entry : playerMarketGames.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String playerName = parts[0];
            Market market = Market.valueOf(parts[1]);
            String team = playerTeams.get(playerName);

            double[] values = entry.getValue().values().stream().mapToDouble(d -> d).toArray();
            DescriptiveStatistics stats = new DescriptiveStatistics(values);

            PlayerStatistic existing = statisticRepository
                    .findByPlayerNameAndMarket(playerName, market)
                    .orElse(null);

            if (existing != null) {
                existing.setMean(stats.getMean());
                existing.setStdDev(stats.getStandardDeviation());
                existing.setSampleSize((int) stats.getN());
                existing.setUpdatedAt(Instant.now());
                statisticRepository.save(existing);
            } else {
                statisticRepository.save(PlayerStatistic.builder()
                        .playerName(playerName)
                        .team(team)
                        .market(market)
                        .mean(stats.getMean())
                        .stdDev(stats.getStandardDeviation())
                        .sampleSize((int) stats.getN())
                        .updatedAt(Instant.now())
                        .build());
            }
            statsComputed++;
        }

        // Step 4: Evict correlation cache
        var cache = cacheManager.getCache("correlations");
        if (cache != null) {
            cache.clear();
        }

        log.info("Correlation rebuild complete. Computed {} correlations, {} player stats.",
                correlationsComputed, statsComputed);
    }
}
