package org.geoatlas.metadata.endpoint;

import org.geoatlas.metadata.model.DataStoreInfo;
import org.geoatlas.metadata.persistence.managent.DataStoreInfoManagement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 11:37
 * @since: 1.0
 **/
@RestController
@RequestMapping("/v1/metadata/datastores")
public class DataStoreInfoEndpoint {

    private final DataStoreInfoManagement management;

    public DataStoreInfoEndpoint(DataStoreInfoManagement management) {
        this.management = management;
    }

    @PostMapping
    public ResponseEntity<?> addDataStoreInfo(@RequestBody DataStoreInfo info) {
        // FIXME: 2024/5/24 目前密码使用明文传递
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
        // FIXME: 2024/5/24 当前为了方便处理, 直接传输明文
        DataStoreInfo dataStoreInfo = management.getDataStoreInfo(id, Boolean.TRUE);
        if (dataStoreInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dataStoreInfo);
    }

    @GetMapping("/page")
    public ResponseEntity<?> pageDatastoreInfo(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "6") int size,
                                               @RequestParam(required = false) String name) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("modified", "created").descending());
        return ResponseEntity.ok(management.pageDatastoreInfo(name, pageRequest));
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(){
        return ResponseEntity.ok(management.list());
    }
}
