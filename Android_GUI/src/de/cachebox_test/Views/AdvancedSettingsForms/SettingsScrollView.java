/* 
 * Copyright (C) 2011 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cachebox_test.Views.AdvancedSettingsForms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.openintents.intents.FileManagerIntents;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Math.UiSizes;
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
import CB_Core.Settings.SettingString;
import CB_Core.Settings.SettingTime;
import CB_Core.Solver.Functions.Function;
import CB_Core.TranslationEngine.LangStrings.Langs;
import android.R.color;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Components.Animations;
import de.cachebox_test.Custom_Controls.downSlider;
import de.cachebox_test.Custom_Controls.IconContextMenu.IconContextMenu;
import de.cachebox_test.Custom_Controls.IconContextMenu.IconContextMenu.IconContextItemSelectedListener;
import de.cachebox_test.Custom_Controls.wheel.OnWheelScrollListener;
import de.cachebox_test.Custom_Controls.wheel.WheelView;
import de.cachebox_test.Custom_Controls.wheel.adapters.NumericWheelAdapter;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.AllContextMenuCallHandler;
import de.cachebox_test.Views.Forms.MessageBox;
import de.cachebox_test.Views.Forms.NumerikInputBox;
import de.cachebox_test.Views.Forms.StringInputBox;

/**
 * @author Longri
 */
public class SettingsScrollView extends Activity
{
	private Intent aktIntent;
	private Button btnOk;
	private Button btnCancel;
	private LinearLayout content;
	private Function selectedFunction;
	private Context context;
	private ScrollView scrollView;

	public static SettingsScrollView Me;

	private SettingBase selectedItem;
	private int selectedPosition = -1;

	private ArrayList<SettingCategory> Categorys = new ArrayList<SettingCategory>();

