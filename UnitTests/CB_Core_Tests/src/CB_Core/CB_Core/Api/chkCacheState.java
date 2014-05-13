package CB_Core.CB_Core.Api;

import __Static.InitTestDBs;
import junit.framework.TestCase;
import CB_UI.Config;

public class chkCacheState extends TestCase
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
	 * lädt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine gültige cachebox.config Datei liegen. Diese Datei
	 * ist auf der Ignore list von SVN, so das diese Persönliche config nicht veröffentlicht werden kann. (zum Schutz des Persönlichen API
	 * Keys)
	 */
	private void LoadConfig()
	{
		InitTestDBs.InitalConfig();
		String key = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));

	}

	public void testCache()
	{

		// TODO geht eine chkState Abfrage mit 100 Blöcken viel schneller als eine mit 10 Blöcken
		// bei 10 Blöcken brauch nicht so lange auf ein cancel gewartet werden!

	}

}
