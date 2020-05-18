package de.droidcachebox.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.R;
import de.droidcachebox.ViewOptionsMenu;
import de.droidcachebox.controls.DescriptionViewControl;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Waypoint;

public class DescriptionView extends FrameLayout implements ViewOptionsMenu, CacheSelectionChangedListeners.CacheSelectionChangedListener {
    private static DescriptionViewControl mDescriptionViewControl;
    private long aktCacheID;
    private LinearLayout mLinearLayout;

    public DescriptionView(Context context, LayoutInflater inflater) {
        super(context);
        // SelectedCacheEventList.Add(this); dont need to create html every change. is enough at calling
        RelativeLayout descriptionLayout = (RelativeLayout) inflater.inflate(R.layout.description_view, null, false);
        this.addView(descriptionLayout);
        mLinearLayout = findViewById(R.id.WebViewLayout);
        mDescriptionViewControl = findViewById(R.id.DescriptionViewControl);
    }

    @Override
    public boolean itemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void beforeShowMenu(Menu menu) {
    }

    @Override
    public void onShow() {
        this.forceLayout();

        mDescriptionViewControl.onShow();

        mLinearLayout.setWillNotDraw(false);
        mLinearLayout.invalidate();

        mDescriptionViewControl.setWillNotDraw(false);
        mDescriptionViewControl.invalidate();

        mDescriptionViewControl.getSettings().setBuiltInZoomControls(true);
    }

    @Override
    public void onHide() {
    }

    @Override
    public void onFree() {
        if (mDescriptionViewControl != null)
            mDescriptionViewControl.onFree();
    }

    @Override
    public int getMenuId() {
        return 0;
    }

    @Override
    public int getContextMenuId() {
        return 0;
    }

    @Override
    public boolean contextMenuItemSelected(MenuItem item) {
        return false;
    }

    /**
     * @param selectedCache ?
     * @param waypoint ?
     */
    @Override
    public void handleCacheChanged(Cache selectedCache, Waypoint waypoint) {
        if (selectedCache == null || mDescriptionViewControl == null)
            return;
        if (aktCacheID != selectedCache.generatedId) {
            aktCacheID = selectedCache.generatedId;
            mDescriptionViewControl.setCache(selectedCache);
        }
    }
}
