package org.geoatlas.pyramid.index;

import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.Arrays;


public class TileMatrixSetFactoryTest {

    public static CoordinateReferenceSystem EPSG4326_CRS;

    static {
        try {
            EPSG4326_CRS = CRS.decode("EPSG:4326", true);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createTileMatrixSet() {
        TileMatrixSet tileMatrixSet = TileMatrixSetFactory.createTileMatrixSet(
                "EPSG:4326",
                EPSG4326_CRS,
                BoundingBox.WORLD4326,
                CornerOfOrigin.TOP_LEFT,
                22,
                null,
                0.00028,
                256,
                256,
                false);
        assert tileMatrixSet.getNumLevels() == 22;
        assert tileMatrixSet.getCrs().equals(EPSG4326_CRS);
        assert tileMatrixSet.getExtent().equals(BoundingBox.WORLD4326);
    }

    @Test
    public void createTileMatrixSet1() {
        TileMatrixSet tileMatrixSet = TileMatrixSetFactory.createTileMatrixSet(
                "EPSG:4326",
                EPSG4326_CRS,
                new BoundingBox(-10.0,-30.0,85.0,21.0),
                CornerOfOrigin.TOP_LEFT,
                22,
                null,
                0.00028,
                256,
                256,
                false);
        System.out.println(tileMatrixSet);
    }

    @Test
    public void createTileMatrixSet4490() throws FactoryException {
        TileMatrixSet tileMatrixSet = TileMatrixSetFactory.createTileMatrixSet(
                "EPSG:4490",
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
        System.out.println(tileMatrixSet.getTitle());
    }

    @Test
    public void createTileMatrixSet44901() throws FactoryException {
        TileMatrixSet tileMatrixSet = TileMatrixSetFactory.createTileMatrixSet(
                "EPSG:4490",
                CRS.decode("EPSG:4490", true),
                new BoundingBox(-180,-85,180,85),
                CornerOfOrigin.TOP_LEFT,
                TileMatrixSetFactory.DEFAULT_LEVELS,
                TileMatrixSetFactory.EPSG_4326_TO_METERS,
                TileMatrixSetFactory.CGCS2000_PIXEL_SIZE_METER,
                256,
                256,
                false);
        System.out.println(tileMatrixSet.getTitle());
    }


    @Test
    public void testCreateTileMatrixSet() throws FactoryException {
        CoordinateReferenceSystem referenceSystem = CRS.decode("EPSG:4549");
        CoordinateReferenceSystem referenceSystem1 = CRS.decode("EPSG:4549");
        assert referenceSystem.equals(referenceSystem1);
    }

    @Test
    public void getMetersPerUnit() {
    }

    @Test
    public void closestIndex() throws FactoryException, MatrixMismatchException {
        TileMatrixSet tileMatrixSet = TileMatrixSetFactory.createTileMatrixSet(
                "EPSG:4490",
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
        long[] index = tileMatrixSet.closestIndex(new BoundingBox(-180.0, -90.0, 180, 90));
        System.out.println(Arrays.toString(index));
    }

    @Test
    public void testClosestRectangle() throws Exception {
        TileMatrixSet tileMatrixSet = TileMatrixSetFactory.createTileMatrixSet(
                "EPSG:4490",
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
        BoundingBox box = new BoundingBox(-180.0, -90.0, 0.0, 0.0);
        long[] rectTL = tileMatrixSet.closestRectangle(box);

        System.out.println(Arrays.toString(rectTL));
    }

}