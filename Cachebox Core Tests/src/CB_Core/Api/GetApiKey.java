package CB_Core.Api;

import junit.framework.TestCase;
import CB_Core.InitTestDBs;

public class GetApiKey extends TestCase
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

	}

	public void test_getAPI_Key()
	{
		// (new GcApiLogin()).RunRequest(); Das Automatische Holen des Keys geht hier noch nicht!
	}

}
