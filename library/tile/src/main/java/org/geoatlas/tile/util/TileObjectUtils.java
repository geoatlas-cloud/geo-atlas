package org.geoatlas.tile.util;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/6/5 17:31
 * @since: 1.0
 **/
public class TileObjectUtils {
    private final static String LAYER_NAME_SEPARATOR = ":";

    public static String GeneratorCombinedName(String prefix, String layerName){
        return prefix + LAYER_NAME_SEPARATOR + layerName;
    }
}
