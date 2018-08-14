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
package CB_UI.GL_UI.Main.Actions;

import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_switch_Torch extends CB_Action {

    public CB_Action_switch_Torch() {
        super("Torch", MenuID.AID_TORCH);
    }

    @Override
    public boolean getEnabled() {
        return PlatformConnector.isTorchAvailable();
    }

    @Override
    public Sprite getIcon() {
        if (PlatformConnector.isTorchOn()) {
            return Sprites.getSprite(IconName.TORCHON.name());
        } else {
            return Sprites.getSprite(IconName.TORCHOFF.name());
        }
    }

    @Override
    public void Execute() {
        PlatformConnector.switchTorch();
    }
}
