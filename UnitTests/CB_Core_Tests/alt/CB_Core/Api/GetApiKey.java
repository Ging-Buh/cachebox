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
	 * l�dt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine g�ltige cachebox.config Datei liegen. Diese Datei
	 * ist auf der Ignore list von SVN, so das diese Pers�nliche config nicht ver�ffentlicht werden kann. (zum Schutz des Pers�nlichen API
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
