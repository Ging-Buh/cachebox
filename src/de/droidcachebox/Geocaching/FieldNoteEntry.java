package de.droidcachebox.Geocaching;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.droidcachebox.Database;
import de.droidcachebox.R;

import android.content.ContentValues;
import android.database.Cursor;

public class FieldNoteEntry implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4110771837489396946L;
	
	public long Id;
	public long CacheId;
    public String gcCode;
    public Date timestamp;
    public String typeString;
    public int type;
    public int cacheType;
    public String comment;
    public int foundNumber;
    public String CacheName;
    public String CacheUrl;
    public int typeIcon;
    public int typeIconId;

	public FieldNoteEntry(int type) {
		Id = -1;
		this.type = type;
        fillType();
	}

    public FieldNoteEntry(Cursor reader)
    {
    	CacheId = reader.getLong(0);
    	Id = reader.getLong(8);
        gcCode = reader.getString(1).trim();
        
        String sDate = reader.getString(4);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
        	timestamp = iso8601Format.parse(sDate);
        } catch (ParseException e) {
		}
        if (timestamp == null)
        	timestamp = new Date();
        
        type = reader.getInt(5);
        cacheType = reader.getInt(3);
        comment = reader.getString(7);
        foundNumber = reader.getInt(6);
        CacheName = reader.getString(2);
        CacheUrl = reader.getString(9);

        fillType();
    
    }
    
    private void fillType()
    {
        if (type == 1)
        {
            typeString = "#" + foundNumber + " - Found it!";
            typeIconId = R.drawable.smilie_gross;
            typeIcon = 0;
            if (cacheType == 5 || cacheType == 6 || cacheType == 7)
                typeString = "Attended";
            if (cacheType == 3)
                typeString = "Webcam Photo Taken";
        }

        if (type == 2)
        {
            typeString = "Did not find!";
            typeIconId = R.drawable.icon_sad;
            typeIcon = 1;
        }

        if (type == 3)
        {
            typeString = "Needs Maintenance";
            typeIconId = R.drawable.maintenance;
            typeIcon = 5;
        }

        if (type == 4)
        {
            typeString = "Write Note";
            typeIconId = R.drawable.log2;
            typeIcon = 2;
        }
    }
    
    public void WriteToDatabase()
    {
        ContentValues args = new ContentValues();
        args.put("cacheid", CacheId);
        args.put("gccode", gcCode);
        args.put("name", CacheName);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stimestamp = iso8601Format.format(timestamp);
        args.put("timestamp", stimestamp);
        args.put("type", type);
        args.put("foundnumber", foundNumber);
        args.put("comment", comment);
        args.put("cachetype", cacheType);
        args.put("url", CacheUrl);
        
        try
        {
        	Database.FieldNotes.myDB.insert("Fieldnotes", null, args);
        } catch (Exception exc)
        {
        	return;       
        }
        // search FieldNote Id
        Cursor reader = Database.FieldNotes.myDB.rawQuery("select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url from FieldNotes where GcCode='" + gcCode + "' and type=" + type, null);
    	reader.moveToFirst();
        while(reader.isAfterLast() == false)
        {
            FieldNoteEntry fne = new FieldNoteEntry(reader);
            this.Id = fne.Id;
            reader.moveToNext();
        }
        reader.close();		
    }

    public void UpdateDatabase()
    {
    	if (timestamp == null)
    		timestamp = new Date();
        ContentValues args = new ContentValues();
        args.put("cacheid", CacheId);
        args.put("gccode", gcCode);
        args.put("name", CacheName);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stimestamp = iso8601Format.format(timestamp);
        args.put("timestamp", stimestamp);
        args.put("type", type);
        args.put("foundnumber", foundNumber);
        args.put("comment", comment);
        args.put("cachetype", cacheType);
        args.put("url", CacheUrl);
        
        try
        {
        	int count = Database.FieldNotes.myDB.update("FieldNotes", args, "id=" + Id, null);
        	if (count > 0)
        		return;
        } catch (Exception exc)
        {
        	return;       
        }
    }
    	
}
