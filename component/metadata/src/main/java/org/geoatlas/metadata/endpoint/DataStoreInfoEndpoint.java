package org.geoatlas.metadata.endpoint;

import org.geoatlas.metadata.model.DataStoreInfo;
import org.geoatlas.metadata.persistence.managent.DataStoreInfoManagement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 11:37
 * @since: 1.0
 **/
@RestController
@RequestMapping("/metadata/datastores")
public class DataStoreInfoEndpoint {

    private final DataStoreInfoManagement management;

    public DataStoreInfoEndpoint(DataStoreInfoManagement management) {
        this.management = management;
    }

    @PostMapping
    public ResponseEntity<?> addDataStoreInfo(@RequestBody DataStoreInfo info) {
        management.addDataStoreInfo(info);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeDataStoreInfo(@PathVariable Long id) {
        management.removeDataStoreInfo(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<?> updateDataStoreInfo(@RequestBody DataStoreInfo info) {
        management.updateDataStoreInfo(info);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDataStoreInfo(@PathVariable Long id) {
        DataStoreInfo dataStoreInfo = management.getDataStoreInfo(id);
        if (dataStoreInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dataStoreInfo);
    }
}
