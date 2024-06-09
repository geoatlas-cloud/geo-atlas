package org.geoatlas.metadata.persistence.repository;

import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 20:58
 * @since: 1.0
 **/
public interface FeatureLayerInfoRepository extends PagingAndSortingRepository<FeatureLayerInfo, Long> {

    FeatureLayerInfo findFirstByNamespaceIdAndName(Long namespaceId, String name);

    boolean existsByNamespaceIdAndName(Long namespaceId, String name);

    List<FeatureLayerInfo> findAllByNamespaceId(Long namespaceId);

    List<FeatureLayerInfo> findAllByDatastoreId(Long datastoreId);

    Page<FeatureLayerInfo> findAllByNamespaceId(Long namespaceId, Pageable pageable);

    Page<FeatureLayerInfo> findAllByNameContaining(String name, Pageable pageable);
}
