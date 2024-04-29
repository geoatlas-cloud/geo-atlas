package org.geoatlas.tile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 15:31
 * @since: 1.0
 **/
@SpringBootApplication
@ComponentScan("org.geoatlas")
public class GeoAtlasTileApplication {
    public static void main(String[] args) {
        // Setting the system-wide default at startup time
        System.setProperty("org.geotools.referencing.forceXY", "true");
        SpringApplication.run(GeoAtlasTileApplication.class, args);
    }
}
