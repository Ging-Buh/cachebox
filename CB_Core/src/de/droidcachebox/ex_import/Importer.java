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
package de.droidcachebox.ex_import;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.ZipException;

import de.droidcachebox.core.GCVote;
import de.droidcachebox.core.GCVoteCacheInfo;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.core.RatingData;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.GCVoteDAO;
import de.droidcachebox.settings.AllSettings;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.ProgresssChangedEventList;
import de.droidcachebox.utils.log.Log;

public class Importer {
    private static final String sKlasse = "Importer";

    /**
     * Importiert die GPX files, die sich in diesem Verzeichniss befinden. Auch wenn sie sich in einem Zip-File befinden. Oder das GPX-File
     * falls eine einzelne Datei übergeben wird.
     *
     * @param directoryPath ?
     * @param ip            ?
     *                      Cache_Log_Return mit dem Inhalt aller Importierten GPX Files
     * @throws Exception ?
     */
    public void importGpx(String directoryPath, ImporterProgress ip) throws Exception {
        // resest import Counter

        GPXFileImporter.CacheCount = 0;
        GPXFileImporter.LogCount = 0;

        // Extract all Zip Files!

        AbstractFile abstractFile = FileFactory.createFile(directoryPath);

        if (abstractFile.isDirectory()) {
            ArrayList<AbstractFile> ordnerInhalt_Zip = FileIO.recursiveDirectoryReader(abstractFile, new ArrayList<>(), "zip", false);

            ip.setJobMax("ExtractZip", ordnerInhalt_Zip.size());

            for (AbstractFile tmpZip : ordnerInhalt_Zip) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e2) {
                    return; // Thread Canceld
                }

                ip.ProgressInkrement("ExtractZip", "", false);
                // Extract ZIP
                try {
                    UnZip.extractHere(tmpZip.getAbsolutePath());
                } catch (ZipException e) {
                    Log.err(sKlasse, "ZipException", e);
                } catch (IOException e) {
                    Log.err(sKlasse, "IOException", e);
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
        AbstractFile[] abstractFileList = GetFilesToLoad(directoryPath);

        ip.setJobMax("AnalyseGPX", abstractFileList.length);

        ImportHandler importHandler = new ImportHandler();

        Integer countwpt = 0;
        HashMap<String, Integer> wptCount = new HashMap<>();

        for (AbstractFile fAbstractFile : abstractFileList) {

            try {
                Thread.sleep(10);
            } catch (InterruptedException e2) {
                return; // Thread Canceld
            }

            ip.ProgressInkrement("AnalyseGPX", fAbstractFile.getName(), false);

            BufferedReader br;
            String strLine;
            try {
                br = new BufferedReader(new InputStreamReader(fAbstractFile.getFileInputStream()));
                while ((strLine = br.readLine()) != null) {
                    if (strLine.contains("<wpt"))
                        countwpt++;
                }
            } catch (IOException e1) {
                Log.err(sKlasse, e1.getLocalizedMessage(), e1);
            }

            wptCount.put(fAbstractFile.getAbsolutePath(), countwpt);
            countwpt = 0;
        }

        if (abstractFileList.length == 0) {
            ip.ProgressInkrement("AnalyseGPX", "", true);
        }

        for (Integer count : wptCount.values()) {
            countwpt += count;
        }

        // Indiziere DB
        CacheInfoList.IndexDB();

        ip.setJobMax("ImportGPX", abstractFileList.length + countwpt);
        for (AbstractFile AbstractFile : abstractFileList) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e2) {
                return; // Thread Canceled
            }

            ip.ProgressInkrement("ImportGPX", "Import: " + AbstractFile.getName(), false);
            GPXFileImporter importer = new GPXFileImporter(AbstractFile, ip);

            try {
                importer.doImport(importHandler, wptCount.get(AbstractFile.getAbsolutePath()));
            } catch (Exception e) {
                Log.err(sKlasse, "importer.doImport => " + AbstractFile.getAbsolutePath(), e);
                throw e;
            }

        }

        if (abstractFileList.length == 0) {
            ip.ProgressInkrement("ImportGPX", "", true);
        }

        importHandler.updateCacheCountForGPXFilenames();

