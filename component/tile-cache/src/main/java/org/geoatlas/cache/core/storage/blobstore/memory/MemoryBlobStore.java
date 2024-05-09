/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 * <p>Copyright 2019
 */
package org.geoatlas.cache.core.storage.blobstore.memory;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.geoatlas.io.ByteArrayResource;
import org.geoatlas.io.Resource;
import org.geoatlas.tile.TileObject;
import org.geotools.util.logging.Logging;
import org.geoatlas.cache.core.storage.*;
import org.geoatlas.cache.core.storage.blobstore.memory.guava.GuavaCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * This class is an implementation of the {@link BlobStore} interface wrapping another {@link
 * BlobStore} implementation and supporting in memory caching. Caching is provided by an input
 * {@link CacheProvider} object. It must be pointed out that this Blobstore has an asynchronous
 * relation with the underlying wrapped {@link BlobStore}. In fact, each operation on the wrapped
 * {@link BlobStore} is scheduled in a queue and will be done by an executor thread. Operations that
 * require a boolean value will have to wait until previous tasks are completed.
 *
 * @author Nicola Lagomarsini Geosolutions
 */
public class MemoryBlobStore implements BlobStore, ApplicationContextAware {

    /** {@link org.slf4j.Logger} object used for logging exceptions */
    private static final Logger log = LoggerFactory.getLogger(MemoryBlobStore.class);

    /** {@link BlobStore} to use when no element is found */
    private BlobStore store;

    /** {@link CacheProvider} object to use for caching */
    private CacheProvider cacheProvider;

    /** Executor service used for scheduling cacheProvider store operations like put,delete,... */
    private final ExecutorService executorService;

    /**
     * Optional name used for searching the bean related to the CacheProvider to set in the
     * ApplicationContext
     */
    private String cacheBeanName;

    /** Boolean used for Application Context initialization */
    private AtomicBoolean cacheAlreadySet;

    /**
     * {@link ReentrantReadWriteLock} used for handling concurrency when accessing the
     * cacheProvider.
     */
    private final ReentrantReadWriteLock lock;

    /** {@link WriteLock} used for scheduling the access to the {@link MemoryBlobStore} state */
    private final WriteLock blobStoreStateLock;

    /**
     * {@link ReadLock} used for granting access to operations which does not change the {@link
     * MemoryBlobStore} state, but can change the state of its components like {@link CacheProvider}
     * and {@link BlobStore}
     */
    private final ReadLock componentsStateLock;

    public MemoryBlobStore() {
        // Initialization of the various elements
        this.executorService = Executors.newFixedThreadPool(1);
        lock = new ReentrantReadWriteLock(true);
        blobStoreStateLock = lock.writeLock();
        componentsStateLock = lock.readLock();
        cacheAlreadySet = new AtomicBoolean(false);
        // Initialization of the cacheProvider and store. Must be overridden, this uses default and
        // caches in memory
        setStore(new NullBlobStore());
        GuavaCacheProvider startingCache = new GuavaCacheProvider(new CacheConfiguration());
        this.cacheProvider = startingCache;
    }

