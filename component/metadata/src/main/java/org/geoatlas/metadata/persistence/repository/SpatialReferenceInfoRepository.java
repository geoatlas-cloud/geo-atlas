package org.geoatlas.metadata.persistence.repository;

import org.geoatlas.metadata.model.SpatialReferenceInfo;
import org.springframework.data.repository.CrudRepository;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 20:57
 * @since: 1.0
 **/
public interface SpatialReferenceInfoRepository extends CrudRepository<SpatialReferenceInfo, Long> {
}
