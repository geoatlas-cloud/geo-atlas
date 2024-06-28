package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.context.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.SpatialReferenceInfo;
import org.geoatlas.metadata.persistence.repository.SpatialReferenceInfoRepository;
import org.geoatlas.metadata.response.PageContent;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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
    public synchronized CoordinateReferenceSystem getCoordinateReferenceSystem(Long spatialReferenceId) {
        CoordinateReferenceSystem coordinateReferenceSystem = null;
        if (spatialReferenceId != null) {
            coordinateReferenceSystem = GeoAtlasMetadataContext.getCoordinateReferenceSystem(spatialReferenceId);
            if (coordinateReferenceSystem == null) {
                Optional<SpatialReferenceInfo> target = this.repository.findById(spatialReferenceId);
                if (target.isPresent()) {
                    try {

                        coordinateReferenceSystem = CRS.decode(target.get().getCode(), true);
                    } catch (FactoryException e) {
                        try {
                            // FIXME: 2024/6/28 目前此种方式是为兼容从数据库获取坐标系定义的方式，但是当心此种方式无法定义轴顺序以及与GeoTools默认初始化坐标系方法存在偏差
                            // FIXME: 2024/6/28 所以可以选择如org.geotools.referencing.factory.epsg.FactoryUsingWKT的方式进行拓展
                            // FIXME: 2024/6/28 或者，全部变更为使用配置文件的方式（目前改项目定位是类库），后续估计会倾向于此种方式进行改造。如果是做平台型的定位，那么会全部变更到数据库中进行处理
                            // 尝试使用wkt进行定义
                            coordinateReferenceSystem = CRS.parseWKT(target.get().getWktText());
                        }catch (FactoryException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            if (coordinateReferenceSystem == null) {
                throw new RuntimeException("CoordinateReferenceSystem not found");
            }
            GeoAtlasMetadataContext.addCoordinateReferenceSystem(spatialReferenceId, coordinateReferenceSystem);
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
