package org.geoatlas.metadata.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/26 12:00
 * @since: 1.0
 **/
@Table("ga_pyramid_rule_expression")
public class PyramidRuleExpression implements Serializable {

    private static final long serialVersionUID = -2287911789786375132L;

    @Id
    private Long id;

    @Column("min_level")
    private int minLevel;

    @Column("max_level")
    private int maxLevel;

    @NotBlank(message = "pyramid rule filter expression can not be null.")
    private String filter;

    @Column("feature_layer_id")
    private Long featureLayerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @CreatedDate
    private Timestamp created;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @LastModifiedDate
    private Timestamp modified;

    public PyramidRuleExpression(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
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

    public Long getFeatureLayerId() {
        return featureLayerId;
    }

    public void setFeatureLayerId(Long featureLayerId) {
        this.featureLayerId = featureLayerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PyramidRuleExpression that = (PyramidRuleExpression) o;
        return minLevel == that.minLevel && maxLevel == that.maxLevel && Objects.equals(filter, that.filter) && Objects.equals(featureLayerId, that.featureLayerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minLevel, maxLevel, filter, featureLayerId);
    }
}
