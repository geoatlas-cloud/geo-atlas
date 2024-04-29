package org.geoatlas.metadata.persistence.repository;

import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.springframework.data.repository.CrudRepository;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 20:58
 * @since: 1.0
 **/
public interface FeatureLayerInfoRepository extends CrudRepository<FeatureLayerInfo, Long> {

    FeatureLayerInfo findFirstByNamespaceIdAndName(Long namespaceId, String name);

    boolean existsByNamespaceIdAndName(Long namespaceId, String name);
}
