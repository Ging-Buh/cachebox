package CB_Core.Api;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.Map.Descriptor;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.MeasuredCoordList;

import junit.framework.TestCase;

/**
 * Enth�lt die Tests um die Caches zu einer bestimmten Position �ber die API
 * abzufragen
 * 
 * @author Longri
 */
public class isPremium_GetFound_Test extends TestCase
{

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		LoadConfig();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();

	}

	/**
	 * l�dt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie
	 * muss eine g�ltige cachebox.config Datei liegen. Diese Datei ist auf der
	 * Ignore list von SVN, so das diese Pers�nliche config nicht ver�ffentlicht
	 * werden kann. (zum Schutz des Pers�nlichen API Keys)
	 */
	private void LoadConfig()
	{
		Config.Initialize("./testdata/", "./testdata/cachebox.config");
		String key = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testIsPremiumMember()
	{
		assertTrue("Muss Premium Member sein", GroundspeakAPI.IsPremiumMember(Config.GetAccessToken()));
	}

	public void testGetCachesFound()
	{

		int Anzahl = GroundspeakAPI.GetCachesFound(Config.GetAccessToken());

		// Bei mir
		assertTrue("Muss Anzahl meiner Funde sein (" + Anzahl + ")", Anzahl == 165);
	}

}
