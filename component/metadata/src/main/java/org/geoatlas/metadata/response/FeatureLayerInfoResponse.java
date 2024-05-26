package org.geoatlas.metadata.response;

import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.SpatialReferenceInfo;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/24 16:44
 * @since: 1.0
 **/
public class FeatureLayerInfoResponse extends FeatureLayerInfo {
    private SpatialReferenceInfo spatialReferenceInfo;

    public FeatureLayerInfoResponse(FeatureLayerInfo info, SpatialReferenceInfo spatialReferenceInfo) {
        this.setId(info.getId());
        this.setName(info.getName());
        this.setDescription(info.getDescription());
        this.setView(info.getView());
        this.setNamespaceId(info.getNamespaceId());
        this.setSpatialReferenceId(info.getSpatialReferenceId());
        this.setDatastoreId(info.getDatastoreId());
        this.setModified(info.getModified());
        this.setCreated(info.getCreated());
        this.setSpatialReferenceId(info.getSpatialReferenceId());
        this.setSpatialReferenceInfo(spatialReferenceInfo);
    }

    public SpatialReferenceInfo getSpatialReferenceInfo() {
        return spatialReferenceInfo;
    }

    public void setSpatialReferenceInfo(SpatialReferenceInfo spatialReferenceInfo) {
        this.spatialReferenceInfo = spatialReferenceInfo;
    }
}
