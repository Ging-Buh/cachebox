package CB_UI.GL_UI.Activitys;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.*;
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Types.*;
import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.WriteIntoDB;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.MenuItemDivider;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;
import CB_Utils.Log.Log;
import CB_Utils.Util.CopyHelper.Copy;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.text.InputType;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.SQLiteInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static CB_Core.Types.Cache.IS_FULL;

public class Import_GSAK extends ActivityBase implements ProgressChangedEvent {
    private static final String sKlasse = "Import_GSAK";
    private static final String fields = "Caches.Code,Name,OwnerName,PlacedBy,PlacedDate,Archived,TempDisabled,HasCorrected,LatOriginal,LonOriginal,Latitude,Longitude,CacheType,Difficulty,Terrain,Container,State,Country,FavPoints,Found,GcNote,UserFlag";
    private static final String memofields = "LongDescription,ShortDescription,Hints,UserNote";
    private EditTextField edtCategory, edtDBName, edtImagesDBName, edtImagesPath;
    private CB_Button bOK, bCancel, btnSelectDB, btnSelectImagesDB, btnSelectImagesPath;
    private CB_CheckBox chkLogImages;
    private ProgressBar progressBar;
    private ScrollBox scrollBox;
    private String mDatabasePath, mImageDatabasePath, mImagesPath;
    private String mDatabaseName, mImageDatabaseName;
    private SQLiteInterface sql;
    private String[] ResultFieldsArray;
    private boolean importRuns, isCanceled;

    public Import_GSAK() {
        super("Import_GSAK");
        progressBar = new ProgressBar(UiSizes.getInstance().getButtonRectF(), "ProgressBar");
        addLast(progressBar);
        bOK = new CB_Button(Translation.get("import"));
        bCancel = new CB_Button(Translation.get("cancel"));
        this.initRow(BOTTOMUP);
        this.addNext(bOK);
        this.addLast(bCancel);

        scrollBox = new ScrollBox(0, getAvailableHeight());
        scrollBox.setBackground(this.getBackground());
        this.addLast(scrollBox);
        Box box = new Box(scrollBox.getInnerWidth(), 0); // height will be adjusted after containing all controls
        scrollBox.addChild(box);

        addScrollBox(box);

        box.adjustHeight();
        scrollBox.setVirtualHeight(box.getHeight());

        initClickHandlersAndContent();
        importRuns = false;
        isCanceled = false;
    }

    private void addScrollBox(Box box) {
        CB_Label lblCategory = new CB_Label(Translation.get("category"));
        lblCategory.setWidth(Fonts.Measure(lblCategory.getText()).width);
        box.addLast(lblCategory, FIXED);
        edtCategory = new EditTextField(this, "*" + Translation.get("category"));
        edtCategory.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addLast(edtCategory);
        CB_Label lblDBName = new CB_Label(Translation.get("GSAKDatabase"));
        lblDBName.setWidth(Fonts.Measure(lblDBName.getText()).width);
        box.addLast(lblDBName, FIXED);
        edtDBName = new EditTextField(this, "*" + Translation.get("GSAKDatabase"));
        box.addNext(edtDBName);
        btnSelectDB = new CB_Button(Translation.get("GSAKButtonSelectDB"));
        box.addLast(btnSelectDB, 0.5f);

        box.addLast(new MenuItemDivider());

        CB_Label lblImagesDBName = new CB_Label(Translation.get("GSAKImagesDatabase"));
        box.addLast(lblImagesDBName);
        edtImagesDBName = new EditTextField(this, "*" + Translation.get("GSAKImagesDatabase"));
        box.addNext(edtImagesDBName);
        btnSelectImagesDB = new CB_Button(Translation.get("GSAKButtonSelectImagesDB"));
        box.addLast(btnSelectImagesDB, 0.5f);

        CB_Label lblImagesPath = new CB_Label(Translation.get("GSAKImagesPath"));
        box.addLast(lblImagesPath);
        edtImagesPath = new EditTextField(this, "*" + Translation.get("GSAKImagesPath"));
        box.addNext(edtImagesPath);
        btnSelectImagesPath = new CB_Button(Translation.get("GSAKButtonSelectImagesPath"));
        box.addLast(btnSelectImagesPath, 0.5f);

        chkLogImages = new CB_CheckBox("GSAKwithLogImages");
        box.addNext(chkLogImages, FIXED);
        CB_Label lblLogImages = new CB_Label(Translation.get("GSAKwithLogImages"));
        box.addLast(lblLogImages);
    }

