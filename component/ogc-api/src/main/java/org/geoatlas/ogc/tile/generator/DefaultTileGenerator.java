package org.geoatlas.ogc.tile.generator;

import com.google.common.base.Throwables;
import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.GeoAtlasCacheExtensions;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.cache.core.locks.MemoryLockProvider;
import org.geoatlas.cache.core.source.TileSource;
import org.geoatlas.cache.core.locks.LockProvider;
import org.geoatlas.metadata.helper.FeatureSourceHelper;
import org.geoatlas.metadata.helper.FeatureSourceConveyor;
import org.geoatlas.metadata.model.PyramidRuleExpression;
import org.geoatlas.pyramid.Pyramid;
import org.geoatlas.pyramid.PyramidFactory;
import org.geoatlas.pyramid.action.vector.RuleExpressHelper;
import org.geoatlas.pyramid.action.vector.RuleExpression;
import org.geoatlas.pyramid.index.*;
import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/10 11:35
 * @since: 1.0
 **/
@Component
public class DefaultTileGenerator extends TileSource implements GeoAtlasTileGenerator {

    private final FeatureSourceHelper featureSourceHelper;

    private String lockProvider;

    private transient LockProvider lockProviderInstance;

    private static final Logger log = LoggerFactory.getLogger(DefaultTileGenerator.class);

    public DefaultTileGenerator(FeatureSourceHelper featureSourceHelper) {
        this.featureSourceHelper = featureSourceHelper;
    }

    public ConveyorTile generator(ConveyorTile tile) throws IOException, GeoAtlasCacheException, OutsideCoverageException {
        // FIXME: 2024/5/10 可以再次检查 MimeType, 在应用层面控制MimeType的支持
        // checkMimeType(tile);

        TileMatrixSet tileMatrixSet = TileMatrixSetContext.getTileMatrixSet(tile.getGridSetId());
        if (tileMatrixSet == null) {
            throw new IllegalArgumentException("TileMatrixSet not found by identifier: " + tile.getGridSetId());
        }
        // FIXME: 2024/5/10 需要在metadata中给featureLayer指定coverage, 可以选择从数据源读取或者是自行设定(更推荐自行设定, 性能友好且更通用)
        final TileMatrixSubset gridSubset = tile.getGridSubset();
        if (gridSubset == null) {
            throw new IllegalArgumentException("Requested gridset not found: " + tile.getRequest().getSchema());
        }
        final long[] gridLoc = tile.getTileIndex();
        checkNotNull(gridLoc);
        // Final preflight check, throws OutsideCoverageException if necessary
        gridSubset.checkCoverage(gridLoc);

        // FIXME: 2024/5/10 暂时不对meta tiles做支持
        int metaX;
        int metaY;
        if (tile.getMimeType().supportsTiling()) {
            metaX = getMetaTilingFactors()[0];
            metaY = getMetaTilingFactors()[1];
        } else {
            metaX = metaY = 1;
        }

        ConveyorTile tileResponse;
        if (tile.getStorageBroker() == null) {
            tileResponse = getNonCachedTile(tile);
        }else {
            tileResponse = getTileResponse(tile, true, true, metaX, metaY);
        }

//        sendTileRequestedEvent(returnTile);
        return tileResponse;
    }
    public ConveyorTile getNonCachedTile(ConveyorTile tile) throws GeoAtlasCacheException {
        try {
            return getTileResponse(tile, false, false, 1, 1);
        } catch (IOException e) {
            throw new GeoAtlasCacheException(e);
        }
    }

    @Override
    public void seedTile(ConveyorTile tile, boolean tryCache) throws GeoAtlasCacheException, IOException {
        // Ignore a seed call on a tile that's outside the cached grid levels range
        // 忽略缓存网格级别范围之外的图块上的种子调用
        final TileMatrixSubset subset = tile.getGridSubset();
        final int zLevel = (int) tile.getTileIndex()[2];
        if (!subset.shouldCacheAtZoom(zLevel)) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Ignoring seed call on tile "
                                + tile
                                + " as it's outside the cacheable zoom level range");
            }
            return;
        }

        int metaX = getMetaTilingFactors()[0];
        int metaY = getMetaTilingFactors()[1];
        if (!tile.getMimeType().supportsTiling()) {
            metaX = metaY = 1;
        }
        getTileResponse(tile, tryCache, true, metaX, metaY);
    }

    protected ConveyorTile getTileResponse(
            ConveyorTile tile, final boolean tryCache, final boolean persistent, final int metaX, final int metaY)
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
                    FeatureSourceConveyor wrapper = featureSourceHelper.getFeatureSource(request.getNamespace(), request.getLayer());
                    Pyramid pyramid = findPyramid(request, wrapper.getRules());
                    target = pyramid.getTile(request, wrapper.getFeatureSource(), wrapper.getCrs());
//              setupCachingStrategy(tile);
                    if (persistent) {
                        saveTiles(target, tile, requestTime);
                    }
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

    protected Pyramid findPyramid(TileRequest request, List<PyramidRuleExpression> ruleExpressionList) {
        List<RuleExpression> rules = null;
        if (!CollectionUtils.isEmpty(ruleExpressionList)) {
            // FIXME: 2024/5/27 假设每次request都要构建, 是不是太过于细致, 待优化
            rules = ruleExpressionList.stream()
                    .map(expression -> RuleExpressHelper
                            .buildRule(expression.getMinLevel(), expression.getMaxLevel(),
                                    expression.getFilter())).collect(Collectors.toList());

        }
        return PyramidFactory.buildPyramid(rules);
    }

    /**
     * @param tile
     */
    private void setTileIndexHeader(ConveyorTile tile) {
        tile.servletResp.addHeader("geoatlas-tile-index", Arrays.toString(tile.getTileIndex()));
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
     * @see DefaultTileGenerator#getLockProvider()
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
     * @see DefaultTileGenerator#setLockProvider(LockProvider)
     * @param lockProvider to set for this configuration
     */
    public void setLockProvider(LockProvider lockProvider){
        this.lockProviderInstance = lockProvider;
    }

}
