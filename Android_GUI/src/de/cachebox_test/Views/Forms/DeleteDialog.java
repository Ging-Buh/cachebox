package de.cachebox_test.Views.Forms;

import CB_Core.GlobalCore;
import CB_Core.Math.UiSizes;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Ui.ActivityUtils;

public class DeleteDialog extends Activity
{
	Button bArchived;
	Button bFound;
	Button bFilter;
	Button bCancel;
	TextView tvMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ActivityUtils.setOriantation(this);
		setContentView(R.layout.del_caches);

		final Intent aktIntent = getIntent();

		if (main.N)
		{
			this.setTheme(R.style.Theme_night_transparent);
		}

		bArchived = (Button) findViewById(R.id.deldialog_button_archived);
		bFound = (Button) findViewById(R.id.deldialog_button_found);
		bFilter = (Button) findViewById(R.id.deldialog_button_filter);
		bCancel = (Button) findViewById(R.id.deldialog_button_cancel);
		tvMsg = (TextView) findViewById(R.id.deldialog_text);

		bArchived.setSingleLine(false);
		bFound.setSingleLine(false);
		bFilter.setSingleLine(false);

		bCancel.setSingleLine(false);

		tvMsg.setMaxHeight(UiSizes.getWindowHeight() - (UiSizes.getQuickButtonHeight() * 4));

		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		// Translations
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(GlobalCore.Translations.Get("DeleteCaches"));
		tvMsg.setText(GlobalCore.Translations.Get("MsgDelCaches"));
		bCancel.setText(GlobalCore.Translations.Get("close"));
		bArchived.setText(GlobalCore.Translations.Get("DelArchived"));
		bFound.setText(GlobalCore.Translations.Get("DelFound"));
		bFilter.setText(GlobalCore.Translations.Get("DelActFilter"));

		bCancel.setText(bCancel.getText() + "\n");

		bArchived.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Bundle extras = new Bundle();
				extras.putSerializable("DelResult", 0);
				aktIntent.putExtras(extras);
				setResult(RESULT_OK, aktIntent);
				finish();
			}
		});

		bFound.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Bundle extras = new Bundle();
				extras.putSerializable("DelResult", 1);
				aktIntent.putExtras(extras);
				setResult(RESULT_OK, aktIntent);
				finish();
			}
		});

		bFilter.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Bundle extras = new Bundle();
				extras.putSerializable("DelResult", 2);
				aktIntent.putExtras(extras);
				setResult(RESULT_OK, aktIntent);
				finish();
			}
		});

	}

}
