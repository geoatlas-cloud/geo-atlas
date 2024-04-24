package org.geoatlas.pyramid;

import org.geoatlas.pyramid.action.ActionPipeline;
import org.geoatlas.pyramid.action.vector.RuleBasedReadAction;
import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;

import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 17:04
 * @since: 1.0
 **/
public class RuleBasedPyramid extends AbstractPyramid{

    public RuleBasedPyramid() {
        super(new ActionPipeline(new RuleBasedReadAction()));
    }
}
