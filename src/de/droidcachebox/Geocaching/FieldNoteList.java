package de.droidcachebox.Geocaching;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.database.Cursor;
import de.droidcachebox.Config;
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
}
