package de.droidcachebox;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.GL_Listener.TextInputInterface;
import CB_Utils.Log.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Android_TextInput implements TextInputInterface {
    private Activity activity;

    public Android_TextInput(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void requestKeyboard(EditTextField editTextField) {
        try {
            if (editTextField == null) return;

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(""); // todo setting a Title

            // Set up the input
            final EditText input = new EditText(activity);

            // Specify the type of input expected;
            input.setText(editTextField.getText());
            input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE); //.TYPE_CLASS_TEXT
            input.setSingleLine(false);
            input.setLines(Math.min(editTextField.getLineCount(), 10)); // todo replace 10 by max nr of lines
            builder.setView(input);
            // Set up the buttons
            builder.setPositiveButton(Translation.Get("ok"), (dialog, which) -> {
                        editTextField.setText(input.getText().toString());
                        editTextField.setFocus(false);
                    }
            );
            builder.setNegativeButton(Translation.Get("cancel"), (dialog, which) -> {
                editTextField.setFocus(false);
                dialog.cancel();
            });
            activity.runOnUiThread(() -> {
                InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                builder.show();
                input.postDelayed(() -> {
                    input.requestFocus();
                    input.setSelection(input.getText().length());
                    manager.showSoftInput(input, 0);
                }, 100);
            });
        } catch (Exception e) {
            Log.err("Android_TextInput", e.getLocalizedMessage(), e);
        }
    }
}