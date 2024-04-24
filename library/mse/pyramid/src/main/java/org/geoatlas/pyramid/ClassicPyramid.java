package org.geoatlas.pyramid;

import org.geoatlas.pyramid.action.ActionPipeline;
import org.geoatlas.pyramid.action.vector.ClassicReadAction;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/24 17:02
 * @since: 1.0
 **/
public class ClassicPyramid extends AbstractPyramid{

    private ActionPipeline pipeline;

    public ClassicPyramid() {
        super(new ActionPipeline(new ClassicReadAction()));
    }

}
