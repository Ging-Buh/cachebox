package de.droidcachebox.TranslationEngine;

import java.io.*;
import java.util.ArrayList;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.StringFunctions;
import de.droidcachebox.Events.SelectedLangChangedEventList;

import android.os.Environment;
import android.view.ContextMenu;
import android.view.MenuItem;

public class LangStrings 
{
	 /// <summary>
    /// Eine Structure, welche die �ID� als String und deren Text (�Trans�) aufnimmt.
    /// </summary>
    public class _Translations
    {
        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="ID">ID as String</param>
        /// <param name="Trans">�bersetzung</param>
        public _Translations(String ID, String Trans)
        {
            this.IdString = ID;
            this.Translation = Trans;
        }
        public String IdString;
        public String Translation;
        
    }

    
    ///<summary>
    /// Eine Structure, welche den Namen und deren Pfad aufnimmt
    ///</summery>
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

   
    // <summary>
    // Wird ausgel�st, wenn sich die geladene Sprach-Datei ge�ndert hat. 
    // </summary>
  //  public event languageChangedEventHandler LangChanged;
   

    /// <summary>
    /// Gibt den Namen der angegebenen Sprach-Datei zur�ck.
    /// </summary>
    /// <param name="FilePath">Voller Pfad zur Sprach Datei.</param>
    /// <returns>Name der Sprach-Datei</returns>
    public String getLangNameFromFile(String FilePath) throws IOException
    {
    	
    	 BufferedReader reader;
    	 reader = new BufferedReader(new FileReader(FilePath));
    	 String Value = reader.readLine().trim();
    	 reader.close();
    	 return Value;
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
            int pos = FilePath.lastIndexOf("/")+1;
            String LangFileName = FilePath.substring(pos);
            String RefPath = FilePath.replace(LangFileName, "en.lan");
            _RefTranslation = ReadFile(RefPath);
        }
        if (FilePath.endsWith("lang"))
            FilePath = FilePath.replace(".lang", ".lan");
        _StringList = ReadFile(FilePath);

        SelectedLangChangedEventList.Call();
    }

    private ArrayList<_Translations> ReadFile(String FilePath) throws IOException
    {
    	String ApplicationPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    	ArrayList<_Translations> Temp = new ArrayList<_Translations>();
        String line;

        // get Encoding
        
        BufferedReader reader;
   	    reader = new BufferedReader(new FileReader(FilePath));
   	    String encoding =reader.readLine().trim();
   	             

   	    BufferedReader Filereader;
        if (encoding == "utf8")
        {
        	Filereader = new BufferedReader( new InputStreamReader(new FileInputStream( FilePath), "UTF8"));
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
    /// Gibt die �bersetzung der geladenen Sprach-Datei anhand der ID zur�ck.
    /// </summary>
    /// <param name="StringId">ID der �bersetzung</param>
    /// <returns>�bersetzung</returns>
    public String Get(String StringId)
    {
        return Get(StringId, false);
    }


    public String Get(String StringId,Boolean withoutRef)
    {
        String retString = "";
        for (_Translations tmp : _StringList)
        {
            if (tmp.IdString.equals(StringId))
            {
                retString = tmp.Translation;
                break;
            }
        }

        if (retString == "" && !withoutRef )
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
            retString = "No translation found";
        }

        return retString;
    }

    // �bersetzt den Titel eines MenuItems
    public void TranslateMenuItem(ContextMenu menu, int id, String StringId)
    {
    	try
    	{
    		MenuItem mi = menu.findItem(id);
    		if (mi != null)
    			mi.setTitle(Global.Translations.Get(StringId));
    	} catch (Exception exc)
    	{ }
    }
    
    public ArrayList<Langs> GetLangs(String FilePath)
    {
    	ArrayList<Langs> Temp = new ArrayList<Langs>();
    	
    	File Dir = new File(FilePath);
    	final ArrayList<String> files = new ArrayList<String>();
    	Dir.listFiles(new FileFilter(){
    	 
    	public boolean accept(File f) { Object Path = f.getAbsolutePath();
    	files.add((String) Path); return false;}});
    	 
    	for (String tmp : files)
    	{
    		try {
				String tmpName = getLangNameFromFile(tmp);
				Temp.add(new Langs(tmpName,tmp));
    			} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
    	}
    	
    	return Temp;
    }


}
