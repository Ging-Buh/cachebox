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

public class SettingFolder extends SettingLongString {

    private final boolean needWritePermission;

    public SettingFolder(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType, boolean needwritePermission) {
        super(name, category, modus, defaultValue, StoreType);
        this.needWritePermission = needwritePermission;
    }

    public SettingFolder(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType, boolean needwritePermission, boolean needRestart) {
        super(name, category, modus, defaultValue, StoreType);
        this.needWritePermission = needwritePermission;
        if (needRestart) setNeedRestart();
    }

    @Override
    public String getValue() {
        return replacePathSeparator(value);
    }

    @Override
    public String getDefaultValue() {
        return replacePathSeparator(defaultValue);
    }

    private String replacePathSeparator(String rep) {
        if (rep.startsWith("?")) {
            // for databases having repository-files in own separate area (setting names end with Local)
            rep = Config_Core.workPath + System.getProperty("file.separator") + "Repositories" + rep.substring(1);
        }
        rep = rep.replace("\\", System.getProperty("file.separator"));
        rep = rep.replace("/", System.getProperty("file.separator"));
        return rep;
    }

    public boolean needWritePermission() {
        return this.needWritePermission;
    }

}
