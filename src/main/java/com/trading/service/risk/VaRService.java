package com.trading.service.risk;

/**
 * Value-at-Risk service (historical/parametric/monte-carlo).
 */
public class VaRService {

	public VaRService() {

    // returns VaR (positive number) for given returns series, e.g. -P&L percentile
    public static double historicalVaR(List<Double> returns, double confidenceLevel, double portfolioValue) {
        // compute portfolio returns sorted
        List<Double> sorted = new ArrayList<>(returns);
        Collections.sort(sorted); // ascending
        int idx = (int)Math.floor((1 - confidenceLevel) * sorted.size());
        double percentileReturn = sorted.get(Math.max(0, idx));
        return -percentileReturn * portfolioValue;
    }

    }

}
