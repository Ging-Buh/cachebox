package de.droidcachebox.Events;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public interface ViewOptionsMenu {

    boolean ItemSelected(MenuItem item);

    void BeforeShowMenu(Menu menu);

    int GetMenuId();

    void OnShow();

    void OnHide();

    void OnFree();

    int GetContextMenuId();

    boolean ContextMenuItemSelected(MenuItem item);

    boolean dispatchTouchEvent(MotionEvent event);

}
