package CB_UI.GL_UI.Main.Actions;

import CB_Core.CacheListChangedEventList;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheDAO;
import CB_Core.Types.CacheListDAO;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import CB_Utils.Events.ProgresssChangedEventList;
import CB_Utils.Log.Log;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;

import static CB_Core.Api.GroundspeakAPI.*;

public class Action_chkState extends AbstractAction {
    private static final String sKlasse = "Action_chkState";

    int ChangedCount = 0;
    int result = 0;
    private ProgressDialog pd;
    private boolean cancel = false;
    private final RunnableReadyHandler ChkStatRunnable = new RunnableReadyHandler() {
        final int BlockSize = 50; // API 1.0 has a limit of 50, handled in GroundspeakAPI but want to write to DB after Blocksize fetched

        @Override
        public void run() {
            cancel = false;
            ArrayList<Cache> chkList = new ArrayList<>();

            synchronized (Database.Data.cacheList) {
                if (Database.Data.cacheList == null || Database.Data.cacheList.size() == 0)
                    return;
                ChangedCount = 0;
                for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                    chkList.add(Database.Data.cacheList.get(i));
                }

            }
            float ProgressInkrement = 100.0f / (chkList.size() / BlockSize); // 100% durch Anzahl Schleifen

            // in Blöcke Teilen

            int skip = 0;

            result = 0;
            ArrayList<Cache> caches = new ArrayList<>();

            boolean cancelThread = false;

            float progress = 0;

            CacheDAO dao = new CacheDAO();
            do {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // thread abgebrochen
                    cancelThread = true;
                }

                caches.clear();
                if (!cancelThread) {

                    if (chkList == null || chkList.size() == 0) {
                        break;
                    }

                    for (int i = skip; i < skip + BlockSize && i < chkList.size(); i++) {
                        caches.add(chkList.get(i));
                    }
                    skip += BlockSize;

                    // Database.Data.beginTransaction();
                    for (GeoCacheRelated ci : updateStatusOfGeoCaches(caches)) {
                        if (dao.UpdateDatabaseCacheState(ci.cache))
                            ChangedCount++;
                    }
                    // Database.Data.setTransactionSuccessful();
                    // Database.Data.endTransaction();

                    if (APIError != OK) {
                        GL.that.Toast(LastAPIError);
                        break;
                    }
                }

                progress += ProgressInkrement;
                ProgresssChangedEventList.Call("", (int) progress);

            } while (skip < chkList.size() && !cancelThread);

            // dao = null;
            pd.close();

        }

        @Override
        public boolean doCancel() {
            Log.debug(sKlasse, "chkState canceled");
            return cancel;
        }

        @Override
        public void RunnableIsReady(boolean canceld) {
            Log.debug(sKlasse, "chkState ready");
            String sCanceld = canceld ? Translation.get("isCanceld") + GlobalCore.br : "";

            if (result != -1) {

                // Reload result from DB
                synchronized (Database.Data.cacheList) {
                    String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
                    CacheListDAO cacheListDAO = new CacheListDAO();
                    cacheListDAO.ReadCacheList(Database.Data.cacheList, sqlWhere, false, Config.ShowAllWaypoints.getValue());
                    cacheListDAO = null;
                }

                CacheListChangedEventList.Call();
                synchronized (Database.Data.cacheList) {
                    MessageBox.show(sCanceld + Translation.get("CachesUpdatet") + " " + ChangedCount + "/" + Database.Data.cacheList.size(), Translation.get("chkState"), MessageBoxIcon.None);
                }

            }
        }
    };

    public Action_chkState() {
        super("chkState", MenuID.AID_CHK_STATE);
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
        Log.debug(sKlasse, "Execute ProgressDialog");
        pd = ProgressDialog.Show(Translation.get("chkState"), DownloadAnimation.GetINSTANCE(), ChkStatRunnable);
    }
}
