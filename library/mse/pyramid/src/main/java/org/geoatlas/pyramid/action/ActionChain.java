package org.geoatlas.pyramid.action;

import org.geoatlas.tile.TileObject;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:09
 * @since: 1.0
 **/
public interface ActionChain {

    TileObject doAction() throws IOException, SQLException;

    void release();
}
