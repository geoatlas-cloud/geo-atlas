package org.geoatlas.metadata.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;

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

    @MappedCollection(idColumn = "feature_layer_id", keyColumn = "id")
    private List<PyramidRuleExpression> rules;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreatedDate
    private Timestamp created;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @LastModifiedDate
    private Timestamp modified;

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

    public List<PyramidRuleExpression> getRules() {
        return rules;
    }

    public void setRules(List<PyramidRuleExpression> rules) {
        this.rules = rules;
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

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getModified() {
        return modified;
    }

    public void setModified(Timestamp modified) {
        this.modified = modified;
    }
}
