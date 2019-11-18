package de.droidcachebox.database;

import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.utils.*;
import de.droidcachebox.utils.log.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CacheDetail implements Serializable {
    private static final long serialVersionUID = 2088367633865443637L;
    private static final String log = "CacheDetail";

    /*
     * Public Member
     */

    /**
     * Erschaffer des Caches
     */
    public String PlacedBy = "";

    /**
     * Datum, an dem der Cache versteckt wurde
     */
    public Date DateHidden;
    /**
     * ApiStatus 0 = Cache.NOT_LIVE: Cache wurde nicht per Api hinzugefuegt
     * 1 = Cache.IS_LITE: Cache wurde per GC Api hinzugefuegt und ist noch nicht komplett geladen (IsLite = true)
     * 2 = Cache.IS_FULL: Cache wurde per GC Api hinzugefuegt und ist komplett geladen (IsLite = false)
     */
    public byte ApiStatus;

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
    public String TourName = "";

    /**
     * Name der GPX-Datei aus der importiert wurde
     */
    public long GPXFilename_ID = 0;

    /**
     * URL des Caches
     */
    public String Url = "";

    /**
     * Country des Caches
     */
    public String Country = "";

    /**
     * State des Caches
     */
    public String State = "";
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
    private ArrayList<Attributes> AttributeList = null;

    /**
     * Constructor
     */
    public CacheDetail() {
        this.DateHidden = new Date();
        AttributeList = null;

    }

    public void dispose() {
        // clear all Lists
        if (AttributeList != null) {
            AttributeList.clear();
            AttributeList = null;
        }

        if (spoilerRessources != null) {
            for (int i = 0, n = spoilerRessources.size(); i < n; i++) {
                ImageEntry entry = spoilerRessources.get(i);
                entry.dispose();
            }
            spoilerRessources.clear();
            spoilerRessources = null;
        }

        // if (waypoints != null)
        // {
        // for (int i = 0, n = waypoints.size(); i < n; i++)
        // {
        // Waypoint entry = waypoints.get(i);
        // entry.dispose();
        // }
        //
        // waypoints.clear();
        // }

        tmpNote = null;
        tmpSolver = null;
        TourName = null;
        PlacedBy = null;
        // setOwner(null);
        DateHidden = null;
        Url = null;
        Country = null;
        State = null;
        // setHint(null);
        shortDescription = null;
        longDescription = null;

    }

    public boolean isAttributePositiveSet(Attributes attribute) {
        return attributesPositive.BitAndBiggerNull(Attributes.GetAttributeDlong(attribute));
        // return (attributesPositive & Attributes.GetAttributeDlong(attribute))
        // > 0;
    }

    public boolean isAttributeNegativeSet(Attributes attribute) {
        return attributesNegative.BitAndBiggerNull(Attributes.GetAttributeDlong(attribute));
        // return (attributesNegative & Attributes.GetAttributeDlong(attribute))
        // > 0;
    }

    public void addAttributeNegative(Attributes attribute) {
        if (attributesNegative == null)
            attributesNegative = new DLong(0, 0);
        attributesNegative.BitOr(Attributes.GetAttributeDlong(attribute));
    }

    public void addAttributePositive(Attributes attribute) {
        if (attributesPositive == null)
            attributesPositive = new DLong(0, 0);
        attributesPositive.BitOr(Attributes.GetAttributeDlong(attribute));
    }

    public void setAttributesPositive(DLong i) {
        attributesPositive = i;
    }

    public void setAttributesNegative(DLong i) {
        attributesNegative = i;
    }

    public DLong getAttributesNegative(long Id) {
        if (this.attributesNegative == null) {
            CoreCursor c = Database.Data.sql.rawQuery("select AttributesNegative,AttributesNegativeHigh from Caches where Id=?", new String[]{String.valueOf(Id)});
            c.moveToFirst();
            while (!c.isAfterLast()) {
                if (!c.isNull(0))
                    this.attributesNegative = new DLong(c.getLong(1), c.getLong(0));
                else
                    this.attributesNegative = new DLong(0, 0);
                break;
            }
            c.close();
        }
        return this.attributesNegative;
    }

    public DLong getAttributesPositive(long Id) {
        if (this.attributesPositive == null) {
            CoreCursor c = Database.Data.sql.rawQuery("select AttributesPositive,AttributesPositiveHigh from Caches where Id=?", new String[]{String.valueOf(Id)});
            c.moveToFirst();
            while (!c.isAfterLast()) {
                if (!c.isNull(0))
                    this.attributesPositive = new DLong(c.getLong(1), c.getLong(0));
                else
                    this.attributesPositive = new DLong(0, 0);
                break;
            }
            ;
            c.close();
        }
        return this.attributesPositive;
    }

    public ArrayList<Attributes> getAttributes(long Id) {
        if (AttributeList == null) {
            AttributeList = Attributes.getAttributes(this.getAttributesPositive(Id), this.getAttributesNegative(Id));
        }

        return AttributeList;
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

        String gcCode = cache.getGcCode();
        if (gcCode.length() < 4)
            return;

        spoilerRessources = new CB_List<ImageEntry>();

        synchronized (spoilerRessources) {

            String directory = "";

            // from own Repository
            String path = CB_Core_Settings.SpoilerFolderLocal.getValue();
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
                path = CB_Core_Settings.DescriptionImageFolderLocal.getValue();
                // Log.debug(log, "from DescriptionImageFolderLocal: " + path);
                directory = path + "/" + gcCode.substring(0, 4);
                loadSpoilerResourcesFromPath(directory, cache);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // from Description Global Repository
            try {
                path = CB_Core_Settings.DescriptionImageFolder.getValue();
                // Log.debug(log, "from DescriptionImageFolder: " + path);
                directory = path + "/" + gcCode.substring(0, 4);
                loadSpoilerResourcesFromPath(directory, cache);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // from Spoiler Global Repository
            try {
                path = CB_Core_Settings.SpoilerFolder.getValue();
                // Log.debug(log, "from SpoilerFolder: " + path);
                directory = path + "/" + gcCode.substring(0, 4);
                loadSpoilerResourcesFromPath(directory, cache);
            } catch (Exception e) {
                Log.err(log, e.getLocalizedMessage());
            }

            // Add own taken photo
            directory = CB_Core_Settings.UserImageFolder.getValue();
            if (directory != null) {
                try {
                    loadSpoilerResourcesFromPath(directory, cache);
                } catch (Exception e) {
                    Log.err(log, e.getLocalizedMessage());
                }
            }
        }
    }

    private void loadSpoilerResourcesFromPath(String directory, final Cache cache) {
        // Log.trace(log, "LoadSpoilerResourcesFromPath from " + directory);
        if (!FileIO.directoryExists(directory))
            return;
        File dir = FileFactory.createFile(directory);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                filename = filename.toLowerCase(Locale.getDefault());
                if (filename.indexOf(cache.getGcCode().toLowerCase(Locale.getDefault())) >= 0) {
                    if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".png") || filename.endsWith(".gif")) {
                        // don't load Thumbs
                        if (filename.startsWith(FileFactory.THUMB) || filename.startsWith(FileFactory.THUMB_OVERVIEW + FileFactory.THUMB)) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
        String[] files = dir.list(filter);
        if (!(files == null)) {
            if (files.length > 0) {
                for (String file : files) {
                    String ext = FileIO.getFileExtension(file);
                    if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif")) {
                        ImageEntry imageEntry = new ImageEntry();
                        imageEntry.LocalPath = directory + "/" + file;
                        imageEntry.Name = file;
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
