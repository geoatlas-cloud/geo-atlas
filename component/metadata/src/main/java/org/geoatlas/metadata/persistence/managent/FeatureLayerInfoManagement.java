package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.context.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.*;
import org.geoatlas.metadata.persistence.repository.FeatureLayerInfoRepository;
import org.geoatlas.metadata.persistence.repository.SpatialReferenceInfoRepository;
import org.geotools.data.DataStore;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.VirtualTable;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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

    private final SpatialReferenceInfoRepository spatialReferenceInfoRepository;

    private final DataStoreInfoManagement dataStoreInfoManagement;

    public FeatureLayerInfoManagement(FeatureLayerInfoRepository repository,
                                      NamespaceInfoManagement namespaceInfoManagement,
                                      SpatialReferenceInfoRepository spatialReferenceInfoRepository,
                                      DataStoreInfoManagement dataStoreInfoManagement) {
        this.repository = repository;
        this.namespaceInfoManagement = namespaceInfoManagement;
        this.spatialReferenceInfoRepository = spatialReferenceInfoRepository;
        this.dataStoreInfoManagement = dataStoreInfoManagement;
    }

    @Transactional
    public void addFeatureLayerInfo(FeatureLayerInfo info) {
        // fetch DataStore, and create virtual table(sql view)
        NamespaceInfo namespaceInfo = namespaceInfoManagement.getNamespaceInfo(info.getNamespaceId());
        if (namespaceInfo == null){
            throw new RuntimeException("namespace not found");
        }
        if (repository.existsByNamespaceIdAndName(info.getNamespaceId(), info.getName())) {
            throw new RuntimeException("feature layer already exists");
        }
        JDBCDataStore dataStore = (JDBCDataStore) this.dataStoreInfoManagement.getDataStore(namespaceInfo.getName(), info);
        VirtualTable virtualTable = getVirtualTable(info);
        try {
            dataStore.createVirtualTable(virtualTable);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        FeatureLayerInfo saved = repository.save(info);
        GeoAtlasMetadataContext.addFeatureLayerInfo(namespaceInfo.getName(), saved);
        cacheCoordinateReferenceSystem(saved);
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

    public FeatureLayerInfo findByNameSpaceIdAndName(Long namespaceId, String name) {
        return repository.findFirstByNamespaceIdAndName(namespaceId, name);
    }

    public static VirtualTable getVirtualTable(FeatureLayerInfo info) {
        VirtualViewInfo view = info.getView();
        VirtualTable virtualTable = new VirtualTable(view.getName(), view.getSql());
        List<String> prime = Arrays.asList(view.getPkColumns().split(","));
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

    private void cacheCoordinateReferenceSystem(FeatureLayerInfo info) {
        Optional<SpatialReferenceInfo> target = spatialReferenceInfoRepository.findById(info.getSpatialReferenceId());
        if (target.isPresent()) {
            SpatialReferenceInfo spatialReferenceInfo = target.get();
            try {
                CoordinateReferenceSystem coordinateReferenceSystem = CRS.parseWKT(spatialReferenceInfo.getWktText());
                GeoAtlasMetadataContext.addCoordinateReferenceSystem(spatialReferenceInfo.getId(), coordinateReferenceSystem);
            } catch (FactoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
