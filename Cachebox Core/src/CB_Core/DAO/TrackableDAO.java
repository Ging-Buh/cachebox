package CB_Core.DAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.DB.Database.Parameters;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Trackable;

public class TrackableDAO
{

	public Trackable ReadFromCursor(CoreCursor reader)
	{
		try
		{
			Trackable trackable = new Trackable(reader);

			return trackable;
		}
		catch (Exception exc)
		{
			Logger.Error("Read Trackable", "", exc);
			return null;
		}
	}

	public void WriteToDatabase(Trackable trackable)
	{
		// int newCheckSum = createCheckSum(WP);
		// Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
		Parameters args = new Parameters();

		args.put("GcCode", trackable.getGcCode());

		args.put("name", trackable.getName());

		args.put("Archived", trackable.getArchived() ? 1 : 0);

		args.put("CurrentOwnerName", trackable.getCurrentOwner());
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(trackable.getDateCreated());
		args.put("DateCreated", stimestamp);
		args.put("Url", trackable.getUrl());

		try
		{
			long Test = Database.Data.insert("Trackable", args);

			int t = 5;

		}
		catch (Exception exc)
		{
			Logger.Error("Write Trackable", "", exc);

		}
	}

	public void UpdateDatabase(Trackable trackable)
	{

		Parameters args = new Parameters();

		args.put("GcCode", trackable.getGcCode());

		args.put("name", trackable.getName());

		args.put("Archived", trackable.getArchived() ? 1 : 0);

		args.put("CurrentOwnerName", trackable.getCurrentOwner());
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(trackable.getDateCreated());
		args.put("DateCreated", stimestamp);
		args.put("Url", trackable.getUrl());

		try
		{
			long Test = Database.Data.update("Trackable", args, "GcCode=" + trackable.getGcCode(), null);

			int t = 5;

		}
		catch (Exception exc)
		{
			Logger.Error("Ubdate Trackable", "", exc);

		}

	}

	public Trackable getFromDbByGcCode(String GcCode)
	{
		String where = "GcCode = \"" + GcCode + "\"";
		String query = "select Id ,Archived ,GcCode ,CacheId ,CurrentGoal ,CurrentOwnerName ,DateCreated ,Description ,IconUrl ,ImageUrl ,Name ,OwnerName ,Url   from Trackable WHERE "
				+ where;
		CoreCursor reader = Database.Data.rawQuery(query, null);

		try
		{
			if (reader != null && reader.getCount() > 0)
			{
				reader.moveToFirst();
				Trackable ret = ReadFromCursor(reader);

				reader.close();
				return ret;
			}
			else
			{
				if (reader != null) reader.close();
				return null;
			}
		}
		catch (Exception e)
		{
			if (reader != null) reader.close();
			e.printStackTrace();
			return null;
		}

	}

}
