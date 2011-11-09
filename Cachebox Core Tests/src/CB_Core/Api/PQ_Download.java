package CB_Core.Api;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.Api.PocketQuery.PQ;
import junit.framework.TestCase;

public class PQ_Download extends TestCase
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
		String key = Config.GetAccessToken();
		assertFalse(
				"Kein Access Key gefunden, liegt die Config an der richtigen stelle?",
				key.equals(""));
	}

	public void testPqDownload()
	{
		ArrayList<PQ> list = new ArrayList<PQ>();

		PocketQuery.GetPocketQueryList(Config.GetAccessToken(), list);
		
		
		PQ pq= list.get(0);
		
		
		PocketQuery.DownloadSinglePocketQuery(pq,"./testdata/");
		
		
	}
}
