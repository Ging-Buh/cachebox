package cb_server.Import;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CB_Core.CB_Core_Settings;
import CB_Core.CacheListChangedEventList;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Api.ApiGroundspeak_GetPocketQueryData;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.PocketQuery;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.PocketqueryDAO;
import CB_Core.Import.Importer;

import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

import cb_server.CacheboxServer;
import cb_server.Config;

public class ImportScheduler implements Runnable {
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private Future<?> future = null;
	private static Logger log;
	private boolean importRunning = false;
	public static ImportScheduler importScheduler = new ImportScheduler();
	private boolean stopAfterImport = false;

	public ImportScheduler() {
		log = LoggerFactory.getLogger(CacheboxServer.class);
	}

	public void start() {
		stop();
		stopAfterImport = false;
		int interval = Config.settings.PQImportInterval.getValue();
		if (interval > 0) {
			log.debug("Start Import Scheduler: " + interval);
			future = scheduler.scheduleAtFixedRate(this, 1, interval, TimeUnit.MINUTES);
		}
	}

	public void stop() {
		if (future != null) {
			log.debug("Stop Import Scheduler");
			future.cancel(true);
			future = null;
		}
	}

	public void startOnce() {
		stop();
		stopAfterImport = true;
		int interval = Config.settings.PQImportInterval.getValue();

		log.debug("Start Import Scheduler: " + interval);
		future = scheduler.scheduleAtFixedRate(this, 1, Long.MAX_VALUE, TimeUnit.SECONDS);

	}

