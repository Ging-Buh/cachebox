package de.droidcachebox.Views;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import CB_Core.Config;
import CB_Core.GlobalCore;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.DAO.CacheDAO;
import de.droidcachebox.DAO.CacheListDAO;
import CB_Core.Events.SelectedCacheEvent;
import de.droidcachebox.Events.ViewOptionsMenu;

import CB_Core.Types.CacheList;
import de.droidcachebox.Geocaching.FieldNoteEntry;
import de.droidcachebox.Geocaching.FieldNoteList;

import de.droidcachebox.Views.CacheListView.CustomAdapter;
import de.droidcachebox.Views.Forms.EditFieldNote;
import de.droidcachebox.Views.Forms.EditWaypoint;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.ProgressDialog;
import CB_Core.FileIO;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Path.FillType;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FieldNotesView extends ListView implements  ViewOptionsMenu {
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

		ActivityUtils.setListViewPropertys(this);
		
		
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
			case R.id.fieldnotesview_upload:
				UploadFieldnotes();
				return true;
			case R.id.fieldnotesview_deleteall:
				deleteAllFieldNote();
				return true;
		}
		return false;
	}
	
	
	
	private void UploadFieldnotes()
    {
        String name = Config.GetString("DatabasePath");
        File file = new File(name);
        name = file.getName().replace(".db3", "").replace(" ", "");
        String dirFileName = Config.GetString("FieldNotesGarminPath");
        file = new File(dirFileName);
        String fileName = file.getName();
        dirFileName = file.getParent();
        String path = dirFileName +"/" + fileName;

        if (FileIO.FileExists(path))
        {
            MessageBox.Show(Global.Translations.Get("uploadFieldNotes?"), 
            		Global.Translations.Get("uploadFieldNotes"), 
            		MessageBoxButtons.YesNo, 
            		MessageBoxIcon.Question, 
            		UploadFieldnotesDialogListner);
            
        }
        else
        {
        	MessageBox.Show(Global.Translations.Get("NoFindsLogged"), 
        			Global.Translations.Get("thisNotWork"),
        			MessageBoxButtons.OK,
        			MessageBoxIcon.Information,null);
        }
            

    }
	
	private final  DialogInterface.OnClickListener  UploadFieldnotesDialogListner = new  DialogInterface.OnClickListener() 
	   { @Override public void onClick(DialogInterface dialog, int button) 
			{
				switch (button)
				{
					case -1:
						UploadFieldNotes();
						break;
					case -2:
						
						break;
								}
				
				dialog.dismiss();
			}
			
	    };
	    
	
	
	private  void UploadFieldNotes()
	{
		Thread UploadFieldNotesdThread = new Thread() 
		{
			public void run() 
			{
				ProgresssChangedEventList.Call("Was tue ich hier eigentlich?","", 0);
				
				//Tue was zu tun ist
				for(int i = 0; i < 100; i++)
			    {
						//Progress status Melden
			        	ProgresssChangedEventList.Call("Thread Läuft" + String.valueOf(i), i);
			        	
			        	try 
			        	{
			        		if(ThreadCancel) // wenn im ProgressDialog Cancel gedrückt wurde.
			        			break;
			        		
							Thread.sleep(50);
						} 
			        	catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
			    }
				if(!ThreadCancel)
				{
					ProgressDialog.Ready(); 				//Dem Progress Dialog melden, dass der Thread fertig ist!
					ProgressHandler.post(ProgressReady);	// auf das Ende des Threads reagieren
				}
			}
		};
		
		
		// ProgressDialog Anzeigen und den Abarbeitungs Thread übergeben.
		ProgressDialog.Show("Upload FieldNotes", UploadFieldNotesdThread, ProgressCanceld);
				
	}
	
	private Boolean ThreadCancel = false; 
	final Runnable ProgressCanceld = new Runnable() 
    {
	    public void run() 
	    {
	    	ThreadCancel=true;
	    	MessageBox.Show("Progress abgebrochen!");
	    }
    };
    
    final Handler ProgressHandler = new Handler();
	final Runnable ProgressReady = new Runnable() 
    {
	    public void run() 
	    {
	    	String Br = String.format("%n");
	    	MessageBox.Show("Leider Funktioniert der Upload noch nicht."+Br+"Die Anzeige ist nur ein UI-Test!", "Schade", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
	    }
    };
    
    	
    
    
	
	

	private void addNewFieldnote(int type) {
		Cache cache = GlobalCore.SelectedCache();
		
		if(cache==null)
		{
			MessageBox.Show(Global.Translations.Get("NoCacheSelect"), Global.Translations.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
			return;
		}
		
		FieldNoteEntry newFieldNote = null;
		if ((type == 1) || (type == 2))
		{
			// nachsehen, ob für diesen Cache bereits eine FieldNote des Types angelegt wurde
			// und gegebenenfalls diese ändern und keine neue anlegen
			// gilt nur für Found It! und DNF. 
			// needMaintance oder Note können zusätzlich angelegt werden
			int index = 0;
			for (FieldNoteEntry nfne : lFieldNotes)
			{
				if ((nfne.CacheId == cache.Id) && (nfne.type == type)) 
				{
					newFieldNote = nfne;
					aktFieldNote = newFieldNote;
					aktFieldNoteIndex = index;
				}
				index++;
			}
		}
		
		if (newFieldNote == null)
		{
			newFieldNote = new FieldNoteEntry(type);
			newFieldNote.CacheName = cache.Name;
			newFieldNote.gcCode = cache.GcCode;
			newFieldNote.foundNumber = Config.GetInt("FoundOffset");
			newFieldNote.timestamp = new Date();
			newFieldNote.CacheId = cache.Id;
			newFieldNote.comment = "";
			newFieldNote.CacheUrl = cache.Url;
			newFieldNote.cacheType= cache.Type.ordinal();
			newFieldNote.fillType();
			aktFieldNoteIndex=-1;
			aktFieldNote=newFieldNote;
		}
		
		switch (type)
		{
		case 1:
			if (!cache.Found)
				newFieldNote.foundNumber++;	//
			newFieldNote.fillType();
			if (newFieldNote.comment.equals(""))
				newFieldNote.comment = ReplaceTemplate(Config.GetString("FoundTemplate"), newFieldNote);
			// wenn eine FieldNote Found erzeugt werden soll und der Cache noch nicht gefunden war -> foundNumber um 1 erhöhen
			break;
		case 2:
			if (newFieldNote.comment.equals(""))
				newFieldNote.comment = ReplaceTemplate(Config.GetString("DNFTemplate"), newFieldNote);
			break;
		case 3:
			if (newFieldNote.comment.equals(""))
				newFieldNote.comment = ReplaceTemplate(Config.GetString("NeedsMaintenanceTemplate"), newFieldNote);
			break;
		case 4:
			if (newFieldNote.comment.equals(""))
				newFieldNote.comment = ReplaceTemplate(Config.GetString("AddNoteTemplate"), newFieldNote);
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
	public int GetMenuId() 
	{
		
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
	public void OnFree() {
		
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
				if ((aktFieldNote != null) && (aktFieldNoteIndex !=-1))
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
					if (fieldNote.type == 1)
					{
						// Found it! -> Cache als gefunden markieren
						if (!GlobalCore.SelectedCache().Found)
						{
							GlobalCore.SelectedCache().Found = true;
							CacheDAO cacheDAO = new CacheDAO();
							cacheDAO.WriteToDatabase_Found(GlobalCore.SelectedCache());
			                Config.Set("FoundOffset", aktFieldNote.foundNumber);
			                Config.AcceptChanges();
						}
					} else if (fieldNote.type == 2)
					{
						// DidNotFound -> Cache als nicht gefunden markieren
						if (GlobalCore.SelectedCache().Found)
						{
							GlobalCore.SelectedCache().Found = false;
							CacheDAO cacheDAO = new CacheDAO();
							cacheDAO.WriteToDatabase_Found(GlobalCore.SelectedCache());
			                Config.Set("FoundOffset", Config.GetInt("FoundOffset") - 1);
			                Config.AcceptChanges();
						}
						// und eine evtl. vorhandene FieldNote FoundIt löschen
						lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.SelectedCache().Id, 1);
					}
					
					FieldNoteList.CreateVisitsTxt();
				}
				lvAdapter.notifyDataSetChanged();
			}
		}
		
	}
	
	
	
	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return R.menu.cmenu_fieldnotesview;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) 
	{
			
		MenuItem mi = menu.findItem(R.id.c_fnv_edit);
		if (mi !=null)
		{
			mi.setEnabled(aktFieldNote != null);
			mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					editFieldNote();
					return false;
				}
			});
		}	
		mi = menu.findItem(R.id.c_fnv_delete);
		if (mi != null)
		{
			mi.setEnabled(aktFieldNote != null);
			mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					deleteFieldNote();
					return false;
				}
			});
		}
			
		mi = menu.findItem(R.id.c_fnv_selectcache);
		if (mi != null)
		{
			mi.setEnabled((aktFieldNote != null) && (Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode) != null));
			mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					selectCacheFromFieldNote();
					return false;
				}
			});
		}
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		
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
        template = template.replace("##owner##", GlobalCore.SelectedCache().Owner);
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
//		final Cache cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
		
		// suche den Cache aus der DB. 
		// Nicht aus der aktuellen Query, da dieser herausgefiltert sein könnte
		CacheList lCaches = new CacheList();
		CacheListDAO cacheListDAO = new CacheListDAO();
		cacheListDAO.ReadCacheList(lCaches, "Id = " + aktFieldNote.CacheId);
		Cache tmpCache = null;
		if (lCaches.size() > 0)
			tmpCache = lCaches.get(0);
		final Cache cache = tmpCache;
	
		if (cache == null)
		{
			String message = "The Cache [" + aktFieldNote.CacheName + "] is not in the actual DB. \nThis FieldNote can not be deleted!";
			MessageBox.Show(message);
			return;
		}
    
		
    
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            // Yes button clicked
		        	// delete aktFieldNote
		        	if (cache != null)
		        	{
		        		if (cache.Found)
		        		{
		        			cache.Found=false;
		        			Database.WriteToDatabase(cache);
			                Config.Set("FoundOffset", Config.GetInt("FoundOffset") - 1);
			                Config.AcceptChanges();		        			
		        		}
		        	}
		        	lFieldNotes.DeleteFieldNote(aktFieldNote.Id, aktFieldNote.type);

		        	aktFieldNote = null;
					aktFieldNoteIndex = -1;
					
					lvAdapter.notifyDataSetChanged();
		            break;
		        case DialogInterface.BUTTON_NEGATIVE:
		            // No button clicked
		        	// do nothing
		            break;
		        }
		        dialog.dismiss();
		    }
		};

		
		String message = "Soll die FieldNote\n\n[" + aktFieldNote.typeString + "]\n\ndes Caches" + "\n\n[" + aktFieldNote.CacheName + "]\n\n gelöscht werden?";
		if (aktFieldNote.type == 1)
			message += "\n\nDer Found Status des Caches wird dabei zurückgesetzt!";
		
		MessageBox.Show(message, "Delete Fieldnote", MessageBoxButtons.YesNo, dialogClickListener);
		
	
    }
    
    
    private void deleteAllFieldNote()
    {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            // Yes button clicked
		        	// delete aktFieldNote
		        	for(FieldNoteEntry entry : lFieldNotes)
		        	{
			        	entry.DeleteFromDatabase();
		        
					}
		        	
		        	lFieldNotes.clear();
		        	aktFieldNote = null;
					aktFieldNoteIndex = -1;
					lvAdapter.notifyDataSetChanged();
		        	break;
		            
		        case DialogInterface.BUTTON_NEGATIVE:
		            // No button clicked
		        	// do nothing
		            break;
		        }
		        dialog.dismiss();

		    }
		};
		String message = "Sollen alle FieldNotes gelöscht werden?";
		MessageBox.Show(message, "Delete Fieldnote", MessageBoxButtons.YesNo, MessageBoxIcon.Warning , dialogClickListener);
		
	
    }
    
    
    private void selectCacheFromFieldNote()
    {
		if (aktFieldNote == null)
			return;
		Cache cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
		Waypoint finalWp = null;
		if (cache.HasFinalWaypoint())
			finalWp = cache.GetFinalWaypoint();
		if (cache != null)
			GlobalCore.SelectedWaypoint(cache, finalWp);    	
    }
}


