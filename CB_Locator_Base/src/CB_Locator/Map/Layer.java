package CB_Locator.Map;

import org.mapsforge.core.model.BoundingBox;

public class Layer {

    public enum MapType {
	ONLINE, MAPSFORGE, FREIZEITKARTE, BITMAP
    }

    public enum Type {
	normal, overlay
    }

    private Type mLayerType = Type.normal;
    public String Name = "";
    public String FriendlyName = "";
    public String Url = ""; // is used as complete path and name for mapsforge files
    private final MapType mapType;
    public BoundingBox boundingBox;

    public Layer(MapType mapType, Type LayerType, String name, String friendlyName, String url) {
	this.mapType = mapType;
	this.Name = name;
	this.FriendlyName = friendlyName;
	this.Url = url;
	this.mLayerType = LayerType;
    }

    public boolean DownloadTile(Descriptor desc) {
	// return DownloadFile(GetUrl(desc), GetLocalFilename(desc));
	return false;
    }

    public static boolean DownloadFile(String Url, String Filename) {
	return false;
	/*
	 * String path = Filename.Substring(0, Filename.LastIndexOf("\\")); // Verzeichnis anlegen if (!Directory.Exists(path))
	 * Directory.CreateDirectory(path); // Kachel laden HttpWebRequest webRequest = null; WebResponse webResponse = null; Stream stream
	 * = null; Stream responseStream = null; try { webRequest = (HttpWebRequest)WebRequest.Create(Url); webRequest.Timeout = 15000;
	 * webResponse = webRequest.GetResponse(); webRequest.Proxy = Global.Proxy; if (!webRequest.HaveResponse) return false;
	 * responseStream = webResponse.GetResponseStream(); byte[] result = Global.ReadFully(responseStream, 64000); // Datei schreiben
	 * stream = new FileStream(Filename, FileMode.Create); stream.Write(result, 0, result.Length); } catch (Exception) {
	 * //System.Windows.Forms.MessageBox.Show(exc.ToString()); return false; } finally { if (stream != null) { stream.Close(); stream =
	 * null; } if (responseStream != null) { responseStream.Close(); responseStream = null; } if (webResponse != null) {
	 * webResponse.Close(); webResponse = null; } if (webRequest != null) { webRequest.Abort(); webRequest = null; } GC.Collect(); }
	 * return true;
	 */
    }

    public String GetUrl(Descriptor desc) {
	if (desc == null)
	    return null;
	if (Name.contains("HillShade"))
	    return Url + "?x=" + desc.getX() + "&y=" + desc.getY() + "&z=" + desc.getZoom();
	else
	    return Url + desc.getZoom() + "/" + desc.getX() + "/" + desc.getY() + ".png";
    }

    public String GetLocalFilename(Descriptor desc) {
	if (desc == null)
	    return null;
	return desc.getLocalCachePath(Name) + ".png";
    }

    // public String GetLocalPath(Descriptor desc)
    // {
    // if (desc == null) return null;
    //
    // String TileCacheFolder = LocatorSettings.TileCacheFolder.getValue();
    // if (LocatorSettings.TileCacheFolderLocal.getValue().length() > 0) TileCacheFolder = LocatorSettings.TileCacheFolderLocal.getValue();
    //
    // return TileCacheFolder + "/" + Name + "/" + desc.getZoom() + "/" + desc.getX();
    // }

    public boolean isOverlay() {
	return mLayerType == Type.overlay;
    }

    public boolean isMapsForge() {
	return this.mapType == MapType.FREIZEITKARTE || this.mapType == MapType.MAPSFORGE;
    }

    public MapType getMapType() {
	return this.mapType;
    }
}
