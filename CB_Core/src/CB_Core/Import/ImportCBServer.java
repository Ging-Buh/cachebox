package CB_Core.Import;

import CB_Core.CB_Core_Settings;
import CB_Core.CoreSettingsForward;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Database;
import CB_Core.Types.*;
import CB_RpcCore.ClientCB.RpcClientCB;
import CB_RpcCore.Functions.RpcAnswer_GetCacheList;
import CB_RpcCore.Functions.RpcAnswer_GetExportList.ListItem;
import CB_RpcCore.Functions.RpcMessage_GetCacheList;
import CB_Utils.Log.Log;
import CB_Utils.Util.SDBM_Hash;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import cb_rpc.Functions.RpcAnswer;
import cb_rpc.Settings.CB_Rpc_Settings;
import de.cb.sqlite.Database_Core.Parameters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static CB_Utils.http.Download.download;

public class ImportCBServer {
    private static final String log = "ImportCBServer";

    public void importCBServer(ArrayList<ListItem> cbServerExportList, ImporterProgress ip, boolean importImages) {
        long startTS = System.currentTimeMillis();
        long tmpTS = System.currentTimeMillis();
        int count = 0;
        int anzToLoad = 10; // Anzahl der Caches, die auf einmal geladen werden sollen
        anzToLoad = CB_Rpc_Settings.CBS_BLOCK_SIZE.getValue();
        int anzDownloadsTotal = 0;
        for (ListItem item : cbServerExportList) {
            if (item.getDownload()) {
                count++;
                // Downloads für diese Category
                int downloads = (item.getCacheCount() - 1) / anzToLoad + 1;
                anzDownloadsTotal += downloads;
            }
        }
        ip.setJobMax("importCBServer", anzDownloadsTotal);
        int actDownload = 0;

        Database.Data.sql.beginTransaction();
        try {
            for (ListItem item : cbServerExportList) {
                if (!item.getDownload())
                    continue;
                // Import
                RpcClientCB rpc = new RpcClientCB();
                boolean dataAvailable = true;
                int startIndex = 0;

                while (dataAvailable) {
                    actDownload++;
                    ip.ProgressInkrement("importCBServer", actDownload + "-" + anzDownloadsTotal + " | " + item.getDescription(), false);
                    System.out.println("Load from Server (" + (System.currentTimeMillis() - tmpTS) + "ms)");
                    tmpTS = System.currentTimeMillis();

                    RpcAnswer answer = rpc.sendRpcToServer(new RpcMessage_GetCacheList(item.getId(), startIndex, anzToLoad));
                    System.out.println("Load from Server finished (" + (System.currentTimeMillis() - tmpTS) + "ms)");
                    tmpTS = System.currentTimeMillis();
                    if (answer instanceof RpcAnswer_GetCacheList) {
                        RpcAnswer_GetCacheList gclAnswer = (RpcAnswer_GetCacheList) answer;
                        System.out.println("************* CacheList ***************");
                        // GPX-Filename und Category Eintrag prüfen
                        Category cat = CoreSettingsForward.Categories.getCategory(item.getDescription());

                        CategoryDAO catDao = new CategoryDAO();
                        // GpxFilenames Eintrag erzeugen
                        // Alle importierten Caches werdem diesem neuen GpxFilename zugeordnet
                        GpxFilename gpxFilename = cat.addGpxFilename(item.getDescription());

                        CacheDAO dao = new CacheDAO();
                        WaypointDAO wayDao = new WaypointDAO();
                        LogDAO logDAO = new LogDAO();

                        for (int i = 0, n = gclAnswer.getCacheList().size(); i < n; i++) {
                            Cache cache = gclAnswer.getCacheList().get(i);
                            // Alle importierten Caches werdem diesem neuen GpxFilename zugeordnet
                            cache.setGPXFilename_ID(gpxFilename.Id);

                            // Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
                            if (!dao.UpdateDatabase(cache)) {
                                dao.WriteToDatabase(cache);
                            }

                            if ((cache.getTmpNote() != null) && (cache.getTmpNote().length() > 0)) {
                                cache.setNoteChecksum((int) SDBM_Hash.sdbm(cache.getTmpNote()));
                                Parameters args = new Parameters();
                                // orginal NoteChecksum in DB speichern
                                args.put("Notes", cache.getTmpNote());
                                Database.Data.sql.update("Caches", args, "id=" + cache.Id, null);

                                cache.setTmpNote(null);
                            }
                            if (cache.getTmpSolver() != null) {
                                cache.setSolverChecksum((int) SDBM_Hash.sdbm(cache.getTmpSolver()));
                                Parameters args = new Parameters();
                                args.put("Solver", cache.getTmpSolver());
                                Database.Data.sql.update("Caches", args, "id=" + cache.Id, null);

                                cache.setTmpSolver(null);
                            }

                            for (int j = 0, m = cache.waypoints.size(); j < m; j++) {
                                Waypoint waypoint = cache.waypoints.get(j);
                                wayDao.WriteToDatabase(waypoint, false); // do not store replication information
                            }
                            if (importImages && (cache.getSpoilerRessources() != null)) {
                                Log.info(log, "Import Images Anz: " + cache.getSpoilerRessources().size());
                                // TODO - Delete old no longer valid Spoiler Images
                                // this can be done with Hash code of Path of URL which is added to Image Name of Spoilers
                                for (int j = 0, m = cache.getSpoilerRessources().size(); j < m; j++) {
                                    ImageEntry image = cache.getSpoilerRessources().get(j);
                                    String url = CB_Rpc_Settings.CBS_IP.getValue();
                                    int pos = url.indexOf(":");
                                    if (pos >= 0) {
                                        url = url.substring(0, pos); // Port abschneiden, da dieser bereits in der imageURL der Images steht
                                    }
                                    url = "http://" + url + image.ImageUrl;
                                    File file = FileFactory.createFile(image.LocalPath);
                                    url += cache.getGcCode().substring(0, 4) + "/";

                                    try {
                                        url += URLEncoder.encode(file.getName().replace(" ", "%20"), "UTF-8");
                                        url = url.replace("%2520", "%20"); // replace wrong converted " " with %20
                                    } catch (UnsupportedEncodingException e) {
                                        continue;
                                    }

                                    String imagePath;
                                    if (image.ImageUrl.indexOf("/spoilers/") >= 0) {
                                        imagePath = CB_Core_Settings.SpoilerFolder.getValue() + "/" + cache.getGcCode().substring(0, 4) + "/" + file.getName();
                                        if (CB_Core_Settings.SpoilerFolderLocal.getValue().length() != 0) {
                                            imagePath = CB_Core_Settings.SpoilerFolderLocal.getValue() + "/" + cache.getGcCode().substring(0, 4) + "/" + file.getName();
                                        }
                                    } else {
                                        imagePath = CB_Core_Settings.DescriptionImageFolder.getValue() + "/" + cache.getGcCode().substring(0, 4) + "/" + file.getName();
                                        if (CB_Core_Settings.DescriptionImageFolderLocal.getValue().length() != 0) {
                                            imagePath = CB_Core_Settings.DescriptionImageFolderLocal.getValue() + "/" + cache.getGcCode().substring(0, 4) + "/" + file.getName();
                                        }
                                    }
                                    file = FileFactory.createFile(imagePath);
                                    if (!file.exists()) {
                                        Log.info(log, "Download " + imagePath + " from " + url);
                                        download(url, imagePath);
                                    } else {
                                        Log.info(log, "already downloaded " + imagePath + " from " + url);
                                    }
                                }
                            }
                        }
                        // Logs in DB eintragen. Die Logs sind hier nicht zu jedem Cache gespeichert sondern komplett in einer Liste
                        for (LogEntry log : gclAnswer.getLogs()) {
                            logDAO.WriteToDatabase(log);
                        }
                        startIndex += anzToLoad;
                        dataAvailable = gclAnswer.isDataAvailable(); // weitere Daten vorhanden?
                        // System.gc();
                    } else {
                        Log.err(log, "Laden nicht erfolgreich. Fehlerhafte Serialisierung?");
                        dataAvailable = false; // keine weiteren Daten laden!!!
                    }
                }
            }
            Database.Data.sql.setTransactionSuccessful();
        } finally {
            Database.Data.sql.endTransaction();
        }
        long endTS = System.currentTimeMillis();
        System.out.println("Import Ende (" + (System.currentTimeMillis() - tmpTS) + ")");
        tmpTS = System.currentTimeMillis();
        System.out.println("Import Gesamtdauer: " + (endTS - startTS) + "ms");
        // Aufzeichnen der Änderungen aktivieren
        if (Database.Data.MasterDatabaseId == 0) {
            Database.Data.MasterDatabaseId = 1;
            Database.Data.WriteConfigLong("MasterDatabaseId", Database.Data.MasterDatabaseId);
        }

    }
}
