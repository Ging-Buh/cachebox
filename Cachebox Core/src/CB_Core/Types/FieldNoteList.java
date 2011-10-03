package CB_Core.Types;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CB_Core.Config;
import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;

public class FieldNoteList extends ArrayList<FieldNoteEntry> {

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
		String sql = "select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url from FieldNotes";
		if (!where.equals(""))
			sql += " where " + where;
		if (order == "")
			sql += " order by FoundNumber DESC, Timestamp DESC";
		else
			sql += " order by " + order;
		CoreCursor reader = null;
		try
		{
			reader = Database.FieldNotes.rawQuery(sql, null);
		} catch (Exception exc)
		{
			String s = exc.getMessage();
		}
    	reader.moveToFirst();
        while(reader.isAfterLast() == false)
        {
            FieldNoteEntry fne = new FieldNoteEntry(reader);
            this.add(fne);
            reader.moveToNext();
        }
        reader.close();		
	}
	
	public static void CreateVisitsTxt()
	{
		FieldNoteList lFieldNotes = new FieldNoteList();
		lFieldNotes.LoadFieldNotes("", "Timestamp ASC");
		
        String[] types = new String[] { "", "Found it", "Didn't find it", "Needs Maintenance", "Write Note" };
        
        String dirFileName = Config.GetString("FieldNotesGarminPath");

	    File txtFile = new File(dirFileName);
	    FileWriter writer;
		try {
			writer = new FileWriter(txtFile);
	
			for (FieldNoteEntry fieldNote : lFieldNotes)
			{				
		        String log = fieldNote.gcCode + ","
	            	+ fieldNote.GetDateTimeString() + ","
	            	+ types[fieldNote.type] + ",\"" + fieldNote.comment + "\"\n";
		        writer.write(log + "\n");

			}
        	writer.flush();
        	writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
	
	public void DeleteFieldNoteByCacheId(long cacheId, int type)
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
			if (fne.type == 1)
				foundNumber = fne.foundNumber;
			this.remove(fne);
			fne.DeleteFromDatabase();
		}
		decreaseFoundNumber(foundNumber);
	}

	public void DeleteFieldNote(long id, int type)
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
			if (fne.type == 1)
				foundNumber = fne.foundNumber;
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
				if ((fn.type == 1) && (fn.foundNumber > deletedFoundNumber))
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
}
