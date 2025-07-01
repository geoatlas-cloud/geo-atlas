package org.geoatlas.metadata.endpoint;

import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.FeatureLayerPreviewInfo;
import org.geoatlas.metadata.persistence.managent.FeatureLayerInfoManagement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 功能图层信息端点 - 支持虚拟线程异步处理
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 11:42
 * @since: 1.0
 **/
@RestController
@RequestMapping("/v1/metadata/feature_layers")
public class FeatureLayerInfoEndpoint {

    private final FeatureLayerInfoManagement management;
    private final ExecutorService databaseOperationExecutor;

    public FeatureLayerInfoEndpoint(FeatureLayerInfoManagement management,
                                   @Qualifier("databaseOperationExecutor") ExecutorService databaseOperationExecutor) {
        this.management = management;
        this.databaseOperationExecutor = databaseOperationExecutor;
    }

    @PostMapping
    public ResponseEntity<?> addFeatureLayerInfo(@RequestBody FeatureLayerInfo info) {
        management.addFeatureLayerInfo(info);
        return ResponseEntity.ok().build();
    }

    /**
     * 异步删除功能图层 - 使用虚拟线程处理
     * 删除操作可能涉及复杂的级联操作，适合使用虚拟线程
     */
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> removeFeatureLayerInfo(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            management.removeFeatureLayerInfo(id);
            return ResponseEntity.ok().build();
        }, databaseOperationExecutor);
    }

    /**
     * 异步获取功能图层 - 使用虚拟线程处理
     */
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> getFeatureLayerInfo(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            FeatureLayerInfo featureLayerInfo = management.getFeatureLayerInfo(id);
            if (featureLayerInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(featureLayerInfo);
        }, databaseOperationExecutor);
    }

    @PutMapping
    public ResponseEntity<?> updateFeatureLayerInfo(@RequestBody FeatureLayerInfo info) {
        management.updateFeatureLayerInfo(info);
        return ResponseEntity.ok().build();
    }

    /**
     * 异步分页查询 - 使用虚拟线程处理
     * 分页查询可能涉及大量数据处理，适合虚拟线程
     */
    @GetMapping("/page")
    public CompletableFuture<ResponseEntity<?>> pageFeatureLayerInfo(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "6") int size,
                                               @RequestParam(required = false) String name) {
        return CompletableFuture.supplyAsync(() -> {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("modified", "created").descending());
            return ResponseEntity.ok(management.pageFeatureLayerInfo(name, pageRequest));
        }, databaseOperationExecutor);
    }

    /**
     * 异步获取预览信息 - 使用虚拟线程处理
     * 预览信息生成可能涉及复杂的空间数据计算，适合虚拟线程
     */
    @GetMapping("/preview/{id}")
    public CompletableFuture<ResponseEntity<?>> getFeatureLayerPreview(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            FeatureLayerPreviewInfo previewInfo = management.getFeatureLayerPreviewInfo(id);
            if (previewInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(previewInfo);
        }, databaseOperationExecutor);
    }
}
