package de.cachebox_test.Views;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import de.cachebox_test.R;
import de.cachebox_test.Events.ViewOptionsMenu;

public class NotesView extends FrameLayout implements ViewOptionsMenu
{
	Context context;
	EditText edNotes;
	Cache aktCache;
	boolean mustLoadNotes;

	public NotesView(Context context, LayoutInflater inflater)
	{
		super(context);
		mustLoadNotes = false;

		RelativeLayout notesLayout = (RelativeLayout) inflater.inflate(Config.settings.nightMode.getValue() ? R.layout.night_notesview
				: R.layout.notesview, null, false);
		this.addView(notesLayout);
		edNotes = (EditText) findViewById(R.id.notesText);
		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
	}

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (aktCache != cache)
		{
			mustLoadNotes = true;
			aktCache = cache;
		}
	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
	}

	@Override
	public void OnShow()
	{
		if (mustLoadNotes)
		{
			edNotes.setText(Database.GetNote(aktCache));
			mustLoadNotes = false;
		}

	}

	@Override
	public void OnHide()
	{
		// Save changed Note text
		try
		{
			Database.SetNote(aktCache, edNotes.getText().toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void OnFree()
	{
		try
		{
			Database.SetNote(aktCache, edNotes.getText().toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		aktCache = null;
		context = null;
		edNotes = null;

	}

	@Override
	public int GetMenuId()
	{
		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
	}

	@Override
	public int GetContextMenuId()
	{
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu)
	{
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item)
	{
		return false;
	}

}
