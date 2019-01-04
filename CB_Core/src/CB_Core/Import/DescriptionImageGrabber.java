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

import CB_Core.CB_Core_Settings;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.SDBM_Hash;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.fileProvider.FilenameFilter;
import de.cb.sqlite.Database_Core.Parameters;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;

import static CB_Core.Api.GroundspeakAPI.*;
import static CB_Utils.http.Download.Download;

public class DescriptionImageGrabber {
    private static final String log = "DescriptionImageGrabber";

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
     * @param GcCode
     * @param _uri
     * @return
     */
    public static String BuildDescriptionImageFilename(String GcCode, URI _uri) {
        // in der DB stehts ohne large. der Dateiname wurde aber mit large gebildet. Ev auch nur ein Handy / PC Problem.
        String path = _uri.getPath();
        String authority = _uri.getAuthority();
        if (authority != null) {
            if (authority.equals("img.geocaching.com")) {
                path = path.replace("/large/", "/");
            }
        }
        String imagePath = CB_Core_Settings.DescriptionImageFolder.getValue() + "/" + GcCode.substring(0, 4);
        if (CB_Core_Settings.DescriptionImageFolderLocal.getValue().length() > 0)
            imagePath = CB_Core_Settings.DescriptionImageFolderLocal.getValue() + "/" + GcCode.substring(0, 4);

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
     * @param Cache
     * @param html
     * @param suppressNonLocalMedia
     * @param NonLocalImages
     * @param NonLocalImagesUrl
     * @return
     */
    public static String ResolveImages(Cache Cache, String html, boolean suppressNonLocalMedia, LinkedList<String> NonLocalImages, LinkedList<String> NonLocalImagesUrl) {
        /*
         * NonLocalImages = new List<string>(); NonLocalImagesUrl = new List<string>();
         */

        URI baseUri;
        try {
            baseUri = URI.create(Cache.getUrl());
        } catch (Exception exc) {
            /*
             * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve '" + Cache.Url + "': " + exc.ToString());
             * #endif
             */
            baseUri = null;
        }

        if (baseUri == null) {
            Cache.setUrl("http://www.geocaching.com/seek/cache_details.aspx?wp=" + Cache.getGcCode());
            try {
                baseUri = URI.create(Cache.getUrl());
            } catch (Exception exc) {
                /*
                 * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve '" + Cache.Url + "': " +
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
                    URI imgUri = URI.create(/* baseUri, */src);
                    String localFile = BuildDescriptionImageFilename(Cache.getGcCode(), imgUri);

                    if (FileIO.FileExistsNotEmpty(localFile)) {
                        int idx = 0;

                        while ((idx = html.indexOf(src, idx)) >= 0) {
                            if (idx >= (img.start + delta) && (idx <= img.ende + delta)) {
                                String head = html.substring(0, img.start + delta);
                                String tail = html.substring(img.ende + delta);
                                String uri = "file://" + localFile;
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
                    /*
                     * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve relative uri. Base '" + baseUri +
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

    public static int GrabImagesSelectedByCache(ImporterProgress ip, boolean descriptionImagesUpdated, boolean additionalImagesUpdated, long id, String gcCode, String description, String url) {
        boolean imageLoadError = false;

        if (!descriptionImagesUpdated) {
            Log.debug(log, "GrabImagesSelectedByCache -> grab description images");
            ip.ProgressChangeMsg("importImages", Translation.Get("DescriptionImageImportForGC") + gcCode);

            LinkedList<URI> imgUris = GetImageUris(description, url);

            for (URI uri : imgUris) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return 0;
                }

                if (BreakawayImportThread.isCanceled())
                    return 0;

                String local = BuildDescriptionImageFilename(gcCode, uri);

                ip.ProgressChangeMsg("importImages", Translation.Get("DescriptionImageImportForGC") + gcCode + Translation.Get("ImageDownloadFrom") + uri);

                // direkt download
                for (int j = 0; j < 1 /* && !parent.Cancel */; j++) {
                    if (Download(uri.toString(), local)) {
                        // Next image
                        DeleteMissingImageInformation(local);
                        break;
                    } else {
                        imageLoadError = HandleMissingImages(imageLoadError, uri.toString(), local);
                    }
                }
            }

            descriptionImagesUpdated = true;

            if (!imageLoadError) {
                Parameters args = new Parameters();
                args.put("DescriptionImagesUpdated", descriptionImagesUpdated);
                Database.Data.sql.update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
            }
            Log.debug(log, "GrabImagesSelectedByCache done");
        }

        if (!additionalImagesUpdated) {
            Log.debug(log, "GrabImagesSelectedByCache -> grab spoiler images");
            // Get additional images (Spoiler)

            String[] files = getFilesInDirectory(CB_Core_Settings.SpoilerFolder.getValue(), gcCode);
            ArrayList<String> allSpoilers = new ArrayList<>();
            for (String file : files)
                allSpoilers.add(file);
            String[] filesLocal = getFilesInDirectory(CB_Core_Settings.SpoilerFolderLocal.getValue(), gcCode);
            for (String file : filesLocal)
                allSpoilers.add(file);

            {
                ip.ProgressChangeMsg("importImages", Translation.Get("SpoilerImageImportForGC") + gcCode);

                // todo always take from database. They are not downloaded yet
                // todo else don't write them to database on fetch/update cache
                ArrayList<ImageEntry> imageEntries = downloadImageListForGeocache(gcCode);
                if (APIError != OK) {
                    return ERROR;
                }

                for (ImageEntry imageEntry : imageEntries) {

                    try {// for cancel/interupt Thread
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        return 0;
                    }

                    if (BreakawayImportThread.isCanceled())
                        return 0;

                    String uri = imageEntry.ImageUrl;

                    ip.ProgressChangeMsg("importImages", Translation.Get("SpoilerImageImportForGC") + gcCode + Translation.Get("ImageDownloadFrom") + uri);

                    imageEntry = BuildAdditionalImageFilenameHashNew(gcCode, imageEntry);
                    if (imageEntry != null) {
                        // todo ? should write or update database
                        String filename = imageEntry.LocalPath.substring(imageEntry.LocalPath.lastIndexOf('/') + 1);

                        if (allSpoilers.contains(filename)) {
                            // wenn ja, dann aus der Liste der aktuell vorhandenen Spoiler entfernen und mit dem nächsten Spoiler weitermachen
                            allSpoilers.remove(filename);
                            continue; // dieser Spoiler muss jetzt nicht mehr geladen werden da er schon vorhanden ist.
                        }

                        for (int j = 0; j < 1; j++) {
                            if (Download(imageEntry.ImageUrl, imageEntry.LocalPath)) {
                                // Next image
                                DeleteMissingImageInformation(imageEntry.LocalPath);
                                break;
                            } else {
                                imageLoadError = HandleMissingImages(imageLoadError, uri, imageEntry.LocalPath);
                            }

                        }
                    }
                }
                Log.debug(log, "images download done");

                additionalImagesUpdated = true;

                if (!imageLoadError) {
                    Parameters args = new Parameters();
                    args.put("ImagesUpdated", additionalImagesUpdated);
                    Database.Data.sql.update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
                    // jetzt können noch alle "alten" Spoiler gelöscht werden.
                    // "alte" Spoiler sind die, die auf der SD vorhanden sind, aber nicht als Link über die API gemeldet wurden.
                    // Alle Spoiler in der Liste allSpoilers sind "alte"
                    Log.debug(log, "Delete old spoilers.");
                    for (String file : allSpoilers) {
                        String fileNameWithOutExt = file.replaceFirst("[.][^.]+$", "");
                        // Testen, ob dieser Dateiname einen gültigen ACB Hash hat (eingeschlossen zwischen @....@>
                        if (fileNameWithOutExt.endsWith("@") && fileNameWithOutExt.contains("@")) {
                            // file enthält nur den Dateinamen, nicht den Pfad. Diesen Dateinamen um den Pfad erweitern, in dem hier die
                            // Spoiler gespeichert wurden
                            String path = getSpoilerPath(gcCode);
                            File f = FileFactory.createFile(path + '/' + file);
                            try {
                                f.delete();
                            } catch (Exception ex) {
                                Log.err(log, "DescriptionImageGrabber - GrabImagesSelectedByCache - DeleteSpoiler", ex);
                            }
                        }
                    }
                }

            }
            Log.debug(log, "GrabImagesSelectedByCache done");
        }
        return 0;
    }

    private static String[] getFilesInDirectory(String path, final String GcCode) {
        String imagePath = path + "/" + GcCode.substring(0, 4);
        boolean imagePathDirExists = FileIO.DirectoryExists(imagePath);

        if (imagePathDirExists) {
            File dir = FileFactory.createFile(imagePath);
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {

                    filename = filename.toLowerCase();
                    if (filename.indexOf(GcCode.toLowerCase()) == 0) {
                        return true;
                    }
                    return false;
                }
            };
            String[] files = dir.list(filter);
            return files;
        }
        return new String[0];
    }

    public static String getSpoilerPath(String GcCode) {
        String imagePath = CB_Core_Settings.SpoilerFolder.getValue() + "/" + GcCode.substring(0, 4);

        if (CB_Core_Settings.SpoilerFolderLocal.getValue().length() > 0)
            imagePath = CB_Core_Settings.SpoilerFolderLocal.getValue() + "/" + GcCode.substring(0, 4);

        return imagePath;
    }

    /**
     * Neue Version, mit @ als Eingrenzung des Hashs, da die Klammern nicht als URL's verwendet werden dürfen
     *
     */
    public static ImageEntry BuildAdditionalImageFilenameHashNew(String GcCode, ImageEntry imageEntry) {
        try {
            String uriPath = new URI(imageEntry.ImageUrl).getPath();
            String imagePath = CB_Core_Settings.SpoilerFolder.getValue() + "/" + GcCode.substring(0, 4);

            if (CB_Core_Settings.SpoilerFolderLocal.getValue().length() > 0)
                imagePath = CB_Core_Settings.SpoilerFolderLocal.getValue() + "/" + GcCode.substring(0, 4);
            imageEntry.Name = imageEntry.Description.trim();
            imageEntry.Name = imageEntry.Name.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");

            int idx = imageEntry.ImageUrl.lastIndexOf('.');
            String extension = (idx >= 0) ? imageEntry.ImageUrl.substring(idx) : ".";

            // Create sdbm Hash from Path of URI, not from complete URI
            imageEntry.LocalPath = imagePath + "/" + GcCode + " - " + imageEntry.Name + " @" + SDBM_Hash.sdbm(uriPath) + "@" + extension;
            return imageEntry;
        }
        catch (Exception ex) {
            return null;
        }
    }

    private static boolean HandleMissingImages(boolean imageLoadError, String uri, String local) {
        try {
            File file = FileFactory.createFile(local + "_broken_link.txt");
            if (!file.exists()) {
                File file2 = FileFactory.createFile(local + ".1st");
                if (file2.exists()) {
                    // After first try, we can be sure that the image cannot be loaded.
                    // At this point mark the image as loaded and go ahead.
                    file2.renameTo(file);
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
        File file = FileFactory.createFile(local + "_broken_link.txt");
        if (file.exists()) {
            try {
                file.delete();
            } catch (IOException ignored) {
            }
        }

        file = FileFactory.createFile(local + ".1st");
        if (file.exists()) {
            try {
                file.delete();
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
