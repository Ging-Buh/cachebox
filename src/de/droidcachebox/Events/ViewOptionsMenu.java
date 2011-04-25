package de.droidcachebox.Events;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public interface ViewOptionsMenu  {

	public boolean ItemSelected(MenuItem item);
	public void BeforeShowMenu(Menu menu);
	public int GetMenuId();
	public void OnShow();
	public void OnHide();
	public void ActivityResult(int requestCode, int resultCode, Intent data);
}
