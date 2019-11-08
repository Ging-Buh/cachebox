/*
 * Copyright (C) 2011-2014 team-cachebox.de
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
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

public class Action_switch_Torch extends AbstractAction {

    private static Action_switch_Torch that;

    private Action_switch_Torch() {
        super("torch", MenuID.AID_TORCH);
    }

    public static Action_switch_Torch getInstance() {
        if (that == null) that = new Action_switch_Torch();
        return that;
    }

    @Override
    public boolean getEnabled() {
        return PlatformUIBase.isTorchAvailable();
    }

    @Override
    public Sprite getIcon() {
        if (PlatformUIBase.isTorchOn()) {
            return Sprites.getSprite(IconName.TORCHON.name());
        } else {
            return Sprites.getSprite(IconName.TORCHOFF.name());
        }
    }

    @Override
    public void Execute() {
        PlatformUIBase.switchTorch();
    }
}
