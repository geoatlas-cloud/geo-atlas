package org.geoatlas.metadata.helper;

import org.geoatlas.metadata.model.FeatureBBoxInfo;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.persistence.managent.SpatialReferenceInfoManagement;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/4 9:30
 * @since: 1.0
 **/
@Component
public class FeatureBBoxHelper {

    public final CoordinateReferenceSystem wgs_84;
    public final CoordinateReferenceSystem webMercator;
    
    private final SpatialReferenceInfoManagement spatialReferenceInfoManagement;

    public FeatureBBoxHelper(SpatialReferenceInfoManagement spatialReferenceInfoManagement){
        this.spatialReferenceInfoManagement = spatialReferenceInfoManagement;
        try {
            this.wgs_84 = CRS.decode("EPSG:4326", true);
            this.webMercator = CRS.decode("EPSG:3857", true);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }


    public FeatureBBoxInfo toWgs84(FeatureLayerInfo layer) {
        return getFeatureBBoxInfo(layer, wgs_84);
    }

    public FeatureBBoxInfo toOther(FeatureLayerInfo layer, CoordinateReferenceSystem other) {
        return getFeatureBBoxInfo(layer, other);
    }

    private FeatureBBoxInfo getFeatureBBoxInfo(FeatureLayerInfo layer, CoordinateReferenceSystem other) {
        ReferencedEnvelope defaultEnvelop = getDefaultEnvelop(layer);
        if (defaultEnvelop == null) {
            return null;
        }
        try {
            defaultEnvelop = defaultEnvelop.transform(other, true);
        } catch (TransformException | FactoryException e) {
            throw new RuntimeException(e);
        }
        return buildFeatureBBox(layer, defaultEnvelop);
    }

    private static FeatureBBoxInfo buildFeatureBBox(FeatureLayerInfo layer, ReferencedEnvelope defaultEnvelop) {
        FeatureBBoxInfo transformedBBox = new FeatureBBoxInfo();
        FeatureBBoxInfo bbox = layer.getBbox();
        transformedBBox.setId(bbox.getId());
        transformedBBox.setFeatureLayerId(bbox.getFeatureLayerId());
        transformedBBox.setMinx(defaultEnvelop.getMinX());
        transformedBBox.setMiny(defaultEnvelop.getMinY());
        transformedBBox.setMaxx(defaultEnvelop.getMaxX());
        transformedBBox.setMaxy(defaultEnvelop.getMaxY());
        return transformedBBox;
    }

    public ReferencedEnvelope getDefaultEnvelop(FeatureLayerInfo layer) {
        FeatureBBoxInfo bbox = layer.getBbox();
        if (bbox != null) {
            if (!bbox.getNatived()) {
                return new ReferencedEnvelope(bbox.getMinx(), bbox.getMaxx(), bbox.getMiny(), bbox.getMaxy(), wgs_84);
            } else {
                CoordinateReferenceSystem nativeCrs = null;
                if (layer.getSpatialReferenceId() != null) {
                    nativeCrs = spatialReferenceInfoManagement.getCoordinateReferenceSystem(layer.getSpatialReferenceId());
                } else {
                    try {
                        nativeCrs = CRS.decode("EPSG:" + layer.getView().getSrid(), true);
                    } catch (FactoryException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (nativeCrs == null) {
                    throw new RuntimeException("Can not fetch the featureLayer`s native crs");
                }
                return new ReferencedEnvelope(bbox.getMinx(), bbox.getMaxx(), bbox.getMiny(), bbox.getMaxy(), nativeCrs);
            }
        }
        return null;
    }

    public CoordinateReferenceSystem getWebMercator() {
        return webMercator;
    }

    public CoordinateReferenceSystem getWgs84() {
        return wgs_84;
    }
}
