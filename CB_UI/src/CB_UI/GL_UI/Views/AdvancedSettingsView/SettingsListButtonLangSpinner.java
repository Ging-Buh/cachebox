/* 
 * Copyright (C) 2011-2014 team-cachebox.de
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
package CB_UI.GL_UI.Views.AdvancedSettingsView;

import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingUsage;

/**
 * Der Button der sich hinter einer Category verbirgt und in der Settings List als Toggle Button dieser Category angezeigt wird.
 * 
 * @author Longri
 */
public class SettingsListButtonLangSpinner<T> extends SettingBase<T> {

	public SettingsListButtonLangSpinner(String name, SettingCategory category, SettingModus modus, SettingStoreType StoreType, SettingUsage usage) {
		super(name, category, modus, StoreType, usage);

	}

	@Override
	public String toDBString() {

		return null;
	}

	@Override
	public boolean fromDBString(String dbString) {

		return false;
	}

	@Override
	public SettingBase<T> copy() {
		// can't copy this obj
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SettingsListButtonLangSpinner<?>))
			return false;

		SettingsListButtonLangSpinner<?> inst = (SettingsListButtonLangSpinner<?>) obj;
		if (!(inst.name.equals(this.name)))
			return false;
		if (inst.value != this.value)
			return false;

		return true;
	}

}
