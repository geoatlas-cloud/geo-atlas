package org.geoatlas.tile.model;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/5/21 18:31
 * @since: 1.0
 **/
public class DashboardTotalCount {

    private long namespaceCount;

    private long dataStoreCount;

    private long layerCount;

    private long spatialReferenceCount;

    public DashboardTotalCount(){}

    public DashboardTotalCount(long namespaceCount, long dataStoreCount, long layerCount, long spatialReferenceCount) {
        this.namespaceCount = namespaceCount;
        this.dataStoreCount = dataStoreCount;
        this.layerCount = layerCount;
        this.spatialReferenceCount = spatialReferenceCount;
    }

    public long getNamespaceCount() {
        return namespaceCount;
    }

    public void setNamespaceCount(long namespaceCount) {
        this.namespaceCount = namespaceCount;
    }

    public long getDataStoreCount() {
        return dataStoreCount;
    }

    public void setDataStoreCount(long dataStoreCount) {
        this.dataStoreCount = dataStoreCount;
    }

    public long getLayerCount() {
        return layerCount;
    }

    public void setLayerCount(long layerCount) {
        this.layerCount = layerCount;
    }

    public long getSpatialReferenceCount() {
        return spatialReferenceCount;
    }

    public void setSpatialReferenceCount(long spatialReferenceCount) {
        this.spatialReferenceCount = spatialReferenceCount;
    }
}
