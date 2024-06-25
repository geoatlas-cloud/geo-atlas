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
//        dataStoreInfo.setType("postgis");
        dataStoreInfo.setType("mysql");
        dataStoreInfo.setHost("192.168.1.5");
        dataStoreInfo.setPort("3301");
        dataStoreInfo.setSchema("");
        dataStoreInfo.setDatabase("mysql");
        dataStoreInfo.setUser("root");
        dataStoreInfo.setPassword("gis@4490");
        try {
            DataStore dataStore = DataStoreFactory.createDataStore(dataStoreInfo);
            System.out.println(dataStore);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
