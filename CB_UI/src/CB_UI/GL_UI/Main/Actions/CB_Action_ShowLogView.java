package CB_UI.GL_UI.Main.Actions;

import CB_Core.CB_Core_Settings;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.LogView;
import CB_UI.GL_UI.Views.SpoilerView;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Timer;
import java.util.TimerTask;

public class CB_Action_ShowLogView extends CB_Action_ShowView {

    private static CB_Action_ShowLogView that;

    private CB_Action_ShowLogView() {
        super("ShowLogs", MenuID.AID_SHOW_LOGS);
        createContextMenu();
    }

    public static CB_Action_ShowLogView getInstance() {
        if (that == null) that = new CB_Action_ShowLogView();
        return that;
    }

    @Override
    public void Execute() {
        GlobalCore.filterLogsOfFriends = false; // Reset Filter by Friends when opening LogView
        TabMainView.leftTab.ShowView(LogView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.listIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return LogView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        // if depends on something: call createContextMenu() again
        return contextMenu;
    }

    private Menu contextMenu;
    private void createContextMenu() {
        contextMenu = new Menu("LogbookContextMenu");

        MenuItem mi;
        contextMenu.addMenuItem("ReloadLogs", Sprites.getSprite(IconName.downloadLogs.name()), (v, x, y, pointer, button) -> {
            reloadLogs(true);
            return true;
        });
        if (CB_Core_Settings.Friends.getValue().length() > 0) {
            contextMenu.addMenuItem("LoadLogsOfFriends", Sprites.getSprite(IconName.downloadFriendsLogs.name()), (v, x, y, pointer, button) -> {
                reloadLogs(false);
                return true;
            });
            mi = contextMenu.addMenuItem("FilterLogsOfFriends", Sprites.getSprite(IconName.friendsLogs.name()), (v, x, y, pointer, button) -> {
                GlobalCore.filterLogsOfFriends = !GlobalCore.filterLogsOfFriends;
                LogView.getInstance().resetInitial();
                return true;
            });
            mi.setCheckable(true);
            mi.setChecked(GlobalCore.filterLogsOfFriends);
        }
        contextMenu.addMenuItem("LoadLogImages", Sprites.getSprite(IconName.downloadLogImages.name()), (v, x, y, pointer, button) -> {
            GL.that.closeDialog(contextMenu);
            GlobalCore.ImportSpoiler(true).setReadyListener(() -> {
                // do after import
                if (GlobalCore.isSetSelectedCache()) {
                    GlobalCore.getSelectedCache().loadSpoilerRessources();
                    SpoilerView.getInstance().ForceReload();
                }
            });
            return true;
        });

    }

    private void reloadLogs(final boolean all) {
        GL.that.postAsync(() -> GlobalCore.chkAPiLogInWithWaitDialog(MemberType -> {
            TimerTask tt = new TimerTask() {

                @Override
                public void run() {
                    GL.that.postAsync(() -> {
                        if (all) {
                            new CB_Action_LoadLogs(true).Execute();
                        } else {
                            new CB_Action_LoadLogs(false).Execute();
                        }
                    });
                }
            };
            Timer t = new Timer();
            t.schedule(tt, 100);
        }));
    }

}
