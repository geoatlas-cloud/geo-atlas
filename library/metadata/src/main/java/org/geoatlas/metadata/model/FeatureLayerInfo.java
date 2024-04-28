package org.geoatlas.metadata.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 9:49
 * @since: 1.0
 **/
public class FeatureLayerInfo {

    @Id
    private Long id;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private NamespaceInfo namespace;

    private String name;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private VirtualViewInfo view;

    // 如果指定, 将会覆盖数据库读取到的空间参考, 可以为空
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private SpatialReferenceInfo spatialReferenceInfo;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private DataStoreInfo storeInfo;

    public FeatureLayerInfo() {
    }

    public NamespaceInfo getNamespace() {
        return namespace;
    }

    public void setNamespace(NamespaceInfo namespace) {
        this.namespace = namespace;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VirtualViewInfo getView() {
        return view;
    }

    public void setView(VirtualViewInfo view) {
        this.view = view;
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
