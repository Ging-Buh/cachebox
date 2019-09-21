/*
 * Copyright (C) 2014 team-cachebox.de
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

package CB_Core.Api;

import CB_Core.CB_Core_Settings;
import CB_Core.CacheListChangedEventList;
import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheListLive;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Map.Descriptor;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Lists.CB_List;
import CB_Utils.Lists.CB_Stack;
import CB_Utils.Lists.CB_Stack.iCompare;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.IChanged;
import CB_Utils.Util.LoopThread;
import com.badlogic.gdx.files.FileHandle;
import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static CB_Locator.Map.Descriptor.TileXToLongitude;
import static CB_Locator.Map.Descriptor.TileYToLatitude;
import static CB_Utils.MathUtils.DEG_RAD;
import static CB_Utils.MathUtils.WGS84_MAJOR_AXIS;

/**
 * @author Longri
 */
public class LiveMapQue {
    public static final String LIVE_CACHE_NAME = "Live_Request";
    public static final String LIVE_CACHE_EXTENSION = ".txt";

    public static final byte DEFAULT_ZOOM_14 = 14;
    public static final int MAX_REQUEST_CACHE_RADIUS_14 = 1060;

    public static final byte DEFAULT_ZOOM_13 = 13;
    public static final int MAX_REQUEST_CACHE_RADIUS_13 = 2120;

    public static final int MAX_REQUEST_CACHE_COUNT = 200;
    public static CacheListLive LiveCaches;
    public static Live_Radius radius = CB_Core.CB_Core_Settings.LiveRadius.getEnumValue();
    public static byte Used_Zoom;
    public static int Used_max_request_radius;
    public static AtomicBoolean DownloadIsActive = new AtomicBoolean(false);
    public static CB_List<QueStateChanged> eventList = new CB_List<LiveMapQue.QueStateChanged>();
    public static CB_Stack<Descriptor> descStack = new CB_Stack<Descriptor>();
    private static GpxFilename gpxFilename;
    private static LoopThread loop = new LoopThread(2000) {
        protected boolean LoopBreak() {
            return descStack.empty();
        }

        protected void Loop() {
            Log.debug(LIVE_CACHE_NAME, "Loop start");
            GL.that.postAsync(() -> {

                Descriptor desc;
                synchronized (descStack) {
                    do {
                        desc = descStack.get();
                    } while (LiveCaches.contains(desc));
                }

                if (desc == null)
                    return;

                for (int i = 0; i < eventList.size(); i++)
                    eventList.get(i).stateChanged();
                DownloadIsActive.set(true);

                double lon1 = DEG_RAD * TileXToLongitude(desc.getZoom(), desc.getX());
                double lat1 = DEG_RAD * TileYToLatitude(desc.getZoom(), desc.getY());
                double lon2 = DEG_RAD * TileXToLongitude(desc.getZoom(), desc.getX() + 1);
                double lat2 = DEG_RAD * TileYToLatitude(desc.getZoom(), desc.getY()+ 1);
                Used_max_request_radius = (int) (WGS84_MAJOR_AXIS * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos((lon2 - lon1))) / 2 + 0.5); // round

                GroundspeakAPI.Query q = new GroundspeakAPI.Query()
                        .setMaxToFetch(MAX_REQUEST_CACHE_COUNT)
                        .setDescriptor(desc)
                        .searchInCircle(desc.getCenterCoordinate(), Used_max_request_radius);
                if (CB_Core_Settings.LiveExcludeFounds.getValue()) q.excludeFinds();
                if (CB_Core_Settings.LiveExcludeOwn.getValue()) q.excludeOwn();
                q.resultWithLiteFields();
                ArrayList<GroundspeakAPI.GeoCacheRelated> apiCaches = null;
                if (descExistLiveCache(desc)) {
                    apiCaches = loadDescLiveFromCache(q);
                }
                if (apiCaches == null) {

                    if (gpxFilename == null) {
                        Category category = CoreSettingsForward.Categories.getCategory("API-Import");
                        gpxFilename = category.addGpxFilename("API-Import");
                    }

                    apiCaches = GroundspeakAPI.searchGeoCaches(q);
                }

                // todo change LiveCaches CB_List<Cache> to ArrayList<GroundspeakAPI.GeoCacheRelated>
                CB_List<Cache> tmp = new CB_List<>();
                for (GroundspeakAPI.GeoCacheRelated c : apiCaches) tmp.add(c.cache);
                final CB_List<Cache> removedCaches = LiveCaches.add(desc, tmp);

                // Log.debug(log, "LIVE_QUE: add " + apiCaches.size() + "from Desc:" + desc.toString() + "/ StackSize:" + descStack.getSize());

                Thread callThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (Database.Data.cacheList) {
                            Database.Data.cacheList.removeAll(removedCaches);
                        }
                        CacheListChangedEventList.Call();
                        for (int i = 0; i < eventList.size(); i++)
                            eventList.get(i).stateChanged();
                    }
                });
                callThread.start();

