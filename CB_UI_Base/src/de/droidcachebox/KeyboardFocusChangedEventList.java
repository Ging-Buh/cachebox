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
    public final static CB_List<KeyboardFocusChangedEvent> listeners = new CB_List<>();

    public static void add(KeyboardFocusChangedEvent listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener))
                listeners.add(listener);
        }
    }

    public static void remove(KeyboardFocusChangedEvent listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public static void keyboardFocusChanged(final EditTextField editTextField) {
        if (editTextField == null || !editTextField.isKeyboardPopupDisabled()) {
            synchronized (listeners) {
                for (KeyboardFocusChangedEvent listener : listeners) {
                    Log.debug("KeyboardFocusChangedEventList", "call event: " + listener + " for " + editTextField);
                    listener.keyboardFocusChanged(editTextField);
                }
            }
        }
    }

    public interface KeyboardFocusChangedEvent {
        void keyboardFocusChanged(EditTextField focus);
    }

}
