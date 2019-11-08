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

package de.droidcachebox.gdx;

import static de.droidcachebox.gdx.main.MenuID.*;

/**
 * Stellt die Identifizierung einer View dar.
 *
 * @author Longri
 */
public class ViewID {

    public final static int DESCRIPTION_VIEW = 4;

    public final static int NAVIGATE_TO = AID_NAVIGATE_TO;
    public final static int VOICE_REC = AID_VOICE_REC;
    public final static int TAKE_PHOTO = AID_TAKE_PHOTO;
    public final static int VIDEO_REC = AID_VIDEO_REC;
    public final static int WhatsApp = AID_Share;

    private final int Id;
    private final UI_Pos pos;
    private final UI_Pos posTab;
    private final UI_Type type;

    /**
     * @param ID     = Int
     * @param Type   = Android or OpenGL
     * @param Pos    = Left or Right for Phone Layout
     * @param PosTab = Left or Right for Tab Layout
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
        return pos;
    }

    public enum UI_Pos {
        Left, Right
    }

    public enum UI_Type {
        Android, OpenGl, Activity
    }
}
