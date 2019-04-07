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
package CB_UI_Base.GL_UI.Main.Actions;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowQuit extends CB_Action {
    static MessageBox msg;
    private static CB_Action_ShowQuit that;
    String OverrideAppName = null;

    private CB_Action_ShowQuit() {
        super("quit", MenuID.AID_SHOW_QUIT);
    }

    public static CB_Action_ShowQuit getInstance() {
        if (that == null) that = new CB_Action_ShowQuit();
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
                        PlatformConnector.callQuit();
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            PlatformConnector.callQuit();
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
