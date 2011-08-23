package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import CB_Core.Config;
import CB_Core.Api.CB_Api;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class GcApiLogin extends Activity {

	private static GcApiLogin gcApiLogin;
	private static ProgressDialog pd;
	private static boolean pdIsShow=false;
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gcapilogin);
		gcApiLogin = this;
		
		String GC_AuthUrl;
		
		if(Config.GetString("OverrideUrl").equals(""))
		{
			GC_AuthUrl = CB_Api.getGcAuthUrl();
		}
		else
		{
			GC_AuthUrl = Config.GetString("OverrideUrl");
		}
		
		if(GC_AuthUrl.equals(""))
		{
			finish();
		}

		View titleView = getWindow().findViewById(android.R.id.title);
		if (titleView != null) {
		  ViewParent parent = titleView.getParent();
		  if (parent != null && (parent instanceof View)) {
		    View parentView = (View)parent;
		    parentView.setBackgroundColor(Global.getColor(R.attr.TitleBarBackColor));
		  }
		}

		
		final WebView webView = (WebView) this.findViewById(R.id.gal_WebView);

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (url.toLowerCase().contains("oauth_verifier=")
						&& (url.toLowerCase().contains("oauth_token="))) {
					webView.addJavascriptInterface(new MyJavaScriptInterface(),
							"HTMLOUT");
					webView.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				} else
					super.onPageFinished(view, url);
			}
			
			
		});
		
		
		webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)   
            {
             //Make the bar disappear after URL is loaded, and changes string to Loading...
            	gcApiLogin.setTitle("Loading...");
            	gcApiLogin.setProgress(progress); //Make the bar disappear after URL is loaded

            	if(!pdIsShow)
            	{
            		pd = ProgressDialog.show(gcApiLogin, "", 
		                 "Loading....", true);
            		pdIsShow=true;
            	}
            	
            	
             // Return the app name after finish loading
                if(progress == 100)
                {
                	gcApiLogin.setTitle(R.string.app_name);
                	pd.dismiss();
                	pdIsShow=false;
                }
                	
              }
            });

		
		WebSettings settings = webView.getSettings();

		settings.setPluginsEnabled(true);
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);

		// webView.setWebChromeClient(new WebChromeClient());
		webView.getSettings().setJavaScriptEnabled(true);

		webView.loadUrl(GC_AuthUrl);
	}

	class MyJavaScriptInterface 
	{
		public void showHTML(String html) 
		{
			//String search = "Authorized!  Access token: "; Longri change: bei mir gab es bei der Antwort nur ein Leerzeichen!
			String search = "Authorized! Access token: ";
			int pos = html.indexOf(search);
			if (pos < 0)
				return;
			int pos2 = html.indexOf("</span>", pos);
			if (pos2 < pos)
				return;
			// zwischen pos und pos2 sollte ein gültiges AccessToken sein!!!
			String accessToken = html.substring(pos + search.length(), pos2);
			Config.Set("GcAPI", accessToken);
			Config.AcceptChanges();
//			MessageBox.Show("AccessToken erfolgreich erstellt! " +accessToken);
			if(Settings.Me!=null)
			{
				Settings.Me.setGcApiKey(accessToken);
			}
			gcApiLogin.finish();
		}
	}
	
}
