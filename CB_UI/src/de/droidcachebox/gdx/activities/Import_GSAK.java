package de.droidcachebox.gdx.activities;

import static de.droidcachebox.database.Cache.IS_FULL;

import android.text.InputType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.droidcachebox.Config;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.Attribute;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Category;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.GeoCacheSize;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.database.GpxFilename;
import de.droidcachebox.database.ImageEntry;
import de.droidcachebox.database.LogEntry;
import de.droidcachebox.database.LogType;
import de.droidcachebox.database.SQLiteInterface;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.database.WriteIntoDB;
import de.droidcachebox.ex_import.DescriptionImageGrabber;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.ProgressBar;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.main.MenuItemDivider;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.Copy;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.log.Log;

public class Import_GSAK extends ActivityBase {
    private static final String sKlasse = "Import_GSAK";
    private static final String fields = "Caches.Code,Name,OwnerName,PlacedBy,PlacedDate,Archived,TempDisabled,HasCorrected,LatOriginal,LonOriginal,Latitude,Longitude,CacheType,Difficulty,Terrain,Container,State,Country,FavPoints,Found,GcNote,UserFlag";
    private static final String memofields = "LongDescription,ShortDescription,Hints,UserNote";
    private EditTextField edtCategory, edtDBName, edtImagesDBName, edtImagesPath;
    private CB_Button bOK, bCancel, btnSelectDB, btnSelectImagesDB, btnSelectImagesPath;
    private CB_CheckBox chkLogImages;
    private ProgressBar progressBar;
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

        ScrollBox scrollBox = new ScrollBox(0, getAvailableHeight());
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

