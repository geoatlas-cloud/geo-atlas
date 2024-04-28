package org.geoatlas.metadata.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 14:15
 * @since: 1.0
 **/
@Table("ga_spatial_reference_info")
public class SpatialReferenceInfo {

    @Id
    private Long id;

    private String name;

    private int srid;

    @Column("auth_name")
    private String authName;

    @Column("auth_srid")
    private int authSrid;

    @Column("wkt_text")
    private String wktText;

    @Column("proj4_text")
    private String proj4Text;

    private String description;

    private static final String EPSG_PREFIX = "EPSG:";

    public SpatialReferenceInfo() {
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

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public int getAuthSrid() {
        return authSrid;
    }

    public void setAuthSrid(int authSrid) {
        this.authSrid = authSrid;
    }

    // For EPSG Code, such as EPSG:4490
    public String getCode() {
        return EPSG_PREFIX + srid;
    }


    public String getWktText() {
        return wktText;
    }

    public void setWktText(String wktText) {
        this.wktText = wktText;
    }

    public String getProj4Text() {
        return proj4Text;
    }

    public void setProj4Text(String proj4Text) {
        this.proj4Text = proj4Text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
