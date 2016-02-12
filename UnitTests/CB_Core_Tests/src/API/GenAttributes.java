package API;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import CB_Core.Api.GroundspeakAPI;
import CB_UI.Config;
import CB_Utils.Util.FileIO;
import CB_Utils.http.HttpUtils;
import __Static.InitTestDBs;

/**
 * Der Test ist kein Wirklicher Test. Hier werden die GS Attribute Herrunter geladen und es wird die "Attributes.java" daraus generiert.
 * Damit die Attribute immer Aktuell gehalten werden k�nnen.
 * 
 * @author Longri
 */
public class GenAttributes extends TestCase {
	private Boolean NotRun = false;

	public static String LastAPIError = "";

	public void testGetAllAttributes() throws IOException {
		if (NotRun)
			return;

		InitTestDBs.InitalConfig();
		String accessToken = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", accessToken.equals(""));

		// read all GS Attributes
		ArrayList<GsAttributes> attList = new ArrayList<GsAttributes>();

		try {
			HttpGet httppost = new HttpGet(GroundspeakAPI.GS_LIVE_URL + "GetAttributeTypesData?AccessToken=" + accessToken + "&format=json");

			String result = HttpUtils.Execute(httppost, null);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0) {
					LastAPIError = "";
					JSONArray jAttributes = json.getJSONArray("AttributeTypes");

					for (int ii = 0; ii < jAttributes.length(); ii++) {
						JSONObject jAtt = (JSONObject) jAttributes.get(ii);

						GsAttributes tmp = new GsAttributes();
						try {
							tmp.ID = jAtt.getInt("ID");
							tmp.hasNo = jAtt.getBoolean("HasNoOption");
							tmp.hasYes = jAtt.getBoolean("HasYesOption");

							String Name = jAtt.getString("Name");
							Name = Name.replace(" ", "_").trim();
							int Pos1 = Name.indexOf("(");
							if (Pos1 > 0) {
								int Pos2 = Name.indexOf(")");
								String clear = Name.substring(Pos1, Pos2);
								Name = Name.replace(clear, "").trim();
							}
							Name = Name.replace("_/", "").trim();

							tmp.Name = Name;
							tmp.UrlNoIcon = jAtt.getString("NoIconName");
							tmp.UrlYesIcon = jAtt.getString("YesIconName");
							tmp.Description = jAtt.getString("Description");
						} catch (JSONException e) {
						}

						attList.add(tmp);
					}

				} else {
					LastAPIError = "";
					LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					LastAPIError += status.getString("StatusMessage") + "\n";
					LastAPIError += status.getString("ExceptionDetails");

					return;
				}

			} catch (JSONException e) {

				e.printStackTrace();
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return;
		}

		// write Attributes.java
		String BR = String.format("%n");

		String java = "";

		java += BR + "package CB_Core.Enums;";
		java += BR + "";
		java += BR + "import java.util.ArrayList;";
		java += BR + "import java.util.HashMap;";
		java += BR + "";
		java += BR + "public enum Attributes";
		java += BR + "{";
		java += BR + "  Default, ";
		Iterator<GsAttributes> iterator = attList.iterator();
		do {
			java += " ";
			java += iterator.next().Name;
			if (iterator.hasNext()) {
				java += ",";
			} else {
				java += ";";
			}

		} while (iterator.hasNext());

		java += BR + "";
		java += BR + "";

		java += BR + "public static long GetAttributeIndex(Attributes attrib)";
		java += BR + "{";
		java += BR + "return ((long) 1) << (attrib.ordinal());";
		java += BR + "}";
		java += BR + "";
		java += BR + "";
		java += BR + "private boolean negative=false;";
		java += BR + "";
		java += BR + "";

		java += BR + "public static Attributes getAttributeEnumByGcComId(int id)";
		java += BR + "{";
		java += BR + "switch (id)";
		java += BR + "{";

		iterator = attList.iterator();
		do {
			GsAttributes tmp = iterator.next();

			java += BR + "case " + String.valueOf(tmp.ID) + ":";
			java += BR + "return CB_Core.Enums.Attributes." + tmp.Name + ";";

		} while (iterator.hasNext());

		java += BR + "}";
		java += BR + "";

		java += BR + "return CB_Core.Enums.Attributes.Default;";
		java += BR + "}";

