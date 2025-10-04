#!/usr/bin/env python3

"""
Demonstration script showing the backtesting functionality
that has been implemented in the Java codebase.

This script simulates what the Java backtesting system does:
1. Load historical price series for assets A, B
2. Calculate daily returns  
3. Feed returns to strategy and simulate orders with realistic fills
4. At each day-end, compute portfolio P&L and update return series
5. Run VaR service for portfolio value using last N returns (rolling window)
6. Save daily VaR numbers and include in backtest report
"""

import random
import math
from datetime import datetime, timedelta
from typing import List, Dict, Tuple
import json

# Set random seed for reproducible results
random.seed(42)

class OHLC:
    """Represents OHLC (Open, High, Low, Close) price data"""
    def __init__(self, symbol: str, date: datetime, open_price: float, 
                 high: float, low: float, close: float, volume: int):
        self.symbol = symbol
        self.date = date
        self.open = open_price
        self.high = high
        self.low = low
        self.close = close
        self.volume = volume
    
    def __str__(self):
        return f"OHLC({self.symbol}, {self.date.strftime('%Y-%m-%d')}, " \
               f"O:{self.open:.2f}, H:{self.high:.2f}, L:{self.low:.2f}, C:{self.close:.2f})"

class Portfolio:
    """Represents a trading portfolio with cash and positions"""
    def __init__(self, initial_cash: float):
        self.cash = initial_cash
        self.positions = {}  # symbol -> quantity
        self.average_costs = {}  # symbol -> average cost
    
    def execute_buy_order(self, symbol: str, quantity: int, price: float):
        """Execute a buy order"""
        total_cost = quantity * price
        self.cash -= total_cost
        
        if symbol in self.positions:
            # Update average cost
            current_qty = self.positions[symbol]
            current_avg = self.average_costs[symbol]
            total_value = (current_qty * current_avg) + total_cost
            new_qty = current_qty + quantity
            self.average_costs[symbol] = total_value / new_qty
            self.positions[symbol] = new_qty
        else:
            self.positions[symbol] = quantity
            self.average_costs[symbol] = price
    
    def execute_sell_order(self, symbol: str, quantity: int, price: float):
        """Execute a sell order"""
        total_value = quantity * price
        self.cash += total_value
        
        if symbol in self.positions:
            self.positions[symbol] -= quantity
            if self.positions[symbol] <= 0:
                del self.positions[symbol]
                del self.average_costs[symbol]
    
    def get_portfolio_value(self, current_prices: Dict[str, float]) -> float:
        """Calculate total portfolio value"""
        total_value = self.cash
        
        for symbol, quantity in self.positions.items():
            if symbol in current_prices:
                total_value += quantity * current_prices[symbol]
        
        return total_value

class MarketDataLoader:
    """Loads and generates historical market data"""
    
    @staticmethod
    def generate_sample_data(symbol: str, start_date: datetime, 
                           days: int, start_price: float) -> List[OHLC]:
        """Generate synthetic OHLC data with realistic price movements"""
        data = []
        current_price = start_price
        
        for i in range(days):
            date = start_date + timedelta(days=i)
            
            # Generate realistic price movements (2% daily volatility)
            daily_return = random.gauss(0, 0.02)
            open_price = current_price
            close_price = open_price * (1 + daily_return)
            
            # Generate intraday high/low (1% intraday volatility)
            intraday_vol = abs(random.gauss(0, 0.01))
            high = max(open_price, close_price) * (1 + intraday_vol)
            low = min(open_price, close_price) * (1 - intraday_vol)
            
            # Generate volume
            volume = random.randint(1_000_000, 10_000_000)
            
            ohlc = OHLC(symbol, date, open_price, high, low, close_price, volume)
            data.append(ohlc)
            current_price = close_price
        
        return data
    
    @staticmethod
    def calculate_daily_returns(ohlc_data: List[OHLC]) -> List[float]:
        """Calculate daily returns from OHLC data"""
        returns = [0.0]  # First day has no return
        
        for i in range(1, len(ohlc_data)):
            prev_close = ohlc_data[i-1].close
            curr_close = ohlc_data[i].close
            daily_return = (curr_close - prev_close) / prev_close
            returns.append(daily_return)
        
        return returns

