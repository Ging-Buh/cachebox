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

import static android.view.View.GONE;

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
import android.widget.TextView;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;

/**
 * Zeigt ein Meldungsfeld an, das Text, Schaltflächen und Symbole mit Informationen und Anweisungen für den Benutzer enthalten kann.
 * Entspricht in etwa der C# .NET Klasse.
 * Da nicht auf ein Result gewartet werden kann, muss ein DialogInterface.OnClickListener() übergeben werden.
 *
 * @author Longri Basiert auf dem Sample Code von Antoine Vianey.
 * Zu finden unter http://code.google.com/p/androgames-sample/
 */
public class MessageBox {
    public static final int MSG_BOX_1 = 1;
    public static final int MSG_BOX_2 = 2;
    public static final int MSG_BOX_3 = 3;
    public static final int MSG_BOX_4 = 4;

    /**
     * Zeigt ein Meldungsfeld mit angegebenem Text an.
     * Im Meldungsfeld wird standardmäßig die Schaltfläche OK angezeigt.
     * Das Meldungsfeld enthält keine Beschriftung im Titel und kein Icon.
     *
     * @param msg Die Message, welche ausgegeben werden soll.
     *
     *            <pre>
     *                                                            Beispiel:</b>
     *                                                            {@code
     *                                                            MessageBox.show("Test");
     *                                                            }
     *                                                        </pre>
     */
    public static void show(Activity activity, String msg) {
        Bundle bundle = new Bundle();
        bundle.putString("msg", msg);
        Dialog dialog = createDialog(activity, MSG_BOX_1, bundle, null);
        dialog.show();
    }

    /**
     * Zeigt ein Meldungsfeld mit dem angegebenen Text und der angegebenen Beschriftung an. Im Meldungsfeld wird standardmäßig die
     * Schaltfläche OK angezeigt. Das Meldungsfeld enthält kein Icon.
     *
     * @param msg      Der im Meldungsfeld anzuzeigende Text.
     * @param title    Der in der Titelleiste des Meldungsfelds anzuzeigende Text.
     * @param buttons  Ein MessageBoxButton-Wert, der angibt, welche Schaltflächen im Meldungsfeld angezeigt werden sollen.
     * @param listener handling all button events
     *
     *                 <pre>
     *                                                                                     <b>Wenn eine Message nicht behandelt werden soll, reicht volgendes Beispiel:</b>
     *                                                                                     {@code
     *                                                                                     MessageBox.show("Test", "Titel" ,null);
     *                                                                                     }
     *                                                                                 </pre>
     *
     *                 <pre>
     *                                                                                     <b>Wenn eine Message behandelt werden soll, wird noch ein DialogInterface.OnClickListener() benötigt:</b>
     *                                                                                     {@code
     *                                                                                     MessageBox.show("Test", "Titel",DialogListener)
     *                                                                                     private final  DialogInterface.OnClickListener  DialogListener = new  DialogInterface.OnClickListener()
     *                                                                                     {
     *                                                                                     &#64;return
     *                                                                                     @Override
     *                                                                                     public void onClick(DialogInterface dialog, int button)
     *                                                                                     {
     *                                                                                     // Behandle das ergebniss
     *                                                                                     switch (button)
     *                                                                                     {
     *                                                                                     case -1:
     *                                                                                     toast.makeText(mainActivity, "Click Button 1", toast.LENGTH_SHORT).show();
     *                                                                                     break;
     *                                                                                     case -2:
     *                                                                                     toast.makeText(mainActivity, "Click Button 2", toast.LENGTH_SHORT).show();
     *                                                                                     break;
     *                                                                                     case -3:
     *                                                                                     toast.makeText(mainActivity, "Click Button 3", toast.LENGTH_SHORT).show();
     *                                                                                     break;
     *                                                                                     }
     *                                                                                     dialog.dismiss();
     *                                                                                     }
     *                                                                                     };
     *                                                                                     }
     *                                                                                 </pre>
     */
    public static Dialog show(Activity activity, String msg, String title, MsgBoxButton buttons, MsgBoxIcon icon, DialogInterface.OnClickListener listener) {
        Bundle b = new Bundle();
        b.putString("msg", msg);
        b.putString("title", title);
        b.putInt("buttons", buttons.ordinal());
        b.putInt("icon", icon.ordinal());
        Dialog dialog = createDialog(activity, MSG_BOX_4, b, listener);
        try {
            dialog.show();
        } catch (Exception ignored) {
        }
        return dialog;
    }