		java += BR + "private static HashMap<Attributes, Integer> attributeLookup;";
		java += BR + "";
		java += BR + "private static void ini()";
		java += BR + "{";
		java += BR + "	attributeLookup = new HashMap<Attributes, Integer>();";
		java += BR + "	attributeLookup.put(Attributes.Default, 0);";
		iterator = attList.iterator();
		do {
			GsAttributes tmp = iterator.next();
			java += BR + "	attributeLookup.put(Attributes." + tmp.Name + ", " + String.valueOf(tmp.ID) + ");";
		} while (iterator.hasNext());

		java += BR + "}";
		java += BR + "";
		java += BR + "public static ArrayList<Attributes> getAttributes(long attributesPositive, long attributesNegative)";
		java += BR + "{";
		java += BR + "ArrayList<Attributes> ret = new ArrayList<Attributes>();";

		java += BR + "if (attributeLookup == null) ini();";

		java += BR + "for (Attributes attribute : attributeLookup.keySet())";
		java += BR + "{";
		java += BR + "long att = Attributes.GetAttributeIndex(attribute);";
		java += BR + "if ((att & attributesPositive) > 0)";
		java += BR + "{";
		java += BR + "ret.add(attribute);";
		java += BR + "}";
		java += BR + "}";
		java += BR + "for (Attributes attribute : attributeLookup.keySet())";
		java += BR + "{";
		java += BR + "long att = Attributes.GetAttributeIndex(attribute);";
		java += BR + "if ((att & attributesNegative) > 0)";
		java += BR + "{";
		java += BR + "attribute.negative=true;";
		java += BR + "ret.add(attribute);";
		java += BR + "}";
		java += BR + "}";
		java += BR + "";
		java += BR + "return ret;";
		java += BR + "}";
		java += BR + "";

		java += BR + "public String getImageName()";
		java += BR + "{";
		java += BR + "if (attributeLookup == null) ini();";
		java += BR + "String ret = \"att_\"+ String.valueOf(attributeLookup.get(this));";
		java += BR + "";
		java += BR + "if (negative)";
		java += BR + "{";
		java += BR + "	ret+=\"_0\";";
		java += BR + "}";
		java += BR + "else";
		java += BR + "{";
		java += BR + "	ret+=\"_1\";";
		java += BR + "}";

		java += BR + "return ret; ";
		java += BR + "}";

		// create folder
		File FolderPath = new File("./testdata/Attributes");

		FolderPath.mkdirs();

		// Write to File
		String FilePath = "./testdata/Attributes/Attributes.java";
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(FilePath));
			writer.write(java);
		} catch (IOException e) {
			writer.close();
		}
		writer.close();

		// Download ATTR Images

		iterator = attList.iterator();
		do {
			GsAttributes tmp = iterator.next();
			String lacalNo = "./testdata/Attributes/att_" + String.valueOf(tmp.ID) + "_0.png";
			String lacalYes = "./testdata/Attributes/att_" + String.valueOf(tmp.ID) + "_1.png";

			if (tmp.hasYes) {
				FileIO.Download(tmp.UrlYesIcon, lacalYes);
			}

			if (tmp.hasNo) {
				FileIO.Download(tmp.UrlNoIcon, lacalNo);
			}
		} while (iterator.hasNext());

		// Write EN Description for Lang File

		String Lang = "";
		iterator = attList.iterator();
		do {
			GsAttributes tmp = iterator.next();
			if (tmp.hasYes) {
				String descYes = tmp.Description;
				if (descYes.length() > 0)
					Lang += "att_" + String.valueOf(tmp.ID) + "_1 = " + descYes + BR;
			}

			if (tmp.hasNo) {
				String descNo = tmp.Description;
				if (descNo.length() > 0)
					Lang += "att_" + String.valueOf(tmp.ID) + "_0 = NO " + descNo + BR;
			}
		} while (iterator.hasNext());

		String enFilePath = "./testdata/Attributes/en.lan";
		BufferedWriter enwriter = null;
		try {
			enwriter = new BufferedWriter(new FileWriter(enFilePath));
			enwriter.write(Lang);
		} catch (IOException e) {
			enwriter.close();
		}
		enwriter.close();

	}

	class GsAttributes {
		int ID;
		String Name;

		String UrlNoIcon;
		String UrlYesIcon;
		String Description;

		Boolean hasNo;
		Boolean hasYes;
	}

}
