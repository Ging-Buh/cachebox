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
	protected static String sqlReadTrackable = "select Id, GcCode, Latitude, Longitude, Name, Size, Difficulty, Terrain, Archived, Available, Found, Type, PlacedBy, Owner, DateHidden, Url, NumTravelbugs, GcId, Rating, Favorit, TourName, GpxFilename_ID, HasUserData, ListingChanged, CorrectedCoordinates, ApiStatus from Caches ";

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

	public void UpdateDatabase(Cache cache)
	{

		// Parameters args = new Parameters();
		//
		// // bei einem Update müssen nicht alle infos überschrieben werden
		//
		// // args.put("Id", cache.Id);
		// // args.put("GcCode", cache.GcCode);
		// // args.put("GcId", cache.GcId);
		// args.put("Latitude", cache.Pos.Latitude);
		// args.put("Longitude", cache.Pos.Longitude);
		// args.put("Name", cache.Name);
		// try
		// {
		// args.put("Size", cache.Size.ordinal());
		// }
		// catch (Exception e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// args.put("Difficulty", (int) (cache.Difficulty * 2));
		// args.put("Terrain", (int) (cache.Terrain * 2));
		// args.put("Archived", cache.Archived ? 1 : 0);
		// args.put("Available", cache.Available ? 1 : 0);
		// args.put("Found", cache.Found);
		// args.put("Type", cache.Type.ordinal());
		// args.put("PlacedBy", cache.PlacedBy);
		// args.put("Owner", cache.Owner);
		// DateFormat iso8601Format = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// String stimestamp = iso8601Format.format(cache.DateHidden);
		// args.put("DateHidden", stimestamp);
		// args.put("Hint", cache.hint);
		// if ((cache.longDescription != null) && (cache.longDescription != ""))
		// args
		// .put("Description", cache.longDescription);
		// cache.longDescription = ""; // clear longDescription because this
		// will
		// // be loaded from database when used
		// args.put("Url", cache.Url);
		// args.put("NumTravelbugs", cache.NumTravelbugs);
		// args.put("Rating", (int) (cache.Rating * 100));
		// // args.put("Vote", cache.);
		// // args.put("VotePending", cache.);
		// // args.put("Notes", );
		// // args.put("Solver", cache.);
		// args.put("AttributesPositive", cache.attributesPositive);
		// args.put("AttributesNegative", cache.attributesNegative);
		// // args.put("ListingCheckSum", cache.);
		// args.put("GPXFilename_Id", cache.GPXFilename_ID);
		// args.put("Favorit", cache.Favorit() ? 1 : 0);
		// args.put("ApiStatus", cache.ApiStatus);
		// args.put("CorrectedCoordinates", cache.CorrectedCoordinates ? 1 : 0);
		//
		// try
		// {
		// Database.Data.update("Caches", args, "Id=" + cache.Id, null);
		// }
		// catch (Exception exc)
		// {
		// Logger.Error("Ubdate Cache", "", exc);
		//
		// }
	}

}
