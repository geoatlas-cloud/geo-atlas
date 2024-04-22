package org.geoatlas.metadata;

import org.geotools.data.DataStore;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 14:40
 * @since: 1.0
 **/
public class GeoAtlasMetadataContext {

    private static final Map<String, DataStore> DATA_STORE_CACHE = new ConcurrentHashMap<>();

    private static final Map<String, FeatureLayerInfo> FEATURE_LAYER_INFO_CACHE = new ConcurrentHashMap<>();

    private static final String DEFAULT_SPLIT_CHAR = ":";

    public static DataStore addDataStore(DataStoreInfo dataStoreInfo) {
        return DATA_STORE_CACHE.computeIfAbsent(dataStoreInfo.getIdentifier(), store -> {
            DataStore dataStore = null;
            try {
                dataStore = DataStoreFactory.createDataStore(dataStoreInfo);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return dataStore;
        });
    }

    public static DataStore getDataStore(String identifier) {
        return DATA_STORE_CACHE.get(identifier);
    }

    public static DataStore getDataStore(DataStoreInfo dataStoreInfo) {
        DataStore dataStore = DATA_STORE_CACHE.get(dataStoreInfo.getIdentifier());
        if (dataStore == null) {
            dataStore = addDataStore(dataStoreInfo);
        }
        return dataStore;
    }

    public static void addFeatureLayerInfo(FeatureLayerInfo featureLayerInfo) {
        String identifier = identifier(featureLayerInfo.getNamespace().getName(), featureLayerInfo.getName());
        FEATURE_LAYER_INFO_CACHE.put(identifier, featureLayerInfo);
    }

    public static FeatureLayerInfo getFeatureLayerInfo(String namespace, String layerName) {
        return FEATURE_LAYER_INFO_CACHE.get(identifier(namespace, layerName));
    }

    private static String identifier(String namespace, String layerName) {
        return namespace + DEFAULT_SPLIT_CHAR + layerName;
    }
}
