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
 * @author Arne Kepp, The Open Planning Project, Copyright 2009
 */
package org.geoatlas.cache.core.conveyor;

import org.geoatlas.cache.core.mime.MimeType;
import org.geoatlas.cache.core.response.TileResponseReceiver;
import org.geoatlas.cache.core.storage.StorageBroker;
import org.geoatlas.cache.core.storage.StorageException;
import org.geoatlas.io.Resource;
import org.geoatlas.pyramid.index.GeoAtlasPyramidException;
import org.geoatlas.pyramid.index.TileMatrixSet;
import org.geoatlas.pyramid.index.TileMatrixSubset;
import org.geoatlas.tile.TileObject;
import org.geoatlas.tile.TileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/** Represents a request for a tile and carries the information needed to complete it. */
public class ConveyorTile extends Conveyor implements TileResponseReceiver {
    private static Logger log = LoggerFactory.getLogger(ConveyorTile.class);

    // Shared request information, this is stored by the cache key
    // protected long[] tileIndex = null;

    // protected SRS srs = null;
    protected String gridSetId = null;

    protected TileMatrixSubset gridSubset = null;

//    protected TileLayer tileLayer = null;

    TileObject stObj = null;

    private TileRequest request;

    private Map<String, String> rawParameters;

    private boolean isMetaTileCacheOnly;

    public ConveyorTile(
            StorageBroker sb,
            String layerId,
            HttpServletRequest servletReq,
            HttpServletResponse servletResp) {
        super(layerId, sb, servletReq, servletResp);
    }

    /** This constructor is used for an incoming request, the data is then added by the cache */
    public ConveyorTile(
            StorageBroker sb,
            String layerId,
            String gridSetId,
            MimeType mimeType,
            TileRequest request,
            Map<String, String> rawParameters,
            HttpServletRequest servletReq,
            HttpServletResponse servletResp) {

        super(layerId, sb, servletReq, servletResp);
        this.gridSetId = gridSetId;
        long[] idx = new long[3];
        if (request != null && request.isValid()) {
            idx[0] = request.getX();
            idx[1] = request.getY();
            idx[2] = request.getZ();
        }

        super.mimeType = mimeType;
        this.request = request;
        this.rawParameters = rawParameters;

        stObj =
                TileObject.createQueryTileObject(
                        layerId, idx, gridSetId, mimeType.getFormat(), rawParameters);
    }

    public TileRequest getRequest() {
        return request;
    }

    public void setRequest(TileRequest request) {
        this.request = request;
    }

    public Map<String, String> getRawParameters() {
        return rawParameters;
    }

    public void setRawParameters(Map<String, String> rawParameters) {
        this.rawParameters = rawParameters;
    }

    //    public TileLayer getLayer() {
//        return this.tileLayer;
//    }

//    public void setTileLayer(TileLayer layer) {
//        this.tileLayer = layer;
//    }

//    public TileLayer getTileLayer() {
//        return tileLayer;
//    }

    /** The time that the stored tile resource was created */
    public long getTSCreated() {
        return stObj.getCreated();
    }

    @Override
    public int getStatus() {
        return (int) status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMsg;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMsg = errorMessage;
    }

    public Map<String, String> getParameters() {
        return stObj.getParameters();
    }

    public long[] getTileIndex() {
        return stObj.getXYZ();
    }

    public synchronized TileMatrixSubset getGridSubset() {
        return gridSubset;
    }

    public String getGridSetId() {
        return gridSetId;
    }

    public void setGridSetId(String gridSetId) {
        this.gridSetId = gridSetId;
    }

    public Resource getBlob() {
        return stObj.getBlob();
    }

    public void setBlob(Resource payload) {
        stObj.setBlob(payload);
    }

    public TileObject getStorageObject() {
        return stObj;
    }

    public boolean persist() throws GeoAtlasPyramidException {
        try {
            return storageBroker.put(stObj);
        } catch (StorageException e) {
            throw new GeoAtlasPyramidException(e);
        }
    }

    public boolean retrieve(long maxAge) throws GeoAtlasPyramidException {
        try {
            if (isMetaTileCacheOnly) {
                boolean cached = storageBroker.getTransient(stObj);
                this.setCacheResult(cached ? CacheResult.HIT : CacheResult.MISS);
                return cached;
            }
            boolean ret = storageBroker.get(stObj);

            // Do we use expiration, and if so, is the tile recent enough ?
            if (ret && maxAge > 0 && stObj.getCreated() + maxAge < System.currentTimeMillis()) {
                ret = false;
            }

            if (ret) {
                this.setCacheResult(CacheResult.HIT);
            } else {
                this.setCacheResult(CacheResult.MISS);
            }

            return ret;

        } catch (StorageException se) {
            log.warn(se.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("ConveyorTile[");
        long[] idx = stObj.getXYZ();

        if (getLayerId() != null) {
            str.append(getLayerId()).append(" ");
        }

        if (this.gridSetId != null) {
            str.append(gridSetId).append(" ");
        }

        if (idx != null && idx.length == 3) {
            str.append("{" + idx[0] + "," + idx[1] + "," + idx[2] + "} ");
        }

        if (this.mimeType != null) {
            str.append(this.mimeType.getFormat());
        }
        str.append(']');
        return str.toString();
    }

    public String getParametersId() {
        return stObj.getParametersId();
    }

    public void setMetaTileCacheOnly(boolean b) {
        this.isMetaTileCacheOnly = b;
    }

    public boolean isMetaTileCacheOnly() {
        return isMetaTileCacheOnly;
    }
}
