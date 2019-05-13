package CB_UI.GL_UI.Main.Actions;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.CB_Core_Settings;
import CB_Core.DAO.LogDAO;
import CB_Core.Database;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.Views.LogView;
import CB_UI.GL_UI.Views.SpoilerView;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import static CB_Core.Api.GroundspeakAPI.*;

public class CB_Action_ShowLogView extends CB_Action_ShowView {

    private static CB_Action_ShowLogView that;
    private Menu contextMenu;
    private CancelWaitDialog pd;
    private int ChangedCount = 0;
    private int result = 0;
    private boolean doCancelThread = false;

    private CB_Action_ShowLogView() {
        super("ShowLogs", MenuID.AID_SHOW_LOGS);
        // createContextMenu(); todo see getContextMenu
    }

    public static CB_Action_ShowLogView getInstance() {
        if (that == null) that = new CB_Action_ShowLogView();
        return that;
    }

    @Override
    public void Execute() {
        GlobalCore.filterLogsOfFriends = false; // Reset Filter by Friends when opening LogView
        ViewManager.leftTab.ShowView(LogView.getInstance());
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
        // todo why are the clickhandlers of the items gone on following calls? temp solution createContextMenu() again
        // has to do with the disposing of the compoundMenu in CB_Button after the Show
        createContextMenu();
        return contextMenu;
    }

    private void createContextMenu() {
        contextMenu = new Menu("LogbookContextMenu");

        MenuItem mi;
        contextMenu.addMenuItem("ReloadLogs", Sprites.getSprite(IconName.downloadLogs.name()), () -> loadLogs(true));
        if (CB_Core_Settings.Friends.getValue().length() > 0) {
            contextMenu.addMenuItem("LoadLogsOfFriends", Sprites.getSprite(IconName.downloadFriendsLogs.name()), () -> loadLogs(false));
            mi = contextMenu.addMenuItem("FilterLogsOfFriends", Sprites.getSprite(IconName.friendsLogs.name()), () -> {
                GlobalCore.filterLogsOfFriends = !GlobalCore.filterLogsOfFriends;
                LogView.getInstance().resetInitial();
            });
            mi.setCheckable(true);
            mi.setChecked(GlobalCore.filterLogsOfFriends);
        }
        contextMenu.addMenuItem("ImportFriends", Sprites.getSprite(Sprites.IconName.friends.name()), this::getFriends);
        contextMenu.addMenuItem("LoadLogImages", Sprites.getSprite(IconName.downloadLogImages.name()), () -> GlobalCore.ImportSpoiler(true).setReadyListener(() -> {
            // do after import
            if (GlobalCore.isSetSelectedCache()) {
                GlobalCore.getSelectedCache().loadSpoilerRessources();
                SpoilerView.getInstance().ForceReload();
            }
        }));

    }

    private void loadLogs(boolean loadAllLogs) {
        GL.that.postAsync(() -> GlobalCore.chkAPiLogInWithWaitDialog(MemberType -> {
            TimerTask tt = new TimerTask() {

                @Override
                public void run() {
                    GL.that.postAsync(() -> pd = CancelWaitDialog.ShowWait(Translation.get("LoadLogs"), DownloadAnimation.GetINSTANCE(),
                            () -> doCancelThread = true, new RunnableReadyHandler() {

                                @Override
                                public boolean doCancel() {
                                    return doCancelThread;
                                }

                                @Override
                                public void run() {
                                    result = 0;
                                    doCancelThread = false;
                                    ArrayList<LogEntry> logList;

                                    try {
                                        Thread.sleep(10);
                                        logList = fetchGeoCacheLogs(GlobalCore.getSelectedCache(), loadAllLogs, this);
                                        if (result == ERROR) {
                                            GL.that.Toast(LastAPIError);
                                        }
                                        if (logList.size() > 0) {
                                            Database.Data.sql.beginTransaction();

                                            Iterator<LogEntry> iterator = logList.iterator();
                                            LogDAO dao = new LogDAO();
                                            if (loadAllLogs)
                                                dao.deleteLogs(GlobalCore.getSelectedCache().Id);
                                            do {
                                                ChangedCount++;
                                                try {
                                                    Thread.sleep(10);
                                                    LogEntry writeTmp = iterator.next();
                                                    dao.WriteToDatabase(writeTmp);
                                                } catch (InterruptedException e) {
                                                    doCancelThread = true;
                                                }
                                            } while (iterator.hasNext() && !doCancelThread);

                                            Database.Data.sql.setTransactionSuccessful();
                                            Database.Data.sql.endTransaction();

                                            LogView.getInstance().resetInitial();

                                        }

                                    } catch (InterruptedException e) {
                                        doCancelThread = true;
                                    }

                                }

                                @Override
                                public void RunnableIsReady(boolean canceled) {
                                    String sCanceled = canceled ? Translation.get("isCanceled") + GlobalCore.br : "";
                                    pd.close();
                                    if (result != -1) {
                                        synchronized (Database.Data.cacheList) {
                                            MessageBox.show(sCanceled + Translation.get("LogsLoaded") + " " + ChangedCount, Translation.get("LoadLogs"), MessageBoxIcon.None);
                                        }

                                    }
                                }
                            }));
                }
            };
            Timer t = new Timer();
            t.schedule(tt, 100);
        }));
    }

    private void getFriends() {
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
