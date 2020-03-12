package de.droidcachebox.settings;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.droidcachebox.Config;
import de.droidcachebox.SoundCache;
import de.droidcachebox.SoundCache.Sounds;
import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.QuickButtonList;
import de.droidcachebox.gdx.activities.ColorPicker;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox.IReturnValueListener;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox.IReturnValueListenerDouble;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox.IReturnValueListenerTime;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBox.OnMsgBoxClickListener;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.MapView;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.menu.QuickButtonItem;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.translation.Lang;
import de.droidcachebox.translation.SelectedLangChangedEvent;
import de.droidcachebox.translation.SelectedLangChangedEventList;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.Config_Core;
import de.droidcachebox.utils.FileFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static de.droidcachebox.utils.Config_Core.br;

public class SettingsActivity extends ActivityBase implements SelectedLangChangedEvent {

    private static int EditKey = -1;
    private static SettingsActivity that;
    private ArrayList<SettingsItem_Audio> audioSettingsList;
    private ArrayList<Lang> Sprachen;
    private OnMsgBoxClickListener msgBoxReturnListener = (which, data) -> {
        show();
        return true;
    };
    private CB_List<SettingCategory> Categorys = new CB_List<>();
    private CB_Button btnOk, btnCancel, btnMenu;
    private ScrollBox scrollBox;
    private CB_RectF ButtonRec, itemRec;
    private API_Button apiBtn;
    private Linearlayout LinearLayout;

    public SettingsActivity() {
        super("Settings");
        initial();
        SelectedLangChangedEventList.Add(this);
        that = this;
    }

    public static void resortList() {
        // show();
        if (that != null) {
            float scrollPos = that.scrollBox.getScrollY();
            that.scrollBox = null;
            that.LinearLayout = null;

            that.fillContent();
            that.scrollBox.scrollTo(scrollPos);
        }

    }

    private void initial() {
        setLongClickable(true);
        Config.settings.SaveToLastValue();
        ButtonRec = new CB_RectF(leftBorder, 0, innerWidth, UiSizes.getInstance().getButtonHeight());

        itemRec = new CB_RectF(leftBorder, 0, ButtonRec.getWidth() - leftBorder - rightBorder, UiSizes.getInstance().getButtonHeight());

        createButtons();
        fillContent();
        resortList();
    }

    @Override
    protected void skinIsChanged() {
        super.skinIsChanged();
        removeChild(btnOk);
        removeChild(btnCancel);
        removeChild(btnMenu);
        createButtons();
        fillContent();
        resortList();
    }

