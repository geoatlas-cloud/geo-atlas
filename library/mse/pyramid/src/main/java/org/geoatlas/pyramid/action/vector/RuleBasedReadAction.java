package org.geoatlas.pyramid.action.vector;

import org.geoatlas.tile.TileRequest;
import org.geotools.data.Query;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;

import java.util.List;
import java.util.Optional;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:42
 * @since: 1.0
 **/
public class RuleBasedReadAction extends AbstractReadAction {

    private final List<RuleExpression> rules;

    public RuleBasedReadAction(List<RuleExpression> rules) {
        this.rules = rules;
    }

    @Override
    protected Query buildQuery(TileRequest request, ReferencedEnvelope bbox, String geometryPropertyName) {
        // hook 自定义规则, 并解析为对应的Filter, 最终构建为Query
        RuleExpression rule = this.obtainRule((int) request.getZ());
        if (rule == null){
            return super.buildQuery(request, bbox, geometryPropertyName);
        }
        Filter ruledFilter = null;
        try {
            ruledFilter = CQL.toFilter(rule.getFilter());
        } catch (CQLException e) {
//            throw new RuntimeException(e);
            log.error(e.getMessage(), e);
        }
        if (ruledFilter == null){
            return super.buildQuery(request, bbox, geometryPropertyName);
        }
        Filter bboxFilter = filterFactory.bbox(filterFactory.property(geometryPropertyName), bbox);
        // 同时使用CQL和BBOX创建组合过滤条件
        Filter combinedFilter = filterFactory.and(ruledFilter, bboxFilter);
        return new Query(request.getLayer(), combinedFilter);
    }

    private RuleExpression obtainRule(int level) {
        Optional<RuleExpression> expression = this.rules.stream().filter(rule -> rule.isMatch(level)).findFirst();
        return expression.orElse(null);
    }
}
