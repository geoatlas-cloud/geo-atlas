/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Arne Kepp, OpenGeo, Copyright 2009
 */
package org.geoatlas.pyramid.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

public class TileMatrixSubsetFactory {

    private static Logger log = LoggerFactory.getLogger(TileMatrixSubsetFactory.class);

    /**
     * 取TileMatrixSet中CRS的Extent范围作为coverage的范围
     *
     * @param gridSet
     * @return
     */
    public static TileMatrixSubset createTileMatrixSubset(TileMatrixSet gridSet) {

        TileMatrixSubset ret =
                createTileMatrixSubset(
                        gridSet, gridSet.getExtent(), 0, gridSet.getNumLevels() - 1);
        return ret;
    }

    public static TileMatrixSubset createTileMatrixSubset(TileMatrixSet gridSet, BoundingBox extent) {
        return createTileMatrixSubset(gridSet, extent, 0, gridSet.getNumLevels() - 1, null, null);
    }

    public static TileMatrixSubset createTileMatrixSubset(
            TileMatrixSet gridSet, BoundingBox extent, Integer zoomStart, Integer zoomStop) {
        return createTileMatrixSubset(gridSet, extent, zoomStart, zoomStop, null, null);
    }

    public static TileMatrixSubset createTileMatrixSubset(
            TileMatrixSet gridSet,
            BoundingBox extent,
            Integer zoomStart,
            Integer zoomStop,
            Integer minCachedZoom,
            Integer maxCachedZoom) {

        if (gridSet == null) {
            throw new NullPointerException("Passed TileMatrixSet was null!");
        }

        final int maxLevel = gridSet.getNumLevels() - 1;
        if (zoomStart == null) {
            zoomStart = 0;
        }
        if (zoomStop == null) {
            zoomStop = maxLevel;
        } else if (zoomStop > maxLevel) {
            String message =
                    "Requested to create TileMatrixSubset with zoomStop "
                            + zoomStop
                            + " for TileMatrixSet "
                            + gridSet.getTitle()
                            + " whose max zoom level is "
                            + maxLevel
                            + ". Limiting TileMatrixSubset to zoomStop = "
                            + maxLevel;
            log.warn(message);
            zoomStop = maxLevel;
        }

        Map<Integer, MatrixCoverage> coverages = new TreeMap<>();
        for (int z = zoomStart; z <= zoomStop; z++) {

            TileMatrix level = gridSet.getMatrix(z);

            long[] coverage;
            if (extent == null) {
//                long maxColX = level.getNumTilesWide() - 1;
                long maxColX = level.getMatrixWidth() - 1;
//                long maxColY = level.getNumTilesHigh() - 1;
                long maxColY = level.getMatrixHeight() - 1;
                coverage = new long[] {0, 0, maxColX, maxColY, z};
            } else {
                coverage = gridSet.closestRectangle(z, extent);
            }

            MatrixCoverage gridCov = new MatrixCoverage(coverage);
            coverages.put(Integer.valueOf(z), gridCov);
        }

        // Save the original extent provided by the user
        BoundingBox originalExtent = extent;
        boolean fullCoverage = false;

        // Is this plain wrong? GlobalCRS84Scale, I guess the resolution forces it
        BoundingBox gridSetBounds = gridSet.getBounds();
        if (extent == null || extent.contains(gridSetBounds)) {
            fullCoverage = true;
            originalExtent = gridSetBounds;
        }

        TileMatrixSubset ret =
                new TileMatrixSubset(
                        gridSet,
                        coverages,
                        originalExtent,
                        fullCoverage,
                        minCachedZoom,
                        maxCachedZoom);
        return ret;
    }
}
