package CB_Core.GL_UI.Controls.MessageBox;

import CB_Core.Events.platformConector;

/**
 * Zeigt ein Meldungsfeld an, das Text, Schaltfl�chen und Symbole mit Informationen und Anweisungen f�r den Benutzer enthalten kann.
 * Entspricht in etwa der C# .NET Klasse. Da nicht auf ein Result gewartet werden kann, muss ein DialogInterface.OnClickListener() �bergeben
 * werden.
 * 
 * @author Longri
 */
public class MsgBox
{
	private static OnClickListener listner;

	/**
	 * Interface used to allow the creator of a dialog to run some code when an item on the dialog is clicked..
	 */
	public interface OnClickListener
	{
		/**
		 * This method will be invoked when a button in the dialog is clicked.
		 * 
		 * @param which
		 *            The button that was clicked ( the position of the item clicked.
		 * @return
		 */
		public boolean onClick(int which);
	}

	/**
	 * Zeigt ein Meldungsfeld mit angegebenem Text an. Im Meldungsfeld wird standardm��ig die Schaltfl�che OK angezeigt. Das Meldungsfeld
	 * enth�lt keine Beschriftung im Titel und kein Icon.
	 * 
	 * @param msg
	 *            Die Message, welche ausgegeben werden soll.
	 * 
	 *            <pre>
	 * Beispiel:</b>
	 * {@code
	 * MessageBox.Show("Test");
	 * }
	 * </pre>
	 */
	public static void Show(String msg)
	{
		listner = null;
		platformConector.Msg.Show(msg);

	}

	/**
	 * Zeigt ein Meldungsfeld mit angegebenem Text an. Im Meldungsfeld wird standardm��ig die Schaltfl�che OK angezeigt. Das Meldungsfeld
	 * enth�lt keine Beschriftung im Titel und kein Icon.
	 * 
	 * @param msg
	 *            Die Message, welche ausgegeben werden soll.
	 * @param Listener
	 *            Welcher die Events der Buttons behandelt
	 * 
	 *            <pre>
	 * <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
	 * {@code
	 * MessageBox.Show("Test",null);
	 * }
	 * </pre>
	 * 
	 *            <pre>
	 * <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() ben�tigt:</b>
	 * {@code
	 * 	MessageBox.Show("Test",DialogListner)
	 * 	
	 *  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 *   {
	 * 	@Override
	 * 	public void onClick(DialogInterface dialog, int button) 
	 * 	{
	 * 		// Behandle das ergebniss
	 * 		switch (button)
	 * 		{
	 * 			case -1:
	 * 				Toast.makeText(mainActivity, "Click Button 1", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 			case -2:
	 * 				Toast.makeText(mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 			case -3:
	 * 				Toast.makeText(mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 		}
	 * 		
	 * 		dialog.dismiss();
	 * 	}
	 * 	
	 *    };
	 * }
	 * </pre>
	 */
	public static void Show(String msg, OnClickListener Listener)
	{
		listner = Listener;
		platformConector.Msg.Show(msg, Listener);
	}

	/**
	 * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. Im Meldungsfeld wird standardm��ig die
	 * Schaltfl�che OK angezeigt. Das Meldungsfeld enth�lt kein Icon.
	 * 
	 * @param msg
	 *            Der im Meldungsfeld anzuzeigende Text.
	 * @param title
	 *            Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
	 * @param Listener
	 *            Welcher die Events der Buttons behandelt
	 * 
	 *            <pre>
	 * <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
	 * {@code
	 * MessageBox.Show("Test", "Titel" ,null);
	 * }
	 * </pre>
	 * 
	 *            <pre>
	 * <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() ben�tigt:</b>
	 * {@code
	 * 	MessageBox.Show("Test", "Titel",DialogListner)
	 * 	
	 *  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 *   {
	 * 	@Override
	 * 	public void onClick(DialogInterface dialog, int button) 
	 * 	{
	 * 		// Behandle das ergebniss
	 * 		switch (button)
	 * 		{
	 * 			case -1:
	 * 				Toast.makeText(mainActivity, "Click Button 1", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 			case -2:
	 * 				Toast.makeText(mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 			case -3:
	 * 				Toast.makeText(mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 		}
	 * 		
	 * 		dialog.dismiss();
	 * 	}
	 * 	
	 *    };
	 * }
	 * </pre>
	 */
	public static void Show(String msg, String title, OnClickListener Listener)
	{
		listner = Listener;
		platformConector.Msg.Show(msg, title, Listener);
	}

