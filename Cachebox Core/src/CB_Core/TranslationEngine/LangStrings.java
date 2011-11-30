package CB_Core.TranslationEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.Log.Logger;

public class LangStrings
{
	// / <summary>
	// / Eine Structure, welche die „ID“ als String und deren Text („Trans“)
	// aufnimmt.
	// / </summary>
	public class _Translations
	{
		// / <summary>
		// / Constructor
		// / </summary>
		// / <param name="ID">ID as String</param>
		// / <param name="Trans">Übersetzung</param>
		public _Translations(String ID, String Trans)
		{
			this.IdString = ID;
			this.Translation = Trans;
		}

		public String IdString;
		public String Translation;

	}

	// /<summary>
	// / Eine Structure, welche den Namen und deren Pfad aufnimmt
	// /</summery>
	public class Langs
	{
		public Langs(String Name, String Pfad)
		{
			this.Name = Name;
			this.Path = Pfad;
		}

		public String Name;
		public String Path;
	}

	public ArrayList<_Translations> _StringList = new ArrayList<_Translations>();
	private ArrayList<_Translations> _RefTranslation;

	public ArrayList<_Translations> _MissingStringList = new ArrayList<_Translations>();

	// / <summary>
	// / Gibt den Namen der angegebenen Sprach-Datei zurück.
	// / </summary>
	// / <param name="FilePath">Voller Pfad zur Sprach Datei.</param>
	// / <returns>Name der Sprach-Datei</returns>
	public String getLangNameFromFile(String FilePath) throws IOException
	{

		BufferedReader reader;
		reader = new BufferedReader(new FileReader(FilePath));
		String Value = reader.readLine().trim();
		reader.close();
		return Value;
	}

	// / <summary>
	// / Liest die angegebene Sprach-Datei ein.
	// / </summary>
	// / <param name="FilePath">Voller Pfad zur Sprach Datei.</param>
	public void ReadTranslationsFile(String FilePath) throws IOException
	{
		if (FilePath.equals(""))
		{
			return;
		}

		if (_RefTranslation == null)
		{
			int pos = FilePath.lastIndexOf("/") + 1;
			String LangFileName = FilePath.substring(pos);
			String RefPath = FilePath.replace(LangFileName, "en.lan");
			_RefTranslation = ReadFile(RefPath);
		}
		if (FilePath.endsWith("lang")) FilePath = FilePath.replace(".lang", ".lan");
		_StringList = ReadFile(FilePath);

		String tmp = FilePath;
		int pos2 = tmp.lastIndexOf("/") + 1;
		tmp = FilePath.substring(pos2);
		LangID = tmp.replace(".lan", "");

		SelectedLangChangedEventList.Call();
	}

	private ArrayList<_Translations> ReadFile(String FilePath) throws IOException
	{

		ArrayList<_Translations> Temp = new ArrayList<_Translations>();
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

			// skip komment line
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

			String readID = line.substring(0, pos - 1);
			String readTransl = line.substring(pos + 1);
			String ReplacedRead = readTransl.trim().replace("\\n", String.format("%n"));
			Temp.add(new _Translations(readID.trim(), ReplacedRead));
		}

		return Temp;
	}

	// / <summary>
	// / Gibt die Übersetzung der geladenen Sprach-Datei anhand der ID zurück.
	// / </summary>
	// / <param name="StringId">ID der Übersetzung</param>
	// / <returns>Übersetzung</returns>
	public String Get(String StringId)
	{
		return Get(StringId, false);
	}

	public String Get(String StringId, String... params)
	{
		return Get(StringId, false, params);
	}

	public String Get(String StringId, Boolean withoutRef, String... params)
	{

		// chk initial
		if (_StringList == null || _RefTranslation == null)
		{
			try
			{
				ReadTranslationsFile(Config.settings.Sel_LanguagePath.getValue());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (_StringList == null || _RefTranslation == null) return "Translation  not initial";

		String retString = "";
		for (_Translations tmp : _StringList)
		{
			if (tmp.IdString.equals(StringId))
			{
				retString = tmp.Translation;
				break;
			}
		}

		if (retString == "" && !withoutRef)
		{
			for (_Translations tmp : _RefTranslation)
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

			_Translations notFound = new _Translations(StringId, "??");
			if (!_MissingStringList.contains(notFound))
			{
				_MissingStringList.add(notFound);
			}

		}

		if (params != null)
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
			retString = retString.replace("{" + i + "}", param);
			i++;
		}
		return retString;
	}

	public ArrayList<Langs> GetLangs(String FilePath)
	{
		ArrayList<Langs> Temp = new ArrayList<Langs>();

		File Dir = new File(FilePath);
		final ArrayList<String> files = new ArrayList<String>();
		Dir.listFiles(new FileFilter()
		{

			public boolean accept(File f)
			{
				Object Path = f.getAbsolutePath();
				files.add((String) Path);
				return false;
			}
		});

		for (String tmp : files)
		{
			try
			{
				String tmpName = getLangNameFromFile(tmp);
				Temp.add(new Langs(tmpName, tmp));
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return Temp;
	}

	private String LangID = "";

	public String getLangId()
	{
		return LangID;
	}

	public void writeMisingStringsFile()
	{
		Logger.DEBUG("List of missing lang strings:");
		for (Iterator<_Translations> it = _MissingStringList.iterator(); it.hasNext();)
		{
			Logger.DEBUG(it.next().IdString);
		}

	}
}
