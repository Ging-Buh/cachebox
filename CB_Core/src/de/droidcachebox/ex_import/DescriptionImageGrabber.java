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

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.ERROR;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.downloadImageListForGeocache;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;

import de.droidcachebox.Platform;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.ImageEntry;
import de.droidcachebox.settings.AllSettings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.FilenameFilter;
import de.droidcachebox.utils.SDBM_Hash;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.log.Log;

public class DescriptionImageGrabber {
    private static final String sClass = "DescriptionImageGrabber";

    public static CB_List<Segment> Segmentize(String text, String leftSeperator, String rightSeperator) {
        CB_List<Segment> result = new CB_List<Segment>();

        if (text == null) {
            return result;
        }

        int idx = 0;

        while (true) {
            int leftIndex = text.toLowerCase().indexOf(leftSeperator, idx);

            if (leftIndex == -1)
                break;

            leftIndex += leftSeperator.length();

            int rightIndex = text.toLowerCase().indexOf(rightSeperator, leftIndex);

            if (rightIndex == -1)
                break;

            // ignoriere URLs, die zu lang sind
            // if (text.length() > 1024)
            if ((rightIndex - leftIndex) > 1024) {
                idx = rightIndex;
                continue;
            }

            int forward = leftIndex + 50;

            if (forward > text.length()) {
                forward = text.length();
            }

            // Test, ob es sich um ein eingebettetes Bild handelt
            if (text.substring(leftIndex, forward).toLowerCase().contains("data:image/")) {
                idx = rightIndex;
                continue;
            }

            // Abschnitt gefunden
            Segment curSegment = new Segment();
            curSegment.start = leftIndex;
            curSegment.ende = rightIndex;
            curSegment.text = text.substring(leftIndex, rightIndex/* - leftIndex */);
            result.add(curSegment);

            idx = rightIndex;
        }

        return result;
    }

    public static String RemoveSpaces(String line) {
        String dummy = line.replace("\n", "");
        dummy = dummy.replace("\r", "");
        dummy = dummy.replace(" ", "");
        return dummy;
    }

    /**
     * @param GcCode ?
     * @param _uri ?
     * @return ?
     */
    public static String buildDescriptionImageFilename(String GcCode, URI _uri) {
        // in der DB stehts ohne large. der Dateiname wurde aber mit large gebildet. Ev auch nur ein Handy / PC Problem.
        String path = _uri.getPath();
        String authority = _uri.getAuthority();
        if (authority != null) {
            if (authority.equals("img.geocaching.com")) {
                path = path.replace("/large/", "/");
            }
        }
        String imagePath = AllSettings.DescriptionImageFolder.getValue() + "/" + GcCode.substring(0, 4);
        if (AllSettings.DescriptionImageFolderLocal.getValue().length() > 0)
            imagePath = AllSettings.DescriptionImageFolderLocal.getValue() + "/" + GcCode.substring(0, 4);

        // String uriName = url.Substring(url.LastIndexOf('/') + 1);
        // int idx = uri.AbsolutePath.LastIndexOf('.');
        // //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        int idx = path.lastIndexOf('.');
        // String extension = (idx >= 0) ? uri.AbsolutePath.Substring(idx) :
        // ".";!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String extension = (idx >= 0) ? path.substring(idx) : ".";

        // return imagePath + "\\" + GcCode +
        // Global.sdbm(uri.AbsolutePath).ToString() + extension;!!!!!!!!!!!!!
        return imagePath + "/" + GcCode + SDBM_Hash.sdbm(path) + extension;
    }

