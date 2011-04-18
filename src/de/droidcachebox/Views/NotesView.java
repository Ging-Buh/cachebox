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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotesView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent {
	Context context;
	TextView tvNotes;
	Cache aktCache;

	public NotesView(Context context, LayoutInflater inflater) {
		super(context);

		SelectedCacheEventList.Add(this);

		RelativeLayout notesLayout = (RelativeLayout)inflater.inflate(R.layout.notesview, null, false);
		this.addView(notesLayout);
        tvNotes = (TextView) findViewById(R.id.notesText);
        tvNotes.setTextColor(Color.BLACK);
}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		aktCache = cache;
		tvNotes.setText(cache.Note());
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

}
