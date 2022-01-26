package de.droidcachebox.dataclasses;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.utils.log.Log;

public class Draft implements Serializable {

    private static final String sClass = "Draft";
    private static final long serialVersionUID = 4110771837489396946L;

    public long Id;
    public long CacheId;
    public String gcCode = "";
    public String gcLogReference = ""; // (mis)used for LogId (or ReferenceCode)
    public Date timestamp;
    public LogType type;
    public int cacheType;
    public String comment = "";
    public String CacheName = "";
    public String CacheUrl = "";
    public boolean isUploaded;
    public int gc_Vote;
    public boolean isTbDraft = false;
    public String TbName = "";
    public String TbIconUrl = "";
    public String TravelBugCode = "";
    public String TrackingNumber = "";
    public boolean isDirectLog = false; // obsolete
    public boolean usedFavoritePoint;
    private int foundNumber;

    public Draft(Draft fne) {
        Id = fne.Id;
        CacheId = fne.CacheId;
        gcCode = fne.gcCode;
        gcLogReference = fne.gcLogReference;
        timestamp = fne.timestamp;
        type = fne.type;
        cacheType = fne.cacheType;
        comment = fne.comment;
        foundNumber = fne.foundNumber;
        CacheName = fne.CacheName;
        CacheUrl = fne.CacheUrl;
        isUploaded = fne.isUploaded;
        gc_Vote = fne.gc_Vote;
        isTbDraft = fne.isTbDraft;
        TbName = fne.TbName;
        TbIconUrl = fne.TbIconUrl;
        TravelBugCode = fne.TravelBugCode;
        TrackingNumber = fne.TrackingNumber;
        isDirectLog = fne.isDirectLog;
    }

    public Draft(LogType logType) {
        Id = -1;
        type = logType;
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
        type = LogType.GC2CB_LogType(reader.getInt(5));
        foundNumber = reader.getInt(6);
        comment = reader.getString(7);
        Id = reader.getLong(8);
        CacheUrl = reader.getString(9);
        isUploaded = reader.getInt(10) != 0;
        gc_Vote = reader.getInt(11);
        isTbDraft = reader.getInt(12) != 0;
        TbName = reader.getString(13);
        TbIconUrl = reader.getString(14);
        TravelBugCode = reader.getString(15);
        TrackingNumber = reader.getString(16);
        isDirectLog = reader.getInt(17) != 0;
        gcLogReference = reader.getString("GcId");
        if (gcLogReference == null) gcLogReference = "";
    }

    public String getTypeString() {
        switch (type) {
            case found:
                return "#" + foundNumber + " - Found it!";
            case attended:
                return "Attended";
            case webcam_photo_taken:
                return "Webcam Photo Taken";
            case didnt_find:
                return "Did not find!";
            case needs_maintenance:
                return "Needs Maintenance";
            case note:
                return "Write Note";
        }
        return "";
    }

    public void writeToDatabase() {
        Parameters args = new Parameters();
        args.put("CacheId", CacheId);
        args.put("GcCode", gcCode);
        args.put("GcId", gcLogReference);
        args.put("Name", CacheName);
        args.put("CacheType", cacheType);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String stimestamp = iso8601Format.format(timestamp);
        args.put("Timestamp", stimestamp);
        args.put("Type", type.gsLogTypeId);
        args.put("FoundNumber", foundNumber);
        args.put("Comment", comment);
        if (Id >= 0)
            args.put("Id", Id); // bei Update!!!
        args.put("Url", CacheUrl);
        args.put("Uploaded", isUploaded);
        args.put("gc_Vote", gc_Vote);
        args.put("TbFieldNote", isTbDraft);
        args.put("TbName", TbName);
        args.put("TbIconUrl", TbIconUrl);
        args.put("TravelBugCode", TravelBugCode);
        args.put("TrackingNumber", TrackingNumber);
        args.put("directLog", isDirectLog);
        try {
            DraftsDatabase.getInstance().insertWithConflictReplace("Fieldnotes", args);
        } catch (Exception exc) {
            Log.err(sClass, exc.toString());
            return;
        }
        // search FieldNote Id : should be the last entry
        CoreCursor reader = DraftsDatabase.getInstance()
                .rawQuery("select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog, GcId from FieldNotes where GcCode='" + gcCode
                        + "' and type=" + type.gsLogTypeId, null);
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Draft fne = new Draft(reader);
            Id = fne.Id;
            reader.moveToNext();
        }
        reader.close();
        if (Id == -1) {
            if (isTbDraft)
                Log.err(sClass, "TB-Log not saved: " + TravelBugCode + " in " + gcCode + ".");
            else
                Log.err(sClass, "Cache-Log not saved: " + gcCode + "");
        }
    }

    public void updateDatabase() {
        if (timestamp == null)
            timestamp = new Date();
        Parameters args = new Parameters();
        args.put("cacheid", CacheId);
        args.put("gccode", gcCode);
        if (gcLogReference == null) gcLogReference = "";
        args.put("GcId", gcLogReference);
        args.put("name", CacheName);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String stimestamp = iso8601Format.format(timestamp);
        args.put("timestamp", stimestamp);
        args.put("type", type.gsLogTypeId);
        args.put("foundnumber", foundNumber);
        args.put("comment", comment);
        args.put("cachetype", cacheType);
        args.put("url", CacheUrl);
        args.put("Uploaded", isUploaded);
        args.put("gc_Vote", gc_Vote);
        args.put("TbFieldNote", isTbDraft);
        args.put("TbName", TbName);
        args.put("TbIconUrl", TbIconUrl);
        args.put("TravelBugCode", TravelBugCode);
        args.put("TrackingNumber", TrackingNumber);
        args.put("directLog", isDirectLog);
        try {
            DraftsDatabase.getInstance().update("FieldNotes", args, "id=" + Id, null);
        } catch (Exception ignored) {
        }
    }

    public void deleteFromDatabase() {
        try {
            DraftsDatabase.getInstance().delete("FieldNotes", "id=" + Id, null);
        } catch (Exception ignored) {
        }
    }

    public boolean equals(Draft fne) {
        boolean ret = true;
        if (gcLogReference != null && fne.gcLogReference != null) {
            if (!gcLogReference.equals(fne.gcLogReference))
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
        if (isUploaded != fne.isUploaded)
            ret = false;
        if (gc_Vote != fne.gc_Vote)
            ret = false;
        if (isTbDraft != fne.isTbDraft)
            ret = false;
        if (!TravelBugCode.equals(fne.TravelBugCode))
            ret = false;
        if (!TrackingNumber.equals(fne.TrackingNumber))
            ret = false;

        return ret;
    }

    public int getFoundNumber() {
        return foundNumber;
    }

    public void setFoundNumber(int foundNumber) {
        this.foundNumber = foundNumber;
    }
}
