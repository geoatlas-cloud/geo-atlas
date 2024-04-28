package org.geoatlas.metadata.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 9:49
 * @since: 1.0
 **/
@Table("ga_feature_layer_info")
public class FeatureLayerInfo {

    @Id
    private Long id;
    @NotBlank(message = "feature layer name is required.")
    private String name;

    @NotNull(message = "namespaceId is required.")
    @Column("namespace_id")
    private Long namespaceId;

    // 如果指定, 将会覆盖数据库读取到的空间参考, 可以为空
    @Column("spatial_ref_id")
    private Long spatialReferenceId;

    @NotNull(message = "datastoreId is required.")
    @Column("datastore_id")
    private Long datastoreId;

    @NotNull(message = "view is required.")
    @MappedCollection(idColumn = "feature_layer_id")
    private VirtualViewInfo view;

    private String description;

    @CreatedDate
    private Instant created;

    @LastModifiedDate
    private Instant modified;

    public FeatureLayerInfo() {
    }

    public Long getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(Long namespaceId) {
        this.namespaceId = namespaceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VirtualViewInfo getView() {
        return view;
    }

    public void setView(VirtualViewInfo view) {
        this.view = view;
    }

    public Long getSpatialReferenceId() {
        return spatialReferenceId;
    }

    public void setSpatialReferenceId(Long spatialReferenceId) {
        this.spatialReferenceId = spatialReferenceId;
    }

    public Long getDatastoreId() {
        return datastoreId;
    }

    public void setDatastoreId(Long datastoreId) {
        this.datastoreId = datastoreId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getModified() {
        return modified;
    }

    public void setModified(Instant modified) {
        this.modified = modified;
    }
}
