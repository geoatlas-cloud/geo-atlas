package org.geoatlas.pyramid;

import org.geoatlas.pyramid.action.ActionPipeline;
import org.geoatlas.pyramid.action.vector.RuleBasedReadAction;
import org.geoatlas.pyramid.action.vector.RuleExpression;

import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 17:04
 * @since: 1.0
 **/
public class RuleBasedPyramid extends AbstractPyramid{

    public RuleBasedPyramid(List<RuleExpression> rules) {
        super(new ActionPipeline(new RuleBasedReadAction(rules)));
    }

    // FIXME: 2024/4/26 后续要提供rule update的通道(且更新完成后需要通过event通知相关方操作, 比如tile cache. 当然生成和推送event不是我的职责, 我只是提醒)
}
