package org.geoatlas.cache.core.config;

import org.geoatlas.cache.core.endpoint.TileSeedEndpoint;
import org.geoatlas.cache.core.seed.SeederThreadPoolExecutor;
import org.geoatlas.cache.core.seed.TileBreeder;
import org.geoatlas.cache.core.service.DefaultTileSeedService;
import org.geoatlas.cache.core.service.TileSeedService;
import org.geoatlas.cache.core.source.DefaultTileSource;
import org.geoatlas.cache.core.source.TileSource;
import org.geoatlas.cache.core.storage.StorageBroker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/5 14:18
 * @since: 1.0
 **/
@ConditionalOnProperty(value = "geo-atlas.cache.enabled", havingValue = "true", matchIfMissing = true)
@Import(StorageBrokerRegister.class)
public class GeoAtlasCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = SeederThreadPoolExecutor.class)
    public SeederThreadPoolExecutor seederThreadPoolExecutor() {
        return new SeederThreadPoolExecutor(16, 32);
    }

    @Bean
    @ConditionalOnMissingBean(value = TileSource.class)
    public TileSource tileSource() {
        return new DefaultTileSource();
    }

    @Bean
    @ConditionalOnMissingBean(value = TileBreeder.class)
    public TileBreeder tileBreeder(StorageBroker storageBroker, TileSource tileSource, SeederThreadPoolExecutor stpe) {
        return new TileBreeder(tileSource, storageBroker, stpe);
    }

    @Bean
    @ConditionalOnMissingBean(value = TileSeedService.class)
    public TileSeedService tileSeedService(TileBreeder tileBreeder){
        return new DefaultTileSeedService(tileBreeder);
    }

    @Bean
    @ConditionalOnMissingBean(value = TileSeedEndpoint.class)
    public TileSeedEndpoint tileSeedEndpoint(TileSeedService tileSeedService) {
        return new TileSeedEndpoint(tileSeedService);
    }
}
