package de.droidcachebox.Views.Forms;

import de.droidcachebox.R;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.ActivityUtils;
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
import CB_Core.GlobalCore;

public class ApiSearchPosDialog extends Activity implements ViewOptionsMenu
{
	public static ApiSearchPosDialog Me;

	private Context context;
	private CheckBox checkBoxExcludeFounds;
	private CheckBox checkBoxOnlyAvible;
	private CheckBox checkBoxExcludeHides;
	private TextView lblMarkerPos;
	private TextView lblRadius;
	private EditText CurentMarkerPos;
	private EditText Radius;
	private Button CancelButton;
	private Button OKButton;
	private Button btnPlus;
	private Button btnMinus;

	public void onCreate(Bundle savedInstanceState)
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.api_search_pos_dialog_layout);
		Me = this;

		context = this.getBaseContext();

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

		initialForm();

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
		CurentMarkerPos = (EditText) this.findViewById(R.id.api_marker_pos);
		Radius = (EditText) this.findViewById(R.id.api_radius);
		btnPlus = (Button) this.findViewById(R.id.api_radius_plus);
		btnMinus = (Button) this.findViewById(R.id.api_radius_minus);
	}

	private void initialForm()
	{

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