    /**
     * @param Cache                 ?
     * @param html                  ?
     * @param suppressNonLocalMedia ?
     * @param NonLocalImages        ?
     * @param NonLocalImagesUrl     ?
     * @return ?
     */
    public static String resolveImages(Cache Cache, String html, boolean suppressNonLocalMedia, LinkedList<String> NonLocalImages, LinkedList<String> NonLocalImagesUrl) {
        /*
         * NonLocalImages = new List<string>(); NonLocalImagesUrl = new List<string>();
         */

        URI baseUri;
        try {
            baseUri = URI.create(Cache.getUrl());
        } catch (Exception exc) {
            /*
             * #if DEBUG Global.AddLog( "DescriptionImageGrabber.resolveImages: failed to resolve '" + Cache.Url + "': " + exc.ToString());
             * #endif
             */
            baseUri = null;
        }

        if (baseUri == null) {
            Cache.setUrl("https://geocaching.com/seek/cache_details.aspx?wp=" + Cache.getGeoCacheCode());
            try {
                baseUri = URI.create(Cache.getUrl());
            } catch (Exception exc) {
                /*
                 * #if DEBUG Global.AddLog( "DescriptionImageGrabber.resolveImages: failed to resolve '" + Cache.Url + "': " +
                 * exc.ToString()); #endif
                 */
                return html;
            }
        }

        // String htmlNoSpaces = RemoveSpaces(html);

        CB_List<Segment> imgTags = Segmentize(html, "<img", ">");

        int delta = 0;

        for (int i = 0, n = imgTags.size(); i < n; i++) {
            Segment img = imgTags.get(i);
            int srcIdx = img.text.toLowerCase().indexOf("src=");
            int srcStart = img.text.indexOf('"', srcIdx + 4);
            int srcEnd = img.text.indexOf('"', srcStart + 1);

            if (srcIdx != -1 && srcStart != -1 && srcEnd != -1) {
                String src = img.text.substring(srcStart + 1, srcEnd);
                try {
                    URI imgUri = URI.create(/* baseUri, */src.replace("\n",""));
                    String localFile;
                    if (imgUri.getScheme().equalsIgnoreCase("file")) {
                        localFile = imgUri.getPath();
                    }
                    else {
                        localFile = buildDescriptionImageFilename(Cache.getGeoCacheCode(), imgUri);
                    }

                    if (FileIO.fileExistsNotEmpty(localFile)) {
                        int idx = 0;

                        while ((idx = html.indexOf(src, idx)) >= 0) {
                            if (idx >= (img.start + delta) && (idx <= img.ende + delta)) {
                                String head = html.substring(0, img.start + delta);
                                String tail = html.substring(img.ende + delta);
                                // String uri = "file://" + localFile;
                                String uri = Platform.getFileProviderContentUrl(localFile);
                                String body = img.text.replace(src, uri);

                                delta += (uri.length() - src.length());
                                html = head + body + tail;
                            }
                            idx++;
                        }
                    } else {
                        NonLocalImages.add(localFile);
                        NonLocalImagesUrl.add(imgUri.toString());

                        if (suppressNonLocalMedia) {
                            // Wenn nicht-lokale Inhalte unterdrückt werden sollen, wird das <img>-Tag vollständig entfernt
                            html = html.substring(0, img.start - 4 + delta) + html.substring(img.ende + 1 + delta);
                            delta -= 5 + img.ende - img.start;
                        }

                    }
                } catch (Exception exc) {
                    Log.err(sClass, "for " + src, exc);
                    /*
                     * #if DEBUG Global.AddLog( "DescriptionImageGrabber.resolveImages: failed to resolve relative uri. Base '" + baseUri +
                     * "', relative '" + src + "': " + exc.ToString()); #endif
                     */
                }
            }
        }

        return html;
    }

    public static LinkedList<URI> GetImageUris(String html, String baseUrl) {

        LinkedList<URI> images = new LinkedList<>();

        // chk baseUrl
        try {
            URI.create(baseUrl);
        } catch (Exception exc) {
            return images;
        }

        CB_List<Segment> imgTags = Segmentize(html, "<img", ">");

        for (int i = 0, n = imgTags.size(); i < n; i++) {
            Segment img = imgTags.get(i);
            int srcStart = -1;
            int srcEnd = -1;
            int srcIdx = img.text.toLowerCase().indexOf("src=");
            if (srcIdx != -1)
                srcStart = img.text.indexOf('"', srcIdx + 4);
            if (srcStart != -1)
                srcEnd = img.text.indexOf('"', srcStart + 1);

            if (srcIdx != -1 && srcStart != -1 && srcEnd != -1) {
                String src = img.text.substring(srcStart + 1, srcEnd);
                try {
                    URI imgUri = URI.create(src);

                    images.add(imgUri);

                } catch (Exception ignored) {
                }
            }
        }

        return images;
    }

