package org.geoatlas.metadata.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.Instant;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 10:08
 * @since: 1.0
 **/
@Table("ga_namespace_info")
public class NamespaceInfo implements Serializable {

    private static final long serialVersionUID = -4363520941758282505L;

    @Id
    private Long id;

    @NotBlank(message = "name can not be null.")
    private String name;

    private String description;

    private String uri;

    @CreatedDate
    private Instant created;

    @LastModifiedDate
    private Instant modified;

    public NamespaceInfo() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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
