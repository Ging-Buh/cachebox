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
import de.droidcachebox.core.RatingData;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.database.CategoryDAO;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.GCVoteDAO;
import de.droidcachebox.settings.AllSettings;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.TestCancel;
import de.droidcachebox.utils.log.Log;

public class Importer {
    private static final String sClass = "Importer";

    /**
     * imports all gpx and zipped gpx in this path.
     */
    public void importGpx(String fileOrFolderName, ImportProgress importProgress, TestCancel testCancel) throws Exception {
        String stepID = "ExtractZip";
        GPXFileImporter.CacheCount = 0;
        GPXFileImporter.LogCount = 0;
        AbstractFile fileOrFolder = FileFactory.createFile(fileOrFolderName);
        if (fileOrFolder.isDirectory()) {
            ArrayList<AbstractFile> zipFiles = FileIO.recursiveDirectoryReader(fileOrFolder, new ArrayList<>(), "zip", false);
            importProgress.setStepFinalValue(stepID, zipFiles.size());
            ArrayList<AbstractFile> files = new ArrayList<>();
            for (AbstractFile tmpZip : zipFiles) {
                importProgress.incrementStep(stepID, "");
                if (testCancel == null || !testCancel.checkCanceled()) {
                    try {
                        files.add(tmpZip);
                        UnZip.extract(tmpZip, true);
                    } catch (ZipException e) {
                        Log.err(sClass, "ZipException", e);
                    } catch (IOException e) {
                        Log.err(sClass, "IOException", e);
                    }
                }
            }
            if (testCancel != null && testCancel.checkCanceled()) {
                for (AbstractFile tmp : files) {
                    tmp.delete();
                }
                return;
            }
            importProgress.finishStep(stepID, "");
        }

        stepID = "AnalyseGPX";
        AbstractFile[] filesToLoad = getFilesToLoad(fileOrFolderName);
        importProgress.setStepFinalValue(stepID, filesToLoad.length);
        HashMap<String, Integer> numberOfWaypoints = new HashMap<>();
        for (AbstractFile fileToLoad : filesToLoad) {
            int numberOfWaypointsInFile = 0;
            importProgress.incrementStep(stepID, fileToLoad.getName());
            BufferedReader br;
            String strLine;
            try {
                br = new BufferedReader(new InputStreamReader(fileToLoad.getFileInputStream()));
                while ((strLine = br.readLine()) != null) {
                    if (strLine.contains("<wpt"))
                        numberOfWaypointsInFile = numberOfWaypointsInFile + 1;
                }
            } catch (IOException e1) {
                Log.err(sClass, e1.getLocalizedMessage(), e1);
            }
            numberOfWaypoints.put(fileToLoad.getAbsolutePath(), numberOfWaypointsInFile);
        }
        importProgress.finishStep(stepID, "");

        stepID = "ImportGPX";
        int totalNumberOfWaypoints = 0;
        for (Integer count : numberOfWaypoints.values()) {
            totalNumberOfWaypoints = totalNumberOfWaypoints + count;
        }
        importProgress.setStepFinalValue(stepID, filesToLoad.length + totalNumberOfWaypoints);
        CacheInfoList.indexDB();
        for (AbstractFile fileToLoad : filesToLoad) {
            importProgress.incrementStep(stepID, "Import: " + fileToLoad.getName());
            GPXFileImporter importer = new GPXFileImporter(fileToLoad, importProgress);
            try {
                importer.doImport(numberOfWaypoints.get(fileToLoad.getAbsolutePath()), testCancel);
            } catch (Exception sex) {
                if (testCancel != null && testCancel.checkCanceled()) {
                    throw new Exception(TestCancel.canceled);
                } else {
                    Log.err(sClass, "importer.doImport => " + fileToLoad.getAbsolutePath(), sex);
                    throw sex;
                }
            }
        }
        importProgress.finishStep(stepID, "");

        new CachesDAO().updateCacheCountForGPXFilenames();
        CategoryDAO.getInstance().deleteEmptyCategories();
        CacheInfoList.writeListToDB();
        CacheInfoList.dispose();

    }

