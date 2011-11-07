package CB_Core.Import;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipException;

import CB_Core.FileIO;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public class Importer
{
	public void importGC(ArrayList<PQ> pqList)
	{
		ProgresssChangedEventList.Call("import Gc.com", "", 0);

	}

	/**
	 * Importiert die GPX files, die sich in diesem Verzeichniss befinden. Auch
	 * wenn sie sich in einem Zip-File befinden.
	 * 
	 * @param directoryPath
	 * @param ip
	 * @return Cache_Log_Return mit dem Inhalt aller Importierten GPX Files
	 */
	public void importGpx(String directoryPath,
			ImporterProgress ip) {
		// Extract all Zip Files!
		ArrayList<String> ordnerInhalt_Zip = Importer.recursiveDirectoryReader(new File(directoryPath), new ArrayList<String>(), "zip");

		ip.setJobMax("ExtractZip", ordnerInhalt_Zip.size());

		for (String tmpZip : ordnerInhalt_Zip)
		{
			ip.ProgressInkrement("ExtractZip", "");
			// Extract ZIP
			try
			{
				UnZip.extractFolder(tmpZip);
			}
			catch (ZipException e)
			{
				Logger.Error("Core.Importer.ImportGPX", "ZipException", e);
				e.printStackTrace();
			}
			catch (IOException e)
			{
				Logger.Error("Core.Importer.ImportGPX", "IOException", e);
				e.printStackTrace();
			}
		}

		// Import all GPX files
		String[] FileList = GetFilesToLoad(directoryPath);
		
		ip.setJobMax("AnalyseGPX", FileList.length);
		
		ImportHandler importHandler = new ImportHandler();

		Integer countwpt = 0;
		HashMap<String, Integer> wptCount = new HashMap<String, Integer>();
		
		for (String File : FileList) {
			ip.ProgressInkrement("AnalyseGPX", new File(File).getName());

			BufferedReader br;
			String strLine;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(File)));
				while ((strLine = br.readLine()) != null)
				{
					if (strLine.contains("<wpt")) countwpt++;
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			wptCount.put(File, countwpt);
			countwpt = 0;
		}

		for (Integer count : wptCount.values()) {
			countwpt += count;
		}
		
		ip.setJobMax("ImportGPX", FileList.length + countwpt);
		for (String File : FileList)
		{
			ip.ProgressInkrement("ImportGPX", "Import: " + new File(File).getName());
			GPXFileImporter importer = new GPXFileImporter(File, ip);
			try
			{
				importer.doImport(importHandler, wptCount.get(File));
			}
			catch (Exception e)
			{
				Logger.Error("Core.Importer.ImportGpx", "importer.doImport => " + File, e);
				e.printStackTrace();
			}
		}

		// Return Caches, Logs und Waypoints
//		return new Cache_Log_Waypoint_Return(importHandler.getCacheIterator(),
//				importHandler.CacheCount(), importHandler.getLogIterator(),
//				importHandler.LogCount(), importHandler.getWaypointIterator(),
//				importHandler.WaypointCount());
	}

	public void importGcVote()
	{
		ProgresssChangedEventList.Call("import GcVote", "", 0);

	}

	public void importImages()
	{
		ProgresssChangedEventList.Call("import Images", "", 0);

	}

	public void importMaps()
	{
		ProgresssChangedEventList.Call("import Map", "", 0);

	}

	public void importMail()
	{
		ProgresssChangedEventList.Call("import from Mail", "", 0);

	}

	private String[] GetFilesToLoad(String directoryPath)
	{
		// GPX sortieren

		FileIO.DirectoryExists(directoryPath);

		ArrayList<String> files = new ArrayList<String>();
		files = recursiveDirectoryReader(new File(directoryPath), files);

		String[] filesSorted = new String[files.size()];

		int idx = 0;
		for (int wptsWanted = 0; wptsWanted < 2; wptsWanted++)
			for (String file : files)
			{
				Boolean isWaypointFile = file.toLowerCase().endsWith("-wpts.gpx");
				if (isWaypointFile == (wptsWanted == 1)) filesSorted[idx++] = file;
			}

		return filesSorted;
	}

	/**
	 * Gibt eine ArrayList<String> zurück, die alle Files mit der Endung gpx
	 * enthält.
	 * 
	 * @param directory
	 * @param files
	 * @return
	 */
	public static ArrayList<String> recursiveDirectoryReader(File directory, ArrayList<String> files)
	{
		return recursiveDirectoryReader(directory, files, "gpx");
	}

	/**
	 * Gibt eine ArrayList<String> zurück, die alle Files mit der angegebenen
	 * Endung haben.
	 * 
	 * @param directory
	 * @param files
	 * @return
	 */
	public static ArrayList<String> recursiveDirectoryReader(File directory, ArrayList<String> files, final String Endung)
	{

		File[] filelist = directory.listFiles(new FilenameFilter()
		{

			@Override
			public boolean accept(File dir, String filename)
			{

				return filename.contains("." + Endung);
			}
		});

		for (File localFile : filelist)
			files.add(localFile.getAbsolutePath());

		File[] directories = directory.listFiles();
		for (File recursiveDir : directories)
		{
			if (recursiveDir.isDirectory()) recursiveDirectoryReader(recursiveDir, files, Endung);
		}
		return files;
	}

}