    @Override
    public boolean layerExists(String layerName) {
        componentsStateLock.lock();
        try {
            return store.layerExists(layerName);
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public boolean delete(String layerName) throws StorageException {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Removing layer: {} from cache provider", layerName);
            }
            // Remove from cacheProvider
            cacheProvider.removeLayer(layerName);
            // Remove the layer. Wait other scheduled tasks
            boolean executed = executeBlobStoreTask(BlobStoreAction.DELETE_LAYER, store, layerName);
            if (log.isDebugEnabled()) {
                if (executed) {
                    log.debug("Delete Layer Task executed");
                } else {
                    log.debug("Delete LayerTask failed");
                }
            }
            // Returns the result
            return executed;
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public boolean deleteByGridsetId(String layerName, String gridSetId) throws StorageException {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Removing Layer: {}", layerName);
            }
            // Remove the layer from the cacheProvider
            cacheProvider.removeLayer(layerName);
            if (log.isDebugEnabled()) {
                log.debug("Scheduling GridSet: {} removal for Layer: {}", gridSetId, layerName);
            }
            // Remove selected gridsets
            executorService.submit(
                    new BlobStoreTask(store, BlobStoreAction.DELETE_GRIDSET, layerName, gridSetId));
            return true;
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public boolean delete(TileObject obj) throws StorageException {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Removing TileObject: {}", obj);
            }
            // Remove from cacheProvider
            cacheProvider.removeTileObj(obj);
            // Remove selected TileObject
            if (log.isDebugEnabled()) {
                log.debug("Scheduling removal of TileObject: {}", obj);
            }
            executorService.submit(new BlobStoreTask(store, BlobStoreAction.DELETE_SINGLE, obj));
            return true;
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public boolean delete(TileRange obj) throws StorageException {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Removing TileObjects for Layer: {}, min/max levels: [{}, {}], TileMatrixSet: {}",
                        obj.getLayerName(), obj.getZoomStart(), obj.getZoomStop(), obj.getGridSetId());
            }
            // Remove layer for the cacheProvider
            cacheProvider.removeLayer(obj.getLayerName());
            // Remove selected TileObject
            if (log.isDebugEnabled()) {
                log.debug(
                        "Scheduling removal of TileObjects for Layer: {}, min/max levels: "
                                + "[{}, {}], TileMatrixSet: {}",
                        obj.getLayerName(), obj.getZoomStart(), obj.getZoomStop(), obj.getGridSetId());
            }
            // Remove selected TileRange
            executorService.submit(new BlobStoreTask(store, BlobStoreAction.DELETE_RANGE, obj));
            return true;
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public boolean get(TileObject obj) throws StorageException {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking if TileObject:{} is present", obj);
            }
            TileObject cached = cacheProvider.getTileObj(obj);
            boolean found = false;
            if (cached == null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "TileObject:{} not found. Try to get it from the wrapped blobstore", obj);
                }
                // Try if it can be found in the system. Wait other scheduled tasks
                found = executeBlobStoreTask(BlobStoreAction.GET, store, obj);

