package com.trading.service.risk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

public class VaRServiceTest {

  @Test
  public void testHistoricalVaR_smallSample() {
    List<Double> returns = Arrays.asList(-0.02, -0.01, 0.0, 0.01, 0.02);
    double var = VaRService.historicalVaR(returns, 0.95, 1000.0);
    // With the conservative ceil-index logic this sample yields the second-worst loss: 0.01 * 1000 = 10
    assertEquals(10.0, var, 1e-6);
  }

  @Test
  public void testHistoricalVaR_edgeCases() {
    // null returns
    assertEquals(0.0, VaRService.historicalVaR(null, 0.95, 1000.0), 1e-12);
    // empty returns
    assertEquals(0.0, VaRService.historicalVaR(Collections.emptyList(), 0.95, 1000.0), 1e-12);
    // invalid confidence
    List<Double> r = Arrays.asList(-0.01, 0.0, 0.01);
    assertEquals(0.0, VaRService.historicalVaR(r, 0.0, 1000.0), 1e-12);
    assertEquals(0.0, VaRService.historicalVaR(r, 1.0, 1000.0), 1e-12);
    // non-positive portfolio value
    assertEquals(0.0, VaRService.historicalVaR(r, 0.95, 0.0), 1e-12);
  }

  @Test
  public void testParametricVaR_expected() {
    List<Double> returns = Arrays.asList(-0.02, -0.01, 0.0, 0.01, 0.02);
    // compute expected via same statistical formula used in the implementation
    double mean = returns.stream().mapToDouble(d -> d).average().orElse(0.0);
    double variance = returns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
    double std = Math.sqrt(Math.max(0.0, variance));
    NormalDistribution nd = new NormalDistribution(0, 1);
    double z = Math.abs(nd.inverseCumulativeProbability(1 - 0.95));
    double expected = z * std * 1000.0;

    double var = VaRService.parametricVaR(returns, 0.95, 1000.0);
    assertEquals(expected, var, 1e-8);
  }

  @Test
  public void testParametricVaR_edgeCases() {
    assertEquals(0.0, VaRService.parametricVaR(null, 0.95, 1000.0), 1e-12);
    assertEquals(0.0, VaRService.parametricVaR(Collections.emptyList(), 0.95, 1000.0), 1e-12);
    List<Double> r = Arrays.asList(-0.01, 0.0, 0.01);
    assertEquals(0.0, VaRService.parametricVaR(r, 0.0, 1000.0), 1e-12);
    assertEquals(0.0, VaRService.parametricVaR(r, 1.0, 1000.0), 1e-12);
    assertEquals(0.0, VaRService.parametricVaR(r, 0.95, -100.0), 1e-12);
  }

  @Test
  public void testMonteCarloVaR_zeroStdAndEdgeCases() {
    // When historical returns have zero variance, the monteCarlo sampling yields zeros -> VaR 0
    List<Double> flat = Arrays.asList(0.0, 0.0, 0.0, 0.0);
    double mc = VaRService.monteCarloVaR(flat, 0.95, 1000.0, 1000);
    assertEquals(0.0, mc, 1e-12);

    // invalid inputs
    assertEquals(0.0, VaRService.monteCarloVaR(null, 0.95, 1000.0, 100), 1e-12);
    assertEquals(0.0, VaRService.monteCarloVaR(Collections.emptyList(), 0.95, 1000.0, 100), 1e-12);
    List<Double> r = Arrays.asList(-0.01, 0.0, 0.01);
    assertEquals(0.0, VaRService.monteCarloVaR(r, 0.0, 1000.0, 100), 1e-12);
    assertEquals(0.0, VaRService.monteCarloVaR(r, 1.0, 1000.0, 100), 1e-12);
    assertEquals(0.0, VaRService.monteCarloVaR(r, 0.95, -100.0, 100), 1e-12);
    // sims <= 0 should be guarded; expect 0
    assertEquals(0.0, VaRService.monteCarloVaR(r, 0.95, 1000.0, 0), 1e-12);
  }
}
