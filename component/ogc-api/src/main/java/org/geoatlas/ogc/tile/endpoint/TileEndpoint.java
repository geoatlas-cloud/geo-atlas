package org.geoatlas.ogc.tile.endpoint;

import org.geoatlas.metadata.FeatureSourceHelper;
import org.geoatlas.pyramid.Pyramid;
import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public TileEndpoint(Pyramid pyramid) {
        this.pyramid = pyramid;
    }

    @GetMapping("/{namespace}/{layer}/{schema}/{tileMatrixId}/{tileRow}/{tileCol}.{format}")
    public ResponseEntity<?> getTile(@PathVariable String namespace, @PathVariable String layer, @PathVariable String schema, @PathVariable int tileMatrixId,
                                     @PathVariable int tileRow, @PathVariable int tileCol, @PathVariable String format) {
        TileRequest request = new TileRequest(namespace, layer, schema, tileRow, tileCol, tileMatrixId, format);
        TileObject tile;
        try {
            tile = this.pyramid.getTile(request, FeatureSourceHelper.getFeatureSource(namespace, layer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (tile != null){
            return ResponseEntity.ok(tile.getBlob());
        }
        return ResponseEntity.notFound().build();
    }
}
