package nonGuiClasses;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;

import de.droidcachebox.Config;
import de.droidcachebox.Geocaching.FieldNoteEntry;



public class UploadFieldnotes 
{
	
	
	private String mUser   = Config.GetString("GcLogin");
	private String mGcPass = Config.GetStringEncrypted("GcPass");
	

	
	
    public String ExecutePostRequest(
            URI url,
            HashMap<String, String> postData,
            HashMap<String, String> postData2,
            File fileToUpload,
            String fileMimeType,
            String fileFormKey
          )
	{
	    	String responseString = "";
	
			if(fileToUpload != null)
			{
				long requestContentLength = 607;
				
				if (Config.GetBool("FieldnotesUploadAll"))
					requestContentLength = requestContentLength - 89;
				
				for(String key : postData.values())
				{
					requestContentLength += key.length() + postData.get(key).length();
				}
				
				for (String key : postData2.values())
				{
					requestContentLength += key.length() + postData2.get(key).length();
				}
				
				requestContentLength += fileMimeType.length();
				requestContentLength += fileFormKey.length();
				requestContentLength += fileToUpload.getName().length();
				requestContentLength += fileToUpload.length();
				
				
				/*Hier komme ich nicht weiter. Andre
				
				                  
				HttpRequest request = //(HttpWebRequest)WebRequest.Create(url.AbsoluteUri);
				
				request.Method = "POST";
				request.Accept = "text/html";
				request.Referer = url.AbsoluteUri;
				request.AllowAutoRedirect = true;              
				request.KeepAlive = true;
				request.Timeout = 15000;
				request.Proxy = Global.Proxy;
				request.ContentLength = requestContentLength;
				cookieManager.PublishCookies(request);
				
				string boundary = DictionaryExtensions.CreateFormDataBoundary();
				request.ContentType = "multipart/form-data; boundary=" + boundary;
				byte[] endBytes = System.Text.Encoding.UTF8.GetBytes("--" + boundary + "--");
				
				Stream requestStream = request.GetRequestStream();
				
				DictionaryExtensions.WriteMultipartFormData(postData, requestStream, boundary);
				FileInfoExtensions.WriteMultipartFormData(fileToUpload, requestStream, boundary, fileMimeType, fileFormKey);
				DictionaryExtensions.WriteMultipartFormData(postData2, requestStream, boundary);
				
				requestStream.Write(endBytes, 0, endBytes.Length);
				
				requestStream.Close();
				
				using (HttpWebResponse response = (HttpWebResponse)request.GetResponse())
				{
				cookieManager.StoreCookies(response);
				using (StreamReader reader = new StreamReader(response.GetResponseStream()))
				{
				responseString = reader.ReadToEnd();
				}
				};
				*/
			}
		
		return responseString;
			
	}
    
    
    
    
}