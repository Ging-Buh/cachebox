package CB_Core.Types;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.DB.Database.Parameters;

public class FieldNoteEntry implements Serializable
{
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
	public boolean uploaded;

	public FieldNoteEntry(int type)
	{
		Id = -1;
		this.type = type;
		fillType();
	}

	public FieldNoteEntry(CoreCursor reader)
	{
		CacheId = reader.getLong(0);
		Id = reader.getLong(8);
		gcCode = reader.getString(1).trim();

		String sDate = reader.getString(4);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			timestamp = iso8601Format.parse(sDate);
		}
		catch (ParseException e)
		{
		}
		if (timestamp == null) timestamp = new Date();

		type = reader.getInt(5);
		cacheType = reader.getInt(3);
		comment = reader.getString(7);
		foundNumber = reader.getInt(6);
		CacheName = reader.getString(2);
		CacheUrl = reader.getString(9);
		uploaded = reader.getInt(10) != 0;

		fillType();

	}

	public void fillType()
	{
		if (type == 1)
		{
			typeString = "#" + foundNumber + " - Found it!";
			typeIcon = 0;
			if (cacheType == 5 || cacheType == 6 || cacheType == 7) typeString = "Attended";
			if (cacheType == 3) typeString = "Webcam Photo Taken";
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

	/**
	 * Unsere ID entspricht nicht der ID von GC beim Upload über die API. <br>
	 * Diese Funktion liefert die Korregierten Werte! <br>
	 * <br>
	 * Name | Unsere ID | zu sendende ID | Bemerkung <br>
	 * <br>
	 * FoundIt | 1 |2| <br>
	 * DNF | 2| 3| <br>
	 * Note | 4|4| <br>
	 * Need Archived | |7| Bei uns nicht vorgesehen Need Maintenance |3|45| <br>
	 * 
	 * @return
	 */
	public int getGcUploadId()
	{
		switch (type)
		{
		case 1:
			return 2;
		case 2:
			return 3;
		case 3:
			return 45;
		case 4:
			return 4;
		default:
			return 4;
		}
	}

	public String GetDateTimeString()
	{
		SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
		String sDate = datFormat.format(timestamp);
		datFormat = new SimpleDateFormat("HH:mm:ss");
		sDate += "T" + datFormat.format(timestamp) + "Z";
		return sDate;
	}

	public void WriteToDatabase()
	{
		Parameters args = new Parameters();
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
		args.put("Uploaded", uploaded);

		try
		{
			Database.FieldNotes.insert("Fieldnotes", args);
		}
		catch (Exception exc)
		{
			return;
		}
		// search FieldNote Id
		CoreCursor reader = Database.FieldNotes.rawQuery(
				"select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded from FieldNotes where GcCode='"
						+ gcCode + "' and type=" + type, null);
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			FieldNoteEntry fne = new FieldNoteEntry(reader);
			this.Id = fne.Id;
			reader.moveToNext();
		}
		reader.close();
	}

	public void UpdateDatabase()
	{
		if (timestamp == null) timestamp = new Date();
		Parameters args = new Parameters();
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
		args.put("Uploaded", uploaded);

		try
		{
			long count = Database.FieldNotes.update("FieldNotes", args, "id=" + Id, null);
			if (count > 0) return;
		}
		catch (Exception exc)
		{
			return;
		}
	}

	public void DeleteFromDatabase()
	{
		try
		{
			Database.FieldNotes.delete("FieldNotes", "id=" + Id, null);
		}
		catch (Exception exc)
		{
			return;
		}
	}

}
