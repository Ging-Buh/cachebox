/* 
 * Copyright (C) 2011 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cachebox_test.Views.Forms;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.Math.UI_Size_Base;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.splash;

/**
 * Zeigt ein Meldungsfeld an, das Text, Schaltflächen und Symbole mit Informationen und Anweisungen für den Benutzer enthalten kann.
 * Entspricht in etwa der C# .NET Klasse. Da nicht auf ein Result gewartet werden kann, muss ein DialogInterface.OnClickListener() übergeben
 * werden.
 * 
 * @author Longri Basiert auf dem Sample Code von Antoine Vianey. Zu finden unter http://code.google.com/p/androgames-sample/
 */
public class MessageBox extends android.app.Dialog
{

	private static DialogInterface.OnClickListener listner;

	/**
	 * Zeigt ein Meldungsfeld mit angegebenem Text an. Im Meldungsfeld wird standardmäßig die Schaltfläche OK angezeigt. Das Meldungsfeld
	 * enthält keine Beschriftung im Titel und kein Icon.
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
		Bundle b = new Bundle();
		b.putString("msg", msg);
		Dialog dialog = CreateDialog(DialogID.MSG_BOX_1, b);
		dialog.show();

	}

	private static Activity parent;

	public static void Show(String msg, Activity act)
	{
		parent = act;
		listner = null;
		Bundle b = new Bundle();
		b.putString("msg", msg);
		Dialog dialog = CreateDialog(DialogID.MSG_BOX_1, b);
		dialog.show();

	}

	/**
	 * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. Im Meldungsfeld wird standardmäßig die
	 * Schaltfläche OK angezeigt. Das Meldungsfeld enthält kein Icon.
	 * 
	 * @param msg
	 *            Der im Meldungsfeld anzuzeigende Text.
	 * @param title
	 *            Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
	 * @param buttons
	 *            Ein MessageBoxButtons-Wert, der angibt, welche Schaltflächen im Meldungsfeld angezeigt werden sollen.
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
	 * <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
	 * {@code
	 * 	MessageBox.Show("Test", "Titel",DialogListner)
	 * 	
	 *  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 *   {
	 * @return 
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
	public static Dialog Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon,
			DialogInterface.OnClickListener Listener)
	{
		listner = Listener;
		Bundle b = new Bundle();
		b.putString("msg", msg);
		b.putString("title", title);
		b.putInt("buttons", buttons.ordinal());
		b.putInt("icon", icon.ordinal());
		Dialog dialog = CreateDialog(DialogID.MSG_BOX_4, b);
		try
		{
			dialog.show();
		}
		catch (Exception e)
		{
			parent = null;
			dialog = CreateDialog(DialogID.MSG_BOX_4, b);
			dialog.show();
		}
		return dialog;
	}

	public static void Show(String msg, String title, MessageBoxIcon icon)
	{
		Show(msg, title, MessageBoxButtons.OK, icon, null);

	}

	static String button1 = "";
	static String button2 = "";
	static String button3 = "";
	static Drawable icon = null;

	public static Dialog CreateDialog(int dialogId, Bundle b)
	{
		if (listner == null) // setze standard Listner zu schliessen des
								// Dialogs, falls kein LÖistner angegeben wurde
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
		case DialogID.MSG_BOX_1:
			MessageBox.Builder customBuilder = new MessageBox.Builder(getActivity());
			customBuilder.setTitle("").setMessage(b.getString("msg")).setPositiveButton(Translation.Get("ok"), listner);
			dialog = customBuilder.create();
			break;

		case DialogID.MSG_BOX_2:
			MessageBox.Builder customBuilder2 = new MessageBox.Builder(getActivity());
			customBuilder2.setTitle(b.getString("title")).setMessage(b.getString("msg")).setPositiveButton(Translation.Get("ok"), listner);
			dialog = customBuilder2.create();
			break;

		case DialogID.MSG_BOX_3:

			setButtonCaptions(b);
			MessageBox.Builder customBuilder3 = new MessageBox.Builder(getActivity());
			customBuilder3.setTitle(b.getString("title")).setMessage(b.getString("msg")).setPositiveButton(button1, listner)
					.setNeutralButton(button2, listner).setNegativeButton(button3, listner);
			dialog = customBuilder3.create();
			break;

		case DialogID.MSG_BOX_4:

			setButtonCaptions(b);
			setIcon(b);
			MessageBox.Builder customBuilder4 = new MessageBox.Builder(getActivity());
			customBuilder4.setTitle(b.getString("title")).setMessage(b.getString("msg")).setPositiveButton(button1, listner)
					.setNeutralButton(button2, listner).setNegativeButton(button3, listner).setIcon(icon);
			dialog = customBuilder4.create();
			break;

		}
		return dialog;
	}

	/**
	 * @return
	 */
	private static Activity getActivity()
	{
		if (parent != null) return parent;
		if (main.mainActivity != null) return main.mainActivity;
		return splash.splashActivity;
	}

