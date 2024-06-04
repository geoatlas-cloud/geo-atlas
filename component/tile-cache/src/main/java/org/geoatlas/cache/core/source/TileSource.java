package org.geoatlas.cache.core.source;

import org.apache.commons.io.IOUtils;
import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.cache.core.locks.LockProvider;
import org.geoatlas.cache.core.storage.StorageException;
import org.geoatlas.io.ByteArrayResource;
import org.geoatlas.io.Resource;
import org.geoatlas.tile.TileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/3 15:12
 * @since: 1.0
 **/
public abstract class TileSource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 即默认不使用MetaTiles
     */
    private final static int[] META_TILING_FACTORS = {1,1};

    protected static final ThreadLocal<ByteArrayResource> TILE_BUFFER = new ThreadLocal<>();

    public abstract void seedTile(ConveyorTile tile, boolean tryCache)
            throws GeoAtlasCacheException, IOException;

    /**
     * The size of a metatile in tiles.
     *
     * @return the {x,y} metatiling factors
     */
    public int[] getMetaTilingFactors() {
        return META_TILING_FACTORS;
    }

    protected void saveTiles(TileObject tile, ConveyorTile tileProto, long requestTime) throws GeoAtlasCacheException {
        ByteArrayResource resource = this.getTileBuffer(TILE_BUFFER);
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
            buffer = new ByteArrayResource(16 * 1024);
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
}