    public void importGcVote(String whereClause, ImportProgress importProgress, TestCancel testCancel) throws Exception {
        String stepID = "sendGcVote";
        GCVoteDAO gcVoteDAO = new GCVoteDAO();
        int i;
        if (AllSettings.GcVotePassword.getValue().length() > 0) {
            ArrayList<GCVoteCacheInfo> pendingVotes = gcVoteDAO.getPendingGCVotes();
            importProgress.setStepFinalValue(stepID, pendingVotes.size());
            i = 0;
            for (GCVoteCacheInfo info : pendingVotes) {
                i++;
                importProgress.incrementStep(stepID, "Sending Votes (" + i + " / " + pendingVotes.size() + ")");
                if (testCancel != null && testCancel.checkCanceled())
                    throw new Exception(TestCancel.canceled);
                if (GCVote.sendVote(AllSettings.GcLogin.getValue(), AllSettings.GcVotePassword.getValue(), info.getVote(), info.getUrl(), info.getGcCode())) {
                    gcVoteDAO.updatePendingVote(info.getId());
                }
            }
            importProgress.finishStep(stepID, "No Votes to send.");
        }

        stepID = "importGcVote";
        int count = gcVoteDAO.getCacheCountToGetVotesFor(whereClause);
        importProgress.setStepFinalValue(stepID, count);
        int packageSize = 100;
        int offset = 0;
        int failCount = 0;
        i = 0;
        while (offset < count) {
            if (testCancel != null && testCancel.checkCanceled())
                throw new Exception(TestCancel.canceled);
            ArrayList<GCVoteCacheInfo> gcVotePackage = gcVoteDAO.getGCVotePackage(whereClause, packageSize, i);
            ArrayList<String> requests = new ArrayList<>();
            HashMap<String, Boolean> resetVote = new HashMap<>();
            HashMap<String, Long> idLookup = new HashMap<>();
            for (GCVoteCacheInfo gcVoteCacheInfo : gcVotePackage) {
                if (!gcVoteCacheInfo.getGcCode().toLowerCase(Locale.getDefault()).startsWith("gc")) {
                    importProgress.incrementStep(stepID, "Not a GC.com Cache");
                    continue;
                }
                requests.add(gcVoteCacheInfo.getGcCode());
                resetVote.put(gcVoteCacheInfo.getGcCode(), !gcVoteCacheInfo.isVotePending());
                idLookup.put(gcVoteCacheInfo.getGcCode(), gcVoteCacheInfo.getId());
            }
            ArrayList<RatingData> ratingData = GCVote.getRating(AllSettings.GcLogin.getValue(), AllSettings.GcVotePassword.getValue(), requests);
            if (ratingData == null || ratingData.isEmpty()) {
                failCount += packageSize;
                importProgress.incrementStep(stepID, "Query failed...");
            } else {
                for (RatingData data : ratingData) {
                    if (testCancel != null && testCancel.checkCanceled())
                        throw new Exception(TestCancel.canceled);
                    if (idLookup.containsKey(data.wayPoint)) {
                        if (resetVote.containsKey(data.wayPoint)) {
                            gcVoteDAO.updateRatingAndVote(idLookup.get(data.wayPoint), data.rating, data.vote);
                        } else {
                            gcVoteDAO.updateRating(idLookup.get(data.wayPoint), data.rating);
                        }
                    }
                    i++;
                    importProgress.incrementStep(stepID, "Writing Ratings (" + (i + failCount) + " / " + count + ")");
                }
            }
            offset = offset + packageSize;
        }
        importProgress.finishStep(stepID, "");
    }

    public void importImages(ImportProgress importProgress, TestCancel testCancel, boolean importImages, boolean importSpoiler, String where) {
        String sql = "select Id, Description, Name, GcCode, Url, ImagesUpdated, DescriptionImagesUpdated from Caches";
        if (where.length() > 0)
            sql = sql + (" where " + where);
        CoreCursor reader = CBDB.getInstance().rawQuery(sql, null);
        int importCount = 0;
        int numberOfGeoCaches = reader.getCount();
        importProgress.setStepFinalValue("importImages", numberOfGeoCaches);
        if (numberOfGeoCaches > 0) {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                if (testCancel != null && testCancel.checkCanceled()) break;
                importCount++;
                try {
                    long id = reader.getLong(0);
                    String gcCode = reader.getString(3);

                    if (gcCode.toLowerCase(Locale.getDefault()).startsWith("gc")) {
                        importProgress.incrementStep("importImages", "Importing Images for " + gcCode + " (" + importCount + " / " + numberOfGeoCaches + ")");

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

                        if (DescriptionImageGrabber.grabImagesSelectedByCache(importProgress, descriptionImagesUpdated, additionalImagesUpdated, id, gcCode, description, uri, false) < 0) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.err(sClass, "importImages", e);
                }
                reader.moveToNext();
            }
        }

        reader.close();
    }

    private AbstractFile[] getFilesToLoad(String directoryPath) {
        ArrayList<AbstractFile> files = new ArrayList<>();
        AbstractFile abstractFile = FileFactory.createFile(directoryPath);
        if (abstractFile.isFile()) {
            files.add(abstractFile);
        } else {
            if (FileIO.directoryExists(directoryPath)) {
                files = FileIO.recursiveDirectoryReader(FileFactory.createFile(directoryPath), files);
            }
        }
        AbstractFile[] result = files.toArray(new AbstractFile[0]);
        Arrays.sort(result, (f1, f2) -> {
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
        return result;
    }

}
