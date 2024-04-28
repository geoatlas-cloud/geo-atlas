package org.geoatlas.metadata.endpoint;

import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.persistence.managent.FeatureLayerInfoManagement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 11:42
 * @since: 1.0
 **/
@RestController
@RequestMapping("/metadata/feature_layers")
public class FeatureLayerInfoEndpoint {

    private final FeatureLayerInfoManagement management;

    public FeatureLayerInfoEndpoint(FeatureLayerInfoManagement management) {
        this.management = management;
    }

    @PostMapping
    public ResponseEntity<?> addFeatureLayerInfo(@RequestBody FeatureLayerInfo info) {
        management.addFeatureLayerInfo(info);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFeatureLayerInfo(@PathVariable Long id) {
        management.removeFeatureLayerInfo(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFeatureLayerInfo(@PathVariable Long id) {
        FeatureLayerInfo featureLayerInfo = management.getFeatureLayerInfo(id);
        if (featureLayerInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(featureLayerInfo);
    }
}
