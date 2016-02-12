package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_Help extends CB_Action {

	public CB_Action_Help() {
		super("Help Online", MenuID.AID_HELP);
	}

	@Override
	public Sprite getIcon() {
		return SpriteCacheBase.Icons.get(IconName.help_34.ordinal());
	}

	@Override
	public void Execute() {
		PlatformConnector.callUrl("http://www.team-cachebox.de/index.php/de/kurzanleitung");
	}

}
