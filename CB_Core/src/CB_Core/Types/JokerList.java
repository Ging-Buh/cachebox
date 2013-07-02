package CB_Core.Types;

import java.util.ArrayList;

import CB_Core.Log.Logger;


public class JokerList extends ArrayList<JokerEntry> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JokerList()
	{
	}
	
	
	public void AddJoker(String vorname, String name, String gclogin, String tage, String telefon, String bemerkung)
	{  // Telefonjoker zur Liste hinzufügen
	    try {
	        long l = Long.parseLong(tage.trim());
	  		JokerEntry je = new JokerEntry(vorname, name, gclogin, telefon, l, bemerkung);
	        this.add(je);
	       } 
	    catch (NumberFormatException nfe) {
	    	Logger.Error("DroidCachebox", "AddJoker", nfe);		
	       }
	}
	
	public void ClearList()
	{	//Telefonjoker Liste löschen
		this.clear();

	}
	
}
