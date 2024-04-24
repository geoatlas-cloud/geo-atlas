package org.geoatlas.pyramid;

import org.geoatlas.metadata.FeatureLayerInfo;
import org.geoatlas.metadata.GeoAtlasMetadataContext;
import org.geoatlas.pyramid.action.ActionPipeline;
import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;
import org.geotools.data.DataStore;

import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 17:04
 * @since: 1.0
 **/
public abstract class AbstractPyramid implements Pyramid{

    private ActionPipeline pipeline;

    protected AbstractPyramid(ActionPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public TileObject getTile(TileRequest request) throws IOException {
        FeatureLayerInfo featureLayerInfo = GeoAtlasMetadataContext.getFeatureLayerInfo(request.getNamespace(), request.getLayer());
        if (featureLayerInfo == null) {
            throw new RuntimeException("FeatureLayerInfo not found");
        }
        DataStore dataStore = GeoAtlasMetadataContext.getDataStore(featureLayerInfo.getStoreInfo().getIdentifier());
        if (dataStore == null) {
            throw new RuntimeException("DataStore not found");
        }
        return pipeline.doAction(request, dataStore.getFeatureSource(request.getLayer()));
    }
}
