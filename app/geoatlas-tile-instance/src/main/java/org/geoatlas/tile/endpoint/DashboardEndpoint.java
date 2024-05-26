package org.geoatlas.tile.endpoint;

import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.persistence.managent.DataStoreInfoManagement;
import org.geoatlas.metadata.persistence.managent.FeatureLayerInfoManagement;
import org.geoatlas.metadata.persistence.managent.NamespaceInfoManagement;
import org.geoatlas.metadata.persistence.managent.SpatialReferenceInfoManagement;
import org.geoatlas.tile.model.DashboardTotalCount;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/21 18:29
 * @since: 1.0
 **/
@RestController
@RequestMapping("/v1/dashboard")
public class DashboardEndpoint {

    private final NamespaceInfoManagement namespaceInfoManagement;
    private final DataStoreInfoManagement dataStoreInfoManagement;
    private final FeatureLayerInfoManagement featureLayerInfoManagement;
    private final SpatialReferenceInfoManagement spatialReferenceInfoManagement;

    public DashboardEndpoint(NamespaceInfoManagement namespaceInfoManagement,
                             DataStoreInfoManagement dataStoreInfoManagement,
                             FeatureLayerInfoManagement featureLayerInfoManagement,
                             SpatialReferenceInfoManagement spatialReferenceInfoManagement) {
        this.namespaceInfoManagement = namespaceInfoManagement;
        this.dataStoreInfoManagement = dataStoreInfoManagement;
        this.featureLayerInfoManagement = featureLayerInfoManagement;
        this.spatialReferenceInfoManagement = spatialReferenceInfoManagement;
    }

    @GetMapping("/count")
    public DashboardTotalCount getTotalCount() {
        long namespaceCount = namespaceInfoManagement.getTotalCount();
        long dataStoreCount = dataStoreInfoManagement.getTotalCount();
        long featureLayerCount = featureLayerInfoManagement.getTotalCount();
        long spatialReferenceCount = spatialReferenceInfoManagement.getTotalCount();
        return new DashboardTotalCount(namespaceCount, dataStoreCount, featureLayerCount, spatialReferenceCount);
    }

    @GetMapping("/layers/recent")
    public List<FeatureLayerInfo> getRecentFeatureLayerInfos() {
        return featureLayerInfoManagement.findRecentFeatureLayerInfo();
    }

    @GetMapping("/layers/active")
    public List<FeatureLayerInfo> getActiveFeatureLayerInfos() {
        return featureLayerInfoManagement.findRecentFeatureLayerInfo();
    }
}
