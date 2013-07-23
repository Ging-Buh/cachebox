package CB_Core.Import;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.zip.ZipException;

import CB_Core.CoreSettingsForward;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.GCVoteDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.DB.Database_Core.Parameters;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GCVote.GCVote;
import CB_Core.GCVote.GCVoteCacheInfo;
import CB_Core.GCVote.RatingData;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Util.FileIO;

public class Importer
{
	public void importGC(ArrayList<PQ> pqList)
	{
		ProgresssChangedEventList.Call("import Gc.com", "", 0);

	}

	boolean canceld = false;

	public void cancel()
	{
		canceld = true;
	}

	/**
	 * Importiert die GPX files, die sich in diesem Verzeichniss befinden. Auch wenn sie sich in einem Zip-File befinden. Oder das GPX-File
	 * falls eine einzelne Datei �bergeben wird.
	 * 
	 * @param directoryPath
	 * @param ip
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @param CategorieList
	 *            GlobalCore.Categories
	 * @return Cache_Log_Return mit dem Inhalt aller Importierten GPX Files
	 */
	public void importGpx(String directoryPath, ImporterProgress ip)
	{
		// resest import Counter

		GPXFileImporter.CacheCount = 0;
		GPXFileImporter.LogCount = 0;

		// Extract all Zip Files!

		File file = new File(directoryPath);

		if (file.isDirectory())
		{
			ArrayList<File> ordnerInhalt_Zip = FileIO.recursiveDirectoryReader(file, new ArrayList<File>(), "zip", false);

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
				Thread.sleep(50);
			}
			catch (InterruptedException e2)
			{
				return; // Thread Canceld
			}
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

		// Indiziere DB
		CacheInfoList.IndexDB();

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

		// Indexierte CacheInfos zur�ck schreiben
		CacheInfoList.writeListToDB();
		CacheInfoList.dispose();

	}

	/**
	 * @param whereClause
	 * @param ip
	 * @param userName
	 *            Config.settings.GcLogin.getValue()
	 * @param GcVotePassword
	 *            Config.settings.GcVotePassword.getValue()
	 */
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

			Boolean ret = GCVote.SendVotes(CoreSettingsForward.userName, CoreSettingsForward.GcVotePassword, info.Vote, info.URL,
					info.GcCode);

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

			ArrayList<RatingData> ratingData = GCVote.GetRating(CoreSettingsForward.userName, CoreSettingsForward.GcVotePassword, requests);

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

