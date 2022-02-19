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

import static de.droidcachebox.core.API_ErrorEventHandlerList.handleApiKeyError;
import static de.droidcachebox.core.GroundspeakAPI.isAccessTokenInvalid;
import static de.droidcachebox.settings.Config_Core.br;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.core.API_ErrorEventHandlerList;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.DisplayType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.solver.SolverCacheInterface;
import de.droidcachebox.solver.SolverLines;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.log.Log;

public class GlobalCore implements SolverCacheInterface {
    public static final String aboutMsg1 = "Team Cachebox (2011-2022)" + br;
    public static final String teamLink = "www.team-cachebox.de";
    public static final String aboutMsg2 = br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission";
    public static final String aboutMsg = aboutMsg1 + teamLink + aboutMsg2;
    public static final String splashMsg = aboutMsg + br + br + "POWERED BY:";
    private static final String currentVersion = "2.0.";
    private static final String sClass = "GlobalCore";
    public static String workPath = "";
    public static DisplayType displayType = DisplayType.Normal;
    public static boolean restartAfterKill = false;
    public static String restartCache;
    public static String restartWayPoint;
    public static boolean filterLogsOfFriends = false;
    public static boolean switchToCompassCompleted = false;
    public static GlobalLocationReceiver receiver;
    public static boolean RunFromSplash = false;
    public static String firstSDCard, secondSDCard;
    private static GlobalCore instance;
    private static Cache selectedCache = null;
    private static boolean autoResort;
    private static Cache nearestCache = null;
    private static Waypoint selectedWayPoint = null;
    private static int currentRevision;
    private static String VersionPrefix;

    private GlobalCore() {
        super();
        SolverLines.solverCacheInterface = this;
        instance = this;
    }

    public static GlobalCore getInstance() {
        if (instance == null) {
            instance = new GlobalCore();
            instance.initVersionInfos();
        }
        return instance;
    }

    public static Cache getSelectedCache() {
        return selectedCache;
    }

    public static void setSelectedCache(Cache cache) {
        setSelectedWaypoint(cache, null);
    }

    public static boolean selectedCacheHasSpoiler() {
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
        setSelectedWaypoint(cache, waypoint, true);
        if (cache != null) {
            if (!CoreData.cacheHistory.startsWith(cache.getGeoCacheCode())) {
                CoreData.cacheHistory = cache.getGeoCacheCode() + (CoreData.cacheHistory.length() > 0 ? "," : "") + CoreData.cacheHistory.replace("," + cache.getGeoCacheCode(), "");
                if (CoreData.cacheHistory.length() > 120) {
                    CoreData.cacheHistory = CoreData.cacheHistory.substring(0, CoreData.cacheHistory.lastIndexOf(","));
                }
            }
        }
    }

    public static void setSelectedWaypoint(Cache cache, Waypoint waypoint, boolean unsetAutoResort) {

        if (cache == null) {
            selectedCache = null;
            selectedWayPoint = null;
            CacheSelectionChangedListeners.getInstance().fire(null, null);
        } else {

            // remove Detail Info from old selectedCache
            if ((selectedCache != cache) && (selectedCache != null) && (selectedCache.getGeoCacheDetail() != null)) {
                Log.debug(sClass, "[GlobalCore]setSelectedWaypoint: deleteDetail " + selectedCache.getGeoCacheCode());
                selectedCache.deleteDetail(Settings.showAllWaypoints.getValue());
            }

            selectedCache = cache;
            Log.debug(sClass, "[GlobalCore]setSelectedWaypoint: cache=" + cache.getGeoCacheCode());
            selectedWayPoint = waypoint;

            // load Detail Info if not available
            if (selectedCache.getGeoCacheDetail() == null) {
                Log.debug(sClass, "[GlobalCore]setSelectedWaypoint: loadDetail of " + cache.getGeoCacheCode());
                new CachesDAO().loadDetail(selectedCache);
            }

            CacheSelectionChangedListeners.getInstance().fire(selectedCache, selectedWayPoint);

            if (unsetAutoResort) {
                setAutoResort(false);
            }
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
        // Prüfen, ob der SelectedCache noch in der cacheList drin ist.
        if (CBDB.cacheList.size() > 0 ) {
            if (GlobalCore.isSetSelectedCache()) {
                if (CBDB.cacheList.getCacheByIdFromCacheList(GlobalCore.getSelectedCache().generatedId) == null) {
                    // selected geocache is not in cacheList
                    GlobalCore.setSelectedCache(CBDB.cacheList.get(0));
                }
            }
            else {
                GlobalCore.setSelectedCache(CBDB.cacheList.get(0));
            }
        }
        else {
            if (GlobalCore.getSelectedCache() != null )GlobalCore.setSelectedCache(null);
        }
    }

    public static boolean getAutoResort() {
        return autoResort;
    }

    public static void setAutoResort(boolean value) {
        autoResort = value;
    }

    public static void MsgDownloadLimit() {
        if (GroundspeakAPI.APIError == 401) {
            new ButtonDialog(Translation.get("apiKeyInvalid"), Translation.get("chkApiState"), MsgBoxButton.OK, MsgBoxIcon.GC_Live).show();
        } else {
            new ButtonDialog(Translation.get("Limit_msg"), Translation.get("Limit_title"), MsgBoxButton.OK, MsgBoxIcon.GC_Live).show();
        }
    }

    public static void chkAPiLogInWithWaitDialog(final iChkReadyHandler handler) {

        // Live API
        if (GroundspeakAPI.isDownloadLimitExceeded()) {
            MsgDownloadLimit();
            return;
        }

        if (isAccessTokenInvalid()) {
            AtomicBoolean isCanceled = new AtomicBoolean(false);
            new CancelWaitDialog("chk API Key", new DownloadAnimation(), new RunAndReady() {
                @Override
                public void ready() {

                }

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
                    ti.schedule(task, 3000);
                }

                @Override
                public void setIsCanceled() {
                    isCanceled.set(true);
                }

            }).show();
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

        return selectedCache.getGeoCacheCode().length() > 0;
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
                Log.err(sClass, "initVersionInfos " + ex.getLocalizedMessage());
            }
        }
        try {
            String[] sections = info.split("#");
            VersionPrefix = sections[1];
            String dat = sections[4];
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dat);
            currentRevision = Integer.decode((new SimpleDateFormat("yyyyMMdd", Locale.US)).format(d));
        } catch (Exception ex) {
            // for parsing of date gives an error
            VersionPrefix = "1000";
            currentRevision = 0;
        }
    }

    public String getVersionString() {
        return "Version: " + currentVersion + currentRevision + "  (" + VersionPrefix + ")";
    }

    public Integer getCurrentRevision() {
        return currentRevision;
    }

    // Interface für den Solver zum Zugriff auf den SelectedCache.
    // Direkter Zugriff geht nicht da der Solver im Core definiert ist
    @Override
    public Cache globalCoreGetSelectedCache() {
        return getSelectedCache();
    }

    @Override
    public void globalCoreSetSelectedCache(Cache cache) {
        setSelectedCache(cache);
    }

    @Override
    public void globalCoreSetSelectedWaypoint(Cache cache, Waypoint waypoint) {
        setSelectedWaypoint(cache, waypoint);
    }

    @Override
    public Waypoint globalCoreGetSelectedWaypoint() {
        return getSelectedWayPoint();
    }

    public interface iChkReadyHandler {
        void checkReady(boolean invalidAccessToken);
    }

}
