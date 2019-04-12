package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Activitys.MapDownload;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Sprites;
import com.badlogic.gdx.graphics.g2d.Sprite;

import static CB_UI_Base.GL_UI.Menu.MenuID.MI_MAP_DOWNOAD;

public class CB_Action_MapDownload extends CB_Action {
    private static CB_Action_MapDownload that;

    private CB_Action_MapDownload() {
        super("MapDownload", MI_MAP_DOWNOAD);
    }

    public static CB_Action_MapDownload getInstance() {
        if (that == null) that = new CB_Action_MapDownload();
        return that;
    }

    @Override
    public void Execute() {
        MapDownload.getInstance().show();
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(Sprites.IconName.freizeit.name());
    }

}
