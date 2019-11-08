package de.droidcachebox.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import de.droidcachebox.controls.DescriptionViewControl;
import de.droidcachebox.R;
import de.droidcachebox.SelectedCacheChangedEventListener;
import de.droidcachebox.ViewOptionsMenu;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Waypoint;
//import org.slf4j.LoggerFactory;

public class DescriptionView extends FrameLayout implements ViewOptionsMenu, SelectedCacheChangedEventListener {
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
        // SelectedCacheChanged(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
        // mDescriptionViewControl.setCache(GlobalCore.getSelectedCache());
    }

    @Override
    public boolean ItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void BeforeShowMenu(Menu menu) {
    }

    @Override
    public void OnShow() {
        this.forceLayout();

        mDescriptionViewControl.OnShow();

        mLinearLayout.setWillNotDraw(false);
        mLinearLayout.invalidate();

        mDescriptionViewControl.setWillNotDraw(false);
        mDescriptionViewControl.invalidate();

        mDescriptionViewControl.getSettings().setBuiltInZoomControls(true);
    }

    @Override
    public void OnHide() {
    }

    @Override
    public void OnFree() {
        if (mDescriptionViewControl != null)
            mDescriptionViewControl.OnFree();
    }

    @Override
    public int GetMenuId() {
        return 0;
    }

    @Override
    public int GetContextMenuId() {
        return 0;
    }

    @Override
    public boolean ContextMenuItemSelected(MenuItem item) {
        return false;
    }

    /**
     * @param selectedCache
     * @param waypoint
     */
    @Override
    public void selectedCacheChanged(Cache selectedCache, Waypoint waypoint) {
        if (selectedCache == null || mDescriptionViewControl == null)
            return;
        if (aktCacheID != selectedCache.Id) {
            aktCacheID = selectedCache.Id;
            mDescriptionViewControl.setCache(selectedCache);
        }
    }
}
