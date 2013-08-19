package CB_UI.GL_UI.Activitys.settings;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import CB_Locator.Events.PositionChangedEventList;
import CB_Translation_Base.TranslationEngine.Lang;
import CB_Translation_Base.TranslationEngine.SelectedLangChangedEvent;
import CB_Translation_Base.TranslationEngine.SelectedLangChangedEventList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.Enums.WrapType;
import CB_UI.Events.platformConector;
import CB_UI.Events.platformConector.IgetFileReturnListner;
import CB_UI.Events.platformConector.IgetFolderReturnListner;
import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.GL_View_Base;
import CB_UI.GL_UI.SoundCache;
import CB_UI.GL_UI.Activitys.ActivityBase;
import CB_UI.GL_UI.Controls.API_Button;
import CB_UI.GL_UI.Controls.Box;
import CB_UI.GL_UI.Controls.Button;
import CB_UI.GL_UI.Controls.FloatControl;
import CB_UI.GL_UI.Controls.Label;
import CB_UI.GL_UI.Controls.LinearCollapseBox;
import CB_UI.GL_UI.Controls.Linearlayout;
import CB_UI.GL_UI.Controls.QuickButtonList;
import CB_UI.GL_UI.Controls.ScrollBox;
import CB_UI.GL_UI.Controls.Spinner;
import CB_UI.GL_UI.Controls.SpinnerAdapter;
import CB_UI.GL_UI.Controls.chkBox;
import CB_UI.GL_UI.Controls.CollapseBox.animatetHeightChangedListner;
import CB_UI.GL_UI.Controls.Dialogs.NumerikInputBox;
import CB_UI.GL_UI.Controls.Dialogs.StringInputBox;
import CB_UI.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListner;
import CB_UI.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListnerDouble;
import CB_UI.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListnerTime;
import CB_UI.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI.GL_UI.Controls.Spinner.selectionChangedListner;
import CB_UI.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_UI.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
import CB_UI.GL_UI.Menu.Menu;
import CB_UI.GL_UI.Menu.MenuID;
import CB_UI.GL_UI.Menu.MenuItem;
import CB_UI.GL_UI.SoundCache.Sounds;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.AdvancedSettingsView.SettingsListButtonLangSpinner;
import CB_UI.GL_UI.Views.AdvancedSettingsView.SettingsListButtonSkinSpinner;
import CB_UI.GL_UI.Views.AdvancedSettingsView.SettingsListCategoryButton;
import CB_UI.GL_UI.Views.AdvancedSettingsView.SettingsListGetApiButton;
import CB_UI.Math.CB_RectF;
import CB_UI.Math.GL_UISizes;
import CB_UI.Math.UI_Size_Base;
import CB_Utils.Settings.Audio;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingDouble;
import CB_Utils.Settings.SettingEnum;
import CB_Utils.Settings.SettingFile;
import CB_Utils.Settings.SettingFolder;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingIntArray;
import CB_Utils.Settings.SettingLongString;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingString;
import CB_Utils.Settings.SettingTime;
import CB_Utils.Settings.SettingsAudio;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SettingsActivity extends ActivityBase implements SelectedLangChangedEvent
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
	public static int EditKey = -1;

	private Linearlayout LinearLayout;

	public SettingsActivity()
	{
		super(ActivityBase.ActivityRec(), "Settings");
		that = this;
		initial();
		SelectedLangChangedEventList.Add(this);
	}

	private void initial()
	{
		this.setLongClickable(true);
		Config.settings.SaveToLastValue();
		ButtonRec = new CB_RectF(leftBorder, 0, innerWidth, UI_Size_Base.that.getButtonHeight());

		itemRec = new CB_RectF(leftBorder, 0, ButtonRec.getWidth() - leftBorder - rightBorder, UI_Size_Base.that.getButtonHeight());

		createButtons();
		fillContent();
		resortList();
	}

	@Override
	protected void SkinIsChanged()
	{
		super.SkinIsChanged();
		this.removeChild(btnOk);
		this.removeChild(btnCancel);
		this.removeChild(btnMenu);
		createButtons();
		fillContent();
		resortList();
	}

	private void createButtons()
	{
		float btnW = (innerWidth - UI_Size_Base.that.getButtonWidth()) / 2;

		btnOk = new Button(leftBorder, this.getBottomHeight(), btnW, UI_Size_Base.that.getButtonHeight(), "OK Button");
		btnMenu = new Button(btnOk.getMaxX(), this.getBottomHeight(), UI_Size_Base.that.getButtonWidth(),
				UI_Size_Base.that.getButtonHeight(), "Menu Button");
		btnCancel = new Button(btnMenu.getMaxX(), this.getBottomHeight(), btnW, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

		// Translations
		btnOk.setText(Translation.Get("save"));
		btnCancel.setText(Translation.Get("cancel"));

		this.addChild(btnMenu);
		btnMenu.setText("...");
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

				icm.setPrompt(Translation.Get("changeSettingsVisibility"));

				icm.Show();
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

			SettingsListButtonLangSpinner<?> lang = new SettingsListButtonLangSpinner<Object>("Lang", SettingCategory.Button,
					SettingModus.Normal, SettingStoreType.Global);
			CB_View_Base langView = getLangSpinnerView(lang);

			addControlToLinearLayout(langView, margin);

			// SettingsListCategoryButton quick = new SettingsListCategoryButton("QuickList", SettingCategory.Button, SettingModus.Normal,
			// true);
			// CB_View_Base quickView = getButtonView(quick, 0);
			// addControlToLinearLayout(quickView, margin);

			ArrayList<SettingBase<?>> SortedSettingList = new ArrayList<SettingBase<?>>();// Config.settings.values().toArray();

			for (Iterator<SettingBase<?>> it = Config.settings.iterator(); it.hasNext();)
			{
				SortedSettingList.add(it.next());
			}

			Collections.sort(SortedSettingList);

			do
			{
				int position = 0;

				SettingCategory cat = iteratorCat.next();
				SettingsListCategoryButton<?> catBtn = new SettingsListCategoryButton<Object>(cat.name(), SettingCategory.Button,
						SettingModus.Normal, SettingStoreType.Global);

				final CB_View_Base btn = getView(catBtn, 1);

				// add Cat einträge
				final LinearCollapseBox lay = new LinearCollapseBox(btn, "");
				lay.setClickable(true);
				lay.setAnimationListner(new animatetHeightChangedListner()
				{

					@Override
					public void animatedHeightChanged(float Height)
					{
						LinearLayout.layout();

						LinearLayout.setZeroPos();
						scrollBox.setVirtualHeight(LinearLayout.getHeight());

					}
				});

				int entryCount = 0;
				if (cat == SettingCategory.Login)
				{
					SettingsListGetApiButton<?> lgIn = new SettingsListGetApiButton<Object>(cat.name(), SettingCategory.Button,
							SettingModus.Normal, SettingStoreType.Global);
					final CB_View_Base btnLgIn = getView(lgIn, 1);
					lay.addChild(btnLgIn);
					entryCount++;
				}

				if (cat == SettingCategory.QuickList)
				{

					final SettingsItem_QuickButton btnLgIn = new SettingsItem_QuickButton(itemRec, "QuickButtonEditor");
					lay.addChild(btnLgIn);
					entryCount++;
				}

				if (cat == SettingCategory.Debug)
				{
					SettingsListCategoryButton<?> disp = new SettingsListCategoryButton<Object>("DebugDisplayInfo", SettingCategory.Button,
							SettingModus.Normal, SettingStoreType.Global);
					final CB_View_Base btnDisp = getView(disp, 1);

					btnDisp.setSize(itemRec);

					lay.addChild(btnDisp);
					entryCount++;
				}

				if (cat == SettingCategory.Skin)
				{
					SettingsListButtonSkinSpinner<?> skin = new SettingsListButtonSkinSpinner<Object>("Skin", SettingCategory.Button,
							SettingModus.Normal, SettingStoreType.Global);
					CB_View_Base skinView = getSkinSpinnerView(skin);
					lay.addChild(skinView);
					entryCount++;
				}

				if (cat == SettingCategory.Sounds)
				{
					CB_RectF rec = itemRec.copy();
					Box lblBox = new Box(rec, "LabelBox");

					CB_RectF rec2 = rec.copy();
					rec2.setWidth(rec.getWidth() - (rec.getLeft() * 2));
					rec2.setHeight(rec.getHalfHeight());

					Label lblVolume = new Label(itemRec, Translation.Get("Volume"));
					Label lblMute = new Label(itemRec, Translation.Get("Mute"));

					lblVolume.setZeroPos();
					lblMute.setZeroPos();

					lblMute.setHAlignment(HAlignment.RIGHT);

					lblBox.addChild(lblMute);
					lblBox.addChild(lblVolume);

					lay.addChild(lblBox);
					entryCount++;
				}

				Boolean expandLayout = false;

				// int layoutHeight = 0;
				for (Iterator<SettingBase<?>> it = SortedSettingList.iterator(); it.hasNext();)
				{
					SettingBase<?> settingItem = it.next();
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
							entryCount++;
							Config.settings.indexOf(settingItem);
							if (Config.settings.indexOf(settingItem) == EditKey)
							{
								expandLayout = true;
							}

						}
					}
				}

				if (entryCount > 0)
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

		setVolumeState(Config.settings.GlobalVolume.getValue().Mute);
		apiBtn.setImage();

	}

	private void addControlToLinearLayout(CB_View_Base view, float itemMargin)
	{
		if (LinearLayout == null || scrollBox == null)
		{

			CB_RectF rec = new CB_RectF(0, btnOk.getMaxY() + margin, this.width, this.height - btnOk.getMaxY() - margin);

			scrollBox = new ScrollBox(rec);
			scrollBox.setClickable(true);
			scrollBox.setLongClickable(true);
			LinearLayout = new Linearlayout(ButtonRec.getWidth(), "SettingsActivity-LinearLayout");
			LinearLayout.setClickable(true);
			LinearLayout.setLongClickable(true);
			LinearLayout.setZeroPos();
			scrollBox.addChild(LinearLayout);
			// LinearLayout.setBackground(new ColorDrawable(Color.RED));
			scrollBox.setBackground(this.getBackground());
			this.addChild(scrollBox);
		}

		view.setZeroPos();
		view.setClickable(true);
		view.setLongClickable(true);

		LinearLayout.addChild(view, itemMargin);
		LinearLayout.setZeroPos();
		scrollBox.setVirtualHeight(LinearLayout.getHeight());

	}

	private CB_View_Base getView(SettingBase<?> SB, int BackgroundChanger)
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
			return getButtonView((SettingsListCategoryButton<?>) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingsListGetApiButton)
		{
			return getApiKeyButtonView((SettingsListGetApiButton<?>) SB, BackgroundChanger);
		}
		else if (SB instanceof SettingsListButtonLangSpinner)
		{
			return getLangSpinnerView((SettingsListButtonLangSpinner<?>) SB);
		}
		else if (SB instanceof SettingsListButtonSkinSpinner)
		{
			return getSkinSpinnerView((SettingsListButtonSkinSpinner<?>) SB);
		}
		else if (SB instanceof SettingsAudio)
		{
			return getAudioView((SettingsAudio) SB, BackgroundChanger);
		}

		return null;
	}

	private CB_View_Base getStringView(final SettingString SB, int backgroundChanger)
	{
		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
		final String trans = Translation.Get(SB.getName());
		item.setName(trans);
		item.setDefault(SB.getValue());

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = Config.settings.indexOf(SB);

				WrapType type;

				type = (SB instanceof SettingLongString) ? WrapType.WRAPPED : WrapType.SINGLELINE;

				StringInputBox.Show(type, "default:" + GlobalCore.br + SB.getDefaultValue(), trans, SB.getValue(),
						new OnMsgBoxClickListener()
						{

							@Override
							public boolean onClick(int which, Object data)
							{
								String text = StringInputBox.editText.getText().toString();
								if (which == GL_MsgBox.BUTTON_POSITIVE)
								{
									SettingString value = (SettingString) Config.settings.get(EditKey);

									// api ohne lineBreak
									if (value.getName().equalsIgnoreCase("GcAPI"))
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

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return true;
			}

		});

		return item;

	}

	private CB_View_Base getEnumView(final SettingEnum<?> SB, int backgroundChanger)
	{

		SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

		item.setName(Translation.Get(SB.getName()));

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

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;
	}

	private CB_View_Base getIntArrayView(final SettingIntArray SB, int backgroundChanger)
	{

		SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

		item.setName(Translation.Get(SB.getName()));

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

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getIntView(final SettingInt SB, int backgroundChanger)
	{

		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
		final String trans = Translation.Get(SB.getName());

		item.setName(trans);
		item.setDefault(String.valueOf(SB.getValue()));

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = Config.settings.indexOf(SB);

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

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;
	}

	private CB_View_Base getDblView(final SettingDouble SB, int backgroundChanger)
	{
		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
		final String trans = Translation.Get(SB.getName());
		item.setName(trans);
		item.setDefault(String.valueOf(SB.getValue()));

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = Config.settings.indexOf(SB);

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

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getFolderView(final SettingFolder SB, int backgroundChanger)
	{

		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());

		item.setName(Translation.Get(SB.getName()));
		item.setDefault(SB.getValue());

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = Config.settings.indexOf(SB);
				File file = new File(SB.getValue());

				final String ApsolutePath = (file != null) ? file.getAbsolutePath() : "";

				Menu icm = new Menu("FileactionMenu");
				icm.addItemClickListner(new OnClickListener()
				{

					@Override
					public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
					{
						switch (((MenuItem) v).getMenuItemId())
						{
						case MenuID.MI_SELECT_PATH:
							platformConector.getFolder(ApsolutePath, Translation.Get("select_folder"), Translation.Get("select"),
									new IgetFolderReturnListner()
									{

										@Override
										public void getFolderReturn(String Path)
										{
											SB.setValue(Path);
											resortList();
										}
									});
							return true;

						case MenuID.MI_CLEAR_PATH:
							SB.setValue("");
							resortList();
							return true;
						}
						return true;
					}
				});

				icm.addItem(MenuID.MI_SELECT_PATH, "select_folder");
				icm.addItem(MenuID.MI_CLEAR_PATH, "ClearPath");
				icm.Show();

				return true;
			}

		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getFileView(final SettingFile SB, int backgroundChanger)
	{
		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());

		item.setName(Translation.Get(SB.getName()));
		item.setDefault(SB.getValue());

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = Config.settings.indexOf(SB);
				File file = new File(SB.getValue());

				final String Path = (file.getParent() != null) ? file.getParent() : "";

				Menu icm = new Menu("FileactionMenu");
				icm.addItemClickListner(new OnClickListener()
				{

					@Override
					public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
					{
						switch (((MenuItem) v).getMenuItemId())
						{
						case MenuID.MI_SELECT_PATH:
							platformConector.getFile(Path, SB.getExt(), Translation.Get("select_file"), Translation.Get("select"),
									new IgetFileReturnListner()
									{
										@Override
										public void getFieleReturn(String Path)
										{
											SB.setValue(Path);
											resortList();
										}
									});
							return true;

						case MenuID.MI_CLEAR_PATH:
							SB.setValue("");
							resortList();
							return true;
						}
						return true;
					}
				});

				icm.addItem(MenuID.MI_SELECT_PATH, "select_file");
				icm.addItem(MenuID.MI_CLEAR_PATH, "ClearPath");
				icm.Show();
				return true;
			}

		});

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return item;

	}

	private CB_View_Base getButtonView(final SettingsListCategoryButton<?> SB, int backgroundChanger)
	{
		Button btn = new Button(ButtonRec, "Button");

		btn.setDrageble();

		btn.setText(Translation.Get(SB.getName()));

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
						info += "Height= " + String.valueOf(UI_Size_Base.that.getWindowHeight()) + GlobalCore.br;
						info += "Width= " + String.valueOf(UI_Size_Base.that.getWindowWidth()) + GlobalCore.br;
						info += "Scale= " + String.valueOf(UI_Size_Base.that.getScale()) + GlobalCore.br;
						info += "FontSize= " + String.valueOf(UI_Size_Base.that.getScaledFontSize()) + GlobalCore.br;
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

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return false;
			}

		});

		return btn;
	}

	private CB_View_Base getApiKeyButtonView(final SettingsListGetApiButton<?> SB, int backgroundChanger)
	{
		apiBtn = new API_Button(itemRec);
		apiBtn.setImage();
		return apiBtn;
	}

	/**
	 * List of audio settings which can mute with GlobalVolume settings
	 */
	ArrayList<SettingsItem_Audio> audioSettingsList;

	private CB_View_Base getAudioView(final SettingsAudio SB, int backgroundChanger)
	{

		boolean full = Config.settings.SettingsShowExpert.getValue() || Config.settings.SettingsShowAll.getValue();
		final String AudioName = SB.getName();
		final SettingsItem_Audio item = new SettingsItem_Audio(itemRec, backgroundChanger, SB.getName(), full,
				new FloatControl.iValueChanged()
				{

					@Override
					public void ValueChanged(int value)
					{
						Audio aud = new Audio(SB.getValue());
						aud.Volume = (float) value / 100f;
						SB.setValue(aud);

						// play Audio now

						if (AudioName.equalsIgnoreCase("GlobalVolume")) SoundCache.play(Sounds.Global, true);
						if (AudioName.equalsIgnoreCase("Approach")) SoundCache.play(Sounds.Approach);
						if (AudioName.equalsIgnoreCase("GPS_lose")) SoundCache.play(Sounds.GPS_lose);
						if (AudioName.equalsIgnoreCase("GPS_fix")) SoundCache.play(Sounds.GPS_fix);
						if (AudioName.equalsIgnoreCase("AutoResortSound")) SoundCache.play(Sounds.AutoResortSound);
					}
				});

		item.setName(Translation.Get(SB.getName()));
		item.setDefault("default: " + String.valueOf(SB.getDefaultValue()));
		item.setVolume((int) (SB.getValue().Volume * 100));
		chkBox chk = item.getCheckBox();

		if (!AudioName.contains("Global"))
		{
			if (audioSettingsList == null) audioSettingsList = new ArrayList<SettingsItem_Audio>();
			audioSettingsList.add(item);
		}

		chk.setChecked(SB.getValue().Mute);
		chk.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(chkBox view, boolean isChecked)
			{
				Audio aud = new Audio(SB.getValue());
				aud.Mute = isChecked;
				SB.setValue(aud);
				item.setMuteDisabeld(isChecked);
				if (AudioName.contains("Global"))
				{
					// Enable or disable all other
					setVolumeState(isChecked);
				}
			}

		});

		item.setMuteDisabeld(chk.isChecked());

		item.setOnLongClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// zeige Beschreibung der Einstellung

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return true;
			}

		});

		return item;

	}

	private void setVolumeState(boolean globalEnabled)
	{
		if (audioSettingsList != null)
		{
			for (SettingsItem_Audio it : audioSettingsList)
			{
				if (globalEnabled)
				{
					it.disable();
				}
				else
				{
					it.enable();
				}
			}
		}
	}

	private CB_View_Base getTimeView(final SettingTime SB, int backgroundChanger)
	{

		SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
		final String trans = Translation.Get(SB.getName());
		item.setName(trans);
		item.setDefault(intToTime(SB.getValue()));

		item.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				EditKey = Config.settings.indexOf(SB);

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

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

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

	ArrayList<Lang> Sprachen;

	private CB_View_Base getLangSpinnerView(final SettingsListButtonLangSpinner<?> SB)
	{
		Sprachen = Translation.GetLangs(Config.settings.LanguagePath.getValue());

		if (Sprachen == null || Sprachen.size() == 0) return null;

		final String[] items = new String[Sprachen.size()];
		int index = 0;
		int selection = -1;

		File file1 = new File(Config.settings.Sel_LanguagePath.getValue());

		for (Lang tmp : Sprachen)
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
				for (Lang tmp : Sprachen)
				{
					if (selected.equals(tmp.Name))
					{
						Config.settings.Sel_LanguagePath.setValue(tmp.Path);
						try
						{
							Translation.LoadTranslation(tmp.Path);
						}
						catch (Exception e)
						{
							try
							{
								Translation.LoadTranslation(Config.settings.Sel_LanguagePath.getDefaultValue());
							}
							catch (IOException e1)
							{
								e1.printStackTrace();
							}
						}
						break;
					}

				}
			}
		});

		spinner.setSelection(selection);

		spinner.setPrompt(Translation.Get("SelectLanguage"));

		spinner.setDrageble();

		return spinner;
	}

	private CB_View_Base getSkinSpinnerView(final SettingsListButtonSkinSpinner<?> SB)
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

		final String[] items = new String[skinFolders.size() + 2];// + internal (default and small)

		items[0] = "default";
		items[1] = "small";

		int index = 2;
		int selection = -1;

		if (Config.settings.SkinFolder.getValue().contains("default")) selection = 0;
		if (Config.settings.SkinFolder.getValue().contains("small")) selection = 1;

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

				if (selected.equals("default"))
				{
					Config.settings.SkinFolder.setValue("default");
				}
				else if (selected.equals("small"))
				{
					Config.settings.SkinFolder.setValue("small");
				}
				else
				{
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
			}
		});

		spinner.setSelection(selection);

		spinner.setPrompt(Translation.Get("SelectSkin"));

		spinner.setDrageble();

		return spinner;
	}

	private CB_View_Base getBoolView(final SettingBool SB, int backgroundChanger)
	{

		SettingsItem_Bool item = new SettingsItem_Bool(itemRec, backgroundChanger, SB.getName());

		item.setName(Translation.Get(SB.getName()));
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

				GL_MsgBox.Show(Translation.Get("Desc_" + SB.getName()), MsgBoxreturnListner);

				return true;
			}

		});

		return item;

	}

	OnMsgBoxClickListener MsgBoxreturnListner = new OnMsgBoxClickListener()
	{

		@Override
		public boolean onClick(int which, Object data)
		{
			show();
			return true;
		}
	};

	public void resortList()
	{
		// show();

		float scrollPos = scrollBox.getScrollY();
		scrollBox = null;
		LinearLayout = null;

		fillContent();
		scrollBox.scrollTo(scrollPos);
	}

	@Override
	public void SelectedLangChangedEventCalled()
	{
		initial();
	}

}
