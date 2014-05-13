package ___Test_Suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import API.searchForGeoCache_Test;
import CB_Core.CB_Core.Api.Bug384;
import CB_Core.CB_Core.Api.PQ_Download;
import CB_Core.CB_Core.Api.PocketQueryTest;
import CB_Core.CB_Core.Api.Trackable_Test;
import CB_Core.CB_Core.Api.chkCacheState;
import CB_Core.CB_Core.Api.isPremium_GetFound_Test;

@RunWith(Suite.class)
@SuiteClasses(
	{ searchForGeoCache_Test.class, Bug384.class, chkCacheState.class, isPremium_GetFound_Test.class, PocketQueryTest.class,
			PQ_Download.class, Trackable_Test.class })
public class Online_tests
{

}