class VaRService:
    """Value-at-Risk calculation service"""
    
    @staticmethod
    def historical_var(returns: List[float], confidence_level: float, 
                      portfolio_value: float) -> float:
        """Calculate Historical VaR"""
        if not returns or portfolio_value <= 0:
            return 0.0
        
        sorted_returns = sorted(returns)
        idx = int((1 - confidence_level) * len(sorted_returns))
        idx = max(0, min(len(sorted_returns) - 1, idx))
        percentile_return = sorted_returns[idx]
        
        return -percentile_return * portfolio_value
    
    @staticmethod
    def parametric_var(returns: List[float], confidence_level: float, 
                      portfolio_value: float) -> float:
        """Calculate Parametric VaR (assumes normal distribution)"""
        if not returns or portfolio_value <= 0:
            return 0.0
        
        mean = sum(returns) / len(returns)
        variance = sum((r - mean) ** 2 for r in returns) / len(returns)
        std_dev = math.sqrt(variance)
        
        # Z-score for confidence level (approximation)
        z_scores = {0.90: 1.28, 0.95: 1.65, 0.99: 2.33}
        z = z_scores.get(confidence_level, 1.65)
        
        return z * std_dev * portfolio_value

class SimpleMovingAverageStrategy:
    """Simple moving average crossover strategy"""
    
    def __init__(self, short_period: int, long_period: int, position_size: int):
        self.short_period = short_period
        self.long_period = long_period
        self.position_size = position_size
        self.is_long = False
    
    def calculate_ma(self, prices: List[float], period: int) -> float:
        """Calculate simple moving average"""
        if len(prices) < period:
            return 0.0
        return sum(prices[-period:]) / period
    
    def generate_signals(self, ohlc_data: List[OHLC]) -> List[Tuple[str, str, int, float]]:
        """Generate trading signals: (action, symbol, quantity, price)"""
        signals = []
        
        if len(ohlc_data) < self.long_period + 1:
            return signals
        
        # Get prices for MA calculation
        prices = [ohlc.close for ohlc in ohlc_data]
        
        # Calculate current and previous MAs
        short_ma = self.calculate_ma(prices, self.short_period)
        long_ma = self.calculate_ma(prices, self.long_period)
        
        prev_prices = prices[:-1]
        prev_short_ma = self.calculate_ma(prev_prices, self.short_period)
        prev_long_ma = self.calculate_ma(prev_prices, self.long_period)
        
        current_ohlc = ohlc_data[-1]
        
        # Detect crossovers
        bullish_crossover = (prev_short_ma <= prev_long_ma) and (short_ma > long_ma)
        bearish_crossover = (prev_short_ma >= prev_long_ma) and (short_ma < long_ma)
        
        if bullish_crossover and not self.is_long:
            signals.append(("BUY", current_ohlc.symbol, self.position_size, current_ohlc.close))
            self.is_long = True
        elif bearish_crossover and self.is_long:
            signals.append(("SELL", current_ohlc.symbol, self.position_size, current_ohlc.close))
            self.is_long = False
        
        return signals