                DownloadIsActive.set(false);

            });
        }
    };
    private static Descriptor lastLo, lastRu;
    private static Byte count = 0;

    static {
        CB_Core.CB_Core_Settings.LiveRadius.addSettingChangedListener(new IChanged() {
            @Override
            public void handleChange() {
                radius = CB_Core.CB_Core_Settings.LiveRadius.getEnumValue();

                switch (radius) {
                    case Zoom_13:
                        Used_Zoom = DEFAULT_ZOOM_13;
                        Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_13;
                        break;
                    default:
                        Used_Zoom = DEFAULT_ZOOM_14;
                        Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
                        break;

                }
            }

        });

        radius = CB_Core.CB_Core_Settings.LiveRadius.getEnumValue();

        switch (radius) {
            case Zoom_13:
                Used_Zoom = DEFAULT_ZOOM_13;
                Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_13;
                break;
            default:
                Used_Zoom = DEFAULT_ZOOM_14;
                Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
                break;

        }

        int maxLiveCount = CB_Core.CB_Core_Settings.LiveMaxCount.getValue();
        LiveCaches = new CacheListLive(maxLiveCount);
        CB_Core.CB_Core_Settings.LiveMaxCount.addSettingChangedListener(new IChanged() {
            @Override
            public void handleChange() {
                int maxLiveCount = CB_Core.CB_Core_Settings.LiveMaxCount.getValue();
                LiveCaches = new CacheListLive(maxLiveCount);
            }
        });

    }

    private static ArrayList<GroundspeakAPI.GeoCacheRelated> loadDescLiveFromCache(GroundspeakAPI.Query query) {
        String path = query.getDescriptor().getLocalCachePath(LIVE_CACHE_NAME) + LIVE_CACHE_EXTENSION;

        String result;
        FileHandle fh = new FileHandle(path);
        try {
            BufferedReader br = fh.reader(1000);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            result = sb.toString();
            br.close();
            if (result.length() > 0) {
                JSONArray json = (JSONArray) new JSONTokener(result).nextValue();
                if (gpxFilename == null) {
                    Category category = CoreSettingsForward.Categories.getCategory("API-Import");
                    gpxFilename = category.addGpxFilename("API-Import");
                }
                return GroundspeakAPI.getGeoCacheRelateds(json, query.getFields(), null);
            }
        } catch (Exception e) {
            Log.err("LiveMapQue", "loadDescLiveFromCache", e);
        }

        return null;
    }

    private static boolean descExistLiveCache(Descriptor desc) {
        String path = desc.getLocalCachePath(LIVE_CACHE_NAME) + LIVE_CACHE_EXTENSION;
        return FileIO.fileExistsMaxAge(path, CB_Core_Settings.LiveCacheTime.getEnumValue().getMinuten());
    }

    static public void quePosition(Coordinate coord) {
        // no request for invalid Coords
        if (coord == null || !coord.isValid())
            return;

        // no request if disabled
        if (CB_Core.CB_Core_Settings.DisableLiveMap.getValue())
            return;

        final Descriptor desc = new Descriptor(coord, Used_Zoom);
        queDesc(desc);
    }

    public static void queScreen(Descriptor lo, Descriptor ru) {
        if (GroundspeakAPI.isDownloadLimitExceeded())
            return;

        // check last request don't Double!
        if (lastLo != null && lastRu != null) {
            // all Descriptor are into the last request?
            if (lastLo.getX() == lo.getX() && lastRu.getX() == ru.getX() && lastLo.getY() == lo.getY() && lastRu.getY() == ru.getY()) {
                // Still run every 15th!
                if (count++ < 15)
                    return;
                count = 0;
            }

        }

        lastLo = lo;
        lastRu = ru;

        CB_List<Descriptor> descList = new CB_List<Descriptor>();
        for (int i = lo.getX(); i <= ru.getX(); i++) {
            for (int j = lo.getY(); j <= ru.getY(); j++) {
                Descriptor desc = new Descriptor(i, j, lo.getZoom(), lo.NightMode);

                CB_List<Descriptor> descAddList = desc.AdjustZoom(Used_Zoom);

                for (int k = 0; k < descAddList.size(); k++) {
                    if (!descList.contains(descAddList.get(k)))
                        descList.add(descAddList.get(k));
                }
            }
        }

        // remove all descriptor are ready loaded at LiveCaches
        descList.removeAll(LiveCaches.getDescriptorList());

        if (!loop.Alive())
            loop.start();

        synchronized (descStack) {
            descStack.addAll_removeOther(descList);

            // Descriptor MapCenter=MapViewBase.center

            Coordinate center = null;
            if ((lo.Data != null) && (lo.Data instanceof Coordinate))
                center = (Coordinate) lo.Data;
            if (center != null) {
                final Descriptor mapCenterDesc = new Descriptor(center, lo.getZoom());
                descStack.sort(new iCompare<Descriptor>() {
                    @Override
                    public int compare(Descriptor item1, Descriptor item2) {
                        int distanceFromCenter1 = item1.getDistance(mapCenterDesc);
                        int distanceFromCenter2 = item2.getDistance(mapCenterDesc);
                        if (distanceFromCenter1 == distanceFromCenter2)
                            return 0;
                        if (distanceFromCenter1 > distanceFromCenter2)
                            return 1;
                        return -1;
                    }
                });
            }
        }
    }

    public static void setCenterDescriptor(CoordinateGPS center) {
        LiveCaches.setCenterDescriptor(new Descriptor(center, Used_Zoom));
    }

    private static void queDesc(Descriptor desc) {

        if (GroundspeakAPI.isDownloadLimitExceeded())
            return;
        if (!loop.Alive())
            loop.start();
        if (LiveCaches.contains(desc))
            return; // all ready for this descriptor
        synchronized (descStack) {
            descStack.add(desc);
        }
    }

    public void addStateChangedListener(QueStateChanged listener) {
        eventList.add(listener);
    }

    public enum Live_Radius {
        Zoom_13, Zoom_14
    }

    public interface QueStateChanged {
        public void stateChanged();
    }
}