        chkLogImages = new CB_CheckBox();
        box.addNext(chkLogImages, FIXED);
        CB_Label lblLogImages = new CB_Label(Translation.get("GSAKwithLogImages"));
        box.addLast(lblLogImages);
    }

    private void initClickHandlersAndContent() {
        bOK.setClickHandler((v, x, y, pointer, button) -> {
            importRuns = true;
            bOK.disable();
            GL.that.postAsync(() -> {
                doImport();
                finish();
            });
            return true;
        });

        bCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (importRuns) {
                bOK.enable();
                isCanceled = true;
            } else {
                finish();
            }
            return true;
        });

        edtDBName.setTextFieldListener(new EditTextField.TextFieldListener() {
            @Override
            public void keyTyped(EditTextField textField, char key) {
                AbstractFile abstractFile = FileFactory.createFile(mDatabasePath + "/" + edtDBName.getText());
                if (abstractFile.exists())
                    bOK.enable();
                else
                    bOK.disable();
            }

            @Override
            public void lineCountChanged(EditTextField textField, int lineCount, float textHeight) {

            }
        });

        btnSelectDB.setClickHandler((v, x, y, pointer, button) -> {
            mDatabasePath = Config.GSAKLastUsedDatabasePath.getValue();
            if (mDatabasePath.length() == 0) {
                mDatabasePath = Config.workPath + "/User";
            }
            new FileOrFolderPicker(mDatabasePath, "*.db3", Translation.get("GSAKTitleSelectDB"), Translation.get("GSAKButtonSelectDB"), abstractFile -> {
                mDatabasePath = abstractFile.getParent();
                mDatabaseName = abstractFile.getName();
                edtDBName.setText(mDatabaseName);
            }).show();
            return true;
        });

        btnSelectImagesDB.setClickHandler((v, x, y, pointer, button) -> {
            mImageDatabasePath = Config.GSAKLastUsedImageDatabasePath.getValue();
            if (mImageDatabasePath.length() == 0) {
                mImageDatabasePath = Config.workPath + "/User";
            }
            new FileOrFolderPicker(mImageDatabasePath, "*.db3", Translation.get("GSAKTitleSelectImagesDB"), Translation.get("GSAKButtonSelectDB"), abstractFile -> {
                mImageDatabasePath = abstractFile.getParent();
                mImageDatabaseName = abstractFile.getName();
                edtImagesDBName.setText(mImageDatabaseName);
            }).show();
            return true;
        });

        btnSelectImagesPath.setClickHandler(((v, x, y, pointer, button) -> {
            mImagesPath = Config.GSAKLastUsedImagesPath.getValue();
            if (mImagesPath.length() == 0) {
                mImagesPath = Config.workPath + "/User";
            }
            new FileOrFolderPicker(mImagesPath, Translation.get("GSAKTitleSelectImagesPath"), Translation.get("GSAKButtonSelectImagesPath"), abstractFile -> {
                mImagesPath = abstractFile.getAbsolutePath();
                edtImagesPath.setText(mImagesPath);
            }).show();
            return true;
        }));

        edtCategory.setText("GSAK_Import");
        mDatabasePath = Config.GSAKLastUsedDatabasePath.getValue();
        if (mDatabasePath == null) mDatabasePath = "";
        if (mDatabasePath.length() == 0) {
            mDatabasePath = Config.workPath + "/User";
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
            mImageDatabasePath = Config.workPath + "/User";
        }
        mImageDatabaseName = Config.GSAKLastUsedImageDatabaseName.getValue();
        if (mImageDatabaseName == null) mImageDatabaseName = "";
        edtImagesDBName.setText(mImageDatabaseName);

        mImagesPath = Config.GSAKLastUsedImagesPath.getValue();
        if (mImagesPath == null) mImagesPath = "";
        if (mImagesPath.length() == 0) {
            mImagesPath = Config.workPath + "/User";
        }
        edtImagesPath.setText(mImagesPath);

        chkLogImages.setChecked(Config.withLogImages.getValue());

        progressBar.setProgress(0, "");
    }


    private void doImport() {
        GpxFilename gpxFilename = null;
        Category category = CoreData.categories.getCategory(edtCategory.getText());
        if (category != null) // should not happen!!!
        {
            gpxFilename = category.addGpxFilename(edtCategory.getText());
            if (gpxFilename == null) {
                isCanceled = true;
            }
        }
        sql = PlatformUIBase.getSQLInstance();
        String ResultFields = fields + "," + memofields;
        ResultFieldsArray = ResultFields.split(",");
        // sql.beginTransaction();
        if (sql != null) {
            if (sql.openReadOnly(mDatabasePath + "/" + mDatabaseName)) {
                Config.GSAKLastUsedDatabasePath.setValue(mDatabasePath);
                Config.GSAKLastUsedDatabaseName.setValue(mDatabaseName);
                Config.withLogImages.setValue(chkLogImages.isChecked());
                Config.AcceptChanges();
                CBDB.Data.sql.beginTransaction();

                int count = 0;
                CoreCursor c = sql.rawQuery("select count(*) from Caches", null);
                c.moveToFirst();
                int anz = c.getInt(0);
                CoreCursor reader = sql.rawQuery("select " + ResultFields + " from Caches inner join CacheMemo on Caches.Code = CacheMemo.Code", null);
                reader.moveToFirst();
                while (!reader.isAfterLast() && !isCanceled) {
                    count++;
                    progressBar.setProgress(count * 100 / anz, count + "/" + anz);
                    String GcCode = "";
                    try {
                        GcCode = reader.getString("Code");
                        // Log.trace(sKlasse, GcCode);
                        Cache cache = createGeoCache(reader);
                        if (cache != null && GcCode.length() > 0) {
                            addAttributes(cache);
                            addWayPoints(cache);
                            // GroundspeakAPI.GeoCacheRelated geocache = new GroundspeakAPI.GeoCacheRelated(cache, createLogs(cache), new ArrayList<>());
                            GroundspeakAPI.GeoCacheRelated geocache = new GroundspeakAPI.GeoCacheRelated(cache, new ArrayList<>(), new ArrayList<>());
                            WriteIntoDB.writeCacheAndLogsAndImagesIntoDB(geocache, gpxFilename, false);
                        }
                    } catch (Exception ex) {
                        Log.err(sKlasse, "Import " + GcCode, ex);
                    }
                    reader.moveToNext();
                }
                reader.close();

                writeLogs();

                CBDB.Data.sql.setTransactionSuccessful();
            }
            PlatformUIBase.freeSQLInstance(sql);
            CBDB.Data.sql.endTransaction();
            CBDB.Data.updateCacheCountForGPXFilenames();

            if (mImageDatabaseName.length() > 0) {
                doImportImages("CacheImages");
                if (chkLogImages.isChecked())
                    doImportImages("LogImages");
            }

            FilterInstances.setLastFilter(new FilterProperties());
            EditFilterSettings.applyFilter(FilterInstances.getLastFilter());
        }
    }

    private void doImportImages(String tableName) {
        AbstractFile abstractFile = FileFactory.createFile(mImageDatabasePath + "/" + mImageDatabaseName);
        if (abstractFile.exists()) {
            // sql.execSQL("ATTACH DATABASE " + file.getAbsolutePath() + " AS imagesLink");
            SQLiteInterface sqlImageLink = PlatformUIBase.getSQLInstance();
            if (sqlImageLink == null) return;
            if (sqlImageLink.openReadOnly(abstractFile.getAbsolutePath())) {
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
                progressBar.setProgress(count * 100 / anz, count + "/" + anz);
                String link = imagesReader.getString("iImage");
                CoreCursor imageLinkReader = sqlImageLink.rawQuery("select Fname from files where link=\"" + link + "\"", null);
                imageLinkReader.moveToFirst();
                String fName = imageLinkReader.getString("Fname");
                if (fName != null) {
                    ImageEntry imageEntry = new ImageEntry();
                    imageEntry.setGcCode(imagesReader.getString("iCode"));
                    if (imageEntry.getGcCode() != null) {
                        imageEntry.setDescription(imagesReader.getString("iName"));
                        if (imageEntry.getDescription() == null) imageEntry.setDescription("");
                        imageEntry.setImageUrl(link);
                        progressBar.setProgress(count * 100 / anz, count + "/" + anz); // fName
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
        if (imageEntry != null) {
            AbstractFile dst = FileFactory.createFile(imageEntry.getLocalPath());
            /* create parent directories, if necessary */
            final AbstractFile parent = dst.getParentFile();
            if ((parent != null) && !parent.exists()) {
                parent.mkdirs();
            }
            if (!dst.exists()) {
                AbstractFile src = FileFactory.createFile(source);
                if (src.exists()) {
                    try {
                        Copy.copyFolder(src, dst);
                    } catch (Exception ignored) {
                    }
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
                logEntry.Timestamp = dateFromString(LogsReader.getString("lDate"));
                logEntry.Type = LogType.parseString(LogsReader.getString("lType"));
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
            progressBar.setProgress(count * 100 / anz, count + "/" + anz);
            LogEntry logEntry = new LogEntry();
            logEntry.cacheId = Cache.generateCacheId(LogsReader.getString("lParent"));
            logEntry.logText = LogsReader.getString("lText");
            logEntry.finder = LogsReader.getString("lBy");
            logEntry.logDate = dateFromString(LogsReader.getString("lDate"));
            logEntry.logType = LogType.parseString(LogsReader.getString("lType"));
            logEntry.logId = LogsReader.getInt("lLogId");

            CBDB.Data.WriteLogEntry(logEntry);

            LogsReader.moveToNext();
        }
        LogsReader.close();

    }

    private void addWayPoints(Cache cache) {
        String cmd = "select cLat,cLon,cName,cType,Waypoints.cCode as cCode,WayMemo.cComment as cComment from Waypoints inner join WayMemo on WayMemo.cCode = Waypoints.cCode";
        CoreCursor WaypointsReader = sql.rawQuery(cmd + " where Waypoints.cParent = ?", new String[]{cache.getGeoCacheCode()});
        WaypointsReader.moveToFirst();
        while (!WaypointsReader.isAfterLast()) {
            Waypoint waypoint = new Waypoint(true);
            waypoint.geoCacheId = cache.generatedId;
            waypoint.setCoordinate(new Coordinate((float) WaypointsReader.getDouble("cLat"), (float) WaypointsReader.getDouble("cLon")));
            waypoint.setTitle(WaypointsReader.getString("cName"));
            waypoint.setDescription(WaypointsReader.getString("cComment"));
            waypoint.waypointType = geoCacheTypeFromGSString(WaypointsReader.getString("cType"));
            waypoint.setWaypointCode(WaypointsReader.getString("cCode"));
            cache.getWayPoints().add(waypoint);
            WaypointsReader.moveToNext();
        }
        WaypointsReader.close();
    }

    private void addAttributes(Cache cache) {
        CoreCursor GcAttributesReader = sql.rawQuery("select aId,aInc from Attributes where aCode = ?", new String[]{cache.getGeoCacheCode()});
        GcAttributesReader.moveToFirst();
        while (!GcAttributesReader.isAfterLast()) {
            int aId = GcAttributesReader.getInt(0); // aId;
            int aInc = GcAttributesReader.getInt(1); // aInc;
            Attribute att = Attribute.getAttributeEnumByGcComId(aId);
            if (aInc == 1) {
                cache.addAttributePositive(att);
            } else {
                cache.addAttributeNegative(att);
            }
            GcAttributesReader.moveToNext();
        }
        GcAttributesReader.close();
    }

    private Cache createGeoCache(CoreCursor reader) {
        Cache cache = new Cache(true);
        String tmp;
        cache.setTmpNote("");
        for (int ii = 0; ii < ResultFieldsArray.length; ii++) {
            switch (ResultFieldsArray[ii]) {
                case "Caches.Code":
                    cache.setGeoCacheCode(reader.getString("Code"));
                    if (cache.getGeoCacheCode().length() == 0) {
                        Log.err(sKlasse, "get no GCCode");
                        return null;
                    }
                    cache.setUrl("https://coord.info/" + cache.getGeoCacheCode());
                    cache.generatedId = Cache.generateCacheId(cache.getGeoCacheCode());
                    cache.numTravelbugs = 0;
                    break;
                case "Name":
                    cache.setGeoCacheName(reader.getString(ii));
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
                    cache.setDateHidden(dateFromString(reader.getString(ii)));
                    break;
                case "CacheType":
                    cache.setGeoCacheType(geoCacheTypeFrom1CharAbbreviation(reader.getString(ii)));
                    break;
                case "Container":
                    cache.geoCacheSize = geoCacheSizeFromString(reader.getString(ii));
                    break;
                case "Country":
                    cache.setCountry(reader.getString(ii));
                case "State":
                    cache.setState(reader.getString(ii));
                    break;
                case "Archived":
                    boolean archived = reader.getInt(ii) != 0;
                    cache.setArchived(archived);
                    break;
                case "TempDisabled":
                    boolean available = reader.getInt("TempDisabled") == 0;
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
                    cache.setFound(reader.getInt(ii) != 0);
                    // FoundByMeDate possible
                    break;
                case "HasCorrected":
                    // " ,Latitude,Longitude,,,LatOriginal,LonOriginal,";
                    boolean hasCorrected = reader.getInt(ii) != 0;
                    // switch subValue
                    if (hasCorrected) {
                        if (CB_Core_Settings.UseCorrectedFinal.getValue()) {
                            cache.setCoordinate(new Coordinate((float) reader.getDouble("LatOriginal"), (float) reader.getDouble("LonOriginal")));
                            cache.getWayPoints().add(new Waypoint(
                                    "!?" + cache.getGeoCacheCode().substring(2),
                                    GeoCacheType.Final,
                                    "",
                                    reader.getDouble("Latitude"),
                                    reader.getDouble("Longitude"),
                                    cache.generatedId,
                                    "",
                                    "Final GSAK Corrected"));
                        } else {
                            cache.setCoordinate(new Coordinate(reader.getDouble("Latitude"), reader.getDouble("Longitude")));
                            cache.setHasCorrectedCoordinates(true);
                        }
                    } else {
                        cache.setCoordinate(new Coordinate((float) reader.getDouble("LatOriginal"), (float) reader.getDouble("LonOriginal")));
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
                case "LonOriginal":
                case "Latitude":
                case "Longitude":
                    break;
                case "UserFlag":
                    cache.setFavorite(reader.getInt(ii) != 0);
                    break;
                default:
                    // Remind the programmer
                    Log.err(sKlasse, "createGeoCache: " + ResultFieldsArray[ii] + " not handled");
            }
        }
        return cache;
    }

    private GeoCacheSize geoCacheSizeFromString(String container) {
        // R=regular, L=large, M=micro, S=small, V=virtual, and U=unknown
        switch (container.toLowerCase()) {
            case "regular":
                return GeoCacheSize.regular;
            case "large":
                return GeoCacheSize.large;
            case "micro":
                return GeoCacheSize.micro;
            case "small":
                return GeoCacheSize.small;
            case "virtual":
                return GeoCacheSize.other; // not in CB
            case "unknown":
                return GeoCacheSize.other;
        }
        return GeoCacheSize.other;
    }

    private GeoCacheType geoCacheTypeFromGSString(String cacheType) {
        switch (cacheType) {
            case "Parking Area":
                return GeoCacheType.ParkingArea;
            case "Reference Point":
                return GeoCacheType.ReferencePoint;
            case "Final Location":
                return GeoCacheType.Final;
            case "Physical Stage":
                return GeoCacheType.MultiStage;
            case "Virtual Stage":
                return GeoCacheType.MultiQuestion;
            case "Trailhead":
                return GeoCacheType.Trailhead;
        }
        return GeoCacheType.ReferencePoint;
    }

    private GeoCacheType geoCacheTypeFrom1CharAbbreviation(String abbreviation) {
        switch (abbreviation) {
            case "A":
                return GeoCacheType.APE;
            case "B":
                return GeoCacheType.Letterbox;
            case "C":
                return GeoCacheType.CITO;
            case "D":
                return GeoCacheType.Event; // Groundspeak Lost and Found Celebration
            case "E":
                return GeoCacheType.Event;
            case "F":
                return GeoCacheType.Event; // Lost and Found Event
            case "G":
                return GeoCacheType.Cache; // BenchMark
            case "H":
                return GeoCacheType.Cache; // Groundspeak HQ Cache
            case "I":
                return GeoCacheType.Wherigo;
            case "J":
                return GeoCacheType.Giga;
            case "L":
                return GeoCacheType.Cache; // Locationless
            case "M":
                return GeoCacheType.Multi;
            case "N":
                return GeoCacheType.Cache; // BenchMark
            case "O":
                return GeoCacheType.Cache; // Other
            case "P":
                return GeoCacheType.HQBlockParty; // Groundspeak Block Party
            case "Q":
                return GeoCacheType.Lab;
            case "R":
                return GeoCacheType.Earth;
            case "T":
                return GeoCacheType.Traditional;
            case "U":
                return GeoCacheType.Mystery;
            case "V":
                return GeoCacheType.Virtual;
            case "W":
                return GeoCacheType.Camera;
            case "X":
                return GeoCacheType.Cache; // Maze
            case "Y":
                return GeoCacheType.Cache; // Waymark
            case "Z":
                return GeoCacheType.MegaEvent;
        }
        Log.err(sKlasse, "Undefined abbreviation:" + abbreviation);
        return GeoCacheType.Undefined;
    }

    private Date dateFromString(String d) {
        String ps = "yyyy-MM-dd";
        try {
            return new SimpleDateFormat(ps, Locale.US).parse(d);
        } catch (Exception e) {
            Log.err(sKlasse, "DateFromString", e);
            return new Date();
        }
    }


    @Override
    public void finish() {
        super.finish();
        CacheListChangedListeners.getInstance().cacheListChanged();
    }
}
