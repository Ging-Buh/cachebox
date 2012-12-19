package CB_Core.GL_UI.Activitys.settings;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.Events.platformConector;
import CB_Core.Events.platformConector.IgetFileReturnListner;
import CB_Core.Events.platformConector.IgetFolderReturnListner;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Controls.API_Button;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CollabseBox.animatetHeightChangedListner;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.LinearCollabseBox;
import CB_Core.GL_UI.Controls.Linearlayout;
import CB_Core.GL_UI.Controls.QuickButtonList;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.GL_UI.Controls.Spinner.selectionChangedListner;
import CB_Core.GL_UI.Controls.SpinnerAdapter;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListner;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListnerDouble;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListnerTime;
import CB_Core.GL_UI.Controls.Dialogs.StringInputBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.MapView;
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

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SettingsActivity extends ActivityBase
{

	public static SettingsActivity that;
	private ArrayList<SettingCategory> Categorys = new ArrayList<SettingCategory>();
	private Button btnOk, btnCancel, btnMenu;
	private ScrollBox scrollBox;
	private CB_RectF ButtonRec, itemRec;
	private API_Button apiBtn;

	/***
	 * Enthält den Key des zu Editierenden Wertes der SettingsList
	 */
	public static String EditKey = "";

	private Linearlayout LinearLayout;

	public SettingsActivity()
	{
		super(ActivityBase.ActivityRec(), "Settings");
		that = this;
		Config.settings.SaveToLastValue();
		ButtonRec = new CB_RectF(this.getLeftWidth(), 0, this.width - this.drawableBackground.getLeftWidth()
				- this.drawableBackground.getRightWidth(), UiSizes.getButtonHeight());

		itemRec = new CB_RectF(this.getLeftWidth(), 0, ButtonRec.getWidth() - this.getLeftWidth() - this.getRightWidth(),
				UiSizes.getButtonHeight());

		createButtons();
		fillContent();
	}

	private void createButtons()
	{
		float btnW = (innerWidth - UiSizes.getButtonWidth()) / 2;

		btnOk = new Button(this.getLeftWidth(), this.getBottomHeight(), btnW, UiSizes.getButtonHeight(), "OK Button");
		btnMenu = new Button(btnOk.getMaxX(), this.getBottomHeight(), UiSizes.getButtonWidth(), UiSizes.getButtonHeight(), "Menu Button");
		btnCancel = new Button(btnMenu.getMaxX(), this.getBottomHeight(), btnW, UiSizes.getButtonHeight(), "Cancel Button");

		// Translations
		btnOk.setText(GlobalCore.Translations.Get("save"));
		btnCancel.setText(GlobalCore.Translations.Get("cancel"));

		this.addChild(btnMenu);
		btnMenu.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				Menu icm = new Menu("menu_mapviewgl");
				icm.addItemClickListner(new OnClickListener()
				{

					@Override
					public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
					{
						switch (((MenuItem) v).getMenuItemId())
						{
						case MenuID.MI_SHOW_EXPERT:
							Config.settings.SettingsShowExpert.setValue(!Config.settings.SettingsShowExpert.getValue());
							resortList();
							return true;

						case MenuID.MI_SHOW_ALL:
							Config.settings.SettingsShowAll.setValue(!Config.settings.SettingsShowAll.getValue());
							resortList();
							return true;
						}

						return false;
					}
				});
				MenuItem mi;

				mi = icm.addItem(MenuID.MI_SHOW_EXPERT, "Settings_Expert");
				mi.setCheckable(true);
				mi.setChecked(Config.settings.SettingsShowExpert.getValue());

				mi = icm.addItem(MenuID.MI_SHOW_ALL, "Settings_All");

				mi.setCheckable(true);
				mi.setChecked(Config.settings.SettingsShowAll.getValue());

				icm.setPrompt(GlobalCore.Translations.Get("changeSettingsVisibility"));

				icm.show();
				return true;
			}
		});

		this.addChild(btnOk);
		btnOk.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				String ActionsString = "";
				int counter = 0;
				for (QuickButtonItem tmp : SettingsItem_QuickButton.tmpQuickList)
				{
					ActionsString += String.valueOf(tmp.getAction().ordinal());
					if (counter < SettingsItem_QuickButton.tmpQuickList.size() - 1)
					{
						ActionsString += ",";
					}
					counter++;
				}
				Config.settings.quickButtonList.setValue(ActionsString);

				Config.settings.SaveToLastValue();
				Config.AcceptChanges();

				// Notify QuickButtonList
				QuickButtonList.that.notifyDataSetChanged();

				if (MapView.that != null) MapView.that.setNewSettings(MapView.INITIAL_NEW_SETTINGS);

				int Time = Config.settings.ScreenLock.getValue();
				platformConector.callsetScreenLockTimet(Time);

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
				Config.settings.LoadFromLastValue();

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

			// SettingsListCategoryButton quick = new SettingsListCategoryButton("QuickList", SettingCategory.Button, SettingModus.Normal,
			// true);
			// CB_View_Base quickView = getButtonView(quick, 0);
			// addControlToLinearLayout(quickView, margin);

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

				if (cat == SettingCategory.QuickList)
				{

					final SettingsItem_QuickButton btnLgIn = new SettingsItem_QuickButton(itemRec, "QuickButtonEditor");
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

		apiBtn.setImage();

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
			return getEnumView((SettingEnum<?>) SB, BackgroundChanger);
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
		final String trans = GlobalCore.Translations.Get(SB.getName());
		item.setName(trans);
		item.setDefault(SB.getValue());

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = SB.getName();

				TextFieldType type;

				type = (SB instanceof SettingLongString) ? TextFieldType.MultiLineWraped : TextFieldType.SingleLine;

				StringInputBox.Show(type, "default:" + GlobalCore.br + SB.getDefaultValue(), trans, SB.getValue(),
						new OnMsgBoxClickListener()
						{

							@Override
							public boolean onClick(int which)
							{
								String text = StringInputBox.editText.getText().toString();
								if (which == GL_MsgBox.BUTTON_POSITIVE)
								{
									SettingString value = (SettingString) Config.settings.get(EditKey);

									// api ohne lineBreak
									if (EditKey.equalsIgnoreCase("GcAPI"))
									{
										text = text.replace("\r", "");
										text = text.replace("\n", "");
									}

									if (value != null) value.setValue(text);

									resortList();
								}
								// Activity wieder anzeigen
								that.show();
								return true;
							}
						});

				return true;
			}

		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getEnumView(final SettingEnum<?> SB, int backgroundChanger)
	{

		SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

		item.setName(GlobalCore.Translations.Get(SB.getName()));

		final Spinner spinner = item.getSpinner();

		spinner.setDrageble();

		final SpinnerAdapter adapter = new SpinnerAdapter()
		{

			@Override
			public String getText(int position)
			{
				return String.valueOf(SB.getValues().get(position));
			}

			@Override
			public Drawable getIcon(int Position)
			{
				return null;
			}

			@Override
			public int getCount()
			{
				return SB.getValues().size();
			}
		};

		spinner.setAdapter(adapter);
		spinner.setSelection(SB.getValues().indexOf(SB.getValue()));

		spinner.setSelectionChangedListner(new selectionChangedListner()
		{
			@Override
			public void selectionChanged(int index)
			{
				SB.setValue((String) SB.getValues().get(index));
			}
		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;
	}

	private CB_View_Base getIntArrayView(final SettingIntArray SB, int backgroundChanger)
	{

		SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

		item.setName(GlobalCore.Translations.Get(SB.getName()));

		final Spinner spinner = item.getSpinner();

		spinner.setDrageble();

		final SpinnerAdapter adapter = new SpinnerAdapter()
		{

			@Override
			public String getText(int position)
			{
				return String.valueOf(SB.getValues()[position]);
			}

			@Override
			public Drawable getIcon(int Position)
			{
				return null;
			}

			@Override
			public int getCount()
			{
				return SB.getValues().length;
			}
		};

		spinner.setAdapter(adapter);
		spinner.setSelection(SB.getIndex());

		spinner.setSelectionChangedListner(new selectionChangedListner()
		{
			@Override
			public void selectionChanged(int index)
			{
				SB.setValue(SB.getValueFromIndex(index));
			}
		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getIntView(final SettingInt SB, int backgroundChanger)
	{

		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
		final String trans = GlobalCore.Translations.Get(SB.getName());

		item.setName(trans);
		item.setDefault(String.valueOf(SB.getValue()));

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = SB.getName();

				// Show NumPad Int Edit
				NumerikInputBox.Show("default: " + GlobalCore.br + String.valueOf(SB.getDefaultValue()), trans, SB.getValue(),
						new returnValueListner()
						{
							@Override
							public void returnValue(int value)
							{
								SettingInt SetValue = (SettingInt) Config.settings.get(EditKey);
								if (SetValue != null) SetValue.setValue(value);
								resortList();
								// Activity wieder anzeigen
								show();
							}

							@Override
							public void cancelClicked()
							{
								// Activity wieder anzeigen
								show();
							}

						});
				return true;
			}

		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;
	}

	private CB_View_Base getDblView(final SettingDouble SB, int backgroundChanger)
	{
		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
		final String trans = GlobalCore.Translations.Get(SB.getName());
		item.setName(trans);
		item.setDefault(String.valueOf(SB.getValue()));

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = SB.getName();

				// Show NumPad Int Edit
				NumerikInputBox.Show("default: " + GlobalCore.br + String.valueOf(SB.getDefaultValue()), trans, SB.getValue(),
						new returnValueListnerDouble()
						{
							@Override
							public void returnValue(double value)
							{
								SettingDouble SetValue = (SettingDouble) Config.settings.get(EditKey);
								if (SetValue != null) SetValue.setValue(value);
								resortList();
								// Activity wieder anzeigen
								that.show();
							}

							@Override
							public void cancelClicked()
							{
								// Activity wieder anzeigen
								that.show();
							}
						});
				return true;
			}

		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getFolderView(final SettingFolder SB, int backgroundChanger)
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
				File file = new File(SB.getValue());

				String ApsolutePath = "";
				if (file != null) ApsolutePath = file.getAbsolutePath();

				platformConector.getFolder(ApsolutePath, GlobalCore.Translations.Get("select_folder"),
						GlobalCore.Translations.Get("select"), new IgetFolderReturnListner()
						{

							@Override
							public void getFolderReturn(String Path)
							{
								SB.setValue(Path);
								resortList();
							}
						});

				return true;
			}

		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getFileView(final SettingFile SB, int backgroundChanger)
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
				File file = new File(SB.getValue());

				String Path = file.getParent();

				if (Path == null) Path = "";

				platformConector.getFile(Path, SB.getExt(), GlobalCore.Translations.Get("select_file"),
						GlobalCore.Translations.Get("select"), new IgetFileReturnListner()
						{

							@Override
							public void getFieleReturn(String Path)
							{
								SB.setValue(Path);
								resortList();
							}
						});

				return true;
			}

		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getButtonView(final SettingsListCategoryButton SB, int backgroundChanger)
	{
		Button btn = new Button(ButtonRec, "Button");

		btn.setDrageble();

		btn.setText(GlobalCore.Translations.Get(SB.getName()));

		if (SB.getName().equals("DebugDisplayInfo"))
		{
			btn.setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{

					if (SB.getName().equals("DebugDisplayInfo"))
					{
						String info = "";

						info += "Density= " + String.valueOf(GL_UISizes.DPI) + GlobalCore.br;
						info += "Height= " + String.valueOf(UiSizes.getWindowHeight()) + GlobalCore.br;
						info += "Width= " + String.valueOf(UiSizes.getWindowWidth()) + GlobalCore.br;
						info += "Scale= " + String.valueOf(UiSizes.getScale()) + GlobalCore.br;
						info += "FontSize= " + String.valueOf(UiSizes.getScaledFontSize()) + GlobalCore.br;

						info += "GPS Thread Time= " + String.valueOf(PositionChangedEventList.maxEventListTime) + GlobalCore.br;
						info += "GPS min pos Time= " + String.valueOf(PositionChangedEventList.minPosEventTime) + GlobalCore.br;
						info += "GPS min Orientation Time= " + String.valueOf(PositionChangedEventList.minOrientationEventTime)
								+ GlobalCore.br;

						GL_MsgBox.Show(info, MsgBoxreturnListner);

						return true;
					}

					return false;
				}

			});
		}

		btn.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return btn;
	}

	private CB_View_Base getApiKeyButtonView(final SettingsListGetApiButton SB, int backgroundChanger)
	{
		apiBtn = new API_Button(itemRec);
		apiBtn.setImage();
		return apiBtn;
	}

	private CB_View_Base getTimeView(final SettingTime SB, int backgroundChanger)
	{

		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
		final String trans = GlobalCore.Translations.Get(SB.getName());
		item.setName(trans);
		item.setDefault(intToTime(SB.getValue()));

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = SB.getName();

				String Value = intToTime(SB.getValue());
				String[] s = Value.split(":");

				int intValueMin = Integer.parseInt(s[0]);
				int intValueSec = Integer.parseInt(s[1]);

				// Show NumPad Int Edit
				NumerikInputBox.Show("default: " + GlobalCore.br + intToTime(SB.getDefaultValue()), trans, intValueMin, intValueSec,
						new returnValueListnerTime()
						{
							@Override
							public void returnValue(int min, int sec)
							{
								SettingTime SetValue = (SettingTime) Config.settings.get(EditKey);
								int value = (min * 60 * 1000) + (sec * 1000);
								if (SetValue != null) SetValue.setValue(value);
								resortList();
								// Activity wieder anzeigen
								that.show();
							}

							@Override
							public void cancelClicked()
							{
								// Activity wieder anzeigen
								that.show();
							}
						});
				return true;
			}

		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private String intToTime(int milliseconds)
	{
		int seconds = (int) (milliseconds / 1000) % 60;
		int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
		// int hours = (int) ((milliseconds / (1000*60*60)) % 24);

		return String.valueOf(minutes) + ":" + String.valueOf(seconds);
	}

	ArrayList<Langs> Sprachen;

	private CB_View_Base getLangSpinnerView(final SettingsListButtonLangSpinner SB)
	{
		Sprachen = GlobalCore.Translations.GetLangs(Config.settings.LanguagePath.getValue());

		if (Sprachen == null || Sprachen.size() == 0) return null;

		final String[] items = new String[Sprachen.size()];
		int index = 0;
		int selection = -1;

		File file1 = new File(Config.settings.Sel_LanguagePath.getValue());

		for (Langs tmp : Sprachen)
		{
			File file2 = new File(tmp.Path);
			if (file1.getAbsoluteFile().compareTo(file2.getAbsoluteFile()) == 0)
			{
				selection = index;
			}

			items[index++] = tmp.Name;
		}

		SpinnerAdapter adapter = new SpinnerAdapter()
		{

			@Override
			public String getText(int position)
			{
				return items[position];
			}

			@Override
			public Drawable getIcon(int Position)
			{
				return null;
			}

			@Override
			public int getCount()
			{
				return items.length;
			}
		};

		final Spinner spinner = new Spinner(ButtonRec, "LangSpinner", adapter, new selectionChangedListner()
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

		spinner.setSelection(selection);

		spinner.setPrompt(GlobalCore.Translations.Get("SelectLanguage"));

		spinner.setDrageble();

		return spinner;
	}

	private CB_View_Base getSkinSpinnerView(final SettingsListButtonSkinSpinner SB)
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
					String Path = f.getAbsolutePath();
					if (!Path.contains(".svn"))
					{
						skinFolders.add(Path);
					}

				}

				return false;
			}
		});

		final String[] items = new String[skinFolders.size()];
		int index = 0;
		int selection = -1;
		for (String tmp : skinFolders)
		{
			if (Config.settings.SkinFolder.getValue().equals(tmp)) selection = index;

			// cut folder name
			int Pos = tmp.lastIndexOf("/");
			if (Pos == -1) Pos = tmp.lastIndexOf("\\");
			tmp = tmp.substring(Pos + 1);

			items[index++] = tmp;
		}

		SpinnerAdapter adapter = new SpinnerAdapter()
		{

			@Override
			public String getText(int position)
			{
				return items[position];
			}

			@Override
			public Drawable getIcon(int Position)
			{
				return null;
			}

			@Override
			public int getCount()
			{
				return items.length;
			}
		};

		final Spinner spinner = new Spinner(itemRec, "SkinSpinner", adapter, new selectionChangedListner()
		{

			@Override
			public void selectionChanged(int index)
			{
				String selected = items[index];
				for (String tmp : skinFolders)
				{
					// cut folder name
					int Pos = tmp.lastIndexOf("/");
					if (Pos == -1) Pos = tmp.lastIndexOf("\\");
					String tmp2 = tmp.substring(Pos + 1);

					if (selected.equals(tmp2))
					{
						Config.settings.SkinFolder.setValue(tmp);

						break;
					}

				}
			}
		});

		spinner.setSelection(selection);

		spinner.setPrompt(GlobalCore.Translations.Get("SelectSkin"));

		spinner.setDrageble();

		return spinner;
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

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(GlobalCore.Translations.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return true;
			}

		});

		return item;

	}

	OnMsgBoxClickListener MsgBoxreturnListner = new OnMsgBoxClickListener()
	{

		@Override
		public boolean onClick(int which)
		{
			show();
			return true;
		}
	};

	public void resortList()
	{
		show();

		float scrollPos = scrollBox.getScrollY();
		scrollBox = null;
		LinearLayout = null;

		fillContent();
		scrollBox.scrollTo(scrollPos);
	}

}
