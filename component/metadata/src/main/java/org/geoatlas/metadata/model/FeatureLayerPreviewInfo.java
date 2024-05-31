package org.geoatlas.metadata.model;

import org.locationtech.jts.geom.Coordinate;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/29 15:38
 * @since: 1.0
 **/
public class FeatureLayerPreviewInfo {

    private Long id;
    private String name;
    private String namespace;

    private FeatureBBoxInfo bbox;

    private Coordinate center;

    private int zoom = 3;

    public FeatureLayerPreviewInfo(){}

    public FeatureLayerPreviewInfo(Long id, String name, String namespace){
        this(id, name, namespace, null, null);
    }

    public FeatureLayerPreviewInfo(Long id, String name, String namespace, FeatureBBoxInfo bbox, Coordinate center){
        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.bbox = bbox;
        this.center = center;
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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public FeatureBBoxInfo getBbox() {
        return bbox;
    }

    public void setBbox(FeatureBBoxInfo bbox) {
        this.bbox = bbox;
    }

    public Coordinate getCenter() {
        return center;
    }

    public void setCenter(Coordinate center) {
        this.center = center;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
}
