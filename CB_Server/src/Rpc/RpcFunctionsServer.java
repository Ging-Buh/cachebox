package Rpc;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CB_Core.CB_Core_Settings;
import CB_Core.Database;
import CB_Core.Types.CacheListDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.ExportEntry;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_RpcCore.Functions.RpcAnswer_ExportChangesToServer;
import CB_RpcCore.Functions.RpcAnswer_GetCacheList;
import CB_RpcCore.Functions.RpcMessage_ExportChangesToServer;
import CB_RpcCore.Functions.RpcMessage_GetCacheList;
import CB_RpcCore.Functions.RpcMessage_GetExportList;
import CB_Utils.Lists.CB_List;
import cb_rpc.Functions.RpcAnswer;
import cb_rpc.Functions.RpcMessage;
import cb_server.DAO.GetExportListDao;

public class RpcFunctionsServer {
	// speichert geladene CacheLists anhand der Categoriy
	private static HashMap<Long, CacheList> loadedCacheLists = new HashMap<Long, CacheList>();
	public static int jettyPort = 80;
	public static Logger log = null;

	public RpcAnswer Msg(RpcMessage message) {
		if (log == null) {
			log = LoggerFactory.getLogger(RpcFunctionsServer.class);
		}

		if (message instanceof RpcMessage_GetExportList) {
			GetExportListDao dao = new GetExportListDao();

			RpcAnswer answer = dao.getList();
			return answer;
		} else if (message instanceof RpcMessage_GetCacheList) {
			// Debug-Meldungen
			log.debug("DescriptionImageFolder: " + CB_Core_Settings.DescriptionImageFolder.getValue());
			log.debug("SpoilerFolder: " + CB_Core_Settings.SpoilerFolder.getValue());

			RpcMessage_GetCacheList msg = (RpcMessage_GetCacheList) message;

			CacheList loadedCacheList = null;
			//xx			if (msg.getStartIndex() == 0) {
			// erster Aufruf -> CachListe erzeugen und aus DB laden
			if (loadedCacheLists.containsKey(msg.getCategoryId())) {
				// bereits vorhandene CacheList entfernen damit diese neu geladen werden kann
				loadedCacheLists.remove(msg.getCategoryId());
			}
			loadedCacheList = new CacheList();
			String joinString = "INNER JOIN GPXFilenames gpx on GpxFilename_Id=gpx.Id";
			String whereString = "gpx.CategoryId=" + msg.getCategoryId();
			whereString += " order by c.Id limit " + String.valueOf(msg.getCount()) + " offset " + String.valueOf(msg.getStartIndex());
			CacheListDAO dao = new CacheListDAO();
			dao.ReadCacheList(loadedCacheList, joinString, whereString, true, true, false);
			// geladene CacheList zur Liste der gespeicherten CacheLists hinzufügen
			loadedCacheLists.put(msg.getCategoryId(), loadedCacheList);
			//xx			} else {
			// CacheList müsste bereits geladen sein -> nur noch daraus die entsprechenden Caches übertragen
			//xx				if (loadedCacheLists.containsKey(msg.getCategoryId())) {
			//xx					loadedCacheList = loadedCacheLists.get(msg.getCategoryId());
			//xx				}
			//xx			}
			if (loadedCacheList != null) {
				//				CacheList cacheList = new CacheList();

				int start = msg.getStartIndex();
				int count = msg.getCount();
				boolean dataAvailable = start + count < loadedCacheList.size() - 1;
				log.debug("CacheList loaded: " + start + "-" + count + "-" + dataAvailable);
				//				for (int i = start; i < start + count; i++) {
				//					if (i >= loadedCacheList.size()) {
				//						break;	// keine weiteren Daten
				//					}
				//					
				//					Cache cache = loadedCacheList.get(i);
				//					cacheList.add(cache);
				//				}
				CacheList cacheList = loadedCacheList;
				RpcAnswer_GetCacheList answer = new RpcAnswer_GetCacheList(0);

				for (int i = 0, n = cacheList.size(); i < n; i++) {
					try {
						Cache cache = cacheList.get(i);
						log.debug("Cache: " + cache.getGcCode());
						CB_List<LogEntry> logs = Database.Logs(cache);
						int maxLogCount = 10;
						int actLogCount = 0;
						for (int j = 0, m = logs.size(); j < m; j++) {
							actLogCount++;
							if (actLogCount > maxLogCount)
								break;
							answer.addLog(logs.get(j));
						}
						cache.loadSpoilerRessources();
						// URL f�r den Download der Spoiler setzen
						if (cache.getSpoilerRessources() != null) {
							for (int j = 0, m = cache.getSpoilerRessources().size(); j < m; j++) {
								ImageEntry image = cache.getSpoilerRessources().get(j);
								String path = "";
								//					log.debug("Image: " + image.LocalPath);
								int pos = image.LocalPath.indexOf(CB_Core_Settings.DescriptionImageFolder.getValue());
								if (pos < 0) {
									pos = image.LocalPath.indexOf(CB_Core_Settings.SpoilerFolder.getValue());
									if (pos < 0) {
										continue;
									}
									path = ":" + jettyPort + "/spoilers/";
								} else {
									path = ":" + jettyPort + "/images/";
								}

								image.ImageUrl = path;
							}
						}
					} catch (Exception ex) {
						log.error("Cache: " + ex.getMessage());
					}
				}
				log.debug("Send Answer: " + cacheList.size());
				try {
					answer.setCacheList(cacheList);
					answer.setDataAvailable(cacheList.size() > 0);
					log.debug("Answer Sent");
				} catch (Exception ex) {
					log.error("Answer: " + ex.getMessage());
				}
				return answer;
			} else {
				// Fehler, keine CacheList geladen
				RpcAnswer_GetCacheList answer = new RpcAnswer_GetCacheList(-1);
				return answer;
			}
		} else if (message instanceof RpcMessage_ExportChangesToServer) {
			RpcMessage_ExportChangesToServer msg = (RpcMessage_ExportChangesToServer) message;
			log.debug("Export vom ACB!!!");

			for (ExportEntry entry : msg.getExportList()) {
				try {
					switch (entry.changeType) {
					case Archived:
						break;
					case Available:
						break;
					case DeleteWaypoint:
						break;
					case Found:
						log.debug("New Found Status: " + entry.cacheId);
						Database.SetFound(entry.cacheId, true);
						break;
					case NewWaypoint:
						log.debug("New Waypoint: " + entry.cacheId + " - " + entry.wpGcCode + " - " + (entry.waypoint != null));
						if (entry.waypoint != null) {
							WaypointDAO wpdao = new WaypointDAO();
							wpdao.WriteToDatabase(entry.waypoint);
						} else {
							log.debug("Waypoint is null!!!");
						}
						break;
					case NotArchived:
						break;
					case NotAvailable:
						break;
					case NotFound:
						log.debug("New not Found Status: " + entry.cacheId);
						Database.SetFound(entry.cacheId, false);
						break;
					case NotesText:
						log.debug("New Notes Text: " + entry.note);
						Database.SetNote(entry.cacheId, entry.note);
						break;
					case NumTravelbugs:
						break;
					case SolverText:
						// Change Solver Text
						log.debug("New Solver Text: " + entry.solver);
						Database.SetSolver(entry.cacheId, entry.solver);
						break;
					case Undefined:
						break;
					case WaypointChanged:
						log.debug("Waypoint changed: " + entry.cacheId);
						WaypointDAO wpdao = new WaypointDAO();
						entry.waypoint.setCheckSum(0); // auf 0 setzen damit der WP in der DB upgedated wird
						wpdao.UpdateDatabase(entry.waypoint);
						break;
					default:
						break;

					}
				} catch (Exception ex) {
					log.error("Error Export to CBS: " + ex.getMessage());
				}
			}
			// TODO Antwort kann Informationen über Konflikte übergeben, ebenfalls als exportList
			// für jeden Konflikt wird ein Eintrag (ExportEntry) in dieser Liste übergeben
			RpcAnswer answer = new RpcAnswer_ExportChangesToServer(0, null);
			return answer;
		}
		return new RpcAnswer(-1);
	}

	public int Add(int i, int j) {
		// TODO Auto-generated method stub
		return (i + j) * 2;
	}
}
