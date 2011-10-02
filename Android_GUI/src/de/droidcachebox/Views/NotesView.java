package de.droidcachebox.Views;

import CB_Core.DB.Database;
import de.droidcachebox.R;
import de.droidcachebox.main;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotesView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent {
	Context context;
	EditText edNotes;
	Cache aktCache;
	boolean mustLoadNotes;

	public NotesView(Context context, LayoutInflater inflater) {
		super(context);
		mustLoadNotes = false;
		SelectedCacheEventList.Add(this);

		RelativeLayout notesLayout = (RelativeLayout)inflater.inflate(main.N? R.layout.night_notesview : R.layout.notesview, null, false);
		this.addView(notesLayout);
        edNotes = (EditText) findViewById(R.id.notesText);
        
       
}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if (aktCache != cache)
		{
			mustLoadNotes = true;
			aktCache = cache;
		}
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnShow() {
		if (mustLoadNotes)
		{
			edNotes.setText(Database.GetNote(aktCache));
			mustLoadNotes = false;
		}
		
	}

	@Override
	public void OnHide() {
		// Save changed Note text
		try {
			Database.SetNote(aktCache,edNotes.getText().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void OnFree() {
		try {
			Database.SetNote(aktCache,edNotes.getText().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		aktCache = null;
	}
	
	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

}
