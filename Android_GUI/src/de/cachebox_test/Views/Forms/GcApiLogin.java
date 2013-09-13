package de.cachebox_test.Views.Forms;

import CB_Core.Api.CB_Api;
import CB_Core.Api.GroundspeakAPI;
import CB_UI.Config;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Ui.ActivityUtils;

public class GcApiLogin extends Activity
{
	private static GcApiLogin gcApiLogin;
	private static ProgressDialog pd;
	private static boolean pdIsShow = false;
	private LinearLayout webViewLayout;
	private WebView WebControl;

	public void onCreate(Bundle savedInstanceState)
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gcapilogin);

		webViewLayout = (LinearLayout) findViewById(R.id.gal_Layout);

		gcApiLogin = this;

		new RetreiveFeedTask().execute();

	}

	class RetreiveFeedTask extends AsyncTask<Void, Void, String>
	{

		@Override
		protected String doInBackground(Void... params)
		{
			String GC_AuthUrl;

			if (Config.OverrideUrl.getValue().equals(""))
			{
				GC_AuthUrl = CB_Api.getGcAuthUrl();
			}
			else
			{
				GC_AuthUrl = Config.OverrideUrl.getValue();
			}

			if (GC_AuthUrl.equals(""))
			{
				finish();
			}

			return GC_AuthUrl;
		}

		@Override
		protected void onPostExecute(String GC_AuthUrl)
		{
			ShowWebsite(GC_AuthUrl);
		}

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (pd != null && pd.isShowing())
		{
			pd.dismiss();
		}
		pd = null;
	}

	private void ShowWebsite(String GC_AuthUrl)
	{
		// Initial new VebView Instanz

		WebControl = (WebView) gcApiLogin.findViewById(R.id.gal_WebView);

		webViewLayout.removeAllViews();
		if (WebControl != null)
		{
			WebControl.destroy();
			WebControl = null;
		}

		// Instanz new WebView
		WebControl = new WebView(main.mainActivity, null, android.R.attr.webViewStyle);
		WebControl.requestFocus(View.FOCUS_DOWN);
		WebControl.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch (event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus())
					{
						v.requestFocus();
					}
					break;
				}
				return false;
			}
		});
		webViewLayout.addView(WebControl);

		if (!pdIsShow)
		{
			gcApiLogin.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					pd = ProgressDialog.show(gcApiLogin, "", "Loading....", true);
					pdIsShow = true;
				}
			});

		}

		View titleView = getWindow().findViewById(android.R.id.title);
		if (titleView != null)
		{
			ViewParent parent = titleView.getParent();
			if (parent != null && (parent instanceof View))
			{
				View parentView = (View) parent;
				parentView.setBackgroundColor(Global.getColor(R.attr.TitleBarBackColor));
			}
		}

		WebControl.setWebViewClient(new WebViewClient()
		{

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				gcApiLogin.setTitle("Loading...");

				if (!pdIsShow)
				{
					gcApiLogin.runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							pd = ProgressDialog.show(gcApiLogin, "", "Loading....", true);
							pdIsShow = true;
						}
					});

				}

				super.onPageStarted(view, url, favicon);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{

				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url)
			{

				gcApiLogin.setTitle(R.string.app_name);
				if (pd != null) pd.dismiss();
				pdIsShow = false;

				if (url.toLowerCase().contains("oauth_verifier=") && (url.toLowerCase().contains("oauth_token=")))
				{
					WebControl
							.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				}
				else
					super.onPageFinished(view, url);
			}

		});

		WebSettings settings = WebControl.getSettings();

		// settings.setPluginsEnabled(true);
		settings.setJavaScriptEnabled(true);
		// settings.setJavaScriptCanOpenWindowsAutomatically(true);

		// webView.setWebChromeClient(new WebChromeClient());
		WebControl.getSettings().setJavaScriptEnabled(true);
		WebControl.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

		WebControl.loadUrl(GC_AuthUrl);
	}

	class MyJavaScriptInterface
	{

		public void showHTML(String html)
		{

			String search = "Access token: ";
			int pos = html.indexOf(search);
			if (pos < 0) return;
			int pos2 = html.indexOf("</span>", pos);
			if (pos2 < pos) return;
			// zwischen pos und pos2 sollte ein gültiges AccessToken sein!!!
			final String accessToken = html.substring(pos + search.length(), pos2);

			Thread thread = new Thread()
			{
				public void run()
				{
					GroundspeakAPI.CacheStatusValid = false;
					GroundspeakAPI.CacheStatusLiteValid = false;

					// store the encrypted AccessToken in the Config file
					// wir bekommen den Key schon verschlüsselt, deshalb muss er
					// nicht noch einmal verschlüsselt werden!
					if (Config.StagingAPI.getValue())
					{
						Config.GcAPIStaging.setEncryptedValue(accessToken);
					}
					else
					{
						Config.GcAPI.setEncryptedValue(accessToken);
					}

					Config.AcceptChanges();

					String act = Config.GetAccessToken();
					if (act.length() > 0)
					{
						int status = GroundspeakAPI.GetMembershipType();
						if (status >= 0)
						{

							Config.GcLogin.setValue(GroundspeakAPI.MemberName);
							Config.AcceptChanges();

						}

					}

					onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
				}
			};
			gcApiLogin.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					pd = ProgressDialog.show(gcApiLogin, "", "Download Username", true);
				}
			});

			thread.start();
		}
	}

	private Handler onlineSearchReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
			{
				pd.dismiss();
				gcApiLogin.finish();
			}
			}
		}
	};
}
