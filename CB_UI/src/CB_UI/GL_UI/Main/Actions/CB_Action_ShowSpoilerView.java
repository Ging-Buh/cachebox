package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.SpoilerView;
import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSpoilerView extends CB_Action_ShowView {
    private static CB_Action_ShowSpoilerView that;
    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
    private int spoilerState = -1;
    private Sprite SpoilerIcon;
    private Menu contextMenu;

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
        // if depends on something: call createContextMenu() again
        // todo why are the clickhandlers of the items gone on following calls? temp solution createContextMenu() again
        // has to do with the disposing of the compoundMenu in CB_Button after the Show
        createContextMenu();
        return contextMenu;
    }

    private void createContextMenu() {
        contextMenu = new Menu("SpoilerContextMenu");

        contextMenu.addMenuItem("reloadSpoiler", null, (v, x, y, pointer, button) -> {
            GlobalCore.ImportSpoiler(false).setReadyListener(() -> {
                // do after import
                if (GlobalCore.isSetSelectedCache()) {
                    GlobalCore.getSelectedCache().loadSpoilerRessources();
                    SpoilerView.getInstance().ForceReload();
                    TabMainView.leftTab.ShowView(SpoilerView.getInstance());
                    SpoilerView.getInstance().onShow();
                }
            });
            return true;
        });

        contextMenu.addMenuItem("LoadLogImages", Sprites.getSprite(IconName.downloadLogImages.name()), (v, x, y, pointer, button) -> {
            GlobalCore.ImportSpoiler(true).setReadyListener(() -> {
                // do after import
                if (GlobalCore.isSetSelectedCache()) {
                    GlobalCore.getSelectedCache().loadSpoilerRessources();
                    SpoilerView.getInstance().ForceReload();
                    TabMainView.leftTab.ShowView(SpoilerView.getInstance());
                    SpoilerView.getInstance().onShow();
                }
            });
            return true;
        });

        contextMenu.addMenuItem("startPictureApp", Sprites.getSprite("image-export"), (v, x, y, pointer, button) -> {
            String file = SpoilerView.getInstance().getSelectedFilePath();
            if (file == null)
                return true;
            PlatformConnector.StartPictureApp(file);
            return true;
        });
    }

}
