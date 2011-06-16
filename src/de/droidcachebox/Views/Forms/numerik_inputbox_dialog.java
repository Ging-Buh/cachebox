package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ImageView;

public class numerik_inputbox_dialog extends android.app.Dialog 
{
 
    public numerik_inputbox_dialog(Context context, int theme) 
    {
        super(context, theme);
    }
 
    public numerik_inputbox_dialog(Context context) 
    {
        super(context);
    }
    
    
    /**
	 * Helper class for creating a custom dialog
	 */
	    public static class Builder {
	 
	        private Context context;
	        private String title;
	        private String message;
	        private String positiveButtonText;
	        private String negativeButtonText;
	        private int value;
	        
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
	        public numerik_inputbox_dialog create() {
	            LayoutInflater inflater = (LayoutInflater) context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            // instantiate the dialog with the custom Theme
	            final numerik_inputbox_dialog dialog = new numerik_inputbox_dialog(context, 
	            		R.style.Dialog);
	            View layout = inflater.inflate(R.layout.numerik_inputbox_layout, null);
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
	            
	            // set the value
	           ((EditText) layout.findViewById(R.id.editNumber)).setText(String.valueOf(value));
	           ((EditText) layout.findViewById(R.id.editNumber)).setTextSize((float) (Global.scaledFontSize_normal*0.8)); 
	           
	            
	            dialog.setContentView(layout);
	            return dialog;
	        }

					 
	    }
	 
	}