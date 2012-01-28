package de.cachebox_test.Views.Forms;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Views.FilterSettings.EditFilterSettings;

public class ParkingDialog extends Activity
{
	Button bSelect;
	Button bAdd;
	Button bDel;

	TextView tvMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parking_dialog);

		final Intent aktIntent = getIntent();

		if (main.N)
		{
			this.setTheme(R.style.Theme_night_transparent);
		}

		bSelect = (Button) findViewById(R.id.parkdialog_button_select);
		bAdd = (Button) findViewById(R.id.parkdialog_button_add);
		bDel = (Button) findViewById(R.id.parkdialog_button_del);

		tvMsg = (TextView) findViewById(R.id.parkdialog_text);

		bSelect.setSingleLine(false);
		bAdd.setSingleLine(false);
		bDel.setSingleLine(false);

		tvMsg.setMaxHeight(UiSizes.getWindowHeight() - (UiSizes.getQuickButtonHeight() * 4));

		// Translations
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(GlobalCore.Translations.Get("My_Parking_Area_Title"));
		tvMsg.setText(GlobalCore.Translations.Get("My_Parking_Area_Text"));

		bSelect.setText(GlobalCore.Translations.Get("My_Parking_Area_select"));
		bAdd.setText(GlobalCore.Translations.Get("My_Parking_Area_Add"));
		bDel.setText(GlobalCore.Translations.Get("My_Parking_Area_Del"));

		bSelect.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				Cache cache = Database.Data.Query.GetCacheByGcCode("CBPark");

				if (cache != null) GlobalCore.SelectedCache(cache);
				finish();
			}
		});

		bAdd.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (GlobalCore.LastValidPosition != null)
				{
					CB_Core.Config.settings.ParkingLatitude.setValue(GlobalCore.LastValidPosition.Latitude);
					CB_Core.Config.settings.ParkingLongitude.setValue(GlobalCore.LastValidPosition.Longitude);
					EditFilterSettings.ApplyFilter(main.mainActivity, Global.LastFilter);
				}

				finish();
			}
		});

		bDel.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				CB_Core.Config.settings.ParkingLatitude.setValue(0);
				CB_Core.Config.settings.ParkingLongitude.setValue(0);
				EditFilterSettings.ApplyFilter(main.mainActivity, Global.LastFilter);
				finish();
			}
		});

	}

}
