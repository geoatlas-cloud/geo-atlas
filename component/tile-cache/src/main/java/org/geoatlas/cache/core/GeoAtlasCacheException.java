package org.geoatlas.cache.core;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/9 12:01
 * @since: 1.0
 **/
public class GeoAtlasCacheException extends Exception {

    private static final long serialVersionUID = 5837933971679774371L;

    public GeoAtlasCacheException(String msg) {
        super(msg);
    }

    public GeoAtlasCacheException(Throwable thrw) {
        super(thrw);
    }

    public GeoAtlasCacheException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