    public static void show(Activity activity, String msg, String title, MsgBoxIcon icon) {
        show(activity, msg, title, MsgBoxButton.OK, icon, null);
    }

    private static Dialog createDialog(Activity activity, int dialogId, Bundle b, DialogInterface.OnClickListener listener) {
        if (listener == null) {
            listener = (dialog, which) -> dialog.dismiss();
        }
        MessageBox.Builder customBuilder = new MessageBox.Builder(activity);
        switch (dialogId) {
            case MSG_BOX_1:
                customBuilder.setTitle("").setMessage(b.getString("msg")).setPositiveButton(Translation.get("ok"), listener);
                break;
            case MSG_BOX_2:
                customBuilder.setTitle(b.getString("title")).setMessage(b.getString("msg")).setPositiveButton(Translation.get("ok"), listener);
                break;
            case MSG_BOX_3:
                setButtonCaptions(b, customBuilder, listener);
                customBuilder.setTitle(b.getString("title")).setMessage(b.getString("msg"));
                break;
            case MSG_BOX_4:
                setButtonCaptions(b, customBuilder, listener);
                customBuilder.setTitle(b.getString("title")).setMessage(b.getString("msg"));
                customBuilder.setIcon(getIcon(b.getInt("icon")));
                break;
        }
        return customBuilder.create();
    }

    private static void setButtonCaptions(Bundle b, MessageBox.Builder customBuilder, DialogInterface.OnClickListener listener) {
        int button = b.getInt("buttons");
        if (button == 0) {
            setButtons(customBuilder, Translation.get("abort"), Translation.get("retry"), Translation.get("ignore"), listener);
        } else if (button == 1) {
            setButtons(customBuilder, Translation.get("ok"), "", "", listener);
        } else if (button == 2) {
            setButtons(customBuilder, Translation.get("ok"), "", Translation.get("cancel"), listener);
        } else if (button == 3) {
            setButtons(customBuilder, Translation.get("retry"), "", Translation.get("cancel"), listener);
        } else if (button == 4) {
            setButtons(customBuilder, Translation.get("yes"), "", Translation.get("no"), listener);
        } else if (button == 5) {
            setButtons(customBuilder, Translation.get("yes"), Translation.get("no"), Translation.get("cancel"), listener);
        }
    }

    private static void setButtons(MessageBox.Builder customBuilder, String positive, String neutral, String negative, DialogInterface.OnClickListener listener) {
        customBuilder.setPositiveButton(positive, listener).setNeutralButton(neutral, listener).setNegativeButton(negative, listener);
    }

