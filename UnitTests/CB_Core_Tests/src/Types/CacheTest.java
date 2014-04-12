package Types;

import junit.framework.TestCase;
import CB_Core.Types.Cache;
import CB_Locator.Coordinate;
import CB_Locator.Location;
import CB_Locator.Location.ProviderType;
import CB_Utils.MathUtils.CalculationType;

public class CacheTest extends TestCase
{

	private Cache mCache;

	@Override
	public void setUp() throws Exception
	{

		super.setUp();
		mCache = new Cache();
	}

	@Override
	protected void tearDown() throws Exception
	{

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
		coordinate1.setLatitude(49.428333);
		coordinate1.setLongitude(6.203333);
		// Vorsicht - hier wird nur die Objekt-Referenz gesetzt...
		mCache.Pos = coordinate1;

		// Initial Locator
		new CB_Locator.Locator(Location.NULL_LOCATION);

		// Set Location
		CB_Locator.Locator.setNewLocation(new CB_Locator.Location(49.427700, 6.204300, 100, false, 0, false, 0, 0, ProviderType.GPS));

		// Distanzberechnung
		float distance = mCache.Distance(CalculationType.ACCURATE, true);
		assertTrue("Entfernung muss 100m sein", (distance > 99.38390) && (distance < 99.38392));

	}
}
