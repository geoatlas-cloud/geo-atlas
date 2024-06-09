package org.geoatlas.metadata.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/9 11:13
 * @since: 1.0
 **/
public class MetadataUpdateEvent extends ApplicationEvent {
    private static final long serialVersionUID = -34035668317464925L;

    private final Object current;

    public MetadataUpdateEvent(Object last, Object current) {
        super(last);
        this.current = current;
    }


    public Object getCurrent() {
        return current;
    }
}
