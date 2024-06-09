package org.geoatlas.metadata.util;

import org.geoatlas.metadata.model.PyramidRuleExpression;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/9 22:17
 * @since: 1.0
 **/
public class PyramidRuleExpressionUtils {

    public static boolean compare(List<PyramidRuleExpression> left, List<PyramidRuleExpression> right) {
        if (CollectionUtils.isEmpty(left) && CollectionUtils.isEmpty(right)) {
            return true;
        }

        if (left.size() == right.size()) {
            return left.equals(right);
        }

        return false;
    }
}
