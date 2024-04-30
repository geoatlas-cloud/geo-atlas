package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.context.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.SpatialReferenceInfo;
import org.geoatlas.metadata.persistence.repository.SpatialReferenceInfoRepository;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/29 17:19
 * @since: 1.0
 **/
@Service
public class SpatialReferenceInfoManagement {

    private final SpatialReferenceInfoRepository repository;

    public SpatialReferenceInfoManagement(SpatialReferenceInfoRepository repository) {
        this.repository = repository;
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem(Long spatialReferenceId) {
        CoordinateReferenceSystem coordinateReferenceSystem = null;
        if (spatialReferenceId != null) {
            coordinateReferenceSystem = GeoAtlasMetadataContext.getCoordinateReferenceSystem(spatialReferenceId);
            if (coordinateReferenceSystem == null) {
                Optional<SpatialReferenceInfo> target = this.repository.findById(spatialReferenceId);
                if (target.isPresent()) {
                    try {
//                        coordinateReferenceSystem = CRS.parseWKT(target.get().getWktText());
                        coordinateReferenceSystem = CRS.decode(target.get().getCode(), true);
                    } catch (FactoryException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (coordinateReferenceSystem == null) {
                throw new RuntimeException("CoordinateReferenceSystem not found");
            }
        }
        return coordinateReferenceSystem;
    }
}
