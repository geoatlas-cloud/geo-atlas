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
 */
package org.geoatlas.cache.core.seed;

import org.geoatlas.cache.core.GeoAtlasCacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

class MTSeeder implements Callable<TileTask> {
    private static Logger log = LoggerFactory.getLogger(MTSeeder.class.getName());

    protected TileTask task = null;

    public MTSeeder(TileTask task) {
        this.task = task;
    }

    @Override
    public TileTask call() {
        try {
            task.doAction();
        } catch (GeoAtlasCacheException gwce) {
            log.error(gwce.getMessage(), gwce);
        } catch (InterruptedException e) {
            log.info(task.getType() + " task #" + task.getTaskId() + " has been interrupted");
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            log.error(task.getType() + " task #" + task.getTaskId() + " failed", e);
        }
        return task;
    }
}
