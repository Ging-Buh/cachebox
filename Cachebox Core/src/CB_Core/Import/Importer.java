package CB_Core.Import;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipException;

import CB_Core.FileIO;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;


public class Importer 
{
	public void importGC()
	{
		ProgresssChangedEventList.Call("import Gc.com", "", 0);
		
		
	}
	
	/**
	 * Structur von zwei Iterator-Listen für Cache und Logs
	 * als Rückgabe beim Import
	 * @author Longri
	 *
	 */
	public class Cache_Log_Return
	{
		public Cache_Log_Return(Iterator<Cache> cacheIterator,int CacheCount, Iterator<LogEntry>logIterator, int LogCount)
		{
			this.cacheIterator=cacheIterator;
			this.logIterator=logIterator;
			this.CacheCount=CacheCount;
			this.LogCount=LogCount;
		}
		
		public Iterator<Cache> cacheIterator; 
		public Iterator<LogEntry> logIterator; 
		public int CacheCount;
		public int LogCount;
	}
	
	/**
	 * Importiert die GPX files, die sich in diesem Verzeichniss befinden.
	 * Auch wenn sie sich in einem Zip-File befinden.
	 * @param directoryPath
	 * @param ip 
	 * @return Cache_Log_Return mit dem Inhalt aller Importierten GPX Files
	 */
	public Cache_Log_Return importGpx(String directoryPath, ImporterProgress ip)
	{
		// Extract all Zip Files!
		ArrayList<String> ordnerInhalt_Zip = Importer.recursiveDirectoryReader(new File(directoryPath),new ArrayList<String>(),"zip" );
		
		
		ip.setJobMax("ExtractZip", ordnerInhalt_Zip.size());
		
		for(String tmpZip : ordnerInhalt_Zip)
		{
			ip.ProgressInkrement("ExtractZip", "");
			//Extract ZIP
			try {
				UnZip.extractFolder(tmpZip);
			} catch (ZipException e) {
				Logger.Error("Core.Importer.ImportGPX", "ZipException", e);
				e.printStackTrace();
			} catch (IOException e) {
				Logger.Error("Core.Importer.ImportGPX", "IOException", e);
				e.printStackTrace();
			}
		}
		
		
		
		// Importiere all GPX files
		String[] FileList = GetFilesToLoad(directoryPath);
		ImportHandler importHandler = new ImportHandler();
		
		ip.setJobMax("ImportGPX", FileList.length);
		for(String File : FileList)
		{
			ip.ProgressInkrement("ImportGPX", "Import: "+File);
			GPXFileImporter importer = new GPXFileImporter( File );
			try {
				importer.doImport( importHandler );
			} catch (Exception e) {
				Logger.Error("Core.Importer.ImportGpx", "importer.doImport => "+ File, e);
				e.printStackTrace();
			}
		}
		
				
		//Return Caches und Logs
		return new Cache_Log_Return(importHandler.getCacheIterator(),importHandler.CacheCount(),importHandler.getLogIterator(),importHandler.LogCount());
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
         files = recursiveDirectoryReader(new File (directoryPath), files);

         String[] filesSorted = new String[files.size()];

         int idx = 0;
         for (int wptsWanted = 0; wptsWanted < 2; wptsWanted++)
             for(String file : files)
             {
                 Boolean isWaypointFile = file.toLowerCase().endsWith("-wpts.gpx");
                 if (isWaypointFile == (wptsWanted == 1))
                     filesSorted[idx++] = file;
             }

         return filesSorted;
     }
	
	 
	 /**
	  * Gibt eine ArrayList<String> zurück, die alle Files mit der Endung gpx enthält.
	  * @param directory
	  * @param files
	  * @return
	  */
	 public static ArrayList<String> recursiveDirectoryReader(File directory,  ArrayList<String> files)
     {
		 return recursiveDirectoryReader( directory,  files,"gpx");
     }
	 
	 /**
	  * Gibt eine ArrayList<String> zurück, die alle Files mit der angegebenen Endung haben.
	  * @param directory
	  * @param files
	  * @return
	  */
	 public static ArrayList<String> recursiveDirectoryReader(File directory,  ArrayList<String> files,final String Endung)
     {
		
		 File[] filelist = directory.listFiles(new FilenameFilter() {

             @Override
             public boolean accept(File dir, String filename) {

                 return filename.contains("."+Endung);
             }
         });


		 
         
         for(File localFile : filelist)
             files.add(localFile.getAbsolutePath());

         File[] directories = directory.listFiles();
         for(File recursiveDir : directories)
         {
        	 if(recursiveDir.isDirectory())
        		 recursiveDirectoryReader(recursiveDir, files, Endung);
         }
        return files;     
     }
	
}
