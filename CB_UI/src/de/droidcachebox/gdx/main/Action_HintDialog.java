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
package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.dialogs.HintDialog;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.utils.UnitFormatter;

import static de.droidcachebox.gdx.controls.messagebox.MessageBox.calcMsgBoxSize;

public class Action_HintDialog extends AbstractAction {

    private static Action_HintDialog that;

    private Action_HintDialog() {
        super("hint", MenuID.AID_SHOW_HINT);
    }

    public static Action_HintDialog getInstance() {
        if (that == null) that = new Action_HintDialog();
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