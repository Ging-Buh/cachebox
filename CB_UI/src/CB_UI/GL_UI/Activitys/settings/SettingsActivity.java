package CB_UI.GL_UI.Activitys.settings;

import CB_Locator.Events.PositionChangedEventList;
import CB_Translation_Base.TranslationEngine.Lang;
import CB_Translation_Base.TranslationEngine.SelectedLangChangedEvent;
import CB_Translation_Base.TranslationEngine.SelectedLangChangedEventList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Controls.API_Button;
import CB_UI.GL_UI.Controls.QuickButtonList;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_UI.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
import CB_UI.GL_UI.SoundCache;
import CB_UI.GL_UI.SoundCache.Sounds;
import CB_UI.GL_UI.Views.AdvancedSettingsView.SettingsListButtonLangSpinner;
import CB_UI.GL_UI.Views.AdvancedSettingsView.SettingsListButtonSkinSpinner;
import CB_UI.GL_UI.Views.AdvancedSettingsView.SettingsListCategoryButton;
import CB_UI.GL_UI.Views.AdvancedSettingsView.SettingsListGetApiButton;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GlobalCore;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Activitys.ColorPicker;
import CB_UI_Base.GL_UI.Activitys.ColorPicker.IReturnListener;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.CB_CheckBox.OnCheckChangedListener;
import CB_UI_Base.GL_UI.Controls.CB_Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.CollapseBox.IAnimatedHeightChangedListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumericInputBox;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumericInputBox.IReturnValueListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumericInputBox.IReturnValueListenerDouble;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumericInputBox.IReturnValueListenerTime;
import CB_UI_Base.GL_UI.Controls.Dialogs.StringInputBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.Spinner.ISelectionChangedListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Config_Core;
import CB_Utils.Lists.CB_List;
import CB_Utils.Settings.*;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.fileProvider.FilenameFilter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        super(ActivityBase.ActivityRec(), "Settings");
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
        this.setLongClickable(true);
        Config.settings.SaveToLastValue();
        ButtonRec = new CB_RectF(leftBorder, 0, innerWidth, UI_Size_Base.that.getButtonHeight());

        itemRec = new CB_RectF(leftBorder, 0, ButtonRec.getWidth() - leftBorder - rightBorder, UI_Size_Base.that.getButtonHeight());

        createButtons();
        fillContent();
        resortList();
    }

    @Override
    protected void SkinIsChanged() {
        super.SkinIsChanged();
        this.removeChild(btnOk);
        this.removeChild(btnCancel);
        this.removeChild(btnMenu);
        createButtons();
        fillContent();
        resortList();
    }

    private void createButtons() {
        float btnW = (innerWidth - UI_Size_Base.that.getButtonWidth()) / 2;

        btnOk = new CB_Button(leftBorder, this.getBottomHeight(), btnW, UI_Size_Base.that.getButtonHeight(), "OK Button");
        btnMenu = new CB_Button(btnOk.getMaxX(), this.getBottomHeight(), UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight(), "Menu Button");
        btnCancel = new CB_Button(btnMenu.getMaxX(), this.getBottomHeight(), btnW, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

        btnOk.setText(Translation.get("save"));
        btnCancel.setText(Translation.get("cancel"));

        this.addChild(btnMenu);
        btnMenu.setText("...");
        btnMenu.setOnClickListener((v, x, y, pointer, button) -> {
            Menu icm = new Menu("SettingsLevelTitle");

            if (Config.SettingsShowAll.getValue())
                Config.SettingsShowExpert.setValue(false);

            icm.addCheckableMenuItem("Settings_Expert", Config.SettingsShowExpert.getValue(), () -> {
                Config.SettingsShowExpert.setValue(!Config.SettingsShowExpert.getValue());
                Config.SettingsShowAll.setValue(false);
                resortList();
            });

            icm.addCheckableMenuItem("Settings_All", Config.SettingsShowAll.getValue(), () -> {
                Config.SettingsShowAll.setValue(!Config.SettingsShowAll.getValue());
                Config.SettingsShowExpert.setValue(false);
                resortList();
            });

            icm.setPrompt(Translation.get("SettingsLevelTitle"));

            icm.show();
            return true;
        });

        this.addChild(btnOk);
        btnOk.setOnClickListener((v, x, y, pointer, button) -> {

            StringBuilder ActionsString = new StringBuilder();
            int counter = 0;
            for (int i = 0, n = SettingsItem_QuickButton.tmpQuickList.size(); i < n; i++) {
                QuickButtonItem tmp = SettingsItem_QuickButton.tmpQuickList.get(i);
                ActionsString.append(tmp.getAction().ordinal());
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

            CB_Action_ShowMap.getInstance().normalMapView.setNewSettings(MapView.INITIAL_NEW_SETTINGS);

            finish();
            return true;
        });

        this.addChild(btnCancel);
        btnCancel.setOnClickListener((v, x, y, pointer, button) -> {
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

        SettingsListButtonLangSpinner<?> lang = new SettingsListButtonLangSpinner<>("Lang", SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);
        CB_View_Base langView = getLangSpinnerView(lang);

        addControlToLinearLayout(langView, margin);

        ArrayList<SettingBase<?>> AllSettingList = new ArrayList<>();// Config.settings.values().toArray();
        for (SettingBase settingItem : Config.settings) {
            if (settingItem.getUsage() == SettingUsage.ACB || settingItem.getUsage() == SettingUsage.ALL)
                // item nur zur Liste Hinzufügen, wenn der SettingModus dies auch zulässt.
                if (((settingItem.getModus() == SettingModus.NORMAL) || (settingItem.getModus() == SettingModus.EXPERT && Config.SettingsShowExpert.getValue()) || Config.SettingsShowAll.getValue())
                        && (settingItem.getModus() != SettingModus.NEVER)) {
                    AllSettingList.add(settingItem);
                }
        }

        for (SettingCategory cat : Categorys) {
            ArrayList<SettingBase<?>> CatList = new ArrayList<>();
            for (SettingBase settingItem : AllSettingList) {
                if (settingItem.getCategory().name().equals(cat.name())) {
                    CatList.add(settingItem);
                }
            }
            Collections.sort(CatList, new SettingsOrder());

            int position = 0;

            SettingsListCategoryButton<?> catBtn = new SettingsListCategoryButton<Object>(cat.name(), SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);

            final CB_View_Base btn = getView(catBtn, 1);

            // add Cat einträge
            final LinearCollapseBox lay = new LinearCollapseBox(btn, "");
            lay.setClickable(true);
            lay.setAnimationListener(new IAnimatedHeightChangedListener() {

                @Override
                public void animatedHeightChanged(float Height) {
                    LinearLayout.layout();

                    LinearLayout.setZeroPos();
                    scrollBox.setVirtualHeight(LinearLayout.getHeight());

                }
            });

            int entryCount = 0;

            switch (cat) {
                case Login:
                    SettingsListGetApiButton<?> lgIn = new SettingsListGetApiButton<Object>(cat.name(), SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);
                    lay.addChild(getView(lgIn, 1));
                    entryCount++;
                    break;
                case QuickList:
                    lay.addChild(new SettingsItem_QuickButton(itemRec, "QuickButtonEditor"));
                    entryCount++;
                    break;
                case Debug:
                    SettingsListCategoryButton<?> disp = new SettingsListCategoryButton<Object>("DebugDisplayInfo", SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);
                    final CB_View_Base btnDisp = getView(disp, 1);
                    btnDisp.setSize(itemRec);
                    lay.addChild(btnDisp);
                    entryCount++;
                    break;
                case Skin:
                    SettingsListButtonSkinSpinner<?> skin = new SettingsListButtonSkinSpinner<Object>("Skin", SettingCategory.Button, SettingModus.NORMAL, SettingStoreType.Global, SettingUsage.ACB);
                    CB_View_Base skinView = getSkinSpinnerView(skin);
                    lay.addChild(skinView);
                    entryCount++;
                    break;
                case Sounds:
                    CB_RectF rec = itemRec.copy();
                    Box lblBox = new Box(rec, "LabelBox");

                    CB_RectF rec2 = rec.copy();
                    rec2.setWidth(rec.getWidth() - (rec.getX() * 2));
                    rec2.setHeight(rec.getHalfHeight());

                    CB_Label lblVolume = new CB_Label(this.name + " lblVolume", itemRec, Translation.get("Volume"));
                    CB_Label lblMute = new CB_Label(this.name + " lblMute", itemRec, Translation.get("Mute"));

                    lblVolume.setZeroPos();
                    lblMute.setZeroPos();

                    lblMute.setHAlignment(HAlignment.RIGHT);

                    lblBox.addChild(lblMute);
                    lblBox.addChild(lblVolume);

                    lay.addChild(lblBox);
                    entryCount++;
                    break;

            }

            Boolean expandLayout = false;

            for (SettingBase settingItem : CatList) {
                final CB_View_Base view = getView(settingItem, position++);

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

            if (entryCount > 0) {

                lay.setBackground(this.getBackground());// Activity Background
                if (!expandLayout)
                    lay.setAnimationHeight(0f);

                addControlToLinearLayout(btn, margin);
                addControlToLinearLayout(lay, -(this.drawableBackground.getBottomHeight()) / 2);

                btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                        lay.Toggle();
                        return true;
                    }
                });
            }
        }

        setVolumeState(Config.GlobalVolume.getValue().Mute);
        apiBtn.setImage();

    }

    private void addControlToLinearLayout(CB_View_Base view, float itemMargin) {
        if (LinearLayout == null || scrollBox == null) {

            CB_RectF rec = new CB_RectF(0, btnOk.getMaxY() + margin, this.getWidth(), this.getHeight() - btnOk.getMaxY() - margin);

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

    private CB_View_Base getView(SettingBase<?> SB, int BackgroundChanger) {
        if (SB instanceof SettingBool) {
            return getBoolView((SettingBool) SB, BackgroundChanger);
        } else if (SB instanceof SettingIntArray) {
            return getIntArrayView((SettingIntArray) SB, BackgroundChanger);
        } else if (SB instanceof SettingStringArray) {
            return getStringArrayView((SettingStringArray) SB, BackgroundChanger);
        } else if (SB instanceof SettingTime) {
            return getTimeView((SettingTime) SB, BackgroundChanger);
        } else if (SB instanceof SettingInt) {
            return getIntView((SettingInt) SB, BackgroundChanger);
        } else if (SB instanceof SettingDouble) {
            return getDblView((SettingDouble) SB, BackgroundChanger);
        } else if (SB instanceof SettingFloat) {
            return getFloatView((SettingFloat) SB, BackgroundChanger);
        } else if (SB instanceof SettingFolder) {
            return getFolderView((SettingFolder) SB, BackgroundChanger);
        } else if (SB instanceof SettingFile) {
            return getFileView((SettingFile) SB, BackgroundChanger);
        } else if (SB instanceof SettingEnum) {
            return getEnumView((SettingEnum<?>) SB, BackgroundChanger);
        } else if (SB instanceof SettingString) {
            return getStringView((SettingString) SB, BackgroundChanger);
        } else if (SB instanceof SettingsListCategoryButton) {
            return getButtonView((SettingsListCategoryButton<?>) SB, BackgroundChanger);
        } else if (SB instanceof SettingsListGetApiButton) {
            return getApiKeyButtonView((SettingsListGetApiButton<?>) SB, BackgroundChanger);
        } else if (SB instanceof SettingsListButtonLangSpinner) {
            return getLangSpinnerView((SettingsListButtonLangSpinner<?>) SB);
        } else if (SB instanceof SettingsListButtonSkinSpinner) {
            return getSkinSpinnerView((SettingsListButtonSkinSpinner<?>) SB);
        } else if (SB instanceof SettingsAudio) {
            return getAudioView((SettingsAudio) SB, BackgroundChanger);
        } else if (SB instanceof SettingColor) {
            return getColorView((SettingColor) SB, BackgroundChanger);
        }

        return null;
    }

    private CB_View_Base getColorView(final SettingColor SB, int backgroundChanger) {
        SettingsItemBase item = new SettingsItem_Color(itemRec, backgroundChanger, SB);
        final String trans = Translation.get(SB.getName());

        item.setName(trans);
        item.setDefault(String.valueOf(SB.getValue()));

        item.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                EditKey = Config.settings.indexOf(SB);

                GL.that.RunOnGLWithThreadCheck(() -> {
                    ColorPicker clrPick = new ColorPicker(ActivityBase.ActivityRec(), SB.getValue(), new IReturnListener() {

                        @Override
                        public void returnColor(Color color) {
                            if (color == null)
                                return; // nothing changed

                            SettingColor SetValue = (SettingColor) Config.settings.get(EditKey);
                            if (SetValue != null)
                                SetValue.setValue(color);
                            resortList();
                            // Activity wieder anzeigen
                            show();
                        }
                    });
                    clrPick.show();
                });

                return true;
            }

        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return false;
            }

        });

        return item;
    }

    private CB_View_Base getStringView(final SettingString SB, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
        final String trans = Translation.get(SB.getName());
        item.setName(trans);

        if (SB.isDefault()) {
            item.setDefault(Translation.get("default"));
        } else {
            item.setDefault(SB.getValue());
        }

        item.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                EditKey = Config.settings.indexOf(SB);

                WrapType type;

                type = (SB instanceof SettingLongString) ? WrapType.WRAPPED : WrapType.SINGLELINE;

                StringInputBox.Show(type, "default:" + GlobalCore.br + SB.getDefaultValue(), trans, SB.getValue(), new OnMsgBoxClickListener() {

                    @Override
                    public boolean onClick(int which, Object data) {
                        String text = StringInputBox.editText.getText().toString();
                        if (which == MessageBox.BUTTON_POSITIVE) {
                            SettingString value = (SettingString) Config.settings.get(EditKey);

                            // api ohne lineBreak
                            if (value.getName().equalsIgnoreCase("AccessToken")) {
                                text = text.replace("\r", "");
                                text = text.replace("\n", "");
                            }

                            if (value != null)
                                value.setValue(text);

                            resortList();
                        }
                        // Activity wieder anzeigen
                        SettingsActivity.this.show();
                        return true;
                    }
                });

                return true;
            }

        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return true;
            }

        });

        return item;

    }

    private CB_View_Base getEnumView(final SettingEnum<?> SB, int backgroundChanger) {

        SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

        item.setName(Translation.get(SB.getName()));

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

        spinner.setSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(int index) {
                SB.setValue(SB.getValues().get(index));
            }
        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return false;
            }

        });

        return item;
    }

    private CB_View_Base getIntArrayView(final SettingIntArray SB, int backgroundChanger) {

        SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

        item.setName(Translation.get(SB.getName()));

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

        spinner.setSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(int index) {
                SB.setValue(SB.getValueFromIndex(index));
            }
        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return false;
            }

        });

        return item;

    }

    private CB_View_Base getStringArrayView(final SettingStringArray SB, int backgroundChanger) {

        SettingsItemEnum item = new SettingsItemEnum(itemRec, backgroundChanger, SB.getName());

        item.setName(Translation.get(SB.getName()));

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

        spinner.setSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(int index) {
                SB.setValue(SB.getValueFromIndex(index));
            }
        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return false;
            }

        });

        return item;

    }

    private CB_View_Base getIntView(final SettingInt SB, int backgroundChanger) {

        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
        final String trans = Translation.get(SB.getName());

        item.setName(trans);
        item.setDefault(String.valueOf(SB.getValue()));

        item.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                EditKey = Config.settings.indexOf(SB);

                // Show NumPad Int Edit
                NumericInputBox.Show("default: " + GlobalCore.br + String.valueOf(SB.getDefaultValue()), trans, SB.getValue(), new IReturnValueListener() {
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
            }

        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return false;
            }

        });

        return item;
    }

    private CB_View_Base getDblView(final SettingDouble SB, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
        final String trans = Translation.get(SB.getName());
        item.setName(trans);
        item.setDefault(String.valueOf(SB.getValue()));

        item.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                EditKey = Config.settings.indexOf(SB);

                // Show NumPad Int Edit
                NumericInputBox.Show("default: " + GlobalCore.br + String.valueOf(SB.getDefaultValue()), trans, SB.getValue(), new IReturnValueListenerDouble() {
                    @Override
                    public void returnValue(double value) {
                        SettingDouble SetValue = (SettingDouble) Config.settings.get(EditKey);
                        if (SetValue != null)
                            SetValue.setValue(value);
                        resortList();
                        // Activity wieder anzeigen
                        SettingsActivity.this.show();
                    }

                    @Override
                    public void cancelClicked() {
                        // Activity wieder anzeigen
                        SettingsActivity.this.show();
                    }
                });
                return true;
            }

        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return false;
            }

        });

        return item;

    }

    private CB_View_Base getFloatView(final SettingFloat SB, int backgroundChanger) {
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, SB.getName());
        final String trans = Translation.get(SB.getName());
        item.setName(trans);
        item.setDefault(String.valueOf(SB.getValue()));

        item.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                EditKey = Config.settings.indexOf(SB);

                // Show NumPad Int Edit
                NumericInputBox.Show("default: " + GlobalCore.br + String.valueOf(SB.getDefaultValue()), trans, SB.getValue(), new IReturnValueListenerDouble() {
                    @Override
                    public void returnValue(double value) {
                        SettingFloat SetValue = (SettingFloat) Config.settings.get(EditKey);
                        if (SetValue != null)
                            SetValue.setValue((float) value);
                        resortList();
                        // Activity wieder anzeigen
                        SettingsActivity.this.show();
                    }

                    @Override
                    public void cancelClicked() {
                        // Activity wieder anzeigen
                        SettingsActivity.this.show();
                    }
                });
                return true;
            }

        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return false;
            }

        });

        return item;

    }

    private CB_View_Base getFolderView(final SettingFolder settingFolder, int backgroundChanger) {

        final boolean needWritePermission = settingFolder.needWritePermission();
        SettingsItemBase item = new SettingsItemBase(itemRec, backgroundChanger, settingFolder.getName());

        item.setName(Translation.get(settingFolder.getName()));
        if (settingFolder.isDefault()) {
            item.setDefault(Translation.get("default"));
        } else {
            item.setDefault(settingFolder.getValue());
        }

        item.setOnClickListener((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(settingFolder);
            File file = FileFactory.createFile(settingFolder.getValue());
            final String absolutePath = (file != null) ? file.getAbsolutePath() : "";
            Menu icm = new Menu("SelectPathTitle");
            icm.addMenuItem("select_folder", null,
                    () -> PlatformConnector.getFolder(absolutePath, Translation.get("select_folder"), Translation.get("select"), Path -> {
                        // check WriteProtection
                        if (needWritePermission && !FileIO.canWrite(Path)) {
                            String WriteProtectionMsg = Translation.get("NoWriteAcces");
                            GL.that.Toast(WriteProtectionMsg, 8000);
                        } else {
                            settingFolder.setValue(Path);
                            resortList();
                        }
                    }));
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
        item.setDefault(settingFile.getValue());

        item.setOnClickListener((v, x, y, pointer, button) -> {
            EditKey = Config.settings.indexOf(settingFile);
            File file = FileFactory.createFile(settingFile.getValue());

            final String Path = (file.getParent() != null) ? file.getParent() : "";

            Menu icm = new Menu("SelectFileTitle");

            icm.addMenuItem("select_file", null,
                    () -> PlatformConnector.getFile(Path, settingFile.getExt(), Translation.get("select_file"), Translation.get("select"), Path1 -> {
                        settingFile.setValue(Path1);
                        resortList();
                    }));
            icm.addMenuItem("ClearPath", null, () -> {
                settingFile.setValue("");
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

    private CB_View_Base getButtonView(final SettingsListCategoryButton<?> SB, int backgroundChanger) {
        CB_Button btn = new CB_Button(ButtonRec, "Button");

        btn.setDraggable();

        btn.setText(Translation.get(SB.getName()));

        if (SB.getName().equals("DebugDisplayInfo")) {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

                    if (SB.getName().equals("DebugDisplayInfo")) {
                        String info = "";

                        info += "Density= " + String.valueOf(GL_UISizes.DPI) + GlobalCore.br;
                        info += "Height= " + String.valueOf(UI_Size_Base.that.getWindowHeight()) + GlobalCore.br;
                        info += "Width= " + String.valueOf(UI_Size_Base.that.getWindowWidth()) + GlobalCore.br;
                        info += "Scale= " + String.valueOf(UI_Size_Base.that.getScale()) + GlobalCore.br;
                        info += "FontSize= " + String.valueOf(UI_Size_Base.that.getScaledFontSize()) + GlobalCore.br;
                        info += "GPS min pos Time= " + String.valueOf(PositionChangedEventList.minPosEventTime) + GlobalCore.br;
                        info += "GPS min Orientation Time= " + String.valueOf(PositionChangedEventList.minOrientationEventTime) + GlobalCore.br;

                        MessageBox.show(info, msgBoxReturnListener);

                        return true;
                    }

                    return false;
                }

            });
        }

        btn.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return false;
            }

        });

        return btn;
    }

    private CB_View_Base getApiKeyButtonView(final SettingsListGetApiButton<?> SB, int backgroundChanger) {
        apiBtn = new API_Button(itemRec);
        apiBtn.setImage();
        return apiBtn;
    }

    private CB_View_Base getAudioView(final SettingsAudio SB, int backgroundChanger) {

        boolean full = Config.SettingsShowExpert.getValue() || Config.SettingsShowAll.getValue();
        final String AudioName = SB.getName();
        final SettingsItem_Audio item = new SettingsItem_Audio(itemRec, backgroundChanger, SB.getName(), full, new FloatControl.iValueChanged() {

            @Override
            public void ValueChanged(int value) {
                Audio aud = new Audio(SB.getValue());
                aud.Volume = value / 100f;
                SB.setValue(aud);

                // play Audio now

                if (AudioName.equalsIgnoreCase("GlobalVolume"))
                    SoundCache.play(Sounds.Global, true);
                if (AudioName.equalsIgnoreCase("Approach"))
                    SoundCache.play(Sounds.Approach);
                if (AudioName.equalsIgnoreCase("GPS_lose"))
                    SoundCache.play(Sounds.GPS_lose);
                if (AudioName.equalsIgnoreCase("GPS_fix"))
                    SoundCache.play(Sounds.GPS_fix);
                if (AudioName.equalsIgnoreCase("AutoResortSound"))
                    SoundCache.play(Sounds.AutoResortSound);
            }
        });

        item.setName(Translation.get(SB.getName()));
        item.setDefault("default: " + String.valueOf(SB.getDefaultValue()));
        item.setVolume((int) (SB.getValue().Volume * 100));
        CB_CheckBox chk = item.getCheckBox();

        if (!AudioName.contains("Global")) {
            if (audioSettingsList == null)
                audioSettingsList = new ArrayList<SettingsItem_Audio>();
            audioSettingsList.add(item);
        }

        chk.setChecked(SB.getValue().Mute);
        chk.setOnCheckChangedListener(new OnCheckChangedListener() {
            @Override
            public void onCheckedChanged(CB_CheckBox view, boolean isChecked) {
                Audio aud = new Audio(SB.getValue());
                aud.Mute = isChecked;
                SB.setValue(aud);
                item.setMuteDisabeld(isChecked);
                if (AudioName.contains("Global")) {
                    // Enable or disable all other
                    setVolumeState(isChecked);
                }
            }

        });

        item.setMuteDisabeld(chk.isChecked());

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return true;
            }

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
        item.setDefault(intToTime(SB.getValue()));

        item.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                EditKey = Config.settings.indexOf(SB);

                String Value = intToTime(SB.getValue());
                String[] s = Value.split(":");

                int intValueMin = Integer.parseInt(s[0]);
                int intValueSec = Integer.parseInt(s[1]);

                // Show NumPad Int Edit
                NumericInputBox.Show("default: " + GlobalCore.br + intToTime(SB.getDefaultValue()), trans, intValueMin, intValueSec, new IReturnValueListenerTime() {
                    @Override
                    public void returnValue(int min, int sec) {
                        SettingTime SetValue = (SettingTime) Config.settings.get(EditKey);
                        int value = (min * 60 * 1000) + (sec * 1000);
                        if (SetValue != null)
                            SetValue.setValue(value);
                        resortList();
                        // Activity wieder anzeigen
                        SettingsActivity.this.show();
                    }

                    @Override
                    public void cancelClicked() {
                        // Activity wieder anzeigen
                        SettingsActivity.this.show();
                    }
                });
                return true;
            }

        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return false;
            }

        });

        return item;

    }

    private String intToTime(int milliseconds) {
        int seconds = milliseconds / 1000 % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        // int hours = (int) ((milliseconds / (1000*60*60)) % 24);

        return String.valueOf(minutes) + ":" + String.valueOf(seconds);
    }

    private CB_View_Base getLangSpinnerView(final SettingsListButtonLangSpinner<?> SB) {
        Sprachen = Translation.GetLangs(Config.LanguagePath.getValue());

        if (Sprachen == null || Sprachen.size() == 0)
            return null;

        final String[] items = new String[Sprachen.size()];
        int index = 0;
        int selection = -1;

        File file1 = FileFactory.createFile(Config.Sel_LanguagePath.getValue());

        for (Lang tmp : Sprachen) {
            File file2 = FileFactory.createFile(tmp.Path);
            if (file1.getAbsoluteFile().compareTo(file2.getAbsoluteFile()) == 0) {
                selection = index;
            }

            items[index++] = tmp.Name;
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

        Spinner spinner = new Spinner(ButtonRec, "SelectLanguage", adapter, index1 -> {
            String selected = items[index1];
            for (Lang tmp : Sprachen) {
                if (selected.equals(tmp.Name)) {
                    Config.Sel_LanguagePath.setValue(tmp.Path);
                    try {
                        Translation.LoadTranslation(tmp.Path);
                    } catch (Exception e) {
                        try {
                            Translation.LoadTranslation(Config.Sel_LanguagePath.getDefaultValue());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
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

    private CB_View_Base getSkinSpinnerView(final SettingsListButtonSkinSpinner<?> SB) {
        String SkinFolder = Config.mWorkPath + "/skins";
        File dir = FileFactory.createFile(SkinFolder);
        final ArrayList<String> skinFolders = new ArrayList<String>();
        dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                if (f.isDirectory()) {
                    String Path = f.getAbsolutePath();
                    if (!Path.contains(".svn")) {
                        skinFolders.add(name);
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
        if (Config.SkinFolder.getValue().equals("default"))
            selection = 0;
        if (Config.SkinFolder.getValue().equals("small"))
            selection = 1;
        for (String tmp : skinFolders) {
            if (Config.SkinFolder.getValue().endsWith(tmp))
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
                Config.SkinFolder.setValue("default");
            } else if (selected.equals("small")) {
                Config.SkinFolder.setValue("small");
            } else {
                Config.SkinFolder.setValue(Config_Core.mWorkPath + "/skins/" + selected);
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
        item.setDefault("default: " + String.valueOf(SB.getDefaultValue()));

        CB_CheckBox chk = item.getCheckBox();

        chk.setChecked(SB.getValue());
        chk.setOnCheckChangedListener(new OnCheckChangedListener() {
            @Override
            public void onCheckedChanged(CB_CheckBox view, boolean isChecked) {
                SB.setValue(isChecked);
                if (SB.getName().equalsIgnoreCase("DraftsLoadAll")) {
                    resortList();
                }
            }
        });

        item.setOnLongClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                // zeige Beschreibung der Einstellung

                MessageBox.show(Translation.get("Desc_" + SB.getName()), msgBoxReturnListener);

                return true;
            }

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

    class SettingsOrder implements Comparator<SettingBase> {
        public int compare(SettingBase a, SettingBase b) {
            return a.compareTo(b);
        }
    }
}
