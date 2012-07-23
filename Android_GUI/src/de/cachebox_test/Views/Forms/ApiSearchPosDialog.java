package de.cachebox_test.Views.Forms;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Coordinate;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import de.cachebox_test.R;
import de.cachebox_test.Custom_Controls.MultiToggleButton;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Ui.ActivityUtils;

public class ApiSearchPosDialog extends Activity implements ViewOptionsMenu
{
	public static ApiSearchPosDialog Me;

	private Intent aktIntent;
	private Context context;
	private CheckBox checkBoxExcludeFounds;
	private CheckBox checkBoxOnlyAvible;
	private CheckBox checkBoxExcludeHides;
	private TextView lblMarkerPos;
	private TextView lblRadius;
	private Button CurentMarkerPos;
	private EditText Radius;
	private Button CancelButton;
	private Button OKButton;
	private Button btnPlus;
	private Button btnMinus;
	private MultiToggleButton tglBtnGPS;
	private MultiToggleButton tglBtnMap;
	private int searcheState = 0; // 0=GPS, 1= Map, 2= Manuell

	private Coordinate actSearchPos;

	public void onCreate(Bundle savedInstanceState)
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.api_search_pos_dialog_layout);
		Me = this;

		context = this.getBaseContext();
		aktIntent = getIntent();
		((TextView) this.findViewById(R.id.title)).setText("Import");

		findViewById();

		OKButton.setText(GlobalCore.Translations.Get("ok"));
		CancelButton.setText(GlobalCore.Translations.Get("cancel"));

		CancelButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				;
				finish();
			}
		});

		OKButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				Config.settings.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
				Config.settings.SearchOnlyAvible.setValue(checkBoxOnlyAvible.isChecked());
				Config.settings.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

				int radius = 0;
				try
				{
					radius = Integer.parseInt(Radius.getText().toString());
				}
				catch (NumberFormatException e)
				{
					// Kein Integer
					e.printStackTrace();
				}

				if (radius != 0) Config.settings.lastSearchRadius.setValue(radius);

				Config.AcceptChanges();

				aktIntent.putExtra("SOMETHING", "EXTRAS");
				Bundle extras = new Bundle();
				extras.putSerializable("CoordResult", actSearchPos);
				aktIntent.putExtras(extras);
				setResult(RESULT_OK, aktIntent);
				finish();

			}
		});

		btnPlus.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				incrementRadius(1);
			}
		});

		btnMinus.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				incrementRadius(-1);
			}
		});

		tglBtnGPS.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				actSearchPos = GlobalCore.LastPosition;
				setToggleBtnState(0);
			}
		});

		tglBtnMap.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				actSearchPos = MapView.that.center;
				setToggleBtnState(1);
			}
		});

		if (MapView.that != null && MapView.that.isVisible())
		{
			actSearchPos = MapView.that.center;
			searcheState = 1;
		}
		else
		{
			actSearchPos = GlobalCore.LastPosition;
			searcheState = 0;
		}

		initialForm();

	}

	private void incrementRadius(int value)
	{
		try
		{
			int ist = Integer.parseInt(Radius.getText().toString());
			ist += value;

			if (ist > 100) ist = 100;
			if (ist < 1) ist = 1;

			Radius.setText(String.valueOf(ist));
		}
		catch (NumberFormatException e)
		{

		}
	}

	private void findViewById()
	{
		CancelButton = (Button) this.findViewById(R.id.cancelButton);
		OKButton = (Button) this.findViewById(R.id.OkButton);
		checkBoxExcludeFounds = (CheckBox) this.findViewById(R.id.api_exclude_founnds);
		checkBoxOnlyAvible = (CheckBox) this.findViewById(R.id.api_only_avible);
		checkBoxExcludeHides = (CheckBox) this.findViewById(R.id.api_exclud_hides);
		lblMarkerPos = (TextView) this.findViewById(R.id.api_lbl_marker_pos);
		lblRadius = (TextView) this.findViewById(R.id.api_lbl_radius);
		CurentMarkerPos = (Button) this.findViewById(R.id.api_marker_pos);
		Radius = (EditText) this.findViewById(R.id.api_radius);
		btnPlus = (Button) this.findViewById(R.id.api_radius_plus);
		btnMinus = (Button) this.findViewById(R.id.api_radius_minus);

		tglBtnGPS = (MultiToggleButton) this.findViewById(R.id.toggle_GPS);
		tglBtnMap = (MultiToggleButton) this.findViewById(R.id.toggle_Map);

		MultiToggleButton.initialOn_Off_ToggleStates(tglBtnGPS, GlobalCore.Translations.Get("FromGps"),
				GlobalCore.Translations.Get("FromGps"));
		MultiToggleButton.initialOn_Off_ToggleStates(tglBtnMap, GlobalCore.Translations.Get("FromMap"),
				GlobalCore.Translations.Get("FromMap"));

		checkBoxExcludeFounds.setText(GlobalCore.Translations.Get("SearchWithoutFounds"));
		checkBoxOnlyAvible.setText(GlobalCore.Translations.Get("SearchOnlyAvible"));
		checkBoxExcludeHides.setText(GlobalCore.Translations.Get("SearchWithoutOwns"));

		CurentMarkerPos.setFocusable(true);
		CurentMarkerPos.setFocusableInTouchMode(true);
		CurentMarkerPos.requestFocus();

		if (actSearchPos != null) CurentMarkerPos.setText(actSearchPos.FormatCoordinate());
		CurentMarkerPos.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// Koordinaten Dialog öffnen
				// Intent coordIntent = new Intent().setClass(CurentMarkerPos.getContext(), EditCoordinate.class);
				// Bundle b = new Bundle();
				// b.putSerializable("Coord", actSearchPos);
				// coordIntent.putExtras(b);
				// startActivityForResult(coordIntent, 0);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (data == null) return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			Coordinate coord = (Coordinate) bundle.getSerializable("CoordResult");
			if (coord != null)
			{
				actSearchPos.Latitude = coord.Latitude;
				actSearchPos.Longitude = coord.Longitude;
				setToggleBtnState(2);
			}
		}
	}

	private void initialForm()
	{
		OKButton.setWidth(UiSizes.getButtonWidthWide());
		CancelButton.setWidth(UiSizes.getButtonWidthWide());
		OKButton.setHeight(UiSizes.getQuickButtonHeight());
		CancelButton.setHeight(UiSizes.getQuickButtonHeight());
		checkBoxExcludeFounds.setChecked(Config.settings.SearchWithoutFounds.getValue());
		checkBoxOnlyAvible.setChecked(Config.settings.SearchOnlyAvible.getValue());
		checkBoxExcludeHides.setChecked(Config.settings.SearchWithoutOwns.getValue());
		Radius.setText(String.valueOf(Config.settings.lastSearchRadius.getValue()));
		setToggleBtnState();
	}

	private void setToggleBtnState(int value)
	{
		searcheState = value;
		setToggleBtnState();
	}

	private void setToggleBtnState()
	{// 0=GPS, 1= Map, 2= Manuell
		switch (searcheState)
		{
		case 0:
			tglBtnGPS.setState(1);
			tglBtnMap.setState(0);
			break;
		case 1:
			tglBtnGPS.setState(0);
			tglBtnMap.setState(1);
			break;
		case 2:
			tglBtnGPS.setState(0);
			tglBtnMap.setState(0);
			break;

		}
		CurentMarkerPos.setText(actSearchPos.FormatCoordinate());
	}

	@Override
	public void OnShow()
	{

	}

	@Override
	public void OnHide()
	{

	}

	@Override
	public void OnFree()
	{

	}

	@Override
	public int GetMenuId()
	{

		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{

	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{

		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{

	}

	@Override
	public int GetContextMenuId()
	{

		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu)
	{

	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item)
	{

		return false;
	}

}
