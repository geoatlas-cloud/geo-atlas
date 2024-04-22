package org.geoatlas.metadata;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 14:47
 * @since: 1.0
 **/
public class DataStoreFactory {

    public static DataStore createDataStore(DataStoreInfo dataStoreInfo) throws IOException {
        if (Objects.isNull(dataStoreInfo)) {
            throw new NullPointerException("DataStoreInfo is null");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", dataStoreInfo.getType());
        params.put("host", dataStoreInfo.getHost());
        params.put("port", dataStoreInfo.getHost());
        params.put("schema", dataStoreInfo.getSchema());
        params.put("database", dataStoreInfo.getDatabase());
        params.put("user", dataStoreInfo.getUser());
        params.put("passwd", dataStoreInfo.getPassword());
        params.put("preparedStatements", true);
        params.put("encode functions", true);
        DataStore dataStore = DataStoreFinder.getDataStore(params);
        return dataStore;
    }
}
