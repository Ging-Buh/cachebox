/*
 * Copyright (C) 2011-2020 team-cachebox.de
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
package de.droidcachebox.settings;


/**
 * String is for translation
 */
public enum SettingCategory {
    Login("Login"), QuickList("QuickList"), Map("Map"), LiveMap("LiveMap"), Gps("Gps"), Compass("Compass"), Misc("Misc"), Sounds("Sounds"), Skin("Skin"), API("API"), Folder("Folder"), Templates("Templates"), Drafts("Drafts"), Internal(
            "Internal"), CarMode("CarMode"), RememberAsk("RememberAsk"), Debug("Debug"), Button("Button"), Positions("Positions"), CBS("CBS"),;

    private String langString;

    SettingCategory(String langString) {
        this.langString = langString;
    }
}
