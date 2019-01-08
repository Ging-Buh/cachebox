package CB_UI.GL_UI.Activitys;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.*;
import CB_Core.Types.*;
import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.WriteIntoDB;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;
import CB_Utils.Log.Log;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.SQLiteInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static CB_Core.Types.Cache.IS_FULL;

public class Import_GSAK extends ActivityBase implements ProgressChangedEvent {
    private static final String sKlasse = "Import_GSAK";
    private static final String fields = "Caches.Code,Name,OwnerName,PlacedBy,PlacedDate,Archived,TempDisabled,HasCorrected,LatOriginal,LonOriginal,Latitude,Longitude,CacheType,Difficulty,Terrain,Container,State,Country,FavPoints,Found,GcNote";
    private static final String memofields = "LongDescription,ShortDescription,Hints,UserNote";
    EditTextField edtCategory, edtDBName;
    private Button bOK, bCancel, btnSelectDB;
    private ProgressBar progressBar;
    private String mPath;
    private String mDatabaseName;
    private SQLiteInterface sql;
    private String[] ResultFieldsArray;
    private boolean importRuns, isCanceled;

    public Import_GSAK() {
        super("Import_GSAK");
        bOK = new Button(Translation.Get("import"));
        bCancel = new Button(Translation.Get("cancel"));
        this.initRow(BOTTOMUP);
        this.addNext(bOK);
        this.addLast(bCancel);
        initRow(TOPDOWN);
        progressBar = new ProgressBar(UiSizes.that.getButtonRectF(), "ProgressBar");
        addLast(progressBar);
        Label lblCategory = new Label(Translation.Get("category"));
        lblCategory.setWidth(Fonts.Measure(lblCategory.getText()).width);
        addNext(lblCategory, FIXED);
        edtCategory = new EditTextField(this, "*" + Translation.Get("category"));
        addLast(edtCategory);
        Label lblDBName = new Label(Translation.Get("GSAKDatabase"));
        lblDBName.setWidth(Fonts.Measure(lblDBName.getText()).width);
        addNext(lblDBName, FIXED);
        edtDBName = new EditTextField(this, "*" + Translation.Get("GSAKDatabase"));
        addLast(edtDBName);
        btnSelectDB = new Button(Translation.Get("GSAKButtonSelectDB"));
        addLast(btnSelectDB);
        initClickHandlersAndContent();
        importRuns = false;
        isCanceled = false;
    }

    private void initClickHandlersAndContent() {
        bOK.setOnClickListener((v, x, y, pointer, button) -> {
            importRuns = true;
            GL.that.postAsync(() -> {
                doImport();
                finish();
            });
            return true;
        });

        bCancel.setOnClickListener((v, x, y, pointer, button) -> {
            if (importRuns) {
                isCanceled = true;
            } else {
                finish();
            }
            return true;
        });

        btnSelectDB.setOnClickListener((v, x, y, pointer, button) -> {
            mPath = Config.GSAKLastUsedDatabasePath.getValue();
            if (mPath.length() == 0) {
                mPath = Config.mWorkPath + "/User";
            }
            PlatformConnector.getFile(mPath, "*.db3", Translation.Get("GSAKTitleSelectDB"), Translation.Get("GSAKButtonSelectDB"), PathAndName -> {
                File file = FileFactory.createFile(PathAndName);
                mPath = file.getParent();
                mDatabaseName = file.getName();
                Config.GSAKLastUsedDatabasePath.setValue(mPath);
                if (mDatabaseName.length() > 0) {
                    Config.GSAKLastUsedDatabaseName.setValue(mDatabaseName);
                    bOK.enable();
                }
                Config.AcceptChanges();
                edtDBName.setText(mDatabaseName);
            });
            return true;
        });

        edtCategory.setText("GSAK_Import");
        mPath = Config.GSAKLastUsedDatabasePath.getValue();
        if (mPath.length() == 0) {
            mPath = Config.mWorkPath + "/User";
        }
        mDatabaseName = Config.GSAKLastUsedDatabaseName.getValue();
        if (mDatabaseName.length() == 0) {
            mDatabaseName = "sqlite.db3";
        }
        edtDBName.setText(mDatabaseName);
        // todo react on edit edtDBName changes, to perhaps
        File file = FileFactory.createFile(mPath + "/" + mDatabaseName);
        if (file.exists())
            bOK.enable();
        else
            bOK.disable();

        progressBar.setProgress(0, "");
    }


