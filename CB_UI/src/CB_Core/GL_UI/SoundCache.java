package CB_Core.GL_UI;

import CB_Core.Config;
import CB_Core.Settings.SettingsAudio;
import CB_Core.Util.iChanged;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public class SoundCache
{
	public enum Sounds
	{
		GPS_lose, GPS_Fix, Approach, AutoResort
	}

	private static Music GPS_lose;
	private static Music GPS_Fix;
	private static Music Approach;
	private static Music AutoResort;

	public static void play(Sounds sound)
	{
		switch (sound)
		{
		case GPS_lose:
			if (!Config.settings.GPS_lose.getValue().mMute) GPS_lose.play();
		case GPS_Fix:
			if (!Config.settings.GPS_Fix.getValue().mMute) GPS_Fix.play();
		case Approach:
			if (!Config.settings.Approach.getValue().mMute) Approach.play();
		case AutoResort:
			if (!Config.settings.AutoResort.getValue().mMute) AutoResort.play();
		}
	}

	public static void loadSounds()
	{
		Approach = getMusikFromSetting(Config.settings.Approach);
		GPS_Fix = getMusikFromSetting(Config.settings.GPS_Fix);
		GPS_lose = getMusikFromSetting(Config.settings.GPS_lose);
		AutoResort = getMusikFromSetting(Config.settings.AutoResort);

		Config.settings.Approach.addChangedEventListner(changedListner);
		Config.settings.GPS_Fix.addChangedEventListner(changedListner);
		Config.settings.GPS_lose.addChangedEventListner(changedListner);
		Config.settings.AutoResort.addChangedEventListner(changedListner);
	}

	private static iChanged changedListner = new iChanged()
	{

		@Override
		public void isChanged()
		{
			loadSounds();
		}
	};

	private static Music getMusikFromSetting(SettingsAudio set)
	{
		String path = set.getValue().mPath;
		FileHandle handle = set.getValue().mClass_Absolute ? Gdx.files.absolute(path) : Gdx.files.classpath(path);
		Music ret = Gdx.audio.newMusic(handle);
		ret.setVolume(set.getValue().mVolume);
		return ret;
	}

}
