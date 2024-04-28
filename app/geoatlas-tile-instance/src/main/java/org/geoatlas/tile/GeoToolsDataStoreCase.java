package org.geoatlas.tile;

import org.geoatlas.metadata.DataStoreFactory;
import org.geoatlas.metadata.model.DataStoreInfo;
import org.geotools.data.DataStore;

import java.io.IOException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/28 22:03
 * @since: 1.0
 **/
public class GeoToolsDataStoreCase {

    public static void main(String[] args) {
        DataStoreInfo dataStoreInfo = new DataStoreInfo();
        dataStoreInfo.setType("postgis");
        dataStoreInfo.setHost("localhost");
        dataStoreInfo.setPort("5432");
        dataStoreInfo.setSchema("public");
        dataStoreInfo.setDatabase("wukong");
        dataStoreInfo.setUser("postgres");
        dataStoreInfo.setPassword("gis@4490");
        try {
            DataStore dataStore = DataStoreFactory.createDataStore(dataStoreInfo);
            System.out.println(dataStore);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
