package de.droidcachebox.Views;

import CB_Core.CacheListChangedEventList;
import CB_Core.CacheListChangedEventListener;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_Utils.Log.Log;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Custom_Controls.DescriptionViewControl;
import de.droidcachebox.Events.ViewOptionsMenu;
import org.slf4j.LoggerFactory;

public class DescriptionView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(DescriptionView.class);
    private long aktCacheID;
    private static DescriptionViewControl mDescriptionViewControl;
    private LinearLayout mLinearLayout;

    public DescriptionView(Context context, LayoutInflater inflater) {
        super(context);
        SelectedCacheEventList.Add(this);
        RelativeLayout descriptionLayout = (RelativeLayout) inflater.inflate(R.layout.description_view, null, false);
        this.addView(descriptionLayout);
        mLinearLayout = (LinearLayout) findViewById(R.id.WebViewLayout);
        mDescriptionViewControl = (DescriptionViewControl) findViewById(R.id.DescriptionViewControl);
        SelectedCacheChanged(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
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
    public void ActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public int GetContextMenuId() {
        return 0;
    }

    @Override
    public void BeforeShowContextMenu(Menu menu) {
    }

    @Override
    public boolean ContextMenuItemSelected(MenuItem item) {
        return false;
    }

    /**
     *
     * @param selectedCache
     * @param waypoint
     */
    @Override
    public void SelectedCacheChanged(Cache selectedCache, Waypoint waypoint) {
        if (selectedCache == null || mDescriptionViewControl == null)
            return;
        if (aktCacheID != selectedCache.Id) {
            aktCacheID = selectedCache.Id;
            mDescriptionViewControl.setCache(selectedCache);
        }
    }
}
