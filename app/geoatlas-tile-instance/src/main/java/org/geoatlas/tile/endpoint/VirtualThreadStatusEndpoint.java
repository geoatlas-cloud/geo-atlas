package org.geoatlas.tile.endpoint;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 虚拟线程状态检查端点
 * 用于运行时验证虚拟线程功能
 * 
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @since: 1.0
 */
@RestController
@RequestMapping("/api/system")
public class VirtualThreadStatusEndpoint {

    private final ExecutorService tileGenerationExecutor;

    public VirtualThreadStatusEndpoint(@Qualifier("tileGenerationExecutor") ExecutorService tileGenerationExecutor) {
        this.tileGenerationExecutor = tileGenerationExecutor;
    }

    /**
     * 获取系统和虚拟线程状态信息
     */
    @GetMapping("/virtual-thread-status")
    public Map<String, Object> getVirtualThreadStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 基本系统信息
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("javaVendor", System.getProperty("java.vendor"));
        status.put("jvmVersion", System.getProperty("java.vm.version"));
        status.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        // 虚拟线程支持检查
        status.put("virtualThreadSupported", isVirtualThreadSupported());
        status.put("springVirtualThreadEnabled", System.getProperty("spring.threads.virtual.enabled"));
        
        // 当前线程信息
        Thread currentThread = Thread.currentThread();
        Map<String, Object> currentThreadInfo = new HashMap<>();
        currentThreadInfo.put("name", currentThread.getName());
        currentThreadInfo.put("id", currentThread.getId());
        currentThreadInfo.put("isVirtual", isCurrentThreadVirtual());
        status.put("currentThread", currentThreadInfo);
        
        return status;
    }

    /**
     * 测试虚拟线程执行
     */
    @GetMapping("/test-virtual-thread")
    public CompletableFuture<Map<String, Object>> testVirtualThread() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();
            Thread currentThread = Thread.currentThread();
            
            result.put("executionTime", System.currentTimeMillis());
            result.put("threadName", currentThread.getName());
            result.put("threadId", currentThread.getId());
            result.put("isVirtual", isCurrentThreadVirtual());
            result.put("message", "虚拟线程测试执行成功");
            
            // 模拟一些I/O操作
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return result;
        }, tileGenerationExecutor);
    }

    /**
     * 批量测试虚拟线程并发能力
     */
    @GetMapping("/test-virtual-thread-concurrent")
    public CompletableFuture<Map<String, Object>> testVirtualThreadConcurrent() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> result = new HashMap<>();
            int taskCount = 100;
            
            long startTime = System.currentTimeMillis();
            
            // 创建100个并发任务
            CompletableFuture<String>[] futures = new CompletableFuture[taskCount];
            for (int i = 0; i < taskCount; i++) {
                final int taskId = i;
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    Thread currentThread = Thread.currentThread();
                    try {
                        Thread.sleep(50); // 模拟I/O操作
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return String.format("Task-%d on %s (isVirtual: %s)", 
                            taskId, currentThread.getName(), isCurrentThreadVirtual());
                }, tileGenerationExecutor);
            }
            
            // 等待所有任务完成
            try {
                CompletableFuture.allOf(futures).get();
            } catch (Exception e) {
                result.put("error", e.getMessage());
                return result;
            }
            
            long endTime = System.currentTimeMillis();
            
            result.put("taskCount", taskCount);
            result.put("executionTimeMs", endTime - startTime);
            result.put("averageTimePerTask", (endTime - startTime) / (double) taskCount);
            result.put("message", "并发虚拟线程测试完成");
            
            return result;
        }, tileGenerationExecutor);
    }

    /**
     * 检查是否支持虚拟线程
     */
    private boolean isVirtualThreadSupported() {
        try {
            String version = System.getProperty("java.version");
            int majorVersion = parseMajorVersion(version);
            return majorVersion >= 21;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查当前线程是否为虚拟线程
     */
    private boolean isCurrentThreadVirtual() {
        // 直接使用JDK 21的原生API
        return Thread.currentThread().isVirtual();
    }

    /**
     * 解析Java主版本号
     */
    private int parseMajorVersion(String version) {
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2, version.indexOf('.', 2)));
        } else {
            int dotIndex = version.indexOf('.');
            int dashIndex = version.indexOf('-');
            int endIndex = Math.min(
                dotIndex == -1 ? version.length() : dotIndex,
                dashIndex == -1 ? version.length() : dashIndex
            );
            return Integer.parseInt(version.substring(0, endIndex));
        }
    }
} 