package org.geoatlas.pyramid;

import org.geoatlas.pyramid.action.ActionPipeline;
import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;
import org.geotools.data.simple.SimpleFeatureSource;

import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 17:04
 * @since: 1.0
 **/
public abstract class AbstractPyramid implements Pyramid{

    private ActionPipeline pipeline;

    protected AbstractPyramid(ActionPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public TileObject getTile(TileRequest request, SimpleFeatureSource featureSource) throws IOException {
        return pipeline.doAction(request, featureSource);
    }
}
