package de.droidcachebox.menu.menuBtn1.contextmenus;

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;
import static de.droidcachebox.core.GroundspeakAPI.LastAPIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.updateStatusOfGeoCaches;
import static de.droidcachebox.settings.Config_Core.br;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ProgresssChangedEventList;
import de.droidcachebox.utils.RunnableReadyHandler;
import de.droidcachebox.utils.log.Log;

public class UpdateCachesState extends AbstractAction {
    private static final String sClass = "UpdateCachesState";

    private int ChangedCount = 0;
    private int result = 0;
    private ProgressDialog pd;
    private boolean isCanceled = false;

    private final RunnableReadyHandler updateStatusOfCaches = new RunnableReadyHandler() {
        final int BlockSize = 50; // API 1.0 has a limit of 50, handled in GroundSpeakAPI but want to write to DB after BlockSize fetched

        @Override
        public void run() {
            isCanceled = false;
            ArrayList<Cache> chkList = new ArrayList<>();

            synchronized (CBDB.getInstance().cacheList) {
                if (CBDB.getInstance().cacheList == null || CBDB.getInstance().cacheList.size() == 0)
                    return;
                ChangedCount = 0;
                for (int i = 0, n = CBDB.getInstance().cacheList.size(); i < n; i++) {
                    chkList.add(CBDB.getInstance().cacheList.get(i));
                }

            }
            float progressIncrement = 100.0f / ((float) chkList.size() / BlockSize); // 100% divided by by number of repeats

            // divide into blocks

            int skip = 0;

            result = 0;
            ArrayList<Cache> caches = new ArrayList<>();

            boolean cancelThread = false;

            float progress = 0;

            CacheDAO dao = CacheDAO.getInstance();
            do {
                if (Thread.interrupted())
                    cancelThread = true;

                caches.clear();
                if (!cancelThread) {

                    if (chkList.size() == 0) {
                        break;
                    }

                    for (int i = skip; i < skip + BlockSize && i < chkList.size(); i++) {
                        caches.add(chkList.get(i));
                    }
                    skip += BlockSize;

                    CBDB.getInstance().beginTransaction();
                    for (GeoCacheRelated ci : updateStatusOfGeoCaches(caches)) {
                        if (dao.updateDatabaseCacheState(ci.cache))
                            ChangedCount++;
                    }
                    CBDB.getInstance().setTransactionSuccessful();
                    CBDB.getInstance().endTransaction();

                    if (APIError != OK) {
                        GL.that.toast(LastAPIError);
                        break;
                    }
                }

                progress += progressIncrement;
                ProgresssChangedEventList.progressChanged("", (int) progress);

            } while (skip < chkList.size() && !cancelThread);

            // dao = null;
            pd.close();

        }

        @Override
        public boolean doCancel() {
            Log.debug(sClass, "chkState canceled");
            // misleading use of Interface ICancel, should be named like "isCanceled" from somewhere else. this is only the question if that did happen
            // seems that no one does that (is always false)
            return isCanceled;
        }

        @Override
        public void runnableIsReady(boolean canceled) {
            Log.debug(sClass, "chkState ready");
            String sCanceled = canceled ? Translation.get("isCanceld") + br : "";
            if (result != -1) {
                // Reload result from DB
                synchronized (CBDB.getInstance().cacheList) {
                    String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue());
                    CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Settings.showAllWaypoints.getValue());
                }
                CacheListChangedListeners.getInstance().cacheListChanged();
                synchronized (CBDB.getInstance().cacheList) {
                    MsgBox.show(sCanceled + Translation.get("CachesUpdated") + " " + ChangedCount + "/" + CBDB.getInstance().cacheList.size(),
                            Translation.get("chkState"),
                            MsgBoxIcon.None);
                }

            }
        }
    };

    UpdateCachesState() {
        super("chkState");
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
    public void execute() {
        Log.debug(sClass, "execute ProgressDialog");
        // todo set the ICancelListener of ProgressDialog by setCancelListener(...) else updateStatusOfCaches.run() can never be canceled
        pd = ProgressDialog.Show(Translation.get("chkState"), DownloadAnimation.GetINSTANCE(), updateStatusOfCaches);
    }
}
