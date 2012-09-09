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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.GL_UI.Views.AdvancedSettingsView.SettingsListButtonSkinSpinner;
import CB_Core.GL_UI.Views.AdvancedSettingsView.SettingsListCategoryButton;
import CB_Core.GL_UI.Views.AdvancedSettingsView.SettingsListGetApiButton;
import CB_Core.Map.ManagerBase;
import CB_Core.Math.UiSizes;
import CB_Core.Settings.SettingBase;
import CB_Core.Settings.SettingCategory;
import CB_Core.Settings.SettingFile;
import CB_Core.Settings.SettingFolder;
import CB_Core.Settings.SettingModus;
import CB_Core.Settings.SettingTime;
import CB_Core.Solver.Functions.Function;
import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Components.Animations;
import de.cachebox_test.Custom_Controls.downSlider;
import de.cachebox_test.Custom_Controls.wheel.OnWheelScrollListener;
import de.cachebox_test.Custom_Controls.wheel.WheelView;
import de.cachebox_test.Custom_Controls.wheel.adapters.NumericWheelAdapter;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Views.Forms.MessageBox;

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
		try
		{
			ActivityUtils.onActivityCreateSetTheme(this);
		}
		catch (Exception e)
		{

			e.printStackTrace();
		}
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
				ManagerBase.RenderThemeChanged = true;
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

				if (cat == SettingCategory.Skin)
				{
					SettingsListButtonSkinSpinner skin = new SettingsListButtonSkinSpinner("Skin", SettingCategory.Button,
							SettingModus.Normal, true);
					View skinView = getSkinSpinnerView(skin, content);

					lay.addView(skinView);
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
									if (view == null) continue;
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

								if (view == null) continue;

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

	private View getView(SettingBase SB, ViewGroup parent, boolean BackgroundChanger)
	{
		if (SB instanceof SettingTime)
		{
			return getTimeView((SettingTime) SB, parent, BackgroundChanger);
		}

		else if (SB instanceof SettingsListCategoryButton)
		{
			return getButtonView((SettingsListCategoryButton) SB, parent, BackgroundChanger);
		}
		else if (SB instanceof SettingsListGetApiButton)
		{
			return getApiKeyButtonView((SettingsListGetApiButton) SB, parent, BackgroundChanger);
		}

		else if (SB instanceof SettingsListButtonSkinSpinner)
		{
			return getSkinSpinnerView((SettingsListButtonSkinSpinner) SB, parent);
		}

		return null;
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

				if (SB.getName().equals("DebugDisplayInfo"))
				{
					String info = "";

					info += "Density= " + SettingsScrollView.Me.getString(R.string.density) + GlobalCore.br + GlobalCore.br;
					info += "Height= " + String.valueOf(UiSizes.getWindowHeight()) + GlobalCore.br;
					info += "Width= " + String.valueOf(UiSizes.getWindowWidth()) + GlobalCore.br;
					info += "Scale= " + String.valueOf(UiSizes.getScale()) + GlobalCore.br;
					info += "FontSize= " + String.valueOf(UiSizes.getScaledFontSize()) + GlobalCore.br;

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

	private View getSkinSpinnerView(final SettingsListButtonSkinSpinner SB, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater();
		View row = inflater.inflate(R.layout.advanced_settings_list_view_item_lang_spinner, parent, false);

		final Spinner spinner = (Spinner) row.findViewById(R.id.Spinner);

		int Height = (int) (UiSizes.getScaledRefSize_normal() * 4);
		spinner.setMinimumHeight(Height);

		spinner.setPrompt(GlobalCore.Translations.Get("SelectSkin"));
		if (spinner.getAdapter() == null)
		{

			String SkinFolder = Config.WorkPath + "/skins";
			File dir = new File(SkinFolder);

			final ArrayList<String> skinFolders = new ArrayList<String>();
			dir.listFiles(new FileFilter()
			{

				public boolean accept(File f)
				{
					if (f.isDirectory())
					{
						Object Path = f.getAbsolutePath();
						skinFolders.add((String) Path);
					}

					return false;
				}
			});

			String[] items = new String[skinFolders.size()];
			int index = 0;
			int selection = -1;
			for (String tmp : skinFolders)
			{
				if (Config.settings.SkinFolder.getValue().equals(tmp)) selection = index;

				// cut folder name
				int Pos = tmp.lastIndexOf("/");
				tmp = tmp.substring(Pos + 1);

				items[index++] = tmp;
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
					String selected = (String) skinFolders.get(arg2);
					for (String tmp : skinFolders)
					{
						if (selected.equals(tmp))
						{
							Config.settings.SkinFolder.setValue(tmp);

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

}
