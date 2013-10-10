package CB_Core.DAO;

import java.util.Iterator;

import CB_Core.DB.Database;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;
import CB_Utils.DB.CoreCursor;

public class TrackableListDAO
{

	public static void WriteToDatabase(TbList trackableList)
	{

		TrackableDAO tDAO = new TrackableDAO();

		Iterator<Trackable> iterator = trackableList.iterator();

		if (iterator != null && iterator.hasNext())
		{
			do
			{
				Trackable tb = iterator.next();

				Trackable tbDB = tDAO.getFromDbByGcCode(tb.getGcCode());

				if (tbDB == null)
				{
					tDAO.WriteToDatabase(tb);
				}
				else
				{
					tDAO.UpdateDatabase(tb);
				}

			}
			while (iterator.hasNext());
		}

	}

	public static TbList ReadTbList(String where)
	{
		TbList trackableList = new TbList();
		CoreCursor reader = Database.FieldNotes
				.rawQuery(
						"select Id ,Archived ,GcCode ,CacheId ,CurrentGoal ,CurrentOwnerName ,DateCreated ,Description ,IconUrl ,ImageUrl ,Name ,OwnerName ,Url,TypeName, Home,TravelDistance   from Trackable",
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

	/**
	 * Deleate all TBs
	 */
	public static void clearDB()
	{
		Database.FieldNotes.delete("Trackable", "", null);
	}

}
