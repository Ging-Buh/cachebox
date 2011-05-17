package de.droidcachebox.Geocaching;

import java.util.ArrayList;
import java.util.Date;

import android.database.Cursor;
import de.droidcachebox.Database;

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
		this.clear();
		String sql = "select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url from FieldNotes";
		if (!where.equals(""))
			sql += " where " + where;
		sql += " order by FoundNumber DESC, Timestamp DESC"; 
		Cursor reader = null;
		try
		{
        reader = Database.FieldNotes.myDB.rawQuery(sql, null);
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
}