    public void doImport() {

        GpxFilename gpxFilename = null;
        Category category = CoreSettingsForward.Categories.getCategory(edtCategory.getText());
        if (category != null) // should not happen!!!
        {
            gpxFilename = category.addGpxFilename(edtCategory.getText());
            if (gpxFilename == null) {
                isCanceled = true;
            }
        }
        sql = PlatformConnector.getSQLInstance();
        String ResultFields = fields + "," + memofields;
        ResultFieldsArray = ResultFields.split(",");
        // sql.beginTransaction();
        if (sql.openReadOnly(mPath + "/" + mDatabaseName)) {
            Config.GSAKLastUsedDatabaseName.setValue(mDatabaseName);
            Config.AcceptChanges();
            int count = -1;
            CoreCursor c = sql.rawQuery("select count(*) from Caches", null);
            c.moveToFirst();
            int anz = c.getInt(0);
            CoreCursor reader = sql.rawQuery("select " + ResultFields + " from Caches inner join CacheMemo on Caches.Code = CacheMemo.Code", null);
            reader.moveToFirst();
            while (!reader.isAfterLast() && !isCanceled) {
                count++;
                ProgresssChangedEventList.Call("", "", count * 100 / anz);
                String GcCode = "";
                try {
                    GcCode = reader.getString("Code");
                    Log.trace(sKlasse, GcCode);
                    Cache cache = createGeoCache(reader);
                    if (cache != null && GcCode.length() > 0) {
                        cache = addAttributes(cache);
                        cache = addWayPoints(cache);
                        GroundspeakAPI.GeoCacheRelated geocache = new GroundspeakAPI.GeoCacheRelated(cache, createLogs(cache), new ArrayList<>());
                        WriteIntoDB.CacheAndLogsAndImagesIntoDB(geocache, gpxFilename);
                    }
                } catch (Exception ex) {
                    Log.err(sKlasse, "Import " + GcCode, ex);
                }
                reader.moveToNext();
            }
            reader.close();
            // sql.setTransactionSuccessful();
        }
        // sql.endTransaction();
        PlatformConnector.freeSQLInstance(sql);
    }

    private ArrayList<LogEntry> createLogs(Cache cache) {
        ArrayList<LogEntry> logList = new ArrayList<>();
        CoreCursor LogsReader = sql.rawQuery("select Logs.lLogId,lType,lBy,lDate,LogMemo.lText as lText from Logs inner join LogMemo on LogMemo.lLogId = Logs.lLogId where Logs.lParent = ?", new String[]{cache.getGcCode()});
        LogsReader.moveToFirst();
        while (!LogsReader.isAfterLast()) {
            LogEntry logEntry = new LogEntry();
            logEntry.CacheId = cache.Id;
            logEntry.Comment = LogsReader.getString("lText");
            logEntry.Finder = LogsReader.getString("lBy");
            logEntry.Timestamp = DateFromString(LogsReader.getString("lDate"));
            logEntry.Type = LogTypes.parseString(LogsReader.getString("lType"));
            logEntry.Id = LogsReader.getInt("lLogId");
            logList.add(logEntry);
            LogsReader.moveToNext();
        }
        LogsReader.close();
        return logList;
    }

    private Cache addWayPoints(Cache cache) {
        String cmd = "select cLat,cLon,cName,cType,Waypoints.cCode as cCode,WayMemo.cComment as cComment from Waypoints inner join WayMemo on WayMemo.cCode = Waypoints.cCode";
        CoreCursor WaypointsReader = sql.rawQuery(cmd + " where Waypoints.cParent = ?", new String[]{cache.getGcCode()});
        WaypointsReader.moveToFirst();
        while (!WaypointsReader.isAfterLast()) {
            Waypoint waypoint = new Waypoint(true);
            waypoint.CacheId = cache.Id;
            waypoint.Pos = new Coordinate((float) WaypointsReader.getDouble("cLat"), (float) WaypointsReader.getDouble("cLon"));
            waypoint.setTitle(WaypointsReader.getString("cName"));
            waypoint.setDescription(WaypointsReader.getString("cComment"));
            waypoint.Type = CacheTypeFromGSString(WaypointsReader.getString("cType"));
            waypoint.setGcCode(WaypointsReader.getString("cCode"));
            cache.waypoints.add(waypoint);
            WaypointsReader.moveToNext();
        }
        WaypointsReader.close();
        return cache;
    }

    private Cache addAttributes(Cache cache) {
        CoreCursor GcAttributesReader = sql.rawQuery("select aId,aInc from Attributes where aCode = ?", new String[]{cache.getGcCode()});
        GcAttributesReader.moveToFirst();
        while (!GcAttributesReader.isAfterLast()) {
            int aId = GcAttributesReader.getInt(0); // aId;
            int aInc = GcAttributesReader.getInt(1); // aInc;
            Attributes att = Attributes.getAttributeEnumByGcComId(aId);
            if (aInc == 1) {
                cache.addAttributePositive(att);
            } else {
                cache.addAttributeNegative(att);
            }
            GcAttributesReader.moveToNext();
        }
        GcAttributesReader.close();
        return cache;
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

    private CacheTypes CacheTypeFromGSString(String cacheType) {
        switch (cacheType) {
            case "Parking Area":
                return CacheTypes.ParkingArea;
            case "Reference Point":
                return CacheTypes.ReferencePoint;
            case "Final Location":
                return CacheTypes.Final;
            case "Physical Stage":
                return CacheTypes.MultiStage;
            case "Virtual Stage":
                return CacheTypes.MultiQuestion;
            case "Trailhead":
                return CacheTypes.Trailhead;
        }
        return CacheTypes.ReferencePoint;
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


    @Override
    protected void finish() {
        super.finish();
        CacheListChangedEventList.Call();
    }

    @Override
    public void ProgressChangedEventCalled(String Message, String ProgressMessage, int Progress) {
        GL.that.RunOnGL(new IRunOnGL() {
            @Override
            public void run() {
                progressBar.setProgress(Progress);
                // lblProgressMsg.setText(ProgressMessage);
                if (!Message.equals(""))
                    progressBar.setText(Message);
            }
        });
    }

    @Override
    public void onShow() {
        ProgresssChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
        ProgresssChangedEventList.Remove(this);
    }
}
