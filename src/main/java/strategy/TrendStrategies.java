package strategy;

import base.Pair;
import base.Triple;

import static indicator.TrendIndicators.*;

/**
 * @author jinfeng.hu  @Date 2022/10/8
 **/
public class TrendStrategies {

    // Chande forecast oscillator strategy.
    public static Action[] ChandeForecastOscillatorStrategy(final ChartBar asset) {
        Action[] actions = new Action[asset.getDatetime().length];

        double[] cfo = ChandeForecastOscillator(asset.getClose());
        for (int i = 0; i < actions.length; i++) {
            if (cfo[i] < 0) {
                actions[i] = Action.BUY;
            } else if (cfo[i] > 0) {
                actions[i] = Action.SELL;
            } else {
                actions[i] = Action.HOLD;
            }
        }

        return actions;
    }

    // Moving chande forecast oscillator strategy function.
    public static Action[] MovingChandeForecastOscillatorStrategy(int period, final ChartBar asset) {
        Action[] actions = new Action[asset.getDatetime().length];

        double[] cfo = MovingChandeForecastOscillator(period, asset.close);

        for (int i = 0; i < actions.length; i++) {
            if (cfo[i] < 0) {
                actions[i] = Action.BUY;
            } else if (cfo[i] > 0) {
                actions[i] = Action.SELL;
            } else {
                actions[i] = Action.HOLD;
            }
        }

        return actions;
    }

    // Make moving chande forecast oscillator strategy.
    public static Strategy MakeMovingChandeForecastOscillatorStrategy(int period) {
        return asset -> MovingChandeForecastOscillatorStrategy(period, asset);
    }

    // The KdjStrategy function uses the k, d, j values that are generated by
    // the Kdj indicator function to provide a BUY action when k crosses
    // above d and j. It is stronger when below 20%. Also the SELL
    // action is when k crosses below d and j. It is strong when
    // above 80%.
    //
    // Returns actions.
    public static Action[] KdjStrategy(int rPeriod, int kPeriod, int dPeriod, final ChartBar asset) {
        Action[] actions = new Action[asset.getDatetime().length];
        Triple<double[], double[], double[]> triple = Kdj(rPeriod, kPeriod, dPeriod, asset.high, asset.low, asset.close);
        double[] k = triple.getLeft(), d = triple.getMiddle(), j = triple.getRight();

        for (int i = 0; i < actions.length; i++) {
            if ((k[i] > d[i]) && (k[i] > j[i]) && (k[i] <= 20)) {
                actions[i] = Action.BUY;
            } else if ((k[i] < d[i]) && (k[i] < j[i]) && (k[i] >= 80)) {
                actions[i] = Action.SELL;
            } else {
                actions[i] = Action.HOLD;
            }
        }

        return actions;
    }

    // Make KDJ strategy function.
    public static Strategy MakeKdjStrategy(int rPeriod, int kPeriod, int dPeriod) {
        return asset -> KdjStrategy(rPeriod, kPeriod, dPeriod, asset);
    }

    // Default KDJ strategy function.
    public static Action[] DefaultKdjStrategy(final ChartBar asset) {
        return KdjStrategy(9, 3, 3, asset);
    }

    // MACD strategy.
    public static Action[] MacdStrategy(final ChartBar asset) {
        Action[] actions = new Action[asset.getDatetime().length];
        Pair<double[], double[]> pair = Macd(asset.close);
        double[] macd = pair.getLeft();
        double[] signal = pair.getRight();

        for (int i = 0; i < actions.length; i++) {
            if (macd[i] > signal[i]) {
                actions[i] = Action.BUY;
            } else if (macd[i] < signal[i]) {
                actions[i] = Action.SELL;
            } else {
                actions[i] = Action.HOLD;
            }
        }

        return actions;
    }

    // Trend strategy. Buy when trending up for count times,
    // sell when trending down for count times.
    public static Action[] TrendStrategy(final ChartBar asset, int count) {
        Action[] actions = new Action[asset.getDatetime().length];

        if (actions.length == 0) {
            return actions;
        }

        double lastClosing = asset.close[0];
        int trendCount = 1;
        boolean trendUp = false;

        actions[0] = Action.HOLD;

        for (int i = 1; i < actions.length; i++) {
            double closing = asset.close[i];

            if (trendUp && (lastClosing <= closing)) {
                trendCount++;
            } else if (!trendUp && (lastClosing >= closing)) {
                trendCount++;
            } else {
                trendUp = !trendUp;
                trendCount = 1;
            }

            lastClosing = closing;

            if (trendCount >= count) {
                if (trendUp) {
                    actions[i] = Action.BUY;
                } else {
                    actions[i] = Action.SELL;
                }
            } else {
                actions[i] = Action.HOLD;
            }
        }

        return actions;
    }

    // Make trend strategy function.
    public static Strategy MakeTrendStrategy(int count) {
        return asset -> TrendStrategy(asset, count);
    }

    // The VwmaStrategy function uses SMA and VWMA indicators to provide
    // a BUY action when VWMA is above SMA, and a SELL signal when VWMA
    // is below SMA, a HOLD signal otherwse.
    //
    // Returns actions
    public static Action[] VwmaStrategy(final ChartBar asset, int period) {
        Action[] actions = new Action[asset.getDatetime().length];

        double[] sma = Sma(period, asset.close);
        double[] vwma = Vwma(period, asset.close, asset.volume);

        for (int i = 0; i < actions.length; i++) {
            if (vwma[i] > sma[i]) {
                actions[i] = Action.BUY;
            } else if (vwma[i] < sma[i]) {
                actions[i] = Action.SELL;
            } else {
                actions[i] = Action.HOLD;
            }
        }

        return actions;
    }

    // Makes a VWMA strategy for the given period.
    public static Strategy MakeVwmaStrategy(int period) {
        return asset -> VwmaStrategy(asset, period);
    }

    // Default VWMA strategy function.
    public static Action[] DefaultVwmaStrategy(final ChartBar asset) {
        return VwmaStrategy(asset, 20);
    }
}
