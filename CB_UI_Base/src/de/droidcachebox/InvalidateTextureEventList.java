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
package de.droidcachebox;

import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.log.Log;

public class InvalidateTextureEventList {
    private static final String log = "InvalidateTextureEventList";
    public static CB_List<invalidateTextureEvent> list = new CB_List<invalidateTextureEvent>();

    public static void addListener(invalidateTextureEvent event) {
        synchronized (list) {
            if (!list.contains(event))
                list.add(event);
        }
    }

    public static void removeListener(invalidateTextureEvent event) {
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

    public interface invalidateTextureEvent {
        void invalidateTexture();
    }

}