	/***
	 * @param ip
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal
	 * @param Staging
	 *            Config.settings.StagingAPI.getValue()
	 * @param AccessToken
	 *            Config.GetAccessToken(true)
	 * @param conectionTimeout
	 *            Config.settings.conection_timeout.getValue()
	 * @param socketTimeout
	 *            Config.settings.socket_timeout.getValue()
	 * @return ErrorCode Use with<br>
	 *         if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 *         {<br>
	 *         GL.that.Toast(ConnectionError.INSTANCE);<br>
	 *         return;<br>
	 *         }<br>
	 * <br>
	 *         if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 *         {<br>
	 *         GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 *         return;<br>
	 *         }<br>
	 */
	public int importImages(ImporterProgress ip)
	{

		int ret = 0;

		CacheDAO CacheDao = new CacheDAO();

		// Index DB
		CacheInfoList.IndexDB();

		// get all GcCodes from Listing changed caches without Typ==4 (ErthCache)
		ArrayList<String> gcCodes = CacheDao.getGcCodesFromMustLoadImages();

		// refresch all Image Url�s
		ip.setJobMax("importImageUrls", gcCodes.size());
		int counter = 0;
		for (String gccode : gcCodes)
		{
			if (gccode.toLowerCase().startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
			{

				// API zugriff nur mit g�ltigem API Key

				int retChk = GroundspeakAPI.isValidAPI_Key(true);

				if (retChk > 0)
				{
					ret = importApiImages(gccode, CacheInfoList.getIDfromGcCode(gccode));
					if (ret < 0) return ret;
				}

				ip.ProgressInkrement("importImageUrls",
						"get Image Url�s for " + gccode + " (" + String.valueOf(counter++) + " / " + String.valueOf(gcCodes.size()) + ")",
						false);
			}
		}

		ImageDAO imageDAO = new ImageDAO();

		// Die Where Clusel sorgt df�r, dass nur die Anzahl der zu ladenden Bilder zur�ck gegeben wird.
		// Da keine Bilder von ErthCaches geladen werden, wird hier auch der Typ 4 ausgelassen.
		String where = " Type<>4 and (ImagesUpdated=0 or DescriptionImagesUpdated=0)";

		Integer count = imageDAO.getImageCount(where);

		ip.setJobMax("importImages", count);

		if (count == 0)
		{
			ip.ProgressInkrement("importImages", "", true);
			return 0;
		}

		int i = 0;

		for (String gccode : gcCodes)
		{
			Boolean downloadedImage = false;
			ArrayList<String> imageURLs = imageDAO.getImageURLsForCache(gccode);

			boolean downloadFaild = false;

			for (String url : imageURLs)
			{
				try
				{
					Thread.sleep(5);
				}
				catch (InterruptedException e2)
				{
					return 0; // Thread Canceld
				}
				String localFile = DescriptionImageGrabber.BuildImageFilename(gccode, URI.create(url));

				if (!FileIO.FileExists(localFile))
				{
					downloadedImage = true;
					if (downloadFaild || !DescriptionImageGrabber.Download(url, localFile))
					{
						downloadFaild = true;
					}
				}

				i++;

				if (gccode.toLowerCase().startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
				{
					ip.ProgressInkrement("importImages",
							"Importing Images for " + gccode + " (" + String.valueOf(i) + " / " + String.valueOf(count) + ")", false);
				}
			}

			if (downloadedImage)
			{
				if (gccode.toLowerCase().startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
				{
					ip.ProgressInkrement("importImages",
							"Importing Images for " + gccode + " (" + String.valueOf(i) + " / " + String.valueOf(count) + ")", false);
				}

			}
			if (!downloadFaild)
			{
				// set DescriptionImagesUpdated and ImagesUpdated
				CacheInfoList.setImageUpdated(gccode);
			}
		}

		// Write CacheInfoList back
		CacheInfoList.writeListToDB();
		CacheInfoList.dispose();

		return ret;
	}

	/**
	 * @param Staging
	 *            Config.settings.StagingAPI.getValue()
	 * @param ip
	 * @param importImages
	 * @param importSpoiler
	 * @param where
	 *            [Last Filter]GlobalCore.LastFilter.getSqlWhere();
	 * @param DescriptionImageFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param AccessToken
	 *            Config.GetAccessToken(true)
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @param conectionTimeout
	 *            Config.settings.conection_timeout.getValue()
	 * @param socketTimeout
	 *            Config.settings.socket_timeout.getValue()
	 * @return ErrorCode Use with<br>
	 *         if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 *         {<br>
	 *         GL.that.Toast(ConnectionError.INSTANCE);<br>
	 *         return;<br>
	 *         }<br>
	 * <br>
	 *         if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 *         {<br>
	 *         GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 *         return;<br>
	 *         }<br>
	 */
	public int importImagesNew(ImporterProgress ip, boolean importImages, boolean importSpoiler, String where)
	{

		// refresch all Image Url�s

		int ret = 0;

		String sql = "select Id, Description, Name, GcCode, Url, ImagesUpdated, DescriptionImagesUpdated from Caches";
		if (where.length() > 0) sql += " where " + where;
		CoreCursor reader = Database.Data.rawQuery(sql, null);

		int cnt = -1;
		int numCaches = reader.getCount();
		ip.setJobMax("importImages", numCaches);

		if (reader.getCount() > 0)
		{
			reader.moveToFirst();
			while (reader.isAfterLast() == false && !canceld)
			{
				cnt++;
				long id = reader.getLong(0);
				String name = reader.getString(2);
				String gcCode = reader.getString(3);

				if (gcCode.toLowerCase().startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
				{
					ip.ProgressInkrement("importImages",
							"Importing Images for " + gcCode + " (" + String.valueOf(cnt) + " / " + String.valueOf(numCaches) + ")", false);

					boolean additionalImagesUpdated = false;
					boolean descriptionImagesUpdated = false;

					if (!reader.isNull(5))
					{
						additionalImagesUpdated = reader.getInt(5) != 0;
					}
					if (!reader.isNull(6))
					{
						descriptionImagesUpdated = reader.getInt(6) != 0;
					}

					String description = reader.getString(1);
					String uri = reader.getString(4);

					if (!importImages)
					{
						// do not import Description Images
						descriptionImagesUpdated = true;
					}
					if (!importSpoiler)
					{
						// do not import Spoiler Images
						additionalImagesUpdated = true;
					}
					ret = importImagesForCacheNew(ip, descriptionImagesUpdated, additionalImagesUpdated, id, gcCode, name, description,
							uri, false);
				}
				reader.moveToNext();
			}
		}

		reader.close();
		return ret;
	}

	/**
	 * Importiert alle Spoiler Images f�r einen Cache (�ber die API-Funktion)
	 * 
	 * @param Staging
	 *            Config.settings.StagingAPI.getValue()
	 * @param ip
	 * @param cache
	 * @param DescriptionImageFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param AccessToken
	 *            Config.GetAccessToken(true)
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @param conectionTimeout
	 *            Config.settings.conection_timeout.getValue()
	 * @param socketTimeout
	 *            Config.settings.socket_timeout.getValue()
	 * @return ErrorCode Use with<br>
	 *         if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 *         {<br>
	 *         GL.that.Toast(ConnectionError.INSTANCE);<br>
	 *         return;<br>
	 *         }<br>
	 * <br>
	 *         if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 *         {<br>
	 *         GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 *         return;<br>
	 *         }<br>
	 */
	public int importSpoilerForCacheNew(ImporterProgress ip, Cache cache)
	{
		return importImagesForCacheNew(ip, true, false, cache.Id, cache.GcCode, cache.Name, "", "", true);
	}

	/**
	 * Bilderimport. Wenn descriptionImagesUpdated oder additionalImagesUpdated == false dann werden die entsprechenden Images importiert
	 * Aber nur dann wenn CheckLocalImages daf�r false liefert. wenn importAlways == true -> die Bilder werden unabh�ngig davon, ob schon
	 * welche existieren importiert
	 * 
	 * @param Staging
	 *            Config.settings.StagingAPI.getValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param AccessToken
	 *            Config.GetAccessToken(true)
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @param conectionTimeout
	 *            Config.settings.conection_timeout.getValue()
	 * @param socketTimeout
	 *            Config.settings.socket_timeout.getValue()
	 * @return ErrorCode Use with<br>
	 *         if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 *         {<br>
	 *         GL.that.Toast(ConnectionError.INSTANCE);<br>
	 *         return;<br>
	 * <br>
	 * <br>
	 *         if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 *         {<br>
	 *         GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 *         return;<br>
	 * <br>
	 */
	private int importImagesForCacheNew(ImporterProgress ip, boolean descriptionImagesUpdated, boolean additionalImagesUpdated, long id,
			String gcCode, String name, String description, String uri, boolean importAlways)
	{
		boolean dbUpdate = false;

		if (!importAlways)
		{
			if (!descriptionImagesUpdated)
			{
				if (CoreSettingsForward.DescriptionImageFolderLocal.length() > 0)
				{
					descriptionImagesUpdated = CheckLocalImages(CoreSettingsForward.DescriptionImageFolderLocal, gcCode);
				}
				else
				{
					descriptionImagesUpdated = CheckLocalImages(CoreSettingsForward.DescriptionImageFolder, gcCode);
				}

				if (descriptionImagesUpdated)
				{
					dbUpdate = true;
				}
			}
			if (!additionalImagesUpdated)
			{
				if (CoreSettingsForward.SpoilerFolderLocal.length() > 0)
				{
					additionalImagesUpdated = CheckLocalImages(CoreSettingsForward.SpoilerFolderLocal, gcCode);
				}
				else
				{
					additionalImagesUpdated = CheckLocalImages(CoreSettingsForward.SpoilerFolder, gcCode);
				}

				if (additionalImagesUpdated)
				{
					dbUpdate = true;
				}
			}
		}
		if (dbUpdate)
		{
			Parameters args = new Parameters();
			args.put("ImagesUpdated", additionalImagesUpdated);
			args.put("DescriptionImagesUpdated", descriptionImagesUpdated);
			Database.Data.update("Caches", args, "Id = ?", new String[]
				{ String.valueOf(id) });
		}

		return DescriptionImageGrabber.GrabImagesSelectedByCache(ip, descriptionImagesUpdated, additionalImagesUpdated, id, gcCode, name,
				description, uri);
	}

	/**
	 * �berpr�ft, ob f�r den gew�hlten Cache die Bilder nicht geladen werden m�ssen
	 * 
	 * @return true wenn schon Images existieren und keine .changed oder .1st Datei ansonsten false
	 */
	private boolean CheckLocalImages(String path, final String GcCode)
	{
		boolean retval = true;

		String imagePath = path + "/" + GcCode.substring(0, 4);
		boolean imagePathDirExists = FileIO.DirectoryExists(imagePath);

		if (imagePathDirExists)
		{
			File dir = new File(imagePath);
			FilenameFilter filter = new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String filename)
				{

					filename = filename.toLowerCase();
					if (filename.indexOf(GcCode.toLowerCase()) == 0)
					{
						return true;
					}
					return false;
				}
			};
			String[] files = dir.list(filter);

			if (files.length > 0)
			{
				for (String file : files)
				{
					if (file.endsWith(".1st") || file.endsWith(".changed"))
					{
						if (file.endsWith(".changed"))
						{
							File f = new File(file);
							try
							{
								f.delete();
							}
							catch (Exception ex)
							{
							}
						}
						retval = false;
					}
				}
			}
			else
			{
				retval = false;
			}
		}
		else
		{
			retval = false;
		}

		return retval;
	}

	/**
	 * @param Staging
	 *            Config.settings.StagingAPI.getValue()
	 * @param GcCode
	 * @param ID
	 * @param AccessToken
	 *            Config.GetAccessToken(true)
	 * @param conectionTimeout
	 *            Config.settings.conection_timeout.getValue()
	 * @param socketTimeout
	 *            Config.settings.socket_timeout.getValue()
	 * @return ErrorCode Use with<br>
	 *         if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 *         {<br>
	 *         GL.that.Toast(ConnectionError.INSTANCE);<br>
	 *         return;<br>
	 *         }<br>
	 * <br>
	 *         if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 *         {<br>
	 *         GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 *         return;<br>
	 *         }<br>
	 */
	private int importApiImages(String GcCode, long ID)
	{

		int ret = 0;

		ImportHandler importHandler = new ImportHandler();
		LinkedList<String> allImages = new LinkedList<String>();
		ArrayList<String> apiImages = new ArrayList<String>();

		if (GcCode.toLowerCase().startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
		{
			int result = GroundspeakAPI.getImagesForGeocache(GcCode, apiImages);

			if (result == GroundspeakAPI.IO)
			{
				for (String image : apiImages)
				{
					if (image.contains("/log/")) continue; // do not import log-images
					if (!allImages.contains(image)) allImages.add(image);
				}
				while (allImages != null && allImages.size() > 0)
				{
					String url;
					url = allImages.poll();

					ImageEntry image = new ImageEntry();

					image.CacheId = ID;
					image.GcCode = GcCode;
					image.Name = url.substring(url.lastIndexOf("/") + 1);
					image.Description = "";
					image.ImageUrl = url;
					image.IsCacheImage = true;

					importHandler.handleImage(image, true);

				}
			}

			if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
			{
				ret = GroundspeakAPI.CONNECTION_TIMEOUT;
			}
			if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
			{
				ret = GroundspeakAPI.API_IS_UNAVAILABLE;
			}
		}
		return ret;

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
		ArrayList<File> files = new ArrayList<File>();

		File file = new File(directoryPath);
		if (file.isFile())
		{
			files.add(file);
		}
		else
		{
			if (FileIO.DirectoryExists(directoryPath))
			{
				files = FileIO.recursiveDirectoryReader(new File(directoryPath), files);
			}
		}

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
