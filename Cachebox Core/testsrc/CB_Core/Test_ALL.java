package CB_Core;

import CB_Core.Types.CacheTest;
import junit.framework.TestCase;

public class Test_ALL extends TestCase
{
	
	public void test_all() throws Exception
	{
		// Test Types
		CacheTest cacheTest = new CacheTest();
		cacheTest.setUp();
		cacheTest.testDistance();
		
		// Test Imports
		Test_all_Imports.test_all_Import();
	}
}
