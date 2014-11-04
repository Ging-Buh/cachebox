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

import CB_Utils.Tag;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.Gdx;

public class SettingEnum<EnumTyp extends Enum<?>> extends SettingString
{

	private CB_List<String> values;

	private EnumTyp myEnum;

	@SuppressWarnings("rawtypes")
	public SettingEnum(String name, SettingCategory category, SettingModus modus, EnumTyp defaultValue, SettingStoreType StoreType, SettingUsage usage, EnumTyp enu)
	{
		super(name, category, modus, defaultValue.name(), StoreType, usage);
		myEnum = enu;

		values = new CB_List<String>();

		// hier bekommst du die Klasse TestEnum
		Class c = enu.getDeclaringClass();
		// hier kannst du alle Zustände abfragen
		Object[] oo = c.getEnumConstants();
		// hier kannst du dann über alle Zustände iterieren
		for (Object o : oo)
		{
			// und von jedem den Namen abfragen (in unserem Beispiel "wert1",
			// "wert2", "wert3"
			values.add(((Enum) o).name());
		}

	}

	public CB_List<String> getValues()
	{
		return values;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(String value)
	{
		if (value == null || value.isEmpty()) return;
		if (this.value.equals(value)) return;

		try
		{
			myEnum = (EnumTyp) EnumTyp.valueOf(myEnum.getDeclaringClass(), value);
			this.value = value;
			setDirty();
		}
		catch (Exception e)
		{
			if (Gdx.app != null)
			{
				Gdx.app.error(Tag.TAG, "Wrong Enum" + value, e);
			}
			else
			{
				e.printStackTrace();
			}

		}
	}

	@SuppressWarnings(
		{ "unchecked" })
	public EnumTyp getEnumValue()
	{
		return (EnumTyp) Enum.valueOf(myEnum.getDeclaringClass(), value);
	}

	@SuppressWarnings(
		{ "unchecked" })
	public EnumTyp getEnumDefaultValue()
	{
		return (EnumTyp) Enum.valueOf(myEnum.getDeclaringClass(), defaultValue);
	}

	public void setEnumValue(EnumTyp value)
	{
		if (this.myEnum == value) return;
		this.value = value.name();
		myEnum = value;
		setDirty();
	}

}
