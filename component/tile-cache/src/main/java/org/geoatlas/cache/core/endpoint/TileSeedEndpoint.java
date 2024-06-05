package org.geoatlas.cache.core.endpoint;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.seed.SeedRequest;
import org.geoatlas.cache.core.service.TileSeedService;
import org.geoatlas.cache.core.storage.StorageBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

/**
 * Geo Atlas Cache -> gac
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/4 20:43
 * @since: 1.0
 **/
@ResponseBody
@RequestMapping("/v1/gac/rest")
public class TileSeedEndpoint {

    private final TileSeedService tileSeedService;

    public TileSeedEndpoint(TileSeedService tileSeedService) {
        this.tileSeedService = tileSeedService;
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seed(@Valid SeedRequest request) throws GeoAtlasCacheException {
        tileSeedService.doSeed(request);
        return ResponseEntity.ok().build();
    }
}
