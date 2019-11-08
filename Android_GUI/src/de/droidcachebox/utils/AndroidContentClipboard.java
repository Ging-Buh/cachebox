package de.droidcachebox.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import com.badlogic.gdx.utils.Clipboard;

public class AndroidContentClipboard implements Clipboard {
    private final ClipboardManager cm;
    private String contents;

    public AndroidContentClipboard(ClipboardManager Cm) {
        this.cm = Cm;
    }

    @Override
    public String getContents() {
        contents = "";
        if (cm.hasPrimaryClip()) {
            ClipData cd = cm.getPrimaryClip();
            if (cd.getItemCount() > 0) {
                CharSequence cs = cd.getItemAt(0).getText();
                if (cs != null)
                    contents = cs.toString();
                else {
                    // maybe it contains a URI
                    // resolveUri(cd.getItemAt(0).getUri())
                }
            }
        }
        return contents;
    }

    @Override
    public void setContents(String contents) {
        this.contents = contents;
        ClipData cd = ClipData.newPlainText("", contents);
        cm.setPrimaryClip(cd);
    }

}
