package de.droidcachebox.Events;

import android.view.Menu;
import android.view.MenuItem;

public interface ViewOptionsMenu  {

	public boolean ItemSelected(MenuItem item);
	public void BeforeShowMenu(Menu menu);
	public void OnShow();
	public void OnHide();
}
