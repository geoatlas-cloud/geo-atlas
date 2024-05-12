package org.geoatlas.pyramid.index;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/9 12:01
 * @since: 1.0
 **/
public class GeoAtlasPyramidException extends Exception {

    private static final long serialVersionUID = 5837933971679774370L;

    public GeoAtlasPyramidException(String msg) {
        super(msg);
    }

    public GeoAtlasPyramidException(Throwable thrw) {
        super(thrw);
    }

    public GeoAtlasPyramidException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
