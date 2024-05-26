package org.geoatlas.ogc.tile.adapter;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.GeoAtlasCacheExtensions;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.cache.core.locks.MemoryLockProvider;
import org.geoatlas.cache.core.storage.StorageBroker;
import org.geoatlas.cache.core.storage.StorageException;
import org.geoatlas.cache.core.locks.LockProvider;
import org.geoatlas.io.ByteArrayResource;
import org.geoatlas.io.Resource;
import org.geoatlas.metadata.helper.FeatureSourceHelper;
import org.geoatlas.metadata.helper.FeatureSourceWrapper;
import org.geoatlas.pyramid.Pyramid;
import org.geoatlas.pyramid.index.GeoAtlasPyramidException;
import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.pyramid.index.TileMatrixSetContext;
import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/10 11:35
 * @since: 1.0
 **/
@Component
public class GeoAtlasCachePyramidAdapter {

    private final Pyramid pyramid;

    private final StorageBroker storageBroker;

    private final FeatureSourceHelper featureSourceHelper;

    private String lockProvider;

    private transient LockProvider lockProviderInstance;

    private static final Logger log = LoggerFactory.getLogger(GeoAtlasCachePyramidAdapter.class);

    protected static final ThreadLocal<ByteArrayResource> TILE_BUFFER = new ThreadLocal();

    public GeoAtlasCachePyramidAdapter(Pyramid pyramid, @Autowired(required = false) StorageBroker storageBroker, FeatureSourceHelper featureSourceHelper) {
        this.pyramid = pyramid;
        this.storageBroker = storageBroker;
        this.featureSourceHelper = featureSourceHelper;
    }

    public StorageBroker getStorageBroker() {
        return storageBroker;
    }

    public ConveyorTile getTile(ConveyorTile tile) throws IOException, GeoAtlasCacheException {
        // FIXME: 2024/5/10 可以再次检查 MimeType, 在应用层面控制MimeType的支持
        // checkMimeType(tile);

        TileMatrixSet tileMatrixSet = TileMatrixSetContext.getTileMatrixSet(tile.getGridSetId());
        if (tileMatrixSet == null) {
            throw new IllegalArgumentException("TileMatrixSet not found by identifier: " + tile.getGridSetId());
        }
        // FIXME: 2024/5/10 需要在metadata中给featureLayer指定coverage, 可以选择从数据源读取或者是自行设定(更推荐自行设定, 性能友好且更通用)
//        final GridSubset gridSubset = getGridSubset(tileGridSetId);
//        if (gridSubset == null) {
//            throw new IllegalArgumentException("Requested gridset not found: " + tileGridSetId);
//        }
//        final long[] gridLoc = tile.getTileIndex();
//        checkNotNull(gridLoc);
        // Final preflight check, throws OutsideCoverageException if necessary
//        gridSubset.checkCoverage(gridLoc);

        // FIXME: 2024/5/10 暂时不对meta tiles做支持
//        int metaX;
//        int metaY;
//        if (mime.supportsTiling()) {
//            metaX = info.getMetaTilingX();
//            metaY = info.getMetaTilingY();
//        } else {
//            metaX = metaY = 1;
//        }

        ConveyorTile returnTile = getTileResponse(tile, true, 1, 1);

//        sendTileRequestedEvent(returnTile);

        return returnTile;
    }
    public ConveyorTile getNonCachedTile(ConveyorTile tile) throws GeoAtlasCacheException {
        try {
            return getTileResponse(tile, false, 1, 1);
        } catch (IOException e) {
            throw new GeoAtlasCacheException(e);
        }
    }

    protected ConveyorTile getTileResponse(
            ConveyorTile tile, final boolean tryCache, final int metaX, final int metaY)
            throws GeoAtlasCacheException, IOException {

        if (tryCache && tryCacheFetch(tile)) {
            return finalizeTile(tile);
        }

        TileObject target = null;
        TileRequest request = tile.getRequest();
        LockProvider.Lock lock = null;
        try {
            /* ****************** Acquire lock ******************* */
            lock = getLockProvider().getLock(buildLockKey(tile));
            // got the lock on the tile, try again
            if (tryCache && tryCacheFetch(tile)) {
                log.debug("--> {} returns cache hit for {}", Thread.currentThread().getName(), Arrays.toString(tile.getTileIndex()));
            } else {
                log.debug("--> {} submitting getTile request for tile matrix location on {}", Thread.currentThread().getName(), Arrays.toString(tile.getTileIndex()));
                long requestTime = System.currentTimeMillis();
                try {
                    FeatureSourceWrapper wrapper = featureSourceHelper.getFeatureSource(request.getNamespace(), request.getLayer());
                    target = pyramid.getTile(request, wrapper.getFeatureSource(), wrapper.getCrs());
//              setupCachingStrategy(tile);
                    saveTiles(target, tile, requestTime);
                }catch (Exception e) {
                    Throwables.throwIfInstanceOf(e, GeoAtlasCacheException.class);
                    throw new GeoAtlasCacheException("Problem communicating with GeoAtlas TileAPI", e);
                }
            }
        } finally {
            if (lock != null) {
                lock.release();
            }
        }

        return finalizeTile(tile);
    }

