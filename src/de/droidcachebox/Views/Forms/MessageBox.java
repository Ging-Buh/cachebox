package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.main;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.app.Dialog;

/**
 * Zeigt ein Meldungsfeld an, das Text, Schaltflächen und Symbole mit Informationen und Anweisungen für den Benutzer enthalten kann. 
 * 
 * Entspricht in etwa der C# .NET Klasse.
 * 
 * Da nicht auf ein Result gewartet werden kann, muss ein DialogInterface.OnClickListener()
 * übergeben werden.
 * 
 * @author Longri
 *
 */
public class MessageBox 
{
	private static DialogInterface.OnClickListener listner;
	public static final int SHOW1 	= 1;
	public static final int SHOW2 	= 2;
	public static final int SHOW3 	= 3;
	public static final int SHOW4 	= 4;
	
	/**
	 * Zeigt ein Meldungsfeld mit angegebenem Text an.
	 * 
	 * Im Meldungsfeld wird standardmäßig die Schaltfläche OK angezeigt. 
	 * Das Meldungsfeld enthält keine Beschriftung im Titel und kein Icon.
	 * 
	 * @param msg Die Message, welche ausgegeben werden soll.
     * @param Listener Welcher die Events der Buttons behandelt
     * 
     * 
     * <pre> <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
     * {@code
     * MessageBox.Show("Test",null);
     * }
     * </pre>
     * 
     *<pre>
     * <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
     * {@code
     * 	MessageBox.Show("Test",DialogListner)
     * 	
     *  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 *   {
	 *		@Override
	 *		public void onClick(DialogInterface dialog, int button) 
	 *		{
	 *			// Behandle das ergebniss
	 *			switch (button)
	 *			{
	 *				case -1:
	 *					Toast.makeText(mainActivity, "Click Button 1", Toast.LENGTH_SHORT).show();
	 *					break;
	 *				case -2:
	 *					Toast.makeText(mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
	 *					break;
	 *				case -3:
	 *					Toast.makeText(mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
	 *					break;
	 *			}
	 *			
	 *			dialog.dismiss();
	 *		}
	 *		
	 *    };
	 * }
	 * </pre>
     * 
     * 
	 */
	public static void Show (String msg, DialogInterface.OnClickListener Listener)
	{
		listner = Listener;
		Bundle b = new Bundle();
        b.putString("msg",msg);
        main.mainActivity.showDialog(SHOW1,b);
	}
	
	
	