    private void initClickHandlersAndContent() {
        bOK.addClickHandler((v, x, y, pointer, button) -> {
            importRuns = true;
            bOK.disable();
            GL.that.postAsync(() -> {
                doImport();
                finish();
            });
            return true;
        });

        bCancel.addClickHandler((v, x, y, pointer, button) -> {
            if (importRuns) {
                bOK.enable();
                isCanceled = true;
            } else {
                finish();
            }
            return true;
        });

        edtDBName.setTextFieldListener(new EditTextFieldBase.TextFieldListener() {
            @Override
            public void keyTyped(EditTextFieldBase textField, char key) {
                File file = FileFactory.createFile(mDatabasePath + "/" + edtDBName.getText());
                if (file.exists())
                    bOK.enable();
                else
                    bOK.disable();
            }

            @Override
            public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight) {

            }
        });

        btnSelectDB.addClickHandler((v, x, y, pointer, button) -> {
            mDatabasePath = Config.GSAKLastUsedDatabasePath.getValue();
            if (mDatabasePath.length() == 0) {
                mDatabasePath = Config.mWorkPath + "/User";
            }
            PlatformConnector.getFile(mDatabasePath, "*.db3", Translation.get("GSAKTitleSelectDB"), Translation.get("GSAKButtonSelectDB"), PathAndName -> {
                File file = FileFactory.createFile(PathAndName);
                mDatabasePath = file.getParent();
                mDatabaseName = file.getName();
                edtDBName.setText(mDatabaseName);
            });
            return true;
        });

        btnSelectImagesDB.addClickHandler((v, x, y, pointer, button) -> {
            mImageDatabasePath = Config.GSAKLastUsedImageDatabasePath.getValue();
            if (mImageDatabasePath.length() == 0) {
                mImageDatabasePath = Config.mWorkPath + "/User";
            }
            PlatformConnector.getFile(mImageDatabasePath, "*.db3", Translation.get("GSAKTitleSelectImagesDB"), Translation.get("GSAKButtonSelectDB"), PathAndName -> {
                File file = FileFactory.createFile(PathAndName);
                mImageDatabasePath = file.getParent();
                mImageDatabaseName = file.getName();
                edtImagesDBName.setText(mImageDatabaseName);
            });
            return true;
        });

        btnSelectImagesPath.addClickHandler(((v, x, y, pointer, button) -> {
            mImagesPath = Config.GSAKLastUsedImagesPath.getValue();
            if (mImagesPath.length() == 0) {
                mImagesPath = Config.mWorkPath + "/User";
            }
            PlatformConnector.getFolder(mImagesPath, Translation.get("GSAKTitleSelectImagesPath"), Translation.get("GSAKButtonSelectImagesPath"), Path -> {
                File file = FileFactory.createFile(Path);
                mImagesPath = file.getAbsolutePath();
                edtImagesPath.setText(mImagesPath);
            });
            return true;
        }));

        edtCategory.setText("GSAK_Import");
        mDatabasePath = Config.GSAKLastUsedDatabasePath.getValue();
        if (mDatabasePath == null) mDatabasePath = "";
        if (mDatabasePath.length() == 0) {
            mDatabasePath = Config.mWorkPath + "/User";
        }
        mDatabaseName = Config.GSAKLastUsedDatabaseName.getValue();
        if (mDatabaseName == null) mDatabaseName = "";
        if (mDatabaseName.length() == 0) {
            mDatabaseName = "sqlite.db3";
        }
        edtDBName.setText(mDatabaseName);

        mImageDatabasePath = Config.GSAKLastUsedImageDatabasePath.getValue();
        if (mImageDatabasePath == null) mImageDatabasePath = "";
        if (mImageDatabasePath.length() == 0) {
            mImageDatabasePath = Config.mWorkPath + "/User";
        }
        mImageDatabaseName = Config.GSAKLastUsedImageDatabaseName.getValue();
        if (mImageDatabaseName == null) mImageDatabaseName = "";
        edtImagesDBName.setText(mImageDatabaseName);

        mImagesPath = Config.GSAKLastUsedImagesPath.getValue();
        if (mImagesPath == null) mImagesPath = "";
        if (mImagesPath.length() == 0) {
            mImagesPath = Config.mWorkPath + "/User";
        }
        edtImagesPath.setText(mImagesPath);

        chkLogImages.setChecked(Config.withLogImages.getValue());

