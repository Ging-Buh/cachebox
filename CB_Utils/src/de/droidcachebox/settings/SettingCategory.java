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
    Login("Login"),
    Templates("Templates"),
    Map("Map"),
    LiveMap("LiveMap"),
    Gps("Gps"),
    Skin("Skin"),
    QuickList("QuickList"),
    Drafts("Drafts"),
    Misc("Misc"),
    Sounds("Sounds"),
    CarMode("CarMode"),
    Internal("Internal"),
    ;

    private String langString;

    SettingCategory(String langString) {
        this.langString = langString;
    }
}
