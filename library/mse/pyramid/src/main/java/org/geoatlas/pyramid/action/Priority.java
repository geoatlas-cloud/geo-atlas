package org.geoatlas.pyramid.action;

import static org.geoatlas.pyramid.action.PriorityConstant.DEFAULT_PRIORITY;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 9:45
 * @since: 1.0
 **/
public interface Priority {
    /**
     * 获取权值, 默认为 100
     *
     * @return
     */
    default int obtainPriority() {
        return DEFAULT_PRIORITY;
    }
}
