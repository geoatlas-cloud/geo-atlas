package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.FeatureLayerInfo;
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

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 22:53
 * @since: 1.0
 **/
@Service
public class FeatureLayerInfoManagement {

    private final FeatureLayerInfoRepository repository;
    private final VirtualViewInfoRepository viewInfoRepository;

    public FeatureLayerInfoManagement(FeatureLayerInfoRepository repository, VirtualViewInfoRepository viewInfoRepository) {
        this.repository = repository;
        this.viewInfoRepository = viewInfoRepository;
    }

    @Transactional
    public void addFeatureLayerInfo(FeatureLayerInfo info) {
        // fetch DataStore, and create virtual table(sql view)
        JDBCDataStore dataStore = (JDBCDataStore) GeoAtlasMetadataContext.getDataStore(info.getStoreInfo().getId());
        VirtualTable virtualTable = getVirtualTable(info);
        try {
            dataStore.createVirtualTable(virtualTable);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        VirtualViewInfo viewInfo = viewInfoRepository.save(info.getView());
        // 其会回带ID, 所以重新设置
        info.setView(viewInfo);
        FeatureLayerInfo saved = repository.save(info);
        GeoAtlasMetadataContext.addFeatureLayerInfo(saved);
    }

    public void removeFeatureLayerInfo(Long id) {
        repository.deleteById(id);
        GeoAtlasMetadataContext.removeDataStore(id);
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
