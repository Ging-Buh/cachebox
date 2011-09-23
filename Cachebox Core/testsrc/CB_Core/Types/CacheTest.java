package CB_Core.Types;

import CB_Core.GlobalCore;
import junit.framework.TestCase;

public class CacheTest extends TestCase
{

	private Cache mCache;

	@Override
	public void setUp() throws Exception
	{
		// TODO Auto-generated method stub
		super.setUp();
		mCache = new Cache();
	}

	@Override
	protected void tearDown() throws Exception
	{
		// TODO Auto-generated method stub
		super.tearDown();
		mCache = null;
	}

	public void testConstructor()
	{
		assertTrue("Objekt muss konstruierbar sein", mCache != null);
	}

	public void testDistance()
	{
		Coordinate coordinate1 = new Coordinate();
		assertTrue("Objekt muss konstruierbar sein", coordinate1 != null);
		coordinate1.Latitude = 49.428333;
		coordinate1.Longitude = 6.203333;
		// Vorsicht - hier wird nur die Objekt-Referenz gesetzt...
		mCache.Pos = coordinate1;

		Coordinate coordinate2 = new Coordinate();
		assertTrue("Objekt muss konstruierbar sein", coordinate2 != null);
		coordinate2.Latitude = 49.427700;
		coordinate2.Longitude = 6.204300;

		
		GlobalCore.LastValidPosition = coordinate2;
		
		
		// TODO Distanzberechnung
		float distance = mCache.Distance(true);
		assertTrue("Entfernung muss 100m sein", (distance > 99.38390) && (distance < 99.38392));

	}
}
