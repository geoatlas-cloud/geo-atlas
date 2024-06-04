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

import com.google.common.annotations.VisibleForTesting;
import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.conveyor.ConveyorTile;
import org.geoatlas.cache.core.source.TileSource;
import org.geoatlas.cache.core.storage.StorageBroker;
import org.geoatlas.cache.core.storage.TileRange;
import org.geoatlas.cache.core.storage.TileRangeIterator;
import org.geoatlas.cache.core.util.Sleeper;
import org.geotools.util.logging.Logging;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A TileTask for seeding/reseeding the cache. */
class SeedTask extends TileTask {
    private static Logger log = Logging.getLogger(SeedTask.class.getName());

    private final TileRangeIterator trIter;

    private final TileSource source;

    private boolean reseed;

    private StorageBroker storageBroker;

    private int tileFailureRetryCount;

    private long tileFailureRetryWaitTime;

    private long totalFailuresBeforeAborting;

    private AtomicLong sharedFailureCounter;

    @VisibleForTesting
    Sleeper sleeper = Thread::sleep;

    /** Constructs a SeedTask */
    public SeedTask(
            StorageBroker sb,
            TileRangeIterator trIter,
            TileSource source,
            boolean reseed, String layerName) {
        this.storageBroker = sb;
        this.trIter = trIter;
        this.source = source;
        this.reseed = reseed;

        tileFailureRetryCount = 0;
        tileFailureRetryWaitTime = 100;
        totalFailuresBeforeAborting = 10000;
        sharedFailureCounter = new AtomicLong();

        if (reseed) {
            super.parsedType = TileTask.TYPE.RESEED;
        } else {
            super.parsedType = TileTask.TYPE.SEED;
        }

        super.state = TileTask.STATE.READY;
        super.layerName = layerName;
    }

    // TODO: refactoring this into smaller functions might improve readability
    @Override
    protected void doActionInternal() throws GeoAtlasCacheException, InterruptedException {
        super.state = TileTask.STATE.RUNNING;

        // Lower the priority of the thread
        reprioritize();

        checkInterrupted();

        // approximate thread creation time
        final long START_TIME = System.currentTimeMillis();

        final String layerName = getLayerName();
        log.info(getThreadName() + " begins seeding layer : " + layerName);

        TileRange tr = trIter.getTileRange();

        checkInterrupted();
        // TODO move to TileRange object, or distinguish between thread and task
        super.tilesTotal = tileCount(tr);

        final int metaTilingFactorX = source.getMetaTilingFactors()[0];
        final int metaTilingFactorY = source.getMetaTilingFactors()[1];

        final boolean tryCache = !reseed;

        checkInterrupted();
        long[] gridLoc = trIter.nextMetaGridLocation(new long[3]);

        long seedCalls = 0;
        while (gridLoc != null && this.terminate == false) {

            checkInterrupted();
            Map<String, String> fullParameters = tr.getParameters();

            ConveyorTile tile =
                    new ConveyorTile(
                            storageBroker,
                            layerName,
                            tr.getGridSetId(),
                            gridLoc,
                            tr.getMimeType(),
                            fullParameters,
                            tr.getSubset(),
                            null,
                            null);

            for (int fetchAttempt = 0;
                    fetchAttempt <= tileFailureRetryCount || tileFailureRetryCount < 0;
                    fetchAttempt++) {
                try {
                    checkInterrupted();
                    source.seedTile(tile, tryCache);
                    break; // success, let it go
                } catch (Exception e) {
                    // if GWC_SEED_RETRY_COUNT was not set then none of the settings have effect, in
                    // order to keep backwards compatibility with the old behaviour
                    if (tileFailureRetryCount < 0) {
                        if (e instanceof GeoAtlasCacheException) {
                            throw (GeoAtlasCacheException) e;
                        }
                        throw new GeoAtlasCacheException(e);
                    }

                    long sharedFailureCount = sharedFailureCounter.incrementAndGet();
                    if (sharedFailureCount >= totalFailuresBeforeAborting) {
                        log.info(
                                "Aborting seed thread "
                                        + getThreadName()
                                        + ". Error count reached configured maximum of "
                                        + totalFailuresBeforeAborting);
                        super.state = TileTask.STATE.DEAD;
                        return;
                    }
                    String logMsg =
                            "Seed failed at "
                                    + tile.toString()
                                    + " after "
                                    + (fetchAttempt + 1)
                                    + " of "
                                    + (tileFailureRetryCount + 1)
                                    + " attempts.";
                    if (fetchAttempt < tileFailureRetryCount) {
                        log.fine(logMsg);
                        if (tileFailureRetryWaitTime > 0) {
                            log.finer(
                                    "Waiting " + tileFailureRetryWaitTime + " before trying again");
                            waitToRetry();
                        }
                    } else {
                        log.log(
                                Level.WARNING,
                                logMsg
                                        + " Skipping and continuing with next tile. Total failure count across threads is at: "
                                        + sharedFailureCount,
                                e);
                    }
                }
            }

            if (log.isLoggable(Level.FINER)) {
                log.finer(getThreadName() + " seeded " + Arrays.toString(gridLoc));
            }

            // final long totalTilesCompleted = trIter.getTilesProcessed();
            // note: computing the # of tiles processed by this thread instead of by the whole group
            // also reduces thread contention as the trIter methods are synchronized and profiler
            // shows 16 threads block on synchronization about 40% the time
            final long tilesCompletedByThisThread =
                    seedCalls * metaTilingFactorX * metaTilingFactorY;

            updateStatusInfo(tilesCompletedByThisThread, START_TIME);

            checkInterrupted();
            seedCalls++;
            gridLoc = trIter.nextMetaGridLocation(gridLoc);
        }

        if (this.terminate) {
            log.info(
                    "Job on "
                            + getThreadName()
                            + " was terminated after "
                            + this.tilesDone
                            + " tiles");
        } else {
            log.info(
                    getThreadName()
                            + " completed (re)seeding layer "
                            + layerName
                            + " after "
                            + this.tilesDone
                            + " tiles and "
                            + this.timeSpent
                            + " seconds.");
        }

        checkInterrupted();
        super.state = TileTask.STATE.DONE;
    }

