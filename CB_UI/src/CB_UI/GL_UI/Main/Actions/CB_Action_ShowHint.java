/*
 * Copyright (C) 2015 team-cachebox.de
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
package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Controls.Dialogs.HintDialog;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowHint extends CB_Action {

    public CB_Action_ShowHint() {
        super("hint", MenuID.AID_SHOW_HINT);
    }

    @Override
    public void Execute() {
        if (getEnabled())
            HintDialog.show();
    }

    @Override
    public boolean getEnabled() {
        // liefert true zurück wenn ein Cache gewählt ist und dieser einen Hint hat
        if (GlobalCore.getSelectedCache() == null)
            return false;
        String hintText = GlobalCore.getSelectedCache().getHint();
        if ((hintText == null) || (hintText.length() == 0))
            return false;
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.hintIcon.name());
    }
}
