package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.context.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.SpatialReferenceInfo;
import org.geoatlas.metadata.persistence.repository.SpatialReferenceInfoRepository;
import org.geoatlas.metadata.response.PageContent;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public long getTotalCount() {
        return this.repository.count();
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
                        GeoAtlasMetadataContext.addCoordinateReferenceSystem(spatialReferenceId, coordinateReferenceSystem);
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

    public SpatialReferenceInfo getSpatialReferenceInfo(Long spatialReferenceId) {
        SpatialReferenceInfo spatialReferenceInfo = null;
        if (spatialReferenceId != null) {
            spatialReferenceInfo = GeoAtlasMetadataContext.getSpatialReferenceInfo(spatialReferenceId);
            if (spatialReferenceInfo == null) {
                Optional<SpatialReferenceInfo> target = this.repository.findById(spatialReferenceId);
                if (target.isPresent()) {
                    GeoAtlasMetadataContext.addSpatialReferenceInfo(spatialReferenceId, target.get());
                    spatialReferenceInfo = target.get();
                }
            }
            if (spatialReferenceInfo == null) {
                throw new RuntimeException("SpatialReferenceInfo not found");
            }
        }
        return spatialReferenceInfo;
    }

    public Optional<SpatialReferenceInfo> findById(Long spatialReferenceId) {
        return this.repository.findById(spatialReferenceId);
    }

    public PageContent<SpatialReferenceInfo> pageSpatialReferenceInfo(String name, PageRequest pageRequest) {
        if (name != null){
            return new PageContent<>(this.repository.findAllByNameContaining(name, pageRequest));
        }
        return new PageContent<>(this.repository.findAll(pageRequest));
    }
}
