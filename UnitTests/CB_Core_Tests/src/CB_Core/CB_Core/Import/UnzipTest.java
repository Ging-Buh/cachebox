package CB_Core.CB_Core.Import;

import java.io.File;
import java.util.ArrayList;

import CB_Core.Import.UnZip;
import CB_Utils.Util.FileIO;
import junit.framework.TestCase;

/**
 * Enth�lt die Tests zum Entpacken von Zip Files
 * 
 * @author Longri
 */
public class UnzipTest extends TestCase {

	/**
	 * Entpackt ein Zip file in den selben Ordner Wird beim Import ben�tigt
	 * 
	 * @throws Exception
	 */
	public static void testUnzip() throws Exception {

		// del alten entpackten Ordener wenn vorhanden?
		File directory = new File("./testdata/gpx/");
		File[] filelist = directory.listFiles();
		for (File tmp : filelist) {
			if (tmp.isDirectory() && tmp.getPath().contains("GS_PQ")) {
				ArrayList<File> ordnerInhalt = FileIO.recursiveDirectoryReader(tmp, new ArrayList<File>());
				for (File tmp2 : ordnerInhalt) {
					tmp2.delete();
				}
				tmp.delete();
			}
		}

		// entpacke zip File
		UnZip.extractFolder("./testdata/gpx/GS_PQ.zip");

		// teste Entpackte Ordner Struktur
		ArrayList<File> ordnerInhalt = FileIO.recursiveDirectoryReader(new File("./testdata/gpx/GS_PQ/"), new ArrayList<File>());
		int counter = 0;
		for (File tmp2 : ordnerInhalt) {
			if (tmp2.getAbsolutePath().contains("6004539_HomeZone.gpx") || tmp2.getAbsolutePath().contains("6004539_HomeZone-wpts")) {
				counter++;
			}
		}

		assertTrue("must unzip to files", counter == 2);

	}
}
