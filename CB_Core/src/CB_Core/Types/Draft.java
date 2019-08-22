package CB_Core.Types;

import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.LogTypes;
import CB_Utils.Log.Log;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Draft implements Serializable {

    private static final String log = "Draft";
    private static final long serialVersionUID = 4110771837489396946L;

    public long Id;
    public long CacheId;
    public String gcCode = "";
    public String GcId = ""; // (mis)used for LogId (or ReferenceCode)
    public Date timestamp;
    public String typeString = "";
    public LogTypes type;
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

    private Draft(Draft fne) {
        this.Id = fne.Id;
        this.CacheId = fne.CacheId;
        this.gcCode = fne.gcCode;
        GcId=fne.GcId;
        this.timestamp = fne.timestamp;
        this.typeString = fne.typeString;
        this.type = fne.type;
        this.cacheType = fne.cacheType;
        this.comment = fne.comment;
        this.foundNumber = fne.foundNumber;
        this.CacheName = fne.CacheName;
        this.CacheUrl = fne.CacheUrl;
        this.typeIcon = fne.typeIcon;
        this.uploaded = fne.uploaded;
        this.gc_Vote = fne.gc_Vote;
        this.isTbDraft = fne.isTbDraft;
        this.TbName = fne.TbName;
        this.TbIconUrl = fne.TbIconUrl;
        this.TravelBugCode = fne.TravelBugCode;
        this.TrackingNumber = fne.TrackingNumber;
        this.isDirectLog = fne.isDirectLog;
    }

    public Draft(LogTypes Type) {
        Id = -1;
        this.type = Type;
        fillType();
    }

    Draft(CoreCursor reader) {
        CacheId = reader.getLong(0);
        gcCode = reader.getString(1).trim();
        CacheName = reader.getString(2);
        cacheType = reader.getInt(3);
        String sDate = reader.getString(4);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            timestamp = iso8601Format.parse(sDate);
        } catch (ParseException e) {
        }
        if (timestamp == null)
            timestamp = new Date();
        type = LogTypes.GC2CB_LogType(reader.getInt(5));
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
    }

    public void fillType() {
        typeIcon = type.getIconID();

        if (type == LogTypes.found || type == LogTypes.attended || type == LogTypes.webcam_photo_taken) {
            typeString = "#" + foundNumber + " - Found it!";
            if (cacheType == CacheTypes.Event.ordinal() //
                    || cacheType == CacheTypes.MegaEvent.ordinal() //
                    || cacheType == CacheTypes.Giga.ordinal() //
                    || cacheType == CacheTypes.CITO.ordinal())
                typeString = "Attended";
            if (cacheType == CacheTypes.Camera.ordinal())
                typeString = "Webcam Photo Taken";
        }

        if (type == LogTypes.didnt_find) {
            typeString = "Did not find!";
        }

        if (type == LogTypes.needs_maintenance) {
            typeString = "Needs Maintenance";
        }

        if (type == LogTypes.note) {
            typeString = "Write Note";
        }
    }

    String GetDateTimeString() {
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sDate = datFormat.format(timestamp) + "T";

        datFormat = new SimpleDateFormat("HH:mm:ss");
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        sDate += datFormat.format(timestamp) + "Z";

        return sDate;
    }

    public void WriteToDatabase() {
        Parameters args = new Parameters();
        args.put("CacheId", CacheId);
        args.put("GcCode", gcCode);
        args.put("GcId", GcId);
        args.put("Name", CacheName);
        args.put("CacheType", cacheType);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
            Log.err(log, exc.toString());
            return;
        }
        // search FieldNote Id : should be the last entry
        CoreCursor reader = Database.Drafts
                .sql.rawQuery("select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog, GcId from FieldNotes where GcCode='" + gcCode
                        + "' and type=" + type.getGcLogTypeId(), null);
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Draft fne = new Draft(reader);
            this.Id = fne.Id;
            reader.moveToNext();
        }
        reader.close();
        if (this.Id == -1) {
            if (isTbDraft)
                Log.err(log, "TB-Log not saved: " + TravelBugCode + " in " + gcCode + ".");
            else
                Log.err(log, "Cache-Log not saved: " + gcCode + "");
        }
    }

    public void UpdateDatabase() {
        if (timestamp == null)
            timestamp = new Date();
        Parameters args = new Parameters();
        args.put("cacheid", CacheId);
        args.put("gccode", gcCode);
        args.put("GcId", GcId);
        args.put("name", CacheName);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
            long count = Database.Drafts.sql.update("FieldNotes", args, "id=" + Id, null);
            if (count > 0)
                return;
        } catch (Exception exc) {
            return;
        }
    }

    public void DeleteFromDatabase() {
        try {
            Database.Drafts.sql.delete("FieldNotes", "id=" + Id, null);
        } catch (Exception exc) {
            return;
        }
    }

    public boolean equals(Draft fne) {
        boolean ret = true;
        if (!GcId.equals(fne.GcId))
            ret = false;
        if (this.Id != fne.Id)
            ret = false;
        if (this.CacheId != fne.CacheId)
            ret = false;
        if (this.gcCode != fne.gcCode)
            ret = false;
        if (this.timestamp != fne.timestamp)
            ret = false;
        if (this.typeString != fne.typeString)
            ret = false;
        if (this.type != fne.type)
            ret = false;
        if (this.cacheType != fne.cacheType)
            ret = false;
        if (this.comment != fne.comment)
            ret = false;
        if (this.foundNumber != fne.foundNumber)
            ret = false;
        if (this.CacheName != fne.CacheName)
            ret = false;
        if (this.CacheUrl != fne.CacheUrl)
            ret = false;
        if (this.typeIcon != fne.typeIcon)
            ret = false;
        if (this.uploaded != fne.uploaded)
            ret = false;
        if (this.gc_Vote != fne.gc_Vote)
            ret = false;
        if (this.isTbDraft != fne.isTbDraft)
            ret = false;
        if (this.TravelBugCode != fne.TravelBugCode)
            ret = false;
        if (this.TrackingNumber != fne.TrackingNumber)
            ret = false;
        if (this.isDirectLog != fne.isDirectLog)
            ret = false;

        return ret;
    }

    public Draft copy() {
        return new Draft(this);
    }

}
