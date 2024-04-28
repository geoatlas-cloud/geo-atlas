package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.model.VirtualViewInfo;
import org.geoatlas.metadata.persistence.repository.FeatureLayerInfoRepository;
import org.geoatlas.metadata.persistence.repository.VirtualViewInfoRepository;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.VirtualTable;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 22:53
 * @since: 1.0
 **/
@Service
public class FeatureLayerInfoManagement {

    private final FeatureLayerInfoRepository repository;

    private final NamespaceInfoManagement namespaceInfoManagement;

    public FeatureLayerInfoManagement(FeatureLayerInfoRepository repository,
                                      NamespaceInfoManagement namespaceInfoManagement) {
        this.repository = repository;
        this.namespaceInfoManagement = namespaceInfoManagement;
    }

    @Transactional
    public void addFeatureLayerInfo(FeatureLayerInfo info) {
        // fetch DataStore, and create virtual table(sql view)
        NamespaceInfo namespaceInfo = namespaceInfoManagement.getNamespaceInfo(info.getNamespaceId());
        if (namespaceInfo == null){
            throw new RuntimeException("namespace not found");
        }
        JDBCDataStore dataStore = (JDBCDataStore) GeoAtlasMetadataContext.getDataStore(namespaceInfo.getName());
        VirtualTable virtualTable = getVirtualTable(info);
        try {
            dataStore.createVirtualTable(virtualTable);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        FeatureLayerInfo saved = repository.save(info);
        GeoAtlasMetadataContext.addFeatureLayerInfo(namespaceInfo.getName(), saved);
    }

    public void removeFeatureLayerInfo(Long id) {
        Optional<FeatureLayerInfo> target = repository.findById(id);
        if (target.isPresent()){
            FeatureLayerInfo featureLayerInfo = target.get();
            NamespaceInfo namespaceInfo = namespaceInfoManagement.getNamespaceInfo(featureLayerInfo.getNamespaceId());
            if (namespaceInfo != null) {
                repository.deleteById(id);
                GeoAtlasMetadataContext.removeFeatureLayerInfo(namespaceInfo.getName(), featureLayerInfo.getName());
            }
        }
    }

    public FeatureLayerInfo getFeatureLayerInfo(Long id) {
        return repository.findById(id).orElse(null);
    }

    private static VirtualTable getVirtualTable(FeatureLayerInfo info) {
        VirtualViewInfo view = info.getView();
        VirtualTable virtualTable = new VirtualTable(view.getName(), view.getSql());
        List<String> prime = Arrays.asList(view.getGeometryColumn().split(","));
        virtualTable.setPrimaryKeyColumns(prime);
        Class<? extends Geometry> geoBinding = Geometry.class;
        if (view.getGeometryType() == 1) {
            geoBinding = Point.class;
        }else if (view.getGeometryType() == 2) {
            geoBinding = LineString.class;
        }else if (view.getGeometryType() == 3) {
            geoBinding = Polygon.class;
        }
        virtualTable.addGeometryMetadatata(view.getGeometryColumn(), geoBinding, view.getSrid());
        return virtualTable;
    }
}
