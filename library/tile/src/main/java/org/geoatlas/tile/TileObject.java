package org.geoatlas.tile;

import org.geoatlas.io.Resource;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:22
 * @since: 1.0
 **/
public class TileObject implements Serializable {

    Resource blob;
    String parameters_id = null;
    long[] xyz;
    String layer_name;
    Map<String, String> parameters;
    String schema;

    long created;

    private String format;

    private TileObject() {
    }

    public static TileObject createQueryTileObject(String layerName, long[] xyz, String schema, String format, Map<String, String> parameters) {
        TileObject obj = new TileObject();
        obj.layer_name = layerName;
        obj.xyz = xyz;
        obj.schema = schema;
        obj.format = format;
        obj.parameters = parameters;
        return obj;
    }

    public static TileObject createCompleteTileObject(TileRequest request, Resource blob) {
        return createCompleteTileObject(request.getLayer(), new long[]{request.getX(), request.getY(), request.getZ()}, request.getSchema(), request.getFormat(), null, blob);
    }

    public static TileObject createCompleteTileObject(String layerName, long[] xyz, String schema, String format, Map<String, String> parameters, Resource blob) {
        TileObject obj = new TileObject();
        obj.layer_name = layerName;
        obj.xyz = xyz;
        obj.schema = schema;
        obj.format = format;
        obj.parameters = parameters;
        if (blob != null) {
            obj.blob = blob;
        }

        obj.created = System.currentTimeMillis();
        return obj;
    }

    public Resource getBlob() {
        return this.blob;
    }

    public void setBlob(Resource blob) {
        this.blob = blob;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getParametersId() {
        return this.parameters_id;
    }

    public void setParametersId(String parameters_id) {
        this.parameters_id = parameters_id;
    }

    public long[] getXYZ() {
        return this.xyz;
    }

    public String getLayerName() {
        return this.layer_name;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public String getType() {
        return "tile";
    }

    public String getFormat() {
        return format;
    }

    public long getCreated() {
        return created;
    }

    public String toString() {
        return "[" + this.layer_name + "," + this.schema + ",{" + Arrays.toString(this.xyz) + "}]";
    }
}
