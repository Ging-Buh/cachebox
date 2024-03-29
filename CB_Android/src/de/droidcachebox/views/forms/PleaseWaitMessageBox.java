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

package de.droidcachebox.views.forms;

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
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import de.droidcachebox.Global;
import de.droidcachebox.Main;
import de.droidcachebox.R;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;

/**
 * Zeigt ein Meldungsfeld an, das Text, Schaltflächen und Symbole mit Informationen und Anweisungen für den Benutzer enthalten kann.
 * Entspricht in etwa der C# .NET Klasse. Da nicht auf ein Result gewartet werden kann, muss ein DialogInterface.OnClickListener() übergeben
 * werden.
 *
 * @author Longri Basiert auf dem Sample Code von Antoine Vianey. Zu finden unter http://code.google.com/p/androgames-sample/
 */
public class PleaseWaitMessageBox extends android.app.Dialog {

    static String button1 = "";
    static String button2 = "";
    static String button3 = "";
    static Drawable icon = null;
    private static DialogInterface.OnClickListener listener;
    private static Activity parent = null;

    public PleaseWaitMessageBox(Context context, int theme) {
        super(context, theme);
    }

    public PleaseWaitMessageBox(Context context) {
        super(context);
    }

    /**
     * Zeigt ein Meldungsfeld mit angegebenem Text an. Im Meldungsfeld wird standardmäßig die Schaltfläche OK angezeigt. Das Meldungsfeld
     * enthält keine Beschriftung im Titel und kein Icon.
     *
     * @param msg Die Message, welche ausgegeben werden soll.
     *
     *            <pre>
     *                                                        Beispiel:</b>
     *                                                        {@code
     *                                                        MessageBox.Show("Test");
     *                                                        }
     *                                                                   </pre>
     */
    public static Dialog show(String msg) {
        listener = null;
        Bundle b = new Bundle();
        b.putString("msg", msg);
        Dialog dialog = CreateDialog(MessageBox.MSG_BOX_1, b);
        dialog.show();
        return dialog;
    }

    /**
     * Zeigt ein Meldungsfeld mit angegebenem Text an. Im Meldungsfeld wird standardmäßig die Schaltfläche OK angezeigt. Das Meldungsfeld
     * enthält keine Beschriftung im Titel und kein Icon.
     *
     * @param msg      Die Message, welche ausgegeben werden soll.
     * @param Listener Welcher die Events der Buttons behandelt
     *
     *                 <pre>
     *                                                                                 <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
     *                                                                                 {@code
     *                                                                                 MessageBox.Show("Test",null);
     *                                                                                 }
     *                                                                                            </pre>
     *
     *                 <pre>
     *                                                                                 <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
     *                                                                                 {@code
     *                                                                                 	MessageBox.Show("Test",DialogListener)
     *
     *                                                                                  private final  DialogInterface.OnClickListener  DialogListener = new  DialogInterface.OnClickListener()
     *                                                                                   {
     *                                                                                 	&#64;Override
     *                                                                                 	public void onClick(DialogInterface dialog, int button)
     *                                                                                    {
     *                                                                                 		// Behandle das ergebniss
     *                                                                                 		switch (button)
     *                                                                                        {
     *                                                                                 			case -1:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 1", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                 			case -2:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 2", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                 			case -3:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 3", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                        }
     *
     *                                                                                 		dialog.dismiss();
     *                                                                                    }
     *
     *                                                                                    };
     *                                                                                 }
     *                                                                                            </pre>
     */
    public static Dialog show(String msg, DialogInterface.OnClickListener Listener) {
        listener = Listener;
        Bundle b = new Bundle();
        b.putString("msg", msg);
        Dialog dialog = CreateDialog(MessageBox.MSG_BOX_1, b);
        dialog.show();
        return dialog;
    }

    /**
     * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. Im Meldungsfeld wird standardmäßig die
     * Schaltfläche OK angezeigt. Das Meldungsfeld enthält kein Icon.
     *
     * @param msg      Der im Meldungsfeld anzuzeigende Text.
     * @param title    Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
     * @param Listener Welcher die Events der Buttons behandelt
     *
     *                 <pre>
     *                                                                                 <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
     *                                                                                 {@code
     *                                                                                 MessageBox.Show("Test", "Titel" ,null);
     *                                                                                 }
     *                                                                                            </pre>
     *
     *                 <pre>
     *                                                                                 <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
     *                                                                                 {@code
     *                                                                                 	MessageBox.Show("Test", "Titel",DialogListener)
     *
     *                                                                                  private final  DialogInterface.OnClickListener  DialogListener = new  DialogInterface.OnClickListener()
     *                                                                                   {
     *                                                                                 	&#64;Override
     *                                                                                 	public void onClick(DialogInterface dialog, int button)
     *                                                                                    {
     *                                                                                 		// Behandle das ergebniss
     *                                                                                 		switch (button)
     *                                                                                        {
     *                                                                                 			case -1:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 1", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                 			case -2:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 2", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                 			case -3:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 3", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                        }
     *
     *                                                                                 		dialog.dismiss();
     *                                                                                    }
     *
     *                                                                                    };
     *                                                                                 }
     *                                                                                            </pre>
     */
    public static Dialog show(String msg, String title, DialogInterface.OnClickListener Listener) {
        listener = Listener;
        Bundle b = new Bundle();
        b.putString("msg", msg);
        b.putString("title", title);
        Dialog dialog = CreateDialog(MessageBox.MSG_BOX_2, b);
        dialog.show();
        return dialog;
    }

