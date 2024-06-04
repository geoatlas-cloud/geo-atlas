package org.geoatlas.ogc.tile.config;

import org.geoatlas.cache.core.config.EnableTileCache;
import org.geoatlas.cache.core.seed.SeederThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 15:22
 * @since: 1.0
 **/
//@EnableTileCache
@Configuration
public class ApplicationConfig {


    @Bean
    public SeederThreadPoolExecutor seederThreadPoolExecutor() {
        return new SeederThreadPoolExecutor(16, 32);
    }
}
