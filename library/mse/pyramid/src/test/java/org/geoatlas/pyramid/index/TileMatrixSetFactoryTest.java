package org.geoatlas.pyramid.index;

import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
    public void testCreateTileMatrixSet() throws FactoryException {
        CoordinateReferenceSystem referenceSystem = CRS.decode("EPSG:4549");
        CoordinateReferenceSystem referenceSystem1 = CRS.decode("EPSG:4549");
        assert referenceSystem.equals(referenceSystem1);
    }

    @Test
    public void getMetersPerUnit() {
    }
}