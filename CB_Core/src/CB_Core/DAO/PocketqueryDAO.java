package CB_Core.DAO;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import CB_Core.Tag;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.DB.Database;
import CB_Utils.DB.CoreCursor;
import CB_Utils.DB.Database_Core.Parameters;

import com.badlogic.gdx.Gdx;

public class PocketqueryDAO
{
	public int writeToDatabase(PQ pq)
	{
		Parameters args = new Parameters();
		args.put("PQName", pq.Name);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(pq.DateLastGenerated);
		args.put("CreationTimeOfPQ", stimestamp);

		try
		{
			Database.Data.insertWithConflictReplace("PocketQueries", args);
		}
		catch (Exception exc)
		{
			Gdx.app.error(Tag.TAG, "Write Pocketquery to DB" + pq.Name, exc);
			return -1;
		}

		return 0;
	}

	/**
	 * liefert das Datum wann die PQ mit dem gegebenen Namen das letzt mal erzeugt wurde Wenn eine PQ noch gar nicht in der Liste ist dann
	 * wird null zurück gegeben
	 * 
	 * @param pqName
	 * @return
	 */
	public Date getLastGeneratedDate(String pqName)
	{
		CoreCursor reader = Database.Data.rawQuery("select max(CreationTimeOfPQ) from PocketQueries where PQName=@PQName", new String[]
			{ pqName });
		try
		{
			if (reader.getCount() > 0)
			{
				reader.moveToFirst();
				while (reader.isAfterLast() == false)
				{
					String sDate = reader.getString(0);
					if (sDate == null)
					{
						// nicht gefunden!
						return null;
					}
					DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try
					{
						return iso8601Format.parse(sDate);
					}
					catch (ParseException e)
					{
						// PQ ist in der DB, aber das Datum konnte nicht geparst werden
						Gdx.app.error(Tag.TAG, "", e);
						return new Date(0);
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			reader.close();
		}
		return null;
	}
}
