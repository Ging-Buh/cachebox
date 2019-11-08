/*
 * Copyright (C) 2014 team-cachebox.de
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
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBox.OnMsgBoxClickListener;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.translation.Translation;

public class Action_ShowQuit extends AbstractAction {
    static MessageBox msg;
    private static Action_ShowQuit that;
    String OverrideAppName = null;

    private Action_ShowQuit() {
        super("quit", MenuID.AID_SHOW_QUIT);
    }

    public static Action_ShowQuit getInstance() {
        if (that == null) that = new Action_ShowQuit();
        return that;
    }

    public void OverrideAppName(String name) {
        OverrideAppName = name;
    }

    @Override
    public void Execute() {
        // if (askIsShown) return;

        if (msg != null && GL.that.getCurrentDialog() == msg)
            return;

        String Msg = Translation.get("QuitReally");
        String Title = Translation.get("Quit?");

        if (OverrideAppName != null) {
            Msg = Msg.replace("Cachebox", OverrideAppName);
            Title = Title.replace("Cachebox", OverrideAppName);
        }

        try {
            msg = MessageBox.show(Msg, Title, MessageBoxButtons.OKCancel, MessageBoxIcon.Stop, new OnMsgBoxClickListener() {
                @Override
                public boolean onClick(int which, Object data) {
                    if (which == MessageBox.BUTTON_POSITIVE) {

                        //Log.debug(log, "\r\n Quit");
                        PlatformUIBase.quit();
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            PlatformUIBase.quit();
        }
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.closeIcon.name());
    }
}
