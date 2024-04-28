package org.geoatlas.ogc.tile.config;

import org.geoatlas.pyramid.ClassicPyramid;
import org.geoatlas.pyramid.Pyramid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @Bean
    public Pyramid buildPyramid() {
        return new ClassicPyramid();
    }
}