class BacktestEngine:
    """Main backtesting engine"""
    
    def __init__(self):
        self.data_loader = MarketDataLoader()
        self.var_service = VaRService()
    
    def run_backtest(self, assets: List[str], start_date: datetime, 
                    days: int, initial_cash: float, strategy,
                    var_window: int = 30, confidence_level: float = 0.95) -> Dict:
        """
        Run complete backtest with VaR analysis
        
        This method implements the requested workflow:
        1. Load historical price series for assets A, B
        2. Calculate daily returns
        3. Feed returns to strategy and simulate orders with realistic fills
        4. At each day-end, compute portfolio P&L and update return series
        5. Run VaR service for portfolio value using last N returns (rolling window)
        6. Save daily VaR numbers and include in backtest report
        """
        
        print(f"Starting backtest for assets: {assets}")
        print(f"Period: {start_date.strftime('%Y-%m-%d')} for {days} days")
        print(f"Initial cash: ${initial_cash:,.2f}")
        print(f"VaR window: {var_window} days, Confidence: {confidence_level*100}%")
        print("-" * 60)
        
        # Step 1: Load historical price series for assets A, B
        asset_data = {}
        for asset in assets:
            data = self.data_loader.generate_sample_data(asset, start_date, days, 100.0)
            asset_data[asset] = data
            print(f"Loaded {len(data)} records for {asset}")
        
        # Step 2: Calculate daily returns for each asset
        asset_returns = {}
        for asset, data in asset_data.items():
            returns = self.data_loader.calculate_daily_returns(data)
            asset_returns[asset] = returns
            
            # Display sample statistics
            avg_return = sum(returns[1:]) / (len(returns) - 1) if len(returns) > 1 else 0
            volatility = math.sqrt(sum((r - avg_return)**2 for r in returns[1:]) / (len(returns) - 1)) if len(returns) > 2 else 0
            print(f"{asset}: Avg daily return: {avg_return:.4f} ({avg_return*100:.2f}%), Volatility: {volatility:.4f} ({volatility*100:.2f}%)")
        
        print("-" * 60)
        
        # Initialize portfolio and tracking
        portfolio = Portfolio(initial_cash)
        portfolio_values = [initial_cash]
        portfolio_returns = [0.0]
        var_reports = []
        
        # Step 3-6: Main backtesting loop
        for day in range(1, days):
            current_date = start_date + timedelta(days=day)
            
            # Get current market data
            current_prices = {}
            for asset in assets:
                if day < len(asset_data[asset]):
                    current_prices[asset] = asset_data[asset][day].close
            
            # Generate and execute trading signals for primary asset
            primary_asset = assets[0]
            if day < len(asset_data[primary_asset]):
                historical_data = asset_data[primary_asset][:day+1]
                signals = strategy.generate_signals(historical_data)
                
                # Execute orders with realistic fills (add slippage)
                for action, symbol, quantity, price in signals:
                    # Add 0.1% slippage
                    slippage = 0.001
                    if action == "BUY":
                        fill_price = price * (1 + slippage)
                        portfolio.execute_buy_order(symbol, quantity, fill_price)
                        print(f"Day {day}: BUY {quantity} {symbol} at ${fill_price:.2f}")
                    elif action == "SELL":
                        fill_price = price * (1 - slippage)
                        portfolio.execute_sell_order(symbol, quantity, fill_price)
                        print(f"Day {day}: SELL {quantity} {symbol} at ${fill_price:.2f}")
            
            # Step 4: Compute portfolio P&L and update return series
            current_portfolio_value = portfolio.get_portfolio_value(current_prices)
            portfolio_values.append(current_portfolio_value)
            
            # Calculate portfolio return
            prev_value = portfolio_values[-2]
            portfolio_return = (current_portfolio_value - prev_value) / prev_value if prev_value > 0 else 0.0
            portfolio_returns.append(portfolio_return)
            
            # Step 5: Run VaR service using rolling window
            if len(portfolio_returns) >= var_window:
                rolling_returns = portfolio_returns[-var_window:]
                
                historical_var = self.var_service.historical_var(rolling_returns, confidence_level, current_portfolio_value)
                parametric_var = self.var_service.parametric_var(rolling_returns, confidence_level, current_portfolio_value)
                
                var_report = {
                    'date': current_date.strftime('%Y-%m-%d'),
                    'portfolio_value': current_portfolio_value,
                    'historical_var': historical_var,
                    'parametric_var': parametric_var,
                    'historical_var_pct': (historical_var / current_portfolio_value) * 100 if current_portfolio_value > 0 else 0,
                    'parametric_var_pct': (parametric_var / current_portfolio_value) * 100 if current_portfolio_value > 0 else 0,
                    'confidence_level': confidence_level,
                    'window_size': var_window
                }
                var_reports.append(var_report)
            
            # Progress reporting
            if day % 50 == 0 or day == days - 1:
                print(f"Day {day}: Portfolio value: ${current_portfolio_value:,.2f}, "
                      f"Return: {portfolio_return:.4f} ({portfolio_return*100:.2f}%)")
        
        # Step 6: Generate comprehensive backtest report
        final_value = portfolio_values[-1]
        total_return = ((final_value - initial_cash) / initial_cash) * 100
        
        # Calculate max drawdown
        max_value = 0
        max_drawdown = 0
        for value in portfolio_values:
            if value > max_value:
                max_value = value
            drawdown = (max_value - value) / max_value if max_value > 0 else 0
            if drawdown > max_drawdown:
                max_drawdown = drawdown
        
        # Calculate Sharpe ratio
        avg_return = sum(portfolio_returns[1:]) / (len(portfolio_returns) - 1) if len(portfolio_returns) > 1 else 0
        return_variance = sum((r - avg_return)**2 for r in portfolio_returns[1:]) / (len(portfolio_returns) - 1) if len(portfolio_returns) > 2 else 0
        volatility = math.sqrt(return_variance)
        sharpe_ratio = (avg_return * 252) / (volatility * math.sqrt(252)) if volatility > 0 else 0
        
        # Average VaR metrics
        avg_historical_var = sum(r['historical_var'] for r in var_reports) / len(var_reports) if var_reports else 0
        avg_parametric_var = sum(r['parametric_var'] for r in var_reports) / len(var_reports) if var_reports else 0
        
        return {
            'status': 'SUCCESS',
            'initial_value': initial_cash,
            'final_value': final_value,
            'total_return_pct': total_return,
            'max_drawdown_pct': max_drawdown * 100,
            'sharpe_ratio': sharpe_ratio,
            'trading_days': days,
            'portfolio_values': portfolio_values,
            'portfolio_returns': portfolio_returns,
            'var_reports_count': len(var_reports),
            'avg_historical_var': avg_historical_var,
            'avg_parametric_var': avg_parametric_var,
            'daily_var_reports': var_reports[-5:] if var_reports else [],  # Last 5 for display
            'final_portfolio': {
                'cash': portfolio.cash,
                'positions': portfolio.positions,
                'average_costs': portfolio.average_costs
            }
        }

