/*
 * Copyright (C) 2013 team-cachebox.de
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

package de.droidcachebox.translation;

import java.util.ArrayList;

public class LanguageChanged {
    private static final ArrayList<event> handlers = new ArrayList<>();

    public static void add(event handler) {
        handlers.add(handler);
    }

    public static void fire() {
        for (event handler : handlers) {
            handler.changeLanguage();
        }
    }

    public static void remove(event handler) {
        handlers.remove(handler);
    }

    public interface event {
        void changeLanguage();
    }

}
