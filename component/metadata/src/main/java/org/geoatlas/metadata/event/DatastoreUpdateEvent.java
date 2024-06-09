package org.geoatlas.metadata.event;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/9 11:34
 * @since: 1.0
 **/
public class DatastoreUpdateEvent extends MetadataUpdateEvent {
    private static final long serialVersionUID = 5221995873709010398L;

    public DatastoreUpdateEvent(Object last, Object current) {
        super(last, current);
    }
}
