package org.geoatlas.metadata.context;

import org.geoatlas.metadata.DataStoreFactory;
import org.geoatlas.metadata.model.DataStoreInfo;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.model.SpatialReferenceInfo;
import org.geotools.data.DataStore;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 14:40
 * @since: 1.0
 **/
public class GeoAtlasMetadataContext {

    private static final Map<String, NamespaceInfo> NAMESPACE_INFO_CACHE = new ConcurrentHashMap<>();

    private static final Map<Long, DataStore> DATA_STORE_CACHE = new ConcurrentHashMap<>();

    private static final Map<String, FeatureLayerInfo> FEATURE_LAYER_INFO_CACHE = new ConcurrentHashMap<>();

    private static final Map<Long, CoordinateReferenceSystem> COORDINATE_REFERENCE_SYSTEM_CACHE = new ConcurrentHashMap<>();
    private static final Map<Long, SpatialReferenceInfo> SPATIAL_REFERENCE_INFO_SYSTEM_CACHE = new ConcurrentHashMap<>();

    private static final String DEFAULT_SPLIT_CHAR = ":";

    public static void addNamespace(NamespaceInfo namespace) {
        NAMESPACE_INFO_CACHE.put(namespace.getName(), namespace);
    }

    public static NamespaceInfo getNamespace(String namespace) {
        return NAMESPACE_INFO_CACHE.get(namespace);
    }

    public static DataStore addDataStore(DataStoreInfo storeInfo) {
        return DATA_STORE_CACHE.computeIfAbsent(storeInfo.getId(), store -> {
            DataStore dataStore = null;
            try {
                dataStore = DataStoreFactory.createDataStore(storeInfo);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return dataStore;
        });
    }

    public static void removeDataStore(Long identifier) {
        DataStore dataStore = DATA_STORE_CACHE.get(identifier);
        if (dataStore != null) {
            try {
                dataStore.dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            DATA_STORE_CACHE.remove(identifier);
        }
    }

    public static DataStore getDataStore(Long identifier) {
        return DATA_STORE_CACHE.get(identifier);
    }

    public static DataStore getDataStore(DataStoreInfo storeInfo) {
        DataStore dataStore = DATA_STORE_CACHE.get(storeInfo.getId());
        if (dataStore == null) {
            dataStore = addDataStore(storeInfo);
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

    public static CoordinateReferenceSystem getCoordinateReferenceSystem(Long spatialReferenceId) {
        return COORDINATE_REFERENCE_SYSTEM_CACHE.get(spatialReferenceId);
    }

    public static void addCoordinateReferenceSystem(Long spatialReferenceId, CoordinateReferenceSystem coordinateReferenceSystem) {
        COORDINATE_REFERENCE_SYSTEM_CACHE.put(spatialReferenceId, coordinateReferenceSystem);
    }

    public static SpatialReferenceInfo getSpatialReferenceInfo(Long spatialReferenceId) {
        return SPATIAL_REFERENCE_INFO_SYSTEM_CACHE.get(spatialReferenceId);
    }

    public static void addSpatialReferenceInfo(Long spatialReferenceId, SpatialReferenceInfo spatialReferenceInfo) {
        SPATIAL_REFERENCE_INFO_SYSTEM_CACHE.put(spatialReferenceId, spatialReferenceInfo);
    }

    private static String identifier(String namespace, String layerName) {
        return namespace + DEFAULT_SPLIT_CHAR + layerName;
    }
}
