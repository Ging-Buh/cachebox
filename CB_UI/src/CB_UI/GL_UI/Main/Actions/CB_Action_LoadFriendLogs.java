package CB_UI.GL_UI.Main.Actions;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.LogDAO;
import CB_Core.Database;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI.GL_UI.Views.LogView;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;
import java.util.Iterator;

public class CB_Action_LoadFriendLogs extends CB_Action {

    int ChangedCount = 0;
    int result = 0;
    boolean cancelThread = false;
    private CancelWaitDialog pd;
    private final RunnableReadyHandler ChkStatRunnable = new RunnableReadyHandler() {

        @Override
        public boolean cancel() {
            return cancelThread;
        }

        @Override
        public void run() {
            result = 0;
            cancelThread = false;
            ArrayList<LogEntry> logList = new ArrayList<LogEntry>();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // thread abgebrochen
                cancelThread = true;
            }
            if (!cancelThread) {
                logList.clear();
                result = GroundspeakAPI.fetchGeocacheLogsByCache(GlobalCore.getSelectedCache(), logList, false, this);
                if (result == GroundspeakAPI.ERROR) // Error
                    GL.that.Toast(ConnectionError.INSTANCE);
            }

            if ((result == 0) && (!cancelThread) && (logList.size() > 0)) {
                Database.Data.beginTransaction();

                Iterator<LogEntry> iterator = logList.iterator();
                LogDAO dao = new LogDAO();
                do {
                    ChangedCount++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        cancelThread = true;
                    }
                    LogEntry writeTmp = iterator.next();
                    dao.WriteToDatabase(writeTmp);
                } while (iterator.hasNext() && !cancelThread);

                Database.Data.setTransactionSuccessful();
                Database.Data.endTransaction();
                if (LogView.that != null) {
                    LogView.that.resetInitial();
                }

            }
        }

        @Override
        public void RunnableReady(boolean canceld) {
            String sCanceld = canceld ? Translation.Get("isCanceld") + GlobalCore.br : "";
            pd.close();
            if (result != -1) {
                /*
                 * // Reload result from DB synchronized (Database.Data.Query) { String sqlWhere =
                 * FilterInstances.LastFilter.getSqlWhere(Config.GcLogin.getValue()); CacheListDAO cacheListDAO = new CacheListDAO();
                 * cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere); }
                 *
                 * CachListChangedEventList.Call();
                 */
                synchronized (Database.Data.Query) {
                    GL_MsgBox.Show(sCanceld + Translation.Get("LogsLoaded") + " " + ChangedCount, Translation.Get("LoadLogs"), MessageBoxIcon.None);
                }

            }
        }
    };

    public CB_Action_LoadFriendLogs() {
        super("LoadLogs", MenuID.AID_LOADLOGS);

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
        pd = CancelWaitDialog.ShowWait(Translation.Get("LoadLogs"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

            @Override
            public void isCanceled() {
                cancelThread = true;
            }
        }, ChkStatRunnable);

    }

}