    private void createButtons() {
        float btnW = (innerWidth - UiSizes.getInstance().getButtonHeight()) / 2;

        btnOk = new CB_Button(leftBorder, getBottomHeight(), btnW, UiSizes.getInstance().getButtonHeight(), "OK Button");
        btnMenu = new CB_Button(new CB_RectF(btnOk.getMaxX(), getBottomHeight(), UiSizes.getInstance().getButtonHeight()), "Menu Button");
        btnCancel = new CB_Button(btnMenu.getMaxX(), getBottomHeight(), btnW, UiSizes.getInstance().getButtonHeight(), "Cancel Button");

        btnOk.setText(Translation.get("save"));
        btnCancel.setText(Translation.get("cancel"));

        addChild(btnMenu);
        btnMenu.setText("...");
        btnMenu.setClickHandler((v, x, y, pointer, button) -> {
            Menu icm = new Menu("SettingsLevelTitle");

            if (Config.isDeveloper.getValue())
                Config.isExpert.setValue(false);

            icm.addCheckableMenuItem("Settings_Expert", Config.isExpert.getValue(), () -> {
                Config.isExpert.setValue(!Config.isExpert.getValue());
                Config.isDeveloper.setValue(false);
                resortList();
            });

            icm.addCheckableMenuItem("Settings_All", Config.isDeveloper.getValue(), () -> {
                Config.isDeveloper.setValue(!Config.isDeveloper.getValue());
                Config.isExpert.setValue(false);
                resortList();
            });

            icm.setPrompt(Translation.get("SettingsLevelTitle"));

            icm.show();
            return true;
        });

        addChild(btnOk);
        btnOk.setClickHandler((v, x, y, pointer, button) -> {

            StringBuilder ActionsString = new StringBuilder();
            int counter = 0;
            for (int i = 0, n = SettingsItem_QuickButton.tmpQuickList.size(); i < n; i++) {
                QuickButtonItem tmp = SettingsItem_QuickButton.tmpQuickList.get(i);
                ActionsString.append(tmp.getQuickAction().ordinal());
                if (counter < SettingsItem_QuickButton.tmpQuickList.size() - 1) {
                    ActionsString.append(",");
                }
                counter++;
            }
            Config.quickButtonList.setValue(ActionsString.toString());

            Config.settings.SaveToLastValue();
            Config.AcceptChanges();

            // Notify QuickButtonList
            QuickButtonList.that.notifyDataSetChanged();

            ShowMap.getInstance().normalMapView.setNewSettings(MapView.INITIAL_NEW_SETTINGS);

            finish();
            return true;
        });

        addChild(btnCancel);
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            Config.settings.LoadFromLastValue();

            finish();
            return true;
        });

    }

    private void fillContent() {

        if (Categorys == null) {
            Categorys = new CB_List<>();
        }
        Categorys.clear();
        SettingCategory[] tmp = SettingCategory.values();
        for (SettingCategory item : tmp) {
            if (item != SettingCategory.Button) {
                Categorys.add(item);
            }
        }

        // SettingsListButtonLangSpinner<?> lang = new SettingsListButtonLangSpinner<>("Lang", SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);
        CB_View_Base langView = getLangSpinnerView();

        addControlToLinearLayout(langView, margin);

        ArrayList<SettingBase<?>> AllSettingList = new ArrayList<>();// Config.settings.values().toArray();
        for (SettingBase<?> settingItem : Config.settings) {
            if (settingItem.getUsage() == SettingUsage.ACB || settingItem.getUsage() == SettingUsage.ALL)
                // item nur zur Liste Hinzufügen, wenn der SettingModus dies auch zulässt.
                if (((settingItem.getModus() == SettingModus.NORMAL) || (settingItem.getModus() == SettingModus.EXPERT && Config.isExpert.getValue()) || Config.isDeveloper.getValue())
                        && (settingItem.getModus() != SettingModus.NEVER)) {
                    AllSettingList.add(settingItem);
                }
        }

        for (SettingCategory cat : Categorys) {
            ArrayList<SettingBase<?>> CatList = new ArrayList<>();
            for (SettingBase<?> settingItem : AllSettingList) {
                if (settingItem.getCategory().name().equals(cat.name())) {
                    CatList.add(settingItem);
                }
            }
            Collections.sort(CatList, new SettingsOrder());

            int position = 0;

            SettingsListCategoryButton<?> catBtn = new SettingsListCategoryButton<>(cat.name(), SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);

            final CB_View_Base btn = getView(catBtn, 1);

            // add Cat einträge
            final LinearCollapseBox lay = new LinearCollapseBox(btn, "");
            lay.setClickable(true);
            lay.setAnimationListener(Height -> {
                LinearLayout.layout();

                LinearLayout.setZeroPos();
                scrollBox.setVirtualHeight(LinearLayout.getHeight());

            });

            int entryCount = 0;

            switch (cat) {
                case Login:
                    SettingsListGetApiButton<?> lgIn = new SettingsListGetApiButton<>(cat.name(), SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);
                    lay.addChild(getView(lgIn, 1));
                    entryCount++;
                    break;
                case QuickList:
                    lay.addChild(new SettingsItem_QuickButton(itemRec, "QuickButtonEditor"));
                    entryCount++;
                    break;
                case Debug:
                    SettingsListCategoryButton<?> disp = new SettingsListCategoryButton<>("DebugDisplayInfo", SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);
                    final CB_View_Base btnDisp = getView(disp, 1);
                    if (btnDisp != null) {
                        btnDisp.setSize(itemRec);
                        lay.addChild(btnDisp);
                        entryCount++;
                    }
                    break;
                case Skin:
                    SettingsListButtonSkinSpinner<?> skin = new SettingsListButtonSkinSpinner<>("Skin", SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);
                    CB_View_Base skinView = getSkinSpinnerView();
                    lay.addChild(skinView);
                    entryCount++;
                    break;
                case Sounds:
                    CB_RectF rec = new CB_RectF(itemRec);
                    Box lblBox = new Box(rec, "LabelBox");

                    CB_RectF rec2 = new CB_RectF(rec);
                    rec2.setWidth(rec.getWidth() - (rec.getX() * 2));
                    rec2.setHeight(rec.getHalfHeight());

                    CB_Label lblVolume = new CB_Label(name + " lblVolume", itemRec, Translation.get("Volume"));
                    CB_Label lblMute = new CB_Label(name + " lblMute", itemRec, Translation.get("Mute"));

                    lblVolume.setZeroPos();
                    lblMute.setZeroPos();

                    lblMute.setHAlignment(HAlignment.RIGHT);

                    lblBox.addChild(lblMute);
                    lblBox.addChild(lblVolume);

                    lay.addChild(lblBox);
                    entryCount++;
                    break;

            }

            boolean expandLayout = false;

            for (SettingBase<?> settingItem : CatList) {
                final CB_View_Base view = getView(settingItem, position++);
                if (view != null) {

                    if (Config.DraftsLoadAll.getValue() && settingItem.getName().equalsIgnoreCase("DraftsLoadLength")) {
                        ((SettingsItemBase) view).disable();
                    }

                    if (view instanceof CB_Button) {
                        view.setSize(itemRec);
                    }

                    lay.addChild(view);
                    entryCount++;
                    Config.settings.indexOf(settingItem);
                    if (Config.settings.indexOf(settingItem) == EditKey) {
                        expandLayout = true;
                    }
                }

            }

            if (entryCount > 0) {

                lay.setBackground(getBackground());// Activity Background
                if (!expandLayout)
                    lay.setAnimationHeight(0f);

                addControlToLinearLayout(btn, margin);
                addControlToLinearLayout(lay, -(drawableBackground.getBottomHeight()) / 2);

                if (btn != null)
                    btn.setClickHandler((v, x, y, pointer, button) -> {
                        lay.Toggle();
                        return true;
                    });
            }
        }

        setVolumeState(Config.globalVolume.getValue().Mute);
        apiBtn.setImage();

    }

    private void addControlToLinearLayout(CB_View_Base view, float itemMargin) {
        if (LinearLayout == null || scrollBox == null) {

            CB_RectF rec = new CB_RectF(0, btnOk.getMaxY() + margin, getWidth(), getHeight() - btnOk.getMaxY() - margin);

            scrollBox = new ScrollBox(rec);
            scrollBox.setClickable(true);
            scrollBox.setLongClickable(true);
            LinearLayout = new Linearlayout(ButtonRec.getWidth(), "SettingsActivity-LinearLayout");
            LinearLayout.setClickable(true);
            LinearLayout.setLongClickable(true);
            LinearLayout.setZeroPos();
            scrollBox.addChild(LinearLayout);
            // LinearLayout.setBackground(new ColorDrawable(Color.RED));
            scrollBox.setBackground(getBackground());
            addChild(scrollBox);
        }

        view.setZeroPos();
        view.setClickable(true);
        view.setLongClickable(true);

        LinearLayout.addChild(view, itemMargin);
        LinearLayout.setZeroPos();
        scrollBox.setVirtualHeight(LinearLayout.getHeight());

    }

    private CB_View_Base getView(SettingBase<?> settingBase, int backgroundChanger) {
        if (settingBase instanceof SettingBool) {
            return getBoolView((SettingBool) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingIntArray) {
            return getIntArrayView((SettingIntArray) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingStringArray) {
            return getStringArrayView((SettingStringArray) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingTime) {
            return getTimeView((SettingTime) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingInt) {
            return getIntView((SettingInt) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingDouble) {
            return getDblView((SettingDouble) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingFloat) {
            return getFloatView((SettingFloat) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingFolder) {
            return getFolderView((SettingFolder) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingFile) {
            return getFileView((SettingFile) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingEnum) {
            return getEnumView((SettingEnum<?>) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingString) {
            return getStringView((SettingString) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingsListCategoryButton) {
            return getButtonView((SettingsListCategoryButton<?>) settingBase);
        } else if (settingBase instanceof SettingsListGetApiButton) {
            return getApiKeyButtonView();
        } else if (settingBase instanceof SettingsListButtonLangSpinner) {
            return getLangSpinnerView();
        } else if (settingBase instanceof SettingsListButtonSkinSpinner) {
            return getSkinSpinnerView();
        } else if (settingBase instanceof SettingsAudio) {
            return getAudioView((SettingsAudio) settingBase, backgroundChanger);
        } else if (settingBase instanceof SettingColor) {
            return getColorView((SettingColor) settingBase, backgroundChanger);
        }

        return null;
    }

    private CB_View_Base getColorView(final SettingColor SB, int backgroundChanger) {
        SettingsItemBase item = new SettingsItem_Color(itemRec, backgroundChanger, SB);
        final String trans = Translation.get(SB.getName());

        item.setName(trans);
        item.setDefault(SB.getValue() + "\n\n" + Translation.get("Desc_" + SB.getName()));

        item.setClickHandler((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(SB);

            GL.that.RunOnGLWithThreadCheck(() -> {
                ColorPicker clrPick = new ColorPicker(SB.getValue(), color -> {
                    if (color == null)
                        return; // nothing changed

                    SettingColor SetValue = (SettingColor) Config.settings.get(EditKey);
                    if (SetValue != null)
                        SetValue.setValue(color);
                    resortList();
                    // Activity wieder anzeigen
                    show();
                });
                clrPick.show();
            });

            return true;
        });

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return false;
        });

        return item;
    }

    private CB_View_Base getStringView(final SettingString SB, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
        final String trans = Translation.get(SB.getName());
        item.setName(trans);

        String toShow;
        if (SB.isDefault()) {
            toShow = Translation.get("default");
        } else {
            toShow = SB.getValue();
        }
        item.setDefault(toShow + "\n\n" + Translation.get("Desc_" + SB.getName()));

        item.setClickHandler((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(SB);

            WrapType type;

            type = (SB instanceof SettingLongString) ? WrapType.WRAPPED : WrapType.SINGLELINE;

            StringInputBox.show(type, "default:" + br + SB.getDefaultValue(), trans, SB.getValue(),
                    (which, data) -> {
                        String text = StringInputBox.editText.getText();
                        if (which == MessageBox.BTN_LEFT_POSITIVE) {
                            SettingString value = (SettingString) Config.settings.get(EditKey);

                            // api ohne lineBreak
                            if (value.getName().equalsIgnoreCase("AccessToken")) {
                                text = text.replace("\r", "");
                                text = text.replace("\n", "");
                            }

                            value.setValue(text);

                            resortList();
                        }
                        // Activity wieder anzeigen
                        activityBase.show();
                        return true;
                    });

            return true;
        });

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return true;
        });

        return item;

    }

    private CB_View_Base getEnumView(final SettingEnum<?> SB, int backgroundChanger) {

        SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

        item.setName(Translation.get(SB.getName()));

        item.setDefault("\n" + Translation.get("Desc_" + SB.getName()) + "\n");

        final Spinner spinner = item.getSpinner();

        spinner.setDraggable();

        final SpinnerAdapter adapter = new SpinnerAdapter() {

            @Override
            public String getText(int position) {
                return Translation.get(SB.getValues().get(position));
            }

            @Override
            public Drawable getIcon(int Position) {
                return null;
            }

            @Override
            public int getCount() {
                return SB.getValues().size();
            }
        };

        spinner.setAdapter(adapter);
        spinner.setSelection(SB.getValues().indexOf(SB.getValue()));

        spinner.setSelectionChangedListener(index -> SB.setValue(SB.getValues().get(index)));

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return false;
        });

        return item;
    }

    private CB_View_Base getIntArrayView(final SettingIntArray SB, int backgroundChanger) {

        SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

        item.setName(Translation.get(SB.getName()));

        item.setDefault("\n" + Translation.get("Desc_" + SB.getName()) + "\n");

        final Spinner spinner = item.getSpinner();

        spinner.setDraggable();

        final SpinnerAdapter adapter = new SpinnerAdapter() {

            @Override
            public String getText(int position) {
                return String.valueOf(SB.getValues()[position]);
            }

            @Override
            public Drawable getIcon(int Position) {
                return null;
            }

            @Override
            public int getCount() {
                return SB.getValues().length;
            }
        };

        spinner.setAdapter(adapter);
        spinner.setSelection(SB.getIndex());

        spinner.setSelectionChangedListener(index -> SB.setValue(SB.getValueFromIndex(index)));

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return false;
        });

        return item;

    }

    private CB_View_Base getStringArrayView(final SettingStringArray SB, int backgroundChanger) {

        SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

        item.setName(Translation.get(SB.getName()));

        item.setDefault("\n" + Translation.get("Desc_" + SB.getName()) + "\n");

        final Spinner spinner = item.getSpinner();

        spinner.setDraggable();

        final SpinnerAdapter adapter = new SpinnerAdapter() {

            @Override
            public String getText(int position) {
                return SB.possibleValues()[position];
            }

            @Override
            public Drawable getIcon(int Position) {
                return null;
            }

            @Override
            public int getCount() {
                return SB.possibleValues().length;
            }
        };

        spinner.setAdapter(adapter);
        spinner.setSelection(SB.getIndexOfValue());

        spinner.setSelectionChangedListener(index -> SB.setValue(SB.getValueFromIndex(index)));

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return false;
        });

        return item;

    }

    private CB_View_Base getIntView(final SettingInt SB, int backgroundChanger) {

        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
        final String trans = Translation.get(SB.getName());

        item.setName(trans);
        item.setDefault(SB.getValue() + "\n\n" + Translation.get("Desc_" + SB.getName()));

        item.setClickHandler((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(SB);

            // Show NumPad Int Edit
            NumericInputBox.Show("default: " + br + SB.getDefaultValue(), trans, SB.getValue(), new IReturnValueListener() {
                @Override
                public void returnValue(int value) {
                    SettingInt SetValue = (SettingInt) Config.settings.get(EditKey);
                    if (SetValue != null)
                        SetValue.setValue(value);
                    resortList();
                    // Activity wieder anzeigen
                    show();
                }

                @Override
                public void cancelClicked() {
                    // Activity wieder anzeigen
                    show();
                }

            });
            return true;
        });

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return false;
        });

        return item;
    }

    private CB_View_Base getDblView(final SettingDouble SB, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
        final String trans = Translation.get(SB.getName());
        item.setName(trans);
        item.setDefault(SB.getValue() + "\n\n" + Translation.get("Desc_" + SB.getName()));

        item.setClickHandler((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(SB);

            // Show NumPad Int Edit
            NumericInputBox.Show("default: " + br + SB.getDefaultValue(), trans, SB.getValue(), new IReturnValueListenerDouble() {
                @Override
                public void returnValue(double value) {
                    SettingDouble SetValue = (SettingDouble) Config.settings.get(EditKey);
                    if (SetValue != null)
                        SetValue.setValue(value);
                    resortList();
                    // Activity wieder anzeigen
                    activityBase.show();
                }

                @Override
                public void cancelClicked() {
                    // Activity wieder anzeigen
                    activityBase.show();
                }
            });
            return true;
        });

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return false;
        });

        return item;

    }

    private CB_View_Base getFloatView(final SettingFloat SB, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
        final String trans = Translation.get(SB.getName());
        item.setName(trans);
        item.setDefault(SB.getValue() + "\n\n" + Translation.get("Desc_" + SB.getName()));

        item.setClickHandler((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(SB);

            // Show NumPad Int Edit
            NumericInputBox.Show("default: " + br + SB.getDefaultValue(), trans, SB.getValue(), new IReturnValueListenerDouble() {
                @Override
                public void returnValue(double value) {
                    SettingFloat SetValue = (SettingFloat) Config.settings.get(EditKey);
                    if (SetValue != null)
                        SetValue.setValue((float) value);
                    resortList();
                    // Activity wieder anzeigen
                    activityBase.show();
                }

                @Override
                public void cancelClicked() {
                    // Activity wieder anzeigen
                    activityBase.show();
                }
            });
            return true;
        });

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return false;
        });

        return item;

    }

    private CB_View_Base getFolderView(final SettingFolder settingFolder, int backgroundChanger) {

        final boolean needWritePermission = settingFolder.needWritePermission();
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, settingFolder.getName());

        item.setName(Translation.get(settingFolder.getName()));
        if (settingFolder.isDefault()) {
            item.setDefault(Translation.get("default") + "\n\n" + Translation.get("Desc_" + settingFolder.getName()));
        } else {
            item.setDefault(settingFolder.getValue() + "\n\n" + Translation.get("Desc_" + settingFolder.getName()));
        }

        item.setClickHandler((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(settingFolder);
            AbstractFile abstractFile = FileFactory.createFile(settingFolder.getValue());
            final String absolutePath = (abstractFile != null) ? abstractFile.getAbsolutePath() : "";
            Menu icm = new Menu("SelectPathTitle");
            icm.addMenuItem("select_folder", null,
                    () -> new FileOrFolderPicker(absolutePath, Translation.get("select_folder"), Translation.get("select"), abstractFile1 -> {
                        // check WriteProtection
                        if (needWritePermission && !abstractFile1.canWrite()) {
                            String WriteProtectionMsg = Translation.get("NoWriteAcces");
                            GL.that.toast(WriteProtectionMsg);
                        } else {
                            settingFolder.setValue(abstractFile1.getAbsolutePath());
                            resortList();
                        }
                    }).show());
            icm.addMenuItem("ClearPath", null, () -> {
                settingFolder.setValue(settingFolder.getDefaultValue());
                resortList();
            });
            icm.show();
            return true;
        });

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            MessageBox.show(Translation.get("Desc_" + settingFolder.getName()), msgBoxReturnListener);
            return false;
        });

        return item;

    }

    private CB_View_Base getFileView(final SettingFile settingFile, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, settingFile.getName());

        item.setName(Translation.get(settingFile.getName()));
        item.setDefault(settingFile.getValue() + "\n\n" + Translation.get("Desc_" + settingFile.getName()));

        item.setClickHandler((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(settingFile);
            AbstractFile abstractFile = FileFactory.createFile(settingFile.getValue());

            final String Path = (abstractFile.getParent() != null) ? abstractFile.getParent() : "";

            Menu icm = new Menu("SelectFileTitle");

            icm.addMenuItem("select_file", null,
                    () -> new FileOrFolderPicker(Path, settingFile.getExt(), Translation.get("select_file"), Translation.get("select"), abstractFile1 -> {
                        settingFile.setValue(abstractFile1.getAbsolutePath());
                        resortList();
                    }).show());
            icm.addMenuItem("ClearPath", null, () -> {
                settingFile.setValue(settingFile.getDefaultValue());
                resortList();
            });
            icm.show();
            return true;
        });

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            MessageBox.show(Translation.get("Desc_" + settingFile.getName()), msgBoxReturnListener);
            return false;
        });

        return item;

    }

    private CB_View_Base getButtonView(final SettingsListCategoryButton<?> SB) {
        CB_Button btn = new CB_Button(ButtonRec, "Button");

        btn.setDraggable();

        btn.setText(Translation.get(SB.getName()));

        if (SB.getName().equals("DebugDisplayInfo")) {
            btn.setClickHandler((v, x, y, pointer, button) -> {

                if (SB.getName().equals("DebugDisplayInfo")) {
                    String info = "";

                    info += "Density= " + GL_UISizes.dpi + br;
                    info += "Height= " + UiSizes.getInstance().getWindowHeight() + br;
                    info += "Width= " + UiSizes.getInstance().getWindowWidth() + br;
                    info += "Scale= " + UiSizes.getInstance().getScale() + br;
                    info += "FontSize= " + UiSizes.getInstance().getScaledFontSize() + br;
                    info += "GPS min pos Time= " + PositionChangedListeners.minPosEventTime + br;
                    info += "GPS min Orientation Time= " + PositionChangedListeners.minOrientationEventTime + br;

                    MessageBox.show(info, msgBoxReturnListener);

                    return true;
                }

                return false;
            });
        }

        btn.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return false;
        });

        return btn;
    }

    private CB_View_Base getApiKeyButtonView() {
        apiBtn = new API_Button(itemRec);
        apiBtn.setImage();
        return apiBtn;
    }

    private CB_View_Base getAudioView(final SettingsAudio settingsAudio, int backgroundChanger) {

        final String audioName = settingsAudio.getName();
        final SettingsItem_Audio item = new SettingsItem_Audio(itemRec, backgroundChanger, audioName, value -> {
            Audio aud = new Audio(settingsAudio.getValue());
            aud.Volume = value / 100f;
            settingsAudio.setValue(aud);

            // play Audio now

            if (audioName.equalsIgnoreCase("GlobalVolume"))
                SoundCache.play(Sounds.Global, true);
            if (audioName.equalsIgnoreCase("Approach"))
                SoundCache.play(Sounds.Approach);
            if (audioName.equalsIgnoreCase("GPS_lose"))
                SoundCache.play(Sounds.GPS_lose);
            if (audioName.equalsIgnoreCase("GPS_fix"))
                SoundCache.play(Sounds.GPS_fix);
            if (audioName.equalsIgnoreCase("AutoResortSound"))
                SoundCache.play(Sounds.AutoResortSound);
        });

        item.setName(Translation.get(settingsAudio.getName()));
        item.setDefault("default: " + settingsAudio.getDefaultValue() + "\n\n" + Translation.get("Desc_" + settingsAudio.getName()));
        item.setVolume((int) (settingsAudio.getValue().Volume * 100));
        CB_CheckBox chk = item.getCheckBox();

        if (!audioName.contains("Global")) {
            if (audioSettingsList == null)
                audioSettingsList = new ArrayList<>();
            audioSettingsList.add(item);
        }

        chk.setChecked(settingsAudio.getValue().Mute);
        chk.setOnCheckChangedListener((view, isChecked) -> {
            Audio aud = new Audio(settingsAudio.getValue());
            aud.Mute = isChecked;
            settingsAudio.setValue(aud);
            item.setMuteDisabeld(isChecked);
            if (audioName.contains("Global")) {
                // Enable or disable all other
                setVolumeState(isChecked);
            }
        });

        item.setMuteDisabeld(chk.isChecked());

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + settingsAudio.getName()), msgBoxReturnListener);

            return true;
        });

        return item;

    }

    private void setVolumeState(boolean globalEnabled) {
        if (audioSettingsList != null) {
            for (SettingsItem_Audio it : audioSettingsList) {
                if (globalEnabled) {
                    it.disable();
                } else {
                    it.enable();
                }
            }
        }
    }

    private CB_View_Base getTimeView(final SettingTime SB, int backgroundChanger) {

        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
        final String trans = Translation.get(SB.getName());
        item.setName(trans);
        item.setDefault(intToTime(SB.getValue()) + "\n\n" + Translation.get("Desc_" + SB.getName()));

        item.setClickHandler((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(SB);

            String Value = intToTime(SB.getValue());
            String[] s = Value.split(":");

            int intValueMin = Integer.parseInt(s[0]);
            int intValueSec = Integer.parseInt(s[1]);

            // Show NumPad Int Edit
            NumericInputBox.Show("default: " + br + intToTime(SB.getDefaultValue()), trans, intValueMin, intValueSec, new IReturnValueListenerTime() {
                @Override
                public void returnValue(int min, int sec) {
                    SettingTime SetValue = (SettingTime) Config.settings.get(EditKey);
                    int value = (min * 60 * 1000) + (sec * 1000);
                    if (SetValue != null)
                        SetValue.setValue(value);
                    resortList();
                    // Activity wieder anzeigen
                    activityBase.show();
                }

                @Override
                public void cancelClicked() {
                    // Activity wieder anzeigen
                    activityBase.show();
                }
            });
            return true;
        });

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return false;
        });

        return item;

    }

    private String intToTime(int milliseconds) {
        int seconds = milliseconds / 1000 % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        // int hours = (int) ((milliseconds / (1000*60*60)) % 24);

        return minutes + ":" + seconds;
    }

    private CB_View_Base getLangSpinnerView() {
        Sprachen = Translation.that.getLangs(Config.languagePath.getValue());

        if (Sprachen == null || Sprachen.size() == 0)
            return null;

        final String[] items = new String[Sprachen.size()];
        int index = 0;
        int selection = -1;

        AbstractFile abstractFile1 = FileFactory.createFile(Config.Sel_LanguagePath.getValue());

        for (Lang tmp : Sprachen) {
            AbstractFile abstractFile2 = FileFactory.createFile(tmp.Path);
            if (abstractFile1.getAbsoluteFile().compareTo(abstractFile2.getAbsoluteFile()) == 0) {
                selection = index;
            }

            items[index++] = tmp.Name;
        }

        Spinner spinner = new Spinner(ButtonRec,
                "SelectLanguage",
                new SpinnerAdapter() {
                    @Override
                    public String getText(int position) {
                        return items[position];
                    }

                    @Override
                    public Drawable getIcon(int Position) {
                        return null;
                    }

                    @Override
                    public int getCount() {
                        return items.length;
                    }
                },
                index1 -> {
                    String selected = items[index1];
                    for (Lang tmp : Sprachen) {
                        if (selected.equals(tmp.Name)) {
                            Config.Sel_LanguagePath.setValue(tmp.Path);
                            try {
                                Translation.that.loadTranslation(tmp.Path);
                            } catch (Exception e) {
                                Translation.that.loadTranslation(Config.Sel_LanguagePath.getDefaultValue());
                            }
                            break;
                        }
                    }
                });
        spinner.setSelection(selection);
        // spinner.setPrompt(Translation.get("SelectLanguage")); since 17.5.2019 within constructor
        spinner.setDraggable();
        return spinner;
    }

    private CB_View_Base getSkinSpinnerView() {
        String SkinFolder = Config.workPath + "/skins";
        AbstractFile dir = FileFactory.createFile(SkinFolder);
        final ArrayList<String> skinFolders = new ArrayList<>();
        dir.listFiles((f, name) -> {
            AbstractFile found = FileFactory.createFile(f, name);
            if (found.isDirectory()) {
                String Path = f.getAbsolutePath();
                if (!Path.contains(".svn")) {
                    skinFolders.add(name);
                }
            }
            return false;
        });

        final String[] items = new String[skinFolders.size() + 2];// + internal (default and small)
        items[0] = "default";
        items[1] = "small";
        int index = 2;
        int selection = -1;
        if (Config.skinFolder.getValue().equals("default"))
            selection = 0;
        if (Config.skinFolder.getValue().equals("small"))
            selection = 1;
        for (String tmp : skinFolders) {
            if (Config.skinFolder.getValue().endsWith(tmp))
                selection = index;
            items[index++] = tmp;
        }

        SpinnerAdapter adapter = new SpinnerAdapter() {
            @Override
            public String getText(int position) {
                return items[position];
            }

            @Override
            public Drawable getIcon(int Position) {
                return null;
            }

            @Override
            public int getCount() {
                return items.length;
            }
        };

        final Spinner spinner = new Spinner(itemRec, "SelectSkin", adapter, index1 -> {
            String selected = items[index1];
            if (selected.equals("default")) {
                Config.skinFolder.setValue("default");
            } else if (selected.equals("small")) {
                Config.skinFolder.setValue("small");
            } else {
                Config.skinFolder.setValue(Config_Core.workPath + "/skins/" + selected);
            }
        });

        spinner.setSelection(selection);

        // spinner.setPrompt(Translation.get("SelectSkin"));

        spinner.setDraggable();

        return spinner;
    }

    private CB_View_Base getBoolView(final SettingBool SB, int backgroundChanger) {

        SettingsItem_Bool item = new SettingsItem_Bool(itemRec, backgroundChanger, SB.getName());

        item.setName(Translation.get(SB.getName()));
        item.setDefault("default: " + SB.getDefaultValue() + "\n\n" + Translation.get("Desc_" + SB.getName()));

        CB_CheckBox chk = item.getCheckBox();

        chk.setChecked(SB.getValue());
        chk.setOnCheckChangedListener((view, isChecked) -> {
            SB.setValue(isChecked);
            if (SB.getName().equalsIgnoreCase("DraftsLoadAll")) {
                resortList();
            }
        });

        item.setOnLongClickListener((v, x, y, pointer, button) -> {
            // zeige Beschreibung der Einstellung

            MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

            return true;
        });

        return item;

    }

    @Override
    public void SelectedLangChangedEventCalled() {
        initial();
    }

    @Override
    public void dispose() {
        that = null;

        if (Categorys != null)
            Categorys.clear();
        Categorys = null;

        if (btnOk != null)
            btnOk.dispose();
        btnOk = null;
        if (btnCancel != null)
            btnCancel.dispose();
        btnCancel = null;
        if (btnMenu != null)
            btnMenu.dispose();
        btnMenu = null;
        if (scrollBox != null)
            scrollBox.dispose();
        scrollBox = null;
        if (ButtonRec != null)
            ButtonRec.dispose();
        ButtonRec = null;
        if (itemRec != null)
            itemRec.dispose();
        itemRec = null;
        if (apiBtn != null)
            apiBtn.dispose();
        apiBtn = null;
        if (LinearLayout != null)
            LinearLayout.dispose();
        LinearLayout = null;

        SelectedLangChangedEventList.Remove(this);

        super.dispose();
    }

    static class SettingsOrder implements Comparator<SettingBase> {
        public int compare(SettingBase a, SettingBase b) {
            return a.compareTo(b);
        }
    }
}
