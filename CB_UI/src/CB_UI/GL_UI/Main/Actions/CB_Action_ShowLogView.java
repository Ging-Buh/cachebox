package CB_UI.GL_UI.Main.Actions;

import CB_Core.CB_Core_Settings;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.LogView;
import CB_UI.GlobalCore;
import CB_UI.GlobalCore.iChkReadyHandler;
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
        Menu cm = new Menu("LogListContextMenu");

        cm.addOnClickListener((v, x, y, pointer, button) -> {
            switch (((MenuItem) v).getMenuItemId()) {
                case MenuID.MI_LOAD_FRIENDS_LOGS:
                    reloadLogs(false);
                    return true;
                case MenuID.MI_RELOADLOGS:
                    reloadLogs(true);
                    return true;
                case MenuID.MI_FILTERLOGS:
                    GlobalCore.filterLogsOfFriends = !GlobalCore.filterLogsOfFriends;
                    LogView.getInstance().resetInitial();
                    break;
            }
            return false;
        });

        MenuItem mi;
        cm.addItem(MenuID.MI_RELOADLOGS, "ReloadLogs", Sprites.getSprite(IconName.importIcon.name()));
        if (CB_Core_Settings.Friends.getValue().length() > 0) {
            cm.addItem(MenuID.MI_LOAD_FRIENDS_LOGS, "LoadLogsOfFriends", Sprites.getSprite(IconName.importIcon.name()));
            mi = cm.addItem(MenuID.MI_FILTERLOGS, "FilterLogsOfFriends", Sprites.getSprite(IconName.filter.name()));
            mi.setCheckable(true);
            mi.setChecked(GlobalCore.filterLogsOfFriends);
        }
        return cm;
    }

    private void reloadLogs(final boolean all) {
        GL.that.postAsync(() -> {
            GlobalCore.chkAPiLogInWithWaitDialog(new iChkReadyHandler() {
                @Override
                public void checkReady(boolean MemberType) {
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
                }
            });
        });
    }

}
