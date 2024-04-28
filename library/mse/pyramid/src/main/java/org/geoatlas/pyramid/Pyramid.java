package org.geoatlas.pyramid;

import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;
import org.geotools.data.simple.SimpleFeatureSource;

import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 9:42
 * @since: 1.0
 **/
public interface Pyramid {

    /**
     * 数据源不是金字塔托管, 且当前SQLView也并未纳入到金字塔的Rule规则中, 所以此处直接传入SimpleFeatureSource即可
     * @param request
     * @param dataSource
     * @return
     * @throws IOException
     */
    TileObject getTile(TileRequest request, SimpleFeatureSource dataSource) throws IOException;
}
