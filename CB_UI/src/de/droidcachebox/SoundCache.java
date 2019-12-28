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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import de.droidcachebox.settings.SettingsAudio;
import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.log.Log;

public class SoundCache {
    private static final String log = "SoundCache";
    private static Music GlobalVolumeSound;
    private static Music GPS_lose;
    private static Music GPS_fix;
    private static Music Approach;
    private static Music AutoResort;
    private static IChanged changedListener = SoundCache::setVolumes;

    public static void play(Sounds sound) {
        play(sound, false);
    }

    public static void play(Sounds sound, boolean ignoreMute) {
        if (Config.globalVolume.getValue().Mute && !ignoreMute)
            return;

        switch (sound) {
            case GPS_lose:
                if ((ignoreMute || !Config.GPS_lose.getValue().Mute) && GPS_lose != null)
                    GPS_lose.play();
                break;
            case GPS_fix:
                if ((ignoreMute || !Config.GPS_fix.getValue().Mute) && GPS_fix != null)
                    GPS_fix.play();
                break;
            case Approach:
                if ((ignoreMute || !Config.Approach.getValue().Mute) && Approach != null)
                    Approach.play();
                break;
            case AutoResortSound:
                if ((ignoreMute || !Config.AutoResortSound.getValue().Mute) && AutoResort != null)
                    AutoResort.play();
                break;
            case Global:
                if ((ignoreMute || !Config.globalVolume.getValue().Mute) && GlobalVolumeSound != null)
                    GlobalVolumeSound.play();
                break;
        }
    }

    public static void loadSounds() {

        GlobalVolumeSound = getMusikFromSetting(Config.globalVolume);
        Approach = getMusikFromSetting(Config.Approach);
        GPS_fix = getMusikFromSetting(Config.GPS_fix);
        GPS_lose = getMusikFromSetting(Config.GPS_lose);
        AutoResort = getMusikFromSetting(Config.AutoResortSound);

        Config.globalVolume.addSettingChangedListener(changedListener);
        Config.Approach.addSettingChangedListener(changedListener);
        Config.GPS_fix.addSettingChangedListener(changedListener);
        Config.GPS_lose.addSettingChangedListener(changedListener);
        Config.AutoResortSound.addSettingChangedListener(changedListener);

        setVolumes();
    }

    private static void setVolumes() {

        // calc volume Global and own
        float GlobalVolume = Config.globalVolume.getValue().Volume;

        if (GlobalVolumeSound != null)
            GlobalVolumeSound.setVolume(GlobalVolume);
        if (Approach != null)
            Approach.setVolume(Config.Approach.getValue().Volume * GlobalVolume);
        if (GPS_fix != null)
            GPS_fix.setVolume(Config.GPS_fix.getValue().Volume * GlobalVolume);
        if (GPS_lose != null)
            GPS_lose.setVolume(Config.GPS_lose.getValue().Volume * GlobalVolume);
        if (AutoResort != null)
            AutoResort.setVolume(Config.AutoResortSound.getValue().Volume * GlobalVolume);
    }

    private static Music getMusikFromSetting(SettingsAudio set) {
        String path = set.getValue().Path;

        FileHandle handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : GlobalCore.getInternalFileHandle(path);

        if (handle == null || !handle.exists() || handle.isDirectory() || path.length() == 0) {
            path = set.getDefaultValue().Path;
            handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : GlobalCore.getInternalFileHandle(path);
            if (handle != null && handle.exists()) {
                set.loadDefault();
                set.setDirty();
            }
        }

        if (handle == null || !handle.exists()) {
            Log.err(log, "LoadSound: " + set.getValue().Path);
            return null;
        }

        Music ret;
        try {
            ret = Gdx.audio.newMusic(handle);
        } catch (Exception e) {
            Log.err(log, "LoadSound: " + set.getValue().Path);
            return null;
        }
        return ret;
    }

    public enum Sounds {
        GPS_lose, GPS_fix, Approach, AutoResortSound, Global
    }

}
