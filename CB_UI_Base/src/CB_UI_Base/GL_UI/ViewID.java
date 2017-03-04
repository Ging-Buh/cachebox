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

package CB_UI_Base.GL_UI;

import CB_UI_Base.Global;

/**
 * Stellt die Identifizierung einer View dar.
 * 
 * @author Longri
 */
public class ViewID {

	public final static int MAP_VIEW = 0;
	public final static int DESCRIPTION_VIEW = 4;
	public final static int JOKER_VIEW = 12;

	public final static int NAVIGATE_TO = 108;
	public final static int VOICE_REC = 110;
	public final static int TAKE_PHOTO = 111;
	public final static int VIDEO_REC = 112;

	public enum UI_Pos {
		Left, Right
	}

	public enum UI_Type {
		Android, OpenGl, Activity
	}

	private final int Id;
	private final UI_Pos pos;
	private final UI_Pos posTab;
	private final UI_Type type;

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
	public ViewID(int ID, UI_Type Type, UI_Pos Pos, UI_Pos PosTab) {
		Id = ID;
		type = Type;
		pos = Pos;
		posTab = PosTab;
	}

	public int getID() {
		return Id;
	}

	public UI_Type getType() {
		return type;
	}

	public UI_Pos getPos() {
		return Global.isTab ? posTab : pos;
	}
}