	/**
	 * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. Im Meldungsfeld wird standardm��ig die
	 * Schaltfl�che OK angezeigt. Das Meldungsfeld enth�lt kein Icon.
	 * 
	 * @param msg
	 *            Der im Meldungsfeld anzuzeigende Text.
	 * @param title
	 *            Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
	 * @param buttons
	 *            Ein MessageBoxButtons-Wert, der angibt, welche Schaltfl�chen im Meldungsfeld angezeigt werden sollen.
	 * @param Listener
	 *            Welcher die Events der Buttons behandelt
	 * 
	 *            <pre>
	 * <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
	 * {@code
	 * MessageBox.Show("Test", "Titel" ,null);
	 * }
	 * </pre>
	 * 
	 *            <pre>
	 * <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() ben�tigt:</b>
	 * {@code
	 * 	MessageBox.Show("Test", "Titel",DialogListner)
	 * 	
	 *  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 *   {
	 * 	@Override
	 * 	public void onClick(DialogInterface dialog, int button) 
	 * 	{
	 * 		// Behandle das ergebniss
	 * 		switch (button)
	 * 		{
	 * 			case -1:
	 * 				Toast.makeText(mainActivity, "Click Button 1", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 			case -2:
	 * 				Toast.makeText(mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 			case -3:
	 * 				Toast.makeText(mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 		}
	 * 		
	 * 		dialog.dismiss();
	 * 	}
	 * 	
	 *    };
	 * }
	 * </pre>
	 */
	public static void Show(String msg, String title, MessageBoxButtons buttons, OnClickListener Listener)
	{
		listner = Listener;
		platformConector.Msg.Show(msg, title, buttons, Listener);
	}

	/**
	 * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. Im Meldungsfeld wird standardm��ig die
	 * Schaltfl�che OK angezeigt. Das Meldungsfeld enth�lt kein Icon.
	 * 
	 * @param msg
	 *            Der im Meldungsfeld anzuzeigende Text.
	 * @param title
	 *            Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
	 * @param buttons
	 *            Ein MessageBoxButtons-Wert, der angibt, welche Schaltfl�chen im Meldungsfeld angezeigt werden sollen.
	 * @param Listener
	 *            Welcher die Events der Buttons behandelt
	 * 
	 *            <pre>
	 * <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
	 * {@code
	 * MessageBox.Show("Test", "Titel" ,null);
	 * }
	 * </pre>
	 * 
	 *            <pre>
	 * <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() ben�tigt:</b>
	 * {@code
	 * 	MessageBox.Show("Test", "Titel",DialogListner)
	 * 	
	 *  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 *   {
	 * 	@Override
	 * 	public void onClick(DialogInterface dialog, int button) 
	 * 	{
	 * 		// Behandle das ergebniss
	 * 		switch (button)
	 * 		{
	 * 			case -1:
	 * 				Toast.makeText(mainActivity, "Click Button 1", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 			case -2:
	 * 				Toast.makeText(mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 			case -3:
	 * 				Toast.makeText(mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
	 * 				break;
	 * 		}
	 * 		
	 * 		dialog.dismiss();
	 * 	}
	 * 	
	 *    };
	 * }
	 * </pre>
	 */
	public static void Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnClickListener Listener)
	{
		listner = Listener;
		platformConector.Msg.Show(msg, title, buttons, icon, Listener);
	}

	public static void Show(String msg, String title, MessageBoxIcon icon)
	{
		Show(msg, title, MessageBoxButtons.OK, icon, null);

	}
}
