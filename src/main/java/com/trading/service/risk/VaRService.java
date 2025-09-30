package com.trading.service.risk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Value-at-Risk service (historical/parametric/monte-carlo).
 */
public class VaRService {

    public VaRService() {}

    /**
     * Historical VaR: returns a positive number representing loss at the given confidence level.
     */
    public static double historicalVaR(List<Double> returns, double confidenceLevel, double portfolioValue) {
        if (returns == null || returns.isEmpty() || portfolioValue <= 0 || confidenceLevel <= 0 || confidenceLevel >= 1) {
            return 0.0;
        }

        List<Double> sorted = new ArrayList<>(returns);
        Collections.sort(sorted); // ascending
        int idx = (int) Math.floor((1 - confidenceLevel) * sorted.size());
        idx = Math.max(0, Math.min(sorted.size() - 1, idx));
        double percentileReturn = sorted.get(idx);
        return -percentileReturn * portfolioValue;
    }

    /**
     * Parametric VaR assuming returns are approximately normal. Uses the inverse CDF (quantile)
     * from Apache Commons Math's NormalDistribution.
     */
    public static double parametricVaR(List<Double> returns, double confidenceLevel, double portfolioValue) {
        if (returns == null || returns.isEmpty() || portfolioValue <= 0 || confidenceLevel <= 0 || confidenceLevel >= 1) {
            return 0.0;
        }

        double mean = returns.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = returns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
        double std = Math.sqrt(Math.max(0.0, variance));

        NormalDistribution nd = new NormalDistribution(0, 1);
        double z = Math.abs(nd.inverseCumulativeProbability(1 - confidenceLevel)); // positive z
        return z * std * portfolioValue;
    }

}
