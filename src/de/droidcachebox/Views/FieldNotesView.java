package de.droidcachebox.Views;

import java.util.Date;

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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FieldNotesView extends ListView implements SelectedCacheEvent, ViewOptionsMenu {

	public FieldNotesView(Context context) {
		super(context);

		FieldNoteList lFieldNotes = new FieldNoteList();
		lFieldNotes.LoadFieldNotes();

		this.setAdapter(null);
		CustomAdapter lvAdapter = new CustomAdapter(getContext(), lFieldNotes);
		this.setAdapter(lvAdapter);
//		this.setLongClickable(true);
		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
//        		Cache cache = Database.Data.Query.get(arg2);
//        		Global.SelectedCache(cache);
//        		invalidate();
				return;
			}
		});

		
		
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
				addNewFieldnote();
				return true;
		}
		return false;
	}

	private void addNewFieldnote() {
		Cache cache = Global.SelectedCache();

//        "select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id from FieldNotes order by FoundNumber DESC, Timestamp DESC"
        ContentValues args = new ContentValues();
        args.put("id", cache.Id);
        args.put("gccode", cache.GcCode);
        args.put("name", cache.Name);
        args.put("timestamp", new Date().toString());
        args.put("type", 1);
        args.put("foundnumber", 1);
        args.put("comment", "Test Test Test");
        try
        {
        	Database.FieldNotes.myDB.insert("Fieldnotes", null, args);
        } catch (Exception exc)
        {
        	return;       
        }
        
		
		
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
		
	}

	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		// TODO Auto-generated method stub
		
	}

}
