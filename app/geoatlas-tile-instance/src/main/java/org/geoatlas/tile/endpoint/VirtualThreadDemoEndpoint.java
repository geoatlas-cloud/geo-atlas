package org.geoatlas.tile.endpoint;

import org.geoatlas.tile.service.VirtualThreadExampleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 虚拟线程演示端点
 * 展示虚拟线程在实际应用中的使用方式和性能优势
 * 
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @since: 1.0
 **/
@RestController
@RequestMapping("/v1/demo/virtual-threads")
public class VirtualThreadDemoEndpoint {

    private final VirtualThreadExampleService exampleService;

    public VirtualThreadDemoEndpoint(VirtualThreadExampleService exampleService) {
        this.exampleService = exampleService;
    }

    /**
     * 演示批量瓦片生成的虚拟线程处理
     */
    @PostMapping("/tiles/batch")
    public CompletableFuture<ResponseEntity<List<String>>> generateTilesBatch(@RequestBody List<String> tileRequests) {
        return exampleService.generateTilesBatch(tileRequests)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * 演示数据处理管道的虚拟线程处理
     */
    @GetMapping("/data/pipeline/{dataId}")
    public CompletableFuture<ResponseEntity<String>> processDataPipeline(@PathVariable String dataId) {
        return exampleService.processDataPipeline(dataId)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * 演示外部API聚合的虚拟线程处理
     */
    @PostMapping("/api/aggregate")
    public CompletableFuture<ResponseEntity<String>> aggregateExternalData(@RequestBody List<String> apiEndpoints) {
        return exampleService.aggregateExternalData(apiEndpoints)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * 演示@Async注解的虚拟线程处理
     */
    @GetMapping("/tiles/async/{tileId}")
    public CompletableFuture<ResponseEntity<String>> asyncTileGeneration(@PathVariable String tileId) {
        return exampleService.asyncTileGeneration(tileId)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * 演示虚拟线程性能监控
     */
    @GetMapping("/performance/test")
    public CompletableFuture<ResponseEntity<String>> performanceTest() {
        return exampleService.performanceMonitoringExample()
                .thenApply(ResponseEntity::ok);
    }

    /**
     * 快速演示端点 - 使用预设数据
     */
    @GetMapping("/quick-demo")
    public CompletableFuture<ResponseEntity<String>> quickDemo() {
        // 模拟一些瓦片请求
        List<String> tileRequests = Arrays.asList(
                "tile-001", "tile-002", "tile-003", "tile-004", "tile-005"
        );
        
        return exampleService.generateTilesBatch(tileRequests)
                .thenApply(results -> ResponseEntity.ok(
                        "虚拟线程演示完成！\n" +
                        "处理了 " + results.size() + " 个瓦片请求\n" +
                        "结果: " + String.join(", ", results)
                ));
    }

    /**
     * 虚拟线程vs传统线程对比演示
     */
    @GetMapping("/comparison")
    public ResponseEntity<String> comparisonInfo() {
        String info = "虚拟线程 vs 传统线程对比:\n\n" +
                "🚀 虚拟线程优势:\n" +
                "• 轻量级: 创建成本极低，可以创建数百万个\n" +
                "• 高并发: 非常适合I/O密集型操作\n" +
                "• 简化代码: 可以使用同步编程风格处理异步操作\n" +
                "• 资源效率: 不会阻塞系统线程\n\n" +
                "💻 适用场景:\n" +
                "• 瓦片生成和处理\n" +
                "• 数据库I/O操作\n" +
                "• 网络API调用\n" +
                "• 文件读写操作\n" +
                "• 缓存操作\n\n" +
                "⚡ 在GeoAtlas中的应用:\n" +
                "• TileEndpoint: 并行瓦片生成\n" +
                "• FeatureLayerInfoEndpoint: 异步数据库操作\n" +
                "• TileSeedEndpoint: 缓存种子生成\n" +
                "• VirtualThreadExampleService: 最佳实践示例\n\n" +
                "📊 性能提升:\n" +
                "• 在高并发场景下，吞吐量可提升10-100倍\n" +
                "• 内存使用更高效\n" +
                "• 响应时间更短";
        
        return ResponseEntity.ok(info);
    }
} 