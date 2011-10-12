package CB_Core.Api;

import java.util.ArrayList;

import junit.framework.TestCase;
import CB_Core.Config;
import CB_Core.Types.Cache;

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
	 * lädt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie
	 * muss eine gültige cachebox.config Datei liegen. Diese Datei ist auf der
	 * Ignore list von SVN, so das diese Persönliche config nicht veröffentlicht
	 * werden kann. (zum Schutz des Persönlichen API Keys)
	 */
	private void LoadConfig()
	{
		Config.Initialize("./testdata/", "./testdata/cachebox.config");
		String key = Config.GetStringEncrypted("GcAPI");
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));

	}

	public void testCache()
	{

		ArrayList<Cache> testCaches = new ArrayList<Cache>();

		Cache c1 = new Cache();
		c1.GcCode = "GC2NFTY";

		Cache c2 = new Cache();
		c2.GcCode = "GC33V5M";

		testCaches.add(c1);
		testCaches.add(c2);

		int test = GroundspeakAPI.GetGeocacheStatus(Config.GetStringEncrypted("GcAPI"), testCaches);

		assertFalse("ProjC40 sollte not Available sein", testCaches.get(0).Available);
		assertTrue("ProjC40 sollte Archived sein", testCaches.get(0).Archived);
	}

}
