package de.droidcachebox.dataclasses;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.settings.AllSettings;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.DLong;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.FilenameFilter;
import de.droidcachebox.utils.log.Log;

public class CacheDetail implements Serializable {
    private static final long serialVersionUID = 2088367633865443637L;
    private static final String sClass = "CacheDetail";

    /**
     * Erschaffer des Caches
     */
    public String placedBy = "";

    /**
     * Datum, an dem der Cache versteckt wurde
     */
    public Date dateHidden;
    /**
     * ApiStatus 0 = Cache.NOT_LIVE: Cache wurde nicht per Api hinzugefuegt
     * 1 = Cache.IS_LITE: Cache wurde per GC Api hinzugefuegt und ist noch nicht komplett geladen (IsLite = true)
     * 2 = Cache.IS_FULL: Cache wurde per GC Api hinzugefuegt und ist komplett geladen (IsLite = false)
     */
    public byte apiStatus;

    /**
     * for Replication
     */
    public int noteCheckSum = 0;
    public String tmpNote = null; // nur fuer den RPC-Import
    public String userNote = "";

    /**
     * for Replication
     */
    public int solverCheckSum = 0;
    public String tmpSolver = null; // nur fuer den RPC-Import

    /**
     * Name der Tour, wenn die GPX-Datei aus GCTour importiert wurde
     */
    public String tourName = "";

    /**
     * Name der GPX-Datei aus der importiert wurde
     */
    public long gpxFilename_ID = 0;

    /**
     * URL des Caches
     */
    public String url = "";

    /**
     * Country des Caches
     */
    public String country = "";

    /**
     * State des Caches
     */
    public String state = "";
    /**
     * Kurz Beschreibung des Caches
     */
    public String shortDescription;
    /**
     * Ausfuehrliche Beschreibung des Caches Nur fuer Import Zwecke. Ist normalerweise leer, da die Description bei aus Speicherplatz
     * Gruenden bei Bedarf aus der DB geladen wird
     */
    public String longDescription;
    /**
     * Positive Attribute des Caches
     */
    private DLong attributesPositive = new DLong(0, 0);
    /**
     * Negative Attribute des Caches
     */
    private DLong attributesNegative = new DLong(0, 0);
    /**
     * Hinweis fuer diesen Cache
     */
    private String hint = "";
    /**
     * Liste der Spoiler Ressourcen
     */
    private CB_List<ImageEntry> spoilerRessources = null;
    private ArrayList<Attribute> attributeList = null;

    /**
     * Constructor
     */
    public CacheDetail() {
        this.dateHidden = new Date();
        attributeList = null;

    }

    public CacheDetail(CoreCursor reader, boolean withReaderOffset, boolean withDescription) {
        int readerOffset = withReaderOffset ? 21 : 0;
        try {
            placedBy = reader.getString(readerOffset).trim();
        } catch (Exception e) {
            placedBy = "";
        }

        if (reader.isNull(readerOffset + 5))
            apiStatus = Cache.NOT_LIVE;
        else
            apiStatus = (byte) reader.getInt(readerOffset + 5);

        String sDate = reader.getString(readerOffset + 1);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            dateHidden = iso8601Format.parse(sDate);
        } catch (Exception ex) {
            dateHidden = new Date();
        }

        url = reader.getString(readerOffset + 2).trim();

        if (reader.getString(readerOffset + 3) != null)
            tourName = reader.getString(readerOffset + 3).trim();
        else
            tourName = "";

        if (reader.getString(readerOffset + 4).length() > 0)
            gpxFilename_ID = reader.getLong(readerOffset + 4);
        else
            gpxFilename_ID = -1;
        setAttributesPositive(new DLong(reader.getLong(readerOffset + 7), reader.getLong(readerOffset + 6)));
        setAttributesNegative(new DLong(reader.getLong(readerOffset + 9), reader.getLong(readerOffset + 8)));

        if (reader.getString(readerOffset + 10) != null)
            setHint(reader.getString(readerOffset + 10).trim());
        else
            setHint("");
        country = reader.getString(readerOffset + 11);
        state = reader.getString(readerOffset + 12);

