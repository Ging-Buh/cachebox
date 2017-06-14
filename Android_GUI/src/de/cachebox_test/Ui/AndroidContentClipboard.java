package de.cachebox_test.Ui;

import com.badlogic.gdx.utils.Clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;

public class AndroidContentClipboard implements Clipboard {
	private String contents;
	private final ClipboardManager cm;

	public AndroidContentClipboard(ClipboardManager Cm) {
		this.cm = Cm;
	}

	@Override
	public String getContents() {
		contents = "";
		if (cm.hasPrimaryClip()) {
			ClipData cd = cm.getPrimaryClip();
			if (cd.getItemCount() > 0) {
				contents = cd.getItemAt(0).getText().toString();
			}
		}
		return contents;
	}

	@Override
	public void setContents(String contents) {
		this.contents = contents;
		ClipData cd = new ClipData(contents, null, null);
		cm.setPrimaryClip(cd);
	}

}
