package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.context.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.DataStoreInfo;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.persistence.repository.DataStoreInfoRepository;
import org.geotools.data.DataStore;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.VirtualTable;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final ModelMapper mapper;

    public DataStoreInfoManagement(PBEStringEncryptor encryptor, DataStoreInfoRepository repository,
                                   NamespaceInfoManagement namespaceInfoManagement, ModelMapper mapper) {
        this.encryptor = encryptor;
        this.repository = repository;
        this.namespaceInfoManagement = namespaceInfoManagement;
        this.mapper = mapper;
    }

    @Transactional
    public void addDataStoreInfo(DataStoreInfo info) {
        NamespaceInfo namespaceInfo = namespaceCheck(info);
        // 先删除后新增, 保证一个Namespace下只有一个Datastore
        List<DataStoreInfo> badData = repository.findDataStoreInfoByNamespaceId(namespaceInfo.getId());
        if (!CollectionUtils.isEmpty(badData)) {
            repository.deleteAllById(badData.stream().map(DataStoreInfo::getId).collect(Collectors.toList()));
        }
        info.setPassword(encryptor.encrypt(info.getPassword()));
        DataStoreInfo saved = repository.save(info);
        saved.setPassword(encryptor.decrypt(saved.getPassword()));
        GeoAtlasMetadataContext.addDataStore(namespaceInfo.getName(), saved);
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
            GeoAtlasMetadataContext.removeDataStore(namespaceInfo.getName());
            GeoAtlasMetadataContext.addDataStore(namespaceInfo.getName(), saved);
        }
    }

    public void removeDataStoreInfo(Long id) {
        Optional<DataStoreInfo> target = repository.findById(id);
        if(target.isPresent()) {
            NamespaceInfo namespaceInfo = namespaceCheck(target.get());
            repository.deleteById(id);
            GeoAtlasMetadataContext.removeDataStore(namespaceInfo.getName());
        }
    }

    /**
     * 获取DataStoreInfo对象, 没有进行密码解密
     * @param id
     * @return
     */
    public DataStoreInfo getDataStoreInfo(Long id) {
        return repository.findById(id).orElse(null);
    }

    private NamespaceInfo namespaceCheck(DataStoreInfo info){
        NamespaceInfo namespaceInfo = namespaceInfoManagement.getNamespaceInfo(info.getNamespaceId());
        if (namespaceInfo == null) {
            throw new RuntimeException("namespace not found");
        }
        return namespaceInfo;
    }

    public DataStore getDataStore(String namespace, FeatureLayerInfo featureLayerInfo) {
        DataStore dataStore = GeoAtlasMetadataContext.getDataStore(namespace);
        if (dataStore == null) {
            DataStoreInfo dataStoreInfo = this.getDataStoreInfo(featureLayerInfo.getDatastoreId());
            if (dataStoreInfo == null) {
                throw new RuntimeException("DataStoreInfo not found");
            }
            dataStoreInfo.setPassword(encryptor.decrypt(dataStoreInfo.getPassword()));
            dataStore = GeoAtlasMetadataContext.addDataStore(namespace, dataStoreInfo);
            JDBCDataStore jdbcDataStore = (JDBCDataStore) dataStore;
            if (!jdbcDataStore.getVirtualTables().containsKey(featureLayerInfo.getView().getName())) {
                VirtualTable virtualTable = FeatureLayerInfoManagement.getVirtualTable(featureLayerInfo);
                try {
                    jdbcDataStore.createVirtualTable(virtualTable);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }
        return dataStore;
    }

}
