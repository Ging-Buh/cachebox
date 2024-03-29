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

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.menuBtn2.executes.Hint;
import de.droidcachebox.utils.UnitFormatter;

public class ShowHint extends AbstractAction {

    public ShowHint() {
        super("hint");
    }

    @Override
    public void execute() {
        if (getEnabled()) {
            new Hint(UnitFormatter.Rot13(GlobalCore.getSelectedCache().getHint()) + "\n ").show();
        }
    }

    @Override
    public boolean getEnabled() {
        return GlobalCore.getSelectedCache() != null && GlobalCore.getSelectedCache().hasHint();
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.hintIcon.name());
    }
}
