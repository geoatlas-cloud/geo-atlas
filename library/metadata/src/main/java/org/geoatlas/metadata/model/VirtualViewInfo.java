package org.geoatlas.metadata.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/27 23:28
 * @since: 1.0
 **/
@Table("ga_virtual_view_info")
public class VirtualViewInfo {

    @Id
    private Long id;

    private String name;

    private String sql;

    /**
     * 逗号分割
     */
    @Column("pk_columns")
    private String pkColumns;

    @Column("geometry_column")
    private String geometryColumn;

    // FIXME: 2024/4/27 后续考虑自动识别?
    @Column("geometry_type")
    private int geometryType;

    // 数据库空间参考SRID, 考虑到存在数据表中srid与数据空间参考srid不一致的情况
    private int srid;

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
}
