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

import CB_Utils.Util.HSV_Color;

import com.badlogic.gdx.graphics.Color;

/**
 * @author Longri
 */
public class SettingColor extends SettingBase<Color>
{

	public SettingColor(String name, SettingCategory category, SettingModus modus, Color defaultValue, SettingStoreType StoreType,
			SettingUsage usage)
	{
		super(name, category, modus, StoreType, usage);
		this.defaultValue = defaultValue;
	}

	@Override
	public String toDBString()
	{
		return value.toString();
	}

	@Override
	public boolean fromDBString(String dbString)
	{
		try
		{
			value = new HSV_Color(dbString);
			return true;
		}
		catch (Exception e)
		{
			value = defaultValue;
			return false;
		}
	}

	@Override
	public SettingBase<Color> copy()
	{
		SettingBase<Color> ret = new SettingColor(this.name, this.category, this.modus, this.defaultValue, this.storeType, this.usage);
		ret.value = this.value;
		ret.lastValue = this.lastValue;
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof SettingColor)) return false;

		SettingColor inst = (SettingColor) obj;
		if (!(inst.name.equals(this.name))) return false;
		if (!inst.value.equals(this.value)) return false;

		return true;
	}

}
