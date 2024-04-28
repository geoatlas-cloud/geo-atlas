package org.geoatlas.metadata;

import org.geoatlas.metadata.model.DataStoreInfo;
import org.geoatlas.metadata.model.FeatureLayerInfo;
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

    public static DataStore addDataStore(String namespace, DataStoreInfo dataStoreInfo) {
        return DATA_STORE_CACHE.computeIfAbsent(namespace, store -> {
            DataStore dataStore = null;
            try {
                dataStore = DataStoreFactory.createDataStore(dataStoreInfo);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return dataStore;
        });
    }

    public static void removeDataStore(String namespace) {
        DataStore dataStore = DATA_STORE_CACHE.get(namespace);
        if (dataStore != null) {
            try {
                dataStore.dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            DATA_STORE_CACHE.remove(namespace);
        }
    }

    public static DataStore getDataStore(String namespace) {
        return DATA_STORE_CACHE.get(namespace);
    }

    public static DataStore getDataStore(String namespace, DataStoreInfo dataStoreInfo) {
        DataStore dataStore = DATA_STORE_CACHE.get(namespace);
        if (dataStore == null) {
            dataStore = addDataStore(namespace, dataStoreInfo);
        }
        return dataStore;
    }

    public static void addFeatureLayerInfo(String namespace, FeatureLayerInfo featureLayerInfo) {
        String identifier = identifier(namespace, featureLayerInfo.getName());
        FEATURE_LAYER_INFO_CACHE.put(identifier, featureLayerInfo);
    }

    public static FeatureLayerInfo getFeatureLayerInfo(String namespace, String layerName) {
        return FEATURE_LAYER_INFO_CACHE.get(identifier(namespace, layerName));
    }

    public static void removeFeatureLayerInfo(String namespace, String layerName) {
        FEATURE_LAYER_INFO_CACHE.remove(identifier(namespace, layerName));
    }

    private static String identifier(String namespace, String layerName) {
        return namespace + DEFAULT_SPLIT_CHAR + layerName;
    }
}
