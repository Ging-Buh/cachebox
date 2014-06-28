package de.cachebox_test.Ui;

import android.text.ClipboardManager;

import com.badlogic.gdx.utils.Clipboard;

@SuppressWarnings("deprecation")
public class AndroidClipboard implements Clipboard
{
	private String contents;
	private ClipboardManager cm;

	public AndroidClipboard(ClipboardManager Cm)
	{
		this.cm = Cm;
	}

	@Override
	public String getContents()
	{

		contents = (String) cm.getText();
		return contents;
	}

	@Override
	public void setContents(String contents)
	{
		this.contents = contents;

		cm.setText(contents);
	}

}
