package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Components.ActivityUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ImageView;


public class ProgressDialog extends android.app.Dialog 
{
private static DialogInterface.OnClickListener listner;
	
	
	
	public static void Show (String title, DialogInterface.OnClickListener Listener)
	{
		listner = Listener;
	 	if(listner==null) // setze standard Listner zu schliessen des Dialogs, falls kein L�istner angegeben wurde
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
    	   		ProgressDialog.Builder customBuilder = new
	    		ProgressDialog.Builder(main.mainActivity);
				customBuilder	.setTitle(title)
								.setPositiveButton(Global.Translations.Get("ok"),listner)
								.setNegativeButton(Global.Translations.Get("cancel"),listner);
	            dialog = customBuilder.create();
	    		
	            dialog.show();
	    	
    	
    	
    }

 
    public ProgressDialog(Context context, int theme) 
    {
        super(context, theme);
    }
 
    public ProgressDialog(Context context) 
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
	        private int value;
	        
	        private TextView titleTextView;
	        private TextView messageTextView;
	        private TextView progressMessageTextView;
	        private ProgressBar progressBar;
	        
	        private View contentView;
	        
	 
	        private DialogInterface.OnClickListener 
	                        positiveButtonClickListener,
	                        negativeButtonClickListener;
	 
	        public Builder(Context context) {
	            this.context = context;
	        }
	        
	        
	        /**
	         * Set the value 
	         * @param value
	         * @return
	         */
	        public Builder setValue(int value) {
	            this.value = value;
	            return this;
	        }
	 
	        /**
	         * Set the Dialog message from String
	         * @param title
	         * @return
	         */
	        public Builder setMessage(String message) {
	            this.message = message;
	            return this;
	        }
	 
	        /**
	         * Set the Dialog message from resource
	         * @param title
	         * @return
	         */
	        public Builder setMessage(int message) {
	            this.message = (String) context.getText(message);
	            return this;
	        }
	        
	       
	 
	        /**
	         * Set the Dialog title from resource
	         * @param title
	         * @return
	         */
	        public Builder setTitle(int title) {
	            this.title = (String) context.getText(title);
	            return this;
	        }
	 
	        /**
	         * Set the Dialog title from String
	         * @param title
	         * @return
	         */
	        public Builder setTitle(String title) {
	            this.title = title;
	            return this;
	        }
	        
	       
	 
	        /**
	         * Set a custom content view for the Dialog.
	         * If a message is set, the contentView is not
	         * added to the Dialog...
	         * @param v
	         * @return
	         */
	        public Builder setContentView(View v) {
	            this.contentView = v;
	            return this;
	        }
	 
	        /**
	         * Set the positive button resource and it's listener
	         * @param positiveButtonText
	         * @param listener
	         * @return
	         */
	        public Builder setPositiveButton(int positiveButtonText,
	                DialogInterface.OnClickListener listener) {
	            this.positiveButtonText = (String) context
	                    .getText(positiveButtonText);
	            this.positiveButtonClickListener = listener;
	            return this;
	        }
	 
	        /**
	         * Set the positive button text and it's listener
	         * @param positiveButtonText
	         * @param listener
	         * @return
	         */
	        public Builder setPositiveButton(String positiveButtonText,
	                DialogInterface.OnClickListener listener) {
	            this.positiveButtonText = positiveButtonText;
	            this.positiveButtonClickListener = listener;
	            return this;
	        }
	        
	       
	 
	        /**
	         * Set the negative button resource and it's listener
	         * @param negativeButtonText
	         * @param listener
	         * @return
	         */
	        public Builder setNegativeButton(int negativeButtonText,
	                DialogInterface.OnClickListener listener) {
	            this.negativeButtonText = (String) context
	                    .getText(negativeButtonText);
	            this.negativeButtonClickListener = listener;
	            return this;
	        }
	 
	        /**
	         * Set the negative button text and it's listener
	         * @param negativeButtonText
	         * @param listener
	         * @return
	         */
	        public Builder setNegativeButton(String negativeButtonText,
	                DialogInterface.OnClickListener listener) {
	            this.negativeButtonText = negativeButtonText;
	            this.negativeButtonClickListener = listener;
	            return this;
	        }
	        
	        
	        
	        
	        
	 
	        /**
	         * Create the custom dialog
	         */
	        public ProgressDialog create() {
	            LayoutInflater inflater = (LayoutInflater) context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            // instantiate the dialog with the custom Theme
	            final ProgressDialog dialog = new ProgressDialog(context, 
	            		R.style.Dialog);
	            View layout = inflater.inflate(R.layout.progress_dialog_layout, null);
	            dialog.addContentView(layout, new LayoutParams(
	                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	            // set the dialog title
	            if(title != null && !title.equals(""))
	            {
	            	((TextView) layout.findViewById(R.id.title)).setText(title);
	            	((TextView) layout.findViewById(R.id.title)).setTextSize((float) (Global.scaledFontSize_normal));
	            }
	            else
	            {
	            	((TextView) layout.findViewById(R.id.title)).setVisibility(
	                        View.GONE);
	            }
	            
	            // set the confirm button
	            if (positiveButtonText != null && !positiveButtonText.equals("") ) {
	                ((Button) layout.findViewById(R.id.positiveButton))
	                        .setText(positiveButtonText);
	                if (positiveButtonClickListener != null) {
	                    ((Button) layout.findViewById(R.id.positiveButton))
	                            .setOnClickListener(new View.OnClickListener() {
	                                public void onClick(View v) {
	                                    positiveButtonClickListener.onClick(
	                                    		dialog, 
	                                            DialogInterface.BUTTON_POSITIVE);
	                                }
	                            });
	                }
	            } else {
	                // if no confirm button just set the visibility to GONE
	                layout.findViewById(R.id.positiveButton).setVisibility(
	                        View.GONE);
	            }
	        
	            // set the cancel button
	            if (negativeButtonText != null && !negativeButtonText.equals("")) {
	                ((Button) layout.findViewById(R.id.negativeButton))
	                        .setText(negativeButtonText);
	                if (negativeButtonClickListener != null) {
	                    ((Button) layout.findViewById(R.id.negativeButton))
	                            .setOnClickListener(new View.OnClickListener() {
	                                public void onClick(View v) {
	                                    positiveButtonClickListener.onClick(
	                                    		dialog, 
	                                            DialogInterface.BUTTON_NEGATIVE);
	                                }
	                            });
	                }
	            } else {
	                // if no confirm button just set the visibility to GONE
	                layout.findViewById(R.id.negativeButton).setVisibility(
	                        View.GONE);
	            }
	            // set the content message
	            if (message != null) {
	                ((TextView) layout.findViewById(R.id.message)).setText(message);
	                ((TextView) layout.findViewById(R.id.message)).setTextSize((float) (Global.scaledFontSize_normal*0.8));
	            } else if (contentView != null) {
	                // if no message set
	                // add the contentView to the dialog body
	                ((LinearLayout) layout.findViewById(R.id.content))
	                        .removeAllViews();
	                ((LinearLayout) layout.findViewById(R.id.content))
	                        .addView(contentView, 
	                                new LayoutParams(
	                                        LayoutParams.WRAP_CONTENT, 
	                                        LayoutParams.WRAP_CONTENT));
	            }
	            
	            
	            dialog.setContentView(layout);
	            return dialog;
	        }

	        public void setProgress(String ProgressMsg, int value)
			{
			
		       // messageTextView;
		       progressMessageTextView.setText(ProgressMsg);
		       progressBar.setProgress(value);
		       
			}
					 
	    }
	    
	}