package CB_UI.GL_UI.Main.Actions;

import CB_Core.DAO.LogDAO;
import CB_Core.Database;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Views.LogView;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;
import java.util.Iterator;

import static CB_Core.Api.GroundspeakAPI.*;

public class CB_Action_LoadLogs extends CB_Action {

    private int ChangedCount = 0;
    private int result = 0;
    private boolean doCancelThread = false;
    private CancelWaitDialog pd;
    private boolean loadAllLogs;

    CB_Action_LoadLogs(boolean loadAllLogs) {
        super("LoadLogs", MenuID.AID_LOADLOGS);
        this.loadAllLogs = loadAllLogs;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.dayGcLiveIcon.name());
    }

    @Override
    public void Execute() {
        pd = CancelWaitDialog.ShowWait(Translation.get("LoadLogs"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

            @Override
            public void isCanceled() {
                doCancelThread = true;
            }
        }, new RunnableReadyHandler() {

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
                    /*
                     * // Reload result from DB synchronized (Database.Data.cacheList) { String sqlWhere =
                     * FilterInstances.LastFilter.getSqlWhere(Config.GcLogin.getValue()); CacheListDAO cacheListDAO = new CacheListDAO();
                     * cacheListDAO.ReadCacheList(Database.Data.cacheList, sqlWhere); }
                     *
                     * CachListChangedEventList.Call();
                     */
                    synchronized (Database.Data.cacheList) {
                        MessageBox.show(sCanceled + Translation.get("LogsLoaded") + " " + ChangedCount, Translation.get("LoadLogs"), MessageBoxIcon.None);
                    }

                }
            }
        });

    }

}
