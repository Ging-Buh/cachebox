package de.cachebox_test.Views.Forms;

import CB_Core.GlobalCore;
import CB_Core.Math.UiSizes;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Ui.ActivityUtils;

public class HintDialog extends Activity
{
	Button bClose;
	Button bDecode;
	EditText etHint;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		ActivityUtils.setOriantation(this);
		setContentView(R.layout.hint);

		if (main.N)
		{
			this.setTheme(R.style.Theme_night_transparent);
		}

		Bundle bundle = getIntent().getExtras();
		String hint = GlobalCore.Rot13((String) bundle.getSerializable("Hint"));

		bClose = (Button) findViewById(R.id.hintdialog_button_close);
		bDecode = (Button) findViewById(R.id.hintdialog_button_decode);
		etHint = (EditText) findViewById(R.id.hintdialog_text);

		etHint.setMaxHeight(UiSizes.getWindowHeight() - (UiSizes.getQuickButtonHeight() * 4));

		etHint.setText(hint);
		bClose.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		bDecode.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				etHint.setText(GlobalCore.Rot13(etHint.getText().toString()));
			}
		});

		// Translations
		bClose.setText(GlobalCore.Translations.Get("close"));
		bDecode.setText(GlobalCore.Translations.Get("decode"));
		this.setTitle(GlobalCore.Translations.Get("hint"));

	}

}