    /**
     * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. Im Meldungsfeld wird standardmäßig die
     * Schaltfläche OK angezeigt. Das Meldungsfeld enthält kein Icon.
     *
     * @param msg      Der im Meldungsfeld anzuzeigende Text.
     * @param title    Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
     * @param buttons  Ein MessageBoxButton-Wert, der angibt, welche Schaltflächen im Meldungsfeld angezeigt werden sollen.
     * @param Listener Welcher die Events der Buttons behandelt
     *
     *                 <pre>
     *                                                                                 <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
     *                                                                                 {@code
     *                                                                                 MessageBox.Show("Test", "Titel" ,null);
     *                                                                                 }
     *                                                                                            </pre>
     *
     *                 <pre>
     *                                                                                 <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
     *                                                                                 {@code
     *                                                                                 	MessageBox.Show("Test", "Titel",DialogListener)
     *
     *                                                                                  private final  DialogInterface.OnClickListener  DialogListener = new  DialogInterface.OnClickListener()
     *                                                                                   {
     *                                                                                 	&#64;Override
     *                                                                                 	public void onClick(DialogInterface dialog, int button)
     *                                                                                    {
     *                                                                                 		// Behandle das ergebniss
     *                                                                                 		switch (button)
     *                                                                                        {
     *                                                                                 			case -1:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 1", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                 			case -2:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 2", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                 			case -3:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 3", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                        }
     *
     *                                                                                 		dialog.dismiss();
     *                                                                                    }
     *
     *                                                                                    };
     *                                                                                 }
     *                                                                                            </pre>
     */

    public static Dialog show(String msg, String title, MsgBoxButton buttons, DialogInterface.OnClickListener Listener) {
        parent = null;
        return show(msg, title, buttons, Listener, null);
    }

    public static Dialog show(String msg, String title, MsgBoxButton buttons, DialogInterface.OnClickListener Listener, Activity act) {
        listener = Listener;
        Bundle b = new Bundle();
        b.putString("msg", msg);
        b.putString("title", title);
        b.putInt("buttons", buttons.ordinal());
        Dialog dialog = CreateDialog(MessageBox.MSG_BOX_3, b);
        dialog.show();
        return dialog;

    }

    /**
     * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. Im Meldungsfeld wird standardmäßig die
     * Schaltfläche OK angezeigt. Das Meldungsfeld enthält kein Icon.
     *
     * @param msg      Der im Meldungsfeld anzuzeigende Text.
     * @param title    Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
     * @param buttons  Ein MessageBoxButton-Wert, der angibt, welche Schaltflächen im Meldungsfeld angezeigt werden sollen.
     * @param Listener Welcher die Events der Buttons behandelt
     *
     *                 <pre>
     *                                                                                 <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
     *                                                                                 {@code
     *                                                                                 MessageBox.Show("Test", "Titel" ,null);
     *                                                                                 }
     *                                                                                            </pre>
     *
     *                 <pre>
     *                                                                                 <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
     *                                                                                 {@code
     *                                                                                 	MessageBox.Show("Test", "Titel",DialogListener)
     *
     *                                                                                  private final  DialogInterface.OnClickListener  DialogListener = new  DialogInterface.OnClickListener()
     *                                                                                   {
     *                                                                                 	&#64;Override
     *                                                                                 	public void onClick(DialogInterface dialog, int button)
     *                                                                                    {
     *                                                                                 		// Behandle das ergebniss
     *                                                                                 		switch (button)
     *                                                                                        {
     *                                                                                 			case -1:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 1", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                 			case -2:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 2", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                 			case -3:
     *                                                                                 				toast.makeText(mainActivity, "Click Button 3", toast.LENGTH_SHORT).show();
     *                                                                                 				break;
     *                                                                                        }
     *
     *                                                                                 		dialog.dismiss();
     *                                                                                    }
     *
     *                                                                                    };
     *                                                                                 }
     *                                                                                            </pre>
     */