	/**
	 * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. 
	 * 
	 * Im Meldungsfeld wird standardmäßig die Schaltfläche OK angezeigt. 
	 * Das Meldungsfeld enthält kein Icon.
	 * 
	 * @param msg Der im Meldungsfeld anzuzeigende Text. 
	 * @param title Der in der Titelleiste des Meldungsfelds anzuzeigende Text. 
     * @param Listener Welcher die Events der Buttons behandelt
     * 
     * 
     * <pre> <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
     * {@code
     * MessageBox.Show("Test", "Titel" ,null);
     * }
     * </pre>
     * 
     *<pre>
     * <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
     * {@code
     * 	MessageBox.Show("Test", "Titel",DialogListner)
     * 	
     *  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 *   {
	 *		@Override
	 *		public void onClick(DialogInterface dialog, int button) 
	 *		{
	 *			// Behandle das ergebniss
	 *			switch (button)
	 *			{
	 *				case -1:
	 *					Toast.makeText(mainActivity, "Click Button 1", Toast.LENGTH_SHORT).show();
	 *					break;
	 *				case -2:
	 *					Toast.makeText(mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
	 *					break;
	 *				case -3:
	 *					Toast.makeText(mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
	 *					break;
	 *			}
	 *			
	 *			dialog.dismiss();
	 *		}
	 *		
	 *    };
	 * }
	 * </pre>
     * 
     * 
	 */
	public static void Show (String msg, String title, DialogInterface.OnClickListener Listener)
	{
		listner = Listener;
		Bundle b = new Bundle();
        b.putString("msg",msg);
        b.putString("title",title);
        main.mainActivity.showDialog(SHOW2,b);
	}
	
	
	
	
	/**
	 * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. 
	 * 
	 * Im Meldungsfeld wird standardmäßig die Schaltfläche OK angezeigt. 
	 * Das Meldungsfeld enthält kein Icon.
	 * 
	 * @param msg Der im Meldungsfeld anzuzeigende Text. 
	 * @param title Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
	 * @param buttons  Ein MessageBoxButtons-Wert, der angibt, welche Schaltflächen im Meldungsfeld angezeigt werden sollen. 
     * @param Listener Welcher die Events der Buttons behandelt
     * 
     * 
     * <pre> <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
     * {@code
     * MessageBox.Show("Test", "Titel" ,null);
     * }
     * </pre>
     * 
     *<pre>
     * <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
     * {@code
     * 	MessageBox.Show("Test", "Titel",DialogListner)
     * 	
     *  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 *   {
	 *		@Override
	 *		public void onClick(DialogInterface dialog, int button) 
	 *		{
	 *			// Behandle das ergebniss
	 *			switch (button)
	 *			{
	 *				case -1:
	 *					Toast.makeText(mainActivity, "Click Button 1", Toast.LENGTH_SHORT).show();
	 *					break;
	 *				case -2:
	 *					Toast.makeText(mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
	 *					break;
	 *				case -3:
	 *					Toast.makeText(mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
	 *					break;
	 *			}
	 *			
	 *			dialog.dismiss();
	 *		}
	 *		
	 *    };
	 * }
	 * </pre>
     * 
     * 
	 */
	public static void Show (String msg, String title, MessageBoxButtons buttons, DialogInterface.OnClickListener Listener)
	{
		listner = Listener;
		Bundle b = new Bundle();
        b.putString("msg",msg);
        b.putString("title",title);
        b.putInt("buttons", buttons.ordinal());
        main.mainActivity.showDialog(SHOW3,b);
	}
	
	
	/**
	 * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. 
	 * 
	 * Im Meldungsfeld wird standardmäßig die Schaltfläche OK angezeigt. 
	 * Das Meldungsfeld enthält kein Icon.
	 * 
	 * @param msg Der im Meldungsfeld anzuzeigende Text. 
	 * @param title Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
	 * @param buttons  Ein MessageBoxButtons-Wert, der angibt, welche Schaltflächen im Meldungsfeld angezeigt werden sollen. 
     * @param Listener Welcher die Events der Buttons behandelt
     * 
     * 
     * <pre> <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
     * {@code
     * MessageBox.Show("Test", "Titel" ,null);
     * }
     * </pre>
     * 
     *<pre>
     * <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
     * {@code
     * 	MessageBox.Show("Test", "Titel",DialogListner)
     * 	
     *  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 *   {
	 *		@Override
	 *		public void onClick(DialogInterface dialog, int button) 
	 *		{
	 *			// Behandle das ergebniss
	 *			switch (button)
	 *			{
	 *				case -1:
	 *					Toast.makeText(mainActivity, "Click Button 1", Toast.LENGTH_SHORT).show();
	 *					break;
	 *				case -2:
	 *					Toast.makeText(mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
	 *					break;
	 *				case -3:
	 *					Toast.makeText(mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
	 *					break;
	 *			}
	 *			
	 *			dialog.dismiss();
	 *		}
	 *		
	 *    };
	 * }
	 * </pre>
     * 
     * 
	 */
	public static void Show (String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, DialogInterface.OnClickListener Listener)
	{
		listner = Listener;
		Bundle b = new Bundle();
        b.putString("msg",msg);
        b.putString("title",title);
        b.putInt("buttons", buttons.ordinal());
        b.putInt("icon", icon.ordinal());
        main.mainActivity.showDialog(SHOW4,b);
	}
	
	
	
	
	
	
	
	
	static String button1="";
	static String button2="";
	static String button3="";
	static Drawable icon = null;
	
