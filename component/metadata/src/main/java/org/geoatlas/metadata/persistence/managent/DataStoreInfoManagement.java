package org.geoatlas.metadata.persistence.managent;

import org.geoatlas.metadata.GeoAtlasMetadataContext;
import org.geoatlas.metadata.model.DataStoreInfo;
import org.geoatlas.metadata.persistence.repository.DataStoreInfoRepository;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 22:44
 * @since: 1.0
 **/
@Service
public class DataStoreInfoManagement {

    private final PBEStringEncryptor encryptor;
    private final DataStoreInfoRepository repository;

    public DataStoreInfoManagement(PBEStringEncryptor encryptor, DataStoreInfoRepository repository) {
        this.encryptor = encryptor;
        this.repository = repository;
    }

    public void addDataStoreInfo(DataStoreInfo info) {
        info.setPassword(encryptor.encrypt(info.getPassword()));
        DataStoreInfo saved = repository.save(info);
        saved.setPassword(encryptor.decrypt(saved.getPassword()));
        GeoAtlasMetadataContext.addDataStore(saved);
    }

    public void updateDataStoreInfo(DataStoreInfo info) {
        DataStoreInfo saved = repository.save(info);
        saved.setPassword(encryptor.decrypt(saved.getPassword()));
        GeoAtlasMetadataContext.removeDataStore(saved.getId());
        GeoAtlasMetadataContext.addDataStore(saved);
    }

    public void removeDataStoreInfo(Long id) {
        repository.deleteById(id);
        GeoAtlasMetadataContext.removeDataStore(id);
    }

    /**
     * 获取DataStoreInfo对象, 没有进行密码解密
     * @param id
     * @return
     */
    public DataStoreInfo getDataStoreInfo(Long id) {
        return repository.findById(id).orElse(null);
    }

}
