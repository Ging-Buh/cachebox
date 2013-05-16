package CB_Core.Log;

/**
 * Klassen die dieses Interface Implementieren, empfangen eine Msg wann auch immer eine Meldung abgesetzt wurde.
 * 
 * @author Longri
 */
public interface ILog
{
	public void receiveLog(String Msg);

	public void receiveShortLog(String Msg);

	public void receiveLogCat(String Msg);
}
