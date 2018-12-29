package CB_UI.GL_UI.Main.Actions;

import CB_Core.Api.GroundspeakAPI;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;

import static CB_UI_Base.GL_UI.Menu.MenuID.AID_GetFriends;

public class CB_Action_GetFriends extends CB_Action {
    public CB_Action_GetFriends() {
        super("Friends", AID_GetFriends);
    }

    @Override
    public void Execute() {
        getFriends();
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    public void getFriends() {
        GL.that.postAsync(() -> {
            String friends = GroundspeakAPI.fetchFriends();
            if (GroundspeakAPI.APIError == 0) {
                Config.Friends.setValue(friends);
                Config.AcceptChanges();
                GL_MsgBox.Show(Translation.Get("ok") + ":\n" + friends, Translation.Get("Friends"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            }
            else {
                GL_MsgBox.Show(GroundspeakAPI.LastAPIError, Translation.Get("Friends"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            }
        });
    }
}
