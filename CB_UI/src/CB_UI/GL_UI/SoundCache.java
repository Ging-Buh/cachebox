package CB_UI.GL_UI;

import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.Tag;
import CB_Utils.Settings.SettingsAudio;
import CB_Utils.Util.iChanged;

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
		if (Config.GlobalVolume.getValue().Mute && !ignoreMute) return;

		switch (sound)
		{
		case GPS_lose:
			if ((ignoreMute || !Config.GPS_lose.getValue().Mute) && GPS_lose != null) GPS_lose.play();
			break;
		case GPS_fix:
			if ((ignoreMute || !Config.GPS_fix.getValue().Mute) && GPS_fix != null) GPS_fix.play();
			break;
		case Approach:
			if ((ignoreMute || !Config.Approach.getValue().Mute) && Approach != null) Approach.play();
			break;
		case AutoResortSound:
			if ((ignoreMute || !Config.AutoResortSound.getValue().Mute) && AutoResort != null) AutoResort.play();
			break;
		case Global:
			if ((ignoreMute || !Config.GlobalVolume.getValue().Mute) && GlobalVolumeSound != null) GlobalVolumeSound.play();
			break;
		}
	}

	public static void loadSounds()
	{

		GlobalVolumeSound = getMusikFromSetting(Config.GlobalVolume);
		Approach = getMusikFromSetting(Config.Approach);
		GPS_fix = getMusikFromSetting(Config.GPS_fix);
		GPS_lose = getMusikFromSetting(Config.GPS_lose);
		AutoResort = getMusikFromSetting(Config.AutoResortSound);

		Config.GlobalVolume.addChangedEventListner(changedListner);
		Config.Approach.addChangedEventListner(changedListner);
		Config.GPS_fix.addChangedEventListner(changedListner);
		Config.GPS_lose.addChangedEventListner(changedListner);
		Config.AutoResortSound.addChangedEventListner(changedListner);

		setVolumes();
	}

	public static void setVolumes()
	{

		// calc volume Global and own
		float GlobalVolume = Config.GlobalVolume.getValue().Volume;

		if (GlobalVolumeSound != null) GlobalVolumeSound.setVolume(GlobalVolume);
		if (Approach != null) Approach.setVolume(Config.Approach.getValue().Volume * GlobalVolume);
		if (GPS_fix != null) GPS_fix.setVolume(Config.GPS_fix.getValue().Volume * GlobalVolume);
		if (GPS_lose != null) GPS_lose.setVolume(Config.GPS_lose.getValue().Volume * GlobalVolume);
		if (AutoResort != null) AutoResort.setVolume(Config.AutoResortSound.getValue().Volume * GlobalVolume);
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

		FileHandle handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : GlobalCore.getInternalFileHandle(path);

		if (handle == null || !handle.exists() || handle.isDirectory() || path.length() == 0)
		{
			path = set.getDefaultValue().Path;
			handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : GlobalCore.getInternalFileHandle(path);
			if (handle != null && handle.exists())
			{
				set.loadDefault();
				set.setDirty();
			}
		}

		if (handle == null || !handle.exists())
		{
			Gdx.app.error(Tag.TAG, "LoadSound" + set.getValue().Path);
			return null;
		}

		Music ret;
		try
		{
			ret = Gdx.audio.newMusic(handle);
		}
		catch (Exception e)
		{
			Gdx.app.error(Tag.TAG, "LoadSound" + set.getValue().Path);
			return null;
		}
		return ret;
	}

}
