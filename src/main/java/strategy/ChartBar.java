package strategy;

import lombok.Data;

/**
 * Description: chart bar
 *
 * @author Jinfeng.hu  @Date 2022-10-06
 **/
@Data
public class ChartBar {
    public String[] datetime;
    public double[] open;
    public double[] close;
    public double[] high;
    public double[] low;
    public double[] volume;
}
