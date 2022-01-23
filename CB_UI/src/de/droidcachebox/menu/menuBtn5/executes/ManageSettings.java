package de.droidcachebox.menu.menuBtn5.executes;

import static de.droidcachebox.settings.Config_Core.br;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.SoundCache;
import de.droidcachebox.SoundCache.Sounds;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.QuickButtonList;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.activities.ColorPicker;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.LinearCollapseBox;
import de.droidcachebox.gdx.controls.Linearlayout;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.controls.Spinner;
import de.droidcachebox.gdx.controls.SpinnerAdapter;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox.IReturnValueListener;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox.IReturnValueListenerDouble;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox.IReturnValueListenerTime;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.menu.QuickButtonItem;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.menu.menuBtn3.executes.MapView;
import de.droidcachebox.menu.menuBtn5.ShowSettings;
import de.droidcachebox.settings.API_Button;
import de.droidcachebox.settings.Audio;
import de.droidcachebox.settings.SettingBase;
import de.droidcachebox.settings.SettingBool;
import de.droidcachebox.settings.SettingCategory;
import de.droidcachebox.settings.SettingColor;
import de.droidcachebox.settings.SettingDouble;
import de.droidcachebox.settings.SettingEnum;
import de.droidcachebox.settings.SettingFile;
import de.droidcachebox.settings.SettingFloat;
import de.droidcachebox.settings.SettingFolder;
import de.droidcachebox.settings.SettingInt;
import de.droidcachebox.settings.SettingIntArray;
import de.droidcachebox.settings.SettingLongString;
import de.droidcachebox.settings.SettingModus;
import de.droidcachebox.settings.SettingString;
import de.droidcachebox.settings.SettingStringArray;
import de.droidcachebox.settings.SettingTime;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.settings.SettingsAudio;
import de.droidcachebox.settings.SettingsItemBase;
import de.droidcachebox.settings.SettingsItemEnum;
import de.droidcachebox.settings.SettingsItem_Audio;
import de.droidcachebox.settings.SettingsItem_Bool;
import de.droidcachebox.settings.SettingsItem_Color;
import de.droidcachebox.settings.SettingsItem_QuickButton;
import de.droidcachebox.settings.SettingsListButtonLangSpinner;
import de.droidcachebox.settings.SettingsListButtonSkinSpinner;
import de.droidcachebox.settings.SettingsListCategoryButton;
import de.droidcachebox.settings.SettingsListGetApiButton;
import de.droidcachebox.translation.Lang;
import de.droidcachebox.translation.LanguageChanged;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.FileFactory;

public class ManageSettings extends ActivityBase implements LanguageChanged.event {

    private final OnClickListener showDescription;
    public LinearCollapseBox loginHead; // hack for show after getting Api-key
    private int editKey;
    private ArrayList<SettingsItem_Audio> audioSettingsList;
    private ArrayList<Lang> languages;
    private CB_List<SettingCategory> settingCategories;
    private CB_Button btnOk, btnCancel, btnMenu;
    private ScrollBox scrollBox;
    private CB_RectF buttonRec, itemRec;
    private API_Button apiBtn;
    private Linearlayout linearLayout;
    private SettingsItem_QuickButton settingsItem_QuickButton;

    public ManageSettings() {
        super("ManageSettings");
        initialize();
        LanguageChanged.add(this);
        showDescription = (v, x, y, pointer, button) -> {
            ButtonDialog bd = new ButtonDialog(Translation.get("Desc_" + v.getData()), "", MsgBoxButton.OK, MsgBoxIcon.None);
            bd.setButtonClickHandler((which, data) -> {
                show();
                return true;
            });
            bd.show();
            return true;
        };
        editKey = -1;
        settingCategories = new CB_List<>();
    }

    public void onHide() {
        ShowSettings.getInstance().onHide();
    }

    public void resortList() {
        float scrollPos;
        scrollPos = scrollBox == null ? 0 : scrollBox.getScrollY();
        scrollBox = null;
        linearLayout = null;
        fillContent();
        scrollBox.scrollTo(scrollPos);
    }

