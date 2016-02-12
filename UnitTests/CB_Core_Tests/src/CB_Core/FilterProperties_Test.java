package CB_Core;

import CB_UI.GL_UI.Activitys.FilterSettings.PresetListView;
import junit.framework.TestCase;

public class FilterProperties_Test extends TestCase {
	public void test_ChkPresets() {

		{// chk Preset List.length! Maybe must add new UnitTest
			assertEquals("presets length change!' => Maybe must add new UnitTest", 9, PresetListView.presets.length);
		}

		{// chk presets[0] 'All Caches'
			assertEquals("presets[0] 'All Caches' => Finds", 0, FilterInstances.ALL.Finds);
			assertEquals("presets[0] 'All Caches' => NotAvailable", 0, FilterInstances.ALL.NotAvailable);
			assertEquals("presets[0] 'All Caches' => Archived", 0, FilterInstances.ALL.Archived);
			assertEquals("presets[0] 'All Caches' => Own", 0, FilterInstances.ALL.Own);
			assertEquals("presets[0] 'All Caches' => ContainsTravelbugs", 0, FilterInstances.ALL.ContainsTravelbugs);
			assertEquals("presets[0] 'All Caches' => Favorites", 0, FilterInstances.ALL.Favorites);
			assertEquals("presets[0] 'All Caches' => HasUserData", 0, FilterInstances.ALL.HasUserData);
			assertEquals("presets[0] 'All Caches' => ListingChanged", 0, FilterInstances.ALL.ListingChanged);
			assertEquals("presets[0] 'All Caches' => WithManualWaypoint", 0, FilterInstances.ALL.WithManualWaypoint);
			assertEquals("presets[0] 'All Caches' => MinDifficulty", 0.0f, FilterInstances.ALL.MinDifficulty);
			assertEquals("presets[0] 'All Caches' => MaxDifficulty", 5.0f, FilterInstances.ALL.MaxDifficulty);
			assertEquals("presets[0] 'All Caches' => MinTerrain", 0.0f, FilterInstances.ALL.MinTerrain);
			assertEquals("presets[0] 'All Caches' => MaxTerrain", 5.0f, FilterInstances.ALL.MaxTerrain);
			assertEquals("presets[0] 'All Caches' => MinContainerSize", 0.0f, FilterInstances.ALL.MinContainerSize);
			assertEquals("presets[0] 'All Caches' => MaxContainerSize", 4.0f, FilterInstances.ALL.MaxContainerSize);
			assertEquals("presets[0] 'All Caches' => MinRating", 0.0f, FilterInstances.ALL.MinRating);
			assertEquals("presets[0] 'All Caches' => MaxRating", 5.0f, FilterInstances.ALL.MaxRating);

			// CacheTypes
			assertEquals("presets[0] 'All Caches' => cacheType.length", 13, FilterInstances.ALL.mCacheTypes.length);
			assertTrue("presets[0] 'All Caches' => cacheType[0]''?", FilterInstances.ALL.mCacheTypes[0]);
			assertTrue("presets[0] 'All Caches' => cacheType[1]''?", FilterInstances.ALL.mCacheTypes[1]);
			assertTrue("presets[0] 'All Caches' => cacheType[2]''?", FilterInstances.ALL.mCacheTypes[2]);
			assertTrue("presets[0] 'All Caches' => cacheType[3]''?", FilterInstances.ALL.mCacheTypes[3]);
			assertTrue("presets[0] 'All Caches' => cacheType[4]''?", FilterInstances.ALL.mCacheTypes[4]);
			assertTrue("presets[0] 'All Caches' => cacheType[5]''?", FilterInstances.ALL.mCacheTypes[5]);
			assertTrue("presets[0] 'All Caches' => cacheType[6]''?", FilterInstances.ALL.mCacheTypes[6]);
			assertTrue("presets[0] 'All Caches' => cacheType[7]''?", FilterInstances.ALL.mCacheTypes[7]);
			assertTrue("presets[0] 'All Caches' => cacheType[8]''?", FilterInstances.ALL.mCacheTypes[8]);
			assertTrue("presets[0] 'All Caches' => cacheType[9]''?", FilterInstances.ALL.mCacheTypes[9]);
			assertTrue("presets[0] 'All Caches' => cacheType[10]''?", FilterInstances.ALL.mCacheTypes[10]);
			assertTrue("presets[0] 'All Caches' => cacheType[11]'Munzee'", FilterInstances.ALL.mCacheTypes[11]);
			assertTrue("presets[0] 'All Caches' => cacheType[12]'GIGA'", FilterInstances.ALL.mCacheTypes[12]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[0] 'All Caches' => attributesFilter.length", AtributeLength, FilterInstances.ALL.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("presets[0] 'All Caches' => attributesFilter[" + i + "]", attributesFilter[i], FilterInstances.ALL.mAttributes[i]);
			}

			assertEquals("presets[0] 'All Caches' => GPXFilenameIds.size", 0, FilterInstances.ALL.GPXFilenameIds.size());
			assertEquals("presets[0] 'All Caches' => Categories.size", 0, FilterInstances.ALL.Categories.size());
			assertEquals("presets[0] 'All Caches' => filterName", "", FilterInstances.ALL.filterName);
			assertEquals("presets[0] 'All Caches' => filterGcCode", "", FilterInstances.ALL.filterGcCode);
			assertEquals("presets[0] 'All Caches' => filterOwner", "", FilterInstances.ALL.filterOwner);

			assertFalse("presets[0] 'All Caches' isExtendsFilter() must by return false", FilterInstances.ALL.isExtendedFilter());

			assertEquals("presets[0] 'All Caches' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterInstances.ALL.toString());

		}

		{// chk presets[1] 'All Caches to find'
			assertEquals("presets[1] 'All Caches to find' => Finds", -1, FilterInstances.ACTIVE.Finds);
			assertEquals("presets[1] 'All Caches to find' => NotAvailable", -1, FilterInstances.ACTIVE.NotAvailable);
			assertEquals("presets[1] 'All Caches to find' => Archived", -1, FilterInstances.ACTIVE.Archived);
			assertEquals("presets[1] 'All Caches to find' => Own", -1, FilterInstances.ACTIVE.Own);
			assertEquals("presets[1] 'All Caches to find' => ContainsTravelbugs", 0, FilterInstances.ACTIVE.ContainsTravelbugs);
			assertEquals("presets[1] 'All Caches to find' => Favorites", 0, FilterInstances.ACTIVE.Favorites);
			assertEquals("presets[1] 'All Caches to find' => HasUserData", 0, FilterInstances.ACTIVE.HasUserData);
			assertEquals("presets[1] 'All Caches to find' => ListingChanged", 0, FilterInstances.ACTIVE.ListingChanged);
			assertEquals("presets[1] 'All Caches to find' => WithManualWaypoint", 0, FilterInstances.ACTIVE.WithManualWaypoint);
			assertEquals("presets[1] 'All Caches to find' => MinDifficulty", 0.0f, FilterInstances.ACTIVE.MinDifficulty);
			assertEquals("presets[1] 'All Caches to find' => MaxDifficulty", 5.0f, FilterInstances.ACTIVE.MaxDifficulty);
			assertEquals("presets[1] 'All Caches to find' => MinTerrain", 0.0f, FilterInstances.ACTIVE.MinTerrain);
			assertEquals("presets[1] 'All Caches to find' => MaxTerrain", 5.0f, FilterInstances.ACTIVE.MaxTerrain);
			assertEquals("presets[1] 'All Caches to find' => MinContainerSize", 0.0f, FilterInstances.ACTIVE.MinContainerSize);
			assertEquals("presets[1] 'All Caches to find' => MaxContainerSize", 4.0f, FilterInstances.ACTIVE.MaxContainerSize);
			assertEquals("presets[1] 'All Caches to find' => MinRating", 0.0f, FilterInstances.ACTIVE.MinRating);
			assertEquals("presets[1] 'All Caches to find' => MaxRating", 5.0f, FilterInstances.ACTIVE.MaxRating);

			// CacheTypes
			assertEquals("presets[1] 'All Caches to find' => cacheType.length", 13, FilterInstances.ACTIVE.mCacheTypes.length);
			assertTrue("presets[1] 'All Caches to find' => cacheType[0]''?", FilterInstances.ACTIVE.mCacheTypes[0]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[1]''?", FilterInstances.ACTIVE.mCacheTypes[1]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[2]''?", FilterInstances.ACTIVE.mCacheTypes[2]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[3]''?", FilterInstances.ACTIVE.mCacheTypes[3]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[4]''?", FilterInstances.ACTIVE.mCacheTypes[4]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[5]''?", FilterInstances.ACTIVE.mCacheTypes[5]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[6]''?", FilterInstances.ACTIVE.mCacheTypes[6]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[7]''?", FilterInstances.ACTIVE.mCacheTypes[7]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[8]''?", FilterInstances.ACTIVE.mCacheTypes[8]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[9]''?", FilterInstances.ACTIVE.mCacheTypes[9]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[10]''?", FilterInstances.ACTIVE.mCacheTypes[10]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[11]'Munzee'", FilterInstances.ACTIVE.mCacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[1] 'All Caches to find' => attributesFilter.length", AtributeLength, FilterInstances.ACTIVE.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("presets[1] 'All Caches to find' => attributesFilter[" + i + "]", attributesFilter[i], FilterInstances.ACTIVE.mAttributes[i]);
			}

			assertEquals("presets[1] 'All Caches to find' => GPXFilenameIds.size", 0, FilterInstances.ACTIVE.GPXFilenameIds.size());
			assertEquals("presets[1] 'All Caches to find' => Categories.size", 0, FilterInstances.ACTIVE.Categories.size());
			assertEquals("presets[1] 'All Caches to find' => filterName", "", FilterInstances.ACTIVE.filterName);
			assertEquals("presets[1] 'All Caches to find' => filterGcCode", "", FilterInstances.ACTIVE.filterGcCode);
			assertEquals("presets[1] 'All Caches to find' => filterOwner", "", FilterInstances.ACTIVE.filterOwner);

			assertFalse("presets[1] 'All Caches to find' isExtendsFilter() must by return false", FilterInstances.ACTIVE.isExtendedFilter());

			assertEquals("presets[1] 'All Caches to find' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterInstances.ACTIVE.toString());

		}

		{// chk presets[2] 'Quick Cache'
			assertEquals("presets[2] 'Quick Cache' => Finds", -1, FilterInstances.QUICK.Finds);
			assertEquals("presets[2] 'Quick Cache' => NotAvailable", -1, FilterInstances.QUICK.NotAvailable);
			assertEquals("presets[2] 'Quick Cache' => Archived", -1, FilterInstances.QUICK.Archived);
			assertEquals("presets[2] 'Quick Cache' => Own", -1, FilterInstances.QUICK.Own);
			assertEquals("presets[2] 'Quick Cache' => ContainsTravelbugs", 0, FilterInstances.QUICK.ContainsTravelbugs);
			assertEquals("presets[2] 'Quick Cache' => Favorites", 0, FilterInstances.QUICK.Favorites);
			assertEquals("presets[2] 'Quick Cache' => HasUserData", 0, FilterInstances.QUICK.HasUserData);
			assertEquals("presets[2] 'Quick Cache' => ListingChanged", 0, FilterInstances.QUICK.ListingChanged);
			assertEquals("presets[2] 'Quick Cache' => WithManualWaypoint", 0, FilterInstances.QUICK.WithManualWaypoint);
			assertEquals("presets[2] 'Quick Cache' => MinDifficulty", 0.0f, FilterInstances.QUICK.MinDifficulty);
			assertEquals("presets[2] 'Quick Cache' => MaxDifficulty", 2.5f, FilterInstances.QUICK.MaxDifficulty);
			assertEquals("presets[2] 'Quick Cache' => MinTerrain", 0.0f, FilterInstances.QUICK.MinTerrain);
			assertEquals("presets[2] 'Quick Cache' => MaxTerrain", 2.5f, FilterInstances.QUICK.MaxTerrain);
			assertEquals("presets[2] 'Quick Cache' => MinContainerSize", 0.0f, FilterInstances.QUICK.MinContainerSize);
			assertEquals("presets[2] 'Quick Cache' => MaxContainerSize", 4.0f, FilterInstances.QUICK.MaxContainerSize);
			assertEquals("presets[2] 'Quick Cache' => MinRating", 0.0f, FilterInstances.QUICK.MinRating);
			assertEquals("presets[2] 'Quick Cache' => MaxRating", 5.0f, FilterInstances.QUICK.MaxRating);

			// CacheTypes
			assertEquals("presets[2] 'Quick Cache' => cacheType.length", 13, FilterInstances.QUICK.mCacheTypes.length);
			assertTrue("presets[2] 'Quick Cache' => cacheType[0]''?", FilterInstances.QUICK.mCacheTypes[0]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[1]''?", FilterInstances.QUICK.mCacheTypes[1]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[2]''?", FilterInstances.QUICK.mCacheTypes[2]);
			assertTrue("presets[2] 'Quick Cache' => cacheType[3]''?", FilterInstances.QUICK.mCacheTypes[3]);
			assertTrue("presets[2] 'Quick Cache' => cacheType[4]''?", FilterInstances.QUICK.mCacheTypes[4]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[5]''?", FilterInstances.QUICK.mCacheTypes[5]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[6]''?", FilterInstances.QUICK.mCacheTypes[6]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[7]''?", FilterInstances.QUICK.mCacheTypes[7]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[8]''?", FilterInstances.QUICK.mCacheTypes[8]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[9]''?", FilterInstances.QUICK.mCacheTypes[9]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[10]''?", FilterInstances.QUICK.mCacheTypes[10]);
			assertTrue("presets[2] 'Quick Cache' => cacheType[11]'Munzee'", FilterInstances.QUICK.mCacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[2] 'Quick Cache' => attributesFilter.length", AtributeLength, FilterInstances.QUICK.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("presets[2] 'Quick Cache' => attributesFilter[" + i + "]", attributesFilter[i], FilterInstances.QUICK.mAttributes[i]);
			}

			assertEquals("presets[2] 'Quick Cache' => GPXFilenameIds.size", 0, FilterInstances.QUICK.GPXFilenameIds.size());
			assertEquals("presets[2] 'Quick Cache' => Categories.size", 0, FilterInstances.QUICK.Categories.size());
			assertEquals("presets[2] 'Quick Cache' => filterName", "", FilterInstances.QUICK.filterName);
			assertEquals("presets[2] 'Quick Cache' => filterGcCode", "", FilterInstances.QUICK.filterGcCode);
			assertEquals("presets[2] 'Quick Cache' => filterOwner", "", FilterInstances.QUICK.filterOwner);

			assertFalse("presets[2] 'Quick Cache' isExtendsFilter() must by return false", FilterInstances.QUICK.isExtendedFilter());

			assertEquals("presets[2] 'Quick Cache' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,0.0,2.5,0.0,2.5,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,false,false,true,true,false,false,false,false,false,false,true,false\",\"filtername\":\"\"}",
					FilterInstances.QUICK.toString());

		}

		{// chk presets[3] 'Fetch some Travelbugs'
			assertEquals("presets[3] 'Fetch some Travelbugs' => Finds", 0, FilterInstances.WITHTB.Finds);
			assertEquals("presets[3] 'Fetch some Travelbugs' => NotAvailable", -1, FilterInstances.WITHTB.NotAvailable);
			assertEquals("presets[3] 'Fetch some Travelbugs' => Archived", -1, FilterInstances.WITHTB.Archived);
			assertEquals("presets[3] 'Fetch some Travelbugs' => Own", 0, FilterInstances.WITHTB.Own);
			assertEquals("presets[3] 'Fetch some Travelbugs' => ContainsTravelbugs", 1, FilterInstances.WITHTB.ContainsTravelbugs);
			assertEquals("presets[3] 'Fetch some Travelbugs' => Favorites", 0, FilterInstances.WITHTB.Favorites);
			assertEquals("presets[3] 'Fetch some Travelbugs' => HasUserData", 0, FilterInstances.WITHTB.HasUserData);
			assertEquals("presets[3] 'Fetch some Travelbugs' => ListingChanged", 0, FilterInstances.WITHTB.ListingChanged);
			assertEquals("presets[3] 'Fetch some Travelbugs' => WithManualWaypoint", 0, FilterInstances.WITHTB.WithManualWaypoint);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MinDifficulty", 0.0f, FilterInstances.WITHTB.MinDifficulty);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MaxDifficulty", 3.0f, FilterInstances.WITHTB.MaxDifficulty);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MinTerrain", 0.0f, FilterInstances.WITHTB.MinTerrain);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MaxTerrain", 3.0f, FilterInstances.WITHTB.MaxTerrain);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MinContainerSize", 0.0f, FilterInstances.WITHTB.MinContainerSize);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MaxContainerSize", 4.0f, FilterInstances.WITHTB.MaxContainerSize);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MinRating", 0.0f, FilterInstances.WITHTB.MinRating);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MaxRating", 5.0f, FilterInstances.WITHTB.MaxRating);

			// CacheTypes
			assertEquals("presets[3] 'Fetch some Travelbugs' => cacheType.length", 13, FilterInstances.WITHTB.mCacheTypes.length);
			assertTrue("presets[3] 'Fetch some Travelbugs' => cacheType[0]''?", FilterInstances.WITHTB.mCacheTypes[0]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[1]''?", FilterInstances.WITHTB.mCacheTypes[1]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[2]''?", FilterInstances.WITHTB.mCacheTypes[2]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[3]''?", FilterInstances.WITHTB.mCacheTypes[3]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[4]''?", FilterInstances.WITHTB.mCacheTypes[4]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[5]''?", FilterInstances.WITHTB.mCacheTypes[5]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[6]''?", FilterInstances.WITHTB.mCacheTypes[6]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[7]''?", FilterInstances.WITHTB.mCacheTypes[7]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[8]''?", FilterInstances.WITHTB.mCacheTypes[8]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[9]''?", FilterInstances.WITHTB.mCacheTypes[9]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[10]''?", FilterInstances.WITHTB.mCacheTypes[10]);
			assertTrue("presets[3] 'Fetch some Travelbugs' => cacheType[11]'Munzee'", FilterInstances.WITHTB.mCacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[3] 'Fetch some Travelbugs' => attributesFilter.length", AtributeLength, FilterInstances.WITHTB.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("presets[3] 'Fetch some Travelbugs' => attributesFilter[" + i + "]", attributesFilter[i], FilterInstances.WITHTB.mAttributes[i]);
			}

