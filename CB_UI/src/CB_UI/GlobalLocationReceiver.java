package CB_UI;

import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheWithWP;
import CB_Locator.GPS;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Events.GPS_FallBackEvent;
import CB_Locator.Events.GPS_FallBackEventList;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_UI.GL_UI.SoundCache;
import CB_UI.GL_UI.SoundCache.Sounds;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Log.Logger;

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

	public GlobalLocationReceiver()
	{

		PositionChangedEventList.Add(this);
		GPS_FallBackEventList.Add(this);
		try
		{
			SoundCache.loadSounds();
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

		PlaySounds = !Config.settings.GlobalVolume.getValue().Mute;

		if (newLocationThread != null)
		{
			if (newLocationThread.getState() != Thread.State.TERMINATED) return;
			else
				newLocationThread = null;
		}

		if (newLocationThread == null) newLocationThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{

				try
				{
					if (PlaySounds && !approachSoundCompleted)
					{
						if (GlobalCore.getSelectedCache() != null)
						{
							float distance = GlobalCore.getSelectedCache().Distance(CalculationType.FAST, false);
							if (GlobalCore.getSelectedWaypoint() != null)
							{
								distance = GlobalCore.getSelectedWaypoint().Distance();
							}

							if (!approachSoundCompleted && (distance < Config.settings.SoundApproachDistance.getValue()))
							{
								SoundCache.play(Sounds.Approach);
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
								CacheWithWP ret = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(),
										new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));

								if (ret != null && ret.getCache() != null)
								{
									GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
									GlobalCore.setNearestCache(ret.getCache());
									ret.dispose();
									ret = null;
								}

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
					if (!Database.Data.Query.ResortAtWork)
					{
						if (GlobalCore.getAutoResort())
						{
							if ((GlobalCore.NearestCache() == null))
							{
								GlobalCore.setNearestCache(GlobalCore.getSelectedCache());
							}
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
									if (GlobalCore.getSelectedCache() != GlobalCore.NearestCache())
									{
										GlobalCore.setSelectedWaypoint(GlobalCore.NearestCache(), null, false);
									}
									float nearestDistance = GlobalCore.NearestCache().Distance(CalculationType.FAST, true);
									for (Cache cache : Database.Data.Query)
									{
										z++;
										if (z >= 50)
										{
											return;
										}
										if (cache.Archived) continue;
										if (!cache.Available) continue;
										if (cache.Found) continue;
										if (cache.ImTheOwner()) continue;
										if (cache.Type == CacheTypes.Mystery) if (!cache.CorrectedCoordiantesOrMysterySolved()) continue;
										if (cache.Distance(CalculationType.FAST, true) < nearestDistance)
										{
											resort = true;
											break;
										}
									}
								}
								if (resort || z == 0)
								{
									CacheWithWP ret = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(),
											new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));

									GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
									GlobalCore.setNearestCache(ret.getCache());
									ret.dispose();

									SoundCache.play(Sounds.AutoResortSound);
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

		newLocationThread.start();

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

	@Override
	public void Fix()
	{
		PlaySounds = !Config.settings.GlobalVolume.getValue().Mute;

		try
		{

			if (!initialFixSoundCompleted && Locator.isGPSprovided() && GPS.getFixedSats() > 3)
			{

				Logger.LogCat("Play Fix");
				if (PlaySounds) SoundCache.play(Sounds.GPS_fix);
				initialFixSoundCompleted = true;
				loseSoundCompleated = false;

			}
		}
		catch (Exception e)
		{
			Logger.Error("GlobalLocationReceiver", "Global.PlaySound(GPS_Fix.ogg)", e);
			e.printStackTrace();
		}

	}

	boolean loseSoundCompleated = false;

	@Override
	public void FallBackToNetworkProvider()
	{
		PlaySounds = !Config.settings.GlobalVolume.getValue().Mute;

		if (initialFixSoundCompleted && !loseSoundCompleated)
		{

			if (PlaySounds) SoundCache.play(Sounds.GPS_lose);

			loseSoundCompleated = true;
			initialFixSoundCompleted = false;
		}

		GL.that.Toast("Network-Position", Toast.LENGTH_LONG);
	}

}
