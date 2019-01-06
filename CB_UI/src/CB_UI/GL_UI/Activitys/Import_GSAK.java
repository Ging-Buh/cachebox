package CB_UI.GL_UI.Activitys;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.CB_Core_Settings;
import CB_Core.CacheSizes;
import CB_Core.CacheTypes;
import CB_Core.CoreSettingsForward;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.WriteIntoDB;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_Utils.Log.Log;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.SQLiteInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static CB_Core.Types.Cache.IS_FULL;

public class Import_GSAK extends ActivityBase {
    private static final String sKlasse = "Import_GSAK";
    private static final String fields = "Caches.Code,Name,OwnerName,PlacedBy,PlacedDate,Archived,TempDisabled,HasCorrected,LatOriginal,LonOriginal,Latitude,Longitude,CacheType,Difficulty,Terrain,Container,State,Country,FavPoints,Found,GcNote";
    private static final String memofields = "LongDescription,ShortDescription,Hints,UserNote";
    private String mPath;
    private String mDatabaseName;
    private SQLiteInterface sql;
    private String[] ResultFieldsArray;

    public Import_GSAK() {
        super("Import_GSAK");
    }

    public void doImport() {
        mPath = Config.GSAKLastUsedDatabasePath.getValue();
        if (mPath.length() == 0) {
            mPath = Config.mWorkPath + "/User";
        }
        PlatformConnector.getFile(mPath, "*.db3", Translation.Get("GSAkTitleSelectDB"), Translation.Get("GSAKButtonSelectDB"), PathAndName -> {
            File file = FileFactory.createFile(PathAndName);
            mPath = file.getParent();
            mDatabaseName = file.getName();
            Config.GSAKLastUsedDatabasePath.setValue(mPath);
            Config.AcceptChanges();
        });

        GpxFilename gpxFilename = null;
        Category category = CoreSettingsForward.Categories.getCategory("GSAK_Import");
        if (category != null) // should not happen!!!
        {
            gpxFilename = category.addGpxFilename("GSAK_Import");
            if (gpxFilename != null) {

            }
        }

        if (mPath.length() > 0) {
            sql = PlatformConnector.getSQLInstance();
            String ResultFields = fields + "," + memofields;
            ResultFieldsArray = ResultFields.split(",");
            if (sql.openReadOnly(mPath + "/" + mDatabaseName)) {
                CoreCursor reader = sql.rawQuery("select " + ResultFields + " from Caches inner join CacheMemo on Caches.Code = CacheMemo.Code", null);
                reader.moveToFirst();
                while (!reader.isAfterLast()) {
                    Cache cache = createGeoCache(reader);
                    Log.debug(sKlasse, cache.getGcCode());
                    GroundspeakAPI.GeoCacheRelated geocache = new GroundspeakAPI.GeoCacheRelated(cache, new ArrayList<>(), new ArrayList<>());
                    try {
                        WriteIntoDB.CacheAndLogsAndImagesIntoDB(geocache, gpxFilename);
                    }
                    catch (Exception ex) {
                        // InterruptedException
                        // todo handle
                    }
                    reader.moveToNext();
                }
                reader.close();
            }
            PlatformConnector.freeSQLInstance(sql);
        }
    }

