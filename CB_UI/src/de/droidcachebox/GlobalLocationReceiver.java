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
package de.droidcachebox;

import de.droidcachebox.SoundCache.Sounds;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheWithWP;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.dialogs.Toast;
import de.droidcachebox.locator.*;
import de.droidcachebox.locator.Location.ProviderType;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.log.Log;

/**
 * Empfängt alle Positions Änderungen und sortiert Liste oder spielt Sounds ab.
 *
 * @author Longri
 */
public class GlobalLocationReceiver implements PositionChangedEvent, GPS_FallBackEvent {
    public final static boolean DEBUG_POSITION = true;
    public final static String GPS_PROVIDER = "gps";
    public final static String NETWORK_PROVIDER = "network";
    private static final String log = "GlobalLocationReceiver";
    private static boolean approachSoundCompleted = false;
    private static boolean PlaySounds = false;
    Thread newLocationThread;
    boolean loseSoundCompleated = false;
    private boolean initialResortAfterFirstFixCompleted = false;
    private boolean initialFixSoundCompleted = false;

    public GlobalLocationReceiver() {

        PositionChangedListeners.addListener(this);
        GPS_FallBackEventList.Add(this);
        try {
            SoundCache.loadSounds();
        } catch (Exception e) {
            Log.err(log, "GlobalLocationReceiver", "Load sound", e);
            e.printStackTrace();
        }
    }

    public static void resetApproach() {

        // set approach sound if the distance low

        if (GlobalCore.isSetSelectedCache()) {
            float distance = GlobalCore.getSelectedCache().Distance(CalculationType.FAST, false);
            boolean value = distance < CB_UI_Settings.SoundApproachDistance.getValue();
            approachSoundCompleted = value;
            GlobalCore.switchToCompassCompleted = value;
        } else {
            approachSoundCompleted = true;
            GlobalCore.switchToCompassCompleted = true;
        }

    }

    @Override
    public void positionChanged() {

        PlaySounds = !CB_UI_Base_Settings.globalVolume.getValue().Mute;

        if (newLocationThread != null) {
            if (newLocationThread.getState() != Thread.State.TERMINATED)
                return;
            else
                newLocationThread = null;
        }

        if (newLocationThread == null)
            newLocationThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        if (PlaySounds && !approachSoundCompleted) {
                            if (GlobalCore.isSetSelectedCache()) {
                                float distance = GlobalCore.getSelectedCache().Distance(CalculationType.FAST, false);
                                if (GlobalCore.getSelectedWaypoint() != null) {
                                    distance = GlobalCore.getSelectedWaypoint().getDistance();
                                }

                                if (!approachSoundCompleted && (distance < CB_UI_Settings.SoundApproachDistance.getValue())) {
                                    SoundCache.play(Sounds.Approach);
                                    approachSoundCompleted = true;

                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.err(log, "GlobalLocationReceiver", "Global.PlaySound(Approach.ogg)", e);
                        e.printStackTrace();
                    }

                    try {
                        if (!initialResortAfterFirstFixCompleted && Locator.getInstance().getProvider() != ProviderType.NULL) {
                            if (GlobalCore.getSelectedCache() == null) {
                                synchronized (Database.Data.cacheList) {
                                    CacheWithWP ret = Database.Data.cacheList.resort(GlobalCore.getSelectedCoordinate(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));

                                    if (ret != null && ret.getCache() != null) {
                                        GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
                                        GlobalCore.setNearestCache(ret.getCache());
                                        ret.dispose();
                                        ret = null;
                                    }

                                }
                            }
                            initialResortAfterFirstFixCompleted = true;
                        }
                    } catch (Exception e) {
                        Log.err(log, "GlobalLocationReceiver", "if (!initialResortAfterFirstFixCompleted && GlobalCore.LastValidPosition.isValid)", e);
                    }

                    try {
                        // schau die 50 nächsten Caches durch, wenn einer davon näher ist
                        // als der aktuell nächste -> umsortieren und raus
                        // only when showing Map or cacheList
                        if (!Database.Data.cacheList.ResortAtWork) {
                            if (GlobalCore.getAutoResort()) {
                                if ((GlobalCore.getNearestCache() == null)) {
                                    GlobalCore.setNearestCache(GlobalCore.getSelectedCache());
                                }
                                int z = 0;
                                if (!(GlobalCore.getNearestCache() == null)) {
                                    boolean resort = false;
                                    if (GlobalCore.getNearestCache().isFound()) {
                                        resort = true;
                                    } else {
                                        if (GlobalCore.getSelectedCache() != GlobalCore.getNearestCache()) {
                                            GlobalCore.setSelectedWaypoint(GlobalCore.getNearestCache(), null, false);
                                        }
                                        float nearestDistance = GlobalCore.getNearestCache().Distance(CalculationType.FAST, true);

                                        for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                                            Cache cache = Database.Data.cacheList.get(i);
                                            z++;
                                            if (z >= 50) {
                                                return;
                                            }
                                            if (cache.isArchived())
                                                continue;
                                            if (!cache.isAvailable())
                                                continue;
                                            if (cache.isFound())
                                                continue;
                                            if (cache.ImTheOwner())
                                                continue;
                                            if (cache.getType() == GeoCacheType.Mystery && !cache.hasCorrectedCoordiantesOrHasCorrectedFinal())
                                                continue;
                                            if (cache.Distance(CalculationType.FAST, true) < nearestDistance) {
                                                resort = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (resort || z == 0) {
                                        CacheWithWP ret = Database.Data.cacheList.resort(GlobalCore.getSelectedCoordinate(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));

                                        GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
                                        GlobalCore.setNearestCache(ret.getCache());
                                        ret.dispose();

                                        SoundCache.play(Sounds.AutoResortSound);
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.err(log, "GlobalLocationReceiver", "Resort", e);
                        e.printStackTrace();
                    }

                }
            });

        try {
            newLocationThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getReceiverName() {
        return "GlobalLocationReceiver";
    }

    @Override
    public void orientationChanged() {
    }

    @Override
    public Priority getPriority() {
        return Priority.High;
    }

    @Override
    public void speedChanged() {
    }

    @Override
    public void Fix() {
        PlaySounds = !CB_UI_Base_Settings.globalVolume.getValue().Mute;

        try {

            if (!initialFixSoundCompleted && Locator.getInstance().isGPSprovided() && GPS.getFixedSats() > 3) {

                Log.debug(log, "Play Fix");
                if (PlaySounds)
                    SoundCache.play(Sounds.GPS_fix);
                initialFixSoundCompleted = true;
                loseSoundCompleated = false;

            }
        } catch (Exception e) {
            Log.err(log, "GlobalLocationReceiver", "Global.PlaySound(GPS_Fix.ogg)", e);
            e.printStackTrace();
        }

    }

    @Override
    public void FallBackToNetworkProvider() {
        PlaySounds = !CB_UI_Base_Settings.globalVolume.getValue().Mute;

        if (initialFixSoundCompleted && !loseSoundCompleated) {

            if (PlaySounds)
                SoundCache.play(Sounds.GPS_lose);

            loseSoundCompleated = true;
            initialFixSoundCompleted = false;
        }

        GL.that.Toast("Network-Position", Toast.LENGTH_LONG);
    }

}
