package org.geoatlas.metadata.event;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/9 19:55
 * @since: 1.0
 **/
public class DatastoreDeleteEvent extends MetadataDeleteEvent{
    private static final long serialVersionUID = -2114528837776862816L;

    public DatastoreDeleteEvent(Object source) {
        super(source);
    }
}
