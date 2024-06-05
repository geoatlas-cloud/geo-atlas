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
 * @author Arne Kepp, OpenGeo, Copyright 2010
 */
package org.geoatlas.cache.core.storage;

import org.geoatlas.cache.core.mime.MimeType;
import org.geoatlas.pyramid.index.TileMatrixSubset;

import java.util.Map;

/** This class is a TileRange object with an additional filter */
public class DiscontinuousTileRange extends TileRange {

    private final TileRangeMask mask;

    public DiscontinuousTileRange(
            String layerName,
            String namespace,
            String gridSetId,
            int zoomStart,
            int zoomStop,
            TileRangeMask rasterMask,
            MimeType mimeType,
            TileMatrixSubset subset,
            Map<String, String> parameters) {

        super(
                layerName,
                namespace,
                gridSetId,
                zoomStart,
                zoomStop,
                rasterMask.getGridCoverages(),
                mimeType,
                subset,
                parameters);

        this.mask = rasterMask;
    }

    @Override
    public boolean contains(long x, long y, int z) {
        if (super.contains(x, y, z)) {
            return mask.lookup(x, y, z);
        }
        return false;
    }

    @Override
    public boolean contains(long[] idx) {
        return contains(idx[0], idx[1], (int) idx[2]);
    }
}
