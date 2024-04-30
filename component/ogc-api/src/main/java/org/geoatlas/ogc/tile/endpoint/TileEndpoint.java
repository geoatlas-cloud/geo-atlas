package org.geoatlas.ogc.tile.endpoint;

import org.geoatlas.metadata.helper.FeatureSourceHelper;
import org.geoatlas.metadata.helper.FeatureSourceWrapper;
import org.geoatlas.pyramid.Pyramid;
import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;
import org.geoatlas.tile.util.ResponseUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 14:53
 * @since: 1.0
 **/
@RestController
@RequestMapping("/tiles")
public class TileEndpoint {

    private final Pyramid pyramid;
    private final FeatureSourceHelper featureSourceHelper;

    public TileEndpoint(Pyramid pyramid, FeatureSourceHelper featureSourceHelper) {
        this.pyramid = pyramid;
        this.featureSourceHelper = featureSourceHelper;
    }

    @CrossOrigin
    @GetMapping("/{namespace}/{layer}/{schema}/{tileMatrixId}/{tileRow}/{tileCol}.{format}")
    public void getTile(@PathVariable String namespace, @PathVariable String layer, @PathVariable String schema, @PathVariable int tileMatrixId,
                                     @PathVariable int tileRow, @PathVariable int tileCol, @PathVariable String format, HttpServletResponse response) {
        TileRequest request = new TileRequest(namespace, layer, schema, tileCol, tileRow, tileMatrixId, format);
        TileObject tile;
        try {
            FeatureSourceWrapper wrapper = featureSourceHelper.getFeatureSource(namespace, layer);
            tile = this.pyramid.getTile(request, wrapper.getFeatureSource(), wrapper.getCrs());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ResponseUtils.writeData(tile, response);
    }
}
