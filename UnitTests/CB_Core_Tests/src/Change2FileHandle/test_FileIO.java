package Change2FileHandle;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Utils.Util.FileIO;

public class test_FileIO extends TestCase {

	@Test
	public void test_DirectoryExists() {
		assertTrue("Folder exist, bad function has not detected!", FileIO.DirectoryExists("./testdata/repository/"));
		assertFalse("Folder dosent exist, bad function has not detected!", FileIO.DirectoryExists("./testdata/rrrrrrr/"));
	}

}
