package org.geoatlas.metadata.persistence.repository;

import org.geoatlas.metadata.model.DataStoreInfo;
import org.geoatlas.metadata.model.NamespaceInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 12:44
 * @since: 1.0
 **/
public interface DataStoreInfoRepository extends PagingAndSortingRepository<DataStoreInfo, Long> {

//    List<DataStoreInfo> findDataStoreInfoByNamespaceId(Long namespaceId);

    Page<DataStoreInfo> findAllByNameContaining(String name, Pageable pageable);

    List<DataStoreInfo> findAll();
}
