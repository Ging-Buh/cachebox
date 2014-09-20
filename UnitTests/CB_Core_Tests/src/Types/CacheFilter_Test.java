package Types;

import junit.framework.TestCase;
import CB_Core.FilterProperties;
import CB_Core.Types.Cache;
import CB_UI.Config;
import __Static.InitTestDBs;

public class CacheFilter_Test extends TestCase
{

	public void test_Cache_correspond_Filter()
	{
		InitTestDBs.InitalConfig();
		Config.GcLogin.setValue("katipa");

		Cache ca = new Cache(true);

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
		assertTrue("must correspond to Filter", ca.correspondToFilter(FilterProperties.presets[5]));

	}

}
