package CB_Core.GL_UI.Activitys.settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CollabseBox.animatetHeightChangedListner;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.LinearCollabseBox;
import CB_Core.GL_UI.Controls.Linearlayout;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.GL_UI.Controls.Spinner.selectionChangedListner;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_Core.GL_UI.Controls.Dialogs.StringInputBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Views.AdvancedSettingsView.SettingsListButtonLangSpinner;
import CB_Core.GL_UI.Views.AdvancedSettingsView.SettingsListButtonSkinSpinner;
import CB_Core.GL_UI.Views.AdvancedSettingsView.SettingsListCategoryButton;
import CB_Core.GL_UI.Views.AdvancedSettingsView.SettingsListGetApiButton;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
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
import CB_Core.Settings.SettingLongString;
import CB_Core.Settings.SettingModus;
import CB_Core.Settings.SettingString;
import CB_Core.Settings.SettingTime;
import CB_Core.TranslationEngine.LangStrings.Langs;

public class SettingsActivity extends ActivityBase
{
	private static SettingsActivity that;
	private ArrayList<SettingCategory> Categorys = new ArrayList<SettingCategory>();
	private Button btnOk, btnCancel, btnMenu;
	private ScrollBox scrollBox;
	private CB_RectF ButtonRec, itemRec;

	/***
	 * Enthält den Key des zu Editierenden Wertes der SettingsList
	 */
	public static String EditKey = "";

	private Linearlayout LinearLayout;

	public SettingsActivity()
	{
		super(ActivityBase.ActivityRec(), "Settings");
		that = this;
		ButtonRec = new CB_RectF(Left, 0, this.width - this.drawableBackground.getLeftWidth() - this.drawableBackground.getRightWidth(),
				UiSizes.getButtonHeight());

		itemRec = new CB_RectF(Left, 0, ButtonRec.getWidth() - Left - Right, UiSizes.getButtonHeight());

		createButtons();
		fillContent();
	}

