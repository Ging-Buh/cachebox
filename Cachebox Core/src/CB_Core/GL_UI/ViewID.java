/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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

package CB_Core.GL_UI;

import CB_Core.GlobalCore;

/**
 * Stellt die Identifizierung einer View dar.
 * 
 * @author Longri
 */
public class ViewID
{

	public final static int MAP_VIEW = 0;
	public final static int CACHE_LIST_VIEW = 1;
	public final static int LOG_VIEW = 3;
	public final static int DESCRIPTION_VIEW = 4;
	public final static int SPOILER_VIEW = 5;
	public final static int NOTES_VIEW = 6;
	public final static int SOLVER_VIEW = 7;
	public final static int COMPASS_VIEW = 8;
	public final static int FIELD_NOTES_VIEW = 9;
	public final static int ABOUT_VIEW = 11;
	public final static int JOKER_VIEW = 12;
	public final static int TRACK_LIST_VIEW = 13;
	public final static int TB_LIST_VIEW = 14;
	public final static int WAYPOINT_VIEW = 15;

	public final static int TEST_VIEW = 16;
	public final static int CREDITS_VIEW = 17;
	public final static int GL_MAP_VIEW = 18;
	public final static int MAP_CONTROL_TEST_VIEW = 19;
	public final static int TEST_LIST_VIEW = 20;

	public final static int SETTINGS = 102;
	public final static int FILTER_SETTINGS = 101;
	public final static int IMPORT = 103;
	public final static int SEARCH = 104;
	public final static int MANAGE_DB = 105;
	public final static int CHK_STATE_API = 106;

	public enum UI_Pos
	{
		Left, Right
	}

	public enum UI_Type
	{
		Android, OpenGl, Activity
	}

	private int Id;
	private UI_Pos pos;
	private UI_Pos posTab;
	private UI_Type type;

	/**
	 * @param ID
	 *            = Int
	 * @param Type
	 *            = Android or OpenGL
	 * @param Pos
	 *            = Left or Right for Phone Layout
	 * @param PosTab
	 *            = Left or Right for Tab Layout
	 */
	public ViewID(int ID, UI_Type Type, UI_Pos Pos, UI_Pos PosTab)
	{
		Id = ID;
		type = Type;
		pos = Pos;
		posTab = PosTab;
	}

	public int getID()
	{
		return Id;
	}

	public UI_Type getType()
	{
		return type;
	}

	public UI_Pos getPos()
	{
		return GlobalCore.isTab ? posTab : pos;
	}
}
