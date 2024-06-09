package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.context.GeoAtlasMetadataContext;
import org.geoatlas.metadata.event.DatastoreDeleteEvent;
import org.geoatlas.metadata.event.DatastoreUpdateEvent;
import org.geoatlas.metadata.model.DataStoreInfo;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.persistence.repository.DataStoreInfoRepository;
import org.geoatlas.metadata.response.PageContent;
import org.geotools.data.DataStore;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.VirtualTable;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 22:44
 * @since: 1.0
 **/
@Service
public class DataStoreInfoManagement {

    private final PBEStringEncryptor encryptor;
    private final DataStoreInfoRepository repository;
    private final NamespaceInfoManagement namespaceInfoManagement;
    
    private final ApplicationEventPublisher eventPublisher;
    
    private final ModelMapper mapper;

    public DataStoreInfoManagement(PBEStringEncryptor encryptor, DataStoreInfoRepository repository,
                                   NamespaceInfoManagement namespaceInfoManagement, ModelMapper mapper,
                                   ApplicationEventPublisher eventPublisher) {
        this.encryptor = encryptor;
        this.repository = repository;
        this.namespaceInfoManagement = namespaceInfoManagement;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void addDataStoreInfo(DataStoreInfo info) {
        NamespaceInfo namespaceInfo = namespaceCheck(info);
        info.setPassword(encryptor.encrypt(info.getPassword()));
        DataStoreInfo saved = repository.save(info);
        saved.setPassword(encryptor.decrypt(saved.getPassword()));
        GeoAtlasMetadataContext.addDataStore(saved);
    }

    @Transactional
    public void updateDataStoreInfo(DataStoreInfo info) {
        Optional<DataStoreInfo> oldOptional = repository.findById(info.getId());
        if (oldOptional.isPresent()){
            NamespaceInfo namespaceInfo = namespaceCheck(info);
            DataStoreInfo last = oldOptional.get();
            mapper.map(info, last);
            info.setPassword(encryptor.encrypt(info.getPassword()));
            DataStoreInfo saved = repository.save(info);
            saved.setPassword(encryptor.decrypt(saved.getPassword()));
            GeoAtlasMetadataContext.removeDataStore(saved.getId());
            GeoAtlasMetadataContext.addDataStore(saved);
            handleMutations(last, info);
        }
    }

    private void handleMutations(DataStoreInfo last, DataStoreInfo current) {
        if (!Objects.equals(last.getSchema(), current.getSchema()) ||
                Objects.equals(last.getDatabase(), current.getDatabase())) {
            last.setPassword(null);
            current.setPassword(null);
            eventPublisher.publishEvent(new DatastoreUpdateEvent(last, current));
        }
    }

    public void removeDataStoreInfo(Long id) {
        Optional<DataStoreInfo> target = repository.findById(id);
        if(target.isPresent()) {
//            NamespaceInfo namespaceInfo = namespaceCheck(target.get());
            repository.deleteById(id);
            GeoAtlasMetadataContext.removeDataStore(id);
            eventPublisher.publishEvent(new DatastoreDeleteEvent(target.get()));
        }
    }

    /**
     * 获取DataStoreInfo对象, 没有进行密码解密
     * @param id
     * @return
     */
    public DataStoreInfo getDataStoreInfo(Long id, boolean decrypt) {
        DataStoreInfo storeInfo = repository.findById(id).orElse(null);
        if (null != storeInfo) {
            if (decrypt){
                // FIXME: 2024/5/24 当前仅是为了方便操作, 目前将对密码做明文传输
                storeInfo.setPassword(encryptor.decrypt(storeInfo.getPassword()));
            }
        }
        return storeInfo;
    }

    public long getTotalCount() {
        return this.repository.count();
    }

    public PageContent<DataStoreInfo> pageDatastoreInfo(String name, PageRequest request) {
        if (name != null){
            return new PageContent<>(repository.findAllByNameContaining(name, request));
        }
        return new PageContent<>(repository.findAll(request));
    }

    public List<DataStoreInfo> list(){
        return repository.findAll();
    }

    private NamespaceInfo namespaceCheck(DataStoreInfo info){
        NamespaceInfo namespaceInfo = namespaceInfoManagement.getNamespaceInfo(info.getNamespaceId());
        if (namespaceInfo == null) {
            throw new RuntimeException("namespace not found");
        }
        return namespaceInfo;
    }

    public DataStore getDataStore(FeatureLayerInfo featureLayerInfo) {
        Long datastoreId = featureLayerInfo.getDatastoreId();
        DataStore dataStore = GeoAtlasMetadataContext.getDataStore(datastoreId);
        if (dataStore == null) {
            DataStoreInfo dataStoreInfo = this.getDataStoreInfo(datastoreId, false);
            if (dataStoreInfo == null) {
                throw new RuntimeException("DataStoreInfo not found");
            }
            dataStoreInfo.setPassword(encryptor.decrypt(dataStoreInfo.getPassword()));
            dataStore = GeoAtlasMetadataContext.addDataStore(dataStoreInfo);
        }
        if (dataStore == null) {
            throw new RuntimeException("DataStore not found");
        }
        JDBCDataStore jdbcDataStore = (JDBCDataStore) dataStore;
        if (!jdbcDataStore.getVirtualTables().containsKey(featureLayerInfo.getView().getName())) {
            VirtualTable virtualTable = FeatureLayerInfoManagement.getVirtualTable(featureLayerInfo);
            try {
                jdbcDataStore.createVirtualTable(virtualTable);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
        return dataStore;
    }

}