			assertEquals("presets[3] 'Fetch some Travelbugs' => GPXFilenameIds.size", 0, FilterInstances.WITHTB.GPXFilenameIds.size());
			assertEquals("presets[3] 'Fetch some Travelbugs' => Categories.size", 0, FilterInstances.WITHTB.Categories.size());
			assertEquals("presets[3] 'Fetch some Travelbugs' => filterName", "", FilterInstances.WITHTB.filterName);
			assertEquals("presets[3] 'Fetch some Travelbugs' => filterGcCode", "", FilterInstances.WITHTB.filterGcCode);
			assertEquals("presets[3] 'Fetch some Travelbugs' => filterOwner", "", FilterInstances.WITHTB.filterOwner);

			assertFalse("presets[3] 'Fetch some Travelbugs' isExtendsFilter() must by return false", FilterInstances.WITHTB.isExtendedFilter());

			assertEquals("presets[3] 'Fetch some Travelbugs' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,-1,-1,0,1,0,0,0,0,0.0,3.0,0.0,3.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,false,false,false,false,false,false,false,false,false,false,true,false\",\"filtername\":\"\"}",
					FilterInstances.WITHTB.toString());

		}

		{// chk presets[4] 'Drop off Travelbugs'
			assertEquals("presets[4] 'Drop off Travelbugs' => Finds", 0, FilterInstances.DROPTB.Finds);
			assertEquals("presets[4] 'Drop off Travelbugs' => NotAvailable", -1, FilterInstances.DROPTB.NotAvailable);
			assertEquals("presets[4] 'Drop off Travelbugs' => Archived", -1, FilterInstances.DROPTB.Archived);
			assertEquals("presets[4] 'Drop off Travelbugs' => Own", 0, FilterInstances.DROPTB.Own);
			assertEquals("presets[4] 'Drop off Travelbugs' => ContainsTravelbugs", 0, FilterInstances.DROPTB.ContainsTravelbugs);
			assertEquals("presets[4] 'Drop off Travelbugs' => Favorites", 0, FilterInstances.DROPTB.Favorites);
			assertEquals("presets[4] 'Drop off Travelbugs' => HasUserData", 0, FilterInstances.DROPTB.HasUserData);
			assertEquals("presets[4] 'Drop off Travelbugs' => ListingChanged", 0, FilterInstances.DROPTB.ListingChanged);
			assertEquals("presets[4] 'Drop off Travelbugs' => WithManualWaypoint", 0, FilterInstances.DROPTB.WithManualWaypoint);
			assertEquals("presets[4] 'Drop off Travelbugs' => MinDifficulty", 0.0f, FilterInstances.DROPTB.MinDifficulty);
			assertEquals("presets[4] 'Drop off Travelbugs' => MaxDifficulty", 3.0f, FilterInstances.DROPTB.MaxDifficulty);
			assertEquals("presets[4] 'Drop off Travelbugs' => MinTerrain", 0.0f, FilterInstances.DROPTB.MinTerrain);
			assertEquals("presets[4] 'Drop off Travelbugs' => MaxTerrain", 3.0f, FilterInstances.DROPTB.MaxTerrain);
			assertEquals("presets[4] 'Drop off Travelbugs' => MinContainerSize", 2.0f, FilterInstances.DROPTB.MinContainerSize);
			assertEquals("presets[4] 'Drop off Travelbugs' => MaxContainerSize", 4.0f, FilterInstances.DROPTB.MaxContainerSize);
			assertEquals("presets[4] 'Drop off Travelbugs' => MinRating", 0.0f, FilterInstances.DROPTB.MinRating);
			assertEquals("presets[4] 'Drop off Travelbugs' => MaxRating", 5.0f, FilterInstances.DROPTB.MaxRating);

			// CacheTypes
			assertEquals("presets[4] 'Drop off Travelbugs' => cacheType.length", 13, FilterInstances.DROPTB.mCacheTypes.length);
			assertTrue("presets[4] 'Drop off Travelbugs' => cacheType[0]''?", FilterInstances.DROPTB.mCacheTypes[0]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[1]''?", FilterInstances.DROPTB.mCacheTypes[1]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[2]''?", FilterInstances.DROPTB.mCacheTypes[2]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[3]''?", FilterInstances.DROPTB.mCacheTypes[3]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[4]''?", FilterInstances.DROPTB.mCacheTypes[4]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[5]''?", FilterInstances.DROPTB.mCacheTypes[5]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[6]''?", FilterInstances.DROPTB.mCacheTypes[6]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[7]''?", FilterInstances.DROPTB.mCacheTypes[7]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[8]''?", FilterInstances.DROPTB.mCacheTypes[8]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[9]''?", FilterInstances.DROPTB.mCacheTypes[9]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[10]''?", FilterInstances.DROPTB.mCacheTypes[10]);
			assertTrue("presets[4] 'Drop off Travelbugs' => cacheType[11]'Munzee'", FilterInstances.DROPTB.mCacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[4] 'Drop off Travelbugs' => attributesFilter.length", AtributeLength, FilterInstances.DROPTB.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("presets[4] 'Drop off Travelbugs' => attributesFilter[" + i + "]", attributesFilter[i], FilterInstances.DROPTB.mAttributes[i]);
			}

			assertEquals("presets[4] 'Drop off Travelbugs' => GPXFilenameIds.size", 0, FilterInstances.DROPTB.GPXFilenameIds.size());
			assertEquals("presets[4] 'Drop off Travelbugs' => Categories.size", 0, FilterInstances.DROPTB.Categories.size());
			assertEquals("presets[4] 'Drop off Travelbugs' => filterName", "", FilterInstances.DROPTB.filterName);
			assertEquals("presets[4] 'Drop off Travelbugs' => filterGcCode", "", FilterInstances.DROPTB.filterGcCode);
			assertEquals("presets[4] 'Drop off Travelbugs' => filterOwner", "", FilterInstances.DROPTB.filterOwner);

			assertFalse("presets[4] 'Drop off Travelbugs' isExtendsFilter() must by return false", FilterInstances.DROPTB.isExtendedFilter());

			assertEquals("presets[4] 'Drop off Travelbugs' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,-1,-1,0,0,0,0,0,0,0.0,3.0,0.0,3.0,2.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,false,false,false,false,false,false,false,false,false,false,true,false\",\"filtername\":\"\"}",
					FilterInstances.DROPTB.toString());

		}

		{// chk presets[5] 'Highlights'
			assertEquals("presets[5] 'Highlights' => Finds", -1, FilterInstances.HIGHLIGHTS.Finds);
			assertEquals("presets[5] 'Highlights' => NotAvailable", -1, FilterInstances.HIGHLIGHTS.NotAvailable);
			assertEquals("presets[5] 'Highlights' => Archived", -1, FilterInstances.HIGHLIGHTS.Archived);
			assertEquals("presets[5] 'Highlights' => Own", 0, FilterInstances.HIGHLIGHTS.Own);
			assertEquals("presets[5] 'Highlights' => ContainsTravelbugs", 0, FilterInstances.HIGHLIGHTS.ContainsTravelbugs);
			assertEquals("presets[5] 'Highlights' => Favorites", 0, FilterInstances.HIGHLIGHTS.Favorites);
			assertEquals("presets[5] 'Highlights' => HasUserData", 0, FilterInstances.HIGHLIGHTS.HasUserData);
			assertEquals("presets[5] 'Highlights' => ListingChanged", 0, FilterInstances.HIGHLIGHTS.ListingChanged);
			assertEquals("presets[5] 'Highlights' => WithManualWaypoint", 0, FilterInstances.HIGHLIGHTS.WithManualWaypoint);
			assertEquals("presets[5] 'Highlights' => MinDifficulty", 0.0f, FilterInstances.HIGHLIGHTS.MinDifficulty);
			assertEquals("presets[5] 'Highlights' => MaxDifficulty", 5.0f, FilterInstances.HIGHLIGHTS.MaxDifficulty);
			assertEquals("presets[5] 'Highlights' => MinTerrain", 0.0f, FilterInstances.HIGHLIGHTS.MinTerrain);
			assertEquals("presets[5] 'Highlights' => MaxTerrain", 5.0f, FilterInstances.HIGHLIGHTS.MaxTerrain);
			assertEquals("presets[5] 'Highlights' => MinContainerSize", 0.0f, FilterInstances.HIGHLIGHTS.MinContainerSize);
			assertEquals("presets[5] 'Highlights' => MaxContainerSize", 4.0f, FilterInstances.HIGHLIGHTS.MaxContainerSize);
			assertEquals("presets[5] 'Highlights' => MinRating", 3.5f, FilterInstances.HIGHLIGHTS.MinRating);
			assertEquals("presets[5] 'Highlights' => MaxRating", 5.0f, FilterInstances.HIGHLIGHTS.MaxRating);

			// CacheTypes
			assertEquals("presets[5] 'Highlights' => cacheType.length", 13, FilterInstances.HIGHLIGHTS.mCacheTypes.length);
			assertTrue("presets[5] 'Highlights' => cacheType[0]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[0]);
			assertTrue("presets[5] 'Highlights' => cacheType[1]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[1]);
			assertTrue("presets[5] 'Highlights' => cacheType[2]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[2]);
			assertTrue("presets[5] 'Highlights' => cacheType[3]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[3]);
			assertTrue("presets[5] 'Highlights' => cacheType[4]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[4]);
			assertTrue("presets[5] 'Highlights' => cacheType[5]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[5]);
			assertTrue("presets[5] 'Highlights' => cacheType[6]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[6]);
			assertTrue("presets[5] 'Highlights' => cacheType[7]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[7]);
			assertTrue("presets[5] 'Highlights' => cacheType[8]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[8]);
			assertTrue("presets[5] 'Highlights' => cacheType[9]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[9]);
			assertTrue("presets[5] 'Highlights' => cacheType[10]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[10]);
			assertTrue("presets[5] 'Highlights' => cacheType[11]'Munzee'", FilterInstances.HIGHLIGHTS.mCacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[5] 'Highlights' => attributesFilter.length", AtributeLength, FilterInstances.HIGHLIGHTS.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("presets[5] 'Highlights' => attributesFilter[" + i + "]", attributesFilter[i], FilterInstances.HIGHLIGHTS.mAttributes[i]);
			}

			assertEquals("presets[5] 'Highlights' => GPXFilenameIds.size", 0, FilterInstances.HIGHLIGHTS.GPXFilenameIds.size());
			assertEquals("presets[5] 'Highlights' => Categories.size", 0, FilterInstances.HIGHLIGHTS.Categories.size());
			assertEquals("presets[5] 'Highlights' => filterName", "", FilterInstances.HIGHLIGHTS.filterName);
			assertEquals("presets[5] 'Highlights' => filterGcCode", "", FilterInstances.HIGHLIGHTS.filterGcCode);
			assertEquals("presets[5] 'Highlights' => filterOwner", "", FilterInstances.HIGHLIGHTS.filterOwner);

			assertFalse("presets[5] 'Highlights' isExtendsFilter() must by return false", FilterInstances.HIGHLIGHTS.isExtendedFilter());

			assertEquals("presets[5] 'Highlights' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"-1,-1,-1,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,3.5,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterInstances.HIGHLIGHTS.toString());

		}

		{// chk presets[6] 'Favoriten'
			assertEquals("presets[6] 'Favoriten' => Finds", 0, FilterInstances.FAVORITES.Finds);
			assertEquals("presets[6] 'Favoriten' => NotAvailable", 0, FilterInstances.FAVORITES.NotAvailable);
			assertEquals("presets[6] 'Favoriten' => Archived", 0, FilterInstances.FAVORITES.Archived);
			assertEquals("presets[6] 'Favoriten' => Own", 0, FilterInstances.FAVORITES.Own);
			assertEquals("presets[6] 'Favoriten' => ContainsTravelbugs", 0, FilterInstances.FAVORITES.ContainsTravelbugs);
			assertEquals("presets[6] 'Favoriten' => Favorites", 1, FilterInstances.FAVORITES.Favorites);
			assertEquals("presets[6] 'Favoriten' => HasUserData", 0, FilterInstances.FAVORITES.HasUserData);
			assertEquals("presets[6] 'Favoriten' => ListingChanged", 0, FilterInstances.FAVORITES.ListingChanged);
			assertEquals("presets[6] 'Favoriten' => WithManualWaypoint", 0, FilterInstances.FAVORITES.WithManualWaypoint);
			assertEquals("presets[6] 'Favoriten' => MinDifficulty", 0.0f, FilterInstances.FAVORITES.MinDifficulty);
			assertEquals("presets[6] 'Favoriten' => MaxDifficulty", 5.0f, FilterInstances.FAVORITES.MaxDifficulty);
			assertEquals("presets[6] 'Favoriten' => MinTerrain", 0.0f, FilterInstances.FAVORITES.MinTerrain);
			assertEquals("presets[6] 'Favoriten' => MaxTerrain", 5.0f, FilterInstances.FAVORITES.MaxTerrain);
			assertEquals("presets[6] 'Favoriten' => MinContainerSize", 0.0f, FilterInstances.FAVORITES.MinContainerSize);
			assertEquals("presets[6] 'Favoriten' => MaxContainerSize", 4.0f, FilterInstances.FAVORITES.MaxContainerSize);
			assertEquals("presets[6] 'Favoriten' => MinRating", 0.0f, FilterInstances.FAVORITES.MinRating);
			assertEquals("presets[6] 'Favoriten' => MaxRating", 5.0f, FilterInstances.FAVORITES.MaxRating);

			// CacheTypes
			assertEquals("presets[6] 'Favoriten' => cacheType.length", 13, FilterInstances.FAVORITES.mCacheTypes.length);
			assertTrue("presets[6] 'Favoriten' => cacheType[0]''?", FilterInstances.FAVORITES.mCacheTypes[0]);
			assertTrue("presets[6] 'Favoriten' => cacheType[1]''?", FilterInstances.FAVORITES.mCacheTypes[1]);
			assertTrue("presets[6] 'Favoriten' => cacheType[2]''?", FilterInstances.FAVORITES.mCacheTypes[2]);
			assertTrue("presets[6] 'Favoriten' => cacheType[3]''?", FilterInstances.FAVORITES.mCacheTypes[3]);
			assertTrue("presets[6] 'Favoriten' => cacheType[4]''?", FilterInstances.FAVORITES.mCacheTypes[4]);
			assertTrue("presets[6] 'Favoriten' => cacheType[5]''?", FilterInstances.FAVORITES.mCacheTypes[5]);
			assertTrue("presets[6] 'Favoriten' => cacheType[6]''?", FilterInstances.FAVORITES.mCacheTypes[6]);
			assertTrue("presets[6] 'Favoriten' => cacheType[7]''?", FilterInstances.FAVORITES.mCacheTypes[7]);
			assertTrue("presets[6] 'Favoriten' => cacheType[8]''?", FilterInstances.FAVORITES.mCacheTypes[8]);
			assertTrue("presets[6] 'Favoriten' => cacheType[9]''?", FilterInstances.FAVORITES.mCacheTypes[9]);
			assertTrue("presets[6] 'Favoriten' => cacheType[10]''?", FilterInstances.FAVORITES.mCacheTypes[10]);
			assertTrue("presets[6] 'Favoriten' => cacheType[11]'Munzee'", FilterInstances.FAVORITES.mCacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[6] 'Favoriten' => attributesFilter.length", AtributeLength, FilterInstances.FAVORITES.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("presets[6] 'Favoriten' => attributesFilter[" + i + "]", attributesFilter[i], FilterInstances.FAVORITES.mAttributes[i]);
			}

			assertEquals("presets[6] 'Favoriten' => GPXFilenameIds.size", 0, FilterInstances.FAVORITES.GPXFilenameIds.size());
			assertEquals("presets[6] 'Favoriten' => Categories.size", 0, FilterInstances.FAVORITES.Categories.size());
			assertEquals("presets[6] 'Favoriten' => filterName", "", FilterInstances.FAVORITES.filterName);
			assertEquals("presets[6] 'Favoriten' => filterGcCode", "", FilterInstances.FAVORITES.filterGcCode);
			assertEquals("presets[6] 'Favoriten' => filterOwner", "", FilterInstances.FAVORITES.filterOwner);

			assertFalse("presets[6] 'Favoriten' isExtendsFilter() must by return false", FilterInstances.FAVORITES.isExtendedFilter());

			assertEquals("presets[6] 'Favoriten' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,1,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterInstances.FAVORITES.toString());

		}

		{// chk presets[7] 'prepare to archive'
			assertEquals("presets[7] 'prepare to archive' => Finds", 0, FilterInstances.TOARCHIVE.Finds);
			assertEquals("presets[7] 'prepare to archive' => NotAvailable", 0, FilterInstances.TOARCHIVE.NotAvailable);
			assertEquals("presets[7] 'prepare to archive' => Archived", -1, FilterInstances.TOARCHIVE.Archived);
			assertEquals("presets[7] 'prepare to archive' => Own", -1, FilterInstances.TOARCHIVE.Own);
			assertEquals("presets[7] 'prepare to archive' => ContainsTravelbugs", 0, FilterInstances.TOARCHIVE.ContainsTravelbugs);
			assertEquals("presets[7] 'prepare to archive' => Favorites", -1, FilterInstances.TOARCHIVE.Favorites);
			assertEquals("presets[7] 'prepare to archive' => HasUserData", -1, FilterInstances.TOARCHIVE.HasUserData);
			assertEquals("presets[7] 'prepare to archive' => ListingChanged", -1, FilterInstances.TOARCHIVE.ListingChanged);
			assertEquals("presets[7] 'prepare to archive' => WithManualWaypoint", 0, FilterInstances.TOARCHIVE.WithManualWaypoint);
			assertEquals("presets[7] 'prepare to archive' => MinDifficulty", 0.0f, FilterInstances.TOARCHIVE.MinDifficulty);
			assertEquals("presets[7] 'prepare to archive' => MaxDifficulty", 5.0f, FilterInstances.TOARCHIVE.MaxDifficulty);
			assertEquals("presets[7] 'prepare to archive' => MinTerrain", 0.0f, FilterInstances.TOARCHIVE.MinTerrain);
			assertEquals("presets[7] 'prepare to archive' => MaxTerrain", 5.0f, FilterInstances.TOARCHIVE.MaxTerrain);
			assertEquals("presets[7] 'prepare to archive' => MinContainerSize", 0.0f, FilterInstances.TOARCHIVE.MinContainerSize);
			assertEquals("presets[7] 'prepare to archive' => MaxContainerSize", 4.0f, FilterInstances.TOARCHIVE.MaxContainerSize);
			assertEquals("presets[7] 'prepare to archive' => MinRating", 0.0f, FilterInstances.TOARCHIVE.MinRating);
			assertEquals("presets[7] 'prepare to archive' => MaxRating", 5.0f, FilterInstances.TOARCHIVE.MaxRating);

			// CacheTypes
			assertEquals("presets[7] 'prepare to archive' => cacheType.length", 13, FilterInstances.TOARCHIVE.mCacheTypes.length);
			assertTrue("presets[7] 'prepare to archive' => cacheType[0]''?", FilterInstances.TOARCHIVE.mCacheTypes[0]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[1]''?", FilterInstances.TOARCHIVE.mCacheTypes[1]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[2]''?", FilterInstances.TOARCHIVE.mCacheTypes[2]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[3]''?", FilterInstances.TOARCHIVE.mCacheTypes[3]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[4]''?", FilterInstances.TOARCHIVE.mCacheTypes[4]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[5]''?", FilterInstances.TOARCHIVE.mCacheTypes[5]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[6]''?", FilterInstances.TOARCHIVE.mCacheTypes[6]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[7]''?", FilterInstances.TOARCHIVE.mCacheTypes[7]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[8]''?", FilterInstances.TOARCHIVE.mCacheTypes[8]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[9]''?", FilterInstances.TOARCHIVE.mCacheTypes[9]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[10]''?", FilterInstances.TOARCHIVE.mCacheTypes[10]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[11]'Munzee'", FilterInstances.TOARCHIVE.mCacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[7] 'prepare to archive' => attributesFilter.length", AtributeLength, FilterInstances.TOARCHIVE.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("presets[7] 'prepare to archive' => attributesFilter[" + i + "]", attributesFilter[i], FilterInstances.TOARCHIVE.mAttributes[i]);
			}

			assertEquals("presets[7] 'prepare to archive' => GPXFilenameIds.size", 0, FilterInstances.TOARCHIVE.GPXFilenameIds.size());
			assertEquals("presets[7] 'prepare to archive' => Categories.size", 0, FilterInstances.TOARCHIVE.Categories.size());
			assertEquals("presets[7] 'prepare to archive' => filterName", "", FilterInstances.TOARCHIVE.filterName);
			assertEquals("presets[7] 'prepare to archive' => filterGcCode", "", FilterInstances.TOARCHIVE.filterGcCode);
			assertEquals("presets[7] 'prepare to archive' => filterOwner", "", FilterInstances.TOARCHIVE.filterOwner);

			assertFalse("presets[7] 'prepare to archive' isExtendsFilter() must by return false", FilterInstances.TOARCHIVE.isExtendedFilter());

			assertEquals("presets[7] 'prepare to archive' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,-1,-1,0,-1,-1,-1,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterInstances.TOARCHIVE.toString());

		}

		{// chk presets[8] 'Listing Changed'
			assertEquals("presets[8] 'Listing Changed' => Finds", 0, FilterInstances.LISTINGCHANGED.Finds);
			assertEquals("presets[8] 'Listing Changed' => NotAvailable", 0, FilterInstances.LISTINGCHANGED.NotAvailable);
			assertEquals("presets[8] 'Listing Changed' => Archived", 0, FilterInstances.LISTINGCHANGED.Archived);
			assertEquals("presets[8] 'Listing Changed' => Own", 0, FilterInstances.LISTINGCHANGED.Own);
			assertEquals("presets[8] 'Listing Changed' => ContainsTravelbugs", 0, FilterInstances.LISTINGCHANGED.ContainsTravelbugs);
			assertEquals("presets[8] 'Listing Changed' => Favorites", 0, FilterInstances.LISTINGCHANGED.Favorites);
			assertEquals("presets[8] 'Listing Changed' => HasUserData", 0, FilterInstances.LISTINGCHANGED.HasUserData);
			assertEquals("presets[8] 'Listing Changed' => ListingChanged", 1, FilterInstances.LISTINGCHANGED.ListingChanged);
			assertEquals("presets[8] 'Listing Changed' => WithManualWaypoint", 0, FilterInstances.LISTINGCHANGED.WithManualWaypoint);
			assertEquals("presets[8] 'Listing Changed' => MinDifficulty", 0.0f, FilterInstances.LISTINGCHANGED.MinDifficulty);
			assertEquals("presets[8] 'Listing Changed' => MaxDifficulty", 5.0f, FilterInstances.LISTINGCHANGED.MaxDifficulty);
			assertEquals("presets[8] 'Listing Changed' => MinTerrain", 0.0f, FilterInstances.LISTINGCHANGED.MinTerrain);
			assertEquals("presets[8] 'Listing Changed' => MaxTerrain", 5.0f, FilterInstances.LISTINGCHANGED.MaxTerrain);
			assertEquals("presets[8] 'Listing Changed' => MinContainerSize", 0.0f, FilterInstances.LISTINGCHANGED.MinContainerSize);
			assertEquals("presets[8] 'Listing Changed' => MaxContainerSize", 4.0f, FilterInstances.LISTINGCHANGED.MaxContainerSize);
			assertEquals("presets[8] 'Listing Changed' => MinRating", 0.0f, FilterInstances.LISTINGCHANGED.MinRating);
			assertEquals("presets[8] 'Listing Changed' => MaxRating", 5.0f, FilterInstances.LISTINGCHANGED.MaxRating);

			// CacheTypes
			assertEquals("presets[8] 'Listing Changed' => cacheType.length", 13, FilterInstances.LISTINGCHANGED.mCacheTypes.length);
			assertTrue("presets[8] 'Listing Changed' => cacheType[0]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[0]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[1]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[1]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[2]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[2]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[3]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[3]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[4]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[4]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[5]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[5]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[6]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[6]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[7]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[7]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[8]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[8]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[9]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[9]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[10]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[10]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[11]'Munzee'", FilterInstances.LISTINGCHANGED.mCacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[8] 'Listing Changed' => attributesFilter.length", AtributeLength, FilterInstances.LISTINGCHANGED.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("presets[8] 'Listing Changed' => attributesFilter[" + i + "]", attributesFilter[i], FilterInstances.LISTINGCHANGED.mAttributes[i]);
			}

			assertEquals("presets[8] 'Listing Changed' => GPXFilenameIds.size", 0, FilterInstances.LISTINGCHANGED.GPXFilenameIds.size());
			assertEquals("presets[8] 'Listing Changed' => Categories.size", 0, FilterInstances.LISTINGCHANGED.Categories.size());
			assertEquals("presets[8] 'Listing Changed' => filterName", "", FilterInstances.LISTINGCHANGED.filterName);
			assertEquals("presets[8] 'Listing Changed' => filterGcCode", "", FilterInstances.LISTINGCHANGED.filterGcCode);
			assertEquals("presets[8] 'Listing Changed' => filterOwner", "", FilterInstances.LISTINGCHANGED.filterOwner);

			assertFalse("presets[8] 'Listing Changed' isExtendsFilter() must by return false", FilterInstances.LISTINGCHANGED.isExtendedFilter());

			assertEquals("presets[8] 'Listing Changed' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,0,0,1,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterInstances.LISTINGCHANGED.toString());

		}

		{// chk default constructor

			FilterProperties defaultCtor = new FilterProperties();

			assertEquals("default constructor => Finds", 0, defaultCtor.Finds);
			assertEquals("default constructor => NotAvailable", 0, defaultCtor.NotAvailable);
			assertEquals("default constructor => Archived", 0, defaultCtor.Archived);
			assertEquals("default constructor => Own", 0, defaultCtor.Own);
			assertEquals("default constructor => ContainsTravelbugs", 0, defaultCtor.ContainsTravelbugs);
			assertEquals("default constructor => Favorites", 0, defaultCtor.Favorites);
			assertEquals("default constructor => HasUserData", 0, defaultCtor.HasUserData);
			assertEquals("default constructor => ListingChanged", 0, defaultCtor.ListingChanged);
			assertEquals("default constructor => WithManualWaypoint", 0, defaultCtor.WithManualWaypoint);
			assertEquals("default constructor => MinDifficulty", 0.0f, defaultCtor.MinDifficulty);
			assertEquals("default constructor => MaxDifficulty", 5.0f, defaultCtor.MaxDifficulty);
			assertEquals("default constructor => MinTerrain", 0.0f, defaultCtor.MinTerrain);
			assertEquals("default constructor => MaxTerrain", 5.0f, defaultCtor.MaxTerrain);
			assertEquals("default constructor => MinContainerSize", 0.0f, defaultCtor.MinContainerSize);
			assertEquals("default constructor => MaxContainerSize", 4.0f, defaultCtor.MaxContainerSize);
			assertEquals("default constructor => MinRating", 0.0f, defaultCtor.MinRating);
			assertEquals("default constructor => MaxRating", 5.0f, defaultCtor.MaxRating);

			// CacheTypes
			assertEquals("default constructor => cacheType.length", 13, defaultCtor.mCacheTypes.length);
			assertTrue("default constructor => cacheType[0]''?", defaultCtor.mCacheTypes[0]);
			assertTrue("default constructor => cacheType[1]''?", defaultCtor.mCacheTypes[1]);
			assertTrue("default constructor => cacheType[2]''?", defaultCtor.mCacheTypes[2]);
			assertTrue("default constructor => cacheType[3]''?", defaultCtor.mCacheTypes[3]);
			assertTrue("default constructor => cacheType[4]''?", defaultCtor.mCacheTypes[4]);
			assertTrue("default constructor => cacheType[5]''?", defaultCtor.mCacheTypes[5]);
			assertTrue("default constructor => cacheType[6]''?", defaultCtor.mCacheTypes[6]);
			assertTrue("default constructor => cacheType[7]''?", defaultCtor.mCacheTypes[7]);
			assertTrue("default constructor => cacheType[8]''?", defaultCtor.mCacheTypes[8]);
			assertTrue("default constructor => cacheType[9]''?", defaultCtor.mCacheTypes[9]);
			assertTrue("default constructor => cacheType[10]''?", defaultCtor.mCacheTypes[10]);
			assertTrue("default constructor => cacheType[11]'Munzee'", defaultCtor.mCacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("default constructor => attributesFilter.length", AtributeLength, defaultCtor.mAttributes.length);

			int[] attributesFilter = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++) {
				assertEquals("default constructor => attributesFilter[" + i + "]", attributesFilter[i], defaultCtor.mAttributes[i]);
			}

			assertEquals("default constructor => GPXFilenameIds.size", 0, defaultCtor.GPXFilenameIds.size());
			assertEquals("default constructor => Categories.size", 0, defaultCtor.Categories.size());
			assertEquals("default constructor => filterName", "", defaultCtor.filterName);
			assertEquals("default constructor => filterGcCode", "", defaultCtor.filterGcCode);
			assertEquals("default constructor => filterOwner", "", defaultCtor.filterOwner);

			assertFalse("default constructor isExtendsFilter() must by return false", defaultCtor.isExtendedFilter());

			assertEquals("default constructor =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					defaultCtor.toString());

		}
	}

	public void test_ChkConstructorAndEquals() {
		int presetSize = PresetListView.presets.length;
		String[] jsonStringList = new String[presetSize];
		FilterProperties[] chkList = new FilterProperties[presetSize];
		for (int i = 0; i < presetSize; i++) {
			jsonStringList[i] = PresetListView.presets[i].toString();
			chkList[i] = new FilterProperties(jsonStringList[i]);
			assertTrue("Constructor or Equals Failures with Preset:" + i, chkList[i].equals(PresetListView.presets[i]));
		}
	}

	public void test_ChkSqlWhere() {
		int presetSize = PresetListView.presets.length;
		String[] SqlStringList = new String[] {
				"Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
				"(Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='User') and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
				"(Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='User') and Difficulty >= 0.0 and Difficulty <= 5.0 and Terrain >= 0.0 and Terrain <= 5.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,3,4,21) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
				"Available=1 and Archived=0 and NumTravelbugs > 0 and Difficulty >= 0.0 and Difficulty <= 6.0 and Terrain >= 0.0 and Terrain <= 6.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,21) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
				"Available=1 and Archived=0 and Difficulty >= 0.0 and Difficulty <= 6.0 and Terrain >= 0.0 and Terrain <= 6.0 and Size >= 2.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,21) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
				"(Found=0 or Found is null) and Available=1 and Archived=0 and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 350.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
				"Favorit=1 and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
				"Archived=0 and (not Owner='User') and (Favorit=0 or Favorit is null) and (HasUserData = 0 or HasUserData is null) and (ListingChanged=0 or ListingChanged is null) and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
				"ListingChanged=1 and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )" };

		for (int i = 0; i < presetSize; i++) {
			assertEquals("presets[" + i + "] '=>getSqlWhere(\"User\")", SqlStringList[i], PresetListView.presets[i].getSqlWhere("User"));
		}
	}
}
