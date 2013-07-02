package CB_Core.Types;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.Enums.LogTypes;
import CB_Core.Log.Logger;

public class FieldNoteList extends ArrayList<FieldNoteEntry>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FieldNoteList()
	{
	}

	public void LoadFieldNotes(String where)
	{
		LoadFieldNotes(where, "");
	}

	public void LoadFieldNotes(String where, String order)
	{
		this.clear();
		String sql = "select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog from FieldNotes";
		if (!where.equals("")) sql += " where " + where;
		if (order == "") sql += " order by FoundNumber DESC, Timestamp DESC";
		else
			sql += " order by " + order;
		CoreCursor reader = null;
		try
		{
			reader = Database.FieldNotes.rawQuery(sql, null);
		}
		catch (Exception exc)
		{
			Logger.Error("FieldNoteList", "LoadFieldNotes", exc);
		}
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			FieldNoteEntry fne = new FieldNoteEntry(reader);
			if (!this.contains(fne))
			{
				this.add(fne);
			}

			reader.moveToNext();
		}
		reader.close();
	}

	/**
	 * @param dirFileName
	 *            Config.settings.FieldNotesGarminPath.getValue()
	 */
	public static void CreateVisitsTxt(String dirFileName)
	{
		FieldNoteList lFieldNotes = new FieldNoteList();
		lFieldNotes.LoadFieldNotes("", "Timestamp ASC");

		File txtFile = new File(dirFileName);
		FileWriter writer;
		try
		{
			writer = new FileWriter(txtFile);

			for (FieldNoteEntry fieldNote : lFieldNotes)
			{
				String log = fieldNote.gcCode + "," + fieldNote.GetDateTimeString() + "," + fieldNote.type.toString() + ",\""
						+ fieldNote.comment + "\"\n";
				writer.write(log + "\n");

			}
			writer.flush();
			writer.close();
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}
		;
	}

	public void DeleteFieldNoteByCacheId(long cacheId, LogTypes type)
	{
		int foundNumber = 0;
		FieldNoteEntry fne = null;
		// löscht eine evtl. vorhandene FieldNote vom type für den Cache cacheId
		for (FieldNoteEntry fn : this)
		{
			if ((fn.CacheId == cacheId) && (fn.type == type))
			{
				fne = fn;
			}
		}
		if (fne != null)
		{
			if (fne.type == LogTypes.found) foundNumber = fne.foundNumber;
			this.remove(fne);
			fne.DeleteFromDatabase();
		}
		decreaseFoundNumber(foundNumber);
	}

	public void DeleteFieldNote(long id, LogTypes type)
	{
		int foundNumber = 0;
		FieldNoteEntry fne = null;
		// löscht eine evtl. vorhandene FieldNote vom type für den Cache cacheId
		for (FieldNoteEntry fn : this)
		{
			if (fn.Id == id)
			{
				fne = fn;
			}
		}
		if (fne != null)
		{
			if (fne.type == LogTypes.found) foundNumber = fne.foundNumber;
			this.remove(fne);
			fne.DeleteFromDatabase();
		}
		decreaseFoundNumber(foundNumber);
	}

	public void decreaseFoundNumber(int deletedFoundNumber)
	{
		if (deletedFoundNumber > 0)
		{
			// alle FoundNumbers anpassen, die größer sind
			for (FieldNoteEntry fn : this)
			{
				if ((fn.type == LogTypes.found) && (fn.foundNumber > deletedFoundNumber))
				{
					int oldFoundNumber = fn.foundNumber;
					fn.foundNumber--;
					String s = fn.comment;
					s = fn.comment.replaceAll("#" + oldFoundNumber, "#" + fn.foundNumber);
					fn.comment = s;
					fn.fillType();
					fn.UpdateDatabase();
				}
			}
		}
	}

	public boolean contains(FieldNoteEntry fne)
	{
		for (FieldNoteEntry item : this)
		{
			if (fne.equals(item)) return true;
		}
		return false;
	}
}
