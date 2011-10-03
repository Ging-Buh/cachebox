package CB_Core.DAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.MysterySolution;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;
import CB_Core.Types.Waypoint;

public class TrackableListDAO
{

	public static void WriteToDatabase(TbList trackableList)
	{

		TrackableDAO tDAO = new TrackableDAO();

		Iterator<Trackable> iterator = trackableList.iterator();
		do
		{
			tDAO.WriteToDatabase(iterator.next());
		}
		while (iterator.hasNext());

	}

	public static TbList ReadTbList(String where)
	{
		TbList trackableList = new TbList();
		CoreCursor reader = Database.Data
				.rawQuery(
						"select Id ,Archived ,GcCode ,CacheId ,CurrentGoal ,CurrentOwnerName ,DateCreated ,Description ,IconUrl ,ImageUrl ,Name ,OwnerName ,Url   from Trackable",
						null);
		reader.moveToFirst();

		while (reader.isAfterLast() == false)
		{
			trackableList.add(new Trackable(reader));
			reader.moveToNext();
		}
		reader.close();
		return trackableList;
	}

}
