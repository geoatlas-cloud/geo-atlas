package org.geoatlas.metadata;

import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureSource;

import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 15:14
 * @since: 1.0
 **/
public class FeatureSourceHelper {

    public static SimpleFeatureSource getFeatureSource(String namespace, String layerName) {
        FeatureLayerInfo featureLayerInfo = GeoAtlasMetadataContext.getFeatureLayerInfo(namespace, layerName);
        if (featureLayerInfo == null) {
            throw new RuntimeException("FeatureLayerInfo not found");
        }
        DataStore dataStore = GeoAtlasMetadataContext.getDataStore(namespace);
        if (dataStore == null) {
            throw new RuntimeException("DataStore not found");
        }
        try {
            // FIXME: 2024/4/28 考虑统一都是用LayerName?不用这个复杂?
            return dataStore.getFeatureSource(featureLayerInfo.getView().getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
