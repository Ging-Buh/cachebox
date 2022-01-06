/*
 * Copyright (C) 2011-2022 team-cachebox.de
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
package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

public class SwitchTorch extends AbstractAction {

    private static SwitchTorch that;

    private SwitchTorch() {
        super("torch");
    }

    public static SwitchTorch getInstance() {
        if (that == null) that = new SwitchTorch();
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
    public void execute() {
        PlatformUIBase.switchTorch();
    }
}
