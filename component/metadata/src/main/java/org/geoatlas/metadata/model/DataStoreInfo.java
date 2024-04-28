package org.geoatlas.metadata.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *  @see <a href="https://docs.geotools.org/latest/userguide/library/jdbc/datastore.html">JDBCDataStore</a>
 *
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 10:17
 * @since: 1.0
 **/
@Table("ga_datastore_info")
public class DataStoreInfo implements Serializable {
    private static final long serialVersionUID = 6803793260512509711L;

    @Id
    private Long id;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private NamespaceInfo namespace;

    private String name;

    private String description;

    // database type, Such as postgis, sqlserver, oracle
    private String type;

    private String host;

    private String port;

    private String schema;

    private String database;

    private String user;

    private String password;

    // For Connection Pooling

    @Column("max_connections")
    private int maxConnections;

    @Column("min_connections")
    private int minConnections;

    // connection timeout
    @Column("connection_timeout")
    private int connectionTimeout;

    // Connection validation is on by default, it takes a small toll to make sure the connection is still valid before using it
    // (e.g., make sure the DBMS did not drop it due to a server side timeout). If you want to get extra performance
    // and you’re sure the connections will never be dropped you can disable connection validation with false
    // 连接验证在默认情况下是开启的，在使用连接之前确保连接仍然有效(例如，确保 DBMS 没有因为服务器端超时而删除连接)需要付出一点代价。
    // 如果希望获得额外的性能，并确保连接永远不会丢失，则可以将其标记为false     这里不得不Q一下OpenGauss主动关闭连接的设定
    @Column("validate_connections")
    private boolean validateConnections;

    // For Tweaking and Performance

    @Column("fetch_size")
    private int fetchSize;

    private Timestamp created;

    private Timestamp modified;

    public DataStoreInfo() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMinConnections() {
        return minConnections;
    }

    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public boolean isValidateConnections() {
        return validateConnections;
    }

    public void setValidateConnections(boolean validateConnections) {
        this.validateConnections = validateConnections;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getModified() {
        return modified;
    }

    public void setModified(Timestamp modified) {
        this.modified = modified;
    }
}
