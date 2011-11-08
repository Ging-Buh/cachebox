package CB_Core.Import;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Enthält die Tests zum Entpacken von Zip Files
 * 
 * @author Longri
 *
 */
public class UnzipTest extends TestCase {
	
	
	/**
	 * Entpackt ein Zip file in den selben Ordner
	 * 
	 * Wird beim Import benötigt
	 * @throws Exception
	 */
	public static void testUnzip() throws Exception
	{
		
		// del alten entpackten Ordener wenn vorhanden?
		File directory = new File("./testdata/gpx/");
		File[] filelist = directory.listFiles();
		for(File tmp : filelist)
		{
			if (tmp.isDirectory() && tmp.getPath().contains("GS_PQ"))
			{
				ArrayList<String> ordnerInhalt = Importer.recursiveDirectoryReader(tmp,new ArrayList<String>() );
				for (String tmp2 : ordnerInhalt)
				{
					File forDel = new File(tmp2);
					forDel.delete();
				}
				tmp.delete();
			}
		}
		
		//entpacke zip File
		UnZip.extractFolder("./testdata/gpx/GS_PQ.zip");
		
		//teste Entpackte Ordner Struktur
		ArrayList<String> ordnerInhalt = Importer.recursiveDirectoryReader(new File("./testdata/gpx/GS_PQ/"),new ArrayList<String>() );
		int counter = 0;
		for (String tmp2 : ordnerInhalt)
		{
			if(tmp2.contains("testdata\\gpx\\GS_PQ\\6004539_HomeZone.gpx") || tmp2.contains("testdata\\gpx\\GS_PQ\\6004539_HomeZone-wpts"))
			{
				counter++;
			}
		}
		
		assertTrue( "Keine zwei GPX-File´s entpackt", counter==2 );
		
		
		
	}
}