    private void reprioritize() {
        Thread.currentThread()
                .setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2);
    }

    private void waitToRetry() throws InterruptedException {
        sleeper.sleep(tileFailureRetryWaitTime);
    }

    private String getThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * helper for counting the number of tiles
     *
     * @return -1 if too many
     */
    private long tileCount(TileRange tr) {

        final int startZoom = tr.getZoomStart();
        final int stopZoom = tr.getZoomStop();

        long count = 0;

        for (int z = startZoom; z <= stopZoom; z++) {
            long[] gridBounds = tr.rangeBounds(z);

            final long minx = gridBounds[0];
            final long maxx = gridBounds[2];
            final long miny = gridBounds[1];
            final long maxy = gridBounds[3];

            long thisLevel = (1 + maxx - minx) * (1 + maxy - miny);

            if (thisLevel > (Long.MAX_VALUE / 4) && z != stopZoom) {
                return -1;
            } else {
                count += thisLevel;
            }
        }

        return count;
    }

    /** Helper method to update the members tracking thread progress. */
    private void updateStatusInfo(long tilesCount, long start_time) {

        // working on tile
        this.tilesDone = tilesCount;

        // estimated time of completion in seconds, use a moving average over the last
        this.timeSpent = (int) (System.currentTimeMillis() - start_time) / 1000;

        int threadCount = sharedThreadCount.get();
        long timeTotal =
                Math.round(
                        (double) timeSpent
                                * (((double) tilesTotal / threadCount) / (double) tilesCount));

        this.timeRemaining = (int) (timeTotal - timeSpent);
    }
    public void setFailurePolicy(
            int tileFailureRetryCount,
            long tileFailureRetryWaitTime,
            long totalFailuresBeforeAborting,
            AtomicLong sharedFailureCounter) {
        this.tileFailureRetryCount = tileFailureRetryCount;
        this.tileFailureRetryWaitTime = tileFailureRetryWaitTime;
        this.totalFailuresBeforeAborting = totalFailuresBeforeAborting;
        this.sharedFailureCounter = sharedFailureCounter;
    }

    @Override
    protected void dispose() {
    }
}