	private void createButtons()
	{
		float btnW = (innerWidth - UiSizes.getButtonWidth()) / 2;

		btnOk = new Button(Left, Bottom, btnW, UiSizes.getButtonHeight(), "OK Button");
		btnMenu = new Button(btnOk.getMaxX(), Bottom, UiSizes.getButtonWidth(), UiSizes.getButtonHeight(), "Menu Button");
		btnCancel = new Button(btnMenu.getMaxX(), Bottom, btnW, UiSizes.getButtonHeight(), "Cancel Button");

		// Translations
		btnOk.setText(GlobalCore.Translations.Get("save"));
		btnCancel.setText(GlobalCore.Translations.Get("cancel"));

		this.addChild(btnMenu);
		btnMenu.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// TODO Show Menu
				return true;
			}
		});

		this.addChild(btnOk);
		btnOk.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// TODO Save settings
				finish();
				return true;
			}
		});

		this.addChild(btnCancel);
		btnCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				finish();
				return true;
			}
		});

	}

	private void fillContent()
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

		Iterator<SettingCategory> iteratorCat = Categorys.iterator();

		if (iteratorCat != null && iteratorCat.hasNext())
		{

			SettingsListButtonLangSpinner lang = new SettingsListButtonLangSpinner("Lang", SettingCategory.Button, SettingModus.Normal,
					true);
			CB_View_Base langView = getLangSpinnerView(lang);

			addControlToLinearLayout(langView, margin);

			SettingsListCategoryButton quick = new SettingsListCategoryButton("QuickList", SettingCategory.Button, SettingModus.Normal,
					true);
			CB_View_Base quickView = getButtonView(quick, 0);
			addControlToLinearLayout(quickView, margin);

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

				final CB_View_Base btn = getView(catBtn, 1);

				// add Cat einträge
				final LinearCollabseBox lay = new LinearCollabseBox(btn, "");
				lay.setClickable(true);
				lay.setAnimationListner(new animatetHeightChangedListner()
				{

					@Override
					public void animatetHeightCanged(float Height)
					{
						LinearLayout.layout();

						LinearLayout.setZeroPos();
						scrollBox.setInerHeight(LinearLayout.getHeight());

					}
				});

				int entrieCount = 0;
				if (cat == SettingCategory.Login)
				{
					SettingsListGetApiButton lgIn = new SettingsListGetApiButton(cat.name(), SettingCategory.Button, SettingModus.Normal,
							true);
					final CB_View_Base btnLgIn = getView(lgIn, 1);
					lay.addChild(btnLgIn);
					entrieCount++;
				}

				if (cat == SettingCategory.Debug)
				{
					SettingsListCategoryButton disp = new SettingsListCategoryButton("DebugDisplayInfo", SettingCategory.Button,
							SettingModus.Normal, true);
					final CB_View_Base btnDisp = getView(disp, 1);

					btnDisp.setSize(itemRec);

					lay.addChild(btnDisp);
					entrieCount++;
				}

				if (cat == SettingCategory.Skin)
				{
					SettingsListButtonSkinSpinner skin = new SettingsListButtonSkinSpinner("Skin", SettingCategory.Button,
							SettingModus.Normal, true);
					CB_View_Base skinView = getSkinSpinnerView(skin);
					lay.addChild(skinView);
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

									CB_View_Base view = getView(settingItem, position++);

									lay.addChild(view);
									entrieCount++;
									if (settingItem.getName().equals(EditKey))
									{
										expandLayout = true;
									}
								}
							}
							else
							{

								CB_View_Base view = getView(settingItem, position++);

								if (view instanceof Button)
								{
									view.setSize(itemRec);
								}

								lay.addChild(view);
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

					lay.setBackground(this.getBackground());// Activity Background
					if (!expandLayout) lay.setAnimationHeight(0f);

					addControlToLinearLayout(btn, margin);
					addControlToLinearLayout(lay, -(this.drawableBackground.getBottomHeight()) / 2);

					btn.setOnClickListener(new OnClickListener()
					{
						@Override
						public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
						{
							lay.Toggle();
							return true;
						}
					});
				}

			}
			while (iteratorCat.hasNext());

		}
	}

	private void addControlToLinearLayout(CB_View_Base view, float itemMargin)
	{
		if (LinearLayout == null || scrollBox == null)
		{

			CB_RectF rec = new CB_RectF(0, btnOk.getMaxY() + margin, this.width, this.height - btnOk.getMaxY() - margin);

			scrollBox = new ScrollBox(rec, rec.getHalfHeight(), "SettingsActivity-scrollBox");
			scrollBox.setClickable(true);
			LinearLayout = new Linearlayout(ButtonRec.getWidth(), "SettingsActivity-LinearLayout");
			LinearLayout.setClickable(true);
			LinearLayout.setZeroPos();
			scrollBox.addChild(LinearLayout);
			// LinearLayout.setBackground(new ColorDrawable(Color.RED));
			scrollBox.setBackground(this.getBackground());
			this.addChild(scrollBox);
		}

		view.setZeroPos();

		LinearLayout.addChild(view, itemMargin);
		LinearLayout.setZeroPos();
		scrollBox.setInerHeight(LinearLayout.getHeight());

	}

	private CB_View_Base getView(SettingBase SB, int BackgroundChanger)
	{
		if (SB instanceof SettingBool)
		{
			return getBoolView((SettingBool) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingIntArray)
		{
			return getIntArrayView((SettingIntArray) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingTime)
		{
			return getTimeView((SettingTime) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingInt)
		{
			return getIntView((SettingInt) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingDouble)
		{
			return getDblView((SettingDouble) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingFolder)
		{
			return getFolderView((SettingFolder) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingFile)
		{
			return getFileView((SettingFile) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingEnum)
		{
			return getEnumView((SettingEnum) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingString)
		{
			return getStringView((SettingString) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingsListCategoryButton)
		{
			return getButtonView((SettingsListCategoryButton) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingsListGetApiButton)
		{
			return getApiKeyButtonView((SettingsListGetApiButton) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingsListButtonLangSpinner)
		{
			return getLangSpinnerView((SettingsListButtonLangSpinner) SB);
		}
		else if (SB instanceof SettingsListButtonSkinSpinner)
		{
			return getSkinSpinnerView((SettingsListButtonSkinSpinner) SB);
		}

		return null;
	}

	private CB_View_Base getStringView(final SettingString SB, int backgroundChanger)
	{
		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());

		item.setName(GlobalCore.Translations.Get(SB.getName()));
		item.setDefault(SB.getValue());

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = SB.getName();

				TextFieldType type;

				type = (SB instanceof SettingLongString) ? TextFieldType.MultiLineWraped : TextFieldType.SingleLine;

				StringInputBox.Show(type, "default: " + SB.getDefaultValue(), SB.getName(), SB.getValue(), new OnMsgBoxClickListener()
				{

					@Override
					public boolean onClick(int which)
					{
						String text = StringInputBox.editText.getText().toString();
						if (which == GL_MsgBox.BUTTON_POSITIVE)
						{
							SettingString value = (SettingString) Config.settings.get(EditKey);
							if (value != null) value.setValue(text);
							// SettingsScrollView.Me.ListInvalidate();

						}
						// Activity wieder anzeigen
						that.show();
						return true;
					}
				});

				return true;
			}

		});

		item.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()));

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getEnumView(final SettingEnum SB, int backgroundChanger)
	{
		// LayoutInflater inflater = getLayoutInflater();
		// View row = inflater.inflate(R.layout.advanced_settings_list_view_item_enum, parent, false);
		//
		// LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		// if (BackgroundChanger)
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background);
		// }
		// else
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background2);
		// }
		//
		// TextView label = (TextView) row.findViewById(R.id.textView1);
		// label.setText(GlobalCore.Translations.Get(SB.getName()));
		// label.setTextSize(UiSizes.getScaledFontSize_big());
		// label.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// final Spinner spinner = (Spinner) row.findViewById(R.id.spinner1);
		//
		// if (spinner.getAdapter() == null)
		// {
		// ArrayAdapter<String> enumAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SB.getValues());
		// enumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// spinner.setAdapter(enumAdapter);
		// spinner.setSelection(SB.getValues().indexOf(SB.getValue()));
		//
		// spinner.setOnItemSelectedListener(new OnItemSelectedListener()
		// {
		//
		// @Override
		// public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		// {
		// if (SB != null)
		// {
		// selectedItem = SB;
		// SB.setValue((String) SB.getValues().get(arg2));
		// }
		// }
		//
		// @Override
		// public void onNothingSelected(AdapterView<?> arg0)
		// {
		// if (SB != null)
		// {
		// selectedItem = SB;
		//
		// }
		// }
		// });
		// }
		//
		// row.setOnLongClickListener(new OnLongClickListener()
		// {
		//
		// @Override
		// public boolean onLongClick(View arg0)
		// {
		// // zeige Beschreibung der Einstellung
		//
		// MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);
		//
		// return false;
		// }
		// });

		return null;

	}

	private CB_View_Base getIntArrayView(final SettingIntArray SB, int backgroundChanger)
	{
		// LayoutInflater inflater = getLayoutInflater();
		// View row = inflater.inflate(R.layout.advanced_settings_list_view_item_enum, parent, false);
		//
		// LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		// if (BackgroundChanger)
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background);
		// }
		// else
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background2);
		// }
		//
		// TextView label = (TextView) row.findViewById(R.id.textView1);
		// label.setText(GlobalCore.Translations.Get(SB.getName()));
		// label.setTextSize(UiSizes.getScaledFontSize_big());
		// label.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// final Spinner spinner = (Spinner) row.findViewById(R.id.spinner1);
		//
		// if (spinner.getAdapter() == null)
		// {
		// ArrayAdapter<Integer> enumAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, SB.getValues());
		// enumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// spinner.setAdapter(enumAdapter);
		// spinner.setSelection(SB.getIndex());
		//
		// spinner.setOnItemSelectedListener(new OnItemSelectedListener()
		// {
		//
		// @Override
		// public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		// {
		// if (SB != null)
		// {
		// selectedItem = SB;
		// SB.setValue(SB.getValueFromIndex(arg2));
		// }
		// }
		//
		// @Override
		// public void onNothingSelected(AdapterView<?> arg0)
		// {
		// if (SB != null)
		// {
		// selectedItem = SB;
		//
		// }
		// }
		// });
		// }
		//
		// row.setOnLongClickListener(new OnLongClickListener()
		// {
		//
		// @Override
		// public boolean onLongClick(View arg0)
		// {
		// // zeige Beschreibung der Einstellung
		//
		// MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);
		//
		// return false;
		// }
		// });

		return null;

	}

	private CB_View_Base getIntView(final SettingInt SB, int backgroundChanger)
	{
		// LayoutInflater inflater = getLayoutInflater();
		// View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);
		//
		// LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		// if (BackgroundChanger)
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background);
		// }
		// else
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background2);
		// }
		//
		// TextView label = (TextView) row.findViewById(R.id.textView1);
		// label.setText(GlobalCore.Translations.Get(SB.getName()));
		// label.setTextSize(UiSizes.getScaledFontSize_big());
		// label.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// TextView label2 = (TextView) row.findViewById(R.id.textView2);
		//
		// label2.setText(String.valueOf(SB.getValue()));
		// label2.setTextSize((float) UiSizes.getScaledFontSize());
		// label2.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// row.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View arg0)
		// {
		// selectedItem = SB;
		// SettingsScrollView.EditKey = SB.getName();
		// // Show NumPad Int Edit
		// NumerikInputBox.Show(SB.getName(), "default: " + String.valueOf(SB.getDefaultValue()), SB.getValue(),
		// new returnValueListner()
		// {
		// @Override
		// public void returnValue(int value)
		// {
		// SettingInt SetValue = (SettingInt) Config.settings.get(SettingsScrollView.EditKey);
		// if (SetValue != null) SetValue.setValue(value);
		// SettingsScrollView.Me.ListInvalidate();
		// // Activity wieder anzeigen
		// main.mainActivity.startActivity(aktIntent);
		// }
		//
		// @Override
		// public void cancelClicked()
		// {
		// // Activity wieder anzeigen
		// main.mainActivity.startActivity(aktIntent);
		// }
		//
		// });
		// // Activity ausblenden, damit OpenGL-InputBox sichtbar wird
		// finish();
		// }
		// });
		//
		// row.setOnLongClickListener(new OnLongClickListener()
		// {
		//
		// @Override
		// public boolean onLongClick(View arg0)
		// {
		// // zeige Beschreibung der Einstellung
		//
		// MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);
		//
		// return false;
		// }
		// });

		return null;

	}

	private CB_View_Base getDblView(final SettingDouble SB, int backgroundChanger)
	{
		// LayoutInflater inflater = getLayoutInflater();
		// View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);
		//
		// LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		// if (BackgroundChanger)
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background);
		// }
		// else
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background2);
		// }
		//
		// TextView label = (TextView) row.findViewById(R.id.textView1);
		// label.setText(GlobalCore.Translations.Get(SB.getName()));
		// label.setTextSize(UiSizes.getScaledFontSize_big());
		// label.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// TextView label2 = (TextView) row.findViewById(R.id.textView2);
		//
		// label2.setText(String.valueOf(SB.getValue()));
		// label2.setTextSize((float) UiSizes.getScaledFontSize());
		// label2.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// row.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View arg0)
		// {
		// selectedItem = SB;
		// SettingsScrollView.EditKey = SB.getName();
		// // Show NumPad Int Edit
		// NumerikInputBox input = NumerikInputBox.Show(SB.getName(), "default: " + String.valueOf(SB.getDefaultValue()),
		// SB.getValue(), new returnValueListnerDouble()
		// {
		// @Override
		// public void returnValue(double value)
		// {
		// SettingDouble setValue = (SettingDouble) Config.settings.get(SettingsScrollView.EditKey);
		// if (setValue != null) setValue.setValue(value);
		// SettingsScrollView.Me.ListInvalidate();
		// // Activity wieder anzeigen
		// main.mainActivity.startActivity(aktIntent);
		// }
		//
		// @Override
		// public void cancelClicked()
		// {
		// // Activity wieder anzeigen
		// main.mainActivity.startActivity(aktIntent);
		// }
		//
		// });
		//
		// // Activity ausblenden, damit OpenGL-InputBox sichtbar wird
		// finish();
		// }
		// });
		//
		// row.setOnLongClickListener(new OnLongClickListener()
		// {
		//
		// @Override
		// public boolean onLongClick(View arg0)
		// {
		// // zeige Beschreibung der Einstellung
		//
		// MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);
		//
		// return false;
		// }
		// });

		return null;

	}

	private CB_View_Base getFolderView(final SettingFolder SB, int backgroundChanger)
	{
		// LayoutInflater inflater = getLayoutInflater();
		// View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);
		//
		// LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		// if (BackgroundChanger)
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background);
		// }
		// else
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background2);
		// }
		//
		// TextView label = (TextView) row.findViewById(R.id.textView1);
		// label.setText(GlobalCore.Translations.Get(SB.getName()));
		// label.setTextSize(UiSizes.getScaledFontSize_big());
		// label.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// TextView label2 = (TextView) row.findViewById(R.id.textView2);
		//
		// label2.setText(SB.getValue());
		// label2.setTextSize((float) UiSizes.getScaledFontSize());
		// label2.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// row.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View arg0)
		// {
		// SettingsScrollView.EditKey = SB.getName();
		//
		// Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
		//
		// // Construct URI from file name.
		// File file = new File(SB.getValue());
		// intent.setData(Uri.fromFile(file));
		//
		// // Set fancy title and button (optional)
		// intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Select Folder");
		// intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, "Select");
		// selectedItem = SB;
		// try
		// {
		// SettingsScrollView.Me.startActivityForResult(intent, Global.REQUEST_CODE_PICK_DIRECTORY);
		// }
		// catch (ActivityNotFoundException e)
		// {
		// // No compatible file manager was found.
		// Toast.makeText(main.mainActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
		// }
		// }
		// });
		//
		// row.setOnLongClickListener(new OnLongClickListener()
		// {
		//
		// @Override
		// public boolean onLongClick(View arg0)
		// {
		// // zeige Beschreibung der Einstellung
		//
		// MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);
		//
		// return false;
		// }
		// });

		return null;

	}

	private CB_View_Base getFileView(final SettingFile SB, int backgroundChanger)
	{
		// LayoutInflater inflater = getLayoutInflater();
		// View row = inflater.inflate(R.layout.advanced_settings_list_view_item, parent, false);
		//
		// LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		// if (BackgroundChanger)
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background);
		// }
		// else
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background2);
		// }
		//
		// TextView label = (TextView) row.findViewById(R.id.textView1);
		// // label.setText(SB.getName());
		// label.setText(GlobalCore.Translations.Get(SB.getName()));
		// label.setTextSize(UiSizes.getScaledFontSize_big());
		// label.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// TextView label2 = (TextView) row.findViewById(R.id.textView2);
		//
		// label2.setText(SB.getValue());
		// label2.setTextSize((float) UiSizes.getScaledFontSize());
		// label2.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// row.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View arg0)
		// {
		// SettingsScrollView.EditKey = SB.getName();
		//
		// Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		//
		// // Construct URI from file name.
		// File file = new File(SB.getValue());
		// intent.setData(Uri.fromFile(file));
		//
		// // Set fancy title and button (optional)
		// intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Select file to open");
		// intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, "Select");
		// selectedItem = SB;
		// try
		// {
		// SettingsScrollView.Me.startActivityForResult(intent, Global.REQUEST_CODE_PICK_FILE);
		// }
		// catch (ActivityNotFoundException e)
		// {
		// // No compatible file manager was found.
		// Toast.makeText(main.mainActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
		// }
		// }
		// });
		//
		// row.setOnLongClickListener(new OnLongClickListener()
		// {
		//
		// @Override
		// public boolean onLongClick(View arg0)
		// {
		// // zeige Beschreibung der Einstellung
		//
		// MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);
		//
		// return false;
		// }
		// });

		return null;

	}

	private CB_View_Base getButtonView(final SettingsListCategoryButton SB, int backgroundChanger)
	{
		Button btn = new Button(ButtonRec, "Button");

		btn.setDrageble();

		btn.setText(GlobalCore.Translations.Get(SB.getName()));

		if (SB.getName().equals("QuickList") || SB.getName().equals("DebugDisplayInfo"))
		{
			btn.setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{

					// wenn QuickList Button, dann öffne Activity
					if (SB.getName().equals("QuickList"))
					{
						// SettingsScrollView.EditKey = SB.getName();
						//
						// Intent intent = new Intent().setClass(SettingsScrollView.Me, SettingsListEditQuickButton.class);
						//
						// SettingsScrollView.Me.startActivityForResult(intent, Global.REQUEST_CODE_EDIT_QUICK_LIST);
						//
						// return;
					}

					if (SB.getName().equals("DebugDisplayInfo"))
					{
						String info = "";

						info += "Density= " + String.valueOf(GL_UISizes.DPI) + GlobalCore.br;
						info += "Height= " + String.valueOf(UiSizes.getWindowHeight()) + GlobalCore.br;
						info += "Width= " + String.valueOf(UiSizes.getWindowWidth()) + GlobalCore.br;
						info += "Scale= " + String.valueOf(UiSizes.getScale()) + GlobalCore.br;
						info += "FontSize= " + String.valueOf(UiSizes.getScaledFontSize()) + GlobalCore.br;

						GL_MsgBox.Show(info);
						return true;
					}

					return false;
				}

			});
		}

		btn.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()));

				return false;
			}

		});

		return btn;
	}

	private CB_View_Base getApiKeyButtonView(final SettingsListGetApiButton SB, int backgroundChanger)
	{
		// LayoutInflater inflater = getLayoutInflater();
		// View row = inflater.inflate(R.layout.advanced_settings_list_view_item_button, parent, false);
		//
		// LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		// if (BackgroundChanger)
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background);
		// }
		// else
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background2);
		// }
		//
		// Button button = (Button) row.findViewById(R.id.Button);
		// button.setText(GlobalCore.Translations.Get("getApiKey"));
		// button.setTextSize(UiSizes.getScaledFontSize_btn());
		// button.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// int Height = (int) (UiSizes.getScaledRefSize_normal() * 4);
		// button.setMinimumHeight(Height);
		//
		// if (Config.settings.GcAPI.getValue().equals(""))
		// {
		// button.setCompoundDrawablesWithIntrinsicBounds(null, null, Global.Icons[39], null);
		// }
		// else
		// {
		// button.setCompoundDrawablesWithIntrinsicBounds(null, null, Global.Icons[27], null);
		// }
		//
		// button.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// ((main) main.mainActivity).GetApiAuth();
		// }
		// });

		return null;
	}

	private CB_View_Base getTimeView(final SettingTime SB, int backgroundChanger)
	{
		// LayoutInflater inflater = getLayoutInflater();
		// View row = inflater.inflate(R.layout.advanced_settings_list_view_time, parent, false);
		//
		// WheelView wheel_m = (WheelView) row.findViewById(R.id.settings_time_m);
		// WheelView wheel_sec = (WheelView) row.findViewById(R.id.settings_time_sec);
		//
		// initWheel(wheel_m, 0, 10);
		// initWheel(wheel_sec, 0, 59);
		//
		// wheel_m.setCurrentItem(SB.getMin());
		// wheel_sec.setCurrentItem(SB.getSec());
		//
		// LinearLayout LL = (LinearLayout) row.findViewById(R.id.backLayout);
		// if (BackgroundChanger)
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background);
		// }
		// else
		// {
		// LL.setBackgroundResource(R.drawable.settings_list_background2);
		// }
		//
		// TextView label = (TextView) row.findViewById(R.id.textView1);
		// label.setText(GlobalCore.Translations.Get(SB.getName()));
		// label.setTextSize(UiSizes.getScaledFontSize_big());
		// label.setTextColor(Global.getColor(R.attr.TextColor));
		//
		// wheel_m.addScrollingListener(new OnWheelScrollListener()
		// {
		//
		// @Override
		// public void onScrollingStarted(WheelView wheel)
		// {
		//
		// }
		//
		// @Override
		// public void onScrollingFinished(WheelView wheel)
		// {
		// SB.setMin(wheel.getCurrentItem());
		// }
		// });
		//
		// wheel_sec.addScrollingListener(new OnWheelScrollListener()
		// {
		//
		// @Override
		// public void onScrollingStarted(WheelView wheel)
		// {
		//
		// }
		//
		// @Override
		// public void onScrollingFinished(WheelView wheel)
		// {
		// SB.setSec(wheel.getCurrentItem());
		// }
		// });
		//
		// row.setOnLongClickListener(new OnLongClickListener()
		// {
		//
		// @Override
		// public boolean onLongClick(View arg0)
		// {
		// // zeige Beschreibung der Einstellung
		//
		// MessageBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), SettingsScrollView.Me);
		//
		// return false;
		// }
		// });

		return null;

	}

	ArrayList<Langs> Sprachen;

	private CB_View_Base getLangSpinnerView(final SettingsListButtonLangSpinner SB)
	{
		Sprachen = GlobalCore.Translations.GetLangs(Config.settings.LanguagePath.getValue());
		final String[] items = new String[Sprachen.size()];
		int index = 0;
		int selection = -1;
		for (Langs tmp : Sprachen)
		{
			if (Config.settings.Sel_LanguagePath.getValue().equals(tmp.Path)) selection = index;
			items[index++] = tmp.Name;
		}

		final Spinner spinner = new Spinner(ButtonRec, "LangSpinner", items, new selectionChangedListner()
		{

			@Override
			public void selectionChanged(int index)
			{
				String selected = items[index];
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
							e.printStackTrace();
						}
						break;
					}

				}
			}
		});

		// TODO Prompt in Spinner einbauen
		// spinner.setPrompt(GlobalCore.Translations.Get("SelectLanguage"));

		spinner.setDrageble();

		return spinner;
	}

	private CB_View_Base getSkinSpinnerView(final SettingsListButtonSkinSpinner SB)
	{

		// View row = inflater.inflate(R.layout.advanced_settings_list_view_item_lang_spinner, parent, false);
		//
		// final Spinner spinner = (Spinner) row.findViewById(R.id.Spinner);
		//
		// int Height = (int) (UiSizes.getScaledRefSize_normal() * 4);
		// spinner.setMinimumHeight(Height);
		//
		// spinner.setPrompt(GlobalCore.Translations.Get("SelectSkin"));
		// if (spinner.getAdapter() == null)
		// {
		//
		// String SkinFolder = Config.WorkPath + "/skins";
		// File dir = new File(SkinFolder);
		//
		// final ArrayList<String> skinFolders = new ArrayList<String>();
		// dir.listFiles(new FileFilter()
		// {
		//
		// public boolean accept(File f)
		// {
		// if (f.isDirectory())
		// {
		// Object Path = f.getAbsolutePath();
		// skinFolders.add((String) Path);
		// }
		//
		// return false;
		// }
		// });
		//
		// String[] items = new String[skinFolders.size()];
		// int index = 0;
		// int selection = -1;
		// for (String tmp : skinFolders)
		// {
		// if (Config.settings.SkinFolder.getValue().equals(tmp)) selection = index;
		//
		// // cut folder name
		// int Pos = tmp.lastIndexOf("/");
		// tmp = tmp.substring(Pos + 1);
		//
		// items[index++] = tmp;
		// }
		// ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// spinner.setAdapter(adapter);
		// spinner.setSelection(selection);
		//
		// spinner.setOnItemSelectedListener(new OnItemSelectedListener()
		// {
		//
		// @Override
		// public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		// {
		// String selected = (String) skinFolders.get(arg2);
		// for (String tmp : skinFolders)
		// {
		// if (selected.equals(tmp))
		// {
		// Config.settings.SkinFolder.setValue(tmp);
		//
		// break;
		// }
		//
		// }
		// }
		//
		// @Override
		// public void onNothingSelected(AdapterView<?> arg0)
		// {
		// // do nothing
		// }
		// });
		// }
		return null;
	}

	private CB_View_Base getBoolView(final SettingBool SB, int backgroundChanger)
	{

		SettingsItem_Bool item = new SettingsItem_Bool(itemRec, backgroundChanger, SB.getName());

		item.setName(GlobalCore.Translations.Get(SB.getName()));
		item.setDefault("default: " + String.valueOf(SB.getDefaultValue()));

		chkBox chk = item.getCheckBox();

		chk.setChecked(SB.getValue());
		chk.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(chkBox view, boolean isChecked)
			{
				SB.setValue(isChecked);
			}
		});

		item.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()));

				return true;
			}

		});

		return item;

	}

}
