package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.SpoilerView;
import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSpoilerView extends CB_Action_ShowView {
    private static CB_Action_ShowSpoilerView that;
    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
    private int spoilerState = -1;
    private Sprite SpoilerIcon;

    private CB_Action_ShowSpoilerView() {
        super("spoiler", MenuID.AID_SHOW_SPOILER);
        // contextMenu = createContextMenu();
    }

    public static CB_Action_ShowSpoilerView getInstance() {
        if (that == null) that = new CB_Action_ShowSpoilerView();
        return that;
    }

    @Override
    public void Execute() {
        TabMainView.leftTab.ShowView(SpoilerView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return GlobalCore.selectedCachehasSpoiler();
    }

    @Override
    public Sprite getIcon() {
        boolean hasSpoiler = GlobalCore.selectedCachehasSpoiler();
        if (hasSpoiler && spoilerState != 1) {
            SpoilerIcon = Sprites.getSprite(IconName.imagesIcon.name());
            spoilerState = 1;
        } else if (!hasSpoiler && spoilerState != 0) {
            SpoilerIcon = new Sprite(Sprites.getSprite(IconName.imagesIcon.name()));
            SpoilerIcon.setColor(DISABLE_COLOR);
            spoilerState = 0;
        }
        return SpoilerIcon;
    }

    @Override
    public CB_View_Base getView() {
        return SpoilerView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return createContextMenu();
    }

    private Menu createContextMenu() {
        Menu icm = new Menu("menu_compassView");
        icm.addOnClickListener((v, x, y, pointer, button) -> {
            switch (((MenuItem) v).getMenuItemId()) {
                case MenuID.MI_RELOAD_SPOILER:
                    GlobalCore.ImportSpoiler().setReadyListener(() -> {
                        // erst die Lokalen Images für den Cache neu laden
                        if (GlobalCore.isSetSelectedCache()) {
                            GlobalCore.getSelectedCache().loadSpoilerRessources();
                            GL.that.RunOnGL(() -> {
                                SpoilerView.getInstance().ForceReload();
                                Execute();
                                SpoilerView.getInstance().onShow();
                            });

                        }

                    });
                    return true;
                case MenuID.MI_START_PICTUREAPP:
                    String file = SpoilerView.getInstance().getSelectedFilePath();
                    if (file == null)
                        return true;
                    PlatformConnector.StartPictureApp(file);
                    return true;
            }
            return false;
        });
        icm.addItem(MenuID.MI_RELOAD_SPOILER, "reloadSpoiler");
        icm.addItem(MenuID.MI_START_PICTUREAPP, "startPictureApp", Sprites.getSprite("image-export"));

        return icm;
    }
}
