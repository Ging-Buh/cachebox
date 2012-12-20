package CB_Core.Api;

import java.util.Iterator;

import junit.framework.TestCase;
import CB_Core.Config;
import CB_Core.InitTestDBs;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;

/**
 * Enth�lt die Tests um die Caches zu einer bestimmten Position �ber die API abzufragen
 * 
 * @author Longri
 */
public class Trackable_Test extends TestCase
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

	public void testGetUserTbList()
	{
		TbList list = new TbList();

		GroundspeakAPI.getMyTbList(Config.GetAccessToken(), list);

		// CB Developer sollten einen "coin of honour" im besitz haben.
		boolean Assert = false;

		Iterator<Trackable> iterator = list.iterator();
		if (iterator != null && iterator.hasNext())
		{
			do
			{
				String Name = iterator.next().getName();
				if (Name.contains("Cachebox") && Name.contains("honour")) Assert = true;
				if (Name.contains("Cachebox") && Name.contains("Honour")) Assert = true;
				if (Name.contains("cachebox") && Name.contains("honour")) Assert = true;
				if (Name.contains("cachebox") && Name.contains("Honour")) Assert = true;
				if (Name.contains("CacheBox") && Name.contains("honour")) Assert = true;
				if (Name.contains("CacheBox") && Name.contains("Honour")) Assert = true;
			}
			while (iterator.hasNext());
		}

		assertTrue("Fehler TB List Abfrage", Assert);
	}

}
