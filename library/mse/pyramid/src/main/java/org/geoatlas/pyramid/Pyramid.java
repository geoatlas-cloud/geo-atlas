package org.geoatlas.pyramid;

import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;

import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 9:42
 * @since: 1.0
 **/
public interface Pyramid {
    TileObject getTile(TileRequest request) throws IOException;
}
