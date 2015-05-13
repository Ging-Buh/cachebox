package Change2FileHandle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipException;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Core.DAO.CacheListDAO;
import CB_Core.Import.UnZip;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.CopyHelper.Copy;
import CB_Utils.Util.CopyHelper.CopyRule;

public class test_CacheListDAO extends TestCase {

    @Test
    public void test_delCacheImagesByPath() {

	String gccode = "GC4H6N6";
	String imagePath = "./testdata/repository/images";

	//create folders and files for test
	{

	    //copy zip
	    CopyRule rule = new CopyRule("./testdata/Images.zip", "./testdata/repository/");
	    Copy copyHelper = new Copy(rule);
	    try {
		copyHelper.Run();
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    // entpacke zip File
	    try {
		UnZip.extractFolder("./testdata/repository/Images.zip");
	    } catch (ZipException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    //chk file exists
	    assertTrue("File must exist for runing test. Must include images.zip", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H6N6802289101.gif"));
	    assertTrue("File must exist for runing test. Must include images.zip", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H6N61744439196.jpg"));
	    assertTrue("File must exist for runing test. Must include images.zip", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H6N62438533361.jpg"));
	    assertTrue("File must exist for runing test. Must include images.zip", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H6N62696779999.jpg"));
	    assertTrue("File must exist for runing test. Must include images.zip", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H7N62696779999.jpg"));
	}

	//call del function
	CacheListDAO dao = new CacheListDAO();
	ArrayList<String> list = new ArrayList<String>();
	list.add(gccode);
	dao.delCacheImagesByPath(imagePath, list);

	//chk file dosent exists
	assertFalse("File must delete from function!", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H6N6802289101.gif"));
	assertFalse("File must delete from function!", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H6N61744439196.jpg"));
	assertFalse("File must delete from function!", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H6N62438533361.jpg"));
	assertFalse("File must delete from function!", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H6N62696779999.jpg"));
	assertTrue("File should not be deleted from function!", FileIO.FileExists("./testdata/repository/Images/GC4H/GC4H7N62696779999.jpg"));

    }

}
