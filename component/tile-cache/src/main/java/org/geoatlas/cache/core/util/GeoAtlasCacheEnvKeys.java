package org.geoatlas.cache.core.util;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/9 20:24
 * @since: 1.0
 **/
public final class GeoAtlasCacheEnvKeys {

    public static final String CACHE_ENV_PREFIX = "geo-atlas.cache";
    public static final String INNER_CACHE_ENV_PREFIX = "geo-atlas.cache.inner.storage";
    public static final String PERSISTENCE_CACHE_ENV_PREFIX = "geo-atlas.cache.persistence.storage";

    private static final String CACHE_ENABLED = "enabled";
    public static final String INNER_CACHING_ENABLED = "inner-caching-enabled";
    public static final String PERSISTENCE_ENABLED = "persistence-enabled";

    public static String getCacheEnabled() {
        return String.format("%s.%s", CACHE_ENV_PREFIX, CACHE_ENABLED);
    }

    /**
     * 获取内存缓存开关key
     * @return
     */
    public static String getInnerCachingEnabled() {
        return String.format("%s.%s", CACHE_ENV_PREFIX, INNER_CACHING_ENABLED);
    }

    public static String getPersistenceEnabled() {
        return String.format("%s.%s", CACHE_ENV_PREFIX, PERSISTENCE_ENABLED);
    }


    public static final String PROVIDER = "persistence-enabled";

    // -----------------------------------  memory cache config key ------------------------------

    private static final String MEMORY_LIMIT = "memory-limit";
    private static final String CONCURRENCY_LEVEL = "concurrency-level";
    private static final String EVICTION_POLICY = "eviction-policy";
    private static final String EVICTION_TIME = "eviction-time";

    public static String getMemoryProvider() {
        return String.format("%s.%s", INNER_CACHE_ENV_PREFIX, PROVIDER);
    }

    public static String getMemoryLimit() {
        return String.format("%s.%s", INNER_CACHE_ENV_PREFIX, MEMORY_LIMIT);
    }

    public static String getConcurrencyLevel() {
        return String.format("%s.%s", INNER_CACHE_ENV_PREFIX, CONCURRENCY_LEVEL);
    }

    public static String getEvictionPolicy() {
        return String.format("%s.%s", INNER_CACHE_ENV_PREFIX, EVICTION_POLICY);
    }

    public static String getEvictionTime() {
        return String.format("%s.%s", INNER_CACHE_ENV_PREFIX, EVICTION_TIME);
    }

    // -----------------------------------  persistence cache config key ------------------------------

    private static final String BASE_DIRECTORY = "base-directory";

    public static String getPersistenceProvider(){
        return String.format("%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, PROVIDER);
    }

    public static String getBaseDirectory() {
        return String.format("%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, BASE_DIRECTORY);
    }

    // -----------------------------------  fs cache config key ------------------------------

    private static final String FILE_SYSTEM = "file-system";
    private static final String PATH_GENERATOR_TYPE = "path-generator-type";

    private static final String FILE_SYSTEM_BLOCK_SIZE = "block-size";

    public static String getPathGeneratorType() {
        return String.format("%s.%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, FILE_SYSTEM, PATH_GENERATOR_TYPE);
    }

    public static String getFileSystemBlockSize() {
        return String.format("%s.%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, FILE_SYSTEM, FILE_SYSTEM_BLOCK_SIZE);
    }

    // -----------------------------------  geo-package cache config key ------------------------------

    private static final String GEO_PACKAGE = "geo-package";

    private static final String LEVEL_RANGE_COUNT = "level-range-count";
    private static final String ROW_RANGE_COUNT = "row-range-count";
    private static final String COLUMN_RANGE_COUNT = "column-range-count";
    private static final String POOL_SIZE = "pool-size";
    private static final String POOL_REAPER_INTERVAL = "pool-reaper-interval";
    private static final String EXECUTOR_CONCURRENCY = "executor-concurrency";

    public static String getLevelRangeCount() {
        return String.format("%s.%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, GEO_PACKAGE, LEVEL_RANGE_COUNT);
    }

    public static String getRowRangeCount() {
        return String.format("%s.%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, GEO_PACKAGE, ROW_RANGE_COUNT);
    }

    public static String getColumnRangeCount() {
        return String.format("%s.%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, GEO_PACKAGE, COLUMN_RANGE_COUNT);
    }

    public static String getPoolSize() {
        return String.format("%s.%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, GEO_PACKAGE, POOL_SIZE);
    }

    public static String getPoolReaperInterval() {
        return String.format("%s.%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, GEO_PACKAGE, POOL_REAPER_INTERVAL);
    }

    public static String getExecutorConcurrency() {
        return String.format("%s.%s.%s", PERSISTENCE_CACHE_ENV_PREFIX, GEO_PACKAGE, EXECUTOR_CONCURRENCY);
    }

    // -----------------------------------  default value ------------------------------

    public static final String DEFAULT_PERSISTENCE_PROVIDER = "file-system";

    public static String getDefaultPersistenceProvider() {
        return DEFAULT_PERSISTENCE_PROVIDER;
    }
}
