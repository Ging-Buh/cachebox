/*
 * Copyright (C) 2014-2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.droidcachebox.core.API_ErrorEventHandlerList;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheList;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.DisplayType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.solver.Solver;
import de.droidcachebox.solver.SolverCacheInterface;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.log.Log;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static de.droidcachebox.core.API_ErrorEventHandlerList.handleApiKeyError;
import static de.droidcachebox.core.GroundspeakAPI.isAccessTokenInvalid;
import static de.droidcachebox.utils.Config_Core.br;

/**
 * @author ging-buh
 * @author arbor95
 * @author longri
 */
public class GlobalCore implements SolverCacheInterface {
    public static final String aboutMsg1 = "Team Cachebox (2011-2020)" + br;
    public static final String teamLink = "www.team-cachebox.de";
    public static final String aboutMsg2 = br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission";
    public static final String aboutMsg = aboutMsg1 + teamLink + aboutMsg2;
    public static final String splashMsg = aboutMsg + br + br + "POWERED BY:";
    private static final String CurrentVersion = "2.0.";
    private static final String log = "GlobalCore";
    public static DisplayType displayType = DisplayType.Normal;
    public static boolean restartAfterKill = false;
    public static String restartCache;
    public static String restartWayPoint;
    public static boolean filterLogsOfFriends = false;
    public static Track currentRoute = null;
    public static int currentRouteCount = 0;
    public static boolean switchToCompassCompleted = false;
    public static GlobalLocationReceiver receiver;
    public static boolean RunFromSplash = false;
    public static String firstSDCard, secondSDCard;
    private static GlobalCore globalCore;
    private static Cache selectedCache = null;
    private static boolean autoResort;
    private static Cache nearestCache = null;
    private static Waypoint selectedWayPoint = null;
    private int CurrentRevision;
    private String VersionPrefix;

    private GlobalCore() {
        super();
        Solver.solverCacheInterface = this;
        globalCore = this;
    }

    public static GlobalCore getInstance() {
        if (globalCore == null) {
            globalCore = new GlobalCore();
            globalCore.initVersionInfos();
        }
        return globalCore;
    }

    public static Cache getSelectedCache() {
        return selectedCache;
    }

    public static void setSelectedCache(Cache cache) {
        setSelectedWaypoint(cache, null);

    }

    public static boolean selectedCachehasSpoiler() {
        if (selectedCache != null) {
            return selectedCache.hasSpoiler();
        } else
            return false;
    }

    static Cache getNearestCache() {
        return nearestCache;
    }

    public static void setNearestCache(Cache Cache) {
        nearestCache = Cache;
    }

    public static void setSelectedWaypoint(Cache cache, Waypoint waypoint) {
        if (cache == null || cache.isDisposed())
            return;
        setSelectedWaypoint(cache, waypoint, true);
        if (!CoreData.cacheHistory.startsWith(cache.getGeoCacheCode())) {
            CoreData.cacheHistory = cache.getGeoCacheCode() + (CoreData.cacheHistory.length() > 0 ? "," : "") + CoreData.cacheHistory.replace("," + cache.getGeoCacheCode(), "");
            if (CoreData.cacheHistory.length() > 120) {
                CoreData.cacheHistory = CoreData.cacheHistory.substring(0, CoreData.cacheHistory.lastIndexOf(","));
            }
        }
    }

    /**
     * if changeAutoResort == false -> do not change state of autoResort Flag
     *
     * @param cache            cache
     * @param waypoint         wapoint
     * @param changeAutoResort changeAutoResort
     */
    public static void setSelectedWaypoint(Cache cache, Waypoint waypoint, boolean changeAutoResort) {

        if (cache == null) {
            selectedCache = null;
            selectedWayPoint = null;
            return;
        }

        // remove Detail Info from old selectedCache
        if ((selectedCache != cache) && (selectedCache != null) && (selectedCache.getGeoCacheDetail() != null)) {
            selectedCache.deleteDetail(Config.showAllWaypoints.getValue());
        }
        selectedCache = cache;
        Log.info(log, "[GlobalCore]setSelectedWaypoint: cache=" + cache.getGeoCacheCode());
        selectedWayPoint = waypoint;

        // load Detail Info if not available
        if (selectedCache.getGeoCacheDetail() == null) {
            selectedCache.loadDetail();
        }

        CacheSelectionChangedListeners.getInstance().fireEvent(selectedCache, selectedWayPoint);

        if (changeAutoResort) {
            // switch off auto select
            GlobalCore.setAutoResort(false);
        }

        GL.that.renderOnce();
    }

    public static Waypoint getSelectedWayPoint() {
        return selectedWayPoint;
    }

