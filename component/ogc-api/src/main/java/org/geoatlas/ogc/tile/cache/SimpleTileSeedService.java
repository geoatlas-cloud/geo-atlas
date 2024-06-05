package org.geoatlas.ogc.tile.cache;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.seed.SeedRequest;
import org.geoatlas.cache.core.seed.TileBreeder;
import org.geoatlas.cache.core.service.AbstractTileSeedService;
import org.geoatlas.cache.core.storage.StorageBroker;
import org.geoatlas.metadata.helper.FeatureSourceHelper;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.ogc.tile.context.FeatureTileMatrixSubsetContext;
import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.pyramid.index.TileMatrixSubset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/4 21:55
 * @since: 1.0
 **/
@Component
@ConditionalOnBean(value = StorageBroker.class)
public class SimpleTileSeedService extends AbstractTileSeedService {

    private final FeatureTileMatrixSubsetContext subsetContext;
    private final FeatureSourceHelper featureSourceHelper;

    public SimpleTileSeedService(TileBreeder breeder, FeatureTileMatrixSubsetContext subsetContext,
                                 FeatureSourceHelper featureSourceHelper) {
        super(breeder);
        this.subsetContext = subsetContext;
        this.featureSourceHelper = featureSourceHelper;
    }

    @Override
    protected TileMatrixSubset getSubset(SeedRequest request, TileMatrixSet matrixSet) throws GeoAtlasCacheException {
        NamespaceInfo namespaceInfo = featureSourceHelper.getNamespaceInfo(request.getNamespace());
        if (namespaceInfo == null) {
            throw new GeoAtlasCacheException("namespace not found");
        }
        FeatureLayerInfo featureLayerInfo = featureSourceHelper.getFeatureLayerInfo(request.getNamespace(), request.getLayerName(), namespaceInfo);
        if (featureLayerInfo == null){
            throw new GeoAtlasCacheException("layer not found");
        }
        return subsetContext.getTileMatrixSubset(featureLayerInfo, matrixSet);
    }
}
