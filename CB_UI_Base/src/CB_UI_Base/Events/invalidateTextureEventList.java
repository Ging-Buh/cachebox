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
package CB_UI_Base.Events;

import CB_Utils.Log.Log; import org.slf4j.LoggerFactory;

import CB_Utils.Lists.CB_List;

public class invalidateTextureEventList {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(invalidateTextureEventList.class);
	public static CB_List<invalidateTextureEvent> list = new CB_List<invalidateTextureEvent>();

	public static void Add(invalidateTextureEvent event) {
		synchronized (list) {
			if (!list.contains(event))
				list.add(event);
		}
	}

	public static void Remove(invalidateTextureEvent event) {
		synchronized (list) {
			list.remove(event);
		}
	}

	public static void Call() {

		try {
			synchronized (list) {
				for (int i = 0, n = list.size(); i < n; i++) {
					invalidateTextureEvent event = list.get(i);
					if (event != null)
						event.invalidateTexture();
				}
			}
		} catch (Exception e) {
			Log.err(log, "Call()", e);
		}
	}
}
