package org.geoatlas.metadata;

import org.geotools.jdbc.JDBCDataStore;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 9:49
 * @since: 1.0
 **/
public class FeatureLayerInfo {

    private NamespaceInfo namespace;

    private String identifier;

    private String name;

    private JDBCDataStore dataStore;

    private SpatialReferenceInfo spatialReferenceInfo;

    private DataStoreInfo storeInfo;

    public FeatureLayerInfo() {
    }

    public NamespaceInfo getNamespace() {
        return namespace;
    }

    public void setNamespace(NamespaceInfo namespace) {
        this.namespace = namespace;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JDBCDataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(JDBCDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public SpatialReferenceInfo getSpatialReferenceInfo() {
        return spatialReferenceInfo;
    }

    public void setSpatialReferenceInfo(SpatialReferenceInfo spatialReferenceInfo) {
        this.spatialReferenceInfo = spatialReferenceInfo;
    }

    public DataStoreInfo getStoreInfo() {
        return storeInfo;
    }

    public void setStoreInfo(DataStoreInfo storeInfo) {
        this.storeInfo = storeInfo;
    }
}
