package org.geoatlas.metadata;

import java.io.Serializable;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 10:08
 * @since: 1.0
 **/
public class NamespaceInfo implements Serializable {

    private static final long serialVersionUID = -4363520941758282505L;

    private String name;

    private String description;

    private String uri;

    public NamespaceInfo() {
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
}
