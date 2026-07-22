package com.sgpanalyzer.service;

import com.sgpanalyzer.model.dto.ParlayLeg;
import com.sgpanalyzer.model.enums.Selection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonteCarloSimulationService {

    @Value("${sgp.simulation.default-iterations:100000}")
    private int defaultIterations;

    /**
     * Runs a Monte Carlo simulation to estimate the joint probability that all parlay legs hit,
     * accounting for correlation between player performances.
     *
     * @param legs              The parlay legs with player, market, line, and selection
     * @param correlationMatrix NxN correlation matrix where N = legs.size()
     * @param numSimulations    Number of simulation iterations (default: 100,000)
     * @return The estimated probability that all legs hit simultaneously
     */
    public double simulate(List<ParlayLeg> legs, double[][] correlationMatrix, int numSimulations) {
        int n = legs.size();
        int iterations = numSimulations > 0 ? numSimulations : defaultIterations;

        // TODO: Step 1 — Look up each player's mean and stdDev from PlayerStatistic
        //  double[] means = new double[n];
        //  double[] stdDevs = new double[n];
        //  for (int i = 0; i < n; i++) {
        //      PlayerStatistic stat = statisticRepository
        //          .findByPlayerNameAndMarket(legs.get(i).getPlayer(), legs.get(i).getMarket())
        //          .orElseThrow(...);
        //      means[i] = stat.getMean();
        //      stdDevs[i] = stat.getStdDev();
        //  }

        // TODO: Step 2 — Perform Cholesky decomposition of the correlation matrix
        //  This transforms independent random samples into correlated samples.
        //  Use Apache Commons Math:
        //
        //  RealMatrix corrMatrix = new Array2DRowRealMatrix(correlationMatrix);
        //  CholeskyDecomposition cholesky = new CholeskyDecomposition(corrMatrix);
        //  RealMatrix L = cholesky.getL();

        // TODO: Step 3 — Run simulation loop
        //  int wins = 0;
        //  Random rng = new Random();
        //
        //  for (int sim = 0; sim < iterations; sim++) {
        //      // Generate n independent standard normal samples
        //      double[] z = new double[n];
        //      for (int i = 0; i < n; i++) z[i] = rng.nextGaussian();
        //
        //      // Transform to correlated samples: correlated = L * z
        //      double[] correlated = new double[n];
        //      for (int i = 0; i < n; i++) {
        //          for (int j = 0; j <= i; j++) {
        //              correlated[i] += L.getEntry(i, j) * z[j];
        //          }
        //      }
        //
        //      // Scale to actual stat distributions: value = mean + stdDev * correlated
        //      boolean allHit = true;
        //      for (int i = 0; i < n; i++) {
        //          double simulatedValue = means[i] + stdDevs[i] * correlated[i];
        //          boolean hit = checkLegHit(legs.get(i), simulatedValue);
        //          if (!hit) { allHit = false; break; }
        //      }
        //
        //      if (allHit) wins++;
        //  }
        //
        //  return (double) wins / iterations;

        log.warn("Monte Carlo simulation not yet implemented — returning 0.0");
        return 0.0;
    }

    /**
     * Checks whether a simulated stat value satisfies the leg's line and selection.
     */
    private boolean checkLegHit(ParlayLeg leg, double simulatedValue) {
        if (leg.getSelection() == Selection.OVER) {
            return simulatedValue > leg.getLine();
        } else {
            return simulatedValue < leg.getLine();
        }
    }
}
