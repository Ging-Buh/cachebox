package CB_Core.GL_UI;

import CB_Core.Config;
import CB_Core.Log.Logger;
import CB_Core.Settings.SettingsAudio;
import CB_Core.Util.iChanged;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public class SoundCache
{
	public enum Sounds
	{
		GPS_lose, GPS_fix, Approach, AutoResortSound, Global
	}

	private static Music GlobalVolumeSound;
	private static Music GPS_lose;
	private static Music GPS_fix;
	private static Music Approach;
	private static Music AutoResort;

	public static void play(Sounds sound)
	{
		play(sound, false);
	}

	public static void play(Sounds sound, boolean ignoreMute)
	{
		if (Config.settings.GlobalVolume.getValue().Mute && !ignoreMute) return;

		switch (sound)
		{
		case GPS_lose:
			if ((ignoreMute || !Config.settings.GPS_lose.getValue().Mute) && GPS_lose != null) GPS_lose.play();
			break;
		case GPS_fix:
			if ((ignoreMute || !Config.settings.GPS_fix.getValue().Mute) && GPS_fix != null) GPS_fix.play();
			break;
		case Approach:
			if ((ignoreMute || !Config.settings.Approach.getValue().Mute) && Approach != null) Approach.play();
			break;
		case AutoResortSound:
			if ((ignoreMute || !Config.settings.AutoResortSound.getValue().Mute) && AutoResort != null) AutoResort.play();
			break;
		case Global:
			if ((ignoreMute || !Config.settings.GlobalVolume.getValue().Mute) && GlobalVolumeSound != null) GlobalVolumeSound.play();
			break;
		}
	}

	public static void loadSounds()
	{

		GlobalVolumeSound = getMusikFromSetting(Config.settings.GlobalVolume);
		Approach = getMusikFromSetting(Config.settings.Approach);
		GPS_fix = getMusikFromSetting(Config.settings.GPS_fix);
		GPS_lose = getMusikFromSetting(Config.settings.GPS_lose);
		AutoResort = getMusikFromSetting(Config.settings.AutoResortSound);

		Config.settings.GlobalVolume.addChangedEventListner(changedListner);
		Config.settings.Approach.addChangedEventListner(changedListner);
		Config.settings.GPS_fix.addChangedEventListner(changedListner);
		Config.settings.GPS_lose.addChangedEventListner(changedListner);
		Config.settings.AutoResortSound.addChangedEventListner(changedListner);

		setVolumes();
	}

	public static void setVolumes()
	{

		// calc volume Global and own
		float GlobalVolume = Config.settings.GlobalVolume.getValue().Volume;

		if (GlobalVolumeSound != null) GlobalVolumeSound.setVolume(GlobalVolume);
		if (Approach != null) Approach.setVolume(Config.settings.Approach.getValue().Volume * GlobalVolume);
		if (GPS_fix != null) GPS_fix.setVolume(Config.settings.GPS_fix.getValue().Volume * GlobalVolume);
		if (GPS_lose != null) GPS_lose.setVolume(Config.settings.GPS_lose.getValue().Volume * GlobalVolume);
		if (AutoResort != null) AutoResort.setVolume(Config.settings.AutoResortSound.getValue().Volume * GlobalVolume);
	}

	private static iChanged changedListner = new iChanged()
	{

		@Override
		public void isChanged()
		{
			setVolumes();
		}

	};

	private static Music getMusikFromSetting(SettingsAudio set)
	{
		String path = set.getValue().Path;
		FileHandle handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : Gdx.files.classpath(path);

		if (handle == null || !handle.exists() || handle.isDirectory() || path.length() == 0)
		{
			path = set.getDefaultValue().Path;
			handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : Gdx.files.classpath(path);
		}

		if (handle == null || !handle.exists())
		{
			Logger.Error("LoadSound", set.getValue().Path);
			return null;
		}

		Music ret;
		try
		{
			ret = Gdx.audio.newMusic(handle);
		}
		catch (Exception e)
		{
			Logger.Error("LoadSound", set.getValue().Path);
			return null;
		}
		return ret;
	}

}
