/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_Core.Import;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.GroundspeakAPI.PQ;
import CB_Core.CB_Core_Settings;
import CB_Core.DAO.GCVoteDAO;
import CB_Core.Database;
import CB_Core.GCVote.GCVote;
import CB_Core.GCVote.GCVoteCacheInfo;
import CB_Core.GCVote.RatingData;
import CB_Utils.Events.ProgresssChangedEventList;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import de.cb.sqlite.CoreCursor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.ZipException;

public class Importer {
    private static final String log = "Importer";

    public void importGC(ArrayList<PQ> pqList) {
        ProgresssChangedEventList.Call("import Gc.com", "", 0);
    }

    /**
     * Importiert die GPX files, die sich in diesem Verzeichniss befinden. Auch wenn sie sich in einem Zip-File befinden. Oder das GPX-File
     * falls eine einzelne Datei übergeben wird.
     *
     * @param directoryPath
     * @param ip
     * @return Cache_Log_Return mit dem Inhalt aller Importierten GPX Files
     * @throws Exception
     */
    public void importGpx(String directoryPath, ImporterProgress ip) throws Exception {
        // resest import Counter

        GPXFileImporter.CacheCount = 0;
        GPXFileImporter.LogCount = 0;

        // Extract all Zip Files!

        File file = FileFactory.createFile(directoryPath);

        if (file.isDirectory()) {
            ArrayList<File> ordnerInhalt_Zip = FileIO.recursiveDirectoryReader(file, new ArrayList<File>(), "zip", false);

            ip.setJobMax("ExtractZip", ordnerInhalt_Zip.size());

            for (File tmpZip : ordnerInhalt_Zip) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e2) {
                    return; // Thread Canceld
                }

                ip.ProgressInkrement("ExtractZip", "", false);
                // Extract ZIP
                try {
                    UnZip.extractFolder(tmpZip.getAbsolutePath());
                } catch (ZipException e) {
                    Log.err(log, "ZipException", e);
                } catch (IOException e) {
                    Log.err(log, "IOException", e);
                }
            }

