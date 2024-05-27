package org.geoatlas.pyramid;

import org.geoatlas.pyramid.action.vector.RuleExpression;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/27 16:31
 * @since: 1.0
 **/
public class PyramidFactory {

    private static final Map<Integer, Pyramid> PYRAMID_CONTEXT = new ConcurrentHashMap<>();

    private static final Pyramid DEFAULT_PYRAMID = new ClassicPyramid();

    public static final Pyramid buildPyramid(List<RuleExpression> expressions) {
        if (null == expressions || expressions.size() == 0) {
            return DEFAULT_PYRAMID;
        }
        int index = expressions.hashCode();
        return PYRAMID_CONTEXT.computeIfAbsent(index, key -> new RuleBasedPyramid(expressions));
    }
}
