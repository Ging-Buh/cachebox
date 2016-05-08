/* 
 * Copyright (C) 2016 team-cachebox.de
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
package CB_Utils.Settings;

/** 
 * TODO document
 * @author Longri  2016
 *
 */
public class SettingStringList extends SettingBase<String[]> {
	final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SettingStringList.class);

	public SettingStringList(String name, SettingCategory category, SettingModus modus, String[] defaultValue, SettingStoreType StoreType, SettingUsage usage) {
		super(name, category, modus, StoreType, usage);
		this.defaultValue = defaultValue;
	}

	@Override
	public String toDBString() {

		StringBuilder sb = new StringBuilder();
		int idx = 0;
		for (String str : value) {
			sb.append(str);
			if (idx++ < value.length - 1)
				sb.append(SettingString.STRING_SPLITTER);
		}
		return String.valueOf(value);
	}

	@Override
	public boolean fromDBString(String dbString) {
		try {
			value = dbString.split(SettingString.STRING_SPLITTER);
			return true;
		} catch (Exception ex) {
			value = defaultValue;
			return false;
		}
	}

	@Override
	public SettingBase<String[]> copy() {
		SettingBase<String[]> ret = new SettingStringList(this.name, this.category, this.modus, this.defaultValue, this.storeType, usage);
		ret.value = this.value;
		ret.lastValue = this.lastValue;
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SettingStringList))
			return false;

		SettingStringList inst = (SettingStringList) obj;
		if (!(inst.name.equals(this.name)))
			return false;
		if (this.value == null) {
			if (inst.value == null)
				return true;
			return false;
		}
		if (!inst.value.equals(this.value))
			return false;

		return true;
	}
}
