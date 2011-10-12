package CB_Core.Api;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.Map.Descriptor;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.MeasuredCoordList;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;

import junit.framework.TestCase;

/**
 * Enthält die Tests um die Caches zu einer bestimmten Position über die API
 * abzufragen
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
	 * lädt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie
	 * muss eine gültige cachebox.config Datei liegen. Diese Datei ist auf der
	 * Ignore list von SVN, so das diese Persönliche config nicht veröffentlicht
	 * werden kann. (zum Schutz des Persönlichen API Keys)
	 */
	private void LoadConfig()
	{
		Config.Initialize("./testdata/", "./testdata/cachebox.config");
		String key=Config.GetStringEncrypted("GcAPI");
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testGetUserTbList()
	{
		TbList list=new TbList();
		
		GroundspeakAPI.getMyTbList(Config.GetStringEncrypted("GcAPI"), list);
		
		// CB Developer sollten einen "coin of honour" im besitz haben.
		boolean Assert=false;
		
		Iterator<Trackable> iterator = list.iterator();
		do
		{
			String Name =iterator.next().getName();
			if(Name.contains("Cachebox")&& Name.contains("honour"))Assert=true;
		}while(iterator.hasNext());
		
		
		assertTrue("Fehler TB List Abfrage", Assert);
	}
	
	

}
