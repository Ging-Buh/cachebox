package Types;

import java.util.ArrayList;
import java.util.Arrays;

import CB_Core.CacheTypes;
import CB_Core.FilterInstances;
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

	assertTrue("must correspond to Filter", FilterInstances.ALL.passed(ca));
	assertFalse("must correspond to Filter", FilterInstances.ACTIVE.passed(ca));
	assertFalse("must correspond to Filter", FilterInstances.QUICK.passed(ca));
	assertFalse("must correspond to Filter", FilterInstances.WITHTB.passed(ca));
	assertFalse("must correspond to Filter", FilterInstances.DROPTB.passed(ca));
	assertFalse("must correspond to Filter", FilterInstances.HIGHLIGHTS.passed(ca));
	// assertFalse("must correspond to Filter", FilterInstances.FAVORITES.passed(ca));
	// assertFalse("must correspond to Filter", FilterInstances.TOARCHIVE.passed(ca));
	// assertFalse("must correspond to Filter", FilterInstances.LISTINGCHANGED.passed(ca));

	ca.setAvailable(true);
	ca.setArchived(false);
	ca.setOwner("katipa");
	ca.setFound(true);

	assertTrue("must correspond to Filter", FilterInstances.ALL.passed(ca));
	assertFalse("must correspond to Filter", FilterInstances.ACTIVE.passed(ca));
	assertFalse("must correspond to Filter", FilterInstances.QUICK.passed(ca));
	assertTrue("must correspond to Filter", FilterInstances.WITHTB.passed(ca));
	assertTrue("must correspond to Filter", FilterInstances.DROPTB.passed(ca));
	assertFalse("must correspond to Filter", FilterInstances.HIGHLIGHTS.passed(ca));

	FilterProperties cacheTypeFilter = new FilterProperties();

	ArrayList<CacheTypes> typesTrue = new ArrayList<CacheTypes>();
	ArrayList<CacheTypes> typesFalse = new ArrayList<CacheTypes>();
	for (CacheTypes type : CacheTypes.values()) {
	    typesTrue.add(type);
	}

	for (int i = 0, n = typesTrue.size(); i < n; i++) {
	    for (CacheTypes type : typesTrue) {
		ca.Type = type;
		assertTrue("must correspond to Filter", cacheTypeFilter.passed(ca));
	    }

	    for (CacheTypes type : typesFalse) {
		ca.Type = type;
		assertFalse("must correspond to Filter", cacheTypeFilter.passed(ca));
	    }
	    typesFalse.add(typesTrue.remove(0));
	    setCacheTypes(typesTrue, cacheTypeFilter);
	}
    }

    private FilterProperties setCacheTypes(ArrayList<CacheTypes> types, FilterProperties cacheTypeFilter) {
	Arrays.fill(cacheTypeFilter.mCacheTypes, false);
	for (CacheTypes type : types) {
	    int TypeIndex = type.ordinal();
	    cacheTypeFilter.mCacheTypes[TypeIndex] = true;
	}
	return cacheTypeFilter;
    }

}
