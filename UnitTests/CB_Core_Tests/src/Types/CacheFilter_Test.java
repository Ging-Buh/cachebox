package Types;

import java.util.ArrayList;

import CB_Core.CacheTypes;
import CB_Core.FilterProperties;
import CB_Core.Types.Cache;
import CB_UI.Config;
import __Static.InitTestDBs;
import junit.framework.TestCase;

public class CacheFilter_Test extends TestCase {

    public void test_Cache_correspond_Filter() {
	InitTestDBs.InitalConfig();
	Config.GcLogin.setValue("katipa");

	Cache ca = new Cache(true);

	ca.Type = CacheTypes.Traditional;
	ca.setAvailable(false);
	ca.setArchived(true);
	ca.setOwner("katipa");
	ca.setFound(true);
	ca.NumTravelbugs = 2;

	assertTrue("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[0]));
	assertFalse("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[1]));
	assertFalse("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[2]));
	assertFalse("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[3]));
	assertFalse("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[4]));
	assertFalse("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[5]));

	ca.setAvailable(true);
	ca.setArchived(false);
	ca.setOwner("katipa");
	ca.setFound(true);

	assertTrue("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[0]));
	assertFalse("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[1]));
	assertFalse("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[2]));
	assertTrue("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[3]));
	assertTrue("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[4]));
	assertFalse("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[5]));

	FilterProperties cacheTypeFilter = new FilterProperties(FilterProperties.presets[0].toString());

	CacheTypes[] types = new CacheTypes[] { CacheTypes.Traditional, CacheTypes.Multi, CacheTypes.Mystery, CacheTypes.Camera, CacheTypes.Earth, CacheTypes.Event, CacheTypes.MegaEvent, CacheTypes.CITO, CacheTypes.Virtual, CacheTypes.Letterbox, CacheTypes.Wherigo, CacheTypes.Munzee, CacheTypes.Giga };

	ArrayList<CacheTypes> typesTrue = new ArrayList<CacheTypes>();
	ArrayList<CacheTypes> typesFalse = new ArrayList<CacheTypes>();
	for (CacheTypes type : types) {
	    typesTrue.add(type);
	}

	for (int i = 0, n = typesTrue.size(); i < n; i++) {
	    for (CacheTypes type : typesTrue) {
		ca.Type = type;
		assertTrue("must correspond to Filter", ca.correspondToFilter(cacheTypeFilter));
	    }

	    for (CacheTypes type : typesFalse) {
		ca.Type = type;
		assertFalse("must correspond to Filter", ca.correspondToFilter(cacheTypeFilter));
	    }
	    typesFalse.add(typesTrue.remove(0));
	    cacheTypeFilter.setCachtypes(typesTrue);
	}
    }
}
