package org.geoatlas.cache.core.service;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.seed.SeedRequest;
import org.geoatlas.cache.core.seed.TileBreeder;
import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.pyramid.index.TileMatrixSetContext;
import org.geoatlas.pyramid.index.TileMatrixSubset;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/4 21:34
 * @since: 1.0
 **/
public abstract class AbstractTileSeedService implements TileSeedService{

    private TileBreeder breeder;

    public AbstractTileSeedService(TileBreeder breeder) {
        this.breeder = breeder;
    }

    public void doSeed(SeedRequest request) throws GeoAtlasCacheException {
        TileMatrixSet matrixSet = TileMatrixSetContext.getTileMatrixSet(request.getMatrixSetId());
        if (matrixSet == null) {
            throw new GeoAtlasCacheException("TileMatrixSet not found");
        }
        breeder.seed(request, getSubset(request, matrixSet));
    }

    /**
     * 必须实现获取subset的方法
     * @param request
     * @param matrixSet
     * @return
     */
    protected abstract TileMatrixSubset getSubset(SeedRequest request, TileMatrixSet matrixSet) throws GeoAtlasCacheException;
}