	/**
	 * @param b
	 */
	private static void setButtonCaptions(Bundle b)
	{
		int button = b.getInt("buttons");
		if (button == 0)
		{
			button1 = Translation.Get("abort");
			button2 = Translation.Get("retry");
			button3 = Translation.Get("ignore");
		}
		else if (button == 1)
		{
			button1 = Translation.Get("ok");
			button2 = "";
			button3 = "";
		}
		else if (button == 2)
		{
			button1 = Translation.Get("ok");
			button2 = "";
			button3 = Translation.Get("cancel");
		}
		else if (button == 3)
		{
			button1 = Translation.Get("retry");
			button2 = "";
			button3 = Translation.Get("cancel");
		}
		else if (button == 4)
		{
			button1 = Translation.Get("yes");
			button2 = "";
			button3 = Translation.Get("no");
		}
		else if (button == 5)
		{
			button1 = Translation.Get("yes");
			button2 = Translation.Get("no");
			button3 = Translation.Get("cancel");
		}
	}

	/**
	 * @param b
	 */
	private static void setIcon(Bundle b)
	{
		switch (b.getInt("icon"))
		{
		case 0:
			icon = Global.Icons[32];
			break;
		case 1:
			icon = Global.Icons[31];
			break;
		case 2:
			icon = Global.Icons[33];
			break;
		case 3:
			icon = Global.Icons[31];
			break;
		case 4:
			icon = Global.Icons[32];
			break;
		case 5:
			icon = null;
			break;
		case 6:
			icon = Global.Icons[34];
			break;
		case 7:
			icon = Global.Icons[31];
			break;
		case 8:
			icon = Global.Icons[33];
			break;
		case 9:
			icon = Global.Icons[35];
			break;
		case 10:
			icon = Global.Icons[36];
			break;

		default:
			icon = null;

		}

	}

	public MessageBox(Context context, int theme)
	{
		super(context, theme);
	}

