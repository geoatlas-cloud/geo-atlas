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
 * @author Marius Suta / The Open Planning Project 2008
 * @author Arne Kepp / The Open Planning Project 2009
 */
package org.geoatlas.cache.core.seed;

import org.geoatlas.cache.core.util.SRS;
import org.geoatlas.pyramid.index.BoundingBox;
import org.geotools.util.logging.Logging;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.logging.Logger;

/** Stores the information for a Seed Request. */
public class SeedRequest {
    private static Logger log = Logging.getLogger(SeedRequest.class.getName());

    @NotBlank(message = "Namespace can not be blank.")
    private String namespace;

    @NotBlank(message = "Layer name can not be blank.")
    private String name = null;

    private BoundingBox bounds = null;

    @NotBlank(message = "MatrixSet id can not be blank.")
    private String matrixSetId = null;

    @NotNull(message = "Thread count can not be null.")
    private Integer threadCount = null;

    @NotNull(message = "Zoom start can not be null.")
    private Integer zoomStart = null;

    @NotNull(message = "Zoom stop can not be null.")
    private Integer zoomStop = null;

    @NotBlank(message = "Mime format can not be blank.")
    private String format = null;

    @NotBlank(message = "Tile task type can not be blank.")
    private String type = null; //  TODO: This appears to do nothing as it is never changed from being null

    private TileTask.TYPE enumType = null;

    private Map<String, String> parameters = null;

//    private Boolean filterUpdate = null;

    private int tileFailureRetryCount = TileBreeder.TILE_FAILURE_RETRY_COUNT_DEFAULT;

    private long tileFailureRetryWaitTime = TileBreeder.TILE_FAILURE_RETRY_WAIT_TIME_DEFAULT;

    private long totalFailuresBeforeAborting = TileBreeder.TOTAL_FAILURES_BEFORE_ABORTING_DEFAULT;

    public SeedRequest() {
        // do nothing, i guess
    }

    /**
     * Create a new SeedRequest
     *
     * <p>Used by SeedPageResource
     *
     * @param layerName name of the tile layer
     * @param bounds bounds for the requested region
     * @param matrixSetId the grid set id for this request
     * @param threadCount the number of threads that should be used for this seed request
     * @param zoomStart the zoom start level for this seed request
     * @param zoomStop the zoom stop level for this seed request
     * @param mimeFormat the MIME format requested
     */
    public SeedRequest(
            String namespace,
            String layerName,
            BoundingBox bounds,
            String matrixSetId,
            int threadCount,
            int zoomStart,
            int zoomStop,
            String mimeFormat,
            TileTask.TYPE type,
            Map<String, String> parameters) {
        this.namespace = namespace;
        this.name = layerName;
        this.bounds = bounds;
        this.matrixSetId = matrixSetId;
        this.threadCount = threadCount;
        this.zoomStart = zoomStart;
        this.zoomStop = zoomStop;
        this.format = mimeFormat;
        this.enumType = type;
        this.parameters = parameters;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Method returns the name of the tileLayer that was requested
     *
     * @return name of the requested tile layer
     */
    public String getLayerName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method gets the bounds for the requested region
     *
     * @return a BBOX
     */
    public BoundingBox getBounds() {
        return this.bounds;
    }

    public void setBounds(BoundingBox bounds) {
        this.bounds = bounds;
    }

    /**
     * Method returns the grid set id for this request
     *
     * @return String
     */
    public String getMatrixSetId() {
        return this.matrixSetId;
    }

    public void setMatrixSetId(String matrixSetId) {
        this.matrixSetId = matrixSetId;
    }

    /**
     * Method returns the MIME format requested
     *
     * @return the format in String form
     */
    public String getMimeFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Method returns the zoom start level for this seed request
     *
     * @return integer representing zoom start level
     */
    public Integer getZoomStart() {
        return this.zoomStart;
    }

    public void setZoomStart(Integer zoomStart) {
        this.zoomStart = zoomStart;
    }

    /**
     * Method returns the zoom stop level for this seed request
     *
     * @return integer representing zoom stop level
     */
    public Integer getZoomStop() {
        return this.zoomStop;
    }

    public void setZoomStop(Integer zoomStop) {
        this.zoomStop = zoomStop;
    }

    /**
     * Method returns the number of threads that should be used for this seed request
     *
     * @return integer representing number of threads
     */
    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    /**
     * Method returns the type of seed, namely one of
     *
     * <ul>
     *   <li>seed - (default) seeds new tiles
     *   <li>reseed - seeds new tiles and replaces old ones
     *   <li>truncate - removes tiles
     * </ul>
     *
     * @return type of seed
     */
    public TileTask.TYPE getType() {
        if (enumType == null) {
            if (type == null || type.equalsIgnoreCase("seed")) {
                return TileTask.TYPE.SEED;
            } else if (type.equalsIgnoreCase("reseed")) {
                return TileTask.TYPE.RESEED;
            } else if (type.equalsIgnoreCase("truncate")) {
                return TileTask.TYPE.TRUNCATE;
            } else {
                log.warning("Unknown type \"" + type + "\", assuming seed");
                return TileTask.TYPE.SEED;
            }
        }

        return enumType;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The settings for the modifiable parameters
     *
     * @return the modifiable parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Number of retries to build a tile before giving up on it. -1 disables also the wait and total
     * failures counters.
     */
    public int getTileFailureRetryCount() {
        return tileFailureRetryCount;
    }

    public void setTileFailureRetryCount(int tileFailureRetryCount) {
        this.tileFailureRetryCount = tileFailureRetryCount;
    }

    /** Time to wait between tile computation failures, in milliseconds */
    public long getTileFailureRetryWaitTime() {
        return tileFailureRetryWaitTime;
    }

    public void setTileFailureRetryWaitTime(long tileFailureRetryWaitTime) {
        this.tileFailureRetryWaitTime = tileFailureRetryWaitTime;
    }

    /**
     * Total amount of failures before stopping the seeding process, computed across all threads in
     * the seed request.
     */
    public long getTotalFailuresBeforeAborting() {
        return totalFailuresBeforeAborting;
    }

    public void setTotalFailuresBeforeAborting(long totalFailuresBeforeAborting) {
        this.totalFailuresBeforeAborting = totalFailuresBeforeAborting;
    }
}