                // If the file has been found, it is inserted in cacheProvider
                if (found) {
                    if (log.isDebugEnabled()) {
                        log.debug("TileObject:{} found. Put it in cache", obj);
                    }
                    // Get the Cached TileObject
                    cached = getByteResourceTile(obj);
                    // Put the file in Cache
                    cacheProvider.putTileObj(cached);
                }
            } else {
                // Found in cacheProvider
                found = true;
            }
            // If found add its resource to the input TileObject
            if (found) {
                if (log.isDebugEnabled()) {
                    log.debug("TileObject:{} found, update the input TileObject", obj);
                }
                Resource resource = cached.getBlob();
                obj.setBlob(resource);
                obj.setCreated(resource.getLastModified());
                obj.setBlobSize((int) resource.getSize());
            }

            return found;
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public void put(TileObject obj) throws StorageException {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Convert Input resource into a Byte Array");
            }
            TileObject cached = getByteResourceTile(obj);
            if (log.isDebugEnabled()) {
                log.debug("Adding TileObject: {} to cache", obj);
            }
            cacheProvider.putTileObj(cached);
            // Add selected TileObject. Wait other scheduled tasks
            if (log.isDebugEnabled()) {
                log.debug("Adding TileObject: {} to the wrapped blobstore", obj);
            }
            // Variable containing the execution result
            executeBlobStoreTask(BlobStoreAction.PUT, store, obj);
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public void clear() throws StorageException {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Flushing cache");
            }
            // flush the cacheProvider
            cacheProvider.clear();
            // Remove all the files
            executorService.submit(new BlobStoreTask(store, BlobStoreAction.CLEAR, ""));
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public void destroy() {
        blobStoreStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Reset cache");
            }
            // flush the cacheProvider
            cacheProvider.reset();
            // Remove all the files
            if (log.isDebugEnabled()) {
                log.debug("Destroy wrapped store");
            }
            executeBlobStoreTask(BlobStoreAction.DESTROY, store, "");
            // Stop the pending tasks
            executorService.shutdown();
        } finally {
            blobStoreStateLock.unlock();
        }
    }

    @Override
    public void addListener(BlobStoreListener listener) {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Adding a new Listener");
            }
            // Add a new Listener
            store.addListener(listener);
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public boolean removeListener(BlobStoreListener listener) {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Removing listener");
            }
            // Remove a listener
            return store.removeListener(listener);
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public boolean rename(String oldLayerName, String newLayerName) throws StorageException {
        componentsStateLock.lock();
        try {
            // flush the cacheProvider
            if (log.isDebugEnabled()) {
                log.debug("Flushing cache");
            }
            cacheProvider.clear();
            // Rename the layer. Wait other scheduled tasks
            if (log.isDebugEnabled()) {
                log.debug("Executing Layer rename task");
            }
            // Variable containing the execution result
            boolean executed =
                    executeBlobStoreTask(BlobStoreAction.RENAME, store, oldLayerName, newLayerName);
            return executed;
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public String getLayerMetadata(String layerName, String key) {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting metadata for Layer: {}", layerName);
            }
            // Get the Layer metadata
            return store.getLayerMetadata(layerName, key);
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public void putLayerMetadata(String layerName, String key, String value) {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Adding metadata for Layer: {}", layerName);
            }
            // Add a new Layer Metadata
            store.putLayerMetadata(layerName, key, value);
        } finally {
            componentsStateLock.unlock();
        }
    }

    /** @return a {@link CacheStatistics} object containing the {@link CacheProvider} statistics */
    public CacheStatistics getCacheStatistics() {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting Cache Statistics");
            }
            return cacheProvider.getStatistics();
        } finally {
            componentsStateLock.unlock();
        }
    }

    /** Setter for the store to wrap */
    public void setStore(BlobStore store) {
        blobStoreStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Setting the wrapped store");
            }
            if (store == null) {
                throw new NullPointerException("Input BlobStore cannot be null");
            }
            this.store = store;
        } finally {
            blobStoreStateLock.unlock();
        }
    }

    /** @return The wrapped {@link BlobStore} implementation */
    public BlobStore getStore() {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Returning the wrapped store");
            }
            return store;
        } finally {
            componentsStateLock.unlock();
        }
    }

    /** Setter for the cacheProvider to use */
    public void setCacheProvider(CacheProvider cache) {
        blobStoreStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Setting cache provided");
            }
            if (cache == null) {
                throw new IllegalArgumentException("Input BlobStore cannot be null");
            }
            this.cacheProvider = cache;
            cacheAlreadySet.getAndSet(true);
        } finally {
            blobStoreStateLock.unlock();
        }
    }

    /**
     * * This method is used for converting a {@link TileObject} {@link Resource} into a {@link
     * ByteArrayResource}.
     *
     * @return a TileObject with resource stored in a Byte Array
     */
    private TileObject getByteResourceTile(TileObject obj) throws StorageException {
        // Get TileObject resource
        Resource blob = obj.getBlob();
        final ByteArrayResource finalBlob;
        // If it is a ByteArrayResource, the result is simply copied
        if (obj.getBlob() instanceof ByteArrayResource) {
            if (log.isDebugEnabled()) {
                log.debug("Resource is already a Byte Array, only a copy is needed");
            }
            ByteArrayResource byteArrayResource = (ByteArrayResource) obj.getBlob();
            byte[] contents = byteArrayResource.getContents();
            finalBlob = new ByteArrayResource(contents);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Resource is not a Byte Array, data must be transferred");
            }
            // Else the result is written to a new WritableByteChannel
            try (ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                    WritableByteChannel wChannel = Channels.newChannel(bOut)) {
                blob.transferTo(wChannel);
                finalBlob = new ByteArrayResource(bOut.toByteArray());
            } catch (IOException e) {
                throw new StorageException(e.getLocalizedMessage(), e);
            }
        }

        finalBlob.setLastModified(blob.getLastModified());
        // Creation of a new Resource
        TileObject cached =
                TileObject.createCompleteTileObject(
                        obj.getLayerName(),
                        obj.getXYZ(),
//                        obj.getGridSetId(),
                        obj.getSchema(),
//                        obj.getBlobFormat(),
                        obj.getFormat(),
                        obj.getParameters(),
                        finalBlob);
        return cached;
    }

    /**
     * Setter for the Cache Provider name, note that this cannot be used in combination with the
     * setCacheProvider method in the application Context initialization
     */
    public void setCacheBeanName(String cacheBeanName) {
        blobStoreStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Setting cache providee name");
            }
            this.cacheBeanName = cacheBeanName;
        } finally {
            blobStoreStateLock.unlock();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!cacheAlreadySet.get()) {
            // Get all the CacheProvider beans
            String[] beans = applicationContext.getBeanNamesForType(CacheProvider.class);
            int beanSize = beans.length;
            boolean configured = false;
            // If at least one bean is present, use it
            if (beanSize > 0) {
                // If a bean name is defined, get the related bean
                if (cacheBeanName != null && !cacheBeanName.isEmpty()) {
                    for (String beanDef : beans) {
                        if (cacheBeanName.equalsIgnoreCase(beanDef)) {
                            CacheProvider bean =
                                    applicationContext.getBean(beanDef, CacheProvider.class);
                            if (bean.isAvailable()) {
                                setCacheProvider(bean);
                                configured = true;
                                break;
                            }
                        }
                    }
                }
                // If only one is present it is used
                if (!configured && beanSize == 1) {
                    CacheProvider bean = applicationContext.getBean(beans[0], CacheProvider.class);
                    if (bean.isAvailable()) {
                        setCacheProvider(bean);
                        configured = true;
                    }
                }
                // If two are present and at least one of them is not guava, then it is used
                if (!configured && beanSize == 2) {
                    for (String beanDef : beans) {
                        CacheProvider bean =
                                applicationContext.getBean(beanDef, CacheProvider.class);
                        if (!(bean instanceof GuavaCacheProvider) && bean.isAvailable()) {
                            setCacheProvider(bean);
                            configured = true;
                            break;
                        }
                    }
                    // Try again and search if at least a GuavaCacheProvider is present
                    if (!configured) {
                        for (String beanDef : beans) {
                            CacheProvider bean =
                                    applicationContext.getBean(beanDef, CacheProvider.class);
                            if (bean.isAvailable()) {
                                setCacheProvider(bean);
                                configured = true;
                                break;
                            }
                        }
                    }
                }
                if (!configured) {
                    if (log.isDebugEnabled()) {
                        log.debug("CacheProvider not configured, use default configuration");
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("CacheProvider already configured");
            }
        }
    }

    private boolean executeBlobStoreTask(BlobStoreAction action, BlobStore store, Object... objs) {
        Future<Boolean> future = executorService.submit(new BlobStoreTask(store, action, objs));
        // Variable containing the execution result
        boolean executed = false;
        if (log.isDebugEnabled()) {
            log.debug("Waiting scheduled Tasks");
        }
        try {
            // Waiting tasks
            executed = future.get();
        } catch (InterruptedException | ExecutionException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return executed;
    }

    /**
     * {@link Callable} implementation used for creating various tasks to submit to the {@link
     * MemoryBlobStore} executor service.
     *
     * @author Nicola Lagomarsini GeoSolutions
     */
    static class BlobStoreTask implements Callable<Boolean> {

        /** Store on which tasks must be executed */
        private BlobStore store;

        /** Array of objects that must be used for the selected operation */
        private Object[] objs;

        /** Enum containing the kind of action to execute */
        private BlobStoreAction action;

        public BlobStoreTask(BlobStore store, BlobStoreAction action, Object... objs) {
            this.objs = objs;
            this.store = store;
            this.action = action;
        }

        @Override
        public Boolean call() throws Exception {
            boolean result = false;
            try {
                // Execution of the requested operation
                result = action.executeOperation(store, objs);
            } catch (StorageException s) {
                if (log.isErrorEnabled()) {
                    log.error(s.getMessage(), s);
                }
            }
            return result;
        }
    }

    /**
     * Enum containing all the possible operations that can be executed by a {@link BlobStoreTask}.
     * Each operation must implement the "executeOperation" method.
     *
     * @author Nicola Lagomarsini GeoSolutions
     */
    public enum BlobStoreAction {
        PUT {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                if (objs == null || objs.length < 1 || !(objs[0] instanceof TileObject)) {
                    return false;
                }
                store.put((TileObject) objs[0]);
                return true;
            }
        },
        GET {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                if (objs == null || objs.length < 1 || !(objs[0] instanceof TileObject)) {
                    return false;
                }
                return store.get((TileObject) objs[0]);
            }
        },
        DELETE_SINGLE {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                if (objs == null || objs.length < 1 || !(objs[0] instanceof TileObject)) {
                    return false;
                }
                return store.delete((TileObject) objs[0]);
            }
        },
        DELETE_RANGE {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                if (objs == null || objs.length < 1 || !(objs[0] instanceof TileRange)) {
                    return false;
                }
                return store.delete((TileRange) objs[0]);
            }
        },
        DELETE_GRIDSET {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                if (objs == null
                        || objs.length < 2
                        || !(objs[0] instanceof String)
                        || !(objs[1] instanceof String)) {
                    return false;
                }
                return store.deleteByGridsetId((String) objs[0], (String) objs[1]);
            }
        },
        DELETE_PARAMS_ID {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                if (objs == null
                        || objs.length < 2
                        || !(objs[0] instanceof String)
                        || !(objs[1] instanceof String)) {
                    return false;
                }
                return store.deleteByParametersId((String) objs[0], (String) objs[1]);
            }
        },
        DELETE_LAYER {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                if (objs == null || objs.length < 1 || !(objs[0] instanceof String)) {
                    return false;
                }
                return store.delete((String) objs[0]);
            }
        },
        CLEAR {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                store.clear();
                return true;
            }
        },
        DESTROY {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                store.destroy();
                return true;
            }
        },
        RENAME {
            @Override
            public boolean executeOperation(BlobStore store, Object... objs)
                    throws StorageException {
                if (objs == null
                        || objs.length < 2
                        || !(objs[0] instanceof String)
                        || !(objs[1] instanceof String)) {
                    return false;
                }
                return store.rename((String) objs[0], (String) objs[1]);
            }
        };

        /**
         * Executes an operation defined by the Enum.
         *
         * @return operation result
         */
        public abstract boolean executeOperation(BlobStore store, Object... objs)
                throws StorageException;
    }

    @Override
    public boolean deleteByParametersId(String layerName, String parametersId)
            throws StorageException {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Removing Layer: {}", layerName);
            }
            // Remove the layer from the cacheProvider
            cacheProvider.removeLayer(layerName);
            if (log.isDebugEnabled()) {
                log.debug(
                        "Scheduling Parameters: {} removal for Layer: {}", parametersId, layerName);
            }
            // Remove selected parameters
            executorService.submit(
                    new BlobStoreTask(
                            store, BlobStoreAction.DELETE_PARAMS_ID, layerName, parametersId));
            return true;
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public Set<Map<String, String>> getParameters(String layerName) throws StorageException {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting parameters for Layer: {}", layerName);
            }
            return store.getParameters(layerName);
        } finally {
            componentsStateLock.unlock();
        }
    }

    @Override
    public Map<String, Optional<Map<String, String>>> getParametersMapping(String layerName) {
        componentsStateLock.lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting parameters for Layer: {}", layerName);
            }
            return store.getParametersMapping(layerName);
        } finally {
            componentsStateLock.unlock();
        }
    }
}
