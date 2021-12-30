package de.droidcachebox.menu.menuBtn1.contextmenus.executes;

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;
import static de.droidcachebox.core.GroundspeakAPI.LastAPIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.updateStatusOfGeoCaches;
import static de.droidcachebox.settings.Config_Core.br;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.RunAndReady;
import de.droidcachebox.utils.log.Log;

public class UpdateCachesState {
    private static final String sClass = "UpdateCachesState";
    private final AtomicBoolean isCanceled;
    private int changedCount;
    private int result = 0;
    private ProgressDialog progressDialog;

    public UpdateCachesState() {
        isCanceled = new AtomicBoolean();
    }

    public void execute() {
        Log.debug("ImportMenuTitle", "chkAPiLogInWithWaitDialog");
        GlobalCore.chkAPiLogInWithWaitDialog(isAccessTokenInvalid -> {
            Log.debug("checkReady", "isAccessTokenInvalid: " + isAccessTokenInvalid);
            progressDialog = new ProgressDialog(Translation.get("chkState"), new DownloadAnimation(), new RunAndReady() {
                final static int blockSize = 50; // API 1.0 has a limit of 50, handled in GroundSpeakAPI but want to write to DB after BlockSize fetched

                @Override
                public void run() {
                    isCanceled.set(false);

                    ArrayList<Cache> chkList = new ArrayList<>();
                    synchronized (CBDB.getInstance().cacheList) {
                        if (CBDB.getInstance().cacheList == null || CBDB.getInstance().cacheList.size() == 0)
                            return;
                        changedCount = 0;
                        for (int i = 0, n = CBDB.getInstance().cacheList.size(); i < n; i++) {
                            chkList.add(CBDB.getInstance().cacheList.get(i));
                        }
                    }
                    float progressIncrement = 100.0f / ((float) chkList.size() / blockSize); // 100% divided by number of blocks (repeats)
                    // divide into blocks
                    int skip = 0;
                    result = 0;
                    ArrayList<Cache> caches = new ArrayList<>();
                    float progress = 0;
                    CacheDAO dao = CacheDAO.getInstance();

                    do {
                        caches.clear();
                        if (chkList.size() == 0) break;
                        for (int i = skip; i < skip + blockSize && i < chkList.size(); i++) {
                            caches.add(chkList.get(i));
                        }
                        skip = skip + blockSize;
                        CBDB.getInstance().beginTransaction();
                        for (GeoCacheRelated ci : updateStatusOfGeoCaches(caches)) {
                            if (dao.updateDatabaseCacheState(ci.cache))
                                changedCount++;
                        }
                        CBDB.getInstance().setTransactionSuccessful();
                        CBDB.getInstance().endTransaction();
                        if (APIError != OK) {
                            GL.that.toast(LastAPIError);
                            break;
                        }

                        progress = progress + progressIncrement;
                        progressDialog.setProgress("", "", (int) progress);

                    } while (skip < chkList.size() && !isCanceled.get());

                    progressDialog.close();

                }

                @Override
                public void ready(boolean canceled) {
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
                            MsgBox.show(sCanceled + Translation.get("CachesUpdated") + " " + changedCount + "/" + CBDB.getInstance().cacheList.size(),
                                    Translation.get("chkState"),
                                    MsgBoxIcon.None);
                        }

                    }
                }

                @Override
                public void setIsCanceled() {
                    isCanceled.set(true);
                }

            });

            if (!isAccessTokenInvalid) {
                GL.that.postAsync(() -> GL.that.showDialog(progressDialog));
            }
        });
    }
}
