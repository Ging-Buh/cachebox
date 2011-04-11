package de.droidcachebox.Components;

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


}
