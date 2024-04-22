package org.geoatlas.pyramid.action;

import org.geoatlas.tile.TileRequest;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:48
 * @since: 1.0
 **/
public class ActionContext {

    private final TileRequest request;

    private final SimpleFeatureSource featureSource;

    /**
     * 用于Simplify阶段简化, 减少内存使用
     */
    private SimpleFeatureIterator simplifyReader;

    /**
     * 后续阶段共享使用
     */
    private DefaultFeatureCollection content;

    public ActionContext(TileRequest request, SimpleFeatureSource featureSource) {
        this.request = request;
        this.featureSource = featureSource;
    }

    public TileRequest getRequest() {
        return request;
    }

    public SimpleFeatureSource getFeatureSource() {
        return featureSource;
    }

    public SimpleFeatureIterator getSimplifyReader() {
        return simplifyReader;
    }

    public void setSimplifyReader(SimpleFeatureIterator simplifyReader) {
        this.simplifyReader = simplifyReader;
    }

    public DefaultFeatureCollection getContent() {
        return content;
    }

    public void setContent(DefaultFeatureCollection content) {
        this.content = content;
    }
}