    private static Drawable getIcon(int which) {
        switch (which) {
            case 1:
            case 3:
            case 7:
                return Global.Icons[31];
            case 0:
            case 4:
                return Global.Icons[32];
            case 2:
            case 8:
                return Global.Icons[33];
            case 6:
                return Global.Icons[34];
            case 9:
                return Global.Icons[35];
            case 10:
                return Global.Icons[36];
            default:
                return null;
        }
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {

        public static int WindowWidth = 200;
        public static int WindowHeight = 200;
        public static float textSize = 12f;
        public static int ButtonHeight = 30;
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

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setIcon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNeutralButton(String neutralButtonText, DialogInterface.OnClickListener listener) {
            this.neutralButtonText = neutralButtonText;
            this.neutralButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Dialog create() {
            int winWidth = UiSizes.getInstance().isInitialized() ? UiSizes.getInstance().getWindowWidth() : WindowWidth;
            int winHeight = UiSizes.getInstance().isInitialized() ? UiSizes.getInstance().getWindowHeight() : WindowHeight;
            int buttonHeight = UiSizes.getInstance().isInitialized() ? UiSizes.getInstance().getButtonHeight() : ButtonHeight;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final Dialog dialog = new Dialog(context, R.style.Dialog);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.message_box_layout, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            if (title != null && !title.equals("")) {
                ((TextView) layout.findViewById(R.id.title)).setText(title);
            } else {
                layout.findViewById(R.id.title).setVisibility(GONE);
            }

            // set the confirm button
            if (positiveButtonText != null && !positiveButtonText.equals("")) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    layout.findViewById(R.id.positiveButton).setOnClickListener(v -> {
                        positiveButtonClickListener.onClick(dialog, Dialog.BUTTON_POSITIVE);
                        dialog.dismiss();
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(GONE);
            }
            // set the neutral button
            if (neutralButtonText != null && !neutralButtonText.equals("")) {
                ((Button) layout.findViewById(R.id.neutralButton)).setText(neutralButtonText);
                if (neutralButtonClickListener != null) {
                    layout.findViewById(R.id.neutralButton).setOnClickListener(v -> {
                        neutralButtonClickListener.onClick(dialog, Dialog.BUTTON_NEUTRAL);
                        dialog.dismiss();
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.neutralButton).setVisibility(GONE);
            }
            // set the cancel button
            if (negativeButtonText != null && !negativeButtonText.equals("")) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    layout.findViewById(R.id.negativeButton).setOnClickListener(v -> {
                        negativeButtonClickListener.onClick(dialog, Dialog.BUTTON_NEGATIVE);
                        dialog.dismiss();
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.negativeButton).setVisibility(GONE);
            }
            // set the content message
            if (message != null) {

                ((TextView) layout.findViewById(R.id.message)).setText(message);
                // ((TextView) layout.findViewById(R.id.message)).setTextSize(txtSize);

                layout.findViewById(R.id.message).measure(winWidth - 100, winHeight - 100);
                int height = layout.findViewById(R.id.message).getMeasuredHeight();

                LayoutParams params = layout.findViewById(R.id.ScrollView01).getLayoutParams();
                if (height > winHeight - (buttonHeight * 4)) {
                    height = winHeight - (buttonHeight * 4);
                    params.height = height;
                    layout.findViewById(R.id.ScrollView01).setLayoutParams(params);
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
                ImageView iv = layout.findViewById(R.id.icon);
                if (iv != null) iv.setVisibility(GONE);
            }

            setBackgroundDrawables(layout);

            dialog.setContentView(layout);
            return dialog;
        }

        private void setBackgroundDrawables(View layout) {
            Resources res = context.getResources();

            boolean NightMode = false;
            if (ViewManager.that != null)
                NightMode = Settings.nightMode.getValue();

            Drawable header = res.getDrawable(NightMode ? R.drawable.night_header : R.drawable.header);
            Drawable title = res.getDrawable(NightMode ? R.drawable.night_title : R.drawable.title);
            Drawable center = res.getDrawable(NightMode ? R.drawable.night_center : R.drawable.center);
            Drawable footer = res.getDrawable(NightMode ? R.drawable.night_footer : R.drawable.footer);

            layout.findViewById(R.id.header).setBackground(header);
            layout.findViewById(R.id.title).setBackground(title);
            layout.findViewById(R.id.content).setBackground(center);
            layout.findViewById(R.id.footer).setBackground(footer);

            ((TextView) layout.findViewById(R.id.title)).setTextColor(Global.getColor(R.attr.TextColor));
            TextView tvM = layout.findViewById(R.id.message);
            if (tvM != null)
                tvM.setTextColor(Global.getColor(R.attr.TextColor));
        }

    }

}
