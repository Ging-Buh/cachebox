package CB_Core.Import;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import CB_Core.FileIO;
import CB_Core.Events.ProgresssChangedEventList;


public class Importer 
{
	public void importGC()
	{
		ProgresssChangedEventList.Call("import Gc.com", "", 0);
		
		
	}
	
	public void importGpx(String directoryPath)
	{
		GPXFileReader gpxFileReader = new GPXFileReader();
		
		ProgresssChangedEventList.Call("import GPX", "", 0);
		
		String[] FileList = GetFilesToLoad(directoryPath);
		
		for(String File : FileList)
		{
			gpxFileReader.read(File);
		}
		
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
	
	 private ArrayList<String> recursiveDirectoryReader(File directory,  ArrayList<String> files)
     {
		
		 File[] filelist = directory.listFiles(new FilenameFilter() {

             @Override
             public boolean accept(File dir, String filename) {

                 return filename.contains(".gpx");
             }
         });


		 
         
         for(File localFile : filelist)
             files.add(localFile.getAbsolutePath());

         File[] directories = directory.listFiles();
         for(File recursiveDir : directories)
         {
        	 if(recursiveDir.isDirectory())
        		 recursiveDirectoryReader(recursiveDir, files);
         }
        return files;     
     }
	
}
