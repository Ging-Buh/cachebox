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
package de.droidcachebox.menu.menuBtn2;

import static de.droidcachebox.gdx.controls.messagebox.MsgBox.calcMsgBoxSize;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.utils.UnitFormatter;

public class HintDialog extends AbstractAction {

    private static HintDialog that;

    private HintDialog() {
        super("hint");
    }

    public static HintDialog getInstance() {
        if (that == null) that = new HintDialog();
        return that;
    }

    @Override
    public void execute() {
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

        new de.droidcachebox.menu.menuBtn2.executes.HintDialog(decodedSize.height > encodedSize.height ? decodedSize : encodedSize, hintTextEncoded).show();
    }
}