            if (ordnerInhalt_Zip.size() == 0) {
                ip.ProgressInkrement("ExtractZip", "", true);
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e2) {
                return; // Thread Canceld
            }
        }

        // Import all GPX files
        File[] FileList = GetFilesToLoad(directoryPath);

        ip.setJobMax("AnalyseGPX", FileList.length);

        ImportHandler importHandler = new ImportHandler();

        Integer countwpt = 0;
        HashMap<String, Integer> wptCount = new HashMap<String, Integer>();

        for (File fFile : FileList) {

            try {
                Thread.sleep(10);
            } catch (InterruptedException e2) {
                return; // Thread Canceld
            }

            ip.ProgressInkrement("AnalyseGPX", fFile.getName(), false);

            BufferedReader br;
            String strLine;
            try {
                br = new BufferedReader(new InputStreamReader(fFile.getFileInputStream()));
                while ((strLine = br.readLine()) != null) {
                    if (strLine.contains("<wpt"))
                        countwpt++;
                }
            } catch (FileNotFoundException e1) {
                Log.err(log, e1.getLocalizedMessage(), e1);
            } catch (IOException e) {
                Log.err(log, e.getLocalizedMessage(), e);
            }

            wptCount.put(fFile.getAbsolutePath(), countwpt);
            countwpt = 0;
        }

        if (FileList.length == 0) {
            ip.ProgressInkrement("AnalyseGPX", "", true);
        }

        for (Integer count : wptCount.values()) {
            countwpt += count;
        }

        // Indiziere DB
        CacheInfoList.IndexDB();

        ip.setJobMax("ImportGPX", FileList.length + countwpt);
        for (File File : FileList) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e2) {
                return; // Thread Canceled
            }

            ip.ProgressInkrement("ImportGPX", "Import: " + File.getName(), false);
            GPXFileImporter importer = new GPXFileImporter(File, ip);

            try {
                importer.doImport(importHandler, wptCount.get(File.getAbsolutePath()));
            } catch (Exception e) {
                Log.err(log, "importer.doImport => " + File.getAbsolutePath(), e);
                throw e;
            }

        }

        if (FileList.length == 0) {
            ip.ProgressInkrement("ImportGPX", "", true);
        }

        importHandler.GPXFilenameUpdateCacheCount();

        importHandler = null;

        // Indexierte CacheInfos zurück schreiben
        CacheInfoList.writeListToDB();
        CacheInfoList.dispose();

    }

    /**
     * @param whereClause
     * @param ip
     */
    public void importGcVote(String whereClause, ImporterProgress ip) {

        GCVoteDAO gcVoteDAO = new GCVoteDAO();
        int i;

        if (CB_Core_Settings.GcVotePassword.getValue().length() > 0) {
            ArrayList<GCVoteCacheInfo> pendingVotes = gcVoteDAO.getPendingGCVotes();

            ip.setJobMax("sendGcVote", pendingVotes.size());
            i = 0;

            for (GCVoteCacheInfo info : pendingVotes) {

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e2) {
                    return; // Thread Canceld
                }

                i++;

                ip.ProgressInkrement("sendGcVote", "Sending Votes (" + String.valueOf(i) + " / " + String.valueOf(pendingVotes.size()) + ")", false);

                Boolean ret = GCVote.SendVotes(CB_Core_Settings.GcLogin.getValue(), CB_Core_Settings.GcVotePassword.getValue(), info.Vote, info.URL, info.GcCode);

                if (ret) {
                    gcVoteDAO.updatePendingVote(info.Id);
                }
            }

            if (pendingVotes.size() == 0) {
                ip.ProgressInkrement("sendGcVote", "No Votes to send.", true);
            }
        }

        Integer count = gcVoteDAO.getCacheCountToGetVotesFor(whereClause);

        ip.setJobMax("importGcVote", count);

        int packageSize = 100;
        int offset = 0;
        int failCount = 0;
        i = 0;

        while (offset < count) {
            ArrayList<GCVoteCacheInfo> workpackage = gcVoteDAO.getGCVotePackage(whereClause, packageSize, i);
            ArrayList<String> requests = new ArrayList<String>();
            HashMap<String, Boolean> resetVote = new HashMap<String, Boolean>();
            HashMap<String, Long> idLookup = new HashMap<String, Long>();

            for (GCVoteCacheInfo info : workpackage) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e2) {
                    return; // Thread Canceld
                }

                if (!info.GcCode.toLowerCase(Locale.getDefault()).startsWith("gc")) {
                    ip.ProgressInkrement("importGcVote", "Not a GC.com Cache", false);
                    continue;
                }

                requests.add(info.GcCode);
                resetVote.put(info.GcCode, !info.VotePending);
                idLookup.put(info.GcCode, info.Id);
            }

            ArrayList<RatingData> ratingData = GCVote.GetRating(CB_Core_Settings.GcLogin.getValue(), CB_Core_Settings.GcVotePassword.getValue(), requests);

            if (ratingData == null || ratingData.isEmpty()) {
                failCount += packageSize;
                ip.ProgressInkrement("importGcVote", "Query failed...", false);
            } else {
                for (RatingData data : ratingData) {
                    if (idLookup.containsKey(data.Waypoint)) {
                        if (resetVote.containsKey(data.Waypoint)) {
                            gcVoteDAO.updateRatingAndVote(idLookup.get(data.Waypoint), data.Rating, data.Vote);
                        } else {
                            gcVoteDAO.updateRating(idLookup.get(data.Waypoint), data.Rating);
                        }
                    }

                    i++;

                    ip.ProgressInkrement("importGcVote", "Writing Ratings (" + String.valueOf(i + failCount) + " / " + String.valueOf(count) + ")", false);
                }

            }

            offset += packageSize;

        }

        if (count == 0) {
            ip.ProgressInkrement("importGcVote", "", true);
        }

    }

    /**
     * @param ip
     * @param importImages
     * @param importSpoiler
     * @param where         [Last Filter]FilterInstances.LastFilter.getSqlWhere();
     * @return ErrorCode Use with<br>
     */
    public int importImages(ImporterProgress ip, boolean importImages, boolean importSpoiler, String where) {

        int ret = 0;

        String sql = "select Id, Description, Name, GcCode, Url, ImagesUpdated, DescriptionImagesUpdated from Caches";
        if (where.length() > 0)
            sql += " where " + where;
        CoreCursor reader = Database.Data.sql.rawQuery(sql, null);

        int cnt = -1;
        int numCaches = reader.getCount();
        ip.setJobMax("importImages", numCaches);

        if (reader.getCount() > 0) {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                try {// for cancel/interupt Thread
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return GroundspeakAPI.OK;
                }

                if (BreakawayImportThread.isCanceled())
                    return GroundspeakAPI.OK;

                cnt++;
                try {
                    long id = reader.getLong(0);
                    String gcCode = reader.getString(3);

                    if (gcCode.toLowerCase(Locale.getDefault()).startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
                    {
                        ip.ProgressInkrement("importImages", "Importing Images for " + gcCode + " (" + String.valueOf(cnt) + " / " + String.valueOf(numCaches) + ")", false);

                        String description = reader.getString(1);
                        String uri = reader.getString(4);

                        boolean descriptionImagesUpdated;
                        if (!importImages) {
                            // do not import Description Images
                            descriptionImagesUpdated = true;
                        }
                        else {
                            if (!reader.isNull(6)) {
                                descriptionImagesUpdated = reader.getInt(6) != 0;
                            }
                            else descriptionImagesUpdated = false;
                        }
                        boolean additionalImagesUpdated;
                        if (!importSpoiler) {
                            // do not import Spoiler Images
                            additionalImagesUpdated = true;
                        }
                        else {
                            if (!reader.isNull(5)) {
                                additionalImagesUpdated = reader.getInt(5) != 0;
                            }
                            else additionalImagesUpdated = false;
                        }
                        ret = DescriptionImageGrabber.GrabImagesSelectedByCache(ip, descriptionImagesUpdated, additionalImagesUpdated, id, gcCode, description, uri);
                        if (ret < 0 ) break;
                    }
                } catch (Exception e) {
                    Log.err(log, "importImages", e);
                }
                reader.moveToNext();
            }
        }

        reader.close();
        return ret;
    }

    public void importMaps() {
        ProgresssChangedEventList.Call("import Map", "", 0);

    }

    private File[] GetFilesToLoad(String directoryPath) {
        ArrayList<File> files = new ArrayList<File>();

        File file = FileFactory.createFile(directoryPath);
        if (file.isFile()) {
            files.add(file);
        } else {
            if (FileIO.DirectoryExists(directoryPath)) {
                files = FileIO.recursiveDirectoryReader(FileFactory.createFile(directoryPath), files);
            }
        }

        File[] fileArray = files.toArray(new File[files.size()]);

        Arrays.sort(fileArray, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {

                if (f1.getName().equalsIgnoreCase(f2.getName().replace(".gpx", "") + "-wpts.gpx")) {
                    return 1;
                } else if (f2.getName().equalsIgnoreCase(f1.getName().replace(".gpx", "") + "-wpts.gpx")) {
                    return -1;
                } else if (f1.lastModified() > f2.lastModified()) {
                    return 1;
                } else if (f1.lastModified() < f2.lastModified()) {
                    return -1;
                } else {
                    return f1.getAbsolutePath().compareToIgnoreCase(f2.getAbsolutePath()) * -1;
                }
            }
        });

        return fileArray;
    }

}
