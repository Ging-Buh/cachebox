package CB_UI.GL_UI.Main.Actions;

import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Action_OpenAndroMaps extends AbstractAction {
    private static Action_OpenAndroMaps that;
    private Action_OpenAndroMaps(){
        super("LoadMapFromOpenAndroMapsMenuTitle", MenuID.AID_URL_OpenAndroMaps);
    }
    public static Action_OpenAndroMaps getInstance() {
        if (that == null) that = new Action_OpenAndroMaps();
        return that;    }
    @Override
    public void Execute() {
        try {
            PlatformConnector.callUrl("https://www.openandromaps.org/downloads/deutschland");
        }
        catch (Exception ignored) {
        }
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(Sprites.IconName.mapsforge_logo.name());
    }
}
