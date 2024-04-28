package org.geoatlas.tile;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:50
 * @since: 1.0
 **/
public class TileRequest {

    private String namespace;

    private String layer;

    // tiling schema, 对应 TileMatrixSet identifier
    private String schema;

    private long x;

    private long y;

    private long z;

    private String format;

    public TileRequest() {
    }

    public TileRequest(String namespace, String layer, String schema, long x, long y, long z) {
        this(namespace, layer, schema, x, y, z, null);
    }

    public TileRequest(String namespace, String layer, String schema, long x, long y, long z, String format) {
        this.namespace = namespace;
        this.layer = layer;
        this.schema = schema;
        this.x = x;
        this.y = y;
        this.z = z;
        this.format = format;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public long getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

    public long getZ() {
        return z;
    }

    public void setZ(long z) {
        this.z = z;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
