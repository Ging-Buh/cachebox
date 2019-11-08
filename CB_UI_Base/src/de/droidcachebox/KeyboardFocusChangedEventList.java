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

import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.log.Log;

public class KeyboardFocusChangedEventList {
    public static CB_List<KeyboardFocusChangedEvent> list = new CB_List<KeyboardFocusChangedEvent>();

    // normally is only one item in this list: the active view or activity
    public static void Add(KeyboardFocusChangedEvent event) {
        synchronized (list) {
            if (!list.contains(event))
                list.add(event);
        }
    }

    public static void Remove(KeyboardFocusChangedEvent event) {
        synchronized (list) {
            list.remove(event);
        }
    }

    public static void Call(final EditTextField editTextField) {
        if (editTextField == null || (editTextField != null && !editTextField.isKeyboardPopupDisabled())) {
            synchronized (list) {
                for (int i = 0, n = list.size(); i < n; i++) {
                    KeyboardFocusChangedEvent event = list.get(i);
                    Log.debug("KeyboardFocusChangedEventList", "call event: " + event + " for " + editTextField);
                    event.KeyboardFocusChanged(editTextField);
                }
            }
        }
    }

}
