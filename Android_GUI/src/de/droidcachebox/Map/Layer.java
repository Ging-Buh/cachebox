package de.droidcachebox.Map;

import CB_Core.Config;
import CB_Core.Map.Descriptor;

public class Layer
{

	public String Name = "";

	public String FriendlyName = "";

	public String Url = "";

	public boolean isMapsForge = false;

	public Layer(String name, String friendlyName, String url)
	{
		Name = name;
		FriendlyName = friendlyName;
		Url = url;
	}

	public boolean DownloadTile(Descriptor desc)
	{
		// return DownloadFile(GetUrl(desc), GetLocalFilename(desc));
		return false;
	}

	public static boolean DownloadFile(String Url, String Filename)
	{
		return false;
		/*
		 * String path = Filename.Substring(0, Filename.LastIndexOf("\\")); //
		 * Verzeichnis anlegen if (!Directory.Exists(path))
		 * Directory.CreateDirectory(path); // Kachel laden HttpWebRequest
		 * webRequest = null; WebResponse webResponse = null; Stream stream =
		 * null; Stream responseStream = null; try { webRequest =
		 * (HttpWebRequest)WebRequest.Create(Url); webRequest.Timeout = 15000;
		 * webResponse = webRequest.GetResponse(); webRequest.Proxy =
		 * Global.Proxy; if (!webRequest.HaveResponse) return false;
		 * responseStream = webResponse.GetResponseStream(); byte[] result =
		 * Global.ReadFully(responseStream, 64000); // Datei schreiben stream =
		 * new FileStream(Filename, FileMode.Create); stream.Write(result, 0,
		 * result.Length); } catch (Exception) {
		 * //System.Windows.Forms.MessageBox.Show(exc.ToString()); return false;
		 * } finally { if (stream != null) { stream.Close(); stream = null; } if
		 * (responseStream != null) { responseStream.Close(); responseStream =
		 * null; } if (webResponse != null) { webResponse.Close(); webResponse =
		 * null; } if (webRequest != null) { webRequest.Abort(); webRequest =
		 * null; } GC.Collect(); } return true;
		 */
	}

	public String GetUrl(Descriptor desc)
	{
		return Url + desc.Zoom + "/" + desc.X + "/" + desc.Y + ".png";
	}

	public String GetLocalFilename(Descriptor desc)
	{
		return GetLocalPath(desc) + "/" + desc.Y + ".png";
	}

	public String GetLocalPath(Descriptor desc)
	{
		return Config.settings.TileCacheFolder.getValue() + "/" + Name + "/" + desc.Zoom + "/" + desc.X;
	}
}
