package org.geoatlas.metadata.persistence.repository;

import org.geoatlas.metadata.model.SpatialReferenceInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 20:57
 * @since: 1.0
 **/
public interface SpatialReferenceInfoRepository extends PagingAndSortingRepository<SpatialReferenceInfo, Long> {
    Page<SpatialReferenceInfo> findAllByNameContaining(String name, Pageable pageable);
}
