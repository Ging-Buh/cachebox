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

package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import CB_Core.GlobalCore;

/**
 * Zeigt eine InputBox an, in welcher der Benutzer ein Int Wert eintragen kann.
 * Da nicht auf ein Result gewartet werden kann, muss ein
 * DialogInterface.OnClickListener() übergeben werden.
 * 
 * @author Longri
 */
public class NumerikInputBox extends android.app.Dialog
{

	private static DialogInterface.OnClickListener listner;

	/**
	 * @param title
	 *            Der Titel der Ausgegeben werden soll.
	 * @param msg
	 *            Die Message, welche ausgegeben werden soll.
	 * @param InitialValue
	 *            Der Wert, welcher beim Anzeigen angezeigt werden soll
	 * @param Listener
	 *            Welcher die Events der Buttons behandelt
	 * 
	 *            <pre>
	 * {@code
	 *       	NumerickInputBox.Show(GlobalCore.Translations.Get("AdjustFinds"),GlobalCore.Translations.Get("TelMeFounds"),Config.GetInt("FoundOffset"), DialogListner);
	 *      	
	 *      	protected static final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	 * 	   { 
	 * 		
	 * 		@Override public void onClick(DialogInterface dialog, int button) 
	 * 			{
	 * 				String text =((numerik_inputbox_dialog) dialog).editText.getText().toString();
	 * 				// Behandle das ergebniss
	 * 				switch (button)
	 * 				{
	 * 					case -1: // ok Clicket
	 * 						Toast.makeText(main.mainActivity, "Eingabe = " + text, Toast.LENGTH_SHORT).show();
	 * 						break;
	 * 					case -2: // cancel clicket
	 * 						Toast.makeText(main.mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
	 * 						break;
	 * 					case -3:
	 * 						Toast.makeText(main.mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
	 * 						break;
	 * 				}
	 * 				
	 * 				dialog.dismiss();
	 * 			}
	 * 			
	 * 	    };
	 * 	
	 * }
	 * </pre>
	 */
	public static void Show(String title, String msg, int InitialValue,
			DialogInterface.OnClickListener Listener)
	{

		Show(title, msg, InitialValue, Listener, main.mainActivity);

	}

	public static void Show(String title, String msg, int InitialValue,
			DialogInterface.OnClickListener Listener, Activity activity)
	{

		listner = Listener;
		Bundle b = new Bundle();
		b.putString("msg", msg);
		b.putString("title", title);
		b.putInt("iniValue", InitialValue);

		if (listner == null) // setze standard Listner zum schliessen des
								// Dialogs, falls kein Listner angegeben wurde
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

		NumerikInputBox.Builder customBuilder = new NumerikInputBox.Builder(
				activity);
		customBuilder.setTitle(b.getString("title"))
				.setMessage(b.getString("msg")).setValue(b.getInt("iniValue"))
				.setPositiveButton(GlobalCore.Translations.Get("ok"), listner)
				.setNegativeButton(GlobalCore.Translations.Get("cancel"), listner);
		dialog = customBuilder.create();

		dialog.show();
	}

	public NumerikInputBox(Context context, int theme)
	{
		super(context, theme);
	}

