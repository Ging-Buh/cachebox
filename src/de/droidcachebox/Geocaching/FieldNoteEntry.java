package de.droidcachebox.Geocaching;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;

public class FieldNoteEntry {
	public int Id;
    public String gcCode;
    public Date timestamp;
    public String typeString;
    public int type;
    public int typeIcon;
    public int cacheType;
    public String comment;
    public int foundNumber;
    public String CacheName;

    public FieldNoteEntry(Cursor reader)
    {
    	Id = reader.getInt(8);
        gcCode = reader.getString(1).trim();
        
        String sDate = reader.getString(4);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
        	timestamp = iso8601Format.parse(sDate);
        } catch (ParseException e) {
		}
        
        type = reader.getInt(5);
        cacheType = reader.getInt(3);
        comment = reader.getString(7);
        foundNumber = reader.getInt(6);
        CacheName = reader.getString(2);

        if (type == 1)
        {
            typeString = "#" + foundNumber + " - Found it!";
            typeIcon = 0;
            if (cacheType == 5 || cacheType == 6 || cacheType == 7)
                typeString = "Attended";
            if (cacheType == 3)
                typeString = "Webcam Photo Taken";
        }

        if (type == 2)
        {
            typeString = "Did not find!";
            typeIcon = 1;
        }

        if (type == 3)
        {
            typeString = "Needs Maintenance";
            typeIcon = 5;
        }

        if (type == 4)
        {
            typeString = "Write Note";
            typeIcon = 2;
        }
    
    }

}