    public static Coordinate getSelectedCoordinate() {
        Coordinate ret = null;
        if (selectedWayPoint != null) {
            ret = selectedWayPoint.getCoordinate();
        } else if (selectedCache != null) {
            ret = selectedCache.getCoordinate();
        }
        return ret;
    }

    public static void checkSelectedCacheValid() {

        CacheList List = Database.Data.cacheList;

        // Prüfen, ob der SelectedCache noch in der cacheList drin ist.
        if ((List.size() > 0) && (GlobalCore.isSetSelectedCache()) && (List.getCacheByIdFromCacheList(GlobalCore.getSelectedCache().generatedId) == null)) {
            // der SelectedCache ist nicht mehr in der cacheList drin -> einen beliebigen aus der CacheList auswählen
            Log.debug(log, "Change SelectedCache from " + GlobalCore.getSelectedCache().getGeoCacheCode() + "to" + List.get(0).getGeoCacheCode());
            GlobalCore.setSelectedCache(List.get(0));
        }
        // Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
        if ((GlobalCore.getSelectedCache() == null) && (List.size() > 0)) {
            GlobalCore.setSelectedCache(List.get(0));
            Log.debug(log, "Set SelectedCache to " + List.get(0).getGeoCacheCode() + " first in List.");
        }
    }

    public static boolean getAutoResort() {
        return autoResort;
    }

    public static void setAutoResort(boolean value) {
        GlobalCore.autoResort = value;
    }

    public static void MsgDownloadLimit() {
        GL.that.RunOnGLWithThreadCheck(() -> MessageBox.show(Translation.get("Limit_msg"), Translation.get("Limit_title"), MessageBoxButton.OK, MessageBoxIcon.GC_Live, null));
    }

    public static void chkAPiLogInWithWaitDialog(final iChkReadyHandler handler) {

        // Live API
        if (GroundspeakAPI.isDownloadLimitExceeded()) {
            MsgDownloadLimit();
            return;
        }

        if (isAccessTokenInvalid()) {
            CancelWaitDialog.ShowWait("chk API Key", DownloadAnimation.GetINSTANCE(), null, new ICancelRunnable() {
                @Override
                public void run() {
                    handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
                    // check after some time, if the AccessToken is now available (is got)
                    Timer ti = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            handler.checkReady(isAccessTokenInvalid());
                        }
                    };
                    ti.schedule(task, 300);
                }

                @Override
                public boolean doCancel() {
                    return false;
                }
            });
        } else {
            handler.checkReady(false);
        }

    }

    /**
     * Returns true, if a Cache selected and this Cache object is valid.
     *
     * @return see above
     */
    public static boolean isSetSelectedCache() {
        if (selectedCache == null)
            return false;

        return selectedCache.getGeoCacheCode().length() != 0;
    }

    private void initVersionInfos() {
        String info;
        try {
            // GDX works with Android
            FileHandle fileHandle = Gdx.files.internal("build.info");
            info = fileHandle.readString("utf-8");
        } catch (Exception ex) {
            // but on Desktop class GDX is not loaded yet
            AbstractFile abstractFile = FileFactory.createFile("build.info");
            try {
                BufferedReader br = new BufferedReader(abstractFile.getFileReader());
                info = br.readLine();
            } catch (Exception ex1) {
                // if nothing works
                info = "#1000#master#687f624ef#2000-01-01";
                Log.err(log, "initVersionInfos " + ex.getLocalizedMessage());
            }
        }
        try {
            String[] sections = info.split("#");
            VersionPrefix = sections[1];
            String dat = sections[4];
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dat);
            CurrentRevision = Integer.decode((new SimpleDateFormat("yyyyMMdd", Locale.US)).format(d));
        } catch (Exception ex) {
            // for parsing of date gives an error
            VersionPrefix = "1000";
            CurrentRevision = 0;
        }
    }

    public String getVersionString() {
        return "Version: " + CurrentVersion + CurrentRevision + "  (" + VersionPrefix + ")";
    }

    public Integer getCurrentRevision() {
        return CurrentRevision;
    }

    // Interface für den Solver zum Zugriff auf den SelectedCache.
    // Direkter Zugriff geht nicht da der Solver im Core definiert ist
    @Override
    public Cache sciGetSelectedCache() {
        return getSelectedCache();
    }

    @Override
    public void sciSetSelectedCache(Cache cache) {
        setSelectedCache(cache);
        CacheListChangedListeners.getInstance().cacheListChanged();
    }

    @Override
    public void sciSetSelectedWaypoint(Cache cache, Waypoint waypoint) {
        setSelectedWaypoint(cache, waypoint);
        CacheListChangedListeners.getInstance().cacheListChanged();
    }

    @Override
    public Waypoint sciGetSelectedWaypoint() {
        return getSelectedWayPoint();
    }

    public interface iChkReadyHandler {
        void checkReady(boolean invalidAccessToken);
    }

}
