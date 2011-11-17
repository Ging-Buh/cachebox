package de.droidcachebox.Views.AdvancedSettingsForms;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.openintents.intents.FileManagerIntents;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.Forms.NumerikInputBox;
import de.droidcachebox.Views.Forms.Settings;
import de.droidcachebox.Views.Forms.StringInputBox;
import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Enums.SmoothScrollingTyp;
import CB_Core.Settings.SettingBase;
import CB_Core.Settings.SettingBool;
import CB_Core.Settings.SettingCategory;
import CB_Core.Settings.SettingDouble;
import CB_Core.Settings.SettingEnum;
import CB_Core.Settings.SettingFile;
import CB_Core.Settings.SettingFolder;
import CB_Core.Settings.SettingInt;
import CB_Core.Settings.SettingIntArray;
import CB_Core.Settings.SettingModus;
import CB_Core.Settings.SettingsList;
import CB_Core.Settings.SettingString;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsListView extends Activity
{
	private static SettingsListView Me;

	private Context context;
	// private SettingsList settingsList;
	private CustomAdapter lvAdapter;
	private ListView listView;

	private Button CancelButton;
	private Button OKButton;

	public void onCreate(Bundle savedInstanceState)
	{
		// ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.advanced_settings_list);

		context = this.getBaseContext();
		Me = this;
		findViewById();

		// loadSettingsFromDB();

		OKButton.setText(GlobalCore.Translations.Get("ok"));
		CancelButton.setText(GlobalCore.Translations.Get("cancel"));

		OKButton.setWidth(Sizes.getButtonWidthWide());
		CancelButton.setWidth(Sizes.getButtonWidthWide());
		OKButton.setHeight(Sizes.getButtonHeight());
		CancelButton.setHeight(Sizes.getButtonHeight());

		CancelButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Config.settings.LoadFromLastValue();
				finish();
			}
		});

		OKButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Config.settings.SaveToLastValue();
				finish();

			}
		});

		ActivityUtils.setListViewPropertys(listView);

		Config.settings.SaveToLastValue();

		// SetListViewHeight Window-ButtonsLayout-margin
		LayoutParams para = listView.getLayoutParams();
		para.height = Sizes.getWindowHeight() - (Sizes.getButtonHeight() * 2) - (Sizes.getScaledFontSize_big() * 3);
		listView.setLayoutParams(para);

		lvAdapter = new CustomAdapter(this, Config.settings);
		listView.setAdapter(lvAdapter);
	}

	public void findViewById()
	{
		CancelButton = (Button) this.findViewById(R.id.cancelButton);
		OKButton = (Button) this.findViewById(R.id.OkButton);
		listView = (ListView) this.findViewById(R.id.settings_ListView);
	}

	public class CustomAdapter extends BaseAdapter
	{

		private Context context;
		private SettingsList mList;

		public CustomAdapter(Context context, SettingsList list)
		{
			this.context = context;
			this.mList = list;
		}

		public int getCount()
		{
			if (mList != null) return mList.values().size();
			else
				return 0;
		}

		public Object getItem(int position)
		{
			if (mList != null)
			{
				return mList.values().toArray()[position];
			}
			else
				return null;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (mList != null && mList.values().size() > 0)
			{
				Boolean BackGroundChanger = ((position % 2) == 1);
				try
				{
					View row = convertView;
					final SettingBase SB = (SettingBase) mList.values().toArray()[position];

					if (SB instanceof SettingBool)
					{
						return getBoolView((SettingBool) SB, convertView, parent);
					}
					else if (SB instanceof SettingIntArray)
					{
						return getIntArrayView((SettingIntArray) SB, convertView, parent);
					}
					else if (SB instanceof SettingInt)
					{
						return getIntView((SettingInt) SB, convertView, parent);
					}
					else if (SB instanceof SettingDouble)
					{
						return getDblView((SettingDouble) SB, convertView, parent);
					}
					else if (SB instanceof SettingFolder)
					{
						return getFolderView((SettingFolder) SB, convertView, parent);
					}
					else if (SB instanceof SettingFile)
					{
						return getFileView((SettingFile) SB, convertView, parent);
					}
					else if (SB instanceof SettingEnum)
					{
						return getEnumView((SettingEnum) SB, convertView, parent);
					}
					else if (SB instanceof SettingString)
					{
						return getStringView((SettingString) SB, convertView, parent);
					}

					return row;
				}
				catch (Exception e)
				{
					return convertView;
				}
			}
			else
				return null;
		}

	}

	/***
	 * Enthält den Key des zu Editierenden Wertes der SettingsList
	 */
	public static String EditKey = "";

	private View getBoolView(final SettingBool SB, View convertView, ViewGroup parent)
	{

		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_bool, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_small());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText("default: " + String.valueOf(SB.getDefaultValue()));
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.7));
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		CheckBox chk = (CheckBox) row.findViewById(R.id.checkBox1);

		chk.setChecked(SB.getValue());
		chk.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				SB.setValue(isChecked);

			}
		});

		return row;

	}

	private View getStringView(final SettingString SB, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_small());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(SB.getValue());
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.7));
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				SettingsListView.EditKey = SB.getName();
				// Show NumPad Int Edit
				StringInputBox.Show(SB.getName(), "default: " + SB.getDefaultValue(), SB.getValue(), new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int button)
					{
						String text = StringInputBox.editText.getText().toString();
						switch (button)
						{
						case -1: // ok Clicket
							SettingString value = (SettingString) Config.settings.get(SettingsListView.EditKey);
							if (value != null) value.setValue(text);
							SettingsListView.Me.ListInvalidate();
							break;
						case -2: // cancel clicket

							break;
						case -3:

							break;
						}

						dialog.dismiss();

					}
				}, SettingsListView.Me);
			}
		});

		return row;

	}

	private View getEnumView(final SettingEnum SB, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_enum, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_small());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		final Spinner spinner = (Spinner) row.findViewById(R.id.spinner1);

		if (spinner.getAdapter() == null)
		{
			ArrayAdapter<String> enumAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SB.getValues());
			enumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(enumAdapter);
			spinner.setSelection(SB.getValues().indexOf(SB.getValue()));

			spinner.setOnItemSelectedListener(new OnItemSelectedListener()
			{

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
				{
					if (SB != null) SB.setValue((String) SB.getValues().get(arg2));
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0)
				{
					// do nothing
				}
			});
		}
		return row;

	}

	private View getIntArrayView(final SettingIntArray SB, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_enum, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_small());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		final Spinner spinner = (Spinner) row.findViewById(R.id.spinner1);

		if (spinner.getAdapter() == null)
		{
			ArrayAdapter<Integer> enumAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, SB.getValues());
			enumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(enumAdapter);
			spinner.setSelection(SB.getIndex());

			spinner.setOnItemSelectedListener(new OnItemSelectedListener()
			{

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
				{
					if (SB != null) SB.setValue(SB.getValueFromIndex(arg2));
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0)
				{
					// do nothing
				}
			});
		}
		return row;

	}

	private View getIntView(final SettingInt SB, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_small());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(String.valueOf(SB.getValue()));
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.7));
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				SettingsListView.EditKey = SB.getName();
				// Show NumPad Int Edit
				NumerikInputBox.Show(SB.getName(), "default: " + String.valueOf(SB.getDefaultValue()), SB.getValue(),
						new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface dialog, int button)
							{
								String text = NumerikInputBox.editText.getText().toString();
								switch (button)
								{
								case -1: // ok Clicket

									try
									{
										int newValue = Integer.parseInt(text);
										SettingInt value = (SettingInt) Config.settings.get(SettingsListView.EditKey);
										if (value != null) value.setValue(newValue);
										SettingsListView.Me.ListInvalidate();
									}
									catch (NumberFormatException e)
									{
										// falsche Eingabe
									}
									break;
								case -2: // cancel clicket

									break;
								case -3:

									break;
								}

								dialog.dismiss();

							}
						}, SettingsListView.Me);
			}
		});

		return row;

	}

	private View getDblView(final SettingDouble SB, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_small());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(String.valueOf(SB.getValue()));
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.7));
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				SettingsListView.EditKey = SB.getName();
				// Show NumPad Int Edit
				NumerikInputBox.Show(SB.getName(), "default: " + String.valueOf(SB.getDefaultValue()), SB.getValue(),
						new DialogInterface.OnClickListener()
						{

							@Override
							public void onClick(DialogInterface dialog, int button)
							{
								String text = NumerikInputBox.editText.getText().toString();
								switch (button)
								{
								case -1: // ok Clicket
									try
									{
										double newValue = Double.parseDouble(text);
										SettingDouble value = (SettingDouble) Config.settings.get(SettingsListView.EditKey);
										if (value != null) value.setValue(newValue);
										SettingsListView.Me.ListInvalidate();
									}
									catch (NumberFormatException e)
									{
										// falsche Eingabe
									}
									break;
								case -2: // cancel clicket

									break;
								case -3:

									break;
								}

								dialog.dismiss();

							}
						}, SettingsListView.Me);
			}
		});

		return row;

	}

	private View getFolderView(final SettingFolder SB, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_small());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(SB.getValue());
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.7));
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				SettingsListView.EditKey = SB.getName();

				Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);

				// Construct URI from file name.
				File file = new File(SB.getValue());
				intent.setData(Uri.fromFile(file));

				// Set fancy title and button (optional)
				intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Select Folder");
				intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, "Select");

				try
				{
					SettingsListView.Me.startActivityForResult(intent, Global.REQUEST_CODE_PICK_DIRECTORY);
				}
				catch (ActivityNotFoundException e)
				{
					// No compatible file manager was found.
					Toast.makeText(main.mainActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
				}
			}
		});

		return row;

	}

	private View getFileView(final SettingFile SB, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_small());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(SB.getValue());
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.7));
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				SettingsListView.EditKey = SB.getName();

				Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);

				// Construct URI from file name.
				File file = new File(SB.getValue());
				intent.setData(Uri.fromFile(file));

				// Set fancy title and button (optional)
				intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Select file to open");
				intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, "Select");

				try
				{
					SettingsListView.Me.startActivityForResult(intent, Global.REQUEST_CODE_PICK_FILE);
				}
				catch (ActivityNotFoundException e)
				{
					// No compatible file manager was found.
					Toast.makeText(main.mainActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
				}
			}
		});

		return row;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case Global.REQUEST_CODE_PICK_FILE:
			if (resultCode == android.app.Activity.RESULT_OK && data != null)
			{
				// obtain the filename
				Uri fileUri = data.getData();
				if (fileUri != null)
				{
					String filePath = fileUri.getPath();
					if (filePath != null)
					{
						SettingFile value = (SettingFile) Config.settings.get(SettingsListView.EditKey);
						if (value != null) value.setValue(filePath);
						SettingsListView.Me.ListInvalidate();
					}
				}
			}
			break;

		case Global.REQUEST_CODE_PICK_DIRECTORY:
			if (resultCode == android.app.Activity.RESULT_OK && data != null)
			{
				// obtain the filename
				Uri fileUri = data.getData();
				if (fileUri != null)
				{
					String filePath = fileUri.getPath();
					if (filePath != null)
					{
						SettingFolder value = (SettingFolder) Config.settings.get(SettingsListView.EditKey);
						if (value != null) value.setValue(filePath);
						SettingsListView.Me.ListInvalidate();
					}
				}
			}
			break;
		}
	}

	public void ListInvalidate()
	{
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				Thread t = new Thread()
				{
					public void run()
					{
						SettingsListView.Me.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								// lvAdapter = new
								// CustomAdapter(SettingsListView.Me,
								// settingsList);
								lvAdapter = new CustomAdapter(SettingsListView.Me, Config.settings);
								listView.setAdapter(lvAdapter);
							}
						});
					}
				};

				t.start();

			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 500);
	}

}
