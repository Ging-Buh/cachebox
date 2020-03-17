package de.droidcachebox.database;

import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.utils.log.Log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Draft implements Serializable {

    private static final String sKlasse = "Draft";
    private static final long serialVersionUID = 4110771837489396946L;

    public long Id;
    public long CacheId;
    public String gcCode = "";
    public String GcId = ""; // (mis)used for LogId (or ReferenceCode)
    public Date timestamp;
    public String typeString = "";
    public GeoCacheLogType type;
    public int cacheType;
    public String comment = "";
    public int foundNumber;
    public String CacheName = "";
    public String CacheUrl = "";
    public int typeIcon;
    public boolean uploaded;
    public int gc_Vote;
    public boolean isTbDraft = false;
    public String TbName = "";
    public String TbIconUrl = "";
    public String TravelBugCode = "";
    public String TrackingNumber = "";
    public boolean isDirectLog = false;

    public Draft(Draft fne) {
        Id = fne.Id;
        CacheId = fne.CacheId;
        gcCode = fne.gcCode;
        GcId = fne.GcId;
        timestamp = fne.timestamp;
        typeString = fne.typeString;
        type = fne.type;
        cacheType = fne.cacheType;
        comment = fne.comment;
        foundNumber = fne.foundNumber;
        CacheName = fne.CacheName;
        CacheUrl = fne.CacheUrl;
        typeIcon = fne.typeIcon;
        uploaded = fne.uploaded;
        gc_Vote = fne.gc_Vote;
        isTbDraft = fne.isTbDraft;
        TbName = fne.TbName;
        TbIconUrl = fne.TbIconUrl;
        TravelBugCode = fne.TravelBugCode;
        TrackingNumber = fne.TrackingNumber;
        isDirectLog = fne.isDirectLog;
    }

    public Draft(GeoCacheLogType logType) {
        Id = -1;
        type = logType;
        fillType();
    }

    Draft(CoreCursor reader) {
        CacheId = reader.getLong(0);
        gcCode = reader.getString(1).trim();
        CacheName = reader.getString(2);
        cacheType = reader.getInt(3);
        String sDate = reader.getString(4);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            timestamp = iso8601Format.parse(sDate);
        } catch (ParseException ignored) {
        }
        if (timestamp == null)
            timestamp = new Date();
        type = GeoCacheLogType.GC2CB_LogType(reader.getInt(5));
        foundNumber = reader.getInt(6);
        comment = reader.getString(7);
        Id = reader.getLong(8);
        CacheUrl = reader.getString(9);
        uploaded = reader.getInt(10) != 0;
        gc_Vote = reader.getInt(11);
        isTbDraft = reader.getInt(12) != 0;
        TbName = reader.getString(13);
        TbIconUrl = reader.getString(14);
        TravelBugCode = reader.getString(15);
        TrackingNumber = reader.getString(16);
        isDirectLog = reader.getInt(17) != 0;
        fillType();
        GcId = reader.getString("GcId");
        if (GcId == null) GcId = "";
    }

    public void fillType() {
        typeIcon = type.getIconID();

        if (type == GeoCacheLogType.found || type == GeoCacheLogType.attended || type == GeoCacheLogType.webcam_photo_taken) {
            typeString = "#" + foundNumber + " - Found it!";
            if (cacheType == GeoCacheType.Event.ordinal() //
                    || cacheType == GeoCacheType.MegaEvent.ordinal() //
                    || cacheType == GeoCacheType.Giga.ordinal() //
                    || cacheType == GeoCacheType.CITO.ordinal())
                typeString = "Attended";
            if (cacheType == GeoCacheType.Camera.ordinal())
                typeString = "Webcam Photo Taken";
        }

        if (type == GeoCacheLogType.didnt_find) {
            typeString = "Did not find!";
        }

        if (type == GeoCacheLogType.needs_maintenance) {
            typeString = "Needs Maintenance";
        }

        if (type == GeoCacheLogType.note) {
            typeString = "Write Note";
        }
    }

    public void writeToDatabase() {
        Parameters args = new Parameters();
        args.put("CacheId", CacheId);
        args.put("GcCode", gcCode);
        args.put("GcId", GcId);
        args.put("Name", CacheName);
        args.put("CacheType", cacheType);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
        String stimestamp = iso8601Format.format(timestamp);
        args.put("Timestamp", stimestamp);
        args.put("Type", type.getGcLogTypeId());
        args.put("FoundNumber", foundNumber);
        args.put("Comment", comment);
        if (Id >= 0)
            args.put("Id", Id); // bei Update!!!
        args.put("Url", CacheUrl);
        args.put("Uploaded", uploaded);
        args.put("gc_Vote", gc_Vote);
        args.put("TbFieldNote", isTbDraft);
        args.put("TbName", TbName);
        args.put("TbIconUrl", TbIconUrl);
        args.put("TravelBugCode", TravelBugCode);
        args.put("TrackingNumber", TrackingNumber);
        args.put("directLog", isDirectLog);
        try {
            Database.Drafts.sql.insertWithConflictReplace("Fieldnotes", args);
        } catch (Exception exc) {
            Log.err(sKlasse, exc.toString());
            return;
        }
        // search FieldNote Id : should be the last entry
        CoreCursor reader = Database.Drafts
                .sql.rawQuery("select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog, GcId from FieldNotes where GcCode='" + gcCode
                        + "' and type=" + type.getGcLogTypeId(), null);
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Draft fne = new Draft(reader);
            Id = fne.Id;
            reader.moveToNext();
        }
        reader.close();
        if (Id == -1) {
            if (isTbDraft)
                Log.err(sKlasse, "TB-Log not saved: " + TravelBugCode + " in " + gcCode + ".");
            else
                Log.err(sKlasse, "Cache-Log not saved: " + gcCode + "");
        }
    }

    public void updateDatabase() {
        if (timestamp == null)
            timestamp = new Date();
        Parameters args = new Parameters();
        args.put("cacheid", CacheId);
        args.put("gccode", gcCode);
        if (GcId == null) GcId = "";
        args.put("GcId", GcId);
        args.put("name", CacheName);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String stimestamp = iso8601Format.format(timestamp);
        args.put("timestamp", stimestamp);
        args.put("type", type.getGcLogTypeId());
        args.put("foundnumber", foundNumber);
        args.put("comment", comment);
        args.put("cachetype", cacheType);
        args.put("url", CacheUrl);
        args.put("Uploaded", uploaded);
        args.put("gc_Vote", gc_Vote);
        args.put("TbFieldNote", isTbDraft);
        args.put("TbName", TbName);
        args.put("TbIconUrl", TbIconUrl);
        args.put("TravelBugCode", TravelBugCode);
        args.put("TrackingNumber", TrackingNumber);
        args.put("directLog", isDirectLog);
        try {
            Database.Drafts.sql.update("FieldNotes", args, "id=" + Id, null);
        } catch (Exception ignored) {
        }
    }

    public void deleteFromDatabase() {
        try {
            Database.Drafts.sql.delete("FieldNotes", "id=" + Id, null);
        } catch (Exception ignored) {
        }
    }

    public boolean equals(Draft fne) {
        boolean ret = true;
        if (GcId != null && fne.GcId != null) {
            if (!GcId.equals(fne.GcId))
                ret = false;
        }
        if (Id != fne.Id)
            ret = false;
        if (CacheId != fne.CacheId)
            ret = false;
        if (!gcCode.equals(fne.gcCode))
            ret = false;
        if (timestamp != fne.timestamp)
            ret = false;
        if (!typeString.equals(fne.typeString))
            ret = false;
        if (type != fne.type)
            ret = false;
        if (cacheType != fne.cacheType)
            ret = false;
        if (!comment.equals(fne.comment))
            ret = false;
        if (foundNumber != fne.foundNumber)
            ret = false;
        if (!CacheName.equals(fne.CacheName))
            ret = false;
        if (!CacheUrl.equals(fne.CacheUrl))
            ret = false;
        if (typeIcon != fne.typeIcon)
            ret = false;
        if (uploaded != fne.uploaded)
            ret = false;
        if (gc_Vote != fne.gc_Vote)
            ret = false;
        if (isTbDraft != fne.isTbDraft)
            ret = false;
        if (!TravelBugCode.equals(fne.TravelBugCode))
            ret = false;
        if (!TrackingNumber.equals(fne.TrackingNumber))
            ret = false;
        if (isDirectLog != fne.isDirectLog)
            ret = false;

        return ret;
    }

}
