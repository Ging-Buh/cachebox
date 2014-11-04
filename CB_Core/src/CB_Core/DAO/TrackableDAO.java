package CB_Core.DAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import CB_Core.Tag;
import CB_Core.DB.Database;
import CB_Core.Types.Trackable;
import CB_Utils.DB.CoreCursor;
import CB_Utils.DB.Database_Core.Parameters;

import com.badlogic.gdx.Gdx;

public class TrackableDAO
{

	private Trackable ReadFromCursor(CoreCursor reader)
	{
		try
		{
			Trackable trackable = new Trackable(reader);

			return trackable;
		}
		catch (Exception exc)
		{
			Gdx.app.error(Tag.TAG, "Read Trackable", exc);
			return null;
		}
	}

	public void WriteToDatabase(Trackable trackable)
	{
		Parameters args = createArgs(trackable);

		try
		{
			Database.FieldNotes.insert("Trackable", args);
		}
		catch (Exception exc)
		{
			Gdx.app.error(Tag.TAG, "Write Trackable", exc);

		}
	}

	public void UpdateDatabase(Trackable trackable)
	{
		Parameters args = createArgs(trackable);

		try
		{
			Database.FieldNotes.update("Trackable", args, "GcCode='" + trackable.getGcCode() + "'", null);
		}
		catch (Exception exc)
		{
			Gdx.app.error(Tag.TAG, "Ubdate Trackable", exc);

		}

	}

	private Parameters createArgs(Trackable trackable)
	{
		String stimestampCreated = "";
		String stimestampLastVisit = "";

		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			stimestampCreated = iso8601Format.format(trackable.getDateCreated());
		}
		catch (Exception e)
		{
			Gdx.app.error(Tag.TAG, "", e);
		}

		try
		{
			stimestampLastVisit = iso8601Format.format(trackable.getLastVisit());
		}
		catch (Exception e)
		{
			Gdx.app.error(Tag.TAG, "", e);
		}

		Parameters args = new Parameters();
		args.put("Archived", trackable.getArchived() ? 1 : 0);
		args.put("GcCode", trackable.getGcCode());
		args.put("CacheID", trackable.getCurrentGeocacheCode());
		args.put("CurrentGoal", trackable.getCurrentGoal());
		args.put("CurrentOwnerName", trackable.getCurrentOwner());
		args.put("DateCreated", stimestampCreated);
		args.put("Description", trackable.getDescription());
		args.put("IconUrl", trackable.getIconUrl());
		args.put("ImageUrl", trackable.getImageUrl());
		args.put("name", trackable.getName());
		args.put("OwnerName", trackable.getOwner());
		args.put("Url", trackable.getUrl());
		args.put("TypeName", trackable.getTypeName());
		args.put("Url", trackable.getUrl());
		args.put("LastVisit", stimestampLastVisit);
		args.put("Home", trackable.getHome());
		args.put("TravelDistance", trackable.getTravelDistance());
		return args;
	}

	public Trackable getFromDbByGcCode(String GcCode)
	{
		String where = "GcCode = \"" + GcCode + "\"";
		String query = "select Id ,Archived ,GcCode ,CacheId ,CurrentGoal ,CurrentOwnerName ,DateCreated ,Description ,IconUrl ,ImageUrl ,Name ,OwnerName ,Url,TypeName, Home,TravelDistance   from Trackable WHERE " + where;
		CoreCursor reader = Database.FieldNotes.rawQuery(query, null);

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
			Gdx.app.error(Tag.TAG, "", e);
			return null;
		}

	}

}
