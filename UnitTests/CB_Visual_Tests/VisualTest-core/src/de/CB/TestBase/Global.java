package de.CB.TestBase;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.mapsforge.map.rendertheme.XmlRenderTheme;

import CB_Locator.Map.ManagerBase;
import de.CB.TestBase.Views.MapView;

/**
 * Enth�lt die Globalen Statichen Member
 * 
 * @author Longri
 */
public class Global extends CB_UI_Base.Global {
	public static final int CurrentRevision = 9;
	public static final String CurrentVersion = "0.3.";
	public static final String VersionPrefix = "test";

	public static final String br = System.getProperty("line.separator");
	public static final String fs = System.getProperty("file.separator");

	public static final String AboutMsg = "freizeitkarte-osm (2013)" + br + "www.freizeitkarte-osm.de" + br + "developed by:" + br + "SammysHP (c:geo)" + br + "Longri (Cachebox)" + br + "On behalf of" + br + "freizeitkarte-osm";

	public static final String splashMsg = AboutMsg + br + br + br + "POWERED BY:";
	public static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR = 2082012;

	public static String getVersionString() {
		final String ret = "Version: " + CurrentVersion + String.valueOf(CurrentRevision) + "  " + (VersionPrefix.equals("") ? "" : "(" + VersionPrefix + ")");
		return ret;
	}

	@Override
	protected String getVersionPrefix() {
		return VersionPrefix;
	}

}
