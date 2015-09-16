package de.cachebox_test.Events;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public interface ViewOptionsMenu
{

	public boolean ItemSelected(MenuItem item);

	public void BeforeShowMenu(Menu menu);

	public int GetMenuId();

	public void OnShow();

	public void OnHide();

	public void OnFree();

	public void ActivityResult(int requestCode, int resultCode, Intent data);

	public int GetContextMenuId();

	public void BeforeShowContextMenu(Menu menu);

	public boolean ContextMenuItemSelected(MenuItem item);

	public boolean dispatchTouchEvent(MotionEvent event);

}
