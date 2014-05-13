package ___Test_Suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import API.GenAttributes;
import API.GenCacheTypes;

@RunWith(Suite.class)
@SuiteClasses(
	{ GenCacheTypes.class, GenAttributes.class })
public class Download_GC_Type_Attribute_Icons
{

}
