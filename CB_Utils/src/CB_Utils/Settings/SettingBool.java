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
package CB_Utils.Settings;

public class SettingBool extends SettingBase<Boolean>
{

	public SettingBool(String name, SettingCategory category, SettingModus modus, boolean defaultValue, SettingStoreType StoreType,
			SettingUsage usage)
	{
		super(name, category, modus, StoreType, usage);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	@Override
	public String toDBString()
	{
		return String.valueOf(value);
	}

	@Override
	public boolean fromDBString(String dbString)
	{
		try
		{
			value = Boolean.valueOf(dbString);
			return true;
		}
		catch (Exception ex)
		{
			value = defaultValue;
			return false;
		}
	}

	@Override
	public SettingBase<Boolean> copy()
	{
		SettingBase<Boolean> ret = new SettingBool(this.name, this.category, this.modus, this.defaultValue, this.storeType, this.usage);

		ret.value = this.value;
		ret.lastValue = this.lastValue;

		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof SettingBool)) return false;

		SettingBool inst = (SettingBool) obj;
		if (!(inst.name.equals(this.name))) return false;
		if (inst.value != this.value) return false;

		return true;
	}
}
