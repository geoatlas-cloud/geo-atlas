package org.geoatlas.metadata.endpoint;

import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.persistence.managent.NamespaceInfoManagement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 11:28
 * @since: 1.0
 **/
@RestController
@RequestMapping("/metadata/namespaces")
public class NamespaceInfoEndpoint {

    private final NamespaceInfoManagement management;

    public NamespaceInfoEndpoint(NamespaceInfoManagement management) {
        this.management = management;
    }

    @PostMapping
    public ResponseEntity<?> addNamespaceInfo(@RequestBody NamespaceInfo info) {
        management.addNamespaceInfo(info);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> removeNamespaceInfo(@PathVariable Long id) {
        management.removeNamespaceInfo(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<?> updateNamespaceInfo(@RequestBody @Valid NamespaceInfo info) {
        management.updateNamespaceInfo(info);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNamespaceInfo(@PathVariable Long id) {
        NamespaceInfo namespaceInfo = management.getNamespaceInfo(id);
        if (namespaceInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(namespaceInfo);
    }
}
