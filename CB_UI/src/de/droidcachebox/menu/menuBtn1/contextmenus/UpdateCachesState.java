package de.droidcachebox.menu.menuBtn1.contextmenus;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.database.Database;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ProgresssChangedEventList;
import de.droidcachebox.utils.RunnableReadyHandler;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;

import static de.droidcachebox.core.GroundspeakAPI.*;

public class UpdateCachesState extends AbstractAction {
    private static final String sKlasse = "UpdateCachesState";

    private int ChangedCount = 0;
    private int result = 0;
    private ProgressDialog pd;
    private boolean isCanceled = false;

    private final RunnableReadyHandler updateStatusOfCaches = new RunnableReadyHandler() {
        final int BlockSize = 50; // API 1.0 has a limit of 50, handled in GroundspeakAPI but want to write to DB after Blocksize fetched

        @Override
        public void run() {
            isCanceled = false;
            ArrayList<Cache> chkList = new ArrayList<>();

            synchronized (Database.Data.cacheList) {
                if (Database.Data.cacheList == null || Database.Data.cacheList.size() == 0)
                    return;
                ChangedCount = 0;
                for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                    chkList.add(Database.Data.cacheList.get(i));
                }

            }
            float ProgressInkrement = 100.0f / ((float) chkList.size() / BlockSize); // 100% durch Anzahl Schleifen

            // in Blöcke Teilen

            int skip = 0;

            result = 0;
            ArrayList<Cache> caches = new ArrayList<>();

            boolean cancelThread = false;

            float progress = 0;

            CacheDAO dao = new CacheDAO();
            do {
                /*
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // thread abgebrochen
                    cancelThread = true;
                }

                 */
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

                    /* */
                    Database.Data.sql.beginTransaction();
                    for (GeoCacheRelated ci : updateStatusOfGeoCaches(caches)) {
                        if (dao.UpdateDatabaseCacheState(ci.cache))
                            ChangedCount++;
                    }
                    Database.Data.sql.setTransactionSuccessful();
                    Database.Data.sql.endTransaction();
                    /* */

                    /*
                    // a test for mass - uploading local notes and corrected coords (does not affect API-Limits)
                    for (Cache aktCache : caches) {
                        String uploadText = Database.getNote(aktCache);
                        boolean perhapsUploadedSomething = false;
                        if (!StringH.isEmpty(uploadText)) {
                            uploadText = uploadText.replace("<Import from Geocaching.com>", "").replace("</Import from Geocaching.com>", "").trim();
                            GroundspeakAPI.uploadCacheNote(aktCache.getGcCode(), uploadText);
                            perhapsUploadedSomething = true;
                        }
                        if (aktCache.hasCorrectedCoordiantesOrHasCorrectedFinal()) {
                            if (aktCache.hasCorrectedCoordinates()) {
                                GroundspeakAPI.uploadCorrectedCoordinates(aktCache.getGcCode(), aktCache.Pos);
                                perhapsUploadedSomething = true;
                            } else {
                                Waypoint correctedFinal = aktCache.getCorrectedFinal();
                                GroundspeakAPI.uploadCorrectedCoordinates(aktCache.getGcCode(), correctedFinal.Pos);
                                perhapsUploadedSomething = true;
                            }
                        }
                        if (perhapsUploadedSomething) {
                            if (GroundspeakAPI.APIError == 0) {
                                ChangedCount++;
                                aktCache.setFavorite(true);
                                // MessageBox.show(Translation.get("ok"), Translation.get("UploadCorrectedCoordinates"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                            } else {
                                MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("UploadCorrectedCoordinates"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                                return;
                            }
                        }
                    }
                     */


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
            // misleading use of Interface ICancel, should be named like "isCanceled" from somewhere else. this is only the question if that did happen
            // seems that noone does that (is always false)
            return isCanceled;
        }

        @Override
        public void runnableIsReady(boolean canceled) {
            Log.debug(sKlasse, "chkState ready");
            String sCanceld = canceled ? Translation.get("isCanceld") + GlobalCore.br : "";
            if (result != -1) {
                // Reload result from DB
                synchronized (Database.Data.cacheList) {
                    String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
                    Database.Data.cacheList = CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Config.showAllWaypoints.getValue());
                }
                CacheListChangedListeners.getInstance().cacheListChanged();
                synchronized (Database.Data.cacheList) {
                    MessageBox.show(sCanceld + Translation.get("CachesUpdated") + " " + ChangedCount + "/" + Database.Data.cacheList.size(),
                            Translation.get("chkState"),
                            MessageBoxIcon.None);
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
        Log.debug(sKlasse, "execute ProgressDialog");
        // todo set the ICancelListener of ProgressDialog by setCancelListener(...) else updateStatusOfCaches.run() can never be canceled
        pd = ProgressDialog.Show(Translation.get("chkState"), DownloadAnimation.GetINSTANCE(), updateStatusOfCaches);
    }
}