def main():
    """Main demonstration function"""
    print("=" * 80)
    print("BACKTESTING SYSTEM DEMONSTRATION")
    print("=" * 80)
    print()
    
    # Create backtesting engine
    engine = BacktestEngine()
    
    # Create simple moving average strategy
    strategy = SimpleMovingAverageStrategy(
        short_period=10,   # 10-day MA
        long_period=30,    # 30-day MA
        position_size=100  # 100 shares per trade
    )
    
    # Run backtest
    start_date = datetime(2023, 1, 1)
    assets = ["ASSET_A", "ASSET_B"]
    
    results = engine.run_backtest(
        assets=assets,
        start_date=start_date,
        days=252,  # 1 year of trading days
        initial_cash=100000.0,  # $100,000
        strategy=strategy,
        var_window=30,
        confidence_level=0.95
    )
    
    # Display results
    print("\n" + "=" * 80)
    print("BACKTEST RESULTS SUMMARY")
    print("=" * 80)
    print(f"Status: {results['status']}")
    print(f"Initial Value: ${results['initial_value']:,.2f}")
    print(f"Final Value: ${results['final_value']:,.2f}")
    print(f"Total Return: {results['total_return_pct']:.2f}%")
    print(f"Max Drawdown: {results['max_drawdown_pct']:.2f}%")
    print(f"Sharpe Ratio: {results['sharpe_ratio']:.3f}")
    print(f"Trading Days: {results['trading_days']}")
    print(f"VaR Reports Generated: {results['var_reports_count']}")
    
    print("\n" + "-" * 60)
    print("AVERAGE VAR METRICS")
    print("-" * 60)
    print(f"Average Historical VaR: ${results['avg_historical_var']:,.2f}")
    print(f"Average Parametric VaR: ${results['avg_parametric_var']:,.2f}")
    
    print("\n" + "-" * 60)
    print("FINAL PORTFOLIO")
    print("-" * 60)
    final_portfolio = results['final_portfolio']
    print(f"Cash: ${final_portfolio['cash']:,.2f}")
    print(f"Positions: {final_portfolio['positions']}")
    print(f"Average Costs: {final_portfolio['average_costs']}")
    
    if results['daily_var_reports']:
        print("\n" + "-" * 60)
        print("SAMPLE VAR REPORTS (Last 5 Days)")
        print("-" * 60)
        for report in results['daily_var_reports']:
            print(f"Date: {report['date']}")
            print(f"  Portfolio Value: ${report['portfolio_value']:,.2f}")
            print(f"  Historical VaR: ${report['historical_var']:,.2f} ({report['historical_var_pct']:.2f}%)")
            print(f"  Parametric VaR: ${report['parametric_var']:,.2f} ({report['parametric_var_pct']:.2f}%)")
            print()
    
    print("=" * 80)
    print("DEMONSTRATION COMPLETED")
    print("=" * 80)
    print()
    print("This demonstration shows the complete backtesting workflow:")
    print("✓ Load historical price series for assets A, B")
    print("✓ Calculate daily returns")
    print("✓ Feed returns to strategy and simulate orders with realistic fills")
    print("✓ At each day-end, compute portfolio P&L and update return series")
    print("✓ Run VaR service for portfolio value using last N returns (rolling window)")
    print("✓ Save daily VaR numbers and include in backtest report")
    print()
    print("The Java implementation provides the same functionality with:")
    print("- OHLC, Portfolio, Order, BacktestConfig, BacktestResult classes")
    print("- MarketDataLoader for CSV loading and sample data generation")
    print("- TradingStrategy interface with MovingAverageCrossoverStrategy")
    print("- VaRService with Historical, Parametric, and Monte Carlo methods")
    print("- Backtest engine with comprehensive reporting")

if __name__ == "__main__":
    main()