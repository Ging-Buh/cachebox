package de.droidcachebox.Geocaching;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.droidcachebox.Database;
import de.droidcachebox.R;

import android.content.ContentValues;
import android.database.Cursor;

public class JokerEntry implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4110771837489396947L;
	
    /// Vorname
    public String Vorname;

    /// Name
    public String Name;

    /// Name
    public String GCLogin;

    /// Telefunnummer
    public String Telefon;

	/// Anzahl der Tage die der Fund her ist. -1 = Owner
    public long Tage;

    /// Bemerkung
    public String Bemerkung;
    


    public JokerEntry(String vorname, String name, String gclogin, String telefon, long tage, String bemerkung)
    {
    	Vorname = vorname;
    	Name = name;
    	GCLogin = gclogin;
    	Telefon = telefon;
    	Tage = tage;
    	Bemerkung = bemerkung;
    	
    }
    
    
    	
}
