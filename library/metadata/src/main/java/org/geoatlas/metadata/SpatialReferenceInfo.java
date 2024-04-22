package org.geoatlas.metadata;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 14:15
 * @since: 1.0
 **/
public class SpatialReferenceInfo {

    private String identifier;

    private String name;

    private int srid;

    private String authName;

    private int authSrid;

    private String wktText;

    private String proj4Text;

    private String description;

    private static final String EPSG_PREFIX = "EPSG:";

    public SpatialReferenceInfo() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
