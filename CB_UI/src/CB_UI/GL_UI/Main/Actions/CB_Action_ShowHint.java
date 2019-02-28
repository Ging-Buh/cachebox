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
import CB_UI_Base.Math.Size;
import CB_Utils.Util.UnitFormatter;
import com.badlogic.gdx.graphics.g2d.Sprite;

import static CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.calcMsgBoxSize;

public class CB_Action_ShowHint extends CB_Action {

    private static CB_Action_ShowHint that;

    private CB_Action_ShowHint() {
        super("hint", MenuID.AID_SHOW_HINT);
    }

    public static CB_Action_ShowHint getInstance() {
        if (that == null) that = new CB_Action_ShowHint();
        return that;
    }

    @Override
    public void Execute() {
        if (getEnabled()) {
            showHint();
        }
    }

    @Override
    public boolean getEnabled() {
        // liefert true zurück wenn ein Cache gewählt ist und dieser einen Hint hat
        if (GlobalCore.getSelectedCache() == null)
            return false;
        return GlobalCore.getSelectedCache().hasHint();
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.hintIcon.name());
    }

    public void showHint() {
        String HintFromDB = GlobalCore.getSelectedCache().getHint();

        String hintTextEncoded = UnitFormatter.Rot13(HintFromDB) + "\n "; // in DB is readable
        String hintTextDecoded = HintFromDB + "\n ";

        Size decodedSize = calcMsgBoxSize(hintTextDecoded, true, true, false);
        Size encodedSize = calcMsgBoxSize(hintTextEncoded, true, true, false);

        new HintDialog(decodedSize.height > encodedSize.height ? decodedSize : encodedSize, hintTextEncoded).show();
    }
}
