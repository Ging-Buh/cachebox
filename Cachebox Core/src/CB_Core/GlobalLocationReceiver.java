package CB_Core;

import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.GL_UI.Controls.Dialogs.Toast;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Locator.Locator;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Empfängt alle Positions Änderungen und sortiert Liste oder spielt Sounds ab.
 * 
 * @author Longri
 */
public class GlobalLocationReceiver implements PositionChangedEvent
{

	public final static String GPS_PROVIDER = "gps";
	public final static String NETWORK_PROVIDER = "network";

	private boolean initialResortAfterFirstFixCompleted = false;
	private boolean initialFixSoundCompleted = false;
	private static boolean approachSoundCompleted = false;

	private Music GPS_lose;
	private Music GPS_Fix;
	private Music Approach;
	private Music AutoResort;

	/*
	 * Wenn 10 Sekunden kein gültiges GPS Signal gefunden wird. Aber nur beim Ersten mal. Danach warten wir lieber 90 sec
	 */
	private int NetworkPositionTime = 10000;
	private static long GPSTimeStamp = 0;

	public GlobalLocationReceiver()
	{

		PositionChangedEventList.Add(this);

		String path = Config.settings.SoundPath.getValue();

		try
		{
			Approach = Gdx.audio.newMusic(Gdx.files.absolute(path + "/Approach.ogg"));
			GPS_Fix = Gdx.audio.newMusic(Gdx.files.absolute(path + "/GPS_Fix.ogg"));
			GPS_lose = Gdx.audio.newMusic(Gdx.files.absolute(path + "/GPS_lose.ogg"));
			AutoResort = Gdx.audio.newMusic(Gdx.files.absolute(path + "/AutoResort.ogg"));
		}
		catch (Exception e)
		{
			Logger.Error("GlobalLocationReceiver", "Load sound", e);
			e.printStackTrace();
		}
	}

	private static boolean PlaySounds = false;

	@Override
	public void PositionChanged(Locator location)
	{

		PlaySounds = Config.settings.PlaySounds.getValue();

		try
		{
			if (location.getProvider().equalsIgnoreCase(GPS_PROVIDER)) // Neue Position von GPS-Empfänger
			{
				newLocationReceived(location);
				GPSTimeStamp = java.lang.System.currentTimeMillis();
				return;
			}
		}
		catch (Exception e)
		{
			Logger.Error("GlobalLocationReceiver", "GPS_PROVIDER", e);
			e.printStackTrace();
		}

		try
		{
			// Neue Position vom Netzwerk
			if (location.getProvider().equalsIgnoreCase(NETWORK_PROVIDER))
			{
				// Wenn 10 Sekunden kein GPS Signal
				if ((java.lang.System.currentTimeMillis() - GPSTimeStamp) > NetworkPositionTime)
				{
					NetworkPositionTime = 90000;
					newLocationReceived(location);
					if (initialFixSoundCompleted)
					{
						// Global.PlaySound("GPS_lose.ogg");
						GPS_lose.play();
						initialFixSoundCompleted = false;
					}

					GL.that.Toast("Network-Position", Toast.LENGTH_LONG);
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("GlobalLocationReceiver", "NETWORK_PROVIDER", e);
			e.printStackTrace();
		}

	}

	private void newLocationReceived(Locator location)
	{
		try
		{

			if (!initialFixSoundCompleted && GlobalCore.LastValidPosition.Valid && location.getProvider().equalsIgnoreCase(GPS_PROVIDER))
			{
				initialFixSoundCompleted = true;
				if (GPS_Fix != null && !GPS_Fix.isPlaying())
				{
					Logger.LogCat("Play Fix");
					GPS_Fix.play();
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("GlobalLocationReceiver", "Global.PlaySound(GPS_Fix.ogg)", e);
			e.printStackTrace();
		}

		try
		{
			if (PlaySounds && !approachSoundCompleted)
			{
				if (GlobalCore.SelectedCache() != null)
				{
					float distance = GlobalCore.SelectedCache().Distance(false);
					if (GlobalCore.SelectedWaypoint() != null)
					{
						distance = GlobalCore.SelectedWaypoint().Distance();
					}

					if (!approachSoundCompleted && (distance < Config.settings.SoundApproachDistance.getValue()))
					{
						if (Approach != null) Approach.play();
						approachSoundCompleted = true;

					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("GlobalLocationReceiver", "Global.PlaySound(Approach.ogg)", e);
			e.printStackTrace();
		}

		try
		{
			if (!initialResortAfterFirstFixCompleted && GlobalCore.LastValidPosition.Valid)
			{
				if (GlobalCore.SelectedCache() == null)
				{
					Database.Data.Query.Resort();
				}
				initialResortAfterFirstFixCompleted = true;
			}
		}
		catch (Exception e)
		{
			Logger.Error("GlobalLocationReceiver", "if (!initialResortAfterFirstFixCompleted && GlobalCore.LastValidPosition.Valid)", e);
			e.printStackTrace();
		}

		try
		{
			// schau die 50 nächsten Caches durch, wenn einer davon näher ist
			// als der aktuell nächste -> umsortieren und raus
			// only when showing Map or cacheList
			if (!GlobalCore.ResortAtWork)
			{
				if (GlobalCore.autoResort)
				{
					int z = 0;
					if (!(GlobalCore.NearestCache() == null))
					{
						boolean resort = false;
						if (GlobalCore.NearestCache().Found)
						{
							resort = true;
						}
						else
						{
							for (Cache cache : Database.Data.Query)
							{
								z++;
								if (z >= 50) return;
								if (cache.Archived) continue;
								if (!cache.Available) continue;
								if (cache.Found) continue;
								if (cache.ImTheOwner()) continue;
								if (cache.Type == CacheTypes.Mystery) if (!cache.CorrectedCoordiantesOrMysterySolved()) continue;
								if (cache.Distance(true) < GlobalCore.NearestCache().Distance(true))
								{
									resort = true;
									break;
								}
							}
						}
						if (resort)
						{
							Database.Data.Query.Resort();

							if (AutoResort != null) AutoResort.play();
							return;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("GlobalLocationReceiver", "Resort", e);
			e.printStackTrace();
		}

	}

	@Override
	public void OrientationChanged(float heading)
	{
	}

	@Override
	public String getReceiverName()
	{
		return "GlobalLocationReceiver";
	}

	public static void resetApprouch()
	{
		approachSoundCompleted = false;
	}

}
