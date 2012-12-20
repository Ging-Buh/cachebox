package CB_Core.Api;

import java.util.ArrayList;

import junit.framework.TestCase;
import CB_Core.Config;
import CB_Core.InitTestDBs;
import CB_Core.Api.PocketQuery.PQ;

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
	 * l�dt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine g�ltige cachebox.config Datei liegen. Diese Datei
	 * ist auf der Ignore list von SVN, so das diese Pers�nliche config nicht ver�ffentlicht werden kann. (zum Schutz des Pers�nlichen API
	 * Keys)
	 */
	private void LoadConfig()
	{
		InitTestDBs.InitalConfig();
		String key = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testPqDownload()
	{
		ArrayList<PQ> list = new ArrayList<PQ>();

		PocketQuery.GetPocketQueryList(Config.GetAccessToken(true), list);

		if (list != null && list.size() > 0)
		{
			PQ pq = list.get(0);
			PocketQuery.DownloadSinglePocketQuery(pq, "./testdata/");
		}
		else
		{
			assertFalse("Kein PQ download, da PQ List leer ist", true);
		}

	}
}
