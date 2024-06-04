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
 * @author Arne Kepp / The Open Planning Project 2008
 */
package org.geoatlas.cache.core.seed;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.geoatlas.cache.core.source.TileSource;
import org.geoatlas.cache.core.storage.StorageBroker;
import org.geoatlas.cache.core.storage.TileRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TruncateTask extends TileTask {
    private static Logger log = LoggerFactory.getLogger(TruncateTask.class.getName());

    private final TileRange tr;

    private final TileSource tl;

    private final StorageBroker storageBroker;

    public TruncateTask(StorageBroker sb, TileRange tr, TileSource tl, String layerName) {
        this.storageBroker = sb;
        this.tr = tr;
        this.tl = tl;

        super.parsedType = TileTask.TYPE.TRUNCATE;
        super.layerName = layerName;
    }

    @Override
    protected void doActionInternal() throws GeoAtlasCacheException, InterruptedException {
        super.state = TileTask.STATE.RUNNING;
        checkInterrupted();
        try {
            storageBroker.delete(tr);
        } catch (Exception e) {
            super.state = TileTask.STATE.DEAD;
            log.error("During truncate request", e);
        }

        checkInterrupted();

        if (super.state != TileTask.STATE.DEAD) {
            super.state = TileTask.STATE.DONE;
            log.info("Completed truncate request.");
        }
    }

    @Override
    protected void dispose() {
        // do nothing
    }
}
