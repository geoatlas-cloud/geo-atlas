package org.geoatlas.cache.core.config;

import org.geoatlas.cache.core.storage.BlobStore;
import org.geoatlas.cache.core.storage.blobstore.memory.CacheConfiguration;
import org.geoatlas.cache.core.storage.blobstore.memory.MemoryBlobStore;
import org.geoatlas.cache.core.storage.blobstore.memory.guava.GuavaCacheProvider;
import org.geoatlas.cache.core.util.GeoAtlasCacheEnvKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/9 21:23
 * @since: 1.0
 **/
public class DefaultBlobStoreConfiguration implements EnvironmentAware, ImportBeanDefinitionRegistrar {

    private static final Logger log = LoggerFactory.getLogger(DefaultBlobStoreConfiguration.class);

    private static final String FILE_SYSTEM_PROVIDER = "file-system";

    private static final String GEO_PACKAGE = "geo-package";


    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Boolean cacheEnabled = getApplicationValue(GeoAtlasCacheEnvKeys.getCacheEnabled(), Boolean.class, Boolean.FALSE);
        if (cacheEnabled) {
            Boolean innerCachingEnabled = getApplicationValue(GeoAtlasCacheEnvKeys.getInnerCachingEnabled(), Boolean.class, Boolean.FALSE);
            Boolean persistenceEnabled = getApplicationValue(GeoAtlasCacheEnvKeys.getPersistenceEnabled(), Boolean.class, Boolean.FALSE);
            BlobStore blobStore = null;
            if (innerCachingEnabled) {
                MemoryBlobStore memoryBlobStore = buildInnerCache();
                if (persistenceEnabled) {
                    memoryBlobStore.setStore(buildPersistenceCache());
                }
                blobStore = memoryBlobStore;
            }else if (persistenceEnabled) {
                blobStore = buildPersistenceCache();
            }else {
                log.warn("The cache configuration can be turned on, but the memory cache and persistent cache are turned off and cannot be further configured." +
                        " Please check your configuration items.");
            }
            if (blobStore == null) {
                log.warn("This blobStore configuration fails and will enter a no-cache state.");
            }
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setSynthetic(true);
            registry.registerBeanDefinition("blobStore", beanDefinition);
            log.info("The BlobStore registration is complete");
        }
    }

    private BlobStore buildPersistenceCache() {
        String persistenceProvider = getApplicationValue(GeoAtlasCacheEnvKeys.getPersistenceProvider(), String.class, GeoAtlasCacheEnvKeys.getDefaultPersistenceProvider());
        // FIXME: 2024/5/9 后续可以考虑维护一个注册表, 结合SPI的方式Load
        if (FILE_SYSTEM_PROVIDER.equals(persistenceProvider)) {
            FileBlobStoreInfo blobStoreInfo = new FileBlobStoreInfo();
            try {
                blobStoreInfo.setBaseDirectory(getApplicationValue(GeoAtlasCacheEnvKeys.getBaseDirectory(), String.class, null));
                blobStoreInfo.setPathGeneratorType(FileBlobStoreInfo.PathGeneratorType.valueOf(getApplicationValue(GeoAtlasCacheEnvKeys.getPathGeneratorType(), String.class, FileBlobStoreInfo.PathGeneratorType.DEFAULT.name())));
                blobStoreInfo.setFileSystemBlockSize(getApplicationValue(GeoAtlasCacheEnvKeys.getFileSystemBlockSize(), Integer.class, 1024));
                blobStoreInfo.setName(getApplicationValue(GeoAtlasCacheEnvKeys.getPersistenceProvider(), String.class, FILE_SYSTEM_PROVIDER));
                return blobStoreInfo.createInstance(null);
            }catch (Exception e) {
                log.warn("The persistent cache initialization failed. If the memory cache is not started, it will enter the no-cache state.");
            }
        }else if (GEO_PACKAGE.equals(persistenceProvider)) {
            throw new UnsupportedOperationException("The persistence provider [" + persistenceProvider + "] is not supported");
        }
        throw new UnsupportedOperationException("The persistence provider [" + persistenceProvider + "] is not supported");
    }

    /**
     * 构建内存缓存对象
     * @return
     */
    private MemoryBlobStore buildInnerCache() {
        // 这里暂时不对 provider 进行处理, 直接使用默认的Guava
        MemoryBlobStore memoryBlobStore = new MemoryBlobStore();
        try {
            CacheConfiguration configuration = new CacheConfiguration();
            configuration.setHardMemoryLimit(getApplicationValue(GeoAtlasCacheEnvKeys.getMemoryLimit(), Long.class, CacheConfiguration.DEFAULT_MEMORY_LIMIT));
            configuration.setConcurrencyLevel(getApplicationValue(GeoAtlasCacheEnvKeys.getConcurrencyLevel(), Integer.class, CacheConfiguration.DEFAULT_CONCURRENCY_LEVEL));
            configuration.setPolicy(CacheConfiguration.EvictionPolicy.valueOf(getApplicationValue(GeoAtlasCacheEnvKeys.getEvictionPolicy(), String.class, CacheConfiguration.DEFAULT_EVICTION_POLICY.name())));
            configuration.setEvictionTime(getApplicationValue(GeoAtlasCacheEnvKeys.getEvictionTime(), Long.class, CacheConfiguration.DEFAULT_EVICTION_TIME));
            GuavaCacheProvider startingCache = new GuavaCacheProvider(configuration);
            memoryBlobStore.setCacheProvider(startingCache);
        }catch (Exception exception) {
            log.warn("Initialization of custom configuration for memory cache failed, default configuration will be used");
        }
        return memoryBlobStore;
    }

    private <T> T getApplicationValue(String key, Class<T> targetType, T defaultValue){
        if (environment == null) {
            String msg = "Spring Environment was not set yet! Damn you Spring Framework :( ";
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        return environment.getProperty(key, targetType, defaultValue);
    }
}
