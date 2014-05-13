package ___Test_Suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import CB_Core.CB_Core.Import.GPX_Unzip_Import_Test;
import CB_Core.CB_Core.Import.GSAKGpxImportTest;
import CB_Core.CB_Core.Import.GpxImportTest;
import CB_Core.CB_Core.Import.IndexDBTest;
import CB_Core.CB_Core.Import.UnzipTest;

@RunWith(Suite.class)
@SuiteClasses(
	{ GPX_Unzip_Import_Test.class, GpxImportTest.class, GSAKGpxImportTest.class, UnzipTest.class, IndexDBTest.class })
public class Import_tests
{

}
