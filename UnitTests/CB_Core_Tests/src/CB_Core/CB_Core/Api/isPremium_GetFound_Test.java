package CB_Core.CB_Core.Api;

import junit.framework.TestCase;
import CB_Core.Api.GroundspeakAPI;
import CB_UI.Config;
import __Static.InitTestDBs;

/**
 * Enth�lt die Tests um die Caches zu einer bestimmten Position �ber die API abzufragen
 * 
 * @author Longri
 */
public class isPremium_GetFound_Test extends TestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		LoadConfig();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

	}

	/**
	 * l�dt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine g�ltige cachebox.db3 Datei liegen. Diese Datei ist
	 * auf der Ignore list von SVN, so das diese Pers�nliche config nicht ver�ffentlicht werden kann. (zum Schutz des Pers�nlichen API Keys)
	 */
	private void LoadConfig() {
		InitTestDBs.InitalConfig();
		String key = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testIsPremiumMember() {
		assertTrue("Muss Premium Member sein", GroundspeakAPI.IsPremiumMember());
	}

	public void testGetCachesFound() {

		int Anzahl = GroundspeakAPI.GetCachesFound(null);

		// Bei mir
		assertTrue("Muss Anzahl meiner Funde sein (" + Anzahl + ")", Anzahl == 508);
	}

}
