package de.droidcachebox.TranslationEngine;

import java.io.*;
import java.util.ArrayList;
import de.droidcachebox.Components.StringFunctions;
import android.os.Environment;

public class LangStrings 
{
	 /// <summary>
    /// Eine Structure, welche die „ID“ als String und deren Text („Trans“) aufnimmt.
    /// </summary>
    public class _Translations
    {
        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="ID">ID as String</param>
        /// <param name="Trans">Übersetzung</param>
        public _Translations(String ID, String Trans)
        {
            this.IdString = ID;
            this.Translation = Trans;
        }
        public String IdString;
        public String Translation;
    }

    public ArrayList<_Translations> _StringList = new ArrayList<_Translations>();
    private ArrayList<_Translations> _RefTranslation;

   
    // <summary>
    // Wird ausgelöst, wenn sich die geladene Sprach-Datei geändert hat. 
    // </summary>
  //  public event languageChangedEventHandler LangChanged;
   

    /// <summary>
    /// Gibt den Namen der angegebenen Sprach-Datei zurück.
    /// </summary>
    /// <param name="FilePath">Voller Pfad zur Sprach Datei.</param>
    /// <returns>Name der Sprach-Datei</returns>
    public String getLangNameFromFile(String FilePath) throws IOException
    {
    	String ApplicationPath = Environment.getExternalStorageDirectory().getAbsolutePath();
       
    	 BufferedReader reader;
    	 reader = new BufferedReader(new FileReader(ApplicationPath + FilePath));
    	 return reader.readLine().trim();
    }

    /// <summary>
    /// Liest die angegebene Sprach-Datei ein.
    /// </summary>
    /// <param name="FilePath">Voller Pfad zur Sprach Datei.</param>
    public void ReadTranslationsFile(String FilePath) throws IOException
    {
        if (StringFunctions.IsNullOrEmpty(FilePath)) { return; }

        if (_RefTranslation == null)
        {
            int pos = FilePath.lastIndexOf("\\")+1;
            String RefPath = FilePath.regionMatches(pos, "", pos, FilePath.length() - pos) + "en.lan";
            _RefTranslation = ReadFile(RefPath);
        }
        if (FilePath.endsWith("lang"))
            FilePath = FilePath.replace(".lang", ".lan");
        _StringList = ReadFile(FilePath);

     // TODO   if (LangChanged != null) { LangChanged(); } // Fire changed event if not null
    }

    private ArrayList<_Translations> ReadFile(String FilePath) throws IOException
    {
    	String ApplicationPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    	ArrayList<_Translations> Temp = new ArrayList<_Translations>();
        String line;

        // get Encoding
        
        BufferedReader reader;
   	    reader = new BufferedReader(new FileReader(ApplicationPath + FilePath));
   	    String encoding =reader.readLine().trim();
   	             

   	    BufferedReader Filereader;
        if (encoding == "utf8")
        {
        	Filereader = new BufferedReader( new InputStreamReader(new FileInputStream(ApplicationPath + FilePath), "UTF8"));
        }
        else
        {
        	Filereader = new BufferedReader(new InputStreamReader(new FileInputStream(ApplicationPath + FilePath)));
        }
            // Read and display lines from the file until the end of 
            // the file is reached:
            while ((line = Filereader.readLine()) != null)
            {
                int pos;

                //skip empty lines
                if (line == "") { continue; }

                //skip komment line
                pos = line.indexOf("//");
                if (pos > -1) 
                { 
                    continue; 
                }

                // skip line without value
                pos = line.indexOf("=");
                if (pos == -1) { continue; }

                String readID = line.substring(0, pos - 1);
                String readTransl = line.substring(pos + 1);
                String ReplacedRead = readTransl.trim().replace("\\n", StringFunctions.newLine());
                Temp.add(new _Translations(readID.trim(), ReplacedRead));
            }
        
        return Temp;
    }

    /// <summary>
    /// Gibt die Übersetzung der geladenen Sprach-Datei anhand der ID zurück.
    /// </summary>
    /// <param name="StringId">ID der Übersetzung</param>
    /// <returns>Übersetzung</returns>
    public String Get(String StringId)
    {
        return Get(StringId, false);
    }


    public String Get(String StringId,Boolean withoutRef)
    {
        String retString = "";
        for (_Translations tmp : _StringList)
        {
            if (tmp.IdString == StringId)
            {
                retString = tmp.Translation;
                break;
            }
        }

        if (retString == "" && !withoutRef )
        {
            for (_Translations tmp : _RefTranslation)
            {
                if (tmp.IdString == StringId)
                {
                    retString = tmp.Translation;
                    break;
                }
            }
        }

        if (retString == "")
        {
            retString = "No translation found";
        }

        return retString;
    }
}
