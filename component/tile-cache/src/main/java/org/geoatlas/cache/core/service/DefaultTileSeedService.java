package org.geoatlas.cache.core.service;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.seed.SeedRequest;
import org.geoatlas.cache.core.seed.TileBreeder;
import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.pyramid.index.TileMatrixSubset;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/5 14:14
 * @since: 1.0
 **/
public class DefaultTileSeedService extends AbstractTileSeedService{

    public DefaultTileSeedService(TileBreeder breeder) {
        super(breeder);
    }

    @Override
    protected TileMatrixSubset getSubset(SeedRequest request, TileMatrixSet matrixSet) throws GeoAtlasCacheException {
        // 由于目前并不存在如Tile Layer的概念, 所以在Cache模块里面无法获取到FeatureLayer的TileMatrixSubset, 所以这里直接返回null,
        // 那么将会使用 request中给定的matrixSetId 来创建 TileMatrixSubset(也就是是一个bbox是与TileMatrixSet相同的TileMatrixSubset)
        // 继承方可以自行实现该接口, 从而注入FeatureLayer的TileMatrixSubset
        return null;
    }
}
