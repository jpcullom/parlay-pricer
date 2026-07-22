package com.sgpanalyzer.service;

import com.sgpanalyzer.model.dto.ExpectedValueRequest;
import com.sgpanalyzer.model.dto.ExpectedValueResponse;
import org.springframework.stereotype.Service;

@Service
public class ExpectedValueService {

    /**
     * Calculates expected value given a fair probability and sportsbook odds.
     *
     * EV = (fairProbability * decimalPayout) - 1
     *
     * Where decimalPayout is derived from the sportsbook's American odds.
     */
    public ExpectedValueResponse calculateEV(ExpectedValueRequest request) {
        double fairProbability = request.getFairProbability();
        int sportsbookOdds = request.getSportsbookOdds();

        int fairOdds = probabilityToAmericanOdds(fairProbability);
        double ev = computeExpectedValue(fairProbability, sportsbookOdds);

        return ExpectedValueResponse.builder()
                .fairOdds(fairOdds)
                .expectedValue(ev)
                .build();
    }

    /**
     * Overload for internal use by ParlayAnalysisService.
     */
    public ExpectedValueResponse calculateEV(double fairProbability, int sportsbookOdds) {
        int fairOdds = probabilityToAmericanOdds(fairProbability);
        double ev = computeExpectedValue(fairProbability, sportsbookOdds);

        return ExpectedValueResponse.builder()
                .fairOdds(fairOdds)
                .expectedValue(ev)
                .build();
    }

    /**
     * Converts American odds to implied probability.
     *
     * Positive odds (e.g., +350): probability = 100 / (odds + 100)
     * Negative odds (e.g., -150): probability = |odds| / (|odds| + 100)
     */
    double americanOddsToImpliedProbability(int americanOdds) {
        // TODO: Handle edge cases (odds = 0, very large odds, even money +100/-100)
        if (americanOdds > 0) {
            return 100.0 / (americanOdds + 100.0);
        } else {
            return Math.abs(americanOdds) / (Math.abs(americanOdds) + 100.0);
        }
    }

    /**
     * Converts a probability to American odds.
     *
     * probability > 50%  →  negative odds: -(probability / (1 - probability)) * 100
     * probability <= 50% →  positive odds: ((1 - probability) / probability) * 100
     */
    int probabilityToAmericanOdds(double probability) {
        // TODO: Handle edge cases (probability near 0 or 1)
        if (probability >= 0.5) {
            return (int) Math.round(-(probability / (1.0 - probability)) * 100.0);
        } else {
            return (int) Math.round(((1.0 - probability) / probability) * 100.0);
        }
    }

    /**
     * Computes expected value.
     *
     * EV = (fairProbability * decimalPayout) - 1
     * where decimalPayout = 1 + (odds/100) for positive, or 1 + (100/|odds|) for negative
     */
    private double computeExpectedValue(double fairProbability, int sportsbookOdds) {
        double decimalPayout = americanOddsToDecimal(sportsbookOdds);
        return (fairProbability * decimalPayout) - 1.0;
    }

    /**
     * Converts American odds to decimal odds.
     *
     * Positive (+350) → 4.50
     * Negative (-150) → 1.667
     */
    private double americanOddsToDecimal(int americanOdds) {
        // TODO: Handle edge cases
        if (americanOdds > 0) {
            return 1.0 + (americanOdds / 100.0);
        } else {
            return 1.0 + (100.0 / Math.abs(americanOdds));
        }
    }
}
