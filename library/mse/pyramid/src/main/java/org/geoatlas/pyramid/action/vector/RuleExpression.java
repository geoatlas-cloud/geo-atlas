package org.geoatlas.pyramid.action.vector;

import java.io.Serializable;
import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/26 17:03
 * @since: 1.0
 **/
public class RuleExpression implements Serializable {
    private static final long serialVersionUID = 4975222324454721664L;

    private List<Integer> levels;

    // 默认将使用and连接, 即 select ** from ** where filter and bbox_filter
    private String filter;

    public RuleExpression(List<Integer> levels, String filter) {
        this.levels = levels;
        this.filter = filter;
    }

    public boolean isMatch(int level) {
        return levels.contains(level);
    }

    public String getFilter() {
        return filter;
    }
}
