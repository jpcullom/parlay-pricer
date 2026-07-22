package com.sgpanalyzer.service;

import com.sgpanalyzer.exception.ResourceNotFoundException;
import com.sgpanalyzer.model.dto.BestParlayResponse;
import com.sgpanalyzer.model.dto.CorrelationLookupResponse;
import com.sgpanalyzer.model.dto.TeamCorrelationMatrixResponse;
import com.sgpanalyzer.model.entity.PlayerCorrelation;
import com.sgpanalyzer.model.entity.PlayerStatistic;
import com.sgpanalyzer.model.enums.Market;
import com.sgpanalyzer.repository.GameLogRepository;
import com.sgpanalyzer.repository.PlayerCorrelationRepository;
import com.sgpanalyzer.repository.PlayerStatisticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorrelationService {

    private final PlayerCorrelationRepository correlationRepository;
    private final PlayerStatisticRepository statisticRepository;
    private final GameLogRepository gameLogRepository;

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

        // TODO: Step 1 — Get distinct player names from team statistics
        //  List<String> playerNames = teamPlayers.stream()
        //      .map(PlayerStatistic::getPlayerName)
        //      .distinct()
        //      .sorted()
        //      .toList();

        // TODO: Step 2 — Query all pairwise correlations for those players
        //  List<PlayerCorrelation> correlations = correlationRepository.findAllByPlayersIn(playerNames);

        // TODO: Step 3 — Build NxN matrix where matrix[i][j] = correlation(player_i, player_j)
        //  Diagonal = 1.0
        //  Fill both (i,j) and (j,i) from each stored correlation

        // TODO: Step 4 — Return assembled matrix response
        return TeamCorrelationMatrixResponse.builder()
                .team(team.toUpperCase())
                .players(List.of()) // TODO: replace with playerNames
                .matrix(new double[0][0]) // TODO: replace with computed matrix
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

        // TODO: Step 1 — Fetch all distinct (playerName, market) combinations from game_logs
        //  Group game logs by (playerName, market) to get stat value arrays

        // TODO: Step 2 — For each pair of (playerA, marketA) and (playerB, marketB):
        //  a. Find games where BOTH players have data (inner join on gameId)
        //  b. Extract paired stat value arrays
        //  c. Require minimum sample size (e.g., 5 games)
        //  d. Compute Pearson correlation using Apache Commons Math:
        //     PearsonsCorrelation corr = new PearsonsCorrelation();
        //     double r = corr.correlation(valuesA, valuesB);
        //  e. Upsert into player_correlations table

        // TODO: Step 3 — Update player_statistics table with mean/stddev per player per market:
        //  DescriptiveStatistics stats = new DescriptiveStatistics(values);
        //  mean = stats.getMean();
        //  stdDev = stats.getStandardDeviation();

        // TODO: Step 4 — Evict correlation cache in Redis after rebuild
        //  cacheManager.getCache("correlations").clear();

        log.info("Correlation rebuild complete.");
    }
}
