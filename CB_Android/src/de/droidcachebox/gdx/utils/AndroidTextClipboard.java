package de.droidcachebox.gdx.utils;

import android.text.ClipboardManager;
import com.badlogic.gdx.utils.Clipboard;

public class AndroidTextClipboard implements Clipboard {
    private String contents;
    private ClipboardManager cm;

    public AndroidTextClipboard(ClipboardManager Cm) {
        this.cm = Cm;
    }

    @Override
    public boolean hasContents() {
        return cm.getText().length() > 0;
    }

    @Override
    public String getContents() {

        contents = (String) cm.getText();
        return contents;
    }

    @Override
    public void setContents(String contents) {
        this.contents = contents;

        cm.setText(contents);
    }

}
