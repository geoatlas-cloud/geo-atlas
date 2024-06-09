package org.geoatlas.metadata.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/9 19:53
 * @since: 1.0
 **/
public class MetadataDeleteEvent extends ApplicationEvent {
    private static final long serialVersionUID = -1450249190463584037L;

    public MetadataDeleteEvent(Object source) {
        super(source);
    }
}
