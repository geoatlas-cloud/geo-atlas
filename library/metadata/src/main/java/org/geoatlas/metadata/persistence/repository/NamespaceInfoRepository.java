package org.geoatlas.metadata.persistence.repository;

import org.geoatlas.metadata.model.NamespaceInfo;
import org.springframework.data.repository.CrudRepository;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 12:29
 * @since: 1.0
 **/
public interface NamespaceInfoRepository extends CrudRepository<NamespaceInfo, Long> {
}