    public static Dialog show(String msg, String title, MsgBoxButton buttons, MsgBoxIcon icon, DialogInterface.OnClickListener Listener) {
        parent = null;
        return show(msg, title, buttons, icon, Listener, null);
    }

    public static Dialog show(String msg, String title, MsgBoxButton buttons, MsgBoxIcon icon, DialogInterface.OnClickListener Listener, Activity act) {
        listener = Listener;
        Bundle b = new Bundle();
        b.putString("msg", msg);
        b.putString("title", title);
        b.putInt("buttons", buttons.ordinal());
        b.putInt("icon", icon.ordinal());
        Dialog dialog = CreateDialog(MessageBox.MSG_BOX_4, b);
        dialog.show();
        return dialog;
    }

    public static Dialog show(String msg, String title, MsgBoxIcon icon) {
        return show(msg, title, MsgBoxButton.OK, icon, null);

    }

    public static Dialog CreateDialog(int dialogId, Bundle b) {
        if (listener == null) {
            listener = (dialog, which) -> dialog.dismiss();
        }

        Dialog dialog = null;
        switch (dialogId) {
            case MessageBox.MSG_BOX_1:
                PleaseWaitMessageBox.Builder customBuilder = new PleaseWaitMessageBox.Builder(getActivity());
                customBuilder.setTitle("").setMessage(b.getString("msg")).setPositiveButton(Translation.get("ok"), listener);
                dialog = customBuilder.create();
                break;

            case MessageBox.MSG_BOX_2:
                PleaseWaitMessageBox.Builder customBuilder2 = new PleaseWaitMessageBox.Builder(getActivity());
                customBuilder2.setTitle(b.getString("title")).setMessage(b.getString("msg")).setPositiveButton(Translation.get("ok"), listener);
                dialog = customBuilder2.create();
                break;

            case MessageBox.MSG_BOX_3:

                setButtonCaptions(b);
                PleaseWaitMessageBox.Builder customBuilder3 = new PleaseWaitMessageBox.Builder(getActivity());
                customBuilder3.setTitle(b.getString("title")).setMessage(b.getString("msg")).setPositiveButton(button1, listener).setNeutralButton(button2, listener).setNegativeButton(button3, listener);
                dialog = customBuilder3.create();
                break;

            case MessageBox.MSG_BOX_4:

                setButtonCaptions(b);
                setIcon(b);
                PleaseWaitMessageBox.Builder customBuilder4 = new PleaseWaitMessageBox.Builder(getActivity());
                customBuilder4.setTitle(b.getString("title")).setMessage(b.getString("msg")).setPositiveButton(button1, listener).setNeutralButton(button2, listener).setNegativeButton(button3, listener).setIcon(icon);
                dialog = customBuilder4.create();
                break;

        }
        return dialog;
    }

    /**
     * @return
     */
    private static Activity getActivity() {
        if (parent == null)
            return Main.getInstance();
        return parent;

    }

    /**
     * @param b
     */
    private static void setButtonCaptions(Bundle b) {
        int button = b.getInt("buttons");
        if (button == 0) {
            button1 = Translation.get("abort");
            button2 = Translation.get("retry");
            button3 = Translation.get("ignore");
        } else if (button == 1) {
            button1 = Translation.get("ok");
            button2 = "";
            button3 = "";
        } else if (button == 2) {
            button1 = Translation.get("ok");
            button2 = "";
            button3 = Translation.get("cancel");
        } else if (button == 3) {
            button1 = Translation.get("retry");
            button2 = "";
            button3 = Translation.get("cancel");
        } else if (button == 4) {
            button1 = Translation.get("yes");
            button2 = "";
            button3 = Translation.get("no");
        } else if (button == 5) {
            button1 = Translation.get("yes");
            button2 = Translation.get("no");
            button3 = Translation.get("cancel");
        } else if (button == 6) {
            button3 = Translation.get("cancel");
        }
    }

