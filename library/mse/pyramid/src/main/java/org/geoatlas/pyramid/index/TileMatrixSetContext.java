package org.geoatlas.pyramid.index;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/22 15:29
 * @since: 1.0
 **/
public class TileMatrixSetContext {

    private static final Map<String, TileMatrixSet> TILE_MATRIX_SET_CACHE = new ConcurrentHashMap<>();

    /**
     *
     * @param identifier tile matrix set title
     * @return
     */
    public static TileMatrixSet getTileMatrixSet(String identifier) {
        return TILE_MATRIX_SET_CACHE.get(identifier);
    }

    public static void addTileMatrixSet(TileMatrixSet tileMatrixSet) {
        TILE_MATRIX_SET_CACHE.put(tileMatrixSet.getTitle(), tileMatrixSet);
    }

    static {
        String projectedName = "EPSG:4326";
        try {
            TileMatrixSet WORLD_EPSG_4326 =
                    TileMatrixSetFactory.createTileMatrixSet(
                            projectedName,
                            CRS.decode("EPSG:4326", true),
                            BoundingBox.WORLD4326,
                            CornerOfOrigin.TOP_LEFT,
                            TileMatrixSetFactory.DEFAULT_LEVELS,
                            null,
                            TileMatrixSetFactory.DEFAULT_PIXEL_SIZE_METER,
                            256,
                            256,
                            false);
            WORLD_EPSG_4326.setDescription(
                    "A default WGS84 tile matrix set where the first zoom level "
                            + "covers the world with two tiles on the horizontal axis and one tile "
                            + "over the vertical axis and each subsequent zoom level is calculated by half "
                            + "the resolution of its previous one. Tiles are 256px wide.");
            addTileMatrixSet(WORLD_EPSG_4326);

            projectedName = "EPSG:4490";
            TileMatrixSet WORLD_EPSG_4490 =
                    TileMatrixSetFactory.createTileMatrixSet(
                            projectedName,
                            CRS.decode("EPSG:4490", true),
                            BoundingBox.WORLD4490,
                            CornerOfOrigin.TOP_LEFT,
                            1.40625d, // 天地图经纬度投影0级的分辨率
                            TileMatrixSetFactory.DEFAULT_LEVELS,
                            TileMatrixSetFactory.EPSG_4326_TO_METERS,
                            TileMatrixSetFactory.CGCS2000_PIXEL_SIZE_METER,
                            256,
                            256,
                            false);
            WORLD_EPSG_4490.setDescription(
                    "A default CGCS4490 tile matrix set where the first zoom level "
                            + "covers the world with two tiles on the horizontal axis and one tile "
                            + "over the vertical axis and each subsequent zoom level is calculated by half "
                            + "the resolution of its previous one. Tiles are 256px wide.");
            addTileMatrixSet(WORLD_EPSG_4490);


            projectedName = "EPSG:3857";
            TileMatrixSet WORLD_EPSG_3857 =
                    TileMatrixSetFactory.createTileMatrixSet(
                            projectedName,
                            CRS.decode("EPSG:3857", true),
                            BoundingBox.WORLD3857,
                            CornerOfOrigin.TOP_LEFT,
                            TileMatrixSetFactory.DEFAULT_LEVELS,
                            null,
                            TileMatrixSetFactory.DEFAULT_PIXEL_SIZE_METER,
                            256,
                            256,
                            false);
            WORLD_EPSG_3857.setDescription(
                    "This well-known scale set has been defined to be compatible with Google Maps and"
                            + " Microsoft Live Map projections and zoom levels. Level 0 allows representing the whole "
                            + "world in a single 256x256 pixels. The next level represents the whole world in 2x2 tiles "
                            + "of 256x256 pixels and so on in powers of 2. Scale denominator is only accurate near the equator.");
            addTileMatrixSet(WORLD_EPSG_3857);

            projectedName = "EPSG:4550";
            TileMatrixSet WORLD_EPSG_4550 =
                    TileMatrixSetFactory.createTileMatrixSet(
                            projectedName,
                            CRS.decode("EPSG:4550", true),
                            BoundingBox.CHINA_4550,
                            CornerOfOrigin.TOP_LEFT,
                            TileMatrixSetFactory.DEFAULT_LEVELS,
                            null,
                            TileMatrixSetFactory.DEFAULT_PIXEL_SIZE_METER,
                            256,
                            256,
                            false);
            addTileMatrixSet(WORLD_EPSG_4550);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }
}
