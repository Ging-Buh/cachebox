package de.droidcachebox.gdx.controls;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.views.forms.MessageBox;

public class Android_TextInput implements TextInputInterface {
    private Activity activity;

    public Android_TextInput(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void requestKeyboard(EditTextField editTextField) {
        try {
            if (editTextField == null) return;

            MessageBox.Builder builder = new MessageBox.Builder(activity);

            if (editTextField.getName().startsWith("*"))
                builder.setTitle(editTextField.getName().substring(1));
            else
                builder.setTitle("");

            // Set up the input
            final EditText input = new EditText(activity);

            // Specify the type of input expected;
            input.setText(editTextField.getText());
            if (editTextField.getInputType() == 0)
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE); //.TYPE_CLASS_TEXT
            else
                input.setInputType(editTextField.getInputType());
            input.setSingleLine(false);
            input.setLines(Math.min(editTextField.getLineCount(), 10)); // todo replace 10 by max nr of lines
            // builder.setView(input);
            builder.setContentView(input).setPositiveButton(Translation.get("ok"), (dialog, which) -> GL.that.RunOnGL(() -> {
                editTextField.setText(input.getText().toString());
                editTextField.setFocus(false);
            })
            ).setNegativeButton(Translation.get("cancel"), (dialog, which) -> {
                editTextField.setFocus(false);
                dialog.cancel();
            });
            activity.runOnUiThread(() -> {
                InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                // builder.show();
                builder.create().show();
                input.postDelayed(() -> {
                    input.requestFocus();
                    input.setSelection(input.getText().length());
                    if (manager != null)
                        manager.showSoftInput(input, 0);
                }, 100);
            });
        } catch (Exception ex) {
            Log.err("Android_TextInput", "requestKeyboard", ex);
        }
    }
}