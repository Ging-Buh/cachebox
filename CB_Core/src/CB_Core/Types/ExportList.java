package CB_Core.Types;

import java.util.ArrayList;

import CB_Core.Tag;
import CB_Core.DB.Database;
import CB_Utils.DB.CoreCursor;

import com.badlogic.gdx.Gdx;

public class ExportList extends ArrayList<ExportEntry>
{
	private static final long serialVersionUID = -7774973724185994203L;

	public ExportList()
	{

	}

	public void loadExportList()
	{
		clear();
		String sql = "select Replication.Id, Replication.ChangeType, Replication.CacheId, Replication.WpGcCode, Replication.SolverCheckSum, Replication.NotesCheckSum, Replication.WpCoordCheckSum, Caches.Name from Replication INNER JOIN Caches ON Replication.CacheId = Caches.Id";

		CoreCursor reader = null;
		try
		{
			reader = Database.Data.rawQuery(sql, null);
		}
		catch (Exception exc)
		{
			Gdx.app.error(Tag.TAG, "ExportList LoadExportList", exc);
		}
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			ExportEntry ee = new ExportEntry(reader);
			if (!this.contains(ee))
			{
				this.add(ee);
			}

			reader.moveToNext();
		}
		reader.close();

	}
}
