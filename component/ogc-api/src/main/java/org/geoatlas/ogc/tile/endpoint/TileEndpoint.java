package org.geoatlas.ogc.tile.endpoint;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.ogc.tile.dispatcher.GeoAtlasTileDispatcher;
import org.geoatlas.ogc.tile.util.ResponseUtils;
import org.geoatlas.tile.TileRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 瓦片服务端点 - 支持虚拟线程异步处理
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 14:53
 * @since: 1.0
 **/
@RestController
@RequestMapping("/v1/tiles")
public class TileEndpoint {

    private final GeoAtlasTileDispatcher dispatcher;
    private final ExecutorService tileGenerationExecutor;

    public TileEndpoint(GeoAtlasTileDispatcher dispatcher,
                        @Qualifier("tileGenerationExecutor") ExecutorService tileGenerationExecutor) {
        this.dispatcher = dispatcher;
        this.tileGenerationExecutor = tileGenerationExecutor;
    }

    /**
     * 获取瓦片 - 使用虚拟线程异步处理
     * 瓦片生成是I/O密集型操作，使用虚拟线程可以显著提升并发性能
     */
    @CrossOrigin
    @GetMapping("/{namespace}/{layer}/{schema}/{tileMatrixId}/{tileRow}/{tileCol}.{format}")
    public CompletableFuture<Void> getTile(@PathVariable String namespace, 
                                          @PathVariable String layer, 
                                          @PathVariable String schema, 
                                          @PathVariable int tileMatrixId,
                                          @PathVariable int tileRow, 
                                          @PathVariable int tileCol, 
                                          @PathVariable String format,
                                          HttpServletRequest servletRequest,
                                          HttpServletResponse response) {
        return CompletableFuture.runAsync(() -> {
            try {
                TileRequest request = new TileRequest(namespace, layer, schema, tileCol, tileRow, tileMatrixId, format);
                ConveyorTile tile = dispatcher.dispatch(request, servletRequest, response);
                ResponseUtils.writeTile(tile);
            } catch (GeoAtlasCacheException e) {
                throw new RuntimeException("瓦片生成失败", e);
            }
        }, tileGenerationExecutor);
    }

    /**
     * 同步版本的瓦片获取（用于兼容性）
     */
    @CrossOrigin
    @GetMapping("/sync/{namespace}/{layer}/{schema}/{tileMatrixId}/{tileRow}/{tileCol}.{format}")
    public void getTileSync(@PathVariable String namespace, 
                           @PathVariable String layer, 
                           @PathVariable String schema, 
                           @PathVariable int tileMatrixId,
                           @PathVariable int tileRow, 
                           @PathVariable int tileCol, 
                           @PathVariable String format,
                           HttpServletRequest servletRequest,
                           HttpServletResponse response) throws GeoAtlasCacheException {
        TileRequest request = new TileRequest(namespace, layer, schema, tileCol, tileRow, tileMatrixId, format);
        ConveyorTile tile = dispatcher.dispatch(request, servletRequest, response);
        ResponseUtils.writeTile(tile);
    }
}
