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
	
	public void LoadFieldNotes()
	{
		this.clear();
        Cursor reader = Database.FieldNotes.myDB.rawQuery("select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id from FieldNotes order by FoundNumber DESC, Timestamp DESC", null);
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
