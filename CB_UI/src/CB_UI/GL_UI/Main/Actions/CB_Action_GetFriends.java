package CB_UI.GL_UI.Main.Actions;

import CB_Core.Api.GroundspeakAPI;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Sprites;
import com.badlogic.gdx.graphics.g2d.Sprite;

import static CB_UI_Base.GL_UI.Menu.MenuID.AID_GetFriends;

public class CB_Action_GetFriends extends CB_Action {
    private static CB_Action_GetFriends that;

    private CB_Action_GetFriends() {
        super("Friends", AID_GetFriends);
    }

    public static CB_Action_GetFriends getInstance() {
        if (that == null) that = new CB_Action_GetFriends();
        return that;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(Sprites.IconName.friends.name());
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
                MessageBox.show(Translation.get("ok") + ":\n" + friends, Translation.get("Friends"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            } else {
                MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("Friends"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            }
        });
    }
}
