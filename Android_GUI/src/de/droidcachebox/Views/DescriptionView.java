package de.droidcachebox.Views;

import CB_Core.GlobalCore;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Custom_Controls.CacheInfoControl;
import de.droidcachebox.Custom_Controls.DescriptionViewControl;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.AllContextMenuCallHandler;
import de.droidcachebox.Ui.Sizes;

public class DescriptionView extends FrameLayout implements ViewOptionsMenu
{
	Context context;
	public Cache aktCache;

	Button TestButton;
	public CacheInfoControl cacheInfo;
	public static DescriptionViewControl WebControl;
	public static LinearLayout webViewLayout;

	public DescriptionView(Context context, LayoutInflater inflater)
	{
		super(context);

		RelativeLayout descriptionLayout = (RelativeLayout) inflater.inflate(R.layout.description_view, null, false);
		this.addView(descriptionLayout);
		webViewLayout = (LinearLayout) findViewById(R.id.WebViewLayout);
		cacheInfo = (CacheInfoControl) findViewById(R.id.CompassDescriptionView);
		cacheInfo.setStyle(DrawStyle.withOwner);
		WebControl = (DescriptionViewControl) findViewById(R.id.DescriptionViewControl);

		SetSelectedCache(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());

		if (main.mainActivity.getString(R.string.density).equals("ldpi"))
		{
			cacheInfo.setVisibility(GONE);
		}

		OnShow();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// we overriding onMeasure because this is where the application gets
		// its right size.
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		cacheInfo.setHeight(Sizes.getCacheInfoHeight());

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
		AllContextMenuCallHandler.showCacheDescViewContextMenu();
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

		WebControl.OnShow();
		webViewLayout.setWillNotDraw(false);
		webViewLayout.invalidate();
		WebControl.setWillNotDraw(false);
		WebControl.invalidate();

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
		WebControl.OnHide();
	}

	@Override
	public void OnFree()
	{
		WebControl.OnFree();
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
