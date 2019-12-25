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

package de.droidcachebox.gdx;

/**
 * Stellt die Identifizierung einer View dar.
 *
 * @author Longri
 */
public class ViewID {

    public final static int DESCRIPTION_VIEW = 4;

    public final static int NAVIGATE_TO = 10;
    public final static int VOICE_REC = 11;
    public final static int TAKE_PHOTO = 12;
    public final static int VIDEO_REC = 13;
    public final static int WhatsApp = 14;

    private final int id;
    private final UI_Pos pos;
    private final UI_Type type;

    /**
     * @param id     = Int
     * @param type   = Android or OpenGL
     * @param pos    = Left or Right for Phone layout
     */
    public ViewID(int id, UI_Type type, UI_Pos pos) {
        this.id = id;
        this.type = type;
        this.pos = pos;
    }

    public int getID() {
        return id;
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
