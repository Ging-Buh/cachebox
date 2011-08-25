package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import CB_Core.Config;
import CB_Core.Api.CB_Api;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.ProgressDialog;
import android.graphics.Bitmap;

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
		
		
		if(!pdIsShow)
    	{
    		pd = ProgressDialog.show(gcApiLogin, "", 
                 "Loading....", true);
    		pdIsShow=true;
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
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				gcApiLogin.setTitle("Loading...");
            	
            	if(!pdIsShow)
            	{
            		pd = ProgressDialog.show(gcApiLogin, "", 
		                 "Loading....", true);
            		pdIsShow=true;
            	}
            	
            	super.onPageStarted( view,  url,  favicon);
			}
			
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				
				
				
				
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				
				gcApiLogin.setTitle(R.string.app_name);
            	if(pd!=null)pd.dismiss();
            	pdIsShow=false;
				
				if (url.toLowerCase().contains("oauth_verifier=")
						&& (url.toLowerCase().contains("oauth_token="))) {
					webView.addJavascriptInterface(new MyJavaScriptInterface(),
							"HTMLOUT");
					webView.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				} else
					super.onPageFinished(view, url);
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

	class MyJavaScriptInterface {

		public void showHTML(String html) {

			String search = "Access token: ";
			int pos = html.indexOf(search);
			if (pos < 0)
				return;
			int pos2 = html.indexOf("</span>", pos);
			if (pos2 < pos)
				return;
			// zwischen pos und pos2 sollte ein gültiges AccessToken sein!!!
			String accessToken = html.substring(pos + search.length(), pos2);
			Config.Set("GcAPI", accessToken);
			if(Settings.Me!=null)
			{
				Settings.Me.setGcApiKey(accessToken);
			}
			gcApiLogin.finish();
		}
	}
}
