package CB_Core.Types;

import junit.framework.TestCase;

public class CacheTest extends TestCase {
	public void testConstructor() {
		Cache cache = new Cache();
		assertTrue( "Objekt muss konstruierbar sein", cache!=null );
	}
}
