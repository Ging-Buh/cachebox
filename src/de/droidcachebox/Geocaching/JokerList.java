package de.droidcachebox.Geocaching;

import java.util.ArrayList;

import android.util.Log;

public class JokerList extends ArrayList<JokerEntry> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JokerList()
	{
	}
	
	
	public void AddJoker(String vorname, String name, String gclogin, String tage, String telefon, String bemerkung)
	{  // Telefonjoker zur Liste hinzuf�gen
	    try {
	        long l = Long.parseLong(tage.trim());
	  		JokerEntry je = new JokerEntry(vorname, name, gclogin, telefon, l, bemerkung);
	        this.add(je);
	       } 
	    catch (NumberFormatException nfe) {
	    	Log.d("DroidCachebox",nfe.getMessage());		
	       }
	}
	
	public void ClearList()
	{	//Telefonjoker Liste l�schen
		this.clear();

	}
	
}