        // Indexierte CacheInfos zurück schreiben
        CacheInfoList.writeListToDB();
        CacheInfoList.dispose();

    }

    /**
     * @param whereClause ?
     * @param ip          ?
     */
    public void importGcVote(String whereClause, ImporterProgress ip) {

        GCVoteDAO gcVoteDAO = new GCVoteDAO();
        int i;

        if (AllSettings.GcVotePassword.getValue().length() > 0) {
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

                ip.ProgressInkrement("sendGcVote", "Sending Votes (" + i + " / " + pendingVotes.size() + ")", false);

                if (GCVote.sendVote(AllSettings.GcLogin.getValue(), AllSettings.GcVotePassword.getValue(), info.getVote(), info.getUrl(), info.getGcCode())) {
                    gcVoteDAO.updatePendingVote(info.getId());
                }
            }

            if (pendingVotes.size() == 0) {
                ip.ProgressInkrement("sendGcVote", "No Votes to send.", true);
            }
        }

        int count = gcVoteDAO.getCacheCountToGetVotesFor(whereClause);

        ip.setJobMax("importGcVote", count);

        int packageSize = 100;
        int offset = 0;
        int failCount = 0;
        i = 0;

        while (offset < count) {
            ArrayList<GCVoteCacheInfo> gcVotePackage = gcVoteDAO.getGCVotePackage(whereClause, packageSize, i);
            ArrayList<String> requests = new ArrayList<>();
            HashMap<String, Boolean> resetVote = new HashMap<>();
            HashMap<String, Long> idLookup = new HashMap<>();

            for (GCVoteCacheInfo gcVoteCacheInfo : gcVotePackage) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e2) {
                    return; // Thread Canceld
                }

                if (!gcVoteCacheInfo.getGcCode().toLowerCase(Locale.getDefault()).startsWith("gc")) {
                    ip.ProgressInkrement("importGcVote", "Not a GC.com Cache", false);
                    continue;
                }

                requests.add(gcVoteCacheInfo.getGcCode());
                resetVote.put(gcVoteCacheInfo.getGcCode(), !gcVoteCacheInfo.isVotePending());
                idLookup.put(gcVoteCacheInfo.getGcCode(), gcVoteCacheInfo.getId());
            }

            ArrayList<RatingData> ratingData = GCVote.getRating(AllSettings.GcLogin.getValue(), AllSettings.GcVotePassword.getValue(), requests);

            if (ratingData == null || ratingData.isEmpty()) {
                failCount += packageSize;
                ip.ProgressInkrement("importGcVote", "Query failed...", false);
            } else {
                for (RatingData data : ratingData) {
                    if (idLookup.containsKey(data.wayPoint)) {
                        if (resetVote.containsKey(data.wayPoint)) {
                            gcVoteDAO.updateRatingAndVote(idLookup.get(data.wayPoint), data.rating, data.vote);
                        } else {
                            gcVoteDAO.updateRating(idLookup.get(data.wayPoint), data.rating);
                        }
                    }

                    i++;

                    ip.ProgressInkrement("importGcVote", "Writing Ratings (" + (i + failCount) + " / " + count + ")", false);
                }

            }

            offset += packageSize;

        }

        if (count == 0) {
            ip.ProgressInkrement("importGcVote", "", true);
        }

    }

    /**
     * @param ip            ?
     * @param importImages  ?
     * @param importSpoiler ?
     * @param where         [Last Filter]FilterInstances.LastFilter.getSqlWhere();
     * @return ErrorCode Use with<br>
     */
    public int importImages(ImporterProgress ip, boolean importImages, boolean importSpoiler, String where) {

        int ret = 0;

        String sql = "select Id, Description, Name, GcCode, Url, ImagesUpdated, DescriptionImagesUpdated from Caches";
        if (where.length() > 0)
            sql += " where " + where;
        CoreCursor reader = CBDB.getInstance().getSql().rawQuery(sql, null);

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
                        ip.ProgressInkrement("importImages", "Importing Images for " + gcCode + " (" + cnt + " / " + numCaches + ")", false);

                        String description = reader.getString(1);
                        String uri = reader.getString(4);

                        boolean descriptionImagesUpdated;
                        if (!importImages) {
                            // do not import Description Images
                            descriptionImagesUpdated = true;
                        } else {
                            if (!reader.isNull(6)) {
                                descriptionImagesUpdated = reader.getInt(6) != 0;
                            } else descriptionImagesUpdated = false;
                        }
                        boolean additionalImagesUpdated;
                        if (!importSpoiler) {
                            // do not import Spoiler Images
                            additionalImagesUpdated = true;
                        } else {
                            if (!reader.isNull(5)) {
                                additionalImagesUpdated = reader.getInt(5) != 0;
                            } else additionalImagesUpdated = false;
                        }
                        ret = DescriptionImageGrabber.GrabImagesSelectedByCache(ip, descriptionImagesUpdated, additionalImagesUpdated, id, gcCode, description, uri, false);
                        if (ret < 0) break;
                    }
                } catch (Exception e) {
                    Log.err(sKlasse, "importImages", e);
                }
                reader.moveToNext();
            }
        }

        reader.close();
        return ret;
    }

    public void importMaps() {
        ProgresssChangedEventList.progressChanged("import Map", "", 0);

    }

    private AbstractFile[] GetFilesToLoad(String directoryPath) {
        ArrayList<AbstractFile> abstractFiles = new ArrayList<>();

        AbstractFile abstractFile = FileFactory.createFile(directoryPath);
        if (abstractFile.isFile()) {
            abstractFiles.add(abstractFile);
        } else {
            if (FileIO.directoryExists(directoryPath)) {
                abstractFiles = FileIO.recursiveDirectoryReader(FileFactory.createFile(directoryPath), abstractFiles);
            }
        }

        AbstractFile[] abstractFileArray = abstractFiles.toArray(new AbstractFile[0]);

        Arrays.sort(abstractFileArray, (f1, f2) -> {

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
        });

        return abstractFileArray;
    }

}
