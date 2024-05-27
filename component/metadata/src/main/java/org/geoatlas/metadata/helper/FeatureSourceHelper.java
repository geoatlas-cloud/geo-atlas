package org.geoatlas.metadata.helper;

import org.geoatlas.metadata.context.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.DataStoreInfo;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.model.SpatialReferenceInfo;
import org.geoatlas.metadata.persistence.managent.DataStoreInfoManagement;
import org.geoatlas.metadata.persistence.managent.FeatureLayerInfoManagement;
import org.geoatlas.metadata.persistence.managent.NamespaceInfoManagement;
import org.geoatlas.metadata.persistence.managent.SpatialReferenceInfoManagement;
import org.geoatlas.metadata.persistence.repository.SpatialReferenceInfoRepository;
import org.geotools.data.DataStore;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 15:14
 * @since: 1.0
 **/
@Component
public class FeatureSourceHelper {

    private final NamespaceInfoManagement namespaceInfoManagement;
    private final DataStoreInfoManagement dataStoreInfoManagement;
    private final FeatureLayerInfoManagement featureLayerInfoManagement;
    private final SpatialReferenceInfoManagement spatialReferenceInfoManagement;

    public FeatureSourceHelper(DataStoreInfoManagement dataStoreInfoManagement, FeatureLayerInfoManagement featureLayerInfoManagement,
                               SpatialReferenceInfoManagement spatialReferenceInfoManagement, NamespaceInfoManagement namespaceInfoManagement) {
        this.dataStoreInfoManagement = dataStoreInfoManagement;
        this.featureLayerInfoManagement = featureLayerInfoManagement;
        this.spatialReferenceInfoManagement = spatialReferenceInfoManagement;
        this.namespaceInfoManagement = namespaceInfoManagement;
    }

    public FeatureSourceWrapper getFeatureSource(String namespace, String layerName) {
        // read namespace info
        NamespaceInfo namespaceInfo = getNamespaceInfo(namespace);

        // read feature layer info 
        FeatureLayerInfo featureLayerInfo = getFeatureLayerInfo(namespace, layerName, namespaceInfo);

        // read crs
        CoordinateReferenceSystem coordinateReferenceSystem = this.spatialReferenceInfoManagement.getCoordinateReferenceSystem(featureLayerInfo.getSpatialReferenceId());

        // read datastore
        DataStore dataStore = this.dataStoreInfoManagement.getDataStore(featureLayerInfo);

        try {
            // FIXME: 2024/4/28 考虑统一都是用LayerName?不用这个复杂?
            return new FeatureSourceWrapper(dataStore.getFeatureSource(featureLayerInfo.getView().getName()),
                    coordinateReferenceSystem, featureLayerInfo.getRules());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public FeatureLayerInfo getFeatureLayerInfo(String namespace, String layerName, NamespaceInfo namespaceInfo) {
        FeatureLayerInfo featureLayerInfo = GeoAtlasMetadataContext.getFeatureLayerInfo(namespace, layerName);
        if (featureLayerInfo == null) {
            featureLayerInfo = this.featureLayerInfoManagement.findByNameSpaceIdAndName(namespaceInfo.getId(), layerName);
            if (featureLayerInfo == null) {
                throw new RuntimeException("FeatureLayerInfo not found");
            }
            GeoAtlasMetadataContext.addFeatureLayerInfo(namespace, featureLayerInfo);
        }
        return featureLayerInfo;
    }

    public NamespaceInfo getNamespaceInfo(String namespace) {
        NamespaceInfo namespaceInfo = GeoAtlasMetadataContext.getNamespace(namespace);
        if (namespaceInfo == null) {
            namespaceInfo = this.namespaceInfoManagement.findByName(namespace);
            if (namespaceInfo == null) {
                throw new RuntimeException("Namespace not found");
            }
            GeoAtlasMetadataContext.addNamespace(namespaceInfo);
        }
        return namespaceInfo;
    }
            

}
