package org.geoatlas.ogc.tile.config;

import org.geoatlas.pyramid.ClassicPyramid;
import org.geoatlas.pyramid.Pyramid;
import org.geoatlas.pyramid.RuleBasedPyramid;
import org.geoatlas.pyramid.action.vector.RuleExpressHelper;
import org.geoatlas.pyramid.action.vector.RuleExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 15:22
 * @since: 1.0
 **/
@Configuration
public class ApplicationConfig {

    /**
     * 先搞一个全局的金字塔, 方便测试, 后续再做图层独立金字塔
     * @return
     */
//    @Bean
    public Pyramid buildPyramid() {
        return new ClassicPyramid();
    }
//    @Bean
    public Pyramid buildRuledPyramid() {
        List<RuleExpression> rules = new ArrayList<>();
        rules.add(RuleExpressHelper.buildRule(0,5, "grade=1"));
        rules.add(RuleExpressHelper.buildRule(6,12, "grade=1 or grade =2"));
        rules.add(RuleExpressHelper.buildRule(13,24, "grade=3"));
        return new RuleBasedPyramid(rules);
    }

    @Bean
    public Pyramid buildRuledSL_FlowpipePyramid() {
        List<RuleExpression> rules = new ArrayList<>();
        rules.add(RuleExpressHelper.buildRule(0,7, "1!=1"));
        rules.add(RuleExpressHelper.buildRule(8,13, "caliber>=600"));
        rules.add(RuleExpressHelper.buildRule(14,15, "caliber>=400"));
        rules.add(RuleExpressHelper.buildRule(16,16, "caliber>=100"));
        return new RuleBasedPyramid(rules);
    }
}
