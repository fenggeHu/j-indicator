package strategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 组合策略
 *
 * @author fengge.hu  @Date 2022-10-06
 **/
public class AllStrategy implements Strategy {
    // 一组策略
    public static Strategy create(Strategy... all) {
        return new AllStrategy(all);
    }

    private Strategy[] all;

    public AllStrategy(Strategy... all) {
        this.all = all;
    }

    @Override
    public Action[] run(final ChartBar chartBar) {
        if (null == all || all.length == 0) {
            return null;
        }
        List<Action[]> actions = new ArrayList<>(all.length);
        for (Strategy is : all) {
            Action[] ac = is.run(chartBar);
            actions.add(ac);
        }
        for (int i = 1; i < actions.size(); i++) {
            for (int j = 0; j < actions.get(0).length; j++) {
                if (actions.get(0)[j] != actions.get(i)[j]) {
                    actions.get(0)[j] = Action.HOLD;
                }
            }
        }

        return actions.get(0);
    }
}
