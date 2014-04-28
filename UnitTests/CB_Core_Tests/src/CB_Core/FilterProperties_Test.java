package CB_Core;

import junit.framework.TestCase;

public class FilterProperties_Test extends TestCase
{
	public void test_ChkPresets()
	{

		{// chk Preset List.length! Maybe must add new UnitTest
			assertEquals("presets length change!' => Maybe must add new UnitTest", 9, FilterProperties.presets.length);
		}

		{// chk presets[0] 'All Caches'
			assertEquals("presets[0] 'All Caches' => Finds", 0, FilterProperties.presets[0].Finds);
			assertEquals("presets[0] 'All Caches' => NotAvailable", 0, FilterProperties.presets[0].NotAvailable);
			assertEquals("presets[0] 'All Caches' => Archived", 0, FilterProperties.presets[0].Archived);
			assertEquals("presets[0] 'All Caches' => Own", 0, FilterProperties.presets[0].Own);
			assertEquals("presets[0] 'All Caches' => ContainsTravelbugs", 0, FilterProperties.presets[0].ContainsTravelbugs);
			assertEquals("presets[0] 'All Caches' => Favorites", 0, FilterProperties.presets[0].Favorites);
			assertEquals("presets[0] 'All Caches' => HasUserData", 0, FilterProperties.presets[0].HasUserData);
			assertEquals("presets[0] 'All Caches' => ListingChanged", 0, FilterProperties.presets[0].ListingChanged);
			assertEquals("presets[0] 'All Caches' => WithManualWaypoint", 0, FilterProperties.presets[0].WithManualWaypoint);
			assertEquals("presets[0] 'All Caches' => MinDifficulty", 0.0f, FilterProperties.presets[0].MinDifficulty);
			assertEquals("presets[0] 'All Caches' => MaxDifficulty", 5.0f, FilterProperties.presets[0].MaxDifficulty);
			assertEquals("presets[0] 'All Caches' => MinTerrain", 0.0f, FilterProperties.presets[0].MinTerrain);
			assertEquals("presets[0] 'All Caches' => MaxTerrain", 5.0f, FilterProperties.presets[0].MaxTerrain);
			assertEquals("presets[0] 'All Caches' => MinContainerSize", 0.0f, FilterProperties.presets[0].MinContainerSize);
			assertEquals("presets[0] 'All Caches' => MaxContainerSize", 4.0f, FilterProperties.presets[0].MaxContainerSize);
			assertEquals("presets[0] 'All Caches' => MinRating", 0.0f, FilterProperties.presets[0].MinRating);
			assertEquals("presets[0] 'All Caches' => MaxRating", 5.0f, FilterProperties.presets[0].MaxRating);

			// CacheTypes
			assertEquals("presets[0] 'All Caches' => cacheType.length", 13, FilterProperties.presets[0].cacheTypes.length);
			assertTrue("presets[0] 'All Caches' => cacheType[0]''?", FilterProperties.presets[0].cacheTypes[0]);
			assertTrue("presets[0] 'All Caches' => cacheType[1]''?", FilterProperties.presets[0].cacheTypes[1]);
			assertTrue("presets[0] 'All Caches' => cacheType[2]''?", FilterProperties.presets[0].cacheTypes[2]);
			assertTrue("presets[0] 'All Caches' => cacheType[3]''?", FilterProperties.presets[0].cacheTypes[3]);
			assertTrue("presets[0] 'All Caches' => cacheType[4]''?", FilterProperties.presets[0].cacheTypes[4]);
			assertTrue("presets[0] 'All Caches' => cacheType[5]''?", FilterProperties.presets[0].cacheTypes[5]);
			assertTrue("presets[0] 'All Caches' => cacheType[6]''?", FilterProperties.presets[0].cacheTypes[6]);
			assertTrue("presets[0] 'All Caches' => cacheType[7]''?", FilterProperties.presets[0].cacheTypes[7]);
			assertTrue("presets[0] 'All Caches' => cacheType[8]''?", FilterProperties.presets[0].cacheTypes[8]);
			assertTrue("presets[0] 'All Caches' => cacheType[9]''?", FilterProperties.presets[0].cacheTypes[9]);
			assertTrue("presets[0] 'All Caches' => cacheType[10]''?", FilterProperties.presets[0].cacheTypes[10]);
			assertTrue("presets[0] 'All Caches' => cacheType[11]'Munzee'", FilterProperties.presets[0].cacheTypes[11]);
			assertTrue("presets[0] 'All Caches' => cacheType[12]'GIGA'", FilterProperties.presets[0].cacheTypes[12]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[0] 'All Caches' => attributesFilter.length", AtributeLength,
					FilterProperties.presets[0].attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("presets[0] 'All Caches' => attributesFilter[" + i + "]", attributesFilter[i],
						FilterProperties.presets[0].attributesFilter[i]);
			}

			assertEquals("presets[0] 'All Caches' => GPXFilenameIds.size", 0, FilterProperties.presets[0].GPXFilenameIds.size());
			assertEquals("presets[0] 'All Caches' => Categories.size", 0, FilterProperties.presets[0].Categories.size());
			assertEquals("presets[0] 'All Caches' => filterName", "", FilterProperties.presets[0].filterName);
			assertEquals("presets[0] 'All Caches' => filterGcCode", "", FilterProperties.presets[0].filterGcCode);
			assertEquals("presets[0] 'All Caches' => filterOwner", "", FilterProperties.presets[0].filterOwner);

			assertFalse("presets[0] 'All Caches' isExtendsFilter() must by return false", FilterProperties.presets[0].isExtendsFilter());

			assertEquals(
					"presets[0] 'All Caches' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterProperties.presets[0].toString());

		}

		{// chk presets[1] 'All Caches to find'
			assertEquals("presets[1] 'All Caches to find' => Finds", -1, FilterProperties.presets[1].Finds);
			assertEquals("presets[1] 'All Caches to find' => NotAvailable", -1, FilterProperties.presets[1].NotAvailable);
			assertEquals("presets[1] 'All Caches to find' => Archived", -1, FilterProperties.presets[1].Archived);
			assertEquals("presets[1] 'All Caches to find' => Own", -1, FilterProperties.presets[1].Own);
			assertEquals("presets[1] 'All Caches to find' => ContainsTravelbugs", 0, FilterProperties.presets[1].ContainsTravelbugs);
			assertEquals("presets[1] 'All Caches to find' => Favorites", 0, FilterProperties.presets[1].Favorites);
			assertEquals("presets[1] 'All Caches to find' => HasUserData", 0, FilterProperties.presets[1].HasUserData);
			assertEquals("presets[1] 'All Caches to find' => ListingChanged", 0, FilterProperties.presets[1].ListingChanged);
			assertEquals("presets[1] 'All Caches to find' => WithManualWaypoint", 0, FilterProperties.presets[1].WithManualWaypoint);
			assertEquals("presets[1] 'All Caches to find' => MinDifficulty", 0.0f, FilterProperties.presets[1].MinDifficulty);
			assertEquals("presets[1] 'All Caches to find' => MaxDifficulty", 5.0f, FilterProperties.presets[1].MaxDifficulty);
			assertEquals("presets[1] 'All Caches to find' => MinTerrain", 0.0f, FilterProperties.presets[1].MinTerrain);
			assertEquals("presets[1] 'All Caches to find' => MaxTerrain", 5.0f, FilterProperties.presets[1].MaxTerrain);
			assertEquals("presets[1] 'All Caches to find' => MinContainerSize", 0.0f, FilterProperties.presets[1].MinContainerSize);
			assertEquals("presets[1] 'All Caches to find' => MaxContainerSize", 4.0f, FilterProperties.presets[1].MaxContainerSize);
			assertEquals("presets[1] 'All Caches to find' => MinRating", 0.0f, FilterProperties.presets[1].MinRating);
			assertEquals("presets[1] 'All Caches to find' => MaxRating", 5.0f, FilterProperties.presets[1].MaxRating);

			// CacheTypes
			assertEquals("presets[1] 'All Caches to find' => cacheType.length", 13, FilterProperties.presets[1].cacheTypes.length);
			assertTrue("presets[1] 'All Caches to find' => cacheType[0]''?", FilterProperties.presets[1].cacheTypes[0]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[1]''?", FilterProperties.presets[1].cacheTypes[1]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[2]''?", FilterProperties.presets[1].cacheTypes[2]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[3]''?", FilterProperties.presets[1].cacheTypes[3]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[4]''?", FilterProperties.presets[1].cacheTypes[4]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[5]''?", FilterProperties.presets[1].cacheTypes[5]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[6]''?", FilterProperties.presets[1].cacheTypes[6]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[7]''?", FilterProperties.presets[1].cacheTypes[7]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[8]''?", FilterProperties.presets[1].cacheTypes[8]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[9]''?", FilterProperties.presets[1].cacheTypes[9]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[10]''?", FilterProperties.presets[1].cacheTypes[10]);
			assertTrue("presets[1] 'All Caches to find' => cacheType[11]'Munzee'", FilterProperties.presets[1].cacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[1] 'All Caches to find' => attributesFilter.length", AtributeLength,
					FilterProperties.presets[1].attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("presets[1] 'All Caches to find' => attributesFilter[" + i + "]", attributesFilter[i],
						FilterProperties.presets[1].attributesFilter[i]);
			}

			assertEquals("presets[1] 'All Caches to find' => GPXFilenameIds.size", 0, FilterProperties.presets[1].GPXFilenameIds.size());
			assertEquals("presets[1] 'All Caches to find' => Categories.size", 0, FilterProperties.presets[1].Categories.size());
			assertEquals("presets[1] 'All Caches to find' => filterName", "", FilterProperties.presets[1].filterName);
			assertEquals("presets[1] 'All Caches to find' => filterGcCode", "", FilterProperties.presets[1].filterGcCode);
			assertEquals("presets[1] 'All Caches to find' => filterOwner", "", FilterProperties.presets[1].filterOwner);

			assertFalse("presets[1] 'All Caches to find' isExtendsFilter() must by return false",
					FilterProperties.presets[1].isExtendsFilter());

			assertEquals(
					"presets[1] 'All Caches to find' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterProperties.presets[1].toString());

		}

		{// chk presets[2] 'Quick Cache'
			assertEquals("presets[2] 'Quick Cache' => Finds", -1, FilterProperties.presets[2].Finds);
			assertEquals("presets[2] 'Quick Cache' => NotAvailable", -1, FilterProperties.presets[2].NotAvailable);
			assertEquals("presets[2] 'Quick Cache' => Archived", -1, FilterProperties.presets[2].Archived);
			assertEquals("presets[2] 'Quick Cache' => Own", -1, FilterProperties.presets[2].Own);
			assertEquals("presets[2] 'Quick Cache' => ContainsTravelbugs", 0, FilterProperties.presets[2].ContainsTravelbugs);
			assertEquals("presets[2] 'Quick Cache' => Favorites", 0, FilterProperties.presets[2].Favorites);
			assertEquals("presets[2] 'Quick Cache' => HasUserData", 0, FilterProperties.presets[2].HasUserData);
			assertEquals("presets[2] 'Quick Cache' => ListingChanged", 0, FilterProperties.presets[2].ListingChanged);
			assertEquals("presets[2] 'Quick Cache' => WithManualWaypoint", 0, FilterProperties.presets[2].WithManualWaypoint);
			assertEquals("presets[2] 'Quick Cache' => MinDifficulty", 0.0f, FilterProperties.presets[2].MinDifficulty);
			assertEquals("presets[2] 'Quick Cache' => MaxDifficulty", 2.5f, FilterProperties.presets[2].MaxDifficulty);
			assertEquals("presets[2] 'Quick Cache' => MinTerrain", 0.0f, FilterProperties.presets[2].MinTerrain);
			assertEquals("presets[2] 'Quick Cache' => MaxTerrain", 2.5f, FilterProperties.presets[2].MaxTerrain);
			assertEquals("presets[2] 'Quick Cache' => MinContainerSize", 0.0f, FilterProperties.presets[2].MinContainerSize);
			assertEquals("presets[2] 'Quick Cache' => MaxContainerSize", 4.0f, FilterProperties.presets[2].MaxContainerSize);
			assertEquals("presets[2] 'Quick Cache' => MinRating", 0.0f, FilterProperties.presets[2].MinRating);
			assertEquals("presets[2] 'Quick Cache' => MaxRating", 5.0f, FilterProperties.presets[2].MaxRating);

			// CacheTypes
			assertEquals("presets[2] 'Quick Cache' => cacheType.length", 13, FilterProperties.presets[2].cacheTypes.length);
			assertTrue("presets[2] 'Quick Cache' => cacheType[0]''?", FilterProperties.presets[2].cacheTypes[0]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[1]''?", FilterProperties.presets[2].cacheTypes[1]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[2]''?", FilterProperties.presets[2].cacheTypes[2]);
			assertTrue("presets[2] 'Quick Cache' => cacheType[3]''?", FilterProperties.presets[2].cacheTypes[3]);
			assertTrue("presets[2] 'Quick Cache' => cacheType[4]''?", FilterProperties.presets[2].cacheTypes[4]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[5]''?", FilterProperties.presets[2].cacheTypes[5]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[6]''?", FilterProperties.presets[2].cacheTypes[6]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[7]''?", FilterProperties.presets[2].cacheTypes[7]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[8]''?", FilterProperties.presets[2].cacheTypes[8]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[9]''?", FilterProperties.presets[2].cacheTypes[9]);
			assertFalse("presets[2] 'Quick Cache' => cacheType[10]''?", FilterProperties.presets[2].cacheTypes[10]);
			assertTrue("presets[2] 'Quick Cache' => cacheType[11]'Munzee'", FilterProperties.presets[2].cacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[2] 'Quick Cache' => attributesFilter.length", AtributeLength,
					FilterProperties.presets[2].attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("presets[2] 'Quick Cache' => attributesFilter[" + i + "]", attributesFilter[i],
						FilterProperties.presets[2].attributesFilter[i]);
			}

			assertEquals("presets[2] 'Quick Cache' => GPXFilenameIds.size", 0, FilterProperties.presets[2].GPXFilenameIds.size());
			assertEquals("presets[2] 'Quick Cache' => Categories.size", 0, FilterProperties.presets[2].Categories.size());
			assertEquals("presets[2] 'Quick Cache' => filterName", "", FilterProperties.presets[2].filterName);
			assertEquals("presets[2] 'Quick Cache' => filterGcCode", "", FilterProperties.presets[2].filterGcCode);
			assertEquals("presets[2] 'Quick Cache' => filterOwner", "", FilterProperties.presets[2].filterOwner);

			assertFalse("presets[2] 'Quick Cache' isExtendsFilter() must by return false", FilterProperties.presets[2].isExtendsFilter());

			assertEquals(
					"presets[2] 'Quick Cache' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,0.0,2.5,0.0,2.5,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,false,false,true,true,false,false,false,false,false,false,true,false\",\"filtername\":\"\"}",
					FilterProperties.presets[2].toString());

		}

		{// chk presets[3] 'Fetch some Travelbugs'
			assertEquals("presets[3] 'Fetch some Travelbugs' => Finds", 0, FilterProperties.presets[3].Finds);
			assertEquals("presets[3] 'Fetch some Travelbugs' => NotAvailable", -1, FilterProperties.presets[3].NotAvailable);
			assertEquals("presets[3] 'Fetch some Travelbugs' => Archived", -1, FilterProperties.presets[3].Archived);
			assertEquals("presets[3] 'Fetch some Travelbugs' => Own", 0, FilterProperties.presets[3].Own);
			assertEquals("presets[3] 'Fetch some Travelbugs' => ContainsTravelbugs", 1, FilterProperties.presets[3].ContainsTravelbugs);
			assertEquals("presets[3] 'Fetch some Travelbugs' => Favorites", 0, FilterProperties.presets[3].Favorites);
			assertEquals("presets[3] 'Fetch some Travelbugs' => HasUserData", 0, FilterProperties.presets[3].HasUserData);
			assertEquals("presets[3] 'Fetch some Travelbugs' => ListingChanged", 0, FilterProperties.presets[3].ListingChanged);
			assertEquals("presets[3] 'Fetch some Travelbugs' => WithManualWaypoint", 0, FilterProperties.presets[3].WithManualWaypoint);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MinDifficulty", 0.0f, FilterProperties.presets[3].MinDifficulty);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MaxDifficulty", 3.0f, FilterProperties.presets[3].MaxDifficulty);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MinTerrain", 0.0f, FilterProperties.presets[3].MinTerrain);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MaxTerrain", 3.0f, FilterProperties.presets[3].MaxTerrain);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MinContainerSize", 0.0f, FilterProperties.presets[3].MinContainerSize);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MaxContainerSize", 4.0f, FilterProperties.presets[3].MaxContainerSize);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MinRating", 0.0f, FilterProperties.presets[3].MinRating);
			assertEquals("presets[3] 'Fetch some Travelbugs' => MaxRating", 5.0f, FilterProperties.presets[3].MaxRating);

			// CacheTypes
			assertEquals("presets[3] 'Fetch some Travelbugs' => cacheType.length", 13, FilterProperties.presets[3].cacheTypes.length);
			assertTrue("presets[3] 'Fetch some Travelbugs' => cacheType[0]''?", FilterProperties.presets[3].cacheTypes[0]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[1]''?", FilterProperties.presets[3].cacheTypes[1]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[2]''?", FilterProperties.presets[3].cacheTypes[2]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[3]''?", FilterProperties.presets[3].cacheTypes[3]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[4]''?", FilterProperties.presets[3].cacheTypes[4]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[5]''?", FilterProperties.presets[3].cacheTypes[5]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[6]''?", FilterProperties.presets[3].cacheTypes[6]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[7]''?", FilterProperties.presets[3].cacheTypes[7]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[8]''?", FilterProperties.presets[3].cacheTypes[8]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[9]''?", FilterProperties.presets[3].cacheTypes[9]);
			assertFalse("presets[3] 'Fetch some Travelbugs' => cacheType[10]''?", FilterProperties.presets[3].cacheTypes[10]);
			assertTrue("presets[3] 'Fetch some Travelbugs' => cacheType[11]'Munzee'", FilterProperties.presets[3].cacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[3] 'Fetch some Travelbugs' => attributesFilter.length", AtributeLength,
					FilterProperties.presets[3].attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("presets[3] 'Fetch some Travelbugs' => attributesFilter[" + i + "]", attributesFilter[i],
						FilterProperties.presets[3].attributesFilter[i]);
			}

			assertEquals("presets[3] 'Fetch some Travelbugs' => GPXFilenameIds.size", 0, FilterProperties.presets[3].GPXFilenameIds.size());
			assertEquals("presets[3] 'Fetch some Travelbugs' => Categories.size", 0, FilterProperties.presets[3].Categories.size());
			assertEquals("presets[3] 'Fetch some Travelbugs' => filterName", "", FilterProperties.presets[3].filterName);
			assertEquals("presets[3] 'Fetch some Travelbugs' => filterGcCode", "", FilterProperties.presets[3].filterGcCode);
			assertEquals("presets[3] 'Fetch some Travelbugs' => filterOwner", "", FilterProperties.presets[3].filterOwner);

			assertFalse("presets[3] 'Fetch some Travelbugs' isExtendsFilter() must by return false",
					FilterProperties.presets[3].isExtendsFilter());

			assertEquals(
					"presets[3] 'Fetch some Travelbugs' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,-1,-1,0,1,0,0,0,0,0.0,3.0,0.0,3.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,false,false,false,false,false,false,false,false,false,false,true,false\",\"filtername\":\"\"}",
					FilterProperties.presets[3].toString());

		}

		{// chk presets[4] 'Drop off Travelbugs'
			assertEquals("presets[4] 'Drop off Travelbugs' => Finds", 0, FilterProperties.presets[4].Finds);
			assertEquals("presets[4] 'Drop off Travelbugs' => NotAvailable", -1, FilterProperties.presets[4].NotAvailable);
			assertEquals("presets[4] 'Drop off Travelbugs' => Archived", -1, FilterProperties.presets[4].Archived);
			assertEquals("presets[4] 'Drop off Travelbugs' => Own", 0, FilterProperties.presets[4].Own);
			assertEquals("presets[4] 'Drop off Travelbugs' => ContainsTravelbugs", 0, FilterProperties.presets[4].ContainsTravelbugs);
			assertEquals("presets[4] 'Drop off Travelbugs' => Favorites", 0, FilterProperties.presets[4].Favorites);
			assertEquals("presets[4] 'Drop off Travelbugs' => HasUserData", 0, FilterProperties.presets[4].HasUserData);
			assertEquals("presets[4] 'Drop off Travelbugs' => ListingChanged", 0, FilterProperties.presets[4].ListingChanged);
			assertEquals("presets[4] 'Drop off Travelbugs' => WithManualWaypoint", 0, FilterProperties.presets[4].WithManualWaypoint);
			assertEquals("presets[4] 'Drop off Travelbugs' => MinDifficulty", 0.0f, FilterProperties.presets[4].MinDifficulty);
			assertEquals("presets[4] 'Drop off Travelbugs' => MaxDifficulty", 3.0f, FilterProperties.presets[4].MaxDifficulty);
			assertEquals("presets[4] 'Drop off Travelbugs' => MinTerrain", 0.0f, FilterProperties.presets[4].MinTerrain);
			assertEquals("presets[4] 'Drop off Travelbugs' => MaxTerrain", 3.0f, FilterProperties.presets[4].MaxTerrain);
			assertEquals("presets[4] 'Drop off Travelbugs' => MinContainerSize", 2.0f, FilterProperties.presets[4].MinContainerSize);
			assertEquals("presets[4] 'Drop off Travelbugs' => MaxContainerSize", 4.0f, FilterProperties.presets[4].MaxContainerSize);
			assertEquals("presets[4] 'Drop off Travelbugs' => MinRating", 0.0f, FilterProperties.presets[4].MinRating);
			assertEquals("presets[4] 'Drop off Travelbugs' => MaxRating", 5.0f, FilterProperties.presets[4].MaxRating);

			// CacheTypes
			assertEquals("presets[4] 'Drop off Travelbugs' => cacheType.length", 13, FilterProperties.presets[4].cacheTypes.length);
			assertTrue("presets[4] 'Drop off Travelbugs' => cacheType[0]''?", FilterProperties.presets[4].cacheTypes[0]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[1]''?", FilterProperties.presets[4].cacheTypes[1]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[2]''?", FilterProperties.presets[4].cacheTypes[2]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[3]''?", FilterProperties.presets[4].cacheTypes[3]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[4]''?", FilterProperties.presets[4].cacheTypes[4]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[5]''?", FilterProperties.presets[4].cacheTypes[5]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[6]''?", FilterProperties.presets[4].cacheTypes[6]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[7]''?", FilterProperties.presets[4].cacheTypes[7]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[8]''?", FilterProperties.presets[4].cacheTypes[8]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[9]''?", FilterProperties.presets[4].cacheTypes[9]);
			assertFalse("presets[4] 'Drop off Travelbugs' => cacheType[10]''?", FilterProperties.presets[4].cacheTypes[10]);
			assertTrue("presets[4] 'Drop off Travelbugs' => cacheType[11]'Munzee'", FilterProperties.presets[4].cacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[4] 'Drop off Travelbugs' => attributesFilter.length", AtributeLength,
					FilterProperties.presets[4].attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("presets[4] 'Drop off Travelbugs' => attributesFilter[" + i + "]", attributesFilter[i],
						FilterProperties.presets[4].attributesFilter[i]);
			}

			assertEquals("presets[4] 'Drop off Travelbugs' => GPXFilenameIds.size", 0, FilterProperties.presets[4].GPXFilenameIds.size());
			assertEquals("presets[4] 'Drop off Travelbugs' => Categories.size", 0, FilterProperties.presets[4].Categories.size());
			assertEquals("presets[4] 'Drop off Travelbugs' => filterName", "", FilterProperties.presets[4].filterName);
			assertEquals("presets[4] 'Drop off Travelbugs' => filterGcCode", "", FilterProperties.presets[4].filterGcCode);
			assertEquals("presets[4] 'Drop off Travelbugs' => filterOwner", "", FilterProperties.presets[4].filterOwner);

			assertFalse("presets[4] 'Drop off Travelbugs' isExtendsFilter() must by return false",
					FilterProperties.presets[4].isExtendsFilter());

			assertEquals(
					"presets[4] 'Drop off Travelbugs' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,-1,-1,0,0,0,0,0,0,0.0,3.0,0.0,3.0,2.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,false,false,false,false,false,false,false,false,false,false,true,false\",\"filtername\":\"\"}",
					FilterProperties.presets[4].toString());

		}

		{// chk presets[5] 'Highlights'
			assertEquals("presets[5] 'Highlights' => Finds", -1, FilterProperties.presets[5].Finds);
			assertEquals("presets[5] 'Highlights' => NotAvailable", -1, FilterProperties.presets[5].NotAvailable);
			assertEquals("presets[5] 'Highlights' => Archived", -1, FilterProperties.presets[5].Archived);
			assertEquals("presets[5] 'Highlights' => Own", 0, FilterProperties.presets[5].Own);
			assertEquals("presets[5] 'Highlights' => ContainsTravelbugs", 0, FilterProperties.presets[5].ContainsTravelbugs);
			assertEquals("presets[5] 'Highlights' => Favorites", 0, FilterProperties.presets[5].Favorites);
			assertEquals("presets[5] 'Highlights' => HasUserData", 0, FilterProperties.presets[5].HasUserData);
			assertEquals("presets[5] 'Highlights' => ListingChanged", 0, FilterProperties.presets[5].ListingChanged);
			assertEquals("presets[5] 'Highlights' => WithManualWaypoint", 0, FilterProperties.presets[5].WithManualWaypoint);
			assertEquals("presets[5] 'Highlights' => MinDifficulty", 0.0f, FilterProperties.presets[5].MinDifficulty);
			assertEquals("presets[5] 'Highlights' => MaxDifficulty", 5.0f, FilterProperties.presets[5].MaxDifficulty);
			assertEquals("presets[5] 'Highlights' => MinTerrain", 0.0f, FilterProperties.presets[5].MinTerrain);
			assertEquals("presets[5] 'Highlights' => MaxTerrain", 5.0f, FilterProperties.presets[5].MaxTerrain);
			assertEquals("presets[5] 'Highlights' => MinContainerSize", 0.0f, FilterProperties.presets[5].MinContainerSize);
			assertEquals("presets[5] 'Highlights' => MaxContainerSize", 4.0f, FilterProperties.presets[5].MaxContainerSize);
			assertEquals("presets[5] 'Highlights' => MinRating", 3.5f, FilterProperties.presets[5].MinRating);
			assertEquals("presets[5] 'Highlights' => MaxRating", 5.0f, FilterProperties.presets[5].MaxRating);

			// CacheTypes
			assertEquals("presets[5] 'Highlights' => cacheType.length", 13, FilterProperties.presets[5].cacheTypes.length);
			assertTrue("presets[5] 'Highlights' => cacheType[0]''?", FilterProperties.presets[5].cacheTypes[0]);
			assertTrue("presets[5] 'Highlights' => cacheType[1]''?", FilterProperties.presets[5].cacheTypes[1]);
			assertTrue("presets[5] 'Highlights' => cacheType[2]''?", FilterProperties.presets[5].cacheTypes[2]);
			assertTrue("presets[5] 'Highlights' => cacheType[3]''?", FilterProperties.presets[5].cacheTypes[3]);
			assertTrue("presets[5] 'Highlights' => cacheType[4]''?", FilterProperties.presets[5].cacheTypes[4]);
			assertTrue("presets[5] 'Highlights' => cacheType[5]''?", FilterProperties.presets[5].cacheTypes[5]);
			assertTrue("presets[5] 'Highlights' => cacheType[6]''?", FilterProperties.presets[5].cacheTypes[6]);
			assertTrue("presets[5] 'Highlights' => cacheType[7]''?", FilterProperties.presets[5].cacheTypes[7]);
			assertTrue("presets[5] 'Highlights' => cacheType[8]''?", FilterProperties.presets[5].cacheTypes[8]);
			assertTrue("presets[5] 'Highlights' => cacheType[9]''?", FilterProperties.presets[5].cacheTypes[9]);
			assertTrue("presets[5] 'Highlights' => cacheType[10]''?", FilterProperties.presets[5].cacheTypes[10]);
			assertTrue("presets[5] 'Highlights' => cacheType[11]'Munzee'", FilterProperties.presets[5].cacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[5] 'Highlights' => attributesFilter.length", AtributeLength,
					FilterProperties.presets[5].attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("presets[5] 'Highlights' => attributesFilter[" + i + "]", attributesFilter[i],
						FilterProperties.presets[5].attributesFilter[i]);
			}

			assertEquals("presets[5] 'Highlights' => GPXFilenameIds.size", 0, FilterProperties.presets[5].GPXFilenameIds.size());
			assertEquals("presets[5] 'Highlights' => Categories.size", 0, FilterProperties.presets[5].Categories.size());
			assertEquals("presets[5] 'Highlights' => filterName", "", FilterProperties.presets[5].filterName);
			assertEquals("presets[5] 'Highlights' => filterGcCode", "", FilterProperties.presets[5].filterGcCode);
			assertEquals("presets[5] 'Highlights' => filterOwner", "", FilterProperties.presets[5].filterOwner);

			assertFalse("presets[5] 'Highlights' isExtendsFilter() must by return false", FilterProperties.presets[5].isExtendsFilter());

			assertEquals(
					"presets[5] 'Highlights' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"-1,-1,-1,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,3.5,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterProperties.presets[5].toString());

		}

		{// chk presets[6] 'Favoriten'
			assertEquals("presets[6] 'Favoriten' => Finds", 0, FilterProperties.presets[6].Finds);
			assertEquals("presets[6] 'Favoriten' => NotAvailable", 0, FilterProperties.presets[6].NotAvailable);
			assertEquals("presets[6] 'Favoriten' => Archived", 0, FilterProperties.presets[6].Archived);
			assertEquals("presets[6] 'Favoriten' => Own", 0, FilterProperties.presets[6].Own);
			assertEquals("presets[6] 'Favoriten' => ContainsTravelbugs", 0, FilterProperties.presets[6].ContainsTravelbugs);
			assertEquals("presets[6] 'Favoriten' => Favorites", 1, FilterProperties.presets[6].Favorites);
			assertEquals("presets[6] 'Favoriten' => HasUserData", 0, FilterProperties.presets[6].HasUserData);
			assertEquals("presets[6] 'Favoriten' => ListingChanged", 0, FilterProperties.presets[6].ListingChanged);
			assertEquals("presets[6] 'Favoriten' => WithManualWaypoint", 0, FilterProperties.presets[6].WithManualWaypoint);
			assertEquals("presets[6] 'Favoriten' => MinDifficulty", 0.0f, FilterProperties.presets[6].MinDifficulty);
			assertEquals("presets[6] 'Favoriten' => MaxDifficulty", 5.0f, FilterProperties.presets[6].MaxDifficulty);
			assertEquals("presets[6] 'Favoriten' => MinTerrain", 0.0f, FilterProperties.presets[6].MinTerrain);
			assertEquals("presets[6] 'Favoriten' => MaxTerrain", 5.0f, FilterProperties.presets[6].MaxTerrain);
			assertEquals("presets[6] 'Favoriten' => MinContainerSize", 0.0f, FilterProperties.presets[6].MinContainerSize);
			assertEquals("presets[6] 'Favoriten' => MaxContainerSize", 4.0f, FilterProperties.presets[6].MaxContainerSize);
			assertEquals("presets[6] 'Favoriten' => MinRating", 0.0f, FilterProperties.presets[6].MinRating);
			assertEquals("presets[6] 'Favoriten' => MaxRating", 5.0f, FilterProperties.presets[6].MaxRating);

			// CacheTypes
			assertEquals("presets[6] 'Favoriten' => cacheType.length", 13, FilterProperties.presets[6].cacheTypes.length);
			assertTrue("presets[6] 'Favoriten' => cacheType[0]''?", FilterProperties.presets[6].cacheTypes[0]);
			assertTrue("presets[6] 'Favoriten' => cacheType[1]''?", FilterProperties.presets[6].cacheTypes[1]);
			assertTrue("presets[6] 'Favoriten' => cacheType[2]''?", FilterProperties.presets[6].cacheTypes[2]);
			assertTrue("presets[6] 'Favoriten' => cacheType[3]''?", FilterProperties.presets[6].cacheTypes[3]);
			assertTrue("presets[6] 'Favoriten' => cacheType[4]''?", FilterProperties.presets[6].cacheTypes[4]);
			assertTrue("presets[6] 'Favoriten' => cacheType[5]''?", FilterProperties.presets[6].cacheTypes[5]);
			assertTrue("presets[6] 'Favoriten' => cacheType[6]''?", FilterProperties.presets[6].cacheTypes[6]);
			assertTrue("presets[6] 'Favoriten' => cacheType[7]''?", FilterProperties.presets[6].cacheTypes[7]);
			assertTrue("presets[6] 'Favoriten' => cacheType[8]''?", FilterProperties.presets[6].cacheTypes[8]);
			assertTrue("presets[6] 'Favoriten' => cacheType[9]''?", FilterProperties.presets[6].cacheTypes[9]);
			assertTrue("presets[6] 'Favoriten' => cacheType[10]''?", FilterProperties.presets[6].cacheTypes[10]);
			assertTrue("presets[6] 'Favoriten' => cacheType[11]'Munzee'", FilterProperties.presets[6].cacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[6] 'Favoriten' => attributesFilter.length", AtributeLength,
					FilterProperties.presets[6].attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("presets[6] 'Favoriten' => attributesFilter[" + i + "]", attributesFilter[i],
						FilterProperties.presets[6].attributesFilter[i]);
			}

			assertEquals("presets[6] 'Favoriten' => GPXFilenameIds.size", 0, FilterProperties.presets[6].GPXFilenameIds.size());
			assertEquals("presets[6] 'Favoriten' => Categories.size", 0, FilterProperties.presets[6].Categories.size());
			assertEquals("presets[6] 'Favoriten' => filterName", "", FilterProperties.presets[6].filterName);
			assertEquals("presets[6] 'Favoriten' => filterGcCode", "", FilterProperties.presets[6].filterGcCode);
			assertEquals("presets[6] 'Favoriten' => filterOwner", "", FilterProperties.presets[6].filterOwner);

			assertFalse("presets[6] 'Favoriten' isExtendsFilter() must by return false", FilterProperties.presets[6].isExtendsFilter());

			assertEquals(
					"presets[6] 'Favoriten' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,1,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterProperties.presets[6].toString());

		}

		{// chk presets[7] 'prepare to archive'
			assertEquals("presets[7] 'prepare to archive' => Finds", 0, FilterProperties.presets[7].Finds);
			assertEquals("presets[7] 'prepare to archive' => NotAvailable", 0, FilterProperties.presets[7].NotAvailable);
			assertEquals("presets[7] 'prepare to archive' => Archived", -1, FilterProperties.presets[7].Archived);
			assertEquals("presets[7] 'prepare to archive' => Own", -1, FilterProperties.presets[7].Own);
			assertEquals("presets[7] 'prepare to archive' => ContainsTravelbugs", 0, FilterProperties.presets[7].ContainsTravelbugs);
			assertEquals("presets[7] 'prepare to archive' => Favorites", -1, FilterProperties.presets[7].Favorites);
			assertEquals("presets[7] 'prepare to archive' => HasUserData", -1, FilterProperties.presets[7].HasUserData);
			assertEquals("presets[7] 'prepare to archive' => ListingChanged", -1, FilterProperties.presets[7].ListingChanged);
			assertEquals("presets[7] 'prepare to archive' => WithManualWaypoint", 0, FilterProperties.presets[7].WithManualWaypoint);
			assertEquals("presets[7] 'prepare to archive' => MinDifficulty", 0.0f, FilterProperties.presets[7].MinDifficulty);
			assertEquals("presets[7] 'prepare to archive' => MaxDifficulty", 5.0f, FilterProperties.presets[7].MaxDifficulty);
			assertEquals("presets[7] 'prepare to archive' => MinTerrain", 0.0f, FilterProperties.presets[7].MinTerrain);
			assertEquals("presets[7] 'prepare to archive' => MaxTerrain", 5.0f, FilterProperties.presets[7].MaxTerrain);
			assertEquals("presets[7] 'prepare to archive' => MinContainerSize", 0.0f, FilterProperties.presets[7].MinContainerSize);
			assertEquals("presets[7] 'prepare to archive' => MaxContainerSize", 4.0f, FilterProperties.presets[7].MaxContainerSize);
			assertEquals("presets[7] 'prepare to archive' => MinRating", 0.0f, FilterProperties.presets[7].MinRating);
			assertEquals("presets[7] 'prepare to archive' => MaxRating", 5.0f, FilterProperties.presets[7].MaxRating);

			// CacheTypes
			assertEquals("presets[7] 'prepare to archive' => cacheType.length", 13, FilterProperties.presets[7].cacheTypes.length);
			assertTrue("presets[7] 'prepare to archive' => cacheType[0]''?", FilterProperties.presets[7].cacheTypes[0]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[1]''?", FilterProperties.presets[7].cacheTypes[1]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[2]''?", FilterProperties.presets[7].cacheTypes[2]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[3]''?", FilterProperties.presets[7].cacheTypes[3]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[4]''?", FilterProperties.presets[7].cacheTypes[4]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[5]''?", FilterProperties.presets[7].cacheTypes[5]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[6]''?", FilterProperties.presets[7].cacheTypes[6]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[7]''?", FilterProperties.presets[7].cacheTypes[7]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[8]''?", FilterProperties.presets[7].cacheTypes[8]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[9]''?", FilterProperties.presets[7].cacheTypes[9]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[10]''?", FilterProperties.presets[7].cacheTypes[10]);
			assertTrue("presets[7] 'prepare to archive' => cacheType[11]'Munzee'", FilterProperties.presets[7].cacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[7] 'prepare to archive' => attributesFilter.length", AtributeLength,
					FilterProperties.presets[7].attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("presets[7] 'prepare to archive' => attributesFilter[" + i + "]", attributesFilter[i],
						FilterProperties.presets[7].attributesFilter[i]);
			}

			assertEquals("presets[7] 'prepare to archive' => GPXFilenameIds.size", 0, FilterProperties.presets[7].GPXFilenameIds.size());
			assertEquals("presets[7] 'prepare to archive' => Categories.size", 0, FilterProperties.presets[7].Categories.size());
			assertEquals("presets[7] 'prepare to archive' => filterName", "", FilterProperties.presets[7].filterName);
			assertEquals("presets[7] 'prepare to archive' => filterGcCode", "", FilterProperties.presets[7].filterGcCode);
			assertEquals("presets[7] 'prepare to archive' => filterOwner", "", FilterProperties.presets[7].filterOwner);

			assertFalse("presets[7] 'prepare to archive' isExtendsFilter() must by return false",
					FilterProperties.presets[7].isExtendsFilter());

			assertEquals(
					"presets[7] 'prepare to archive' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,-1,-1,0,-1,-1,-1,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterProperties.presets[7].toString());

		}

		{// chk presets[8] 'Listing Changed'
			assertEquals("presets[8] 'Listing Changed' => Finds", 0, FilterProperties.presets[8].Finds);
			assertEquals("presets[8] 'Listing Changed' => NotAvailable", 0, FilterProperties.presets[8].NotAvailable);
			assertEquals("presets[8] 'Listing Changed' => Archived", 0, FilterProperties.presets[8].Archived);
			assertEquals("presets[8] 'Listing Changed' => Own", 0, FilterProperties.presets[8].Own);
			assertEquals("presets[8] 'Listing Changed' => ContainsTravelbugs", 0, FilterProperties.presets[8].ContainsTravelbugs);
			assertEquals("presets[8] 'Listing Changed' => Favorites", 0, FilterProperties.presets[8].Favorites);
			assertEquals("presets[8] 'Listing Changed' => HasUserData", 0, FilterProperties.presets[8].HasUserData);
			assertEquals("presets[8] 'Listing Changed' => ListingChanged", 1, FilterProperties.presets[8].ListingChanged);
			assertEquals("presets[8] 'Listing Changed' => WithManualWaypoint", 0, FilterProperties.presets[8].WithManualWaypoint);
			assertEquals("presets[8] 'Listing Changed' => MinDifficulty", 0.0f, FilterProperties.presets[8].MinDifficulty);
			assertEquals("presets[8] 'Listing Changed' => MaxDifficulty", 5.0f, FilterProperties.presets[8].MaxDifficulty);
			assertEquals("presets[8] 'Listing Changed' => MinTerrain", 0.0f, FilterProperties.presets[8].MinTerrain);
			assertEquals("presets[8] 'Listing Changed' => MaxTerrain", 5.0f, FilterProperties.presets[8].MaxTerrain);
			assertEquals("presets[8] 'Listing Changed' => MinContainerSize", 0.0f, FilterProperties.presets[8].MinContainerSize);
			assertEquals("presets[8] 'Listing Changed' => MaxContainerSize", 4.0f, FilterProperties.presets[8].MaxContainerSize);
			assertEquals("presets[8] 'Listing Changed' => MinRating", 0.0f, FilterProperties.presets[8].MinRating);
			assertEquals("presets[8] 'Listing Changed' => MaxRating", 5.0f, FilterProperties.presets[8].MaxRating);

			// CacheTypes
			assertEquals("presets[8] 'Listing Changed' => cacheType.length", 13, FilterProperties.presets[8].cacheTypes.length);
			assertTrue("presets[8] 'Listing Changed' => cacheType[0]''?", FilterProperties.presets[8].cacheTypes[0]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[1]''?", FilterProperties.presets[8].cacheTypes[1]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[2]''?", FilterProperties.presets[8].cacheTypes[2]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[3]''?", FilterProperties.presets[8].cacheTypes[3]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[4]''?", FilterProperties.presets[8].cacheTypes[4]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[5]''?", FilterProperties.presets[8].cacheTypes[5]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[6]''?", FilterProperties.presets[8].cacheTypes[6]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[7]''?", FilterProperties.presets[8].cacheTypes[7]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[8]''?", FilterProperties.presets[8].cacheTypes[8]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[9]''?", FilterProperties.presets[8].cacheTypes[9]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[10]''?", FilterProperties.presets[8].cacheTypes[10]);
			assertTrue("presets[8] 'Listing Changed' => cacheType[11]'Munzee'", FilterProperties.presets[8].cacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("presets[8] 'Listing Changed' => attributesFilter.length", AtributeLength,
					FilterProperties.presets[8].attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("presets[8] 'Listing Changed' => attributesFilter[" + i + "]", attributesFilter[i],
						FilterProperties.presets[8].attributesFilter[i]);
			}

			assertEquals("presets[8] 'Listing Changed' => GPXFilenameIds.size", 0, FilterProperties.presets[8].GPXFilenameIds.size());
			assertEquals("presets[8] 'Listing Changed' => Categories.size", 0, FilterProperties.presets[8].Categories.size());
			assertEquals("presets[8] 'Listing Changed' => filterName", "", FilterProperties.presets[8].filterName);
			assertEquals("presets[8] 'Listing Changed' => filterGcCode", "", FilterProperties.presets[8].filterGcCode);
			assertEquals("presets[8] 'Listing Changed' => filterOwner", "", FilterProperties.presets[8].filterOwner);

			assertFalse("presets[8] 'Listing Changed' isExtendsFilter() must by return false",
					FilterProperties.presets[8].isExtendsFilter());

			assertEquals(
					"presets[8] 'Listing Changed' =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,0,0,1,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					FilterProperties.presets[8].toString());

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
			assertEquals("default constructor => cacheType.length", 13, defaultCtor.cacheTypes.length);
			assertTrue("default constructor => cacheType[0]''?", defaultCtor.cacheTypes[0]);
			assertTrue("default constructor => cacheType[1]''?", defaultCtor.cacheTypes[1]);
			assertTrue("default constructor => cacheType[2]''?", defaultCtor.cacheTypes[2]);
			assertTrue("default constructor => cacheType[3]''?", defaultCtor.cacheTypes[3]);
			assertTrue("default constructor => cacheType[4]''?", defaultCtor.cacheTypes[4]);
			assertTrue("default constructor => cacheType[5]''?", defaultCtor.cacheTypes[5]);
			assertTrue("default constructor => cacheType[6]''?", defaultCtor.cacheTypes[6]);
			assertTrue("default constructor => cacheType[7]''?", defaultCtor.cacheTypes[7]);
			assertTrue("default constructor => cacheType[8]''?", defaultCtor.cacheTypes[8]);
			assertTrue("default constructor => cacheType[9]''?", defaultCtor.cacheTypes[9]);
			assertTrue("default constructor => cacheType[10]''?", defaultCtor.cacheTypes[10]);
			assertTrue("default constructor => cacheType[11]'Munzee'", defaultCtor.cacheTypes[11]);

			// AttributesFilter
			int AtributeLength = 66;
			assertEquals("default constructor => attributesFilter.length", AtributeLength, defaultCtor.attributesFilter.length);

			int[] attributesFilter = new int[]
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			for (int i = 0; i < AtributeLength; i++)
			{
				assertEquals("default constructor => attributesFilter[" + i + "]", attributesFilter[i], defaultCtor.attributesFilter[i]);
			}

			assertEquals("default constructor => GPXFilenameIds.size", 0, defaultCtor.GPXFilenameIds.size());
			assertEquals("default constructor => Categories.size", 0, defaultCtor.Categories.size());
			assertEquals("default constructor => filterName", "", defaultCtor.filterName);
			assertEquals("default constructor => filterGcCode", "", defaultCtor.filterGcCode);
			assertEquals("default constructor => filterOwner", "", defaultCtor.filterOwner);

			assertFalse("default constructor isExtendsFilter() must by return false", defaultCtor.isExtendsFilter());

			assertEquals(
					"default constructor =>toString",
					"{\"gpxfilenameids\":\"\",\"caches\":\"0,0,0,0,0,0,0,0,0,0.0,5.0,0.0,5.0,0.0,4.0,0.0,5.0\",\"filtergc\":\"\",\"filterowner\":\"\",\"categories\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true\",\"filtername\":\"\"}",
					defaultCtor.toString());

		}
	}

	public void test_ChkConstructorAndEquals()
	{
		int presetSize = FilterProperties.presets.length;
		String[] jsonStringList = new String[presetSize];
		FilterProperties[] chkList = new FilterProperties[presetSize];
		for (int i = 0; i < presetSize; i++)
		{
			jsonStringList[i] = FilterProperties.presets[i].toString();
			chkList[i] = new FilterProperties(jsonStringList[i]);
			assertTrue("Constructor or Equals Failures with Preset:" + i, chkList[i].equals(FilterProperties.presets[i]));

		}
	}

	public void test_ChkSqlWhere()
	{
		int presetSize = FilterProperties.presets.length;
		String[] SqlStringList = new String[]
			{
					"Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
					"(Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='User') and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
					"(Found=0 or Found is null) and Available=1 and Archived=0 and (not Owner='User') and Difficulty >= 0.0 and Difficulty <= 5.0 and Terrain >= 0.0 and Terrain <= 5.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,3,4,21) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
					"Available=1 and Archived=0 and NumTravelbugs > 0 and Difficulty >= 0.0 and Difficulty <= 6.0 and Terrain >= 0.0 and Terrain <= 6.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,21) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
					"Available=1 and Archived=0 and Difficulty >= 0.0 and Difficulty <= 6.0 and Terrain >= 0.0 and Terrain <= 6.0 and Size >= 2.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,21) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
					"(Found=0 or Found is null) and Available=1 and Archived=0 and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 350.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
					"Favorit=1 and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
					"Archived=0 and (not Owner='User') and (Favorit=0 or Favorit is null) and (HasUserData = 0 or HasUserData is null) and (ListingChanged=0 or ListingChanged is null) and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
					"ListingChanged=1 and Difficulty >= 0.0 and Difficulty <= 10.0 and Terrain >= 0.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,21,22) and Name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )" };

		for (int i = 0; i < presetSize; i++)
		{
			assertEquals("presets[" + i + "] '=>getSqlWhere(\"User\")", SqlStringList[i], FilterProperties.presets[i].getSqlWhere("User"));
		}
	}
}
