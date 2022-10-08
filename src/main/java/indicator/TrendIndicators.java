package indicator;

import base.Tree;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import static indicator.Helper.*;

/**
 * @author jinfeng.hu  @Date 2022-10-07
 **/
public class TrendIndicators {
    private static final double psarAfStep = 0.02;
    private static final double psarAfMax = 0.20;

    // The AbsolutePriceOscillator function calculates a technical indicator that is used
    // to follow trends. APO crossing above zero indicates bullish, while crossing below
    // zero indicates bearish. Positive value is upward trend, while negative value is
    // downward trend.
    //
    // Fast = Ema(fastPeriod, values)
    // Slow = Ema(slowPeriod, values)
    // APO = Fast - Slow
    //
    // Returns apo.
    public static double[] absolutePriceOscillator(int fastPeriod, int slowPeriod, double[] values) {
        double[] fast = ema(fastPeriod, values);
        double[] slow = ema(slowPeriod, values);
        double[] apo = subtract(fast, slow);

        return apo;
    }

    // The DefaultAbsolutePriceOscillator function calculates APO with the most
    // frequently used fast and short periods are 14 and 30.
    //
    // Returns apo.
    public static double[] defaultAbsolutePriceOscillator(double[] values) {
        return absolutePriceOscillator(14, 30, values);
    }

    // Aroon Indicator. It is a technical indicator that is used to identify trend changes
    // in the price of a stock, as well as the strength of that trend. It consists of two
    // lines, Arron Up, and Aroon Down. The Aroon Up line measures the strength of the
    // uptrend, and the Aroon Down measures the strength of the downtrend. When Aroon Up
    // is above Aroon Down, it indicates bullish price, and when Aroon Down is above
    // Aroon Up, it indicates bearish price.
    //
    // Aroon Up = ((25 - Period Since Last 25 Period High) / 25) * 100
    // Aroon Down = ((25 - Period Since Last 25 Period Low) / 25) * 100
    //
    // Returns aroonUp, aroonDown
    public static Pair<double[], double[]> aroon(double[] high, double[] low) {
        checkSameSize(high, low);

        int[] sinceLastHigh25 = since(max(25, high));
        int[] sinceLastLow25 = since(min(25, low));
        double[] aroonUp = new double[high.length];
        double[] aroonDown = new double[high.length];

        for (int i = 0; i < aroonUp.length; i++) {
            aroonUp[i] = ((25 - sinceLastHigh25[i]) / 25.000) * 100;
            aroonDown[i] = ((25 - sinceLastLow25[i]) / 25.000) * 100;
        }

        return Pair.of(aroonUp, aroonDown);
    }

    // The BalanceOfPower function calculates the strength of buying and selling
    // pressure. Positive value indicates an upward trend, and negative value
    // indicates a downward trend. Zero indicates a balance between the two.
    //
    // BOP = (Closing - Opening) / (High - Low)
    //
    // Returns bop.
    public static double[] balanceOfPower(double[] opening, double[] high, double[] low, double[] closing) {
        double[] bop = divide(subtract(closing, opening), subtract(high, low));
        return bop;
    }

    // The Chande Forecast Oscillator developed by Tushar Chande The Forecast
    // Oscillator plots the percentage difference between the closing price and
    // the n-period linear regression forecasted price. The oscillator is above
    // zero when the forecast price is greater than the closing price and less
    // than zero if it is below.
    //
    // R = Linreg(Closing)
    // CFO = ((Closing - R) / Closing) * 100
    //
    // Returns cfo.
    public static double[] chandeForecastOscillator(double[] closing) {
        double[] x = generateNumbers(0, closing.length, 1);
        double[] r = Regression.linearRegressionUsingLeastSquare(x, closing);
        double[] cfo = multiplyBy(divide(subtract(closing, r), closing), 100);

        return cfo;
    }

