package de.cachebox_test.Views;

import CB_Core.GlobalCore;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Components.CacheDraw.DrawStyle;
import de.cachebox_test.Custom_Controls.CacheInfoControl;
import de.cachebox_test.Custom_Controls.DescriptionViewControl;
import de.cachebox_test.Events.ViewOptionsMenu;

public class DescriptionView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent
{
	Context context;
	public Cache aktCache;

	Button TestButton;
	public CacheInfoControl cacheInfo;
	public static DescriptionViewControl WebControl;
	public static LinearLayout webViewLayout;

	private Point lastScrollPos = new Point(0, 0);

	public DescriptionView(Context context, LayoutInflater inflater)
	{
		super(context);
		SelectedCacheEventList.Add(this);
		RelativeLayout descriptionLayout = (RelativeLayout) inflater.inflate(R.layout.description_view, null, false);
		this.addView(descriptionLayout);
		webViewLayout = (LinearLayout) findViewById(R.id.WebViewLayout);
		cacheInfo = (CacheInfoControl) findViewById(R.id.CompassDescriptionView);
		cacheInfo.setStyle(DrawStyle.withOwner);
		WebControl = (DescriptionViewControl) findViewById(R.id.DescriptionViewControl);
		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());

		if (main.mainActivity.getString(R.string.density).equals("ldpi"))
		{
			cacheInfo.setVisibility(GONE);
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// we overriding onMeasure because this is where the application gets
		// its right size.
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		cacheInfo.setHeight(UiSizes.getCacheInfoHeight());

	}

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (aktCache != cache)
		{
			aktCache = cache;
			cacheInfo.setCache(aktCache);
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
		// AllContextMenuCallHandler.showCacheDescViewContextMenu();
	}

	public void reloadCacheInfo()
	{
		String html = "</br>"

		+ "</br></br></br><form action=\"download\"><input type=\"submit\" value=\" "
				+ GlobalCore.Translations.Get("GC_DownloadDescription") + " \"></form>";

		WebControl.loadDataWithBaseURL("fake://fake.de/download", html, "text/html", "utf-8", null);
	}

	@Override
	public void OnShow()
	{
		this.forceLayout();

		// Del View from XML Layout
		webViewLayout.removeAllViews();
		if (WebControl != null)
		{
			WebControl.destroy();
			WebControl = null;
		}

		// Instanz new WebView
		WebControl = new DescriptionViewControl(main.mainActivity);
		WebControl.setScrollPos(lastScrollPos);
		webViewLayout.addView(WebControl);

		WebControl.OnShow();
		webViewLayout.setWillNotDraw(false);
		webViewLayout.invalidate();
		WebControl.setWillNotDraw(false);
		WebControl.invalidate();

		SelectedCacheEventList.Add(this);

		WebControl.getSettings().setBuiltInZoomControls(true);

	}

	public int getHeightForWebViewSpacious()
	{
		android.view.ViewGroup.LayoutParams paramsWebControl = WebControl.getLayoutParams();

		if (!(paramsWebControl == null))
		{
			return this.getHeight() - cacheInfo.getHeight();
		}
		return 0;
	}

	public int getWidthForWebViewSpacious()
	{
		return this.getWidth();
	}

	@Override
	public void OnHide()
	{

		// save last ScrollPos

		Point scpo = WebControl.getScrollPos();

		lastScrollPos.x = scpo.x;
		lastScrollPos.y = scpo.y;

		webViewLayout.removeAllViews();
		if (WebControl != null)
		{
			WebControl.destroy();
			WebControl = null;
		}

	}

	@Override
	public void OnFree()
	{
		if (WebControl != null) WebControl.OnFree();
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

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{

		// reset ScrollPos
		lastScrollPos = new Point(0, 0);

		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());

		// Thread t = new Thread()
		// {
		// public void run()
		// {
		// main.mainActivity.runOnUiThread(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// WebControl.OnShow();
		// WebControl.invalidate();
		// }
		// });
		// }
		// };
		//
		// t.start();

	}
}
