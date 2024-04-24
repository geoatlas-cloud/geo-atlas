package org.geoatlas.pyramid.action.vector;

import org.geoatlas.tile.TileRequest;
import org.geotools.data.Query;
import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:42
 * @since: 1.0
 **/
public class RuleBasedReadAction extends AbstractReadAction {

    @Override
    protected Query buildQuery(TileRequest request, ReferencedEnvelope bbox, String geometryPropertyName) {
        // hook 自定义规则, 并解析为对应的Filter, 最终构建为Query
        return super.buildQuery(request, bbox, geometryPropertyName);
    }
}
