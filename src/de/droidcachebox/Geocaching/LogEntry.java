package de.droidcachebox.Geocaching;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;

public class LogEntry {
    /// <summary>
    /// Benutzername des Loggers
    /// </summary>
    public String Finder = "";

    /// <summary>
    /// Logtyp, z.B. "Found it!"
    /// </summary>
    public short Type = -1;

    /// <summary>
    /// Index des zu verwendenden Bildchens
    /// </summary>
    public int TypeIcon = -1;

    /// <summary>
    /// Geschriebener Text
    /// </summary>
    public String Comment = "";

    /// <summary>
    /// Zeitpunkt
    /// </summary>
    public Date Timestamp = new Date(0);

    /// <summary>
    /// Id des Caches
    /// </summary>
    public long CacheId = -1;

    /// <summary>
    /// Id des Logs
    /// </summary>
    public long Id = -1;

    public LogEntry(Cursor reader, boolean filterBbCode)
    {
      CacheId = reader.getLong(0);
      
      String sDate = reader.getString(1);
      DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      try {
      	Timestamp = iso8601Format.parse(sDate);
      } catch (ParseException e) {
      }
      Finder = reader.getString(2);
      TypeIcon = reader.getInt(3);
      Comment = reader.getString(4);
      Id = reader.getLong(5);

      if (filterBbCode)
      {
        int lIndex;

        while ((lIndex = Comment.indexOf('[')) >= 0)
        {
          int rIndex = Comment.indexOf(']', lIndex);

          if (rIndex == -1)
            break;

          Comment = Comment.substring(0, lIndex) + Comment.substring(rIndex + 1);
        }
      }
    }

}
