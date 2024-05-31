package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.context.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.*;
import org.geoatlas.metadata.persistence.repository.FeatureLayerInfoRepository;
import org.geoatlas.metadata.response.FeatureLayerInfoResponse;
import org.geoatlas.metadata.response.PageContent;
import org.geoatlas.metadata.response.ResponseStatus;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.VirtualTable;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 22:53
 * @since: 1.0
 **/
@Service
public class FeatureLayerInfoManagement {

    private final FeatureLayerInfoRepository repository;

    private final NamespaceInfoManagement namespaceInfoManagement;

    private final DataStoreInfoManagement dataStoreInfoManagement;

    private final SpatialReferenceInfoManagement spatialReferenceInfoManagement;

    private final CoordinateReferenceSystem wgs_84;
    private final CoordinateReferenceSystem webMercator;

    public FeatureLayerInfoManagement(FeatureLayerInfoRepository repository,
                                      NamespaceInfoManagement namespaceInfoManagement,
                                      DataStoreInfoManagement dataStoreInfoManagement,
                                      SpatialReferenceInfoManagement spatialReferenceInfoManagement) {
        this.repository = repository;
        this.namespaceInfoManagement = namespaceInfoManagement;
        this.dataStoreInfoManagement = dataStoreInfoManagement;
        this.spatialReferenceInfoManagement = spatialReferenceInfoManagement;
        try {
            this.wgs_84 = CRS.decode("EPSG:4326", true);
            this.webMercator = CRS.decode("EPSG:3857", true);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
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
        JDBCDataStore dataStore = (JDBCDataStore) this.dataStoreInfoManagement.getDataStore(info);
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

    @Transactional
    public void updateFeatureLayerInfo(FeatureLayerInfo info) {
        Optional<FeatureLayerInfo> old = repository.findById(info.getId());
        if (old.isPresent()){
            NamespaceInfo namespaceInfo = namespaceInfoManagement.getNamespaceInfo(info.getNamespaceId());
            if (namespaceInfo == null){
                throw new RuntimeException("namespace not found");
            }
            handleMutations(old.get(), info);
            repository.save(info);
        }else {
            throw new RuntimeException("feature layer not found");
        }
    }

    /**
     * 突变内容处理
     * @param last
     * @param current
     */
    private void handleMutations(FeatureLayerInfo last, FeatureLayerInfo current) {
        if (!last.getName().equals(current.getName())) {
            if (repository.existsByNamespaceIdAndName(current.getNamespaceId(), current.getName())) {
                throw new RuntimeException("feature layer already exists");
            }
        }
        
        if (last.getSpatialReferenceId() != current.getSpatialReferenceId()) {
            // FIXME: 2024/5/25 移除旧的CRS
            cacheCoordinateReferenceSystem(current);
        }

        // FIXME: 2024/5/26 build event and push, 可能需要外部系统自行处理缓存等问题
    }

    public void removeFeatureLayerInfo(Long id) {
        Optional<FeatureLayerInfo> target = repository.findById(id);
        if (target.isPresent()){
            FeatureLayerInfo featureLayerInfo = target.get();
            NamespaceInfo namespaceInfo = namespaceInfoManagement.getNamespaceInfo(featureLayerInfo.getNamespaceId());
            if (namespaceInfo != null) {
                repository.deleteById(id);
                GeoAtlasMetadataContext.removeFeatureLayerInfo(namespaceInfo.getName(), featureLayerInfo.getName());
                // FIXME: 2024/5/25 还应该同步移除Datastore中的virtualView实例
            }
        }
    }

    public FeatureLayerInfo getFeatureLayerInfo(Long id) {
        return repository.findById(id).orElse(null);
    }

    public FeatureLayerPreviewInfo getFeatureLayerPreviewInfo(Long id) {
        FeatureLayerInfo featureLayer = getFeatureLayerInfo(id);
        if (featureLayer ==  null) {
            throw new RuntimeException(ResponseStatus.NOT_FOUND.getMessage());
        }
        NamespaceInfo namespace = namespaceInfoManagement.getNamespaceInfo(featureLayer.getNamespaceId());
        if (namespace == null) {
            throw new RuntimeException("Namespace not found.");
        }
        FeatureLayerPreviewInfo previewInfo = new FeatureLayerPreviewInfo(featureLayer.getId(), featureLayer.getName(), namespace.getName());
        FeatureBBoxInfo bbox = featureLayer.getBbox();
        if (bbox != null) {
            previewInfo.setZoom(7);
            if (!bbox.getNatived()) {
                Envelope envelope = new Envelope(bbox.getMinx(), bbox.getMaxx(), bbox.getMiny(), bbox.getMaxy());
                Coordinate coordinate = JTS.toGeometry(envelope).getInteriorPoint().getCoordinate();
                previewInfo.setBbox(bbox);
                previewInfo.setCenter(coordinate);
            }else {
                CoordinateReferenceSystem nativeCrs = null;
                if (featureLayer.getSpatialReferenceId() != null){
                    nativeCrs = spatialReferenceInfoManagement.getCoordinateReferenceSystem(featureLayer.getSpatialReferenceId());
                }else {
                    try {
                        nativeCrs = CRS.decode("EPSG:" + featureLayer.getView().getSrid(), true);
                    } catch (FactoryException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (nativeCrs == null) {
                    throw new RuntimeException("Can not fetch the featureLayer`s native crs");
                }
                ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(bbox.getMinx(), bbox.getMaxx(), bbox.getMiny(), bbox.getMaxy(), nativeCrs);
                try {
                    ReferencedEnvelope transform = referencedEnvelope.transform(wgs_84, true);
                    FeatureBBoxInfo transformedBBox = new FeatureBBoxInfo();
                    transformedBBox.setId(bbox.getId());
                    transformedBBox.setFeatureLayerId(bbox.getFeatureLayerId());
                    transformedBBox.setMinx(transform.getMinX());
                    transformedBBox.setMiny(transform.getMinY());
                    transformedBBox.setMaxx(transform.getMaxX());
                    transformedBBox.setMaxy(transform.getMaxY());
                    previewInfo.setBbox(transformedBBox);
                    previewInfo.setCenter(JTS.toGeometry(transform).getCoordinate());
                } catch (TransformException e) {
                    throw new RuntimeException(e);
                } catch (FactoryException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return previewInfo;
    }

    public FeatureLayerInfo findByNameSpaceIdAndName(Long namespaceId, String name) {
        return repository.findFirstByNamespaceIdAndName(namespaceId, name);
    }

    public long getTotalCount() {
        return this.repository.count();
    }

    public List<FeatureLayerInfo> findRecentFeatureLayerInfo() {
        // JPA 中,page是从0开始,不是从1开始
        Page<FeatureLayerInfo> recent = repository.findAll(PageRequest.of(0, 5, Sort.Direction.DESC, "created"));
        return recent.toList();
    }

    public Page<FeatureLayerInfo> findAll(PageRequest request) {
        return repository.findAll(request);
    }

    public Page<FeatureLayerInfo> findAllByNamespaceId(Long namespaceId, PageRequest request) {
        return repository.findAllByNamespaceId(namespaceId, request);
    }

    public PageContent<FeatureLayerInfoResponse> pageFeatureLayerInfo(String name, PageRequest request) {
        Page<FeatureLayerInfo> page;
        if (name != null){
            page = repository.findAllByNameContaining(name, request);
        }else {
            page = repository.findAll(request);
        }
        if (page == null) {
            throw new RuntimeException("page not found");
        }
        List<FeatureLayerInfo> content = page.getContent();
        List<FeatureLayerInfoResponse> filled = content.stream()
                .map(item -> buildFeatureLayerInfoResponse(item))
                .collect(Collectors.toList());
        return new PageContent<>(filled, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private FeatureLayerInfoResponse buildFeatureLayerInfoResponse(FeatureLayerInfo item) {
        if (item.getSpatialReferenceId() != null){
            return new FeatureLayerInfoResponse(item,
                    spatialReferenceInfoManagement.getSpatialReferenceInfo(item.getSpatialReferenceId()));
        }else {
            return new FeatureLayerInfoResponse(item, null);
        }
    }

    /**
     *
     * @param info
     * @return
     */
    public static VirtualTable getVirtualTable(FeatureLayerInfo info) {
        VirtualViewInfo view = info.getView();
        // FIXME: 2024/5/24 后续应去除view 中的 name, 直接用featureLayer中的name即可. 目前是仍然保留
        VirtualTable virtualTable = new VirtualTable(view.getName(), view.getSql());
        List<String> prime = Arrays.stream(view.getPkColumns().split(",")).filter(StringUtils::hasText).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(prime)) {
            virtualTable.setPrimaryKeyColumns(prime);
        }
        Class<? extends Geometry> geoBinding = Geometry.class;
        /*
         * 这里的类型会涉及到后续的类型转换, @see org.geotools.jdbc.SQLDialect.convertValue
         * 如果设置有问题, 那么会导致数据类型转换异常, 错误等
         * <p>
         * 那么, 不是很清楚，或者目前Layer存在混合类型的情况下, 直接给Geometry.class
         *
         */
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
        if (info.getSpatialReferenceId() != null) {
            Optional<SpatialReferenceInfo> target = spatialReferenceInfoManagement.findById(info.getSpatialReferenceId());
            if (target.isPresent()) {
                SpatialReferenceInfo spatialReferenceInfo = target.get();
                try {
                    CoordinateReferenceSystem coordinateReferenceSystem = CRS.parseWKT(spatialReferenceInfo.getWktText());
                    GeoAtlasMetadataContext.addCoordinateReferenceSystem(spatialReferenceInfo.getId(), coordinateReferenceSystem);
                    GeoAtlasMetadataContext.addSpatialReferenceInfo(spatialReferenceInfo.getId(), spatialReferenceInfo);
                } catch (FactoryException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
