package de.droidcachebox.Components;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.text.TextPaint;

public class StringFunctions {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static boolean IsNullOrEmpty(String param) { 
	    return param == null || param.trim().length() == 0;
	}
	
	public static String newLine()
	{
		return String.format("%n");
	}

	  public static String TextWarp(String Text, int maxCaracters)
	    {
			return "";
	    
	    }
	
    public static String[] TextWarpArray(String Text, int maxCaracters)
    {
        String[] functionReturnValue = null;
        try
        {
            int WortZ�hler = 0;
            String[] Wort = GetWordArray(Text, WortZ�hler);
            //if (WortZ�hler == 0)
            //    return new String[]{Text};
            // nur ein Wort
            // In Zeilen aufteilen
            int AnzahlW�rter = WortZ�hler = Wort.length;
            String[] Zeile = new String[6];
            int ZeilenZ�hler = 0;
            int AnzahlZeilen = 0;
            WortZ�hler = 0;
            while (WortZ�hler < AnzahlW�rter)
            {
                String tmpZeile = "";
                while (tmpZeile.length() < (maxCaracters - Wort[WortZ�hler].length()))
                {
                    tmpZeile = tmpZeile + Wort[WortZ�hler] + " ";
                    WortZ�hler += 1;
                    if (WortZ�hler == AnzahlW�rter)
                        break; 
                }
                Zeile[ZeilenZ�hler] = tmpZeile.trim();
                ZeilenZ�hler += 1;
            }
            
            
            return Zeile;
          
   
        }
        catch(Exception e) 
        {
            functionReturnValue = new String[]{" "," "};
        }
        return functionReturnValue;
    }

    public static String[] GetWordArray(String Text, int WortZ�hler)
    {

        if (Text == null)
            return null;
        if (IsNullOrEmpty(Text))
            return new String[] { "" };

        String TextWorkCopy = Text.trim();

        // Einzelne W�rter ermitteln
        List<String> WortList = new ArrayList<String>();


        while (true)
        {
            int posLeer = TextWorkCopy.indexOf(" ");
            if (posLeer <= 0)
            {
                WortList.add(TextWorkCopy);
                break;
            }
            WortList.add(TextWorkCopy.substring(0, posLeer));   
            TextWorkCopy =TextWorkCopy.substring(posLeer,TextWorkCopy.length()).trim();

        }

        WortZ�hler = WortList.size();
        
        String[] retArray = new String[WortList.size()];
        
        int count =0;
        for (String tmp : WortList)
        {
        	retArray[count++]=tmp;
        }
        
        return retArray;

    }
	
	
	
	
	
	
	
	
	
	
	

}    

	
	
	


