package org.geoatlas.cache.core.endpoint;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.seed.SeedRequest;
import org.geoatlas.cache.core.service.TileSeedService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Geo Atlas Cache -> gac
 * 瓦片种子服务端点 - 支持虚拟线程异步处理
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/4 20:43
 * @since: 1.0
 **/
@ResponseBody
@RequestMapping("/v1/gac/rest")
public class TileSeedEndpoint {

    private final TileSeedService tileSeedService;
    private final ExecutorService cacheOperationExecutor;

    public TileSeedEndpoint(TileSeedService tileSeedService,
                           @Qualifier("cacheOperationExecutor") ExecutorService cacheOperationExecutor) {
        this.tileSeedService = tileSeedService;
        this.cacheOperationExecutor = cacheOperationExecutor;
    }

    /**
     * 异步瓦片种子生成 - 使用虚拟线程处理
     * 种子生成是长时间运行的I/O密集型操作，非常适合虚拟线程
     */
    @PostMapping("/seed")
    public CompletableFuture<ResponseEntity<?>> seed(@Valid @RequestBody SeedRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                tileSeedService.doSeed(request);
                return ResponseEntity.ok().build();
            } catch (GeoAtlasCacheException e) {
                throw new RuntimeException("瓦片种子生成失败", e);
            }
        }, cacheOperationExecutor);
    }

    /**
     * 同步版本的种子生成（用于向下兼容）
     */
    @PostMapping("/seed/sync")
    public ResponseEntity<?> seedSync(@Valid @RequestBody SeedRequest request) throws GeoAtlasCacheException {
        tileSeedService.doSeed(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 使用@Async注解的异步种子生成示例
     */
    @Async("cacheOperationExecutor")
    @PostMapping("/seed/async")
    public CompletableFuture<ResponseEntity<?>> seedAsync(@Valid @RequestBody SeedRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                tileSeedService.doSeed(request);
                return ResponseEntity.ok().build();
            } catch (GeoAtlasCacheException e) {
                throw new RuntimeException("异步瓦片种子生成失败", e);
            }
        });
    }
}
