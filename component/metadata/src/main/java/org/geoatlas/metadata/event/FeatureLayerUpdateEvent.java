package org.geoatlas.metadata.event;

import org.geoatlas.metadata.model.NamespaceInfo;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/9 11:34
 * @since: 1.0
 **/
public class FeatureLayerUpdateEvent extends MetadataUpdateEvent {
    private static final long serialVersionUID = 2980529005338998856L;

    private final NamespaceInfo namespace;

    public FeatureLayerUpdateEvent(Object last, Object current, NamespaceInfo namespace) {
        super(last, current);
        this.namespace = namespace;
    }

    public NamespaceInfo getNamespace() {
        return namespace;
    }
}
