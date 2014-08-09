package Types;

import junit.framework.TestCase;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheListLive;
import CB_Locator.Map.Descriptor;
import CB_Utils.Lists.CB_List;

public class CacheListLive_test extends TestCase
{

	public void test_fillAndOverload()
	{

		// 1. aa +
		// ab +
		// ac +
		// ad + 4
		// ______________________________________
		// 2. aa -
		// ae +
		// af +
		// ag + 7
		// _______________________________________
		// 3. aa -
		// ab -
		// ac -
		// ad - 7
		// _______________________________________
		// 4. ba +
		// be +
		// bf +
		// bg +
		// ba -
		// he +
		// hf +
		// hg + 14
		// ____________________________________
		// 5. ce +
		// cf +
		// cg +
		// ca +
		// cb +
		// cc + 20-4=16
		// ________________________________________
		// 6. ke +
		// lf +
		// mg +
		// na +
		// oe +
		// pf +
		// ef +
		// ff +
		// gf +
		// hf -
		// if +
		// jf + 27-3=24-4=20-6=14

		CacheListLive cacheList = new CacheListLive(16);

		Descriptor desc1 = new Descriptor(50, 50, 13, false);
		CB_List<Cache> listDesc1 = new CB_List<Cache>();
		Cache ca1 = getTmpCache("aa");
		Cache ca2 = getTmpCache("ab");
		Cache ca3 = getTmpCache("ac");
		Cache ca4 = getTmpCache("ad");
		listDesc1.add(ca1);
		listDesc1.add(ca2);
		listDesc1.add(ca3);
		listDesc1.add(ca4);

		cacheList.add(desc1, listDesc1);
		int s = cacheList.getSize();
		assertTrue("Cache size must be 4", s == 4);
		assertTrue("Cache size must lower then max capacity", s < cacheList.getCapacity());
		assertCacheAtList(cacheList, ca1, ca2, ca3, ca4);

		// 2.-------------------------------------------------------------------------------------

		Descriptor desc2 = new Descriptor(51, 50, 13, false);
		CB_List<Cache> listDesc2 = new CB_List<Cache>();
		Cache ca5 = getTmpCache("aa");
		Cache ca6 = getTmpCache("ae");
		Cache ca7 = getTmpCache("af");
		Cache ca8 = getTmpCache("ag");
		listDesc2.add(ca5);
		listDesc2.add(ca6);
		listDesc2.add(ca7);
		listDesc2.add(ca8);

		cacheList.add(desc2, listDesc2);
		int s2 = cacheList.getSize();
		assertTrue("Cache size must be 7", s2 == 7);// ca5 must not be added, GcCode aa is already at the list with ca1
		assertTrue("Cache size must lower then max capacity", s < cacheList.getCapacity());
		assertCacheAtList(cacheList, ca1, ca2, ca3, ca4, ca6, ca7, ca8);

		// 3. -------------------------------------------------------------------------------------

		Descriptor desc3 = new Descriptor(50, 50, 13, false);
		CB_List<Cache> listDesc3 = new CB_List<Cache>();
		listDesc3.add(ca1);
		listDesc3.add(ca2);
		listDesc3.add(ca3);
		listDesc3.add(ca4);

		cacheList.add(desc3, listDesc3);
		int s3 = cacheList.getSize();
		assertTrue("Cache size must be 7", s3 == 7);// must not be added, Descriptor is already at the list.
		assertTrue("Cache size must lower then max capacity", s < cacheList.getCapacity());
		assertCacheAtList(cacheList, ca1, ca2, ca3, ca4, ca6, ca7, ca8);

		// 4.-------------------------------------------------------------------------------------

		Descriptor desc4 = new Descriptor(51, 51, 13, false);
		CB_List<Cache> listDesc4 = new CB_List<Cache>();
		Cache ca9 = getTmpCache("ba");
		Cache ca10 = getTmpCache("be");
		Cache ca11 = getTmpCache("bf");
		Cache ca12 = getTmpCache("bg");
		Cache ca13 = getTmpCache("ba");
		Cache ca14 = getTmpCache("he");
		Cache ca15 = getTmpCache("hf");
		Cache ca16 = getTmpCache("hg");
		listDesc4.add(ca9);
		listDesc4.add(ca10);
		listDesc4.add(ca11);
		listDesc4.add(ca12);
		listDesc4.add(ca13);
		listDesc4.add(ca14);
		listDesc4.add(ca15);
		listDesc4.add(ca16);

		cacheList.add(desc4, listDesc4);
		int s4 = cacheList.getSize();
		assertTrue("Cache size must be 14", s4 == 14);// must not be added, Descriptor is already at the list.
		assertTrue("Cache size must lower then max capacity", s < cacheList.getCapacity());
		assertCacheAtList(cacheList, ca1, ca2, ca3, ca4, ca6, ca7, ca8, ca9, ca10, ca11, ca12, ca14, ca15, ca16);

		// 5.-------------------------------------------------------------------------------------

		Descriptor desc5 = new Descriptor(52, 51, 13, false);
		CB_List<Cache> listDesc5 = new CB_List<Cache>();
		Cache ca17 = getTmpCache("ce");
		Cache ca18 = getTmpCache("cf");
		Cache ca19 = getTmpCache("cg");
		Cache ca20 = getTmpCache("ca");
		Cache ca21 = getTmpCache("cb");
		Cache ca22 = getTmpCache("cc");
		listDesc5.add(ca17);
		listDesc5.add(ca18);
		listDesc5.add(ca19);
		listDesc5.add(ca20);
		listDesc5.add(ca21);
		listDesc5.add(ca22);

		cacheList.add(desc5, listDesc5);
		int s5 = cacheList.getSize();
		assertTrue("Cache size must be 16", s5 == 16);// first descriptor must remove
		assertTrue("Cache size must lower then max capacity", s < cacheList.getCapacity());
		assertCacheAtList(cacheList, ca6, ca7, ca8, ca9, ca10, ca11, ca12, ca14, ca15, ca16, ca17, ca18, ca19, ca20, ca21, ca22);
		assertCacheNotAtList(cacheList, ca1, ca2, ca3, ca4);

		// 6.-------------------------------------------------------------------------------------
		// must remove three Descriptors
		Descriptor desc6 = new Descriptor(52, 52, 13, false);
		CB_List<Cache> listDesc6 = new CB_List<Cache>();
		Cache ca23 = getTmpCache("ke");
		Cache ca24 = getTmpCache("lf");
		Cache ca25 = getTmpCache("mg");
		Cache ca26 = getTmpCache("na");
		Cache ca27 = getTmpCache("oe");
		Cache ca28 = getTmpCache("pf");
		Cache ca29 = getTmpCache("ef");
		Cache ca30 = getTmpCache("ff");
		Cache ca31 = getTmpCache("gf");
		Cache ca32 = getTmpCache("hf");
		Cache ca33 = getTmpCache("if");
		Cache ca34 = getTmpCache("jf");
		listDesc6.add(ca23);
		listDesc6.add(ca24);
		listDesc6.add(ca25);
		listDesc6.add(ca26);
		listDesc6.add(ca27);
		listDesc6.add(ca28);
		listDesc6.add(ca29);
		listDesc6.add(ca30);
		listDesc6.add(ca31);
		listDesc6.add(ca32);
		listDesc6.add(ca33);
		listDesc6.add(ca34);

		cacheList.add(desc6, listDesc6);
		int s6 = cacheList.getSize();
		assertTrue("Cache size must be 11", s6 == 11);// first three descriptors must remove
		assertTrue("Cache size must lower then max capacity", s < cacheList.getCapacity());
		assertCacheAtList(cacheList, ca23, ca24, ca25, ca26, ca27, ca28, ca29, ca30, ca31, ca33, ca34);
		assertCacheNotAtList(cacheList, ca1, ca2, ca3, ca4, ca6, ca7, ca8, ca9, ca10, ca11, ca12, ca13, ca14, ca15, ca16, ca17, ca18, ca19,
				ca20, ca21, ca22);

		// -------------------------------------------------------------------------------------

		// New CacheList with low capacity for test hold at least one Descriptor
		cacheList = new CacheListLive(5);

		// -------------------------------------------------------------------------------------
		listDesc1 = new CB_List<Cache>();
		ca1 = getTmpCache("aa");
		ca2 = getTmpCache("ab");
		ca3 = getTmpCache("ac");
		ca4 = getTmpCache("ad");
		listDesc1.add(ca1);
		listDesc1.add(ca2);
		listDesc1.add(ca3);
		listDesc1.add(ca4);

		cacheList.add(desc1, listDesc1);
		int s7 = cacheList.getSize();
		assertTrue("Cache size must be 4", s7 == 4);
		assertTrue("Cache size must lower then max capacity", s < cacheList.getCapacity());
		assertCacheAtList(cacheList, ca1, ca2, ca3, ca4);

		// -------------------------------------------------------------------------------------

		cacheList.add(desc6, listDesc6);
		int s8 = cacheList.getSize();
		assertTrue("Cache size must be 12", s8 == 12);// first three descriptors must remove
		assertTrue("Cache size must lower then max capacity", s < cacheList.getCapacity());
		assertCacheAtList(cacheList, ca23, ca24, ca25, ca26, ca27, ca28, ca29, ca30, ca31, ca32, ca33, ca34);
		assertCacheNotAtList(cacheList, ca1, ca2, ca3, ca4);

		// -------------------------------------------------------------------------------------

	}

	private void assertCacheAtList(CacheListLive cacheList, Cache... array)
	{

		CB_List<Cache> tmp = new CB_List<Cache>();

		for (int i = 0; i < cacheList.getSize(); i++)
		{
			Cache ca2 = cacheList.get(i);
			assertFalse("wurde schon aus der Liste zurück gegeben", tmp.contains(ca2));
			tmp.add(ca2);
		}

		for (Cache ca : array)
		{
			assertTrue("CacheList must contains the Cache", cacheList.contains(ca));
			assertTrue("Getted CacheList must contains the Cache", tmp.contains(ca));
		}

	}

	private void assertCacheNotAtList(CacheListLive cacheList, Cache... array)
	{

		for (Cache ca : array)
		{
			assertFalse("CacheList must Not contains the Cache", cacheList.contains(ca));
		}

	}

	private Cache getTmpCache(String GcCode)
	{
		Cache ca = new Cache(false);
		ca.setGcCode(GcCode);
		return ca;
	}

}
