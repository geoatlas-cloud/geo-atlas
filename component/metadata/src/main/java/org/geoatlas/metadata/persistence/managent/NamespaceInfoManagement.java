package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.persistence.repository.NamespaceInfoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 10:47
 * @since: 1.0
 **/
@Service
public class NamespaceInfoManagement {

    private final NamespaceInfoRepository repository;
    private final ModelMapper mapper;

    public NamespaceInfoManagement(NamespaceInfoRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public void addNamespaceInfo(NamespaceInfo info) {
        repository.save(info);
    }

    public void removeNamespaceInfo(Long id) {
        repository.deleteById(id);
    }

    public void updateNamespaceInfo(NamespaceInfo info) {
        Optional<NamespaceInfo> old = repository.findById(info.getId());
        if (old.isPresent()){
            NamespaceInfo last = old.get();
            mapper.map(info, last);
            repository.save(last);
        }else {
            throw new RuntimeException("NamespaceInfo not found");
        }
    }

    public NamespaceInfo getNamespaceInfo(Long id) {
        return repository.findById(id).orElse(null);
    }
}
