package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.persistence.repository.NamespaceInfoRepository;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 10:47
 * @since: 1.0
 **/
@Service
public class NamespaceInfoManagement {

    private final NamespaceInfoRepository repository;

    public NamespaceInfoManagement(NamespaceInfoRepository repository) {
        this.repository = repository;
    }

    public void addNamespaceInfo(NamespaceInfo info) {
        repository.save(info);
    }

    public void removeNamespaceInfo(Long id) {
        repository.deleteById(id);
    }

    public void updateNamespaceInfo(NamespaceInfo info) {
        repository.save(info);
    }

    public NamespaceInfo getNamespaceInfo(Long id) {
        return repository.findById(id).orElse(null);
    }
}