	public static Dialog CreateDialog(int dialogId, Bundle b) 
	{
    	if(listner==null) // setze standard Listner zu schliessen des Dialogs, falls kein LÖistner angegeben wurde
		{
			listner = new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
				}
			};
		}
		
		
    	Dialog dialog = null;
    	switch (dialogId) 
    	{
	    	case MessageBox.SHOW1 :
	    		message_box_dialog.Builder customBuilder = new
	    		message_box_dialog.Builder(main.mainActivity);
				customBuilder	.setTitle("")
								.setMessage(b.getString("msg"))
								.setPositiveButton(Global.Translations.Get("ok"),listner);
	            dialog = customBuilder.create();
	    		break;
	    
	    	case MessageBox.SHOW2 :
	    		message_box_dialog.Builder customBuilder2 = new
	    		message_box_dialog.Builder(main.mainActivity);
				customBuilder2	.setTitle(b.getString("title"))
								.setMessage(b.getString("msg"))
								.setPositiveButton(Global.Translations.Get("ok"),listner);
	            dialog = customBuilder2.create();
	    		break;
	    		
	    	case MessageBox.SHOW3 :
	    		
			setButtonCaptions(b);
	    		message_box_dialog.Builder customBuilder3 = new
	    		message_box_dialog.Builder(main.mainActivity);
				customBuilder3	.setTitle(b.getString("title"))
								.setMessage(b.getString("msg"))
								.setPositiveButton(button1,listner)
								.setNeutralButton(button2,listner)
								.setNegativeButton(button3,listner);
	            dialog = customBuilder3.create();
	    		break;
	    		
	    		
	    	case MessageBox.SHOW4 :
	    		
				setButtonCaptions(b);
				setIcon(b); 
		    		message_box_dialog.Builder customBuilder4 = new
		    		message_box_dialog.Builder(main.mainActivity);
					customBuilder4	.setTitle(b.getString("title"))
									.setMessage(b.getString("msg"))
									.setPositiveButton(button1,listner)
									.setNeutralButton(button2,listner)
									.setNegativeButton(button3,listner)
									.setIcon(icon);
		            dialog = customBuilder4.create();
		    		break;
	    		
    	}
    	return dialog;
    }



	/**
	 * @param b
	 */
	private static void setButtonCaptions(Bundle b) {
		int button = b.getInt("buttons");
		if(button == 0)
		{
			button1=Global.Translations.Get("abort");
			button2=Global.Translations.Get("retry");
			button3=Global.Translations.Get("ignore");
		}
		else if(button == 1)
		{
			button1=Global.Translations.Get("ok");
			button2="";
			button3="";
		}
		else if(button == 2)
		{
			button1=Global.Translations.Get("ok");
			button2="";
			button3=Global.Translations.Get("cancel");
		}
		else if(button == 3)
		{
			button1=Global.Translations.Get("retry");
			button2="";
			button3=Global.Translations.Get("cancel");
		}
		else if(button == 4)
		{
			button1=Global.Translations.Get("yes");
			button2="";
			button3=Global.Translations.Get("no");
		}
		else if(button == 5)
		{
			button1=Global.Translations.Get("yes");
			button2=Global.Translations.Get("no");
			button3=Global.Translations.Get("cancel");
		}
	}
	
	/**
	 * @param b
	 */
	private static void setIcon(Bundle b) 
	{
		switch(b.getInt("icon"))
		{
		case 0:icon = Global.Icons[32];break;
		case 1:icon = Global.Icons[31];break;
		case 2:icon = Global.Icons[33];break;
		case 3:icon = Global.Icons[31];break;	
		case 4:icon = Global.Icons[32];break;	
		case 5:icon = null;break;
		case 6:icon = Global.Icons[34];break;
		case 7:icon = Global.Icons[31];break;
		case 8:icon = Global.Icons[33];break;
		
		default :icon = null;
			
		}
		
	}
	
	
	
	
	
	
	
	
}
