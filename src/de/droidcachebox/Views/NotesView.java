package de.droidcachebox.Views;

import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import android.content.Context;
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

		RelativeLayout notesLayout = (RelativeLayout)inflater.inflate(R.layout.notesview, null, false);
		this.addView(notesLayout);
        edNotes = (EditText) findViewById(R.id.notesText);
        edNotes.setTextColor(Color.BLACK);
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
			edNotes.setText(aktCache.GetNote());
			mustLoadNotes = false;
		}
		
	}

	@Override
	public void OnHide() {
		// Save changed Note text
		aktCache.SetNote(edNotes.getText().toString());		
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
