package org.geoatlas.metadata.endpoint;

import org.geoatlas.metadata.model.SpatialReferenceInfo;
import org.geoatlas.metadata.persistence.managent.SpatialReferenceInfoManagement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/24 18:43
 * @since: 1.0
 **/
@RestController
@RequestMapping("/v1/metadata/spatial_refs")
public class SpatialReferenceInfoEndpoint {

    private final SpatialReferenceInfoManagement management;

    public SpatialReferenceInfoEndpoint(SpatialReferenceInfoManagement management) {
        this.management = management;
    }

    @GetMapping("/page")
    public ResponseEntity<?> pageSpatialReferenceInfo(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "6") int size,
                                               @RequestParam(required = false) String name) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("modified", "created").descending());
        return ResponseEntity.ok(management.pageSpatialReferenceInfo(name, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSpatialReferenceInfo(@PathVariable Long id) {
        SpatialReferenceInfo spatialReferenceInfo = management.getSpatialReferenceInfo(id);
        if (spatialReferenceInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spatialReferenceInfo);
    }
}
