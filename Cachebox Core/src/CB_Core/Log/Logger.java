package CB_Core.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Der Logger basiert auf einem Interface als CallBack und kann damit auch von nicht GUI Klassen implementiert werden, 
 * damit sie einen Fehler Melden können.
 * 
 * Wenn keine GUI zur Darstellung diese Meldung empfängt, ist es halt so.
 * Also benutzt so häufig wie möglich den Logger in euren Klassen, dass wird uns helfen Fehler aus der debug.txt auszulesen.

 * @author Longri
 *
 */
public class Logger 
{
	
	private static Boolean mDebug;
	private static ArrayList<ILog> list = new ArrayList<ILog>();
	
	/**
	 * Wird der Debug-Modus aktiviert (true), werden alle Meldungen weitergeleitet.
	 * Ansonsten werden die Debug Meldungen unterdrückt.
	 * 
	 * Default = false
	 * 
	 * @param debug
	 */
	public static void setDebug(Boolean debug)
	{
		mDebug = debug;
	}
	

	/**
	 * Registriert eine Klasse zum Empfang  der LogMeldungen
	 * @param event
	 * 
	 * <pre> Beispiel:</b>
     * 
     * Log.Add(<span style=' color: Blue;'>this</span>); 
     * 
     * </pre>
	 */
	public static void Add(ILog event)
	{
		list.add(event);	
	}
	
	/**
	 * Löscht eine Registrierung zum Empfang  von LogMeldungen
	 * @param event
	 * <pre> Beispiel:</b>
     * 
     * Log.Remove(<span style=' color: Blue;'>this</span>); 
     * 
     * </pre>
	 */
	public static void Remove(ILog event)
	{
		list.remove(event);	
	}
	
	/**
	 * Meldet einen Fehler
	 * @param Name (Name der Klasse und Methode in der der Fehler auftrat.)
	 * @param Msg  (Meldung die übergeben werden soll.)
	 * @param e    (Exeption die abgefangen wurde. null ist möglich!)
	 * 
	 * 
	 * <pre>    
	 * <span style=' color: Blue;'>try</span> 
     *{
     *    mp.setDataSource(Config.WorkPath + <span style=' color: Maroon;'>"/data/sound/"</span> + soundFile);
     *    mp.prepare();
     *} 
     *<span style=' color: Blue;'>catch</span> (Exception e) 
     *{
     *    Log.Error(<span style=' color: Maroon;'>"Global.PlaySound()"</span>, Config.WorkPath + <span style=' color: Maroon;'>"/data/sound/"</span> + soundFile ,e);
     *    e.printStackTrace();
     *}   
     *</pre>
	 * 
	 */
	public static void Error(String Name,String Msg, Exception e)
	{
		String Ex = "";
		if(e != null && e.getMessage()!=null)
			Ex = "Ex = [" + e.getMessage() + "]";
		
		String Short = "[ERR]" + Name + " [" + Msg + "] ";
		Msg = "[ERROR]- at " + Name + "- [" + Msg + "] " + Ex  ;
		
		sendMsg(Msg,Short);
	}
	
	
	/**
	 * Meldet eine generelle Msg
	 * @param Msg
	 */
	public static void General(String Msg) 
	{
		String Short = "[GEN] [" + Msg + "] " ;
		Msg = "[GENERAL]- [" + Msg + "] " ;
		
		sendMsg(Msg,Short);
	}
	
	
	/**
	 * Meldet eine Msg nur wenn der Debug-Modus aktiviert ist.
	 * @param Msg
	 */
	public static void DEBUG(String Msg) 
	{
		if(mDebug)
		{
			Msg = "[DEBUG]- [" + Msg + "] " ;
			String Short = "[DEB] [" + Msg + "] " ;
			sendMsg(Msg,Short);
		}
	}
	
	
	/**
	 * Sendet die aufbereitete Msg von Error,Debug oder General
	 * @param Msg
	 */
	private static void sendMsg(String Msg , String Short)
	{
		
		//add Timestamp 
		Date now = new Date();
		
	       SimpleDateFormat postFormater = new SimpleDateFormat("mm:ss"); 
	       String dateString = postFormater.format(now); 
	       
	       SimpleDateFormat postFormater2 = new SimpleDateFormat("dd/MM hh:mm:ss"); 
	       String dateString2 = postFormater2.format(now); 
		
	       Short = dateString + Short + "\n";
	       Msg = dateString2 + " - " + Msg + "\n";
		
		
		for (ILog event : list)
		{
        	event.receiveLog(Msg);
        	event.receiveShortLog(Short);
		}
	}


	
}
