package de.droidcachebox.Views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.CacheList;
import de.droidcachebox.Geocaching.FieldNoteEntry;
import de.droidcachebox.Geocaching.FieldNoteList;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Views.CacheListView.CustomAdapter;
import de.droidcachebox.Views.Forms.EditFieldNote;
import de.droidcachebox.Views.Forms.EditWaypoint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Path.FillType;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FieldNotesView extends ListView implements SelectedCacheEvent, ViewOptionsMenu {
	public static FieldNoteEntry aktFieldNote;
	private int aktFieldNoteIndex = -1;
	Activity parentActivity;
	FieldNoteList lFieldNotes;
	CustomAdapter lvAdapter;
	
	public FieldNotesView(Context context, final Activity parentActivity) {
		super(context);
		this.parentActivity = parentActivity;

		lFieldNotes = new FieldNoteList();

		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), lFieldNotes);
		this.setAdapter(lvAdapter);
//		this.setLongClickable(true);
		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				aktFieldNote = lFieldNotes.get(arg2);
				aktFieldNoteIndex = arg2;
        		invalidate();
				return;
			}
		});

		this.setBackgroundColor(Global.getColor(R.attr.EmptyBackground));
		this.setCacheColorHint(R.color.Day_TitleBarColor);
		this.setDividerHeight(5);
		this.setDivider(getBackground());
		
		
	}

 	static public int windowW=0;
    static public int windowH=0 ;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
    {
    // we overriding onMeasure because this is where the application gets its right size.
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	windowW = getMeasuredWidth();
    	windowH = getMeasuredHeight();
    }

    public class CustomAdapter extends BaseAdapter {
		 
	    private Context context;
	    private FieldNoteList fieldNoteList;
	 
	    public CustomAdapter(Context context, FieldNoteList fieldNoteList ) {
	        this.context = context;
	        this.fieldNoteList = fieldNoteList;
	    }
	 
	    public int getCount() {
	        return fieldNoteList.size();
	    }
	 
	    public Object getItem(int position) {
	        return fieldNoteList.get(position);
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	        FieldNoteEntry fne = fieldNoteList.get(position);
	        Boolean BackGroundChanger = ((position % 2) == 1);
	        FieldNoteViewItem v = new FieldNoteViewItem(context, fne, BackGroundChanger);
	 
	        return v;
	    }
	}
	
	
	
	
	
	@Override
	public boolean ItemSelected(MenuItem item) {
		Global.AddLog("Hallo Log");
		switch (item.getItemId())
		{
			case R.id.fieldnotesview_found:
				addNewFieldnote(1);
				return true;
			case R.id.fieldnotesview_notfound:
				addNewFieldnote(2);
				return true;
			case R.id.fieldnotesview_maintenance:
				addNewFieldnote(3);
				return true;
			case R.id.fieldnotesview_addnote:
				addNewFieldnote(4);
				return true;
		}
		return false;
	}

	private void addNewFieldnote(int type) {
		Cache cache = Global.SelectedCache();

		FieldNoteEntry newFieldNote = new FieldNoteEntry(type);
		newFieldNote.CacheName = cache.Name;
		newFieldNote.gcCode = cache.GcCode;
		newFieldNote.foundNumber = Config.GetInt("FoundOffset");
		newFieldNote.timestamp = new Date();
		newFieldNote.CacheId = cache.Id;
		newFieldNote.comment = "";
		newFieldNote.CacheUrl = cache.Url;
		newFieldNote.fillType();

		FieldNoteList fnl = new FieldNoteList();
		fnl.LoadFieldNotes("CacheId=" + cache.Id + " and Type=" + type);
		if (fnl.size() > 0)
		{
			// für diesen Cache ist bereits eine FieldNote vom typ vorhanden 
			// -> diese ändern und keine neue erstellen
			FieldNoteEntry nfne = fnl.get(0);
			int index = 0;
			for (FieldNoteEntry nfne2 : lFieldNotes)
			{
				if (nfne2.Id == nfne.Id)
				{
					newFieldNote = nfne;
					aktFieldNote = nfne;
					aktFieldNoteIndex = index;
				}
				index++;
			}
		}
		
		
		switch (type)
		{
		case 1:
			if (!cache.Found())
				newFieldNote.foundNumber++;	//
			newFieldNote.fillType();
			if (newFieldNote.comment.equals(""))
				newFieldNote.comment = ReplaceTemplate(Config.GetString("FoundTemplate"), newFieldNote);
			// wenn eine FieldNote Found erzeugt werden soll und der Cache noch nicht gefunden war -> foundNumber um 1 erhöhen
			break;
		case 2:
			if (newFieldNote.comment.equals(""))
				newFieldNote.comment = ReplaceTemplate(Config.GetString("DNFTemplate"), aktFieldNote);
			break;
		case 3:
			if (newFieldNote.comment.equals(""))
				newFieldNote.comment = ReplaceTemplate(Config.GetString("NeedsMaintenanceTemplate"), aktFieldNote);
			break;
		case 4:
			if (newFieldNote.comment.equals(""))
				newFieldNote.comment = ReplaceTemplate(Config.GetString("AddNoteTemplate"), aktFieldNote);
			break;
		}
		
		Intent mainIntent = new Intent().setClass(getContext(), EditFieldNote.class);
        Bundle b = new Bundle();
        b.putSerializable("FieldNote", newFieldNote);
        mainIntent.putExtras(b);
        
		parentActivity.startActivityForResult(mainIntent, 0);
		
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return R.menu.menu_fieldnotesview;
	}

	@Override
	public void OnShow() {
		// TODO Auto-generated method stub
		if (lFieldNotes.size() == 0)
			lFieldNotes.LoadFieldNotes("");
	}

	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{																	   	
			FieldNoteEntry fieldNote = (FieldNoteEntry)bundle.getSerializable("FieldNoteResult");
			if (fieldNote != null)
			{
				if ((aktFieldNote != null) && (fieldNote.Id == aktFieldNote.Id))
				{
					// Änderungen in aktFieldNote übernehmen
					lFieldNotes.remove(aktFieldNoteIndex);
					aktFieldNote = fieldNote;
					lFieldNotes.add(aktFieldNoteIndex, aktFieldNote);
					aktFieldNote.UpdateDatabase();

					FieldNoteList.CreateVisitsTxt();
				} else
				{
					// neue FieldNote
					lFieldNotes.add(0, fieldNote);
					fieldNote.WriteToDatabase();
					aktFieldNote = fieldNote;
					
					Global.SelectedCache().Found(true);
	                Config.Set("FoundOffset", aktFieldNote.foundNumber);
	                Config.AcceptChanges();
					
					FieldNoteList.CreateVisitsTxt();
				}
				lvAdapter.notifyDataSetChanged();
			}
		}
		
	}
	
	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		// TODO Auto-generated method stub
	}

	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return R.menu.cmenu_fieldnotesview;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem mi = menu.findItem(R.id.c_fnv_edit);
		if (mi !=null)
			mi.setEnabled(aktFieldNote != null);
		mi = menu.findItem(R.id.c_fnv_delete);
		if (mi != null)
			mi.setEnabled(aktFieldNote != null);
		mi = menu.findItem(R.id.c_fnv_selectcache);
		if (mi != null)
			mi.setEnabled((aktFieldNote != null) && (Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode) != null));
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.c_fnv_edit:
			editFieldNote();
			return true;
		case R.id.c_fnv_delete:
			deleteFieldNote();
			return true;
		case R.id.c_fnv_selectcache:
			selectCacheFromFieldNote();
			return true;
		}
		return false;
	}

    private String ReplaceTemplate(String template, FieldNoteEntry fieldNote)
    {
        DateFormat iso8601Format = new SimpleDateFormat("HH:mm");
        String stime = iso8601Format.format(fieldNote.timestamp);
        iso8601Format = new SimpleDateFormat("dd-MM-yyyy");
        String sdate = iso8601Format.format(fieldNote.timestamp);

    	template = template.replace("<br>", "\n");
        template = template.replace("##finds##", String.valueOf(fieldNote.foundNumber));
        template = template.replace("##date##", sdate);
        template = template.replace("##time##", stime);
        template = template.replace("##owner##", Global.SelectedCache().Owner);
        template = template.replace("##gcusername##", Config.GetString("GcLogin"));
//        template = template.replace("##gcvote##", comboBoxVote.SelectedIndex.ToString());
        return template;
    }

    private void editFieldNote()
    {
		Intent mainIntent = new Intent().setClass(getContext(), EditFieldNote.class);
        Bundle b = new Bundle();
        b.putSerializable("FieldNote", aktFieldNote);
        mainIntent.putExtras(b);
		parentActivity.startActivityForResult(mainIntent, 0);		    	
    }
    
    private void deleteFieldNote()
    {
		// aktuell selectierte FieldNote löschen
		if (aktFieldNote == null)
			return;
		Cache cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);    	
    
    
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            // Yes button clicked
		        	// delete aktFieldNote
		        	aktFieldNote.DeleteFromDatabase();

		        	lFieldNotes.remove(aktFieldNote);
					aktFieldNote = null;
					aktFieldNoteIndex = -1;
					
					lvAdapter.notifyDataSetChanged();
		            break;
		        case DialogInterface.BUTTON_NEGATIVE:
		            // No button clicked
		        	// do nothing
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
		String message = "Soll die FieldNote\n\n[" + aktFieldNote.typeString + "]\n\ndes Caches" + "\n\n[" + aktFieldNote.CacheName + "]\n\n gelöscht werden?";
		if (aktFieldNote.type == 1)
			message += "\n\nDer Found Status des Caches wird dabei zurückgesetzt!";
		builder.setMessage(message)
			.setTitle("Delete Fieldnote")
			.setPositiveButton(Global.Translations.Get("yes"), dialogClickListener)
		    .setNegativeButton(Global.Translations.Get("no"), dialogClickListener).show();
    }
    
    private void selectCacheFromFieldNote()
    {
		if (aktFieldNote == null)
			return;
		Cache cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
		if (cache != null)
			Global.SelectedCache(cache);    	
    }
}


