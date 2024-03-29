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
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
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
            if (isAccessTokenInvalid) {
                return;
            }
            Log.debug("checkReady", "isAccessTokenInvalid: " + isAccessTokenInvalid);
            progressDialog = new ProgressDialog(Translation.get("chkState"), new DownloadAnimation(), new RunAndReady() {
                final static int blockSize = 50; // API 1.0 has a limit of 50, handled in GroundSpeakAPI but want to write to DB after BlockSize fetched
                final CachesDAO cachesDAO = new CachesDAO();

                @Override
                public void run() {
                    isCanceled.set(false);

                    ArrayList<Cache> chkList = new ArrayList<>();
                    synchronized (CBDB.cacheList) {
                        if (CBDB.cacheList.size() == 0)
                            return;
                        changedCount = 0;
                        for (int i = 0, n = CBDB.cacheList.size(); i < n; i++) {
                            chkList.add(CBDB.cacheList.get(i));
                        }
                    }
                    float progressIncrement = 100.0f / ((float) chkList.size() / blockSize); // 100% divided by number of blocks (repeats)
                    // divide into blocks
                    int skip = 0;
                    result = 0;
                    ArrayList<Cache> caches = new ArrayList<>();
                    float progress = 0;

                    do {
                        caches.clear();
                        if (chkList.size() == 0) break;
                        for (int i = skip; i < skip + blockSize && i < chkList.size(); i++) {
                            caches.add(chkList.get(i));
                        }
                        skip = skip + blockSize;
                        CBDB.getInstance().beginTransaction();
                        for (GeoCacheRelated ci : updateStatusOfGeoCaches(caches)) {
                            if (cachesDAO.updateDatabaseCacheState(ci.cache))
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

                }

                @Override
                public void ready() {
                    Log.debug(sClass, "chkState ready");
                    String sCanceled = isCanceled.get() ? Translation.get("isCanceld") + br : "";
                    if (result != -1) {
                        // Reload result from DB
                        synchronized (CBDB.cacheList) {
                            String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue());
                            cachesDAO.readCacheList(sqlWhere, false, false, Settings.showAllWaypoints.getValue());
                        }
                        CacheListChangedListeners.getInstance().fire(sClass);
                        synchronized (CBDB.cacheList) {
                            new ButtonDialog(sCanceled + Translation.get("CachesUpdated") + " " + changedCount + "/" + CBDB.cacheList.size(),
                                    Translation.get("chkState"), MsgBoxButton.OK, MsgBoxIcon.None).show();
                        }

                    }
                }

                @Override
                public void setIsCanceled() {
                    isCanceled.set(true);
                }

            });
            GL.that.postAsync(progressDialog::show);
        });
    }
}
