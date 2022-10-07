package strategy;

import indicator.Action;
import indicator.ChartBar;

/**
 * 策略接口
 *
 * @author jinfeng.hu  @Date 2022-10-06
 **/
public interface IStrategy {
    // run strategy
    Action[] run(final ChartBar chartBar);
}