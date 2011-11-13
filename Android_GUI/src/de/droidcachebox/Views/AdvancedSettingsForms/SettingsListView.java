package de.droidcachebox.Views.AdvancedSettingsForms;

import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.Forms.NumerikInputBox;
import de.droidcachebox.Views.Forms.Settings;
import de.droidcachebox.Views.Forms.StringInputBox;
import CB_Core.GlobalCore;
import CB_Core.Settings.SettingBase;
import CB_Core.Settings.SettingBool;
import CB_Core.Settings.SettingCategory;
import CB_Core.Settings.SettingDouble;
import CB_Core.Settings.SettingInt;
import CB_Core.Settings.SettingModus;
import CB_Core.Settings.SettingsList;
import CB_Core.Settings.SettingString;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsListView extends Activity
{
	private static SettingsListView Me;

	private Context context;
	private SettingsList settingsList;
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

		loadSettingsFromDB();

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
				finish();
			}
		});

		OKButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// aktIntent.putExtra("SOMETHING", "EXTRAS");
				// Bundle extras = new Bundle();
				// extras.putSerializable("PqList", PqList);
				// aktIntent.putExtras(extras);
				// setResult(RESULT_OK, aktIntent);
				finish();

			}
		});

		lvAdapter = new CustomAdapter(this, settingsList);
		listView.setAdapter(lvAdapter);
	}

	public void loadSettingsFromDB()
	{
		// Tmp gefüllt für Layout Tests

		settingsList = new SettingsList();

		settingsList.put("Test1", new SettingString("Test1", SettingCategory.Gps, SettingModus.Normal, "default 1", false));
		settingsList.put("Test2", new SettingString("Test2", SettingCategory.Gps, SettingModus.Normal, "default 2", false));
		settingsList.put("Test3", new SettingString("Test3", SettingCategory.Gps, SettingModus.Normal, "default 3", false));

		settingsList.put("Test4", new SettingBool("Test4", SettingCategory.Gps, SettingModus.Normal, true, false));
		settingsList.put("Test5", new SettingBool("Test5", SettingCategory.Gps, SettingModus.Normal, false, false));

		settingsList.put("Test6", new SettingInt("Test6", SettingCategory.Gps, SettingModus.Normal, 10, false));
		settingsList.put("Test7", new SettingInt("Test7", SettingCategory.Gps, SettingModus.Normal, 200, false));

		settingsList.put("Test8", new SettingDouble("Test8", SettingCategory.Gps, SettingModus.Normal, 10.56, false));
		settingsList.put("Test9", new SettingDouble("Test9", SettingCategory.Gps, SettingModus.Normal, 200.1123, false));
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
				try
				{
					View row = convertView;
					final SettingBase SB = (SettingBase) mList.values().toArray()[position];

					if (SB instanceof SettingString)
					{
						return getStringView((SettingString) SB, convertView, parent);
					}
					else if (SB instanceof SettingBool)
					{
						return getBoolView((SettingBool) SB, convertView, parent);
					}
					else if (SB instanceof SettingInt)
					{
						return getIntView((SettingInt) SB, convertView, parent);
					}
					else if (SB instanceof SettingDouble)
					{
						return getDblView((SettingDouble) SB, convertView, parent);
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
	public static String EditKey;

	private View getBoolView(final SettingBool SB, View convertView, ViewGroup parent)
	{

		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_bool, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_normal());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText("default: " + String.valueOf(SB.getDefaultValue()));
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.8));
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
		label.setTextSize(Sizes.getScaledFontSize_normal());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(SB.getValue());
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.8));
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		Button btnEdit = (Button) row.findViewById(R.id.btn_Set);

		btnEdit.setOnClickListener(new OnClickListener()
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

							SettingString value = (SettingString) SettingsListView.Me.settingsList.get(SettingsListView.EditKey);
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

	private View getIntView(final SettingInt SB, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_normal());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(String.valueOf(SB.getValue()));
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.8));
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		Button btnEdit = (Button) row.findViewById(R.id.btn_Set);

		btnEdit.setOnClickListener(new OnClickListener()
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
									int newValue = Integer.parseInt(text);

									SettingInt value = (SettingInt) SettingsListView.Me.settingsList.get(SettingsListView.EditKey);
									if (value != null) value.setValue(newValue);
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

	private View getDblView(final SettingDouble SB, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(SB.getName());
		label.setTextSize(Sizes.getScaledFontSize_normal());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(String.valueOf(SB.getValue()));
		label2.setTextSize((float) (Sizes.getScaledFontSize_small() * 0.8));
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		Button btnEdit = (Button) row.findViewById(R.id.btn_Set);

		btnEdit.setOnClickListener(new OnClickListener()
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
									double newValue = Double.parseDouble(text);

									SettingDouble value = (SettingDouble) SettingsListView.Me.settingsList.get(SettingsListView.EditKey);
									if (value != null) value.setValue(newValue);
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
								lvAdapter = new CustomAdapter(SettingsListView.Me, settingsList);
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
