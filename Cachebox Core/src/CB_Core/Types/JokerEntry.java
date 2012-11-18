package CB_Core.Types;

import java.io.Serializable;

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
