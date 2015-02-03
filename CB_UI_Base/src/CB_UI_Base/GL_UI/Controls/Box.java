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

package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.Math.CB_RectF;

/**
 * Eine leere Box, die Controls(GL_View_Base) aufnehmen kann
 * 
 * @author Longri
 */
public class Box extends CB_View_Base {

    public Box() {
	super("Box");
    }

    public Box(CB_RectF rec, String Name) {
	super(rec, Name);
    }

    public Box(float Width, float Height, String Name) {
	super(0f, 0f, Width, Height, Name);
    }

    public Box(float width, float height) {
	super(0f, 0f, width, height, "");
    }

    @Override
    protected void Initial() {

    }

    @Override
    protected void SkinIsChanged() {

    }
}
