package org.geoatlas.ogc.tile.context;

import org.geoatlas.metadata.helper.FeatureBBoxHelper;
import org.geoatlas.metadata.model.FeatureBBoxInfo;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.pyramid.index.BoundingBox;
import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.pyramid.index.TileMatrixSubset;
import org.geoatlas.pyramid.index.TileMatrixSubsetFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/4 21:56
 * @since: 1.0
 **/
@Component
public class FeatureTileMatrixSubsetContext {

    private final FeatureBBoxHelper featureBBoxHelper;

    public FeatureTileMatrixSubsetContext(FeatureBBoxHelper featureBBoxHelper) {
        this.featureBBoxHelper = featureBBoxHelper;
    }

    private final static Map<Integer, TileMatrixSubset> FEATURE_MATRIX_SUBSET_CACHE = new ConcurrentHashMap<>();

    public TileMatrixSubset getTileMatrixSubset(FeatureLayerInfo featureLayerInfo, TileMatrixSet tileMatrixSet) {
        FeatureBBoxInfo bbox = featureLayerInfo.getBbox();
        if (bbox == null){
            return FEATURE_MATRIX_SUBSET_CACHE.computeIfAbsent(Objects.hash(tileMatrixSet.getTitle(), tileMatrixSet.getExtent()), key -> TileMatrixSubsetFactory.createTileMatrixSubset(tileMatrixSet));
        }else {
            return FEATURE_MATRIX_SUBSET_CACHE.computeIfAbsent(Objects.hash(tileMatrixSet.getTitle(), bbox), key -> {
                FeatureBBoxInfo transformed = featureBBoxHelper.toOther(featureLayerInfo, tileMatrixSet.getCrs());
                return TileMatrixSubsetFactory.createTileMatrixSubset(tileMatrixSet, new BoundingBox(transformed.getMinx(), transformed.getMiny(), transformed.getMaxx(), transformed.getMaxy()));
            });
        }
        // FIXME: 2024/6/4 再做一次容错? 如果创建失败还是给默认?
    }

}
