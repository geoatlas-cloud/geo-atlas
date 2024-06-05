package org.geoatlas.tile;

import org.geoatlas.io.Resource;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import static org.geoatlas.tile.util.TileObjectUtils.GeneratorCombinedName;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:22
 * @since: 1.0
 **/
public class TileObject implements Serializable {

    public static enum Status {
        UNSET,
        HIT,
        MISS,
        LOCK,
        EXPIRED_LOCK
    };

    Status status = Status.UNSET;

    Resource blob;
    String parameters_id = null;
    long[] xyz;
    String layer_name;

    String namespace;

    Map<String, String> parameters;
    String schema;

    long created;

    private String format;

    int blob_size;

    private TileObject() {
    }

    public static TileObject createQueryTileObject(String layerName, String namespace, long[] xyz, String schema, String format, Map<String, String> parameters) {
        TileObject obj = new TileObject();
        obj.layer_name = layerName;
        obj.namespace = namespace;
        obj.xyz = xyz;
        obj.schema = schema;
        obj.format = format;
        obj.parameters = parameters;
        return obj;
    }

    public static TileObject createCompleteTileObject(TileRequest request, Resource blob) {
        return createCompleteTileObject(request.getLayer(), request.getNamespace(), new long[]{request.getX(), request.getY(), request.getZ()}, request.getSchema(), request.getFormat(), null, blob);
    }

    public static TileObject createCompleteTileObject(TileRequest request, Resource blob, String mimeType) {
        return createCompleteTileObject(request.getLayer(), request.getNamespace(), new long[]{request.getX(), request.getY(), request.getZ()}, request.getSchema(), mimeType, null, blob);
    }

    public static TileObject createCompleteTileObject(String layerName, String namespace, long[] xyz, String schema, String format, Map<String, String> parameters, Resource blob) {
        TileObject obj = new TileObject();
        obj.layer_name = layerName;
        obj.namespace = namespace;
        obj.xyz = xyz;
        obj.schema = schema;
        obj.format = format;
        obj.parameters = parameters;

        if (blob == null) {
            obj.blob_size = -1;
        } else {
            obj.blob_size = (int) blob.getSize();
            obj.blob = blob;
        }
        obj.created = System.currentTimeMillis();
        return obj;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    public String getNamespace() {
        return namespace;
    }

    /**
     * 使用该属性替换LayerName, 用以包含namespace
     * @return
     */
    public String getCombinedLayerName(){
        return GeneratorCombinedName(namespace, layer_name);
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

    /** The time that the stored resource was created/modified */
    public void setCreated(long created) {
        this.created = created;
    }

    public int getBlobSize() {
        return blob_size;
    }

    public void setBlobSize(int blob_size) {
        this.blob_size = blob_size;
    }

    public String toString() {
        return "[" + this.layer_name + "," + this.schema + ",{" + Arrays.toString(this.xyz) + "}]";
    }
}
