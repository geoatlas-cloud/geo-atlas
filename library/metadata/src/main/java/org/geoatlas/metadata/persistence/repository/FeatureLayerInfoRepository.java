package org.geoatlas.metadata.persistence.repository;

import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.springframework.data.repository.CrudRepository;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 20:58
 * @since: 1.0
 **/
public interface FeatureLayerInfoRepository extends CrudRepository<FeatureLayerInfo, Long> {
}
