package org.geoatlas.ogc.tile.config;

import org.geoatlas.cache.core.config.EnableTileCache;
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
@EnableTileCache
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

//    @Bean
    public Pyramid buildRuledSL_FlowpipePyramid() {
        List<RuleExpression> rules = new ArrayList<>();
        rules.add(RuleExpressHelper.buildRule(0,7, "1!=1"));
        rules.add(RuleExpressHelper.buildRule(8,13, "caliber>=600"));
        rules.add(RuleExpressHelper.buildRule(14,15, "caliber>=400"));
        rules.add(RuleExpressHelper.buildRule(16,16, "caliber>=100"));
        return new RuleBasedPyramid(rules);
    }

//    @Bean
    public Pyramid buildRuledOSMLinesPyramid() {
        List<RuleExpression> rules = new ArrayList<>();
        rules.add(RuleExpressHelper.buildRule(0,5, "EXCLUDE"));
        rules.add(RuleExpressHelper.buildRule(6,6, "highway='motorway'"));
        rules.add(RuleExpressHelper.buildRule(7,8, "highway='motorway' or highway='trunk'"));
        rules.add(RuleExpressHelper.buildRule(9,9, "highway='motorway' or highway='trunk' or highway='primary'"));
        rules.add(RuleExpressHelper.buildRule(10,12, "highway='motorway' or highway='trunk' or highway='primary' or highway='secondary'"));
        rules.add(RuleExpressHelper.buildRule(13,15, "highway='motorway' or highway='trunk' or highway='primary' or highway='secondary' or highway='tertiary' " +
                "or highway='motorway_link' or highway='trunk_link' or highway='primary_link_link' or highway='secondary_link' or highway='tertiary_link'"));
        rules.add(RuleExpressHelper.buildRule(16,24, "highway='motorway' or highway='trunk' or highway='primary' or highway='secondary' or highway='tertiary' or highway='unclassified' or highway='residential' or highway='service' " +
                "or highway='motorway_link' or highway='trunk_link' or highway='primary_link_link' or highway='secondary_link' or highway='tertiary_link' or (railway is not null)"));
        return new RuleBasedPyramid(rules);
    }
}