    // The Community Channel Index (CMI) is a momentum-based oscillator
    // used to help determine when an investment vehicle is reaching a
    // condition of being overbought or oversold.
    //
    // Moving Average = Sma(Period, Typical Price)
    // Mean Deviation = Sma(Period, Abs(Typical Price - Moving Average))
    // CMI = (Typical Price - Moving Average) / (0.015 * Mean Deviation)
    //
    // Returns cmi.
    public static double[] communityChannelIndex(int period, double[] high, double[] low, double[] closing) {
        double[] tp = typicalPrice(low, high, closing).getLeft();
        double[] ma = sma(period, tp);
        double[] md = sma(period, abs(subtract(tp, ma)));
        double[] cci = divide(subtract(tp, ma), multiplyBy(md, 0.015));
        cci[0] = 0;

        return cci;
    }

    // The default community channel index with the period of 20.
    public static double[] defaultCommunityChannelIndex(double[] high, double[] low, double[] closing) {
        return communityChannelIndex(20, high, low, closing);
    }

    // Dema calculates the Double Exponential Moving Average (DEMA).
    //
    // DEMA = (2 * EMA(values)) - EMA(EMA(values))
    //
    // Returns dema.
    public static double[] dema(int period, double[] values) {
        double[] ema1 = ema(period, values);
        double[] ema2 = ema(period, ema1);
        double[] dema = subtract(multiplyBy(ema1, 2), ema2);

        return dema;
    }

