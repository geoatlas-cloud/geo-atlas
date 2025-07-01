package org.geoatlas.tile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 虚拟线程使用示例服务
 * 展示在GeoAtlas项目中如何正确使用虚拟线程的最佳实践
 * 
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @since: 1.0
 **/
@Service
public class VirtualThreadExampleService {
    
    private static final Logger log = LoggerFactory.getLogger(VirtualThreadExampleService.class);
    
    private final ExecutorService tileGenerationExecutor;
    private final ExecutorService databaseOperationExecutor;
    private final ExecutorService cacheOperationExecutor;
    private final ExecutorService externalApiExecutor;

    public VirtualThreadExampleService(
            @Qualifier("tileGenerationExecutor") ExecutorService tileGenerationExecutor,
            @Qualifier("databaseOperationExecutor") ExecutorService databaseOperationExecutor,
            @Qualifier("cacheOperationExecutor") ExecutorService cacheOperationExecutor,
            @Qualifier("externalApiExecutor") ExecutorService externalApiExecutor) {
        this.tileGenerationExecutor = tileGenerationExecutor;
        this.databaseOperationExecutor = databaseOperationExecutor;
        this.cacheOperationExecutor = cacheOperationExecutor;
        this.externalApiExecutor = externalApiExecutor;
    }

    /**
     * 示例1: 并行瓦片生成
     * 虚拟线程最佳实践：I/O密集型操作
     */
    public CompletableFuture<List<String>> generateTilesBatch(List<String> tileRequests) {
        log.info("开始批量生成瓦片，数量: {}", tileRequests.size());
        
        // 使用虚拟线程并行处理瓦片生成
        List<CompletableFuture<String>> futures = tileRequests.stream()
                .map(request -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // 模拟瓦片生成过程（I/O密集型）
                        Thread.sleep(100); // 模拟数据库查询
                        Thread.sleep(200); // 模拟图像处理
                        log.debug("生成瓦片: {}", request);
                        return "瓦片生成完成: " + request;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("瓦片生成被中断", e);
                    }
                }, tileGenerationExecutor))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    /**
     * 示例2: 数据库操作管道
     * 虚拟线程优势：高并发数据库I/O
     */
    public CompletableFuture<String> processDataPipeline(String dataId) {
        return CompletableFuture
                // 步骤1: 从数据库读取数据
                .supplyAsync(() -> {
                    try {
                        Thread.sleep(50); // 模拟数据库查询
                        log.debug("读取数据: {}", dataId);
                        return "数据内容-" + dataId;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("数据读取失败", e);
                    }
                }, databaseOperationExecutor)
                
                // 步骤2: 处理数据并存储到缓存
                .thenComposeAsync(data -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(30); // 模拟缓存操作
                        log.debug("缓存数据: {}", data);
                        return "已缓存-" + data;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("缓存操作失败", e);
                    }
                }, cacheOperationExecutor))
                
                // 步骤3: 更新数据库状态
                .thenComposeAsync(cachedData -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(40); // 模拟数据库更新
                        log.debug("更新状态: {}", cachedData);
                        return "处理完成-" + cachedData;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("状态更新失败", e);
                    }
                }, databaseOperationExecutor));
    }

    /**
     * 示例3: 外部API调用聚合
     * 虚拟线程优势：网络I/O并发处理
     */
    public CompletableFuture<String> aggregateExternalData(List<String> apiEndpoints) {
        log.info("开始聚合外部API数据，端点数量: {}", apiEndpoints.size());
        
        List<CompletableFuture<String>> apiFutures = apiEndpoints.stream()
                .map(endpoint -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // 模拟外部API调用
                        Thread.sleep(150 + (int)(Math.random() * 100)); // 模拟网络延迟
                        log.debug("调用API: {}", endpoint);
                        return "API响应-" + endpoint;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("API调用失败: " + endpoint, e);
                    }
                }, externalApiExecutor))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(apiFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    String result = apiFutures.stream()
                            .map(CompletableFuture::join)
                            .reduce("", (a, b) -> a + "; " + b);
                    log.info("API数据聚合完成");
                    return result;
                });
    }

    /**
     * 示例4: 使用@Async注解的虚拟线程
     * Spring的异步注解会自动使用配置的虚拟线程执行器
     */
    @Async("tileGenerationExecutor")
    public CompletableFuture<String> asyncTileGeneration(String tileId) {
        try {
            log.info("开始异步生成瓦片: {}", tileId);
            Thread.sleep(200); // 模拟瓦片生成
            String result = "异步瓦片生成完成: " + tileId;
            log.info(result);
            return CompletableFuture.completedFuture(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("异步瓦片生成失败", e));
            return future;
        }
    }

    /**
     * 示例5: 虚拟线程性能监控
     * 监控虚拟线程的使用情况
     */
    public CompletableFuture<String> performanceMonitoringExample() {
        long startTime = System.currentTimeMillis();
        
        // 创建大量并发任务来展示虚拟线程的优势
        List<CompletableFuture<String>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final int taskId = i;
            tasks.add(CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(10); // 短暂的I/O模拟
                    return "Task-" + taskId + " 在线程: " + Thread.currentThread().getName();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Task-" + taskId + " 被中断";
                }
            }, tileGenerationExecutor));
        }

        return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    
                    String report = String.format(
                        "性能测试完成 - 执行了1000个并发任务，耗时: %d毫秒，平均每任务: %.2f毫秒",
                        duration, 
                        (double) duration / 1000
                    );
                    
                    log.info(report);
                    return report;
                });
    }
} 