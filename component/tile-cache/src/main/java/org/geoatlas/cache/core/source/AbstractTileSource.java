package org.geoatlas.cache.core.source;

import org.apache.commons.io.IOUtils;
import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
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
public abstract class AbstractTileSource implements TileSource{
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected static final ThreadLocal<ByteArrayResource> TILE_BUFFER = new ThreadLocal<>();

    public void seedTile(ConveyorTile tile, boolean tryCache)
            throws GeoAtlasCacheException, IOException {
        // Cache模块无法自主执行seed&reseed操作，需要集成方自行实现
        if (log.isWarnEnabled()){
            log.warn("The Cache module cannot perform seed & reseed operations autonomously, and the integrator needs to implement it by itself.");
        }
    }

    protected void transferTile(TileObject tile, ConveyorTile tileProto, long requestTime, boolean persistent) throws GeoAtlasCacheException {
        ByteArrayResource resource = this.getTileBuffer(TILE_BUFFER);
        // copy resource
        tileProto.setBlob(resource);
        try {
            writeTileToStream(tile, resource);
            tile.setCreated(requestTime);
            if (persistent){
                tileProto.getStorageBroker().put(tile);
            }
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