    private void initialize() {
        setLongClickable(true);
        Settings.getInstance().saveToLastValues();
        buttonRec = new CB_RectF(leftBorder, 0, innerWidth, UiSizes.getInstance().getButtonHeight());
        itemRec = new CB_RectF(leftBorder, 0, buttonRec.getWidth() - leftBorder - rightBorder, UiSizes.getInstance().getButtonHeight());
        createButtons();
        resortList();
    }

    @Override
    protected void skinIsChanged() {
        super.skinIsChanged();
        removeChild(btnOk);
        removeChild(btnCancel);
        removeChild(btnMenu);
        createButtons();
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

            if (Settings.isDeveloper.getValue())
                Settings.isExpert.setValue(false);

            icm.addCheckableMenuItem("Settings_Expert", Settings.isExpert.getValue(), () -> {
                Settings.isExpert.setValue(!Settings.isExpert.getValue());
                Settings.isDeveloper.setValue(false);
                resortList();
            });

            icm.addCheckableMenuItem("Settings_All", Settings.isDeveloper.getValue(), () -> {
                Settings.isDeveloper.setValue(!Settings.isDeveloper.getValue());
                Settings.isExpert.setValue(false);
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
            for (int i = 0, n = settingsItem_QuickButton.getTmpQuickList().size(); i < n; i++) {
                QuickButtonItem tmp = settingsItem_QuickButton.getTmpQuickList().get(i);
                ActionsString.append(tmp.getQuickAction().ordinal());
                if (counter < settingsItem_QuickButton.getTmpQuickList().size() - 1) {
                    ActionsString.append(",");
                }
                counter++;
            }
            Settings.quickButtonList.setValue(ActionsString.toString());

            Settings.getInstance().saveToLastValues();
            Settings.getInstance().acceptChanges();

            // Notify QuickButtonList
            QuickButtonList.that.notifyDataSetChanged();

            ShowMap.getInstance().normalMapView.setNewSettings(MapView.INITIAL_NEW_SETTINGS);

            finish();
            return true;
        });

        addChild(btnCancel);
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            Settings.getInstance().loadFromLastValues();
            finish();
            return true;
        });

    }

    private void fillContent() {

        if (settingCategories == null) {
            settingCategories = new CB_List<>();
        }
        settingCategories.clear();
        SettingCategory[] tmp = SettingCategory.values();
        for (SettingCategory item : tmp) {
            settingCategories.add(item);
        }

        CB_View_Base langView = getLangSpinnerView();

        addControlToLinearLayout(langView, margin); // recreates scrollBox

        ArrayList<SettingBase<?>> AllSettingList = new ArrayList<>();// Config.settings.values().toArray();
        for (SettingBase<?> settingItem : Settings.getInstance()) {
            // add item to list depending on SettingMode
            if (((settingItem.getModus() == SettingModus.NORMAL) || (settingItem.getModus() == SettingModus.EXPERT && Settings.isExpert.getValue()) || Settings.isDeveloper.getValue())
                    && (settingItem.getModus() != SettingModus.NEVER)) {
                AllSettingList.add(settingItem);
            }
        }

        for (SettingCategory settingCategory : settingCategories) {
            ArrayList<SettingBase<?>> categoryList = new ArrayList<>();
            for (SettingBase<?> settingItem : AllSettingList) {
                if (settingItem.getCategory().name().equals(settingCategory.name())) {
                    categoryList.add(settingItem);
                }
            }
            Collections.sort(categoryList, new SettingsOrder());

            int position = 0;

            SettingsListCategoryButton<?> catBtn = new SettingsListCategoryButton<>(settingCategory.name());

            final CB_View_Base btn = getView(catBtn, 1);

            // add Cat entries
            final LinearCollapseBox linearCollapseBox = new LinearCollapseBox(btn, "");
            linearCollapseBox.setClickable(true);
            linearCollapseBox.setAnimationListener(Height -> {
                linearLayout.layout();
                linearLayout.setZeroPos();
                scrollBox.setVirtualHeight(linearLayout.getHeight());
            });

            int entryCount = 0;

            switch (settingCategory) {
                case Login:
                    SettingsListGetApiButton<?> lgIn = new SettingsListGetApiButton<>(settingCategory.name());
                    linearCollapseBox.addChild(getView(lgIn, 1));
                    entryCount++;
                    loginHead = linearCollapseBox;
                    break;
                case QuickList:
                    linearCollapseBox.addChild(settingsItem_QuickButton = new SettingsItem_QuickButton(itemRec, "QuickButtonEditor"));
                    entryCount++;
                    break;
                case Skin:
                    // SettingsListButtonSkinSpinner<?> skin = new SettingsListButtonSkinSpinner<>("Skin");
                    CB_View_Base skinView = getSkinSpinnerView();
                    linearCollapseBox.addChild(skinView);
                    entryCount++;
                    break;
                case Sounds:
                    if (categoryList.size() > 0) {
                        // top line for sound settings
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

                        linearCollapseBox.addChild(lblBox);
                        entryCount++;
                    }
                    break;
            }

            boolean expandLayout = false;

            for (SettingBase<?> settingItem : categoryList) {
                final CB_View_Base view = getView(settingItem, position++);
                if (view != null) {

                    if (Settings.DraftsLoadAll.getValue() && settingItem.getName().equalsIgnoreCase("DraftsLoadLength")) {
                        ((SettingsItemBase) view).disable();
                    }

                    if (view instanceof CB_Button) {
                        view.setSize(itemRec);
                    }

                    linearCollapseBox.addChild(view);
                    entryCount++;
                    Settings.getInstance().indexOf(settingItem);
                    if (Settings.getInstance().indexOf(settingItem) == editKey) {
                        expandLayout = true;
                    }
                }

            }

            if (entryCount > 0) {

                linearCollapseBox.setBackground(getBackground());// Activity Background
                if (!expandLayout)
                    linearCollapseBox.setAnimationHeight(0f);

                addControlToLinearLayout(btn, margin);
                addControlToLinearLayout(linearCollapseBox, -(drawableBackground.getBottomHeight()) / 2);

                if (btn != null)
                    btn.setClickHandler((v, x, y, pointer, button) -> {
                        linearCollapseBox.toggle();
                        return true;
                    });
            }
        }

        setVolumeState(Settings.globalVolume.getValue().Mute);
        apiBtn.setImage();

    }

    private void addControlToLinearLayout(CB_View_Base view, float itemMargin) {
        if (linearLayout == null || scrollBox == null) {

            CB_RectF rec = new CB_RectF(0, btnOk.getMaxY() + margin, getWidth(), getHeight() - btnOk.getMaxY() - margin);

            scrollBox = new ScrollBox(rec);
            scrollBox.setClickable(true);
            scrollBox.setLongClickable(true);
            linearLayout = new Linearlayout(buttonRec.getWidth(), "SettingsActivity-LinearLayout");
            linearLayout.setClickable(true);
            linearLayout.setLongClickable(true);
            linearLayout.setZeroPos();
            scrollBox.addChild(linearLayout);
            // LinearLayout.setBackground(new ColorDrawable(Color.RED));
            scrollBox.setBackground(getBackground());
            addChild(scrollBox);
        }

        view.setZeroPos();
        view.setClickable(true);
        view.setLongClickable(true);

        linearLayout.addChild(view, itemMargin);
        linearLayout.setZeroPos();
        scrollBox.setVirtualHeight(linearLayout.getHeight());

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

    private CB_View_Base getColorView(final SettingColor sb, int backgroundChanger) {
        SettingsItemBase item = new SettingsItem_Color(itemRec, backgroundChanger, sb);
        item.setName(Translation.get(sb.getName()));
        item.setDefault(sb.getValue() + "\n\n" + Translation.get("Desc_" + sb.getName()));
        item.setClickHandler((v, x, y, pointer, button) -> {
            editKey = Settings.getInstance().indexOf(sb);

            GL.that.runOnGLWithThreadCheck(() -> {
                ColorPicker clrPick = new ColorPicker(sb.getValue(), color -> {
                    if (color == null)
                        return; // nothing changed

                    SettingColor SetValue = (SettingColor) Settings.getInstance().get(editKey);
                    if (SetValue != null)
                        SetValue.setValue(color);
                    resortList();
                    // reshow Activity
                    show();
                });
                clrPick.show();
            });

            return true;
        });
        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getStringView(final SettingString sb, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, sb.getName());
        item.setName(Translation.get(sb.getName()));
        String toShow;
        if (sb.isDefault()) {
            toShow = Translation.get("default");
        } else {
            toShow = sb.getValue();
        }
        item.setDefault(toShow + "\n\n" + Translation.get("Desc_" + sb.getName()));
        item.setClickHandler((v, x, y, pointer, button) -> {
            editKey = Settings.getInstance().indexOf(sb);
            WrapType wrapType;

            wrapType = (sb instanceof SettingLongString) ? WrapType.WRAPPED : WrapType.SINGLELINE;

            StringInputBox stringInputBox = new StringInputBox("default:" + br + sb.getDefaultValue(), Translation.get(sb.getName()), sb.getValue(), wrapType);
            stringInputBox.setButtonClickHandler(
                    (which, data) -> {
                        String text = StringInputBox.editTextField.getText();
                        if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                            SettingString value = (SettingString) Settings.getInstance().get(editKey);
                            // api without lineBreak
                            if (value.getName().equalsIgnoreCase("AccessToken")) {
                                text = text.replace("\r", "");
                                text = text.replace("\n", "");
                            }
                            value.setValue(text);
                            resortList();
                        }
                        // reshow Activity
                        activityBase.show();
                        return true;
                    });
            stringInputBox.showAtTop();

            return true;
        });
        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getEnumView(final SettingEnum<?> sb, int backgroundChanger) {
        SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, sb.getName());
        item.setName(Translation.get(sb.getName()));
        item.setDefault("\n" + Translation.get("Desc_" + sb.getName()) + "\n");

        final Spinner spinner = item.getSpinner();
        spinner.setDraggable();
        final SpinnerAdapter adapter = new SpinnerAdapter() {

            @Override
            public String getText(int position) {
                return Translation.get(sb.getValues().get(position));
            }

            @Override
            public Drawable getIcon(int Position) {
                return null;
            }

            @Override
            public int getCount() {
                return sb.getValues().size();
            }
        };

        spinner.setAdapter(adapter);
        spinner.setSelection(sb.getValues().indexOf(sb.getValue()));
        spinner.setSelectionChangedListener(index -> sb.setValue(sb.getValues().get(index)));

        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getIntArrayView(final SettingIntArray sb, int backgroundChanger) {
        SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, sb.getName());
        item.setName(Translation.get(sb.getName()));
        item.setDefault("\n" + Translation.get("Desc_" + sb.getName()) + "\n");

        final Spinner spinner = item.getSpinner();
        spinner.setDraggable();
        final SpinnerAdapter adapter = new SpinnerAdapter() {
            @Override
            public String getText(int position) {
                return String.valueOf(sb.getValues()[position]);
            }

            @Override
            public Drawable getIcon(int Position) {
                return null;
            }

            @Override
            public int getCount() {
                return sb.getValues().length;
            }
        };
        spinner.setAdapter(adapter);
        spinner.setSelection(sb.getIndex());
        spinner.setSelectionChangedListener(index -> sb.setValue(sb.getValueFromIndex(index)));

        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());

        return item;

    }

    private CB_View_Base getStringArrayView(final SettingStringArray sb, int backgroundChanger) {
        SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, sb.getName());
        item.setName(Translation.get(sb.getName()));
        item.setDefault("\n" + Translation.get("Desc_" + sb.getName()) + "\n");

        final Spinner spinner = item.getSpinner();
        spinner.setDraggable();
        final SpinnerAdapter adapter = new SpinnerAdapter() {

            @Override
            public String getText(int position) {
                return sb.possibleValues()[position];
            }

            @Override
            public Drawable getIcon(int Position) {
                return null;
            }

            @Override
            public int getCount() {
                return sb.possibleValues().length;
            }
        };
        spinner.setAdapter(adapter);
        spinner.setSelection(sb.getIndexOfValue());
        spinner.setSelectionChangedListener(index -> sb.setValue(sb.getValueFromIndex(index)));

        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getIntView(final SettingInt sb, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, sb.getName());
        final String trans = Translation.get(sb.getName());
        item.setName(trans);
        item.setDefault(sb.getValue() + "\n\n" + Translation.get("Desc_" + sb.getName()));
        item.setClickHandler((v, x, y, pointer, button) -> {
            editKey = Settings.getInstance().indexOf(sb);
            // Show NumPad Int Edit
            NumericInputBox numericInputBox = new NumericInputBox("default: " + br + sb.getDefaultValue(), trans);
            numericInputBox.initIntInput(sb.getValue(), new IReturnValueListener() {
                @Override
                public void returnValue(int value) {
                    SettingInt SetValue = (SettingInt) Settings.getInstance().get(editKey);
                    if (SetValue != null)
                        SetValue.setValue(value);
                    resortList();
                    // reshow Activity
                    show();
                }

                @Override
                public void cancelClicked() {
                    // reshow Activity
                    show();
                }

            });
            numericInputBox.show();
            return true;
        });

        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getDblView(final SettingDouble sb, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, sb.getName());
        final String trans = Translation.get(sb.getName());
        item.setName(trans);
        item.setDefault(sb.getValue() + "\n\n" + Translation.get("Desc_" + sb.getName()));
        item.setClickHandler((v, x, y, pointer, button) -> {
            editKey = Settings.getInstance().indexOf(sb);
            NumericInputBox numericInputBox = new NumericInputBox("default: " + br + sb.getDefaultValue(), trans);
            numericInputBox.initDoubleInput(String.valueOf(sb.getValue()), new IReturnValueListenerDouble() {
                @Override
                public void returnValue(double value) {
                    SettingDouble SetValue = (SettingDouble) Settings.getInstance().get(editKey);
                    if (SetValue != null)
                        SetValue.setValue(value);
                    resortList();
                    // reshow Activity
                    activityBase.show();
                }

                @Override
                public void cancelClicked() {
                    // reshow Activity
                    activityBase.show();
                }
            });
            numericInputBox.show();
            return true;
        });

        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getFloatView(final SettingFloat sb, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, sb.getName());
        final String trans = Translation.get(sb.getName());
        item.setName(trans);
        item.setDefault(sb.getValue() + "\n\n" + Translation.get("Desc_" + sb.getName()));
        item.setClickHandler((v, x, y, pointer, button) -> {
            editKey = Settings.getInstance().indexOf(sb);
            NumericInputBox numericInputBox = new NumericInputBox("default: " + br + sb.getDefaultValue(), trans);
            numericInputBox.initDoubleInput(String.valueOf(sb.getValue()), new IReturnValueListenerDouble() {
                @Override
                public void returnValue(double value) {
                    SettingFloat SetValue = (SettingFloat) Settings.getInstance().get(editKey);
                    if (SetValue != null)
                        SetValue.setValue((float) value);
                    resortList();
                    // reshow Activity
                    activityBase.show();
                }

                @Override
                public void cancelClicked() {
                    // reshow Activity
                    activityBase.show();
                }
            });
            numericInputBox.show();
            return true;
        });

        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getFolderView(final SettingFolder sb, int backgroundChanger) {
        final boolean needWritePermission = sb.needWritePermission();
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, sb.getName());
        item.setName(Translation.get(sb.getName()));
        if (sb.isDefault()) {
            item.setDefault(Translation.get("default") + "\n\n" + Translation.get("Desc_" + sb.getName()));
        } else {
            item.setDefault(sb.getValue() + "\n\n" + Translation.get("Desc_" + sb.getName()));
        }
        item.setClickHandler((v, x, y, pointer, button) -> {
            editKey = Settings.getInstance().indexOf(sb);
            AbstractFile abstractFile = FileFactory.createFile(sb.getValue());
            final String absolutePath = (abstractFile != null) ? abstractFile.getAbsolutePath() : "";
            Menu icm = new Menu("SelectPathTitle");
            icm.addMenuItem("select_folder", null,
                    () -> new FileOrFolderPicker(absolutePath, Translation.get("select_folder"), Translation.get("select"), abstractFile1 -> {
                        // check WriteProtection
                        if (needWritePermission && !abstractFile1.canWrite()) {
                            String WriteProtectionMsg = Translation.get("NoWriteAcces");
                            GL.that.toast(WriteProtectionMsg);
                        } else {
                            sb.setValue(abstractFile1.getAbsolutePath());
                            resortList();
                        }
                    }).show());
            icm.addMenuItem("ClearPath", null, () -> {
                sb.setValue(sb.getDefaultValue());
                resortList();
            });
            icm.show();
            return true;
        });
        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getFileView(final SettingFile sb, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, sb.getName());
        item.setName(Translation.get(sb.getName()));
        item.setDefault(sb.getValue() + "\n\n" + Translation.get("Desc_" + sb.getName()));
        item.setClickHandler((v, x, y, pointer, button) -> {
            editKey = Settings.getInstance().indexOf(sb);
            AbstractFile abstractFile = FileFactory.createFile(sb.getValue());
            final String Path = (abstractFile.getParent() != null) ? abstractFile.getParent() : "";
            Menu icm = new Menu("SelectFileTitle");
            icm.addMenuItem("select_file", null,
                    () -> new FileOrFolderPicker(Path, sb.getExt(), Translation.get("select_file"), Translation.get("select"), abstractFile1 -> {
                        sb.setValue(abstractFile1.getAbsolutePath());
                        resortList();
                    }).show());
            icm.addMenuItem("ClearPath", null, () -> {
                sb.setValue(sb.getDefaultValue());
                resortList();
            });
            icm.show();
            return true;
        });
        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getButtonView(final SettingsListCategoryButton<?> sb) {
        CB_Button item = new CB_Button(buttonRec, "Button");
        item.setDraggable();
        item.setText(Translation.get(sb.getName()));
        if (sb.getName().equals("DebugDisplayInfo")) {
            item.setClickHandler((v, x, y, pointer, button) -> {
                if (sb.getName().equals("DebugDisplayInfo")) {
                    String info = "";
                    info += "Density= " + GL_UISizes.dpi + br;
                    info += "Height= " + UiSizes.getInstance().getWindowHeight() + br;
                    info += "Width= " + UiSizes.getInstance().getWindowWidth() + br;
                    info += "Scale= " + UiSizes.getInstance().getScale() + br;
                    info += "FontSize= " + UiSizes.getInstance().getScaledFontSize() + br;
                    info += "GPS min pos Time= " + PositionChangedListeners.minPosEventTime + br;
                    info += "GPS min Orientation Time= " + PositionChangedListeners.minOrientationEventTime + br;
                    ButtonDialog bd = new ButtonDialog(info, "", MsgBoxButton.OK, MsgBoxIcon.None);
                    bd.setButtonClickHandler((which, data) -> {
                        show();
                        return true;
                    });
                    bd.show();
                    return true;
                }
                return false;
            });
        }
        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private CB_View_Base getApiKeyButtonView() {
        apiBtn = new API_Button(itemRec);
        apiBtn.setImage();
        return apiBtn;
    }

    private CB_View_Base getAudioView(final SettingsAudio sb, int backgroundChanger) {
        final String audioName = sb.getName();
        final SettingsItem_Audio item = new SettingsItem_Audio(itemRec, backgroundChanger, audioName, value -> {
            Audio aud = new Audio(sb.getValue());
            aud.Volume = value / 100f;
            sb.setValue(aud);
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
        item.setName(Translation.get(sb.getName()));
        item.setDefault("default: " + sb.getDefaultValue() + "\n\n" + Translation.get("Desc_" + sb.getName()));
        item.setVolume((int) (sb.getValue().Volume * 100));
        CB_CheckBox chk = item.getCheckBox();
        if (!audioName.contains("Global")) {
            if (audioSettingsList == null)
                audioSettingsList = new ArrayList<>();
            audioSettingsList.add(item);
        }
        chk.setChecked(sb.getValue().Mute);
        chk.setOnCheckChangedListener((view, isChecked) -> {
            Audio aud = new Audio(sb.getValue());
            aud.Mute = isChecked;
            sb.setValue(aud);
            item.setMuteDisabeld(isChecked);
            if (audioName.contains("Global")) {
                // Enable or disable all other
                setVolumeState(isChecked);
            }
        });
        item.setMuteDisabeld(chk.isChecked());
        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
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

    private CB_View_Base getTimeView(final SettingTime sb, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, sb.getName());
        final String trans = Translation.get(sb.getName());
        item.setName(trans);
        item.setDefault(intToTime(sb.getValue()) + "\n\n" + Translation.get("Desc_" + sb.getName()));
        item.setClickHandler((v, x, y, pointer, button) -> {
            editKey = Settings.getInstance().indexOf(sb);
            String Value = intToTime(sb.getValue());
            String[] s = Value.split(":");
            int intValueMin = Integer.parseInt(s[0]);
            int intValueSec = Integer.parseInt(s[1]);
            NumericInputBox numericInputBox = new NumericInputBox("default: " + br + intToTime(sb.getDefaultValue()), trans);
            numericInputBox.initTimeInput(intValueMin, intValueSec, new IReturnValueListenerTime() {
                @Override
                public void returnValue(int min, int sec) {
                    SettingTime SetValue = (SettingTime) Settings.getInstance().get(editKey);
                    int value = (min * 60 * 1000) + (sec * 1000);
                    if (SetValue != null)
                        SetValue.setValue(value);
                    resortList();
                    // reshow Activity
                    activityBase.show();
                }

                @Override
                public void cancelClicked() {
                    // reshow Activity
                    activityBase.show();
                }
            });
            numericInputBox.show();
            return true;
        });
        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    private String intToTime(int milliseconds) {
        int seconds = milliseconds / 1000 % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        // int hours = (int) ((milliseconds / (1000*60*60)) % 24);
        return minutes + ":" + seconds;
    }

    private CB_View_Base getLangSpinnerView() {
        languages = Translation.that.getLangs(Settings.languagePath.getValue());
        if (languages == null || languages.size() == 0)
            return null;
        final String[] items = new String[languages.size()];
        int index = 0;
        int selection = -1;
        AbstractFile abstractFile1 = FileFactory.createFile(Settings.Sel_LanguagePath.getValue());
        for (Lang tmp : languages) {
            AbstractFile abstractFile2 = FileFactory.createFile(tmp.Path);
            if (abstractFile1.getAbsoluteFile().compareTo(abstractFile2.getAbsoluteFile()) == 0) {
                selection = index;
            }
            items[index++] = tmp.Name;
        }
        Spinner spinner = new Spinner(buttonRec,
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
                    for (Lang tmp : languages) {
                        if (selected.equals(tmp.Name)) {
                            Settings.Sel_LanguagePath.setValue(tmp.Path);
                            try {
                                Translation.that.loadTranslation(tmp.Path);
                            } catch (Exception e) {
                                Translation.that.loadTranslation(Settings.Sel_LanguagePath.getDefaultValue());
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
        String SkinFolder = GlobalCore.workPath + "/skins";
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
        if (Settings.skinFolder.getValue().equals("default"))
            selection = 0;
        if (Settings.skinFolder.getValue().equals("small"))
            selection = 1;
        for (String tmp : skinFolders) {
            if (Settings.skinFolder.getValue().endsWith(tmp))
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
                Settings.skinFolder.setValue("default");
            } else if (selected.equals("small")) {
                Settings.skinFolder.setValue("small");
            } else {
                Settings.skinFolder.setValue(GlobalCore.workPath + "/skins/" + selected);
            }
        });
        spinner.setSelection(selection);
        // spinner.setPrompt(Translation.get("SelectSkin"));
        spinner.setDraggable();
        return spinner;
    }

    private CB_View_Base getBoolView(final SettingBool sb, int backgroundChanger) {
        SettingsItem_Bool item = new SettingsItem_Bool(itemRec, backgroundChanger, sb.getName());
        item.setName(Translation.get(sb.getName()));
        item.setDefault("default: " + sb.getDefaultValue() + "\n\n" + Translation.get("Desc_" + sb.getName()));
        CB_CheckBox chk = item.getCheckBox();
        chk.setChecked(sb.getValue());
        chk.setOnCheckChangedListener((view, isChecked) -> {
            sb.setValue(isChecked);
            if (sb.getName().equalsIgnoreCase("DraftsLoadAll")) {
                resortList();
            }
        });
        item.setLongClickHandler(showDescription);
        item.setData(sb.getName());
        return item;
    }

    @Override
    public void changeLanguage() {
        initialize();
    }

    static class SettingsOrder implements Comparator<SettingBase<?>> {
        public int compare(SettingBase a, SettingBase b) {
            return a.compareTo(b);
        }
    }
}
