package org.geoatlas.metadata.event;

import org.geoatlas.metadata.model.NamespaceInfo;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/9 19:55
 * @since: 1.0
 **/
public class FeatureLayerDeleteEvent extends MetadataDeleteEvent{
    private static final long serialVersionUID = 1325942131107992218L;

    private final NamespaceInfo namespace;

    public FeatureLayerDeleteEvent(Object source, NamespaceInfo namespace) {
        super(source);
        this.namespace = namespace;
    }

    public NamespaceInfo getNamespace() {
        return namespace;
    }
}
