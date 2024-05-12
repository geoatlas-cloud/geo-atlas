package org.geoatlas.ogc.tile.endpoint;

import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.ogc.tile.dispatcher.GeoAtlasTileDispatcher;
import org.geoatlas.ogc.tile.util.ResponseUtils;
import org.geoatlas.tile.TileRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 14:53
 * @since: 1.0
 **/
@RestController
@RequestMapping("/tiles")
public class TileEndpoint {

    private final GeoAtlasTileDispatcher dispatcher;

    public TileEndpoint(GeoAtlasTileDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @CrossOrigin
    @GetMapping("/{namespace}/{layer}/{schema}/{tileMatrixId}/{tileRow}/{tileCol}.{format}")
    public void getTile(@PathVariable String namespace, @PathVariable String layer, @PathVariable String schema, @PathVariable int tileMatrixId,
                                     @PathVariable int tileRow, @PathVariable int tileCol, @PathVariable String format,
                        HttpServletRequest servletRequest,
                        HttpServletResponse response) {
        TileRequest request = new TileRequest(namespace, layer, schema, tileCol, tileRow, tileMatrixId, format);
        ConveyorTile tile;
        try {
            tile = dispatcher.dispatch(request, servletRequest, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ResponseUtils.writeTile(tile);
    }
}
