package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.persistence.repository.NamespaceInfoRepository;
import org.geoatlas.metadata.response.PageContent;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
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
        if (repository.existsByName(info.getName())) {
            throw new RuntimeException("NamespaceInfo already exists");
        }
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

    public NamespaceInfo findByName(String name) {
        return repository.findFirstByName(name);
    }

    public NamespaceInfo getNamespaceInfo(Long id) {
        return repository.findById(id).orElse(null);
    }

    public long getTotalCount() {
        return this.repository.count();
    }

    public PageContent<NamespaceInfo> pageNamespaceInfo(String name, PageRequest request) {
        if (name != null){
            return new PageContent<>(repository.findAllByNameContaining(name, request));
        }
        return new PageContent<>(repository.findAll(request));
    }

    public NamespaceInfo getNamespaceInfoByFeatureLayerId(Long featureLayerId) {
        return repository.findByFeatureLayerId(featureLayerId);
    }

    public List<NamespaceInfo> list() {
        return repository.findAll();
    }
}