    /**
     * @param b
     */
    private static void setIcon(Bundle b) {
        switch (b.getInt("icon")) {
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

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {

        private final Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private String neutralButtonText;
        private View contentView;
        private Drawable icon;

        private DialogInterface.OnClickListener positiveButtonClickListener, neutralButtonClickListener, negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set the Dialog message from String
         *
         * @param message
         * @return ?
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set the Dialog message from resource
         *
         * @param message ?
         * @return ?
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title ?
         * @return ?
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         *
         * @param title ?
         * @return ?
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the Dialog icon from Drawable
         *
         * @param icon ?
         * @return ?
         */
        public Builder setIcon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Set a custom content view for the Dialog. If a message is set, the contentView is not added to the Dialog...
         *
         * @param v ?
         * @return ?
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText ?
         * @param listener           ?
         * @return ?
         */
        public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context.getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the positive button text and it's listener
         *
         * @param positiveButtonText ?
         * @param listener           ?
         * @return ?
         */
        public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param neutralButtonText ?
         * @param listener          ?
         * @return ?
         */
        public Builder setNeutralButton(int neutralButtonText, DialogInterface.OnClickListener listener) {
            this.neutralButtonText = (String) context.getText(neutralButtonText);
            this.neutralButtonClickListener = listener;
            return this;
        }

        /**
         * Set the positive button text and it's listener
         *
         * @param neutralButtonText ?
         * @param listener          ?
         * @return ?
         */
        public Builder setNeutralButton(String neutralButtonText, DialogInterface.OnClickListener listener) {
            this.neutralButtonText = neutralButtonText;
            this.neutralButtonClickListener = listener;
            return this;
        }

        /**
         * Set the negative button resource and it's listener
         *
         * @param negativeButtonText ?
         * @param listener           ?
         * @return ?
         */
        public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
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
        public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * Create the custom dialog
         */
        @SuppressWarnings("deprecation")
        public PleaseWaitMessageBox create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final PleaseWaitMessageBox dialog = new PleaseWaitMessageBox(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.please_wait_message_box_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            if (title != null && !title.equals("")) {
                ((TextView) layout.findViewById(R.id.title)).setText(title);
                ((TextView) layout.findViewById(R.id.title)).setTextSize((UiSizes.getInstance().getScaledFontSize_btn()));
            } else {
                ((TextView) layout.findViewById(R.id.title)).setVisibility(View.GONE);
            }

            // set the confirm button
            if (positiveButtonText != null && !positiveButtonText.equals("")) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.positiveButton)).setOnClickListener((View.OnClickListener) v -> positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE));
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }
            // set the neutral button
            if (neutralButtonText != null && !neutralButtonText.equals("")) {
                ((Button) layout.findViewById(R.id.neutralButton)).setText(neutralButtonText);
                if (neutralButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.neutralButton)).setOnClickListener((View.OnClickListener) v -> neutralButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEUTRAL));
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.neutralButton).setVisibility(View.GONE);
            }
            // set the cancel button
            if (negativeButtonText != null && !negativeButtonText.equals("")) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener((View.OnClickListener) v -> positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE));
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
            }
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
                ((TextView) layout.findViewById(R.id.message)).setTextSize((UiSizes.getInstance().getScaledFontSize_small()));

                ((TextView) layout.findViewById(R.id.message)).measure(UiSizes.getInstance().getWindowWidth() - 100, UiSizes.getInstance().getWindowHeight() - 100);
                int height = ((TextView) layout.findViewById(R.id.message)).getMeasuredHeight();

                LayoutParams params = ((ScrollView) layout.findViewById(R.id.ScrollView01)).getLayoutParams();
                if (height > UiSizes.getInstance().getWindowHeight() - (UiSizes.getInstance().getButtonHeight() * 4)) {
                    height = UiSizes.getInstance().getWindowHeight() - (UiSizes.getInstance().getButtonHeight() * 4);
                    params.height = height;
                    ((ScrollView) layout.findViewById(R.id.ScrollView01)).setLayoutParams(params);
                }

            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }

            // set Icon
            if (icon != null) {
                ((ImageView) layout.findViewById(R.id.icon)).setImageDrawable(icon);
            } else {
                ((ImageView) layout.findViewById(R.id.icon)).setVisibility(View.GONE);
            }

            setBackgroundDrawables(layout);

            dialog.setContentView(layout);

            // set Wait Animation
            ProgressBar PB = ((ProgressBar) layout.findViewById(R.id.progressBar1));

            PB.setIndeterminate(true);
            PB.setIndeterminateDrawable(Global.Icons[42]);
            RotateAnimation anim = new RotateAnimation(0f, 360f, Global.Icons[42].getIntrinsicWidth(), Global.Icons[42].getIntrinsicHeight());
            anim.setInterpolator(new LinearInterpolator());
            anim.setRepeatCount(Animation.INFINITE);
            anim.setDuration(1500);

            PB.setAnimation(anim);
            return dialog;
        }

        @SuppressWarnings({"deprecation"})
        private void setBackgroundDrawables(View layout) {
            Resources res = context.getResources();

            Drawable header = res.getDrawable(Settings.nightMode.getValue() ? R.drawable.night_header : R.drawable.header);
            Drawable title = res.getDrawable(Settings.nightMode.getValue() ? R.drawable.night_title : R.drawable.title);
            Drawable center = res.getDrawable(Settings.nightMode.getValue() ? R.drawable.night_center : R.drawable.center);
            Drawable footer = res.getDrawable(Settings.nightMode.getValue() ? R.drawable.night_footer : R.drawable.footer);

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