    // Exponential Moving Average (EMA).
    public static double[] ema(int period, double[] values) {
        double[] result = new double[values.length];

        double k = 2.00 / (1 + period);
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                result[i] = (values[i] * k) + (result[i - 1] * (1 - k));
            } else {
                result[i] = values[i];
            }
        }

        return result;
    }

    // Moving Average Convergence Divergence (MACD).
    //
    // MACD = 12-Period EMA - 26-Period EMA.
    // Signal = 9-Period EMA of MACD.
    //
    // Returns MACD, signal.
    public static Pair<double[], double[]> macd(double[] close) {
        double[] ema12 = ema(12, close);
        double[] ema26 = ema(26, close);
        double[] macd = subtract(ema12, ema26);
        double[] signal = ema(9, macd);

        return Pair.of(macd, signal);
    }

    // The Mass Index (MI) uses the high-low range to identify trend reversals
    // based on range expansions.
    //
    // Singe EMA = EMA(9, Highs - Lows)
    // Double EMA = EMA(9, Single EMA)
    // Ratio = Single EMA / Double EMA
    // MI = Sum(25, Ratio)
    //
    // Returns mi.
    public static double[] massIndex(double[] high, double[] low) {
        double[] ema1 = ema(9, subtract(high, low));
        double[] ema2 = ema(9, ema1);
        double[] ratio = divide(ema1, ema2);
        double[] mi = sum(25, ratio);

        return mi;
    }

    // Moving Chande Forecast Oscillator calculates based on
    // the given period.
    //
    // The Chande Forecast Oscillator developed by Tushar Chande The Forecast
    // Oscillator plots the percentage difference between the closing price and
    // the n-period linear regression forecasted price. The oscillator is above
    // zero when the forecast price is greater than the closing price and less
    // than zero if it is below.
    //
    // R = Linreg(Closing)
    // CFO = ((Closing - R) / Closing) * 100
    //
    // Returns cfo.
    public static double[] movingChandeForecastOscillator(int period, double[] closing) {
        double[] x = generateNumbers(0, closing.length, 1);
        double[] r = Regression.movingLinearRegressionUsingLeastSquare(period, x, closing);

        double[] cfo = multiplyBy(divide(subtract(closing, r), closing), 100);

        return cfo;
    }

    // Moving max for the given period.
    public static double[] max(int period, double[] values) {
        double[] result = new double[values.length];
        double[] buffer = new double[period];
        Tree bst = Tree.New();
        for (int i = 0; i < values.length; i++) {
            bst.insert(values[i]);

            if (i >= period) {
                bst.remove(buffer[i % period]);
            }

            buffer[i % period] = values[i];
            result[i] = bst.max().doubleValue();
        }

        return result;
    }

    // Moving min for the given period.
    public static double[] min(int period, double[] values) {
        double[] result = new double[values.length];
        double[] buffer = new double[period];
        Tree bst = Tree.New();

        for (int i = 0; i < values.length; i++) {
            bst.insert(values[i]);

            if (i >= period) {
                bst.remove(buffer[i % period]);
            }

            buffer[i % period] = values[i];
            result[i] = bst.min().doubleValue();
        }

        return result;
    }

    // Parabolic SAR. It is a popular technical indicator for identifying the trend
    // and as a trailing stop.
    //
    // PSAR = PSAR[i - 1] - ((PSAR[i - 1] - EP) * AF)
    //
    // If the trend is Falling:
    //  - PSAR is the maximum of PSAR or the previous two high values.
    //  - If the current high is greather than or equals to PSAR, use EP.
    //
    // If the trend is Rising:
    //  - PSAR is the minimum of PSAR or the previous two low values.
    //  - If the current low is less than or equals to PSAR, use EP.
    //
    // If PSAR is greater than the closing, trend is falling, and the EP
    // is set to the minimum of EP or the low.
    //
    // If PSAR is lower than or equals to the closing, trend is rising, and the EP
    // is set to the maximum of EP or the high.
    //
    // If the trend is the same, and AF is less than 0.20, increment it by 0.02.
    // If the trend is not the same, set AF to 0.02.
    //
    // Based on video https://www.youtube.com/watch?v=MuEpGBAH7pw&t=0s.
    //
    // Returns psar, trend
    public static Pair<double[], Trend[]> ParabolicSar(double[] high, double[] low, double[] closing) {
        checkSameSize(high, low);

        Trend[] trend = new Trend[high.length];
        double[] psar = new double[high.length];

        double af, ep;

        trend[0] = Trend.Falling;
        psar[0] = high[0];
        af = psarAfStep;
        ep = low[0];

        for (int i = 1;
             i < psar.length;
             i++) {
            psar[i] = psar[i - 1] - ((psar[i - 1] - ep) * af);

            if (trend[i - 1] == Trend.Falling) {
                psar[i] = Math.max(psar[i], high[i - 1]);
                if (i > 1) {
                    psar[i] = Math.max(psar[i], high[i - 2]);
                }

                if (high[i] >= psar[i]) {
                    psar[i] = ep;
                }
            } else {
                psar[i] = Math.min(psar[i], low[i - 1]);
                if (i > 1) {
                    psar[i] = Math.min(psar[i], low[i - 2]);
                }

                if (low[i] <= psar[i]) {
                    psar[i] = ep;
                }
            }

            double prevEp = ep;

            if (psar[i] > closing[i]) {
                trend[i] = Trend.Falling;
                ep = Math.min(ep, low[i]);
            } else {
                trend[i] = Trend.Rising;
                ep = Math.max(ep, high[i]);
            }

            if (trend[i] != trend[i - 1]) {
                af = psarAfStep;
            } else if (prevEp != ep && af < psarAfMax) {
                af += psarAfStep;
            }
        }

        return Pair.of(psar, trend);
    }

    // The Qstick function calculates the ratio of recent up and down bars.
    //
    // QS = Sma(Closing - Opening)
    //
    // Returns qs.
    public static double[] qstick(int period, double[] opening, double[] closing) {
        double[] qs = sma(period, subtract(closing, opening));
        return qs;
    }

    // The Kdj function calculates the KDJ  indicator, also known as
    // the Random Index. KDJ is calculated similar to the Stochastic
    // Oscillator with the difference of having the J line. It is
    // used to analyze the trend and entry points.
    //
    // The K and D lines show if the asset is overbought when they
    // crosses above 80%, and oversold when they crosses below
    // 20%. The J line represents the divergence.
    //
    //
    // RSV = ((Closing - Min(Low, rPeriod))
    //       / (Max(High, rPeriod) - Min(Low, rPeriod))) * 100
    // K = Sma(RSV, kPeriod)
    // D = Sma(K, dPeriod)
    // J = (3 * K) - (2 * D)
    //
    // Returns k, d, j.
    public static Triple<double[], double[], double[]> kdj(int rPeriod, int kPeriod, int dPeriod,
                                                           double[] high, double[] low, double[] closing) {
        double[] highest = max(rPeriod, high);
        double[] lowest = min(rPeriod, low);

        double[] rsv = multiplyBy(divide(subtract(closing, lowest), subtract(highest, lowest)), 100);

        double[] k = sma(kPeriod, rsv);
        double[] d = sma(dPeriod, k);
        double[] j = subtract(multiplyBy(k, 3), multiplyBy(d, 2));

        return Triple.of(k, d, j);
    }

    // The DefaultKdj function calculates KDJ based on default periods
    // consisting of rPeriod of 9, kPeriod of 3, and dPeriod of 3.
    //
    // Returns k, d, j.
    public static Triple<double[], double[], double[]> defaultKdj(double[] high, double[] low, double[] closing) {
        return kdj(9, 3, 3, high, low, closing);
    }

    // Rolling Moving Average (RMA).
    //
    // R[0] to R[p-1] is SMA(values)
    // R[p] and after is R[i] = ((R[i-1]*(p-1)) + v[i]) / p
    //
    // Returns r.
    public static double[] rma(int period, double[] values) {
        double[] result = new double[values.length];
        double sum = 0.00;

        for (int i = 0; i < values.length; i++) {
            int count = i + 1;

            if (i < period) {
                sum += values[i];
            } else {
                sum = result[i - 1] * (period - 1) + values[i];
                count = period;
            }

            result[i] = sum / count;
        }

        return result;
    }

    // Simple Moving Average (SMA).
    public static double[] sma(int period, double[] values) {
        double[] result = new double[values.length];
        double sum = 0.00;

        for (int i = 0; i < values.length; i++) {
            int count = i + 1;
            sum += values[i];

            if (i >= period) {
                sum -= values[i - period];
                count = period;
            }

            result[i] = sum / count;
        }

        return result;
    }

    // Since last values change.
    public static int[] since(double[] values) {
        int[] result = new int[values.length];

        double lastValue = 0.000; // TODO
        int sinceLast = 0;

        for (int i = 0; i < values.length; i++) {
            double value = values[i];

            if (value != lastValue) {
                lastValue = value;
                sinceLast = 0;
            } else {
                sinceLast++;
            }

            result[i] = sinceLast;
        }

        return result;
    }

    // Moving sum for the given period.
    public static double[] sum(int period, double[] values) {
        double[] result = new double[values.length];
        double sum = 0.0;

        for (int i = 0; i < values.length; i++) {
            sum += values[i];
            if (i >= period) {
                sum -= values[i - period];
            }
            result[i] = sum;
        }

        return result;
    }

    // Tema calculates the Triple Exponential Moving Average (TEMA).
    //
    // TEMA = (3 * EMA1) - (3 * EMA2) + EMA3
    // EMA1 = EMA(values)
    // EMA2 = EMA(EMA1)
    // EMA3 = EMA(EMA2)
    //
    // Returns tema.
    public static double[] tema(int period, double[] values) {
        double[] ema1 = ema(period, values);
        double[] ema2 = ema(period, ema1);
        double[] ema3 = ema(period, ema2);

        double[] tema = add(subtract(multiplyBy(ema1, 3), multiplyBy(ema2, 3)), ema3);

        return tema;
    }

    // Trima function calculates the Triangular Moving Average (TRIMA).
    //
    // If period is even:
    //   TRIMA = SMA(period / 2, SMA((period / 2) + 1, values))
    // If period is odd:
    //   TRIMA = SMA((period + 1) / 2, SMA((period + 1) / 2, values))
    //
    // Returns trima.
    public static double[] trima(int period, double[] values) {
        int n1, n2;

        if (period % 2 == 0) {
            n1 = period / 2;
            n2 = n1 + 1;
        } else {
            n1 = (period + 1) / 2;
            n2 = n1;
        }

        double[] trima = sma(n1, sma(n2, values));
        return trima;
    }

    // Triple Exponential Average (TRIX) indicator is an oscillator used to
    // identify oversold and overbought markets, and it can also be used
    // as a momentum indicator. Like many oscillators, TRIX oscillates
    // around a zero line.
    //
    // EMA1 = EMA(period, values)
    // EMA2 = EMA(period, EMA1)
    // EMA3 = EMA(period, EMA2)
    // TRIX = (EMA3 - Previous EMA3) / Previous EMA3
    //
    // Returns trix.
    public static double[] trix(int period, double[] values) {
        double[] ema1 = ema(period, values);
        double[] ema2 = ema(period, ema1);
        double[] ema3 = ema(period, ema2);
        double[] previous = shiftRightAndFillBy(1, ema3[0], ema3);
        double[] trix = divide(subtract(ema3, previous), previous);

        return trix;
    }

    // Typical Price. It is another approximation of average price for each
    // period and can be used as a filter for moving average systems.
    //
    // Typical Price = (High + Low + Closing) / 3
    //
    // Returns typical price, 20-Period SMA.
    public static Pair<double[], double[]> typicalPrice(double[] low, double[] high, double[] closing) {
        checkSameSize(high, low, closing);
        double[] sma20 = sma(20, closing);
        double[] ta = new double[closing.length];

        for (int i = 0; i < ta.length; i++) {
            ta[i] = (high[i] + low[i] + closing[i]) / 3;
        }

        return Pair.of(ta, sma20);
    }

    // Vortex Indicator. It provides two oscillators that capture positive and
    // negative trend movement. A bullish signal triggers when the positive
    // trend indicator crosses above the negative trend indicator or a key
    // level. A bearish signal triggers when the negative trend indicator
    // crosses above the positive trend indicator or a key level.
    //
    // +VM = Abs(Current High - Prior Low)
    // -VM = Abd(Current Low - Prior High)
    //
    // +VM14 = 14-Period Sum of +VM
    // -VM14 = 14-Period Sum of -VM
    //
    // TR = Max((High[i]-Low[i]), Abs(High[i]-Closing[i-1]), Abs(Low[i]-Closing[i-1]))
    // TR14 = 14-Period Sum of TR
    //
    // +VI14 = +VM14 / TR14
    // -VI14 = -VM14 / TR14
    //
    // Based on https://school.stockcharts.com/doku.php?id=technical_indicators:vortex_indicator
    //
    // Returns plusVi, minusVi
    public static Pair<double[], double[]> vortex(double[] high, double[] low, double[] closing) {
        checkSameSize(high, low, closing);

        int period = 14;

        double[] plusVi = new double[high.length];
        double[] minusVi = new double[high.length];
        double[] plusVm = new double[period];
        double[] minusVm = new double[period];
        double[] tr = new double[period];

        double plusVmSum = 0, minusVmSum = 0, trSum = 0;

        for (int i = 0; i < high.length; i++) {
            int j = i % period;

            plusVmSum -= plusVm[j];
            plusVm[j] = Math.abs(high[i] - low[i - 1]);
            plusVmSum += plusVm[j];

            minusVmSum -= minusVm[j];
            minusVm[j] = Math.abs(low[i] - high[i - 1]);
            minusVmSum += minusVm[j];

            double highLow = high[i] - low[i];
            double highPrevClosing = Math.abs(high[i] - closing[i - 1]);
            double lowPrevClosing = Math.abs(low[i] - closing[i - 1]);

            trSum -= tr[j];
            tr[j] = Math.max(highLow, Math.max(highPrevClosing, lowPrevClosing));
            trSum += tr[j];

            plusVi[i] = plusVmSum / trSum;
            minusVi[i] = minusVmSum / trSum;
        }

        return Pair.of(plusVi, minusVi);
    }

    // The Vwma function calculates the Volume Weighted Moving Average (VWMA)
    // averaging the price data with an emphasis on volume, meaning areas
    // with higher volume will have a greater weight.
    //
    // VWMA = Sum(Price * Volume) / Sum(Volume) for a given Period.
    //
    // Returns vwma
    public static double[] vwma(int period, double[] closing, long[] volume) {
        double[] floatVolume = asDouble(volume);
        double[] vwma = divide(sum(period, multiply(closing, floatVolume)), sum(period, floatVolume));

        return vwma;
    }

    // The DefaultVwma function calculates VWMA with a period of 20.
    public static double[] defaultVwma(double[] closing, long[] volume) {
        return vwma(20, closing, volume);
    }

}