        progressBar.setProgress(0, "");
    }


    private void doImport() {
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
        if (sql.openReadOnly(mDatabasePath + "/" + mDatabaseName)) {
            Config.GSAKLastUsedDatabasePath.setValue(mDatabasePath);
            Config.GSAKLastUsedDatabaseName.setValue(mDatabaseName);
            Config.withLogImages.setValue(chkLogImages.isChecked());
            Config.AcceptChanges();
            Database.Data.sql.beginTransaction();

            int count = 0;
            CoreCursor c = sql.rawQuery("select count(*) from Caches", null);
            c.moveToFirst();
            int anz = c.getInt(0);
            CoreCursor reader = sql.rawQuery("select " + ResultFields + " from Caches inner join CacheMemo on Caches.Code = CacheMemo.Code", null);
            reader.moveToFirst();
            while (!reader.isAfterLast() && !isCanceled) {
                count++;
                ProgresssChangedEventList.Call("" + count + "/" + anz, count * 100 / anz);
                String GcCode = "";
                try {
                    GcCode = reader.getString("Code");
                    Log.trace(sKlasse, GcCode);
                    Cache cache = createGeoCache(reader);
                    if (cache != null && GcCode.length() > 0) {
                        cache = addAttributes(cache);
                        cache = addWayPoints(cache);
                        // GroundspeakAPI.GeoCacheRelated geocache = new GroundspeakAPI.GeoCacheRelated(cache, createLogs(cache), new ArrayList<>());
                        GroundspeakAPI.GeoCacheRelated geocache = new GroundspeakAPI.GeoCacheRelated(cache, new ArrayList<>(), new ArrayList<>());
                        WriteIntoDB.CacheAndLogsAndImagesIntoDB(geocache, gpxFilename, false);
                    }
                } catch (Exception ex) {
                    Log.err(sKlasse, "Import " + GcCode, ex);
                }
                reader.moveToNext();
            }
            reader.close();

            writeLogs();

            Database.Data.sql.setTransactionSuccessful();
        }
        PlatformConnector.freeSQLInstance(sql);
        Database.Data.sql.endTransaction();
        Database.Data.GPXFilenameUpdateCacheCount();

        if (mImageDatabaseName.length() > 0) {
            doImportImages("CacheImages");
            if (chkLogImages.isChecked())
                doImportImages("LogImages");
        }

        FilterInstances.setLastFilter(new FilterProperties());
        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());

    }

    private void doImportImages(String tableName) {
        File file = FileFactory.createFile(mImageDatabasePath + "/" + mImageDatabaseName);
        if (file.exists()) {
            // sql.execSQL("ATTACH DATABASE " + file.getAbsolutePath() + " AS imagesLink");
            SQLiteInterface sqlImageLink = PlatformConnector.getSQLInstance();
            if (sqlImageLink.openReadOnly(file.getAbsolutePath())) {
                Config.GSAKLastUsedImageDatabasePath.setValue(mImageDatabasePath);
                Config.GSAKLastUsedImageDatabaseName.setValue(mImageDatabaseName);
                Config.GSAKLastUsedImagesPath.setValue(mImagesPath);
                Config.AcceptChanges();
            }
            CoreCursor c = sql.rawQuery("select count(*) from " + tableName, null);
            c.moveToFirst();
            int anz = c.getInt(0);
            int count = 0;
            progressBar.resetProgress("Wait for Images query");
            String cmd = "select iCode,iName,iDescription,iGuid,iImage from " + tableName;
            CoreCursor imagesReader = sql.rawQuery(cmd, null);
            imagesReader.moveToFirst();
            while (!imagesReader.isAfterLast() && !isCanceled) {
                count++;
                ProgresssChangedEventList.Call("" + count + "/" + anz, count * 100 / anz);
                String link = imagesReader.getString("iImage");
                CoreCursor imageLinkReader = sqlImageLink.rawQuery("select Fname from files where link=\"" + link + "\"", null);
                imageLinkReader.moveToFirst();
                String fName = imageLinkReader.getString("Fname");
                if (fName != null) {
                    ImageEntry imageEntry = new ImageEntry();
                    imageEntry.GcCode = imagesReader.getString("iCode");
                    if (imageEntry.GcCode != null) {
                        imageEntry.Description = imagesReader.getString("iName");
                        if (imageEntry.Description == null) imageEntry.Description = "";
                        imageEntry.ImageUrl = link;
                        ProgresssChangedEventList.Call(fName, count + "/" + anz, count * 100 / anz);
                        copyImage(mImagesPath + "/" + fName, imageEntry);
                    }
                }
                imageLinkReader.close();
                imagesReader.moveToNext();
            }
            imagesReader.close();
        }
    }

    private void copyImage(String source, ImageEntry imageEntry) {
        imageEntry = DescriptionImageGrabber.BuildAdditionalImageFilenameHashNew(imageEntry);
        File dst = FileFactory.createFile(imageEntry.LocalPath);
        /* create parent directories, if necessary */
        final File parent = dst.getParentFile();
        if ((parent != null) && !parent.exists()) {
            parent.mkdirs();
        }
        if (!dst.exists()) {
            File src = FileFactory.createFile(source);
            if (src.exists()) {
                try {
                    Copy.copyFolder(src, dst);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /*
    private ArrayList<LogEntry> createLogs(Cache cache) {
        ArrayList<LogEntry> logList = new ArrayList<>();
        if (numberOfLogs > 0) {
            String cmd = "select Logs.lLogId,lType,lBy,lDate,LogMemo.lText as lText from Logs inner join LogMemo on LogMemo.lLogId = Logs.lLogId where Logs.lParent = ?";
            if (numberOfLogs < Integer.MAX_VALUE) cmd = cmd + " order by LogMemo.lLogId desc";
            CoreCursor LogsReader = sql.rawQuery(cmd, new String[]{cache.getGcCode()});
            LogsReader.moveToFirst();
            int count = 0;
            while (!LogsReader.isAfterLast() && count++ < numberOfLogs) {
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
        }
        return logList;
    }

     */

    private void writeLogs() {
        CoreCursor c = sql.rawQuery("select count(*) from Logs", null);
        c.moveToFirst();
        int anz = c.getInt(0);
        int count = 0;
        progressBar.resetProgress("Wait for Logs query");
        String cmd = "select Logs.lLogId,Logs.lParent,lType,lBy,lDate,LogMemo.lText as lText from Logs inner join LogMemo on LogMemo.lLogId = Logs.lLogId";
        CoreCursor LogsReader = sql.rawQuery(cmd, null);
        LogsReader.moveToFirst();
        while (!LogsReader.isAfterLast() && !isCanceled) {
            count++;
            // ProgresssChangedEventList.Call("" + count + "/" + anz, count * 100 / anz);
            progressBar.setProgress(count * 100 / anz, "" + count + "/" + anz);
            LogEntry logEntry = new LogEntry();
            logEntry.CacheId = Cache.GenerateCacheId(LogsReader.getString("lParent"));
            logEntry.Comment = LogsReader.getString("lText");
            logEntry.Finder = LogsReader.getString("lBy");
            logEntry.Timestamp = DateFromString(LogsReader.getString("lDate"));
            logEntry.Type = LogTypes.parseString(LogsReader.getString("lType"));
            logEntry.Id = LogsReader.getInt("lLogId");

            WriteIntoDB.logDAO.WriteToDatabase(logEntry);

            LogsReader.moveToNext();
        }
        LogsReader.close();

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
                        Log.err(sKlasse, "get no GCCode");
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
                    cache.setType(CacheTypeFrom1CharAbbreviation(reader.getString(ii)));
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
                    cache.setUserNote(reader.getString(ii));
                    break;
                case "LatOriginal":
                    break;
                case "LonOriginal":
                    break;
                case "Latitude":
                    break;
                case "Longitude":
                    break;
                case "UserFlag":
                    cache.setFavorite(reader.getInt(ii) == 0 ? false : true);
                    break;
                default:
                    // Remind the programmer
                    Log.err(sKlasse, "createGeoCache: " + ResultFieldsArray[ii] + " not handled");
            }
        }
        return cache;
    }

    private CacheSizes CacheSizeFromString(String container) {
        // R=regular, L=large, M=micro, S=small, V=virtual, and U=unknown
        switch (container.toLowerCase()) {
            case "regular":
                return CacheSizes.regular;
            case "large":
                return CacheSizes.large;
            case "micro":
                return CacheSizes.micro;
            case "small":
                return CacheSizes.small;
            case "virtual":
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
        switch (abbreviation) {
            case "A":
                return CacheTypes.APE;
            case "B":
                return CacheTypes.Letterbox;
            case "C":
                return CacheTypes.CITO;
            case "D":
                return CacheTypes.Event; // Groundspeak Lost and Found Celebration
            case "E":
                return CacheTypes.Event;
            case "F":
                return CacheTypes.Event; // Lost and Found Event
            case "G":
                return CacheTypes.Cache; // BenchMark
            case "H":
                return CacheTypes.Cache; // Groundspeak HQ Cache
            case "I":
                return CacheTypes.Wherigo;
            case "J":
                return CacheTypes.Giga;
            case "L":
                return CacheTypes.Cache; // Locationless
            case "M":
                return CacheTypes.Multi;
            case "N":
                return CacheTypes.Cache; // BenchMark
            case "O":
                return CacheTypes.Cache; // Other
            case "P":
                return CacheTypes.Event; // Groundspeak Block Party
            case "Q":
                return CacheTypes.Lab;
            case "R":
                return CacheTypes.Earth;
            case "T":
                return CacheTypes.Traditional;
            case "U":
                return CacheTypes.Mystery;
            case "V":
                return CacheTypes.Virtual;
            case "W":
                return CacheTypes.Camera;
            case "X":
                return CacheTypes.Cache; // Maze
            case "Y":
                return CacheTypes.Cache; // Waymark
            case "Z":
                return CacheTypes.MegaEvent;
        }
        Log.err(sKlasse, "Undefined abbreviation:" + abbreviation);
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
    public void ProgressChangedEventCalled(String message, String progressMessage, int progress) {
        progressBar.setProgress(progress, progressMessage);
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
