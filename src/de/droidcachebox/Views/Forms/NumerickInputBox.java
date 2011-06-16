package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Components.ActivityUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ImageView;
import android.app.Dialog;
/**
 * Zeigt eine InputBox an, das Text, Schaltflächen und Symbole mit Informationen und Anweisungen für den Benutzer enthalten kann. 
 * 
 * 
 * Da nicht auf ein Result gewartet werden kann, muss ein DialogInterface.OnClickListener()
 * übergeben werden.
 * 
 * @author Longri
 * 
 
 */
public class NumerickInputBox 
{
	private static DialogInterface.OnClickListener listner;
	
	
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
	public static void Show (String title,String msg,int InitialValue, DialogInterface.OnClickListener Listener)
	{
		listner = Listener;
		Bundle b = new Bundle();
        b.putString("msg",msg);
        b.putString("title", title);
        b.putInt("iniValue", InitialValue);
        main.mainActivity.showDialog(DialogID.NUMERICK_INPUT,b);
	}
	
	
	
	static String button1="";
	static String button2="";
	static String button3="";
	
	
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
	    	case DialogID.NUMERICK_INPUT :
	    		numerik_inputbox_dialog.Builder customBuilder = new
	    		numerik_inputbox_dialog.Builder(main.mainActivity);
				customBuilder	.setTitle(b.getString("title"))
								.setMessage(b.getString("msg"))
								.setValue(b.getInt("iniValue"))
								.setPositiveButton(Global.Translations.Get("ok"),listner)
								.setNegativeButton(Global.Translations.Get("cancel"),listner);
	            dialog = customBuilder.create();
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
	
			
	}
	

	 
	
	
	
	
	