	@Override
	public void run() {
		System.out.println("run Import");
		//		ProgresssChangedEventList.Call("ProgressChanged", 100);
		//		if (true) return;
		log.info("Start Import");
		if (importRunning) {
			log.debug("Import already started");
			// wenn der Import noch l�uft, kein 2. mal starten!
			return;
		}
		importRunning = true;
		ServerImporterProgress ip = new ServerImporterProgress();
		try {
			// Import ZIP GPX Files
			Importer importer = new Importer();
			try {
				boolean importPQfromGC = true;
				boolean importGPX = true;
				boolean importImages = true;
				boolean importSpoiler = true;
				// if Import PQ
				if (importPQfromGC) {
					ip.addStep(ip.new Step("importGC", 4));
				}
				// if Import GPX
				if (importGPX) {
					ip.addStep(ip.new Step("ExtractZip", 1));
					ip.addStep(ip.new Step("AnalyseGPX", 1));
					ip.addStep(ip.new Step("ImportGPX", 4));
				}
				// if Import Vote
				//				ip.addStep(ip.new Step("sendGcVote", 1));
				//				ip.addStep(ip.new Step("importGcVote", 4));
				// if Import Images
				if (importImages) {
					ip.addStep(ip.new Step("importImages", 10));
				}
				// if Clean Logs
				//				ip.addStep(ip.new Step("DeleteLogs", 1));
				// if CompactDB
				//				ip.addStep(ip.new Step("CompactDB", 1));

				if (importPQfromGC) {
					ip.setJobMax("importGC", 10);
					ip.ProgressChangeMsg("importGC", "Download PQ-List from GC");
					// get List of all PQ's to import (from Setting)
					String pqsToImport = ";" + Config.settings.PQImportNames.getValue() + ";";

					// Import PQs
					ArrayList<PQ> pqList = new ArrayList<PQ>();
					log.debug("Load PQ-List");
					CB_Core.Api.PocketQuery.GetPocketQueryList(pqList);
					ip.setJobMax("importGC", pqList.size() + 1);
					log.debug("Load PQ-List ready");
					ApiGroundspeak_GetPocketQueryData ipq = new ApiGroundspeak_GetPocketQueryData();
					PocketqueryDAO dao = new PocketqueryDAO();
					for (PQ pq : pqList) {
						ip.ProgressInkrement("importGC", "Download PQ - " + pq.Name, false);
						log.debug("Load PQ " + pq.Name);
						if (!pqsToImport.contains(pq.Name)) {
							// not in Import List
							log.debug("Not in PQ-List to import!");
							continue;
						}
						Date lastGenerated = dao.getLastGeneratedDate(pq.Name);
						if (lastGenerated == null) {
							// lastGenerated == null -> PQ wurde in der DB nicht gefunden -> importieren
							lastGenerated = new Date(0);
						}
						if (lastGenerated.getTime() >= pq.DateLastGenerated.getTime()) {
							// diese PQ mit dem Timestamp wurde schon importiert -> nicht nochmal
							log.debug("PQ " + pq.Name + " already imported!");
							continue;
						}

						// Zipped Pocketquery
						int i = PocketQuery.DownloadSinglePocketQuery(pq, CB_Core_Settings.PocketQueryFolder.getValue());
						if (i == 0) {
							// Importierte PQ in DB speichern
							dao.writeToDatabase(pq);
						}
						System.out.println(i);
					}
					ip.ProgressInkrement("importGC", "Download PQ-List from GC finished", true);
				}

				if (importGPX) {
					System.gc();
					long startTime = System.currentTimeMillis();

					Database.Data.beginTransaction();
					//					Database.Data.Query.clear();
					try {

						importer.importGpx(CB_Core_Settings.PocketQueryFolder.getValue(), ip);

						Database.Data.setTransactionSuccessful();
					} catch (Exception exc) {
						exc.printStackTrace();
					}
					Database.Data.endTransaction();

					//					if (BreakawayImportThread.isCanceld())
					//					{
					//						cancelImport();
					//						ip.ProgressChangeMsg("", "");
					//						return;
					//					}

					log.debug("Import  GPX Import took " + (System.currentTimeMillis() - startTime) + "ms");

					System.gc();

					// del alten entpackten Ordener wenn vorhanden?
					File directory = FileFactory.createFile(CB_Core_Settings.PocketQueryFolder.getValue());
					File[] filelist = directory.listFiles();
					for (File tmp : filelist) {
						if (tmp.isDirectory()) {
							ArrayList<File> ordnerInhalt = FileIO.recursiveDirectoryReader(tmp, new ArrayList<File>());
							for (File tmp2 : ordnerInhalt) {
								tmp2.delete();
							}

						}
						tmp.delete();
					}

				}
				if (importImages || importSpoiler) {

					//					dis.setAnimationType(AnimationType.Download);
					int result = importer.importImagesNew(ip, importImages, importSpoiler, "");

					if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
						//						GL.that.Toast(ConnectionError.INSTANCE);
						ip.ProgressChangeMsg("", "");
						return;
					}

					if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
						//						GL.that.Toast(ApiUnavailable.INSTANCE);
						ip.ProgressChangeMsg("", "");
						return;
					}

					//					if (BreakawayImportThread.isCanceld())
					//					{
					//						cancelImport();
					//						ip.ProgressChangeMsg("", "");
					//						return;
					//					}
					//					dis.setAnimationType(AnimationType.Work);

				}
			} catch (/*Interrupted*/Exception e) {
				// import canceld
				//					cancelImport();
				//					FilterProperties props = GlobalCore.LastFilter;
				//					EditFilterSettings.ApplyFilter(props);
				ip.ProgressChangeMsg("", "");
				return;
			}
		} catch (Exception ex) {
			log.error("Import Error: " + ex.getMessage());
		} finally {
			importRunning = false;
		}
		System.out.println("Import finished");
		log.info("Import finished");
		ip.ProgressInkrement("importImages", "Importing Images finished", true);
		// Refresh Cache List
		log.info("Load CacheList!");
		FilterProperties lastFilter = FilterInstances.ALL;
		String sqlWhere = lastFilter.getSqlWhere(CB_Core_Settings.GcLogin.getValue());

		CacheListDAO cacheListDAO = new CacheListDAO();
		cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, true, false);
		CacheListChangedEventList.Call();
		log.debug("CacheList loaded!");

		if (stopAfterImport) {
			stop();
		}
	}
}
