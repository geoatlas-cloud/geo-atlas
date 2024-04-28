package org.geoatlas.metadata.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 23:28
 * @since: 1.0
 **/
@Table("ga_virtual_view_info")
public class VirtualViewInfo {

    @Id
    private Long id;

    @NotBlank(message = "virtual view name can not be null.")
    private String name;

    @NotBlank(message = "virtual view sql can not be null.")
    private String sql;

    /**
     * 逗号分割
     */
    @NotBlank(message = "virtual view pk columns can not be null.")
    @Column("pk_columns")
    private String pkColumns;

    @NotBlank(message = "virtual view geometry column can not be null.")
    @Column("geometry_column")
    private String geometryColumn;

    // FIXME: 2024/4/27 后续考虑自动识别?
    @NotNull(message = "virtual view geometry type can not be null.")
    @Column("geometry_type")
    private int geometryType;

    // 数据库空间参考SRID, 考虑到存在数据表中srid与数据空间参考srid不一致的情况
    @NotNull(message = "virtual view srid can not be null.")
    private int srid;

    @Column("feature_layer_id")
    private Long featureLayerId;

    @CreatedDate
    private Instant created;

    @LastModifiedDate
    private Instant modified;

    public VirtualViewInfo() {
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

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getPkColumns() {
        return pkColumns;
    }

    public void setPkColumns(String pkColumns) {
        this.pkColumns = pkColumns;
    }

    public String getGeometryColumn() {
        return geometryColumn;
    }

    public void setGeometryColumn(String geometryColumn) {
        this.geometryColumn = geometryColumn;
    }

    public int getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(int geometryType) {
        this.geometryType = geometryType;
    }

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public Long getFeatureLayerId() {
        return featureLayerId;
    }

    public void setFeatureLayerId(Long featureLayerId) {
        this.featureLayerId = featureLayerId;
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
