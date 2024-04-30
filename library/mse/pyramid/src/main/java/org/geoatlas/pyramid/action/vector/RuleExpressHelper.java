package org.geoatlas.pyramid.action.vector;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/26 17:29
 * @since: 1.0
 **/
public class RuleExpressHelper {

    public static RuleExpression buildRule(int minLevel, int maxLevel, String filter) {
        return new RuleExpression(buildLevels(minLevel, maxLevel), filter);
    }

    private static List<Integer> buildLevels(int minLevel, int maxLevel) {
        if (minLevel == maxLevel) {
            return Stream.of(minLevel).collect(Collectors.toList());
        }
        return Stream.iterate(minLevel, i -> i + 1).limit(maxLevel - minLevel + 1).collect(Collectors.toList());
    }
}
