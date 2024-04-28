package org.geoatlas.metadata.persistence.repository;

import org.geoatlas.metadata.model.DataStoreInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 12:44
 * @since: 1.0
 **/
public interface DataStoreInfoRepository extends CrudRepository<DataStoreInfo, Long> {

    List<DataStoreInfo> findDataStoreInfoByNamespaceId(Long namespaceId);
}