    public static int grabImagesSelectedByCache(ImportProgress importProgress, boolean descriptionImagesUpdated, boolean additionalImagesUpdated, long id, String gcCode, String description, String url, boolean withLogImages) {
        boolean imageLoadError = false;
        if (!descriptionImagesUpdated) {
            Log.debug(sClass, "GrabImagesSelectedByCache -> grab description images");
            importProgress.changeMsg("importImages", Translation.get("DescriptionImageImportForGC") + gcCode);

            LinkedList<URI> imgUris = GetImageUris(description, url);

            for (URI uri : imgUris) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return 0;
                }

                String local = buildDescriptionImageFilename(gcCode, uri);

                importProgress.changeMsg("importImages", Translation.get("DescriptionImageImportForGC") + gcCode + Translation.get("ImageDownloadFrom") + uri);

                // direkt download
                Download download = new Download(null, null);
                if (download.download(uri.toString(), local)) {
                    // there could be an pseudo image indicating a previous error
                    // this file must be deleted
                    DeleteMissingImageInformation(local);
                    break;
                } else {
                    imageLoadError = HandleMissingImages(imageLoadError, uri.toString(), local);
                }
            }

            descriptionImagesUpdated = true;

            if (!imageLoadError) {
                Parameters args = new Parameters();
                args.put("DescriptionImagesUpdated", descriptionImagesUpdated);
                CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
            }
            Log.debug(sClass, "GrabImagesSelectedByCache done");
        }
        if (!additionalImagesUpdated) {
            Log.debug(sClass, "GrabImagesSelectedByCache -> grab spoiler images");
            // Get additional images (Spoiler)

            String[] files = getFilesInDirectory(AllSettings.SpoilerFolder.getValue(), gcCode);
            ArrayList<String> allSpoilers = new ArrayList<>();
            for (String file : files)
                allSpoilers.add(file);
            String[] filesLocal = getFilesInDirectory(AllSettings.SpoilerFolderLocal.getValue(), gcCode);
            for (String file : filesLocal)
                allSpoilers.add(file);

            {
                importProgress.changeMsg("importImages", Translation.get("SpoilerImageImportForGC") + gcCode);

                // todo always take from database. They are not downloaded yet
                // todo else don't write them to database on fetch/update cache
                ArrayList<ImageEntry> imageEntries = downloadImageListForGeocache(gcCode, withLogImages);
                if (APIError != OK) {
                    return ERROR;
                }

                for (ImageEntry imageEntry : imageEntries) {

                    try {// for cancel/interupt Thread
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        return 0;
                    }

                    String uri = imageEntry.getImageUrl();

                    importProgress.changeMsg("importImages", Translation.get("SpoilerImageImportForGC") + gcCode + Translation.get("ImageDownloadFrom") + uri);

                    imageEntry = BuildAdditionalImageFilenameHashNew(imageEntry);
                    if (imageEntry != null) {
                        // todo ? should write or update database
                        String filename = imageEntry.getLocalPath().substring(imageEntry.getLocalPath().lastIndexOf('/') + 1);

                        if (allSpoilers.contains(filename)) {
                            // wenn ja, dann aus der Liste der aktuell vorhandenen Spoiler entfernen und mit dem nächsten Spoiler weitermachen
                            allSpoilers.remove(filename);
                            continue; // dieser Spoiler muss jetzt nicht mehr geladen werden da er schon vorhanden ist.
                        }
                        // todo first look for an image from gsak
                        Download download = new Download(null, null);
                        if (download.download(imageEntry.getImageUrl(), imageEntry.getLocalPath())) {
                            // there could be an pseudo image indicating a pprevious error
                            // this file must be deleted
                            DeleteMissingImageInformation(imageEntry.getLocalPath());
                        } else {
                            imageLoadError = HandleMissingImages(imageLoadError, uri, imageEntry.getLocalPath());
                        }
                    }
                }
                Log.debug(sClass, "images download done");

                additionalImagesUpdated = true;

                if (!imageLoadError) {
                    Parameters args = new Parameters();
                    args.put("ImagesUpdated", additionalImagesUpdated);
                    CBDB.getInstance().update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
                    // jetzt können noch alle "alten" Spoiler gelöscht werden.
                    // "alte" Spoiler sind die, die auf der SD vorhanden sind, aber nicht als Link über die API gemeldet wurden.
                    // Alle Spoiler in der Liste allSpoilers sind "alte"
                    Log.debug(sClass, "Delete old spoilers.");
                    for (String file : allSpoilers) {
                        String fileNameWithOutExt = file.replaceFirst("[.][^.]+$", "");
                        // Testen, ob dieser Dateiname einen gültigen ACB Hash hat (eingeschlossen zwischen @....@>
                        if (fileNameWithOutExt.endsWith("@") && fileNameWithOutExt.contains("@")) {
                            // file enthält nur den Dateinamen, nicht den Pfad. Diesen Dateinamen um den Pfad erweitern, in dem hier die
                            // Spoiler gespeichert wurden
                            String path = getSpoilerPath(gcCode);
                            AbstractFile f = FileFactory.createFile(path + '/' + file);
                            try {
                                f.delete();
                            } catch (Exception ex) {
                                Log.err(sClass, "DescriptionImageGrabber - GrabImagesSelectedByCache - DeleteSpoiler", ex);
                            }
                        }
                    }
                }

            }
            Log.debug(sClass, "GrabImagesSelectedByCache done");
        }
        return 0;
    }

    private static String[] getFilesInDirectory(String path, final String GcCode) {
        String imagePath = path + "/" + GcCode.substring(0, 4);
        boolean imagePathDirExists = FileIO.directoryExists(imagePath);

        if (imagePathDirExists) {
            AbstractFile dir = FileFactory.createFile(imagePath);
            FilenameFilter filter = (dir1, filename) -> {

                filename = filename.toLowerCase();
                return filename.indexOf(GcCode.toLowerCase()) == 0;
            };
            String[] files = dir.list(filter);
            return files;
        }
        return new String[0];
    }

    public static String getSpoilerPath(String GcCode) {
        String imagePath = AllSettings.SpoilerFolder.getValue() + "/" + GcCode.substring(0, 4);

        if (AllSettings.SpoilerFolderLocal.getValue().length() > 0)
            imagePath = AllSettings.SpoilerFolderLocal.getValue() + "/" + GcCode.substring(0, 4);

        return imagePath;
    }

    /**
     * Neue Version, mit @ als Eingrenzung des Hashs, da die Klammern nicht als URL's verwendet werden dürfen
     */
    public static ImageEntry BuildAdditionalImageFilenameHashNew(ImageEntry imageEntry) {
        try {
            String uriPath = new URI(imageEntry.getImageUrl()).getPath();
            String imagePath = AllSettings.SpoilerFolder.getValue() + "/" + imageEntry.getGcCode().substring(0, 4);

            if (AllSettings.SpoilerFolderLocal.getValue().length() > 0)
                imagePath = AllSettings.SpoilerFolderLocal.getValue() + "/" + imageEntry.getGcCode().substring(0, 4);
            imageEntry.setName(imageEntry.getDescription().trim());
            imageEntry.setName(imageEntry.getName().replaceAll("[^a-zA-Z0-9_\\.\\-]", "_"));

            int idx = imageEntry.getImageUrl().lastIndexOf('.');
            String extension = (idx >= 0) ? imageEntry.getImageUrl().substring(idx) : ".";

            // Create sdbm Hash from Path of URI, not from complete URI
            imageEntry.setLocalPath(imagePath + "/" + imageEntry.getGcCode() + " - " + imageEntry.getName() + " @" + SDBM_Hash.sdbm(uriPath) + "@" + extension);
            return imageEntry;
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean HandleMissingImages(boolean imageLoadError, String uri, String local) {
        try {
            AbstractFile abstractFile = FileFactory.createFile(local + "_broken_link.txt");
            if (!abstractFile.exists()) {
                AbstractFile abstractFile2 = FileFactory.createFile(local + ".1st");
                if (abstractFile2.exists()) {
                    // After first try, we can be sure that the image cannot be loaded.
                    // At this point mark the image as loaded and go ahead.
                    abstractFile2.renameTo(abstractFile);
                } else {
                    // Crate a local file for marking it that it could not loaded one time.
                    // Maybe the link is broken temporarily. So try it next time once again.
                    try {
                        String text = "Could not load image from:" + uri;
                        BufferedWriter out = new BufferedWriter(new FileWriter(local + ".1st"));
                        out.write(text);
                        out.close();
                        imageLoadError = true;
                    } catch (IOException e) {
                        System.out.println("Exception ");
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return imageLoadError;
    }

    private static void DeleteMissingImageInformation(String local) {
        AbstractFile abstractFile = FileFactory.createFile(local + "_broken_link.txt");
        if (abstractFile.exists()) {
            try {
                abstractFile.delete();
            } catch (IOException ignored) {
            }
        }

        abstractFile = FileFactory.createFile(local + ".1st");
        if (abstractFile.exists()) {
            try {
                abstractFile.delete();
            } catch (IOException ignored) {
            }
        }
    }

    public static class Segment {
        public int start;
        public int ende;
        public String text;
    }

}