    private boolean tryCacheFetch(ConveyorTile tile) {
        try {
            // 24h
            return tile.retrieve(86400 * 1000L);
        } catch (GeoAtlasPyramidException exception) {
            log.info(exception.getMessage());
            tile.setErrorMsg(exception.getMessage());
            return false;
        }catch (Exception other) {
            throw new RuntimeException(other);
        }
    }

    private ConveyorTile finalizeTile(ConveyorTile tile) {

        if (tile.servletResp != null) {
            // do not call setExpirationHeaders from superclass, we have a more complex logic
            // to determine caching headers here
            Map<String, String> headers = new HashMap<>();
            // TODO: 10/11/23 set cache control headers
            setCacheControlHeaders(headers, (int) tile.getTileIndex()[2]);
            headers.forEach((k, v) -> tile.servletResp.setHeader(k, v));
            setTileIndexHeader(tile);
        }

//        tile.setTileLayer(this);
        return tile;
    }

    /**
     * @param tile
     */
    private void setTileIndexHeader(ConveyorTile tile) {
        tile.servletResp.addHeader("geoatlas-tile-index", Arrays.toString(tile.getTileIndex()));
    }

    protected void saveTiles(TileObject tile, ConveyorTile tileProto, long requestTime) throws GeoAtlasCacheException {
        ByteArrayResource resource;
        resource = this.getTileBuffer(TILE_BUFFER);
        // copy resource
        tileProto.setBlob(resource);
        LockProvider.Lock lock = null;
        try {
            writeTileToStream(tile, resource);
            tile.setCreated(requestTime);
            tileProto.getStorageBroker().put(tile);
            tileProto.getStorageObject().setCreated(tile.getCreated());
        } catch (StorageException var18) {
            throw new GeoAtlasCacheException(var18);
        } catch (IOException e) {
            log.error("Unable to write image tile to ByteArrayOutputStream", e);
        }
    }

    protected ByteArrayResource getTileBuffer(ThreadLocal<ByteArrayResource> tl) {
        ByteArrayResource buffer = (ByteArrayResource) tl.get();
        if (buffer == null) {
            buffer = new ByteArrayResource(16384);
            tl.set(buffer);
        }

        buffer.truncate();
        return buffer;
    }

    public boolean writeTileToStream(final TileObject raw, Resource target) throws IOException {
        try (OutputStream outStream = target.getOutputStream()) {
            IOUtils.copy(raw.getBlob().getInputStream(), outStream);
        }
        return true;
    }

    public static void setCacheControlHeaders(
            Map<String, String> map, int zoomLevel) {
//        if (skipCaching(layer)) {
//            setupNoCacheHeaders(map);
//        } else {
//            Integer cacheAgeMax = getCacheAge(layer, zoomLevel);
//            log.log(Level.FINE, "Using cacheAgeMax {0}", cacheAgeMax);
//            if (cacheAgeMax != null) {
//                map.put("Cache-Control", "max-age=" + cacheAgeMax + ", must-revalidate");
//                map.put("Expires", ServletUtils.makeExpiresHeader(cacheAgeMax));
//            } else {
//                setupNoCacheHeaders(map);
//            }
//        }
    }

    /**
     * Returns the chosen lock provider
     *
     * @see GeoAtlasCachePyramidAdapter#getLockProvider()
     */
    public LockProvider getLockProvider() {
        if (lockProviderInstance == null) {
            if (lockProvider == null) {
                lockProviderInstance = new MemoryLockProvider();
            } else {
                Object provider = GeoAtlasCacheExtensions.bean(lockProvider);
                if (provider == null) {
                    throw new RuntimeException(
                            "Could not find lock provider "
                                    + lockProvider
                                    + " in the spring application context");
                } else if (!(provider instanceof LockProvider)) {
                    throw new RuntimeException(
                            "Found bean "
                                    + lockProvider
                                    + " in the spring application context, but it was not a LockProvider");
                } else {
                    lockProviderInstance = (LockProvider) provider;
                }
            }
        }

        return lockProviderInstance;
    }

    private String buildLockKey(ConveyorTile tile) {
        StringBuilder metaKey = new StringBuilder();

        final long[] tileIndex = tile.getTileIndex();
        metaKey.append("tile_");
        long x = tileIndex[0];
        long y = tileIndex[1];
        long z = tileIndex[2];

        metaKey.append(tile.getLayerId());
        metaKey.append("_").append(tile.getGridSetId());
        metaKey.append("_").append(x).append("_").append(y).append("_").append(z);
        if (tile.getParametersId() != null) {
            metaKey.append("_").append(tile.getParametersId());
        }
        metaKey.append(".").append(tile.getMimeType().getFileExtension());

        return metaKey.toString();
    }

    /**
     * Set the LockProvider is present
     *
     * @see GeoAtlasCachePyramidAdapter#setLockProvider(LockProvider)
     * @param lockProvider to set for this configuration
     */
    public void setLockProvider(LockProvider lockProvider){
        this.lockProviderInstance = lockProvider;
    }

}
