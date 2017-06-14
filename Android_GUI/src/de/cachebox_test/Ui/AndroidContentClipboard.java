package de.cachebox_test.Ui;

import com.badlogic.gdx.utils.Clipboard;

import android.content.ClipboardManager;

public class AndroidContentClipboard implements Clipboard {
	private String contents;
	private ClipboardManager cm;

	public AndroidContentClipboard(ClipboardManager Cm) {
		this.cm = Cm;
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
