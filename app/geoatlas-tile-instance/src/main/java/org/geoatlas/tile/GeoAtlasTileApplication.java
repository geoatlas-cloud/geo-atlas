package org.geoatlas.tile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 15:31
 * @since: 1.0
 **/
@SpringBootApplication
@ComponentScan("org.geoatlas")
@EnableAsync
public class GeoAtlasTileApplication {
    public static void main(String[] args) {
        // 系统信息输出
        System.out.println("=== GeoAtlas 启动信息 ===");
        System.out.println("Java版本: " + System.getProperty("java.version"));
        System.out.println("Java厂商: " + System.getProperty("java.vendor"));
        System.out.println("JVM版本: " + System.getProperty("java.vm.version"));
        
        // Setting the system-wide default at startup time
        System.setProperty("org.geotools.referencing.forceXY", "true");
        
        // JDK 21虚拟线程相关系统属性配置
        configureVirtualThreadProperties();
        
        // 启动前检查虚拟线程支持
        checkVirtualThreadSupport();
        
        // 启动Spring Boot应用
        SpringApplication.run(GeoAtlasTileApplication.class, args);
    }
    
    /**
     * 配置虚拟线程相关的系统属性
     */
    private static void configureVirtualThreadProperties() {
        // Spring Boot 3.2+ 虚拟线程配置
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        // 确保使用平台线程作为载体线程
        System.setProperty("jdk.virtualThreadScheduler.parallelism", 
            String.valueOf(Runtime.getRuntime().availableProcessors()));
            
        // 虚拟线程调试信息（开发环境）
        if (isDevProfile()) {
            System.setProperty("jdk.tracePinnedThreads", "full");
        }
        
        System.out.println("虚拟线程系统属性已配置");
    }
    
    /**
     * 检查是否为开发环境
     */
    private static boolean isDevProfile() {
        String profiles = System.getProperty("spring.profiles.active", "");
        return profiles.contains("dev") || profiles.isEmpty();
    }
    
    /**
     * 检查虚拟线程支持 - 暂时移除测试，让Spring Boot配置处理
     */
    private static void checkVirtualThreadSupport() {
        System.out.println("=== 虚拟线程状态检查 ===");
        System.out.println("虚拟线程配置将由Spring Boot 3.x自动处理");
        System.out.println("请启动后访问 /api/system/virtual-thread-status 端点验证");
    }
}