	public NumerikInputBox(Context context)
	{
		super(context);
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder
	{

		private static Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private int value;

		private View contentView;

		private DialogInterface.OnClickListener positiveButtonClickListener,
				negativeButtonClickListener;

		public Builder(Context context)
		{
			this.context = context;
		}

		/**
		 * Set the value
		 * 
		 * @param value
		 * @return
		 */
		public Builder setValue(int value)
		{
			this.value = value;
			return this;
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
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
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
		public Builder setPositiveButton(int positiveButtonText,
				DialogInterface.OnClickListener listener)
		{
			this.positiveButtonText = (String) context
					.getText(positiveButtonText);
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
		public Builder setPositiveButton(String positiveButtonText,
				DialogInterface.OnClickListener listener)
		{
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText,
				DialogInterface.OnClickListener listener)
		{
			this.negativeButtonText = (String) context
					.getText(negativeButtonText);
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
		public Builder setNegativeButton(String negativeButtonText,
				DialogInterface.OnClickListener listener)
		{
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		public NumerikInputBox create()
		{
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final NumerikInputBox dialog = new NumerikInputBox(context,
					R.style.Dialog);
			View layout = inflater.inflate(R.layout.numerik_inputbox_layout,
					null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// set the dialog title
			if (title != null && !title.equals(""))
			{
				((TextView) layout.findViewById(R.id.title)).setText(title);
				((TextView) layout.findViewById(R.id.title))
						.setTextSize((float) (Sizes.getScaledFontSize_normal()));
			}
			else
			{
				((TextView) layout.findViewById(R.id.title))
						.setVisibility(View.GONE);
			}

			// set the confirm button
			if (positiveButtonText != null && !positiveButtonText.equals(""))
			{
				((Button) layout.findViewById(R.id.positiveButton))
						.setText(positiveButtonText);
				if (positiveButtonClickListener != null)
				{
					((Button) layout.findViewById(R.id.positiveButton))
							.setOnClickListener(new View.OnClickListener()
							{
								public void onClick(View v)
								{
									positiveButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
							});
				}
			}
			else
			{
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.positiveButton).setVisibility(
						View.GONE);
			}

			// set the cancel button
			if (negativeButtonText != null && !negativeButtonText.equals(""))
			{
				((Button) layout.findViewById(R.id.negativeButton))
						.setText(negativeButtonText);
				if (negativeButtonClickListener != null)
				{
					((Button) layout.findViewById(R.id.negativeButton))
							.setOnClickListener(new View.OnClickListener()
							{
								public void onClick(View v)
								{
									negativeButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_NEGATIVE);
								}
							});
				}
			}
			else
			{
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.negativeButton).setVisibility(
						View.GONE);
			}

			// set the content message
			if (message != null)
			{
				((TextView) layout.findViewById(R.id.message)).setText(message);
				((TextView) layout.findViewById(R.id.message))
						.setTextSize((float) (Sizes.getScaledFontSize_small()));
			}
			else if (contentView != null)
			{
				// if no message set
				// add the contentView to the dialog body
				((LinearLayout) layout.findViewById(R.id.content))
						.removeAllViews();
				((LinearLayout) layout.findViewById(R.id.content)).addView(
						contentView, new LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
			}

			editText = (EditText) layout.findViewById(R.id.editNumber);
			ActivityUtils.initialNumPadInt(main.mainActivity, layout, editText,
					String.valueOf(value), null, null);
			setBackgroundDrawables(layout);
			dialog.setContentView(layout);
			return dialog;
		}

	}

	public static EditText editText;

	private static void setBackgroundDrawables(View layout)
	{
		Resources res = Builder.context.getResources();

		Drawable header = res.getDrawable(main.N ? R.drawable.night_header
				: R.drawable.header);
		Drawable title = res.getDrawable(main.N ? R.drawable.night_title
				: R.drawable.title);
		Drawable center = res.getDrawable(main.N ? R.drawable.night_center
				: R.drawable.center);
		Drawable footer = res.getDrawable(main.N ? R.drawable.night_footer
				: R.drawable.footer);

		((LinearLayout) layout.findViewById(R.id.header))
				.setBackgroundDrawable(header);
		((TextView) layout.findViewById(R.id.title))
				.setBackgroundDrawable(title);
		((LinearLayout) layout.findViewById(R.id.content))
				.setBackgroundDrawable(center);
		((LinearLayout) layout.findViewById(R.id.footer))
				.setBackgroundDrawable(footer);

		((TextView) layout.findViewById(R.id.title)).setTextColor(Global
				.getColor(R.attr.TextColor));
		((TextView) layout.findViewById(R.id.message)).setTextColor(Global
				.getColor(R.attr.TextColor));

		res = null;
	}

}