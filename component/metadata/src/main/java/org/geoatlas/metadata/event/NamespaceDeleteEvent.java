package org.geoatlas.metadata.event;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/9 19:54
 * @since: 1.0
 **/
public class NamespaceDeleteEvent extends MetadataDeleteEvent{
    private static final long serialVersionUID = 6963359704854079687L;

    public NamespaceDeleteEvent(Object source) {
        super(source);
    }
}
