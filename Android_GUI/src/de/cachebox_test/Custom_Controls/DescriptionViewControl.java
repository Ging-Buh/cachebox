package de.cachebox_test.Custom_Controls;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.DB.Database;
import CB_Core.Enums.Attributes;
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.Api.SearchForGeocaches;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Log.Logger;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import de.cachebox_test.Global;
import de.cachebox_test.main;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Views.Forms.MessageBox;

public class DescriptionViewControl extends WebView implements ViewOptionsMenu
{

	public static boolean mustLoadDescription;
	private Cache aktCache;
	private LinkedList<String> NonLocalImages = new LinkedList<String>();
	private LinkedList<String> NonLocalImagesUrl = new LinkedList<String>();
	private static ProgressDialog pd;
	private static DescriptionViewControl that;
	private boolean firstLoadReady = false;

	public DescriptionViewControl(Context context)
	{
		super(context);

		mustLoadDescription = false;

		this.setDrawingCacheEnabled(false);
		this.setAlwaysDrawnWithCacheEnabled(false);

		// this.getSettings().setJavaScriptEnabled(true);
		this.getSettings().setLightTouchEnabled(false);
		this.getSettings().setLoadWithOverviewMode(true);
		this.getSettings().setSupportZoom(true);
		this.getSettings().setBuiltInZoomControls(true);
		this.getSettings().setJavaScriptEnabled(true);
		this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

		this.setWebViewClient(clint);
		that = this;
		this.setFocusable(false);
	}

	public DescriptionViewControl(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		mustLoadDescription = false;

		this.setDrawingCacheEnabled(false);
		this.setAlwaysDrawnWithCacheEnabled(false);

		// this.getSettings().setJavaScriptEnabled(true);
		this.getSettings().setLightTouchEnabled(false);
		this.getSettings().setLoadWithOverviewMode(true);
		this.getSettings().setSupportZoom(true);
		this.getSettings().setBuiltInZoomControls(true);
		this.getSettings().setJavaScriptEnabled(true);
		this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

		this.setWebViewClient(clint);
		that = this;
	}

