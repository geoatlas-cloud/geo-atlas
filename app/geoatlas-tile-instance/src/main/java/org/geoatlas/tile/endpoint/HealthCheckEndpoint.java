package org.geoatlas.tile.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/18 10:33
 * @since: 1.0
 **/
@RestController
@RequestMapping("/v1/health")
public class HealthCheckEndpoint {

    @GetMapping("/check")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
