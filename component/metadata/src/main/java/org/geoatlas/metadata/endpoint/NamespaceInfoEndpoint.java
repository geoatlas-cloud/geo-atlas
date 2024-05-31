package org.geoatlas.metadata.endpoint;

import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.metadata.persistence.managent.NamespaceInfoManagement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 11:28
 * @since: 1.0
 **/
@RestController
@RequestMapping("/v1/metadata/namespaces")
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

    @GetMapping("/feature_layers/{id}")
    public ResponseEntity<?> getNamespaceInfoByFeatureLayerId(@PathVariable Long id) {
        NamespaceInfo namespaceInfo = management.getNamespaceInfoByFeatureLayerId(id);
        if (namespaceInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(namespaceInfo);
    }

    @GetMapping("/page")
    public ResponseEntity<?> pageNamespaceInfo(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "6") int size,
                                               @RequestParam(required = false) String name) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("modified", "created").descending());
        return ResponseEntity.ok(management.pageNamespaceInfo(name, pageRequest));
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(){
        return ResponseEntity.ok(management.list());
    }
}