	public void onCreate(Bundle savedInstanceState)
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_scroll_layout);
		context = this;
		Me = this;
		if (!GlobalCore.isTab) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		aktIntent = getIntent();
		this.getWindow().setBackgroundDrawableResource(Config.settings.nightMode.getValue() ? color.darker_gray : color.background_dark);

		findViewsById();
		setLang();

		fillContent();

		Config.settings.SaveToLastValue();
		btnCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Config.settings.LoadFromLastValue();
				finish();
			}
		});

		btnOk.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				boolean QuickButtonShowChanged = Config.settings.quickButtonShow.isDirty();

				Config.settings.SaveToLastValue();
				int Time = Config.settings.ScreenLock.getValue();

				((main) main.mainActivity).setScreenLockTimerNew(Time);

				if (QuickButtonShowChanged)
				{
					Config.settings.quickButtonLastShow.setValue(true);
					downSlider.ButtonShowStateChanged();
				}

				Config.AcceptChanges();
				finish();

			}
		});

	}

	void findViewsById()
	{
		((LinearLayout) findViewById(R.id.main_LinearLayout)).setBackgroundColor(Global.getColor(R.attr.EmptyBackground));
		btnOk = (Button) findViewById(R.id.solver_function_ok);
		btnCancel = (Button) findViewById(R.id.solver_function_cancel);
		content = (LinearLayout) findViewById(R.id.settings_scrollView_content);
		scrollView = (ScrollView) findViewById(R.id.settings_scrollView);
	}

	void setLang()
	{
		btnOk.setText(GlobalCore.Translations.Get("ok"));
		btnCancel.setText(GlobalCore.Translations.Get("cancel"));
	}

	void fillContent()
	{

		// Categorie List zusammen stellen

		if (Categorys == null)
		{
			Categorys = new ArrayList<SettingCategory>();
		}

		Categorys.clear();
		SettingCategory[] tmp = SettingCategory.values();
		for (SettingCategory item : tmp)
		{
			if (item != SettingCategory.Button)
			{
				Categorys.add(item);
			}

		}

		Resources res = this.getResources();
		Iterator<SettingCategory> iteratorCat = Categorys.iterator();

		if (iteratorCat != null && iteratorCat.hasNext())
		{

			SettingsListButtonLangSpinner lang = new SettingsListButtonLangSpinner("Lang", SettingCategory.Button, SettingModus.Normal,
					true);
			View langView = getLangSpinnerView(lang, content);
			langView.setMinimumWidth(UiSizes.getQuickButtonWidth());
			langView.setMinimumHeight(UiSizes.getQuickButtonHeight());
			content.addView(langView);

			SettingsListCategoryButton quick = new SettingsListCategoryButton("QuickList", SettingCategory.Button, SettingModus.Normal,
					true);
			View quickView = getButtonView(quick, content, false);
			content.addView(quickView);

			ArrayList<SettingBase> SortedSettingList = new ArrayList<SettingBase>();// Config.settings.values().toArray();

			for (Iterator<SettingBase> it = Config.settings.values().iterator(); it.hasNext();)
			{
				SortedSettingList.add(it.next());
			}

			Collections.sort(SortedSettingList);

			do
			{
				int position = 0;

				SettingCategory cat = iteratorCat.next();
				SettingsListCategoryButton catBtn = new SettingsListCategoryButton(cat.name(), SettingCategory.Button, SettingModus.Normal,
						true);

				final View btn = getView(catBtn, content, true);
				content.addView(btn);

				// add Cat einträge
				final LinearLayout lay = new LinearLayout(this);
				int entrieCount = 0;
				if (cat == SettingCategory.Login)
				{
					SettingsListGetApiButton lgIn = new SettingsListGetApiButton(cat.name(), SettingCategory.Button, SettingModus.Normal,
							true);
					final View btnLgIn = getView(lgIn, content, true);
					lay.addView(btnLgIn);
					entrieCount++;
				}

				if (cat == SettingCategory.Debug)
				{
					SettingsListCategoryButton disp = new SettingsListCategoryButton("DebugDisplayInfo", SettingCategory.Button,
							SettingModus.Normal, true);
					final View btnDisp = getView(disp, content, true);
					lay.addView(btnDisp);
					entrieCount++;
				}

				Boolean expandLayout = false;

				// int layoutHeight = 0;
				for (Iterator<SettingBase> it = SortedSettingList.iterator(); it.hasNext();)
				{
					SettingBase settingItem = it.next();
					if (settingItem.getCategory().name().equals(cat.name()))
					{
						// item nur zur Liste Hinzufügen, wenn der
						// SettingModus
						// dies auch zu lässt.
						if (((settingItem.getModus() == SettingModus.Normal)
								|| (settingItem.getModus() == SettingModus.Expert && Config.settings.SettingsShowExpert.getValue()) || Config.settings.SettingsShowAll
									.getValue()) && (settingItem.getModus() != SettingModus.Never))
						{

							if (settingItem.getName().equals("GcJoker"))
							{
								if (Config.settings.hasCallPermission.getValue())
								{
									Boolean BackGroundChanger = ((position % 2) == 1);
									position++;

									View view = getView(settingItem, lay, BackGroundChanger);

									lay.addView(view);
									entrieCount++;
									if (settingItem.getName().equals(EditKey))
									{
										expandLayout = true;
									}
								}
							}
							else
							{
								Boolean BackGroundChanger = ((position % 2) == 1);
								position++;

								View view = getView(settingItem, lay, BackGroundChanger);

								lay.addView(view);
								entrieCount++;
								if (settingItem.getName().equals(EditKey))
								{
									expandLayout = true;
								}
							}
						}
					}
				}

				if (entrieCount > 0)
				{

					lay.setOrientation(LinearLayout.VERTICAL);

					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);

					// layoutParams.setMargins(20, 0, 20, 0);

					lay.setLayoutParams(layoutParams);
					lay.setBackgroundDrawable(Global.getDrawable(R.drawable.day_settings_group, res));
					if (!expandLayout) lay.setVisibility(View.GONE);
					content.addView(lay);
					// }

					btn.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View arg0)
						{
							Animations.ToggleViewSlideUp_Down((View) lay, context, scrollView, btn);
						}
					});
				}
				else
				{
					content.removeView(btn);
				}
			}
			while (iteratorCat.hasNext());

		}
	}

	void resortList()
	{
		content.removeAllViews();
		fillContent();
		scrollView.scrollTo(0, scrollView.getScrollY());
	}

	/***
	 * Enthält den Key des zu Editierenden Wertes der SettingsList
	 */
	public static String EditKey = "";

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();

		AllContextMenuCallHandler.icm = new IconContextMenu(this, R.menu.menu_settings_view_mode);
		AllContextMenuCallHandler.icm.setOnIconContextItemSelectedListener(OnIconContextItemSelectedListener);
		Menu IconMenu = AllContextMenuCallHandler.icm.getMenu();

		MenuItem miExpert = IconMenu.findItem(R.id.miSettings_show_Expert);
		MenuItem miAll = IconMenu.findItem(R.id.miSettings_show_All);

		miExpert.setChecked(Config.settings.SettingsShowExpert.getValue());
		miAll.setChecked(Config.settings.SettingsShowAll.getValue());

		AllContextMenuCallHandler.icm.show();

		return super.onPrepareOptionsMenu(IconMenu);
	}

	public IconContextItemSelectedListener OnIconContextItemSelectedListener = new IconContextItemSelectedListener()
	{

		@Override
		public void onIconContextItemSelected(MenuItem item, Object info)
		{
			switch (item.getItemId())
			{

			case R.id.miSettings_show_Expert:
				Config.settings.SettingsShowExpert.setValue(!Config.settings.SettingsShowExpert.getValue());
				resortList();
				return;

			case R.id.miSettings_show_All:
				Config.settings.SettingsShowAll.setValue(!Config.settings.SettingsShowAll.getValue());
				resortList();
				return;

			}

		}
	};

	private View getBoolView(final SettingBool SB, ViewGroup parent, boolean BackgroundChanger)
	{

		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_bool, parent, false);

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(GlobalCore.Translations.Get(SB.getName()));
		label.setTextSize(UiSizes.getScaledFontSize_big());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText("default: " + String.valueOf(SB.getDefaultValue()));
		label2.setTextSize((float) UiSizes.getScaledFontSize());
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

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;

	}

	private View getView(SettingBase SB, ViewGroup parent, boolean BackgroundChanger)
	{
		if (SB instanceof SettingBool)
		{
			return getBoolView((SettingBool) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingIntArray)
		{
			return getIntArrayView((SettingIntArray) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingTime)
		{
			return getTimeView((SettingTime) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingInt)
		{
			return getIntView((SettingInt) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingDouble)
		{
			return getDblView((SettingDouble) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingFolder)
		{
			return getFolderView((SettingFolder) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingFile)
		{
			return getFileView((SettingFile) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingEnum)
		{
			return getEnumView((SettingEnum) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingString)
		{
			return getStringView((SettingString) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingsListCategoryButton)
		{
			return getButtonView((SettingsListCategoryButton) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingsListGetApiButton)
		{
			return getApiKeyButtonView((SettingsListGetApiButton) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingsListButtonLangSpinner)
		{
			return getLangSpinnerView((SettingsListButtonLangSpinner) SB, parent);
		}

		return null;
	}

	private View getStringView(final SettingString SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(GlobalCore.Translations.Get(SB.getName()));
		label.setTextSize(UiSizes.getScaledFontSize_big());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(SB.getValue());
		label2.setTextSize((float) UiSizes.getScaledFontSize());
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				SettingsScrollView.EditKey = SB.getName();
				selectedItem = SB;
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
							SettingString value = (SettingString) Config.settings.get(SettingsScrollView.EditKey);
							if (value != null) value.setValue(text);
							SettingsScrollView.Me.ListInvalidate();
							break;
						case -2: // cancel clicket

							break;
						case -3:

							break;
						}

						dialog.dismiss();

					}
				}, SettingsScrollView.Me);
			}
		});

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;

	}

	private View getEnumView(final SettingEnum SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_enum, parent, false);

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(GlobalCore.Translations.Get(SB.getName()));
		label.setTextSize(UiSizes.getScaledFontSize_big());
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
					if (SB != null)
					{
						selectedItem = SB;
						SB.setValue((String) SB.getValues().get(arg2));
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0)
				{
					if (SB != null)
					{
						selectedItem = SB;

					}
				}
			});
		}

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;

	}

	private View getIntArrayView(final SettingIntArray SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_enum, parent, false);

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(GlobalCore.Translations.Get(SB.getName()));
		label.setTextSize(UiSizes.getScaledFontSize_big());
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
					if (SB != null)
					{
						selectedItem = SB;
						SB.setValue(SB.getValueFromIndex(arg2));
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0)
				{
					if (SB != null)
					{
						selectedItem = SB;

					}
				}
			});
		}

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;

	}

	private View getIntView(final SettingInt SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(GlobalCore.Translations.Get(SB.getName()));
		label.setTextSize(UiSizes.getScaledFontSize_big());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(String.valueOf(SB.getValue()));
		label2.setTextSize((float) UiSizes.getScaledFontSize());
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				selectedItem = SB;
				SettingsScrollView.EditKey = SB.getName();
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
										SettingInt value = (SettingInt) Config.settings.get(SettingsScrollView.EditKey);
										if (value != null) value.setValue(newValue);
										SettingsScrollView.Me.ListInvalidate();
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
						}, SettingsScrollView.Me);
			}
		});

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;

	}

	private View getDblView(final SettingDouble SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(GlobalCore.Translations.Get(SB.getName()));
		label.setTextSize(UiSizes.getScaledFontSize_big());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(String.valueOf(SB.getValue()));
		label2.setTextSize((float) UiSizes.getScaledFontSize());
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				selectedItem = SB;
				SettingsScrollView.EditKey = SB.getName();
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
										SettingDouble value = (SettingDouble) Config.settings.get(SettingsScrollView.EditKey);
										if (value != null) value.setValue(newValue);
										SettingsScrollView.Me.ListInvalidate();
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
						}, SettingsScrollView.Me);
			}
		});

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;

	}

	private View getFolderView(final SettingFolder SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(GlobalCore.Translations.Get(SB.getName()));
		label.setTextSize(UiSizes.getScaledFontSize_big());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(SB.getValue());
		label2.setTextSize((float) UiSizes.getScaledFontSize());
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				SettingsScrollView.EditKey = SB.getName();

				Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);

				// Construct URI from file name.
				File file = new File(SB.getValue());
				intent.setData(Uri.fromFile(file));

				// Set fancy title and button (optional)
				intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Select Folder");
				intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, "Select");
				selectedItem = SB;
				try
				{
					SettingsScrollView.Me.startActivityForResult(intent, Global.REQUEST_CODE_PICK_DIRECTORY);
				}
				catch (ActivityNotFoundException e)
				{
					// No compatible file manager was found.
					Toast.makeText(main.mainActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
				}
			}
		});

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;

	}

	private View getFileView(final SettingFile SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		TextView label = (TextView) row.findViewById(R.id.textView1);
		// label.setText(SB.getName());
		label.setText(GlobalCore.Translations.Get(SB.getName()));
		label.setTextSize(UiSizes.getScaledFontSize_big());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		TextView label2 = (TextView) row.findViewById(R.id.textView2);

		label2.setText(SB.getValue());
		label2.setTextSize((float) UiSizes.getScaledFontSize());
		label2.setTextColor(Global.getColor(R.attr.TextColor));

		row.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				SettingsScrollView.EditKey = SB.getName();

				Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);

				// Construct URI from file name.
				File file = new File(SB.getValue());
				intent.setData(Uri.fromFile(file));

				// Set fancy title and button (optional)
				intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Select file to open");
				intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, "Select");
				selectedItem = SB;
				try
				{
					SettingsScrollView.Me.startActivityForResult(intent, Global.REQUEST_CODE_PICK_FILE);
				}
				catch (ActivityNotFoundException e)
				{
					// No compatible file manager was found.
					Toast.makeText(main.mainActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
				}
			}
		});

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;

	}

	private View getButtonView(final SettingsListCategoryButton SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		final View row = inflater.inflate(R.layout.advanced_settings_list_view_item_category_button, parent, false);

		Button button = (Button) row.findViewById(R.id.Button);
		button.setText(GlobalCore.Translations.Get(SB.getName()));
		button.setTextSize(UiSizes.getScaledFontSize_btn());
		button.setTextColor(Global.getColor(R.attr.TextColor));
		int Height = (UiSizes.getQuickButtonHeight());
		button.setHeight(Height);

		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				// wenn QuickList Button, dann öffne Activity
				if (SB.getName().equals("QuickList"))
				{
					SettingsScrollView.EditKey = SB.getName();

					Intent intent = new Intent().setClass(SettingsScrollView.Me, SettingsListEditQuickButton.class);

					SettingsScrollView.Me.startActivityForResult(intent, Global.REQUEST_CODE_EDIT_QUICK_LIST);

					return;
				}

				if (SB.getName().equals("DebugDisplayInfo"))
				{
					String info = "";

					info += "Density= " + SettingsScrollView.Me.getString(R.string.density) + Global.br + Global.br;
					info += "Height= " + String.valueOf(UiSizes.getWindowHeight()) + Global.br;
					info += "Width= " + String.valueOf(UiSizes.getWindowWidth()) + Global.br;
					info += "Scale= " + String.valueOf(UiSizes.getScale()) + Global.br;
					info += "FontSize= " + String.valueOf(UiSizes.getScaledFontSize()) + Global.br;

					MessageBox.Show(info, SettingsScrollView.Me);

					return;
				}

				if (SB instanceof SettingsListCategoryButton)
				{
					row.performClick();

					return;
				}

			}
		});

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;
	}

	private View getApiKeyButtonView(final SettingsListGetApiButton SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_button, parent, false);

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		Button button = (Button) row.findViewById(R.id.Button);
		button.setText(GlobalCore.Translations.Get("getApiKey"));
		button.setTextSize(UiSizes.getScaledFontSize_btn());
		button.setTextColor(Global.getColor(R.attr.TextColor));

		int Height = (int) (UiSizes.getScaledRefSize_normal() * 4);
		button.setMinimumHeight(Height);

		if (Config.settings.GcAPI.getValue().equals(""))
		{
			button.setCompoundDrawablesWithIntrinsicBounds(null, null, Global.Icons[39], null);
		}
		else
		{
			button.setCompoundDrawablesWithIntrinsicBounds(null, null, Global.Icons[27], null);
		}

		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((main) main.mainActivity).GetApiAuth();
			}
		});

		return row;
	}

	private View getTimeView(final SettingTime SB, ViewGroup parent, boolean BackgroundChanger)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_time, parent, false);

		WheelView wheel_m = (WheelView) row.findViewById(R.id.settings_time_m);
		WheelView wheel_sec = (WheelView) row.findViewById(R.id.settings_time_sec);

		initWheel(wheel_m, 0, 10);
		initWheel(wheel_sec, 0, 59);

		wheel_m.setCurrentItem(SB.getMin());
		wheel_sec.setCurrentItem(SB.getSec());

		LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		if (BackgroundChanger)
		{
			LL.setBackgroundResource(R.drawable.settings_list_background);
		}
		else
		{
			LL.setBackgroundResource(R.drawable.settings_list_background2);
		}

		TextView label = (TextView) row.findViewById(R.id.textView1);
		label.setText(GlobalCore.Translations.Get(SB.getName()));
		label.setTextSize(UiSizes.getScaledFontSize_big());
		label.setTextColor(Global.getColor(R.attr.TextColor));

		wheel_m.addScrollingListener(new OnWheelScrollListener()
		{

			@Override
			public void onScrollingStarted(WheelView wheel)
			{

			}

			@Override
			public void onScrollingFinished(WheelView wheel)
			{
				SB.setMin(wheel.getCurrentItem());
			}
		});

		wheel_sec.addScrollingListener(new OnWheelScrollListener()
		{

			@Override
			public void onScrollingStarted(WheelView wheel)
			{

			}

			@Override
			public void onScrollingFinished(WheelView wheel)
			{
				SB.setSec(wheel.getCurrentItem());
			}
		});

		row.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View arg0)
			{
				// zeige Beschreibung der Einstellung

				MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);

				return false;
			}
		});

		return row;

	}

	/**
	 * Initializes wheel
	 * 
	 * @param id
	 *            the wheel widget Id
	 */
	private void initWheel(WheelView wheel, int min, int max)
	{
		wheel.setViewAdapter(new NumericWheelAdapter(this, min, max));
		wheel.setVisibleItems(3);
		wheel.setCyclic(true);
		wheel.setEnabled(true);
	}

	ArrayList<Langs> Sprachen;

	private View getLangSpinnerView(final SettingsListButtonLangSpinner SB, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_lang_spinner, parent, false);

		final Spinner spinner = (Spinner) row.findViewById(R.id.Spinner);

		int Height = (int) (UiSizes.getScaledRefSize_normal() * 4);
		spinner.setMinimumHeight(Height);

		spinner.setPrompt(GlobalCore.Translations.Get("SelectLanguage"));
		if (spinner.getAdapter() == null)
		{
			Sprachen = GlobalCore.Translations.GetLangs(Config.settings.LanguagePath.getValue());
			String[] items = new String[Sprachen.size()];
			int index = 0;
			int selection = -1;
			for (Langs tmp : Sprachen)
			{
				if (Config.settings.Sel_LanguagePath.getValue().equals(tmp.Path)) selection = index;
				items[index++] = tmp.Name;
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setSelection(selection);

			spinner.setOnItemSelectedListener(new OnItemSelectedListener()
			{

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
				{
					String selected = (String) spinner.getSelectedItem();
					for (Langs tmp : Sprachen)
					{
						if (selected.equals(tmp.Name))
						{
							Config.settings.Sel_LanguagePath.setValue(tmp.Path);
							try
							{
								GlobalCore.Translations.ReadTranslationsFile(tmp.Path);
							}
							catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}

					}
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
						SettingFile value = (SettingFile) Config.settings.get(SettingsScrollView.EditKey);
						if (value != null) value.setValue(filePath);
						SettingsScrollView.Me.ListInvalidate();
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
						SettingFolder value = (SettingFolder) Config.settings.get(SettingsScrollView.EditKey);
						if (value != null) value.setValue(filePath);
						SettingsScrollView.Me.ListInvalidate();
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
						SettingsScrollView.Me.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								SettingsScrollView.Me.resortList();
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

	public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{

		AllContextMenuCallHandler.icm = new IconContextMenu(this, R.menu.menu_settings_view_mode);
		AllContextMenuCallHandler.icm.setOnIconContextItemSelectedListener(OnIconContextItemSelectedListener);
		Menu IconMenu = AllContextMenuCallHandler.icm.getMenu();

		MenuItem miExpert = IconMenu.findItem(R.id.miMap_HideFinds);
		MenuItem miAll = IconMenu.findItem(R.id.miMap_ShowRatings);

		miExpert.setChecked(Config.settings.SettingsShowExpert.getValue());
		miAll.setChecked(Config.settings.SettingsShowAll.getValue());

		AllContextMenuCallHandler.icm.show();

	}

}
