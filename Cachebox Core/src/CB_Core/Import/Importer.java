package CB_Core.Import;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.zip.ZipException;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.DAO.GCVoteDAO;
import CB_Core.DAO.ImageDAO;
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
	 * Importiert die GPX files, die sich in diesem Verzeichniss befinden. Auch wenn sie sich in einem Zip-File befinden.
	 * 
	 * @param directoryPath
	 * @param ip
	 * @return Cache_Log_Return mit dem Inhalt aller Importierten GPX Files
	 */
	public void importGpx(String directoryPath, ImporterProgress ip)
	{
		// resest import Counter

		GPXFileImporter.CacheCount = 0;
		GPXFileImporter.LogCount = 0;

		// Extract all Zip Files!
		ArrayList<File> ordnerInhalt_Zip = FileIO.recursiveDirectoryReader(new File(directoryPath), new ArrayList<File>(), "zip", false);

		ip.setJobMax("ExtractZip", ordnerInhalt_Zip.size());

		for (File tmpZip : ordnerInhalt_Zip)
		{
			try
			{
				Thread.sleep(20);
			}
			catch (InterruptedException e2)
			{
				return; // Thread Canceld
			}

			ip.ProgressInkrement("ExtractZip", "", false);
			// Extract ZIP
			try
			{
				UnZip.extractFolder(tmpZip.getAbsolutePath());
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

		if (ordnerInhalt_Zip.size() == 0)
		{
			ip.ProgressInkrement("ExtractZip", "", true);
		}

		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e2)
		{
			return; // Thread Canceld
		}

		// Import all GPX files
		File[] FileList = GetFilesToLoad(directoryPath);

		ip.setJobMax("AnalyseGPX", FileList.length);

		ImportHandler importHandler = new ImportHandler();

		Integer countwpt = 0;
		HashMap<String, Integer> wptCount = new HashMap<String, Integer>();

		for (File File : FileList)
		{

			try
			{
				Thread.sleep(20);
			}
			catch (InterruptedException e2)
			{
				return; // Thread Canceld
			}

			ip.ProgressInkrement("AnalyseGPX", File.getName(), false);

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

				e1.printStackTrace();
			}
			catch (IOException e)
			{

				e.printStackTrace();
			}

			wptCount.put(File.getAbsolutePath(), countwpt);
			countwpt = 0;
		}

		if (FileList.length == 0)
		{
			ip.ProgressInkrement("AnalyseGPX", "", true);
		}

		for (Integer count : wptCount.values())
		{
			countwpt += count;
		}

		ip.setJobMax("ImportGPX", FileList.length + countwpt);
		for (File File : FileList)
		{
			try
			{
				Thread.sleep(20);
			}
			catch (InterruptedException e2)
			{
				return; // Thread Canceled
			}

			ip.ProgressInkrement("ImportGPX", "Import: " + File.getName(), false);
			GPXFileImporter importer = new GPXFileImporter(File, ip);
			try
			{
				importer.doImport(importHandler, wptCount.get(File.getAbsolutePath()));
			}
			catch (Exception e)
			{
				Logger.Error("Core.Importer.ImportGpx", "importer.doImport => " + File.getAbsolutePath(), e);
				e.printStackTrace();
			}
		}

		if (FileList.length == 0)
		{
			ip.ProgressInkrement("ImportGPX", "", true);
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

			try
			{
				Thread.sleep(20);
			}
			catch (InterruptedException e2)
			{
				return; // Thread Canceld
			}

			i++;

			ip.ProgressInkrement("sendGcVote", "Sending Votes (" + String.valueOf(i) + " / " + String.valueOf(pendingVotes.size()) + ")",
					false);

			Boolean ret = GCVote.SendVotes(Config.settings.GcLogin.getValue(), Config.settings.GcVotePassword.getValue(), info.Vote,
					info.URL, info.GcCode);

			if (ret)
			{
				gcVoteDAO.updatePendingVote(info.Id);
			}
		}

		if (pendingVotes.size() == 0)
		{
			ip.ProgressInkrement("sendGcVote", "No Votes to send.", true);
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
				try
				{
					Thread.sleep(20);
				}
				catch (InterruptedException e2)
				{
					return; // Thread Canceld
				}

				if (!info.GcCode.toLowerCase().startsWith("gc"))
				{
					ip.ProgressInkrement("importGcVote", "Not a GC.com Cache", false);
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
				ip.ProgressInkrement("importGcVote", "Query failed...", false);
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
							"Writing Ratings (" + String.valueOf(i + failCount) + " / " + String.valueOf(count) + ")", false);
				}

			}

			offset += packageSize;

		}

		if (count == 0)
		{
			ip.ProgressInkrement("importGcVote", "", true);
		}

	}

	public void importImages(String whereClause, ImporterProgress ip) // wir brachen kein delay mehr
	{
		ImageDAO imageDAO = new ImageDAO();

		Integer count = imageDAO.getImageCount(whereClause);

		ip.setJobMax("importImages", count);

		if (count == 0)
		{
			ip.ProgressInkrement("importImages", "", true);
			return;
		}

		ArrayList<String> gcCodes = imageDAO.getGcCodes(whereClause);

		int i = 0;

		for (String gccode : gcCodes)
		{
			Boolean downloadedImage = false;
			ArrayList<String> imageURLs = imageDAO.getImageURLsForCache(gccode);

			for (String url : imageURLs)
			{
				try
				{
					Thread.sleep(20);
				}
				catch (InterruptedException e2)
				{
					return; // Thread Canceld
				}
				String localFile = DescriptionImageGrabber.BuildImageFilename(gccode, URI.create(url));

				if (!FileIO.FileExists(localFile))
				{
					downloadedImage = true;
					DescriptionImageGrabber.Download(url, localFile);
				}

				i++;

				ip.ProgressInkrement("importImages",
						"Importing Images for " + gccode + " (" + String.valueOf(i) + " / " + String.valueOf(count) + ")", false);
			}

			if (downloadedImage)
			{
				ip.ProgressInkrement("importImages",
						"Importing Images for " + gccode + " (" + String.valueOf(i) + " / " + String.valueOf(count) + ")", false);

			}

		}
	}

	public void importMaps()
	{
		ProgresssChangedEventList.Call("import Map", "", 0);

	}

	public void importMail()
	{
		ProgresssChangedEventList.Call("import from Mail", "", 0);

	}

	private File[] GetFilesToLoad(String directoryPath)
	{
		// GPX sortieren

		FileIO.DirectoryExists(directoryPath);

		ArrayList<File> files = new ArrayList<File>();
		files = FileIO.recursiveDirectoryReader(new File(directoryPath), files);

		File[] fileArray = files.toArray(new File[files.size()]);

		Arrays.sort(fileArray, new Comparator<File>()
		{
			public int compare(File f1, File f2)
			{

				if (f1.getName().equalsIgnoreCase(f2.getName().replace(".gpx", "") + "-wpts.gpx"))
				{
					return 1;
				}
				else if (f2.getName().equalsIgnoreCase(f1.getName().replace(".gpx", "") + "-wpts.gpx"))
				{
					return -1;
				}
				else if (f1.lastModified() > f2.lastModified())
				{
					return 1;
				}

				else if (f1.lastModified() < f2.lastModified())
				{
					return -1;
				}

				else
				{
					return f1.getAbsolutePath().compareToIgnoreCase(f2.getAbsolutePath()) * -1;
				}
			}
		});

		return fileArray;
	}

}
