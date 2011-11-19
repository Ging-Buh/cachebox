package CB_Core.Import;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipException;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.DAO.GCVoteDAO;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GCVote.GCVote;
import CB_Core.GCVote.GCVoteCacheInfo;
import CB_Core.GCVote.RatingData;
import CB_Core.Log.Logger;

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
	public void importGpx(String directoryPath, ImporterProgress ip)
	{
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

		for (String File : FileList)
		{
			ip.ProgressInkrement("AnalyseGPX", new File(File).getName());

			BufferedReader br;
			String strLine;
			try
			{
				br = new BufferedReader(new InputStreamReader(new FileInputStream(File)));
				while ((strLine = br.readLine()) != null)
				{
					if (strLine.contains("<wpt")) countwpt++;
				}
			}
			catch (FileNotFoundException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			wptCount.put(File, countwpt);
			countwpt = 0;
		}

		for (Integer count : wptCount.values())
		{
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

		importHandler.GPXFilenameUpdateCacheCount();

	}

	public void importGcVote(String whereClause, ImporterProgress ip)
	{

		GCVoteDAO gcVoteDAO = new GCVoteDAO();

		ArrayList<GCVoteCacheInfo> pendingVotes = gcVoteDAO.getPendingGCVotes();

		ip.setJobMax("sendGcVote", pendingVotes.size());
		int i = 0;

		for (GCVoteCacheInfo info : pendingVotes)
		{
			i++;

			ip.ProgressInkrement("sendGcVote", "Sending Votes (" + String.valueOf(i) + " / " + String.valueOf(pendingVotes.size()) + ")");

			Boolean ret = GCVote.SendVotes(Config.settings.GcLogin.getValue(), Config.settings.GcVotePassword.getValue(), info.Vote,
					info.URL, info.GcCode);

			if (ret)
			{
				gcVoteDAO.updatePendingVote(info.Id);
			}
		}

		Integer count = gcVoteDAO.getCacheCountToGetVotesFor(whereClause);

		ip.setJobMax("importGcVote", count);

		int packageSize = 100;
		int offset = 0;
		int failCount = 0;
		i = 0;

		while (offset < count)
		{
			ArrayList<GCVoteCacheInfo> workpackage = gcVoteDAO.getGCVotePackage(whereClause, packageSize, i);
			ArrayList<String> requests = new ArrayList<String>();
			HashMap<String, Boolean> resetVote = new HashMap<String, Boolean>();
			HashMap<String, Long> idLookup = new HashMap<String, Long>();

			for (GCVoteCacheInfo info : workpackage)
			{
				if (!info.GcCode.toLowerCase().startsWith("gc"))
				{
					ip.ProgressInkrement("importGcVote", "Not a GC.com Cache");
					continue;
				}

				requests.add(info.GcCode);
				resetVote.put(info.GcCode, !info.VotePending);
				idLookup.put(info.GcCode, info.Id);
			}

			ArrayList<RatingData> ratingData = GCVote.GetRating(Config.settings.GcLogin.getValue(),
					Config.settings.GcVotePassword.getValue(), requests);

			if (ratingData == null)
			{
				failCount += packageSize;
				ip.ProgressInkrement("importGcVote", "Query " + String.valueOf(i + 1) + " failed...");
			}
			else
			{
				for (RatingData data : ratingData)
				{
					if (idLookup.containsKey(data.Waypoint))
					{
						if (resetVote.containsKey(data.Waypoint))
						{
							gcVoteDAO.updateRatingAndVote(idLookup.get(data.Waypoint), data.Rating, data.Vote);
						}
						else
						{
							gcVoteDAO.updateRating(idLookup.get(data.Waypoint), data.Rating);
						}
					}

					i++;

					ip.ProgressInkrement("importGcVote",
							"Writing Ratings (" + String.valueOf(i + failCount) + " / " + String.valueOf(count) + ")");
				}

			}

			offset += packageSize;

		}

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
