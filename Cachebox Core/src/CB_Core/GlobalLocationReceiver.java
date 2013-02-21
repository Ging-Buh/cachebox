package CB_Core;

import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.GL_UI.Controls.Dialogs.Toast;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Events.GPS_FallBackEvent;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Empfängt alle Positions Änderungen und sortiert Liste oder spielt Sounds ab.
 * 
 * @author Longri
 */
public class GlobalLocationReceiver implements PositionChangedEvent, GPS_FallBackEvent
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
	public void PositionChanged()
	{

		PlaySounds = Config.settings.PlaySounds.getValue();
		if (newLocationThread == null) newLocationThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{

					if (!initialFixSoundCompleted && Locator.isGPSprovided())
					{
						initialFixSoundCompleted = true;
						if (PlaySounds && GPS_Fix != null && !GPS_Fix.isPlaying())
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
						if (GlobalCore.getSelectedCache() != null)
						{
							float distance = GlobalCore.getSelectedCache().Distance(false);
							if (GlobalCore.getSelectedWaypoint() != null)
							{
								distance = GlobalCore.getSelectedWaypoint().Distance();
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
					if (!initialResortAfterFirstFixCompleted && Locator.getProvider() != ProviderType.NULL)
					{
						if (GlobalCore.getSelectedCache() == null)
						{
							synchronized (Database.Data.Query)
							{
								Database.Data.Query.Resort();
							}
						}
						initialResortAfterFirstFixCompleted = true;
					}
				}
				catch (Exception e)
				{
					Logger.Error("GlobalLocationReceiver",
							"if (!initialResortAfterFirstFixCompleted && GlobalCore.LastValidPosition.Valid)", e);
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
		});

		newLocationThread.run();

	}

	Thread newLocationThread;

	@Override
	public String getReceiverName()
	{
		return "GlobalLocationReceiver";
	}

	public static void resetApprouch()
	{
		approachSoundCompleted = false;
		GlobalCore.switchToCompassCompleted = false;
	}

	@Override
	public void FallBackToNetworkProvider()
	{
		if (initialFixSoundCompleted)
		{
			if (GPS_lose != null) GPS_lose.play();
			initialFixSoundCompleted = false;
		}

		GL.that.Toast("Network-Position", Toast.LENGTH_LONG);
	}

	@Override
	public void OrientationChanged()
	{
	}

	@Override
	public Priority getPriority()
	{
		return Priority.High;
	}

	@Override
	public void SpeedChanged()
	{
	}

}
