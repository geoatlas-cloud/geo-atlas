package org.geoatlas.metadata.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/29 20:59
 * @since: 1.0
 **/
@Table("ga_feature_bbox_info")
public class FeatureBBoxInfo implements Serializable {

    private static final long serialVersionUID = -3829080433530653144L;

    @Id
    private Long id;

    @Column("min_x")
    private Double minx;

    @Column("min_y")
    private Double miny;

    @Column("max_x")
    private Double maxx;

    @Column("max_y")
    private Double maxy;

    private Boolean natived;

    @Column("feature_layer_id")
    private Long featureLayerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreatedDate
    private Timestamp created;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @LastModifiedDate
    private Timestamp modified;

    public FeatureBBoxInfo(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getMinx() {
        return minx;
    }

    public void setMinx(Double minx) {
        this.minx = minx;
    }

    public Double getMiny() {
        return miny;
    }

    public void setMiny(Double miny) {
        this.miny = miny;
    }

    public Double getMaxx() {
        return maxx;
    }

    public void setMaxx(Double maxx) {
        this.maxx = maxx;
    }

    public Double getMaxy() {
        return maxy;
    }

    public void setMaxy(Double maxy) {
        this.maxy = maxy;
    }

    public Boolean getNatived() {
        return natived;
    }

    public void setNatived(Boolean natived) {
        this.natived = natived;
    }

    public Long getFeatureLayerId() {
        return featureLayerId;
    }

    public void setFeatureLayerId(Long featureLayerId) {
        this.featureLayerId = featureLayerId;
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
