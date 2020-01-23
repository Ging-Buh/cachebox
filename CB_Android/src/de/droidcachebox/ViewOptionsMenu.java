package de.droidcachebox;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public interface ViewOptionsMenu {

    boolean itemSelected(MenuItem item);

    void beforeShowMenu(Menu menu);

    int getMenuId();

    void onShow();

    void onHide();

    void onFree();

    int getContextMenuId();

    boolean contextMenuItemSelected(MenuItem item);

    boolean dispatchTouchEvent(MotionEvent event);

}
