package org.geoatlas.cache.core.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/9 22:02
 * @since: 1.0
 **/
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({DefaultBlobStoreConfiguration.class})
public @interface EnableTileCache {
}
