/* 
 * Copyright (C) 2013 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CB_Core.TranslationEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.FileUtil;

/**
 * @author Longri
 */
public class Translation
{

	/**
	 * @uml.property name="that"
	 * @uml.associationEnd
	 */
	private static Translation that;

	private final String BR;
	private ArrayList<Translations> mStringList;
	private ArrayList<Translations> mRefTranslation;
	private final ArrayList<Translations> mMissingStringList;
	private String mLangID;
	private final String mWorkPath;
	private String mInitialLangPath;

	/**
	 * Constructor
	 * 
	 * @param WorkPath
	 */
	public Translation(String WorkPath)
	{
		that = this;
		mWorkPath = WorkPath;
		BR = System.getProperty("line.separator");
		mStringList = new ArrayList<Translations>();
		mMissingStringList = new ArrayList<Translations>();
	}

	// #######################################################################
	// Public static access
	// #######################################################################

	/**
	 * Load the Translation from File
	 * 
	 * @param LangPath
	 */
	public static void LoadTranslation(String LangPath)
	{
		that.mInitialLangPath = LangPath;

		try
		{
			that.ReadTranslationsFile(that.mInitialLangPath);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Returns the translation from StringID
	 * 
	 * @param StringId
	 *            as String
	 * @return String
	 */
	public static String Get(String StringId)
	{
		return that.get(StringId);
	}

	/**
	 * Returns the translation from StringID </br>with params ??????
	 * 
	 * @param StringId
	 *            as String
	 * @param params
	 *            ?????
	 * @return String
	 */
	public static String Get(String StringId, String... params)
	{
		return that.get(StringId, params);
	}

	/**
	 * Returns a list of languages found on Path
	 * 
	 * @param FilePath
	 *            as String
	 * @return ArrayList of Lang
	 */
	public static ArrayList<Lang> GetLangs(String FilePath)
	{
		return that.getLangs(FilePath);
	}

	/**
	 * Returns the actual loaded LangID
	 * 
	 * @return LangID as String
	 */
	public static String getLangId()
	{
		return that.mLangID;
	}

	/**
	 * Write list of missing StringId's to debug.txt
	 * 
	 * @throws IOException
	 */
	public static void writeMisingStringsFile() throws IOException
	{
		that.writeMisingStrings();
	}

	/**
	 * Read the missing StringID's from debug.txt
	 */
	public static void readMissingStringsFile() throws IOException
	{
		that.readMissing();
	}

	/**
	 * Return a String from File
	 * 
	 * @param Name
	 *            File Name
	 * @return String from File
	 * @throws IOException
	 */
	public static String GetTextFile(String Name, String overrideLangId) throws IOException
	{
		return that.getTextFile(Name, overrideLangId);
	}

	// #######################################################################
	// Private access
	// #######################################################################

	// #######################################################################
	// Private access
	// #######################################################################

	private String getLangNameFromFile(String FilePath) throws IOException
	{
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(FilePath));
		String Value = reader.readLine().trim();
		int pos = Value.indexOf("=");
		Value = Value.substring(pos + 1);

		reader.close();
		return Value;
	}

	private void ReadTranslationsFile(String FilePath) throws IOException
	{
		if (FilePath.equals(""))
		{
			return;
		}

		if (mRefTranslation == null)
		{
			int pos = FilePath.lastIndexOf("lang") + 4;
			String LangFileName = FilePath.substring(0, pos) + "/en-GB/strings.ini";
			mRefTranslation = ReadFile(LangFileName);
		}

		mStringList = ReadFile(FilePath);

		String tmp = FilePath;
		int pos2 = tmp.lastIndexOf("/") + 1;
		tmp = FilePath.substring(pos2);
		mLangID = tmp.replace(".lan", "");

		SelectedLangChangedEventList.Call();
	}

	private ArrayList<Translations> ReadFile(String FilePath) throws IOException
	{

		ArrayList<Translations> Temp = new ArrayList<Translations>();
		String line;

		// get Encoding

		BufferedReader reader;
		reader = new BufferedReader(new FileReader(FilePath));
		String encoding = reader.readLine().trim();

		BufferedReader Filereader;
		if (encoding == "utf8")
		{
			Filereader = new BufferedReader(new InputStreamReader(new FileInputStream(FilePath), "UTF8"));
		}
		else
		{
			Filereader = new BufferedReader(new InputStreamReader(new FileInputStream(FilePath)));
		}
		// Read and display lines from the file until the end of
		// the file is reached:
		while ((line = Filereader.readLine()) != null)
		{
			int pos;

			// skip empty lines
			if (line == "")
			{
				continue;
			}

			// skip comment line
			pos = line.indexOf("//");
			if (pos > -1)
			{
				continue;
			}

			// skip line without value
			pos = line.indexOf("=");
			if (pos == -1)
			{
				continue;
			}

			String readID = line.substring(0, pos);
			String readTransl = line.substring(pos + 1);
			String ReplacedRead = readTransl.trim().replace("\\n", String.format("%n"));
			Temp.add(new Translations(readID.trim(), ReplacedRead));
		}

		reader.close();
		Filereader.close();
		return Temp;
	}

	private String get(String StringId, String... params)
	{

		if (mStringList == null || mRefTranslation == null) return "Translation  not initial";

		String retString = "";
		for (Translations tmp : mStringList)
		{
			if (tmp.IdString.equals(StringId))
			{
				retString = tmp.Translation;
				break;
			}
		}

		if (retString == "")
		{
			for (Translations tmp : mRefTranslation)
			{
				if (tmp.IdString.equals(StringId))
				{
					retString = tmp.Translation;
					break;
				}
			}
		}

		if (retString == "")
		{
			retString = "$ID: " + StringId;// "No translation found";

			Translations notFound = new Translations(StringId, "??");
			if (!mMissingStringList.contains(notFound))
			{
				mMissingStringList.add(notFound);
			}

		}

		if (params != null && params.length > 0)
		{
			retString = replaceParams(retString, params);
		}

		return retString;
	}

	private String replaceParams(String retString, String... params)
	{
		int i = 1;
		for (String param : params)
		{
			if ((param.length() >= 1) && (param.charAt(0) == '$')) param = Get(param.substring(1));
			retString = retString.replace("{" + i + "}", param);
			i++;
		}
		return retString;
	}

	private ArrayList<Lang> getLangs(String FilePath)
	{
		ArrayList<Lang> Temp = new ArrayList<Lang>();

		File Dir = new File(FilePath);
		final String[] files;

		files = Dir.list();

		for (String tmp : files)
		{
			try
			{
				tmp = FilePath + "/" + tmp;

				String stringFile = tmp + "/strings.ini";

				if (FileUtil.FileExists(stringFile))
				{
					String tmpName = getLangNameFromFile(stringFile);
					Temp.add(new Lang(tmpName, stringFile));
				}

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}

		return Temp;
	}

	private void writeMisingStrings() throws IOException
	{
		File file = new File(mWorkPath + "/debug.txt");

		if (file.exists())
		{

			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line;

			boolean override = false;

			while ((line = reader.readLine()) != null)
			{
				if (!override) sb.append(line + BR);
				if (line.contains("##########  Missing Lang Strings ######"))
				{
					// Beginn des schreibbereichs
					for (Iterator<Translations> it = mMissingStringList.iterator(); it.hasNext();)
					{
						sb.append(it.next().IdString + BR);
					}
					override = true;
				}
				if (override && line.contains("############################"))
				{
					// jetzt kann weiter gelesen werden
					override = false;
					sb.append(line + BR);
				}
			}
			reader.close();

			// zurück schreiben
			PrintWriter writer = new PrintWriter(new FileWriter(file));

			writer.write(sb.toString());
			writer.close();

		}

	}

	private void readMissing() throws IOException
	{
		File file = new File(mWorkPath + "/debug.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;

		boolean read = false;

		while ((line = reader.readLine()) != null)
		{
			if (read && line.contains("############################")) break;

			if (read)
			{
				Translations notFound = new Translations(line, "??");
				if (!mMissingStringList.contains(notFound))
				{
					mMissingStringList.add(notFound);
				}
			}

			if (line.contains("##########  Missing Lang Strings ######")) read = true;

		}
		reader.close();

	}

	private String getTextFile(String Name, String overrideLangId) throws IOException
	{
		String FilePath = mWorkPath + "/data/string_files/" + Name + "." + overrideLangId + ".txt";

		if (!FileUtil.FileExists(FilePath))
		{
			FilePath = mWorkPath + "/data/string_files/" + Name + ".en.txt";
			if (!FileUtil.FileExists(FilePath))
			{
				return "File not found => " + Name;
			}
		}

		StringBuilder retSb = new StringBuilder();
		BufferedReader Filereader;
		Filereader = new BufferedReader(new InputStreamReader(new FileInputStream(FilePath), "UTF8"));

		String line;
		while ((line = Filereader.readLine()) != null)
		{
			retSb.append(line + String.format("%n"));
		}

		Filereader.close();
		return retSb.toString();
	}

}