    private Cache createGeoCache(CoreCursor reader) {
        Cache cache = new Cache(true);
        String tmp;
        cache.setTmpNote("");
        for (int ii = 0; ii < ResultFieldsArray.length; ii++) {
            switch (ResultFieldsArray[ii]) {
                case "Caches.Code":
                    cache.setGcCode(reader.getString("Code"));
                    if (cache.getGcCode().length() == 0) {
                        Log.err(sKlasse, "Get no GCCode");
                        return null;
                    }
                    cache.setUrl("https://coord.info/" + cache.getGcCode());
                    cache.Id = Cache.GenerateCacheId(cache.getGcCode());
                    cache.NumTravelbugs = 0;
                    break;
                case "Name":
                    cache.setName(reader.getString(ii));
                    break;
                case "Difficulty":
                    cache.setDifficulty((float) reader.getDouble(ii));
                    break;
                case "Terrain":
                    cache.setTerrain((float) reader.getDouble(ii));
                    break;
                case "FavPoints":
                    cache.favPoints = reader.getInt(ii);
                    break;
                case "PlacedDate":
                    cache.setDateHidden(DateFromString(reader.getString(ii)));
                    break;
                case "CacheType":
                    cache.Type = CacheTypeFrom1CharAbbreviation(reader.getString(ii));
                    break;
                case "Container":
                    cache.Size = CacheSizeFromString(reader.getString(ii));
                    break;
                case "Country":
                    cache.setCountry(reader.getString(ii));
                case "State":
                    cache.setState(reader.getString(ii));
                    break;
                case "Archived":
                    boolean archived = reader.getInt(ii) == 0 ? false : true;
                    cache.setArchived(archived);
                    break;
                case "TempDisabled":
                    boolean available = reader.getInt("TempDisabled") == 0 ? true : false;
                    cache.setAvailable(available);
                    break;
                case "PlacedBy":
                    cache.setPlacedBy(reader.getString(ii));
                    break;
                case "OwnerName":
                    cache.setOwner(reader.getString(ii));
                    break;
                case "GcNote":
                    cache.setTmpNote(reader.getString(ii));
                    break;
                case "Found":
                    cache.setFound(reader.getInt(ii) == 0 ? false : true);
                    // FoundByMeDate possible
                    break;
                case "HasCorrected":
                    // " ,Latitude,Longitude,,,LatOriginal,LonOriginal,";
                    boolean hasCorrected = reader.getInt(ii) == 0 ? false : true;
                    // switch subValue
                    if (hasCorrected) {
                        if (CB_Core_Settings.UseCorrectedFinal.getValue()) {
                            cache.Pos = new Coordinate((float) reader.getDouble("LatOriginal"), (float) reader.getDouble("LonOriginal"));
                            cache.waypoints.add(new Waypoint(
                                    "!?" + cache.getGcCode().substring(2),
                                    CacheTypes.Final,
                                    "",
                                    reader.getDouble("Latitude"),
                                    reader.getDouble("Longitude"),
                                    cache.Id,
                                    "",
                                    "Final GSAK Corrected"));
                        } else {
                            cache.Pos = new Coordinate(reader.getDouble("Latitude"), reader.getDouble("Longitude"));
                            cache.setHasCorrectedCoordinates(true);
                        }
                    } else {
                        cache.Pos = new Coordinate((float) reader.getDouble("LatOriginal"), (float) reader.getDouble("LonOriginal"));
                    }
                    break;
                case "ShortDescription":
                    tmp = reader.getString(ii);
                    if (tmp.length() > 0) {
                        // containsHtml liefert immer false
                        if (!tmp.contains("<")) {
                            tmp = tmp.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                        }
                        cache.setShortDescription(tmp);
                        cache.setApiStatus(IS_FULL); // got a cache without LongDescription
                    }
                    break;
                case "LongDescription":
                    tmp = reader.getString(ii);
                    if (tmp.length() > 0) {
                        // containsHtml liefert immer false
                        if (!tmp.contains("<")) {
                            tmp = tmp.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                        }
                        cache.setLongDescription(tmp);
                        cache.setApiStatus(IS_FULL);
                    }
                    break;
                case "Hints":
                    cache.setHint(reader.getString(ii));
                    break;
                case "UserNote":
                    cache.setTmpNote(cache.getTmpNote() + reader.getString(ii));
                    break;
                case "LatOriginal":
                    break;
                case "LonOriginal":
                    break;
                case "Latitude":
                    break;
                case "Longitude":
                    break;
                default:
                    // Remind the programmer
                    Log.err(sKlasse, "createGeoCache: " + ResultFieldsArray[ii] + " not handled");

                    /*
                case "attributes":
                    JSONArray attributes = API1Cache.optJSONArray(switchValue);
                    if (attributes != null) {
                        for (int j = 0; j < attributes.length(); j++) {
                            JSONObject attribute = attributes.optJSONObject(j);
                            if (attribute != null) {
                                Attributes att = Attributes.getAttributeEnumByGcComId(attribute.optInt("id", 0));
                                if (attribute.optBoolean("isOn", false)) {
                                    cache.addAttributePositive(att);
                                } else {
                                    cache.addAttributeNegative(att);
                                }
                            }
                        }
                    }
                    break;
                case "additionalWaypoints":
                    addWayPoints(cache, API1Cache.optJSONArray(switchValue));
                    break;
                case "userWaypoints":
                    addUserWayPoints(cache, API1Cache.optJSONArray(switchValue));
                    break;
                    */
            }
        }
        return cache;
    }

    private CacheSizes CacheSizeFromString(String container) {
        // R=regular, L=large, M=micro, S=Small, V=Virtual, and U=unknown
        switch (container) {
            case "regular":
                return CacheSizes.regular;
            case "large":
                return CacheSizes.large;
            case "micro":
                return CacheSizes.micro;
            case "Small":
                return CacheSizes.small;
            case "Virtual":
                return CacheSizes.other; // not in CB
            case "unknown":
                return CacheSizes.other;
        }
        return CacheSizes.other;
    }

    private CacheTypes CacheTypeFrom1CharAbbreviation(String abbreviation) {
        // T=traditional, M=multi, B=letterbox hybrid, C=CITO, E=event, L=locationless, V=virtual, W=webcam, O=Other, G=Benchmark, R=Earth, I=Wherigo and U=mystery/Unknown
        switch (abbreviation) {
            case "T":
                return CacheTypes.Traditional;
            case "M":
                return CacheTypes.Multi;
            case "B":
                return CacheTypes.Letterbox;
            case "C":
                return CacheTypes.CITO;
            case "E":
                return CacheTypes.Event;
            case "L":
                return CacheTypes.Cache; // not in CB
            case "V":
                return CacheTypes.Virtual;
            case "W":
                return CacheTypes.Camera;
            case "O":
                return CacheTypes.Cache; // not in CB
            case "G":
                return CacheTypes.Cache; // not in CB
            case "R":
                return CacheTypes.Earth;
            case "I":
                return CacheTypes.Wherigo;
            case "U":
                return CacheTypes.Mystery;
        }
        return CacheTypes.Undefined;
    }

    private Date DateFromString(String d) {
        String ps = "yyyy-MM-dd";
        try {
            return new SimpleDateFormat(ps).parse(d);
        } catch (Exception e) {
            Log.err(sKlasse, "DateFromString", e);
            return new Date();
        }
    }

}
