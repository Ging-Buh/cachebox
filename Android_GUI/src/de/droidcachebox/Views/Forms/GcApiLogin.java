package de.droidcachebox.Views.Forms;

import de.droidcachebox.R;
import CB_Core.Config;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class GcApiLogin extends Activity {
	private static final String TEAM_CACHEBOX_ASP_NET_URL = "http://aspspider.info/TeamCachebox/";
	private static GcApiLogin gcApiLogin;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gcapilogin);
		gcApiLogin = this;

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
				} else if (url.toLowerCase().contains(TEAM_CACHEBOX_ASP_NET_URL)) 
				{
					/*
					 * Auf der Seite ist eine Java Script Funktion eingebaut, die nach dem Laden der 
					 * Seite Automatich den AuthButton Clickt.
					 * 
					 * Damit wird sofort die GC Seite mit der Eingabe Maske Angezeigt.
					 */
					webView.loadUrl("javascript:clickButton()");
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

		webView.loadUrl(TEAM_CACHEBOX_ASP_NET_URL);
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
			MessageBox.Show("AccessToken erfolgreich erstellt!");
			gcApiLogin.finish();
		}
	}
	
		
}