	public MessageBox(Context context)
	{
		super(context);
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder
	{

		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private String neutralButtonText;
		private View contentView;
		private Drawable icon;

		private DialogInterface.OnClickListener positiveButtonClickListener, neutralButtonClickListener, negativeButtonClickListener;

		public Builder(Context context)
		{
			this.context = context;
		}

		/**
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(String message)
		{
			this.message = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(int message)
		{
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title)
		{
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title)
		{
			this.title = title;
			return this;
		}

		/**
		 * Set the Dialog icon from Drawable
		 * 
		 * @param title
		 * @return
		 */
		public Builder setIcon(Drawable icon)
		{
			this.icon = icon;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v)
		{
			this.contentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener)
		{
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener)
		{
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNeutralButton(int neutralButtonText, DialogInterface.OnClickListener listener)
		{
			this.neutralButtonText = (String) context.getText(neutralButtonText);
			this.neutralButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNeutralButton(String neutralButtonText, DialogInterface.OnClickListener listener)
		{
			this.neutralButtonText = neutralButtonText;
			this.neutralButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener)
		{
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener)
		{
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		public static int WindowWidth = 200;
		public static int WindowHeight = 200;
		public static float textSize = 12f;
		public static int ButtonHeight = 30;

		/**
		 * Create the custom dialog
		 */
		@SuppressWarnings("deprecation")
		public MessageBox create()
		{
			int winWidth = UI_Size_Base.that != null ? UI_Size_Base.that.getWindowWidth() : WindowWidth;
			int winHeight = UI_Size_Base.that != null ? UI_Size_Base.that.getWindowHeight() : WindowHeight;
			// float txtSize = UI_Size_Base.that != null ? (float) (UI_Size_Base.that.getScaledFontSize_btn()) : textSize;
			int Buttonheight = UI_Size_Base.that != null ? UI_Size_Base.that.getButtonHeight() : ButtonHeight;

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final MessageBox dialog = new MessageBox(context, R.style.Dialog);
			View layout = inflater.inflate(R.layout.message_box_layout, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// set the dialog title
			if (title != null && !title.equals(""))
			{
				((TextView) layout.findViewById(R.id.title)).setText(title);
				// ((TextView) layout.findViewById(R.id.title)).setTextSize(txtSize);
			}
			else
			{
				((TextView) layout.findViewById(R.id.title)).setVisibility(View.GONE);
			}

			// set the confirm button
			if (positiveButtonText != null && !positiveButtonText.equals(""))
			{
				((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
				if (positiveButtonClickListener != null)
				{
					((Button) layout.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
					});
				}
			}
			else
			{
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
			}
			// set the neutral button
			if (neutralButtonText != null && !neutralButtonText.equals(""))
			{
				((Button) layout.findViewById(R.id.neutralButton)).setText(neutralButtonText);
				if (neutralButtonClickListener != null)
				{
					((Button) layout.findViewById(R.id.neutralButton)).setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							neutralButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
						}
					});
				}
			}
			else
			{
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.neutralButton).setVisibility(View.GONE);
			}
			// set the cancel button
			if (negativeButtonText != null && !negativeButtonText.equals(""))
			{
				((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
				if (negativeButtonClickListener != null)
				{
					((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
						}
					});
				}
			}
			else
			{
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
			}
			// set the content message
			if (message != null)
			{

				((TextView) layout.findViewById(R.id.message)).setText(message);
				// ((TextView) layout.findViewById(R.id.message)).setTextSize(txtSize);

				((TextView) layout.findViewById(R.id.message)).measure(winWidth - 100, winHeight - 100);
				int height = ((TextView) layout.findViewById(R.id.message)).getMeasuredHeight();

				LayoutParams params = ((ScrollView) layout.findViewById(R.id.ScrollView01)).getLayoutParams();
				if (height > winHeight - (Buttonheight * 4))
				{
					height = winHeight - (Buttonheight * 4);
					params.height = height;
					((ScrollView) layout.findViewById(R.id.ScrollView01)).setLayoutParams(params);
				}

			}
			else if (contentView != null)
			{
				// if no message set
				// add the contentView to the dialog body
				((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
				((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));
			}

			// set Icon
			if (icon != null)
			{
				((ImageView) layout.findViewById(R.id.icon)).setImageDrawable(icon);
			}
			else
			{
				((ImageView) layout.findViewById(R.id.icon)).setVisibility(View.GONE);
			}

			setBackgroundDrawables(layout);

			dialog.setContentView(layout);
			return dialog;
		}

		@SuppressWarnings("deprecation")
		private void setBackgroundDrawables(View layout)
		{
			Resources res = context.getResources();

			boolean NightMode = false;
			if (Config.settings != null) NightMode = Config.nightMode.getValue();

			Drawable header = res.getDrawable(NightMode ? R.drawable.night_header : R.drawable.header);
			Drawable title = res.getDrawable(NightMode ? R.drawable.night_title : R.drawable.title);
			Drawable center = res.getDrawable(NightMode ? R.drawable.night_center : R.drawable.center);
			Drawable footer = res.getDrawable(NightMode ? R.drawable.night_footer : R.drawable.footer);

			((LinearLayout) layout.findViewById(R.id.header)).setBackgroundDrawable(header);
			((TextView) layout.findViewById(R.id.title)).setBackgroundDrawable(title);
			((LinearLayout) layout.findViewById(R.id.content)).setBackgroundDrawable(center);
			((LinearLayout) layout.findViewById(R.id.footer)).setBackgroundDrawable(footer);

			((TextView) layout.findViewById(R.id.title)).setTextColor(Global.getColor(R.attr.TextColor));
			((TextView) layout.findViewById(R.id.message)).setTextColor(Global.getColor(R.attr.TextColor));

			res = null;
		}

	}

}
