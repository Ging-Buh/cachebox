/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_Locator.Map;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Longri
 */
class QueueData
{
	final LoadetSortedTiles loadedTiles = new LoadetSortedTiles();
	final LoadetSortedTiles loadedOverlayTiles = new LoadetSortedTiles();
	final Lock loadedTilesLock = new ReentrantLock();
	final Lock loadedOverlayTilesLock = new ReentrantLock();
	final SortedMap<Long, Descriptor> queuedTiles = new TreeMap<Long, Descriptor>();
	final SortedMap<Long, Descriptor> queuedOverlayTiles = new TreeMap<Long, Descriptor>();
	final Lock queuedTilesLock = new ReentrantLock();
	final Lock queuedOverlayTilesLock = new ReentrantLock();
	Layer CurrentLayer = null;
	Layer CurrentOverlayLayer = null;
}