	WebViewClient clint = new WebViewClient()
	{
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			if (url.contains("fake://fake.de/Attr"))
			{
				int pos = url.indexOf("+");
				if (pos < 0) return true;

				final String attr = url.substring(pos + 1, url.length() - 1);

				MessageBox.Show(Translation.Get(attr));
				return true;
			}
			else if (url.contains("fake://fake.de/download"))
			{

				Thread thread = new Thread()
				{
					public void run()
					{

						if (!CB_Core.Api.GroundspeakAPI.CacheStatusValid)
						{
							int result = CB_Core.Api.GroundspeakAPI.GetCacheLimits();
							if (result != 0)
							{
								onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
								return;
							}

							if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
							{
								GL.that.Toast(ConnectionError.INSTANCE);
								return;
							}
							if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
							{
								GL.that.Toast(ApiUnavailable.INSTANCE);
								return;
							}
						}
						if (CB_Core.Api.GroundspeakAPI.CachesLeft <= 0)
						{
							String s = "Download limit is reached!\n";
							s += "You have downloaded the full cache details of " + CB_Core.Api.GroundspeakAPI.MaxCacheCount
									+ " caches in the last 24 hours.\n";
							if (CB_Core.Api.GroundspeakAPI.MaxCacheCount < 10) s += "If you want to download the full cache details of 6000 caches per day you can upgrade to Premium Member at \nwww.geocaching.com!";

							message = s;

							onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(2));

							return;
						}

						if (!CB_Core.Api.GroundspeakAPI.IsPremiumMember())
						{
							String s = "Download Details of this cache?\n";
							s += "Full Downloads left: " + CB_Core.Api.GroundspeakAPI.CachesLeft + "\n";
							s += "Actual Downloads: " + CB_Core.Api.GroundspeakAPI.CurrentCacheCount + "\n";
							s += "Max. Downloads in 24h: " + CB_Core.Api.GroundspeakAPI.MaxCacheCount;
							message = s;
							onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(3));
							return;
						}
						else
						{
							// call the download directly
							onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(4));
							return;
						}
					}
				};
				pd = ProgressDialog.show(getContext(), "", "Download Description", true);

				thread.start();

				return true;
			}
			else if (url.startsWith("http://"))
			{
				// Load Url in ext Browser
				platformConector.callUrl(url);
				return true;
			}
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url)
		{
			firstLoadReady = true;
			super.onPageFinished(view, url);

			Timer timer = new Timer();
			TimerTask task = new TimerTask()
			{
				@Override
				public void run()
				{
					main.mainActivity.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							myScrollTo(scrollPos.x, scrollPos.y);
						}
					});
				}
			};
			timer.schedule(task, 100);

		}

	};

	private String message = "";
	private Handler onlineSearchReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
			{
				pd.dismiss();
				break;
			}
			case 2:
			{
				pd.dismiss();
				MessageBox.Show(message, Translation.Get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, null);
				break;
			}
			case 3:
			{
				pd.dismiss();
				MessageBox.Show(message, Translation.Get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live,
						DownloadCacheDialogResult);
				break;
			}
			case 4:
			{
				pd.dismiss();
				DownloadCacheDialogResult.onClick(null, -1);
				break;
			}
			}
		}
	};

	private DialogInterface.OnClickListener DownloadCacheDialogResult = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			switch (button)
			{
			case -1:
				Cache newCache = SearchForGeocaches.LoadApiDetails(aktCache);
				if (newCache != null)
				{
					aktCache = newCache;
					setCache(newCache);

					if (!CB_Core.Api.GroundspeakAPI.IsPremiumMember())
					{
						String s = "Download successful!\n";
						s += "Downloads left for today: " + CB_Core.Api.GroundspeakAPI.CachesLeft + "\n";
						s += "If you upgrade to Premium Member you are allowed to download the full cache details of 6000 caches per day and you can search not only for traditional caches (www.geocaching.com).";

						MessageBox.Show(s, Translation.Get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, null);
					}
				}
				break;
			}
			if (dialog != null) dialog.dismiss();
		}
	};

	private int downloadTryCounter = 0;

	public void setCache(final Cache cache)
	{
		final String mimeType = "text/html";
		final String encoding = "utf-8";
		if (cache != null)
		{
			NonLocalImages.clear();
			NonLocalImagesUrl.clear();
			String cachehtml = Database.GetDescription(cache);
			String html = "";
			if (cache.ApiStatus == 1)// GC.com API lite
			{ // Load Standard HTML
				String nodesc = Translation.Get("GC_NoDescription");
				html = "</br>" + nodesc + "</br></br></br><form action=\"download\"><input type=\"submit\" value=\" "
						+ Translation.Get("GC_DownloadDescription") + " \"></form>";
			}
			else
			{
				html = DescriptionImageGrabber.ResolveImages(cache, cachehtml, false, NonLocalImages, NonLocalImagesUrl);

				if (!Config.DescriptionNoAttributes.getValue()) html = getAttributesHtml(cache) + html;

				// add 2 empty lines so that the last line of description can be selected with the markers
				html += "</br></br>";
			}

			final String FinalHtml = html;

			main.mainActivity.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						DescriptionViewControl.this.loadDataWithBaseURL("fake://fake.de", FinalHtml, mimeType, encoding, null);
					}
					catch (Exception e)
					{
						return; // if an exception here, then this is not initializes
					}
				}
			});

		}

		try
		{
			if (this.getSettings() != null) this.getSettings().setLightTouchEnabled(true);
		}
		catch (Exception e1)
		{
			// dann kann eben nicht gezoomt werden!
		}

		// Falls nicht geladene Bilder vorliegen und eine Internetverbindung
		// erlaubt ist, diese laden und Bilder erneut aufl�sen
		if (NonLocalImagesUrl.size() > 0)
		{
			downloadThread = new Thread()
			{
				public void run()
				{

					if (downloadTryCounter > 0)
					{
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
							Logger.Error("DescriptionViewControl.setCache()", "Thread.sleep fehler", e);
							e.printStackTrace();
						}
					}

					while (NonLocalImagesUrl != null && NonLocalImagesUrl.size() > 0)
					{
						String local, url;
						local = NonLocalImages.poll();
						url = NonLocalImagesUrl.poll();

						try
						{
							DescriptionImageGrabber.Download(url, local);
						}
						catch (Exception e)
						{
							Logger.Error("DescriptionViewControl.setCache()", "downloadThread run()", e);
						}
					}
					downloadReadyHandler.post(downloadComplete);

				}
			};
			downloadThread.start();
		}

		if (cache != null)
		{
			cache.ReloadSpoilerRessources();
		}
	}

	final Handler downloadReadyHandler = new Handler();
	Thread downloadThread;

	final Runnable downloadComplete = new Runnable()
	{
		public void run()
		{
			Global.setDebugMsg("Reload " + String.valueOf(downloadTryCounter++));
			if (downloadTryCounter < 10) // nur 10 Download versuche zu lassen
			setCache(aktCache);
		}
	};

	private String getAttributesHtml(Cache cache)
	{
		StringBuilder sb = new StringBuilder();

		Iterator<Attributes> attrs = cache.getAttributes().iterator();

		if (attrs == null || !attrs.hasNext()) return "";

		do
		{
			Attributes attribute = attrs.next();
			File result = new File(Config.WorkPath + "/data/Attributes/" + attribute.getImageName() + ".png");

			sb.append("<form action=\"Attr\">");
			sb.append("<input name=\"Button\" type=\"image\" src=\"file://" + result.getAbsolutePath() + "\" value=\" "
					+ attribute.getImageName() + " \">");
		}
		while (attrs.hasNext());

		sb.append("</form>");

		if (sb.length() > 0) sb.append("<br>");
		return sb.toString();
	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
	}

	@Override
	public void OnShow()
	{

		main.mainActivity.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
				// that.getParent().requestLayout();

				if (downloadTryCounter > 9) mustLoadDescription = true; // Versuchs
																		// nochmal mit
																		// dem Download
				downloadTryCounter = 0;
				if (mustLoadDescription)
				{
					setCache(aktCache);
					mustLoadDescription = false;
				}

				// im Day Mode brauchen wir kein InvertView
				// das sollte mehr Performance geben
				if (Config.nightMode.getValue())
				{
					invertViewControl.Me.setVisibility(VISIBLE);
				}
				else
				{
					invertViewControl.Me.setVisibility(GONE);
				}

				that.setWillNotDraw(false);
				that.invalidate();
			}
		});

	}

	@Override
	public void OnHide()
	{
		try
		{
			// this.clearCache(true);
		}
		catch (Exception e)
		{
			Logger.Error("DescriptionViewControl.OnHide()", "clearCache", e);
			e.printStackTrace();
		}
	}

	@Override
	public void OnFree()
	{
		try
		{
			// this.clearCache(true);
		}
		catch (Exception e)
		{
			Logger.Error("DescriptionViewControl.OnFree()", "clearCache", e);
			e.printStackTrace();
		}
		this.destroy();
	}

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{

		aktCache = cache;
		mustLoadDescription = true;
		setCache(aktCache);
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

	public static boolean isDrawn = false;

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		isDrawn = true;
		invertViewControl.Me.invalidate();
	}

	private Point scrollPos = new Point(0, 0);

	@Override
	protected void onScrollChanged(int x, int y, int oldl, int oldt)
	{
		super.onScrollChanged(x, y, oldl, oldt);
		scrollPos.x = x;
		scrollPos.y = y;
	}

	public Point getScrollPos()
	{
		return scrollPos;
	}

	public void setScrollPos(Point pos)
	{
		scrollPos = pos;
	}

	private boolean myScroll = false;

	private void myScrollTo(int x, int y)
	{
		myScroll = true;
		scrollTo(x, y);
	}

	@Override
	public void scrollTo(int x, int y)
	{
		if (!myScroll) return;
		myScroll = false;
		if (firstLoadReady) super.scrollTo(x, y);
	}

}
