package CB_Core.Import;

import java.io.File;
import java.util.ArrayList;

import CB_Core.CoreSettingsForward;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Settings.CB_Core_Settings;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_RpcCore.ClientCB.RpcClientCB;
import CB_RpcCore.Functions.RpcAnswer_GetCacheList;
import CB_RpcCore.Functions.RpcAnswer_GetExportList.ListItem;
import CB_RpcCore.Functions.RpcMessage_GetCacheList;
import CB_Utils.DB.Database_Core.Parameters;
import CB_Utils.Util.SDBM_Hash;
import cb_rpc.Functions.RpcAnswer;
import cb_rpc.Settings.CB_Rpc_Settings;

public class ImportCBServer
{
	public void importCBServer(ArrayList<ListItem> cbServerExportList, ImporterProgress ip, boolean importImages)
	{
		long startTS = System.currentTimeMillis();
		int count = 0;
		int anzToLoad = 100; // Anzahl der Caches, die auf einmal geladen werden sollen
		int anzDownloadsTotal = 0;
		for (ListItem item : cbServerExportList)
		{
			if (item.getDownload())
			{
				count++;
				// Downloads für diese Category
				int downloads = (item.getCacheCount() - 1) / anzToLoad + 1;
				anzDownloadsTotal += downloads;
			}
		}
		ip.setJobMax("importCBServer", anzDownloadsTotal);
		int actDownload = 0;

		for (ListItem item : cbServerExportList)
		{
			if (!item.getDownload()) continue;
			// Import
			RpcClientCB rpc = new RpcClientCB();
			boolean dataAvailable = true;
			int startIndex = 0;

			while (dataAvailable)
			{
				actDownload++;
				ip.ProgressInkrement("importCBServer", actDownload + "-" + anzDownloadsTotal + " | " + item.getDescription(), false);
				RpcAnswer answer = rpc.sendRpcToServer(new RpcMessage_GetCacheList(item.getId(), startIndex, anzToLoad));
				if (answer instanceof RpcAnswer_GetCacheList)
				{
					RpcAnswer_GetCacheList gclAnswer = (RpcAnswer_GetCacheList) answer;
					System.out.println("************* CacheList ***************");
					// GPX-Filename und Category Eintrag prüfen
					Category cat = null;
					CategoryDAO catDao = new CategoryDAO();
					// Suchen, ob diese Category schon vorhanden ist
					for (int i = 0, n = CoreSettingsForward.Categories.size(); i < n; i++)
					{
						Category tcat = CoreSettingsForward.Categories.get(i);
						if (tcat.GpxFilename.equals(item.getDescription()))
						{
							cat = tcat;
							break;
						}
					}
					// Wenn die Category noch nicht vorhanden ist -> erzeugen
					if (cat == null)
					{
						cat = catDao.CreateNewCategory(item.getDescription());
						CoreSettingsForward.Categories.add(cat); // Category hinzufügen
					}
					// GpxFilenames Eintrag erzeugen
					// Alle importierten Caches werdem diesem neuen GpxFilename zugeordnet
					GpxFilename gpxFilename = catDao.CreateNewGpxFilename(cat, item.getDescription());

					CacheDAO dao = new CacheDAO();
					WaypointDAO wayDao = new WaypointDAO();
					LogDAO logDAO = new LogDAO();

					for (int i = 0, n = gclAnswer.getCacheList().size(); i < n; i++)
					{
						Cache cache = gclAnswer.getCacheList().get(i);
						System.out.println(cache.Name);
						cache.GPXFilename_ID = gpxFilename.Id;

						dao.WriteToDatabase(cache);
						if ((cache.tmpNote != null) && (cache.tmpNote.length() > 0))
						{
							cache.noteCheckSum = (int) SDBM_Hash.sdbm(cache.tmpNote);
							Parameters args = new Parameters();
							// orginal NoteChecksum in DB speichern
							args.put("Notes", cache.tmpNote);
							Database.Data.update("Caches", args, "id=" + cache.Id, null);

							cache.tmpNote = null;
						}
						if (cache.tmpSolver != null)
						{
							cache.solverCheckSum = (int) SDBM_Hash.sdbm(cache.tmpSolver);
							Parameters args = new Parameters();
							args.put("Solver", cache.tmpSolver);
							Database.Data.update("Caches", args, "id=" + cache.Id, null);

							cache.tmpSolver = null;
						}

						for (int j = 0, m = cache.waypoints.size(); j < m; j++)
						{
							Waypoint waypoint = cache.waypoints.get(j);
							wayDao.WriteToDatabase(waypoint);
						}
						if (importImages)
						{
							for (int j = 0, m = cache.spoilerRessources.size(); j < m; j++)
							{
								ImageEntry image = cache.spoilerRessources.get(j);
								String url = CB_Rpc_Settings.CBS_IP.getValue();
								int pos = url.indexOf(":");
								if (pos >= 0)
								{
									url = url.substring(0, pos); // Port abschneiden, da dieser bereits in der imageURL der Images steht
								}
								url = "http://" + url + image.ImageUrl;
								File file = new File(image.LocalPath);
								url += cache.GcCode.substring(0, 4) + "/";

								url += file.getName().replace(" ", "%20");

								String imagePath;
								if (image.ImageUrl.indexOf("/spoilers/") >= 0)
								{
									imagePath = CB_Core_Settings.SpoilerFolder.getValue() + "/" + cache.GcCode.substring(0, 4) + "/"
											+ file.getName();
									if (CB_Core_Settings.SpoilerFolderLocal.getValue().length() != 0)
									{
										// Own Repo
										imagePath = CB_Core_Settings.SpoilerFolderLocal.getValue() + "/" + cache.GcCode.substring(0, 4)
												+ "/" + file.getName();
									}
								}
								else
								{
									imagePath = CB_Core_Settings.DescriptionImageFolder.getValue() + "/" + cache.GcCode.substring(0, 4)
											+ "/" + file.getName();
									if (CB_Core_Settings.DescriptionImageFolderLocal.getValue().length() != 0)
									{
										// Own Repo
										imagePath = CB_Core_Settings.DescriptionImageFolderLocal.getValue() + "/"
												+ cache.GcCode.substring(0, 4) + "/" + file.getName();
									}
								}
								file = new File(imagePath);
								if (!file.exists())
								{
									DescriptionImageGrabber.Download(url, imagePath);
								}
							}
						}
					}
					// Logs in DB eintragen. Die Logs sind hier nicht zu jedem Cache gespeichert sondern komplett in einer Liste
					for (LogEntry log : gclAnswer.getLogs())
					{
						logDAO.WriteToDatabase(log);
					}
					startIndex += anzToLoad;
					dataAvailable = gclAnswer.isDataAvailable(); // weitere Daten vorhanden?
					System.gc();
				}
				else
				{
					// Laden nicht erfolgreich
					dataAvailable = false; // keine weiteren Daten laden!!!
				}
			}
		}
		long endTS = System.currentTimeMillis();
		System.out.println("Import Dauer: " + (endTS - startTS) + "ms");
		// Aufzeichnen der Änderungen aktivieren
		if (Database.Data.MasterDatabaseId == 0)
		{
			Database.Data.MasterDatabaseId = 1;
			Database.Data.WriteConfigLong("MasterDatabaseId", Database.Data.MasterDatabaseId);
		}

	}
}
