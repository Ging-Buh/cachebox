package de.droidcachebox.menu.menuBtn1.contextmenus.executes;

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;
import static de.droidcachebox.core.GroundspeakAPI.LastAPIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.isAccessTokenInvalid;
import static de.droidcachebox.core.GroundspeakAPI.updateStatusOfGeoCaches;
import static de.droidcachebox.settings.Config_Core.br;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import de.droidcachebox.settings.AllSettings;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.log.Log;

public class UpdateCachesState {
    private static final String sClass = "UpdateCachesState";
    private final AtomicBoolean isCanceled;
    private int changedCount;
    private int result = 0;
    private ProgressDialog progressDialog;
    private int chkListLen = 0;

    public UpdateCachesState() {
        isCanceled = new AtomicBoolean();
    }

    public void execute() {
        Log.debug("ImportMenuTitle", "chkAPiLogInWithWaitDialog");
        //GlobalCore.chkAPiLogInWithWaitDialog(isAccessTokenInvalid ->
        {
            final boolean isAccessTokenInvalid = isAccessTokenInvalid();
//            if (isAccessTokenInvalid) {
//                return;
//            }
            Log.debug("checkReady", "isAccessTokenInvalid: " + isAccessTokenInvalid);
            progressDialog = new ProgressDialog(Translation.get("chkState"), new DownloadAnimation(), new RunAndReady() {
                final static int blockSize = 50; // API 1.0 has a limit of 50, handled in GroundSpeakAPI but want to write to DB after BlockSize fetched
                final static int blockDownSize = 3; // Get response after 3 dowwnloads
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
                    int blocks = (isAccessTokenInvalid ? blockDownSize : blockSize);
                    chkListLen = chkList.size();
                    float progressIncrement = 100.0f / ((float) chkListLen / blocks); // 100% divided by number of blocks (repeats)
                    // divide into blocks

                    progressDialog.setProgress("", "0/" + chkListLen, 0);

                    int skip = 0;
                    result = 0;
                    ArrayList<Cache> caches = new ArrayList<>();
                    float progress = 0;

                    do {
                        caches.clear();
                        if (chkListLen == 0) break;
                        for (int i = skip; i < skip + blocks && i < chkListLen; i++) {
                            caches.add(chkList.get(i));
                        }
                        skip = skip + blocks;
                        CBDB.getInstance().beginTransaction();
                        if (!isAccessTokenInvalid) {
                            for (GeoCacheRelated ci : updateStatusOfGeoCaches(caches)) {
                                if (cachesDAO.updateDatabaseCacheState(ci.cache))
                                    changedCount++;
                            }
                        } else {
                            for (Cache cache : caches) {
                                if (cache.mustLoadDetail())
                                    new CachesDAO().loadDetail(cache);
                                Download dl = new Download(null, null);
                                String destFileName = AllSettings.UserImageFolder.getValue() + "/temp.html";
                                if (dl.download(cache.getUrl(), destFileName)) {
                                    // Parse temp.html
                                    // - search for alert-info
                                    // - if not found: cache is available
                                    // - if found: disabledMessage or archivedMessage? -> set unavailable or archived
                                    BufferedReader br;
                                    String strLine;
                                    boolean isAvailable = true;
                                    boolean isArchived = false;
                                    AbstractFile fileToLoad = FileFactory.createFile(destFileName);
                                    try {
                                        br = new BufferedReader(new InputStreamReader(fileToLoad.getFileInputStream()));
                                        while ((strLine = br.readLine()) != null && isAvailable && !isArchived) {
                                            if (strLine.contains("alert-info")) {
                                                if (strLine.contains("archivedMessage")) {
                                                    isArchived = true;
                                                }
                                                else {
                                                    isAvailable = false;
                                                }
                                            }
                                            // Premium cache contains no alert-info.
                                            // Only the icon is disabled if it is disabled or archived
                                            if (strLine.contains("/app/ui-icons/sprites/cache-types.svg#icon") && strLine.contains("disabled")) {
                                                isAvailable = false;
                                            }
                                        }
                                    } catch (IOException e1) {
                                        Log.err(sClass, e1.getLocalizedMessage(), e1);
                                    }
                                    cache.setAvailable(isAvailable);
                                    cache.setArchived(isArchived);
                                    if (cachesDAO.updateDatabaseCacheState(cache))
                                        changedCount++;

                                    try {
                                        fileToLoad.delete();
                                    } catch (IOException e) {
                                        Log.err(sClass, e.getLocalizedMessage(), e);
                                    }
                                }
                            }
                        }
                        CBDB.getInstance().setTransactionSuccessful();
                        CBDB.getInstance().endTransaction();
                        if (!isAccessTokenInvalid && APIError != OK) {
                            GL.that.toast(LastAPIError);
                            break;
                        }

                        progress = progress + progressIncrement;
                        progressDialog.setProgress("", skip + "/" + chkListLen, (int) progress);

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
                            new ButtonDialog(sCanceled + Translation.get("CachesUpdated") + " " + changedCount + "/" + chkListLen,
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
        };
    }
}