        if (withDescription) {
            longDescription = reader.getString(readerOffset + 13);
            tmpSolver = reader.getString(readerOffset + 14);
            tmpNote = reader.getString(readerOffset + 15);
            shortDescription = reader.getString(readerOffset + 16);
        }
    }

    public boolean isAttributePositiveSet(Attribute attribute) {
        return attributesPositive.bitAndBiggerNull(attribute.getDLong());
    }

    public boolean isAttributeNegativeSet(Attribute attribute) {
        return attributesNegative.bitAndBiggerNull(attribute.getDLong());
    }

    public void addAttributeNegative(Attribute attribute) {
        if (attributesNegative == null)
            attributesNegative = new DLong(0, 0);
        attributesNegative.bitOr(attribute.getDLong());
    }

    public void addAttributePositive(Attribute attribute) {
        if (attributesPositive == null)
            attributesPositive = new DLong(0, 0);
        attributesPositive.bitOr(attribute.getDLong());
    }

    public void setAttributesPositive(DLong i) {
        attributesPositive = i;
    }

    public void setAttributesNegative(DLong i) {
        attributesNegative = i;
    }

    public DLong getAttributesNegative(long generatedId) {
        if (attributesNegative == null) {
            CoreCursor c = CBDB.getInstance().rawQuery("select AttributesNegative,AttributesNegativeHigh from Caches where Id=?", new String[]{String.valueOf(generatedId)});
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    if (!c.isNull(0))
                        attributesNegative = new DLong(c.getLong(1), c.getLong(0));
                    else
                        attributesNegative = new DLong(0, 0);
                } else
                    attributesNegative = new DLong(0, 0);
                c.close();
            }
        }
        return attributesNegative;
    }

    public DLong getAttributesPositive(long generatedId) {
        if (attributesPositive == null) {
            CoreCursor c = CBDB.getInstance().rawQuery("select AttributesPositive,AttributesPositiveHigh from Caches where Id=?", new String[]{String.valueOf(generatedId)});
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    if (!c.isNull(0))
                        attributesPositive = new DLong(c.getLong(1), c.getLong(0));
                    else
                        attributesPositive = new DLong(0, 0);
                } else
                    attributesPositive = new DLong(0, 0);
                c.close();
            }
        }
        return attributesPositive;
    }

    public ArrayList<Attribute> getAttributes(long Id) {
        if (attributeList == null) {
            attributeList = getAttributes(this.getAttributesPositive(Id), this.getAttributesNegative(Id));
        }
        return attributeList;
    }

    private ArrayList<Attribute> getAttributes(DLong attributesPositive, DLong attributesNegative) {
        ArrayList<Attribute> ret = new ArrayList<>();
        for (Attribute attribute : Attribute.values()) {
            DLong att = attribute.getDLong();
            if ((att.bitAndBiggerNull(attributesPositive))) {
                attribute.setNegative(false);
                ret.add(attribute);
            }
        }
        for (Attribute attribute : Attribute.values()) {
            DLong att = attribute.getDLong();
            if ((att.bitAndBiggerNull(attributesNegative))) {
                attribute.setNegative(true);
                ret.add(attribute);
            }
        }
        return ret;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint2) {
        this.hint = hint2;

    }

    /**
     * Returns a List of Spoiler Ressources
     *
     * @return ArrayList of String
     */
    public CB_List<ImageEntry> getSpoilerRessources(Cache cache) {
        if (spoilerRessources == null) {
            loadSpoilerRessources(cache);
        }

        return spoilerRessources;
    }

    /**
     * Set a List of Spoiler Ressources
     *
     * @param value ArrayList of String
     */
    public void setSpoilerRessources(CB_List<ImageEntry> value) {
        spoilerRessources = value;
    }

    /**
     * Returns true has the Cache Spoilers else returns false
     *
     * @return Boolean
     */
    public boolean hasSpoiler(Cache cache) {
        try {
            if (spoilerRessources == null)
                loadSpoilerRessources(cache);
            return spoilerRessources.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void SpoilerForceReEvaluate(Cache cache) {
        spoilerRessources = null;
    }

    public void loadSpoilerRessources(Cache cache) {

        String gcCode = cache.getGeoCacheCode();
        if (gcCode.length() < 4)
            return;

        spoilerRessources = new CB_List<ImageEntry>();

        synchronized (spoilerRessources) {

            String directory = "";

            // from own Repository
            String path = AllSettings.SpoilerFolderLocal.getValue();
            // Log.debug(log, "from SpoilerFolderLocal: " + path);
            try {
                if (path != null && path.length() > 0) {
                    directory = path + "/" + gcCode.substring(0, 4);
                    loadSpoilerResourcesFromPath(directory, cache);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // from Description own Repository
            try {
                path = AllSettings.DescriptionImageFolderLocal.getValue();
                // Log.debug(log, "from DescriptionImageFolderLocal: " + path);
                directory = path + "/" + gcCode.substring(0, 4);
                loadSpoilerResourcesFromPath(directory, cache);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // from Description Global Repository
            try {
                path = AllSettings.DescriptionImageFolder.getValue();
                // Log.debug(log, "from DescriptionImageFolder: " + path);
                directory = path + "/" + gcCode.substring(0, 4);
                loadSpoilerResourcesFromPath(directory, cache);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // from Spoiler Global Repository
            try {
                path = AllSettings.SpoilerFolder.getValue();
                // Log.debug(log, "from SpoilerFolder: " + path);
                directory = path + "/" + gcCode.substring(0, 4);
                loadSpoilerResourcesFromPath(directory, cache);
            } catch (Exception e) {
                Log.err(sClass, e.getLocalizedMessage());
            }

            // Add own taken photo
            directory = AllSettings.UserImageFolder.getValue();
            if (directory != null) {
                try {
                    loadSpoilerResourcesFromPath(directory, cache);
                } catch (Exception e) {
                    Log.err(sClass, e.getLocalizedMessage());
                }
            }
        }
    }

    private void loadSpoilerResourcesFromPath(String directory, final Cache cache) {
        // Log.trace(log, "LoadSpoilerResourcesFromPath from " + directory);
        if (!FileIO.directoryExists(directory))
            return;
        AbstractFile dir = FileFactory.createFile(directory);
        FilenameFilter filter = (dir1, filename) -> {
            filename = filename.toLowerCase(Locale.getDefault());
            if (filename.indexOf(cache.getGeoCacheCode().toLowerCase(Locale.getDefault())) >= 0) {
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".png") || filename.endsWith(".gif")) {
                    // don't load Thumbs
                    return !filename.startsWith(FileFactory.THUMB) && !filename.startsWith(FileFactory.THUMB_OVERVIEW + FileFactory.THUMB);
                }
            }
            return false;
        };
        String[] files = dir.list(filter);
        if (!(files == null)) {
            if (files.length > 0) {
                for (String file : files) {
                    String ext = FileIO.getFileExtension(file);
                    if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif")) {
                        ImageEntry imageEntry = new ImageEntry();
                        imageEntry.setLocalPath(directory + "/" + file);
                        imageEntry.setName(file);
                        // Log.debug(log, imageEntry.Name);
                        spoilerRessources.add(imageEntry);
                    }
                }
            }
        }
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String value) {
        longDescription = value;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String value) {
        shortDescription = value;
    }

}
