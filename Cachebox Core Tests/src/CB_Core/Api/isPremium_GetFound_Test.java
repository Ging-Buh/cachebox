package CB_Core.Api;

import junit.framework.TestCase;
import CB_Core.Config;
import CB_Core.InitTestDBs;

/**
 * Enthält die Tests um die Caches zu einer bestimmten Position über die API abzufragen
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
	 * lädt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine gültige cachebox.db3 Datei liegen. Diese Datei ist
	 * auf der Ignore list von SVN, so das diese Persönliche config nicht veröffentlicht werden kann. (zum Schutz des Persönlichen API Keys)
	 */
	private void LoadConfig()
	{
		InitTestDBs.InitalConfig();
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
		assertTrue("Muss Anzahl meiner Funde sein (" + Anzahl + ")", Anzahl == 286);
	}

}
