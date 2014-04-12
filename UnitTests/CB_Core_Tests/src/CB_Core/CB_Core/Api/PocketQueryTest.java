package CB_Core.CB_Core.Api;

import java.util.ArrayList;

import junit.framework.TestCase;
import CB_Core.InitTestDBs;
import CB_Core.Api.PocketQuery;
import CB_Core.Api.PocketQuery.PQ;
import CB_UI.Config;

public class PocketQueryTest extends TestCase
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
		String key = Config.GetAccessToken(true);
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testGetPQ_List()
	{
		ArrayList<PQ> list = new ArrayList<PQ>();

		PocketQuery.GetPocketQueryList(list);

		if (list == null || list.size() == 0)
		{
			assertFalse("PQ List ist leer", true);
		}

	}
}
