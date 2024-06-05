package org.geoatlas.cache.core.service;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.seed.SeedRequest;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/4 23:26
 * @since: 1.0
 **/
public interface TileSeedService {
    void doSeed(SeedRequest request) throws GeoAtlasCacheException;
}
