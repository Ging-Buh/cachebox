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

/***
 * Login("Login"), Map("Map"), Gps("Gps"), Skin("Skin"), Internal("Internal"), Folder("Folder"), Button("Button"), Misc("Misc"),
 * Templates("Templates"), API("API"), Debug("Debug")
 */
public enum SettingCategory
{
	Login("Login"),
	QuickList("QuickList"),
	Map("Map"),
	LiveMap("LiveMap"),
	Gps("Gps"),
	Compass("Compass"),
	Misc("Misc"),
	Sounds("Sounds"),
	Skin("Skin"),
	API("API"),
	Folder("Folder"),
	Templates("Templates"),
	Fieldnotes("Fieldnotes"),
	Internal("Internal"),
	CarMode("CarMode"),
	RememberAsk("RememberAsk"),
	Debug("Debug"),
	Button("Button"),
	Positions("Positions"),
	CBS("CBS"), ;

	private String langString;

	SettingCategory(String langString)
	{
		this.setLangString(langString);
	}

	public String getLangString()
	{
		return langString;
	}

	public void setLangString(String langString)
	{
		this.langString = langString;
	}

	private boolean mIsCollapse = false;

	public boolean IsCollapse()
	{
		return mIsCollapse;
	}

	public void Toggle()
	{
		mIsCollapse = !mIsCollapse;
	}

	public void Toggle(boolean value)
	{
		mIsCollapse = value;
	}

}
