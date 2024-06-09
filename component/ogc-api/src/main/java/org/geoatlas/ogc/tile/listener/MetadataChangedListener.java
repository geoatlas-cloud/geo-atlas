package org.geoatlas.ogc.tile.listener;

import org.geoatlas.cache.core.storage.StorageBroker;
import org.geoatlas.cache.core.storage.StorageException;
import org.geoatlas.metadata.event.FeatureLayerDeleteEvent;
import org.geoatlas.metadata.event.FeatureLayerUpdateEvent;
import org.geoatlas.metadata.model.FeatureLayerInfo;
import org.geoatlas.metadata.model.NamespaceInfo;
import org.geoatlas.tile.util.TileObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/9 20:08
 * @since: 1.0
 **/

@Component
public class MetadataChangedListener {

    private final StorageBroker storageBroker;
    private final static Logger log = LoggerFactory.getLogger(MetadataChangedListener.class);

    public MetadataChangedListener(@Autowired(required = false) StorageBroker storageBroker) {
        this.storageBroker = storageBroker;
    }

    @EventListener(value = FeatureLayerUpdateEvent.class)
    public void onFeatureLayerInfoUpdate(FeatureLayerUpdateEvent event) {
        if (log.isDebugEnabled()){
            log.debug("FeatureLayerInfo update event: {}", event.getSource());
        }
        // 直接清除FeatureLayer缓存
        removeLayerCache((FeatureLayerInfo) event.getSource(), event.getNamespace());
    }

    @EventListener(value = FeatureLayerDeleteEvent.class)
    public void onFeatureLayerInfoDelete(FeatureLayerDeleteEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("FeatureLayerInfo delete event: {}", event.getSource());
        }
        removeLayerCache((FeatureLayerInfo) event.getSource(), event.getNamespace());
    }

    // FIXME: 2024/6/9 处理DatastoreActionEvent
    
    private void removeLayerCache(FeatureLayerInfo last, NamespaceInfo namespace) {
        if (Objects.nonNull(last) && Objects.nonNull(namespace)){
            if (checkStorageBroker()) {
                try {
                    storageBroker.delete(TileObjectUtils.GeneratorCombinedName(namespace.getName(), last.getName()));
                } catch (StorageException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private boolean checkStorageBroker() {
        return storageBroker != null;
    }
}
