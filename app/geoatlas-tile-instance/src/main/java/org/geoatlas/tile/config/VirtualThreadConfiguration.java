package org.geoatlas.tile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 虚拟线程配置类 - JDK 21最佳实践
 * 配置Spring Boot 3和JDK 21的虚拟线程支持
 * 
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @since: 1.0
 **/
@Configuration
@EnableAsync
public class VirtualThreadConfiguration implements AsyncConfigurer, WebMvcConfigurer {

    /**
     * 创建虚拟线程执行器（JDK 21原生API）
     * 直接使用JDK 21的虚拟线程API，无需反射
     */
    private ExecutorService createVirtualThreadExecutor(String namePrefix) {
        System.out.println("当前Java版本: " + System.getProperty("java.version"));
        
        // 直接使用JDK 21的原生虚拟线程API
        ThreadFactory virtualThreadFactory = Thread.ofVirtual()
                .name(namePrefix + "-virtual-", 0)
                .factory();
        
        ExecutorService executor = Executors.newThreadPerTaskExecutor(virtualThreadFactory);
        System.out.println("✅ 虚拟线程已启用: " + namePrefix);
        return executor;
    }

    /**
     * 主虚拟线程执行器 - 用于通用异步任务
     */
    @Bean("virtualThreadTaskExecutor")
    public AsyncTaskExecutor virtualThreadTaskExecutor() {
        return new TaskExecutorAdapter(createVirtualThreadExecutor("geo-atlas-main"));
    }

    /**
     * 专用于瓦片生成的虚拟线程池
     * 瓦片生成是I/O密集型操作，非常适合虚拟线程
     */
    @Bean("tileGenerationExecutor")
    public ExecutorService tileGenerationExecutor() {
        return createVirtualThreadExecutor("geo-atlas-tile");
    }

    /**
     * 专用于缓存操作的虚拟线程池
     * 缓存读写操作适合虚拟线程处理
     */
    @Bean("cacheOperationExecutor")
    public ExecutorService cacheOperationExecutor() {
        return createVirtualThreadExecutor("geo-atlas-cache");
    }

    /**
     * 专用于数据库操作的虚拟线程池
     * 数据库I/O操作是虚拟线程的理想用例
     */
    @Bean("databaseOperationExecutor")
    public ExecutorService databaseOperationExecutor() {
        return createVirtualThreadExecutor("geo-atlas-db");
    }

    /**
     * 专用于外部API调用的虚拟线程池
     * 网络I/O调用适合虚拟线程
     */
    @Bean("externalApiExecutor")
    public ExecutorService externalApiExecutor() {
        return createVirtualThreadExecutor("geo-atlas-api");
    }

    /**
     * 为@Async注解配置默认虚拟线程执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return createVirtualThreadExecutor("geo-atlas-async");
    }

    /**
     * 配置Web异步请求使用虚拟线程
     * Spring Boot 3.2+已原生支持，这里仅作补充配置
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(virtualThreadTaskExecutor());
        configurer.setDefaultTimeout(60000); // 60秒超时，适合复杂的瓦片生成操作
    }
} 