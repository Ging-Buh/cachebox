/*
 * Copyright (C) 2014 team-cachebox.de
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
package de.droidcachebox.gdx.activities;

import static de.droidcachebox.gdx.controls.FilterSetListViewItem.CHECK_ITEM;
import static de.droidcachebox.gdx.controls.FilterSetListViewItem.COLLAPSE_BUTTON_ITEM;
import static de.droidcachebox.gdx.controls.FilterSetListViewItem.FilterSetEntry;
import static de.droidcachebox.gdx.controls.FilterSetListViewItem.NUMERIC_INT_ITEM;
import static de.droidcachebox.gdx.controls.FilterSetListViewItem.NUMERIC_ITEM;
import static de.droidcachebox.gdx.controls.FilterSetListViewItem.SELECT_ALL_ITEM;
import static de.droidcachebox.gdx.controls.FilterSetListViewItem.THREE_STATE_ITEM;
import static de.droidcachebox.locator.map.MapViewBase.INITIAL_WP_LIST;
import static de.droidcachebox.menu.Action.ShowEditFilterSettings;
import static de.droidcachebox.menu.Action.ShowMap;
import static de.droidcachebox.settings.Config_Core.br;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.KeyboardFocusChangedEventList;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.database.CategoryDAO;
import de.droidcachebox.dataclasses.Attribute;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.GpxFilename;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.FilterSetListViewItem;
import de.droidcachebox.gdx.controls.MultiToggleButton;
import de.droidcachebox.gdx.controls.TextFilterView;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.dialogs.WaitDialog;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.SettingStringList;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

/**
 * Defining a Filter for geoCaches to show is done by selection within 4 Tabs (in contentBox):
 * first  tab (presets) : combined from settings the predefined filters (in FilterInstances) and the saved filters named by the user can be selected with one click + ok
 * second tab (settings): 4 Buttons (general, dt, cacheTypes, attributes) for general (common) boolean properties, valued ranges (difficulty, terrain,..), geoCacheTypes and attributes.
 * third  tab (Category View): buttons for categories and subcategories reflecting the different imports of the actual database
 * forth  tab (TextView): string filters by GCCodes, owner and geocache title
 */
public class EditFilterSettings extends ActivityBase {
    private static final String sClass = "EditFilterSettings";
    private static final int presetViewId = 0;
    private static final int filterSetViewId = 1;
    private static final int categoryViewId = 2;
    private static final int textFilterViewId = 3;
    private static CB_RectF itemRec;
    private static FilterProperties tmpFilterProps; // this is the storage for the filter
    private final MultiToggleButton btnPresetFilters;
    private final MultiToggleButton btnFilterSetFilters;
    private final MultiToggleButton btnCategoryFilters;
    private final MultiToggleButton btnTextFilters;
    private final CB_Button btnAddUserDefinedFilter;
    private final PresetListView presetView;
    private FilterSetListView filterSetView;
    private CategoryListView categoryView;
    private TextFilterView textFilterView;
    private int lastViewId;

    public EditFilterSettings(String Name) {
        super(Name);
        itemRec = new CB_RectF(leftBorder, 0, innerWidth, UiSizes.getInstance().getButtonHeight() * 1.1f);

        tmpFilterProps = FilterInstances.getLastFilter();

        float myWidth = getWidth() - leftBorder;
        float yy = leftBorder;
        CB_Button btnOK = new CB_Button(leftBorder / 2, yy, myWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Button");
        btnOK.setText(Translation.get("ok"));
        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            switch (lastViewId) {
                case presetViewId:
                    break;
                case filterSetViewId:
                    tmpFilterProps = filterSetView.updateFilterProperties(tmpFilterProps);
                    break;
                case categoryViewId:
                    tmpFilterProps = categoryView.updateFilterProperties(tmpFilterProps); // categories to tmpFilterProps
                    break;
                case textFilterViewId:
                    tmpFilterProps = textFilterView.updateFilterProperties(tmpFilterProps); // textFilters to tmpFilterProps
                    break;
                default:
            }

            FilterInstances.setLastFilter(tmpFilterProps); // remember and access from everywhere
            KeyboardFocusChangedEventList.remove(textFilterView);
            finish();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    FilterProperties filter = FilterInstances.getLastFilter();
                    applyFilter(filter);

                    // Save selected filter (new JSON Format)
                    // wont save History at the Moment
                    // Marker must be removed, else isFiltered is shown
                    // wont change the LastFilter
                    if (filter.isHistory) {
                        FilterProperties tmp = new FilterProperties(filter.toString());
                        tmp.isHistory = false;
                        Settings.lastFilter.setValue(tmp.toString());
                    } else {
                        Settings.lastFilter.setValue(filter.toString());
                    }
                    Settings.getInstance().acceptChanges();
                }
            }, 300);
            return true;
        });
        addChild(btnOK);

        CB_Button btnCancel = new CB_Button(btnOK.getMaxX(), yy, myWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Button");
        btnCancel.setText(Translation.get("cancel"));
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            finish();
            return true;
        });
        addChild(btnCancel);

        float topButtonY = getHeight() - leftBorder - UiSizes.getInstance().getButtonHeight();

        Box contentBox = new Box(new CB_RectF(0, btnOK.getMaxY(), getWidth(), topButtonY - btnOK.getMaxY()), "contentBox");
        contentBox.setBackground(Sprites.activityBackground);
        addChild(contentBox);

        CB_RectF MTBRec = new CB_RectF(leftBorder / 2, topButtonY, myWidth / 4, UiSizes.getInstance().getButtonHeight());

        btnPresetFilters = new MultiToggleButton(MTBRec, "btPre");
        btnFilterSetFilters = new MultiToggleButton(MTBRec, "btSet");
        btnCategoryFilters = new MultiToggleButton(MTBRec, "btCat");
        btnTextFilters = new MultiToggleButton(MTBRec, "btTxt");

        btnFilterSetFilters.setX(btnPresetFilters.getMaxX());
        btnCategoryFilters.setX(btnFilterSetFilters.getMaxX());
        btnTextFilters.setX(btnCategoryFilters.getMaxX());

        String sPre = Translation.get("preset");
        String sSet = Translation.get("setting");
        String sCat = Translation.get("category");
        String sTxt = Translation.get("text");

        btnPresetFilters.initialOn_Off_ToggleStates(sPre, sPre);
        btnFilterSetFilters.initialOn_Off_ToggleStates(sSet, sSet);
        btnCategoryFilters.initialOn_Off_ToggleStates(sCat, sCat);
        btnTextFilters.initialOn_Off_ToggleStates(sTxt, sTxt);

        btnPresetFilters.setClickHandler((v, x, y, pointer, button) -> {
            setViewVisible(presetViewId);
            return true;
        });

        btnFilterSetFilters.setClickHandler((v, x, y, pointer, button) -> {
            setViewVisible(filterSetViewId);
            return true;
        });

        btnCategoryFilters.setClickHandler((v, x, y, pointer, button) -> {
            setViewVisible(categoryViewId);
            return true;
        });

        btnTextFilters.setClickHandler((v, x, y, pointer, button) -> {
            setViewVisible(textFilterViewId);
            return true;
        });

        addChild(btnPresetFilters);
        addChild(btnFilterSetFilters);
        addChild(btnCategoryFilters);
        addChild(btnTextFilters);

        btnAddUserDefinedFilter = new CB_Button(new CB_RectF(leftBorder, margin, innerWidth, UiSizes.getInstance().getButtonHeight()), "AddPresetButton");
        btnAddUserDefinedFilter.setText(Translation.get("AddOwnFilterPreset"));
        btnAddUserDefinedFilter.setClickHandler((v, x, y, pointer, button) -> {
            addUserDefinedFilter();
            return true;
        });
        contentBox.addChild(btnAddUserDefinedFilter);

        CB_RectF listViewRec = new CB_RectF(0, margin, getWidth(), btnPresetFilters.getY() - btnOK.getMaxY() - margin - margin);
        CB_RectF preRec = new CB_RectF(listViewRec);
        preRec.setHeight(listViewRec.getHeight() - UiSizes.getInstance().getButtonHeight() - margin);
        preRec.setY(btnAddUserDefinedFilter.getMaxY() + margin);

        presetView = new PresetListView(preRec);
        contentBox.addChild(presetView);

        filterSetView = new FilterSetListView(listViewRec);
        contentBox.addChild(filterSetView);

        categoryView = new CategoryListView(listViewRec);
        contentBox.addChild(categoryView);

        textFilterView = new TextFilterView(listViewRec, "TextFilterView");
        contentBox.addChild(textFilterView);
        filterSetView.setFilter(tmpFilterProps);
        textFilterView.setFilter(tmpFilterProps);

        lastViewId = -1;
        setViewVisible(0);

    }

    public static void applyFilter(final FilterProperties filterProperties) {

        new WaitDialog(Translation.get("FilterCaches"), new RunAndReady() {
            @Override
            public void ready() {
                ViewManager.that.filterSetChanged();
                ((ShowMap) ShowMap.action).normalMapView.setNewSettings(INITIAL_WP_LIST);

                // Save selected filter (new JSON Format)
                // wont save History
                // Marker must be removed, else isFiltered is shown
                // wont change the LastFilter
                if (FilterInstances.getLastFilter().isHistory) {
                    FilterProperties tmp = new FilterProperties(FilterInstances.getLastFilter().toString());
                    tmp.isHistory = false;
                    Settings.lastFilter.setValue(tmp.toString());
                } else {
                    Settings.lastFilter.setValue(FilterInstances.getLastFilter().toString());
                }
                Settings.getInstance().acceptChanges();
            }

            @Override
            public void setIsCanceled() {

            }

            @Override
            public void run() {
                try {
                    synchronized (CBDB.getInstance().cacheList) {
                        String sqlWhere = filterProperties.getSqlWhere(Settings.GcLogin.getValue());
                        new CachesDAO().readCacheList(sqlWhere, false, false, Settings.showAllWaypoints.getValue());
                        GlobalCore.checkSelectedCacheValid();
                    }
                    CacheListChangedListeners.getInstance().fire();
                } catch (Exception ex) {
                    Log.err(sClass, "applyFilter", ex);
                }
            }
        }).show();

    }

    private void setViewVisible(int viewId) {
        if (lastViewId == viewId) return;
        switch (lastViewId) {
            case presetViewId:
                btnPresetFilters.setState(0);
                presetView.setInvisible();
                btnAddUserDefinedFilter.setInvisible();
                break;
            case filterSetViewId:
                btnFilterSetFilters.setState(0);
                filterSetView.setInvisible();
                tmpFilterProps = filterSetView.updateFilterProperties(tmpFilterProps);
                break;
            case categoryViewId:
                btnCategoryFilters.setState(0);
                categoryView.setInvisible();
                tmpFilterProps = categoryView.updateFilterProperties(tmpFilterProps);
                break;
            case textFilterViewId:
                btnTextFilters.setState(0);
                textFilterView.setInvisible();
                tmpFilterProps = textFilterView.updateFilterProperties(tmpFilterProps);
                KeyboardFocusChangedEventList.remove(textFilterView);
                break;
            default:
                btnPresetFilters.setState(0);
                presetView.setInvisible();
                btnAddUserDefinedFilter.setInvisible();
                btnFilterSetFilters.setState(0);
                filterSetView.setInvisible();
                btnCategoryFilters.setState(0);
                categoryView.setInvisible();
                btnTextFilters.setState(0);
                textFilterView.setInvisible();
        }
        btnPresetFilters.setClickable(true);
        btnFilterSetFilters.setClickable(true);
        btnCategoryFilters.setClickable(true);
        btnTextFilters.setClickable(true);
        switch (viewId) {
            case presetViewId:
                btnPresetFilters.setState(1);
                btnPresetFilters.setClickable(false);
                presetView.setVisible();
                btnAddUserDefinedFilter.setVisible();
                // data by other views reflected for selection marking on render
                presetView.onShow();
                break;
            case filterSetViewId:
                btnFilterSetFilters.setState(1);
                btnFilterSetFilters.setClickable(false);
                filterSetView.setVisible();
                // cause may be changed by preset
                filterSetView.setFilter(tmpFilterProps);
                filterSetView.onShow();
                break;
            case categoryViewId:
                btnCategoryFilters.setState(1);
                btnCategoryFilters.setClickable(false);
                categoryView.setVisible();
                // data unchanged by other views
                categoryView.onShow();
                break;
            case textFilterViewId:
                btnTextFilters.setState(1);
                btnTextFilters.setClickable(false);
                textFilterView.setVisible();
                textFilterView.setFilter(tmpFilterProps);
                textFilterView.onShow();
                break;
            default:
        }
        lastViewId = viewId;
    }

    private void addUserDefinedFilter() {
        GL.that.closeActivity();

        // Check if Preset exist
        boolean exist = false;
        String existName = "";
        for (PresetListView.PresetListViewItem v : presetView.presetListViewItems) {
            if (v.getEntry().getFilterProperties().equals(tmpFilterProps)) {
                exist = true;
                existName = v.getEntry().getName();
            }
        }

        if (exist) {
            ButtonDialog bd = new ButtonDialog(Translation.get("PresetExist") + br + br + "\"" + existName + "\"", null, MsgBoxButton.OK, MsgBoxIcon.Warning);
            bd.setButtonClickHandler((which, data) -> {
                ShowEditFilterSettings.action.execute();
                return true;
            });
            bd.show();
            return;
        }

        StringInputBox stringInputBox = new StringInputBox(Translation.get("NewUserPreset"), Translation.get("InsNewUserPreset"), "UserPreset", WrapType.SINGLELINE);
        stringInputBox.setButtonClickHandler((which, data) -> {
            if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                String nameOfNewFilter = StringInputBox.editTextField.getText();
                String userFilters = Settings.UserFilters.getValue();
                String newFilterString = tmpFilterProps.toString();

                int pos = newFilterString.indexOf("^");
                if (pos > -1) {
                    int posE = newFilterString.indexOf("\"", pos);
                    String after = newFilterString.substring(posE);
                    newFilterString = newFilterString.substring(0, pos) + after;
                }

                userFilters = userFilters + nameOfNewFilter + ";" + newFilterString + SettingStringList.SPLITTER;
                Settings.UserFilters.setValue(userFilters);
                Settings.getInstance().acceptChanges();
                presetView.fillPresetList();
                presetView.notifyDataSetChanged();
            }
            ShowEditFilterSettings.action.execute();
            return true;
        });
        stringInputBox.showAtTop();
    }

    @Override
    public void onShow() {
        if (presetView != null) {
            presetView.notifyDataSetChanged();
        }
    }

    public interface ISelectAllHandler {
        void selectAllCacheTypes();

        void selectNoCacheTypes();
    }

    private static class PresetListView extends V_ListView {

        ArrayList<PresetListViewItem> presetListViewItems;
        private ArrayList<Preset> presets;

        PresetListView(CB_RectF rec) {
            super(rec, "");
            setHasInvisibleItems();
            fillPresetList();
            setDisposeFlag(false);
            PresetsAdapter presetsAdapter = new PresetsAdapter(presets);
            setAdapter(presetsAdapter);
        }

        void fillPresetList() {
            if (presets == null) presets = new ArrayList<>();
            else presets.clear();
            if (presetListViewItems == null) presetListViewItems = new ArrayList<>();
            else presetListViewItems.clear();

            FilterInstances.HISTORY.isHistory = true;

            presets.add(new Preset(Translation.get("HISTORY"), Sprites.getSprite("HISTORY"), FilterInstances.HISTORY));
            presets.add(new Preset(Translation.get("AllCachesToFind"), Sprites.getSprite("log0icon"), FilterInstances.ACTIVE));
            presets.add(new Preset(Translation.get("QuickCaches"), Sprites.getSprite("QuickCaches"), FilterInstances.QUICK));
            presets.add(new Preset(Translation.get("BEGINNER"), Sprites.getSprite("BEGINNER"), FilterInstances.BEGINNER));
            presets.add(new Preset(Translation.get("GrabTB"), Sprites.getSprite(Sprites.IconName.TBGRAB), FilterInstances.WITHTB));
            presets.add(new Preset(Translation.get("DropTB"), Sprites.getSprite(Sprites.IconName.TBDROP), FilterInstances.DROPTB));
            presets.add(new Preset(Translation.get("Highlights"), Sprites.getSprite("star"), FilterInstances.HIGHLIGHTS));
            presets.add(new Preset(Translation.get("Favorites"), Sprites.getSprite("favorit"), FilterInstances.FAVORITES));
            presets.add(new Preset(Translation.get("PrepareToArchive"), Sprites.getSprite(Sprites.IconName.DELETE), FilterInstances.TOARCHIVE));
            presets.add(new Preset(Translation.get("ListingChanged"), Sprites.getSprite(Sprites.IconName.warningIcon), FilterInstances.LISTINGCHANGED));
            presets.add(new Preset(Translation.get("AllCaches"), Sprites.getSprite("earth"), FilterInstances.ALL));
            int index = 0;
            for (Preset entry : presets) {
                PresetListViewItem v = new PresetListViewItem(itemRec, index, entry);
                v.setClickHandler((v12, x, y, pointer, button) -> {
                    PresetListViewItem clickedItem = (PresetListViewItem) v12;
                    tmpFilterProps = new FilterProperties(clickedItem.mPreset.filterProperties.toString());
                    return true;
                });
                presetListViewItems.add(v);
                index++;
            }

            // add userFilters from Config.UserFilter
            if (Settings.UserFilters.getValue().length() > 0) {
                String[] userFilters = Settings.UserFilters.getValue().split(SettingStringList.SPLITTER);
                try {
                    for (String userFilter : userFilters) {
                        int pos = userFilter.indexOf(";");
                        String name = userFilter.substring(0, pos);
                        String filter = userFilter.substring(pos + 1);
                        if (filter.endsWith("#"))
                            filter = filter.substring(0, filter.length() - 1); // relict?
                        Preset entry = new Preset(name, Sprites.getSprite("userdata"), new FilterProperties(filter));
                        presets.add(entry);
                        PresetListViewItem v = new PresetListViewItem(itemRec, index, entry);
                        presetListViewItems.add(v);
                        index++;
                        v.setClickHandler((v12, x, y, pointer, button) -> {
                            PresetListViewItem clickedItem = (PresetListViewItem) v12;
                            tmpFilterProps = new FilterProperties(clickedItem.mPreset.filterProperties.toString());
                            return true;
                        });
                        v.setLongClickHandler((v1, x, y, pointer, button) -> {
                            GL.that.closeActivity();
                            PresetListViewItem clickedItem = (PresetListViewItem) v1;
                            tmpFilterProps = new FilterProperties(clickedItem.mPreset.filterProperties.toString());
                            ButtonDialog bd = new ButtonDialog(Translation.get("?DelUserPreset"), Translation.get("DelUserPreset"), MsgBoxButton.YesNo, MsgBoxIcon.Question);
                            bd.setButtonClickHandler((which, data) -> {
                                // NO clicked
                                if (which == ButtonDialog.BTN_LEFT_POSITIVE) { // YES Clicked
                                    try {
                                        String userEntries = Settings.UserFilters.getValue();
                                        int p1 = userEntries.indexOf(clickedItem.mPreset.mName);
                                        int p2 = userEntries.indexOf(SettingStringList.SPLITTER, p1) + 1;
                                        String newUserEntries;
                                        if (p2 > p1)
                                            newUserEntries = userEntries.replace(userEntries.substring(p1, p2), "");
                                        else newUserEntries = userEntries.substring(0, p1);
                                        Settings.UserFilters.setValue(newUserEntries);
                                        Settings.getInstance().acceptChanges();
                                        fillPresetList();
                                        notifyDataSetChanged();
                                    } catch (Exception ex) {
                                        Log.err(sClass, "DelUserPreset", ex);
                                    }
                                }
                                ShowEditFilterSettings.action.execute();
                                return true;
                            });
                            bd.show();
                            return true;
                        });
                    }
                } catch (Exception ex) {
                    Log.err("PresetListView", "", ex);
                }
            }
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            if (visible) {
                for (PresetListViewItem item : presetListViewItems) {
                    item.setSelected(false);
                }
                notifyDataSetChanged();
            }
        }

        public static class Preset {
            private final String mName;
            private final Sprite mIcon;
            private final FilterProperties filterProperties;

            Preset(String Name, Sprite Icon, FilterProperties PresetFilter) {
                mName = Name;
                mIcon = Icon;
                filterProperties = PresetFilter;
            }

            public String getName() {
                return mName;
            }

            public Sprite getIcon() {
                return mIcon;
            }

            FilterProperties getFilterProperties() {
                return filterProperties;
            }

        }

        private static class PresetListViewItem extends ListViewItemBackground {
            private final PresetListView.Preset mPreset;
            BitmapFontCache EntryName;
            float left = 0;
            float top = 0;

            PresetListViewItem(CB_RectF rec, int Index, PresetListView.Preset fne) {
                super(rec, Index, fne.getName());
                mPreset = fne;
            }

            @Override
            protected void render(Batch batch) {
                if (isDisposed)
                    return;

                if (tmpFilterProps != null) {
                    isSelected = mPreset.getFilterProperties().equals(tmpFilterProps);
                }

                super.render(batch);

                if (isPressed) {
                    isPressed = GL_Input.that.getIsTouchDown();
                }

                // initial
                left = getLeftWidth();
                top = (getHeight() + Fonts.getNormal().getLineHeight()) / 2f; //getTopHeight();

                drawIcon(batch);

                // draw Name
                if (EntryName == null) {
                    EntryName = new BitmapFontCache(Fonts.getNormal());
                    EntryName.setColor(COLOR.getFontColor());
                    EntryName.setText(name, left + 10, top);
                }
                EntryName.draw(batch);

            }

            private void drawIcon(Batch batch) {
                if (mPreset.getIcon() != null) {
                    float iconHeight = getHeight() * 0.8f;
                    float iconWidth = getHeight() * 0.8f;
                    float y = (getHeight() - iconHeight) / 2f; // UI_Size_Base.that.getMargin()
                    mPreset.getIcon().setBounds(left, y, iconWidth, iconHeight);
                    mPreset.getIcon().draw(batch);
                    left = left + iconWidth + y + getLeftWidth();
                }
            }

            PresetListView.Preset getEntry() {
                return mPreset;
            }
        }

        public class PresetsAdapter implements Adapter {

            private final ArrayList<Preset> presets;

            PresetsAdapter(ArrayList<Preset> presets) {
                this.presets = presets;
            }

            @Override
            public int getCount() {
                return presets.size();
            }

            @Override
            public ListViewItemBase getView(final int position) {
                return presetListViewItems.get(position);
            }

            @Override
            public float getItemSize(int position) {
                return EditFilterSettings.itemRec.getHeight();
            }
        }

    }

    private static class FilterSetListView extends V_ListView {
        // the collapse buttons
        private FilterSetListViewItem activeCollapseButton; // only one should be active
        private FilterSetListViewItem cacheTypes;
        private FilterSetListViewItem attributes;
        //
        private FilterSetListViewItem notAvailable;
        private FilterSetListViewItem archived;
        private FilterSetListViewItem finds;
        private FilterSetListViewItem own;
        private FilterSetListViewItem containsTravelBugs;
        private FilterSetListViewItem favorites;
        private FilterSetListViewItem hasUserData;
        private FilterSetListViewItem listingChanged;
        private FilterSetListViewItem withManualWaypoint;
        private FilterSetListViewItem hasCorrectedCoordinates;
        private FilterSetListViewItem minTerrain;
        private FilterSetListViewItem maxTerrain;
        private FilterSetListViewItem minDifficulty;
        private FilterSetListViewItem maxDifficulty;
        private FilterSetListViewItem minContainerSize;
        private FilterSetListViewItem maxContainerSize;
        private FilterSetListViewItem minRating;
        private FilterSetListViewItem maxRating;
        private FilterSetListViewItem minFavPoints;
        private FilterSetListViewItem maxFavPoints;
        private int index = 0;
        private ArrayList<FilterSetEntry> filterSetEntries;
        private ArrayList<FilterSetListViewItem> filterSetListViewItems;

        FilterSetListView(CB_RectF rec) {
            super(rec, "FilterSetListView");
            setHasInvisibleItems();
            fillFilterSetList();
            setAdapter(new FilterSetAdapter());
            setDisposeFlag(false);
        }

        FilterProperties updateFilterProperties(FilterProperties filter) {
            filter.setFinds(finds.getChecked());
            filter.setNotAvailable(notAvailable.getChecked());
            filter.setArchived(archived.getChecked());
            filter.setOwn(own.getChecked());
            filter.setContainsTravelbugs(containsTravelBugs.getChecked());
            filter.setFavorites(favorites.getChecked());
            filter.setHasUserData(hasUserData.getChecked());
            filter.setListingChanged(listingChanged.getChecked());
            filter.setWithManualWaypoint(withManualWaypoint.getChecked());
            filter.setHasCorrectedCoordinates(hasCorrectedCoordinates.getChecked());

            filter.setMinDifficulty(minDifficulty.getValue());
            filter.setMaxDifficulty(maxDifficulty.getValue());
            filter.setMinTerrain(minTerrain.getValue());
            filter.setMaxTerrain(maxTerrain.getValue());
            filter.setMinContainerSize(minContainerSize.getValue());
            filter.setMaxContainerSize(maxContainerSize.getValue());
            filter.setMinRating(minRating.getValue());
            filter.setMaxRating(maxRating.getValue());
            filter.setMinFavPoints(minFavPoints.getValue());
            filter.setMaxFavPoints(maxFavPoints.getValue());

            filter.cacheTypes = "";
            String sep = "";
            for (FilterSetListViewItem itm : cacheTypes.getChildList()) {
                if (itm.getBoolean()) {
                    filter.cacheTypes = String.format(Locale.US, "%s%s%d", filter.cacheTypes, sep, itm.getFilterSetEntry().getCacheType().ordinal());
                    sep = ",";
                }
            }

            for (int i = 0; i < attributes.getChildLength(); i++) {
                filter.attributes[i + 1] = attributes.getChild(i).getChecked();
            }

            return filter;
        }

        private void setFilter(FilterProperties props) {
            finds.setValue(props.getFinds());
            notAvailable.setValue(props.getNotAvailable());
            archived.setValue(props.getArchived());
            own.setValue(props.getOwn());
            containsTravelBugs.setValue(props.getContainsTravelbugs());
            favorites.setValue(props.getFavorites());
            hasUserData.setValue(props.getHasUserData());
            listingChanged.setValue(props.getListingChanged());
            withManualWaypoint.setValue(props.getWithManualWaypoint());
            hasCorrectedCoordinates.setValue(props.getHasCorrectedCoordinates());

            minTerrain.setValue(props.getMinTerrain());
            maxTerrain.setValue(props.getMaxTerrain());
            minDifficulty.setValue(props.getMinDifficulty());
            maxDifficulty.setValue(props.getMaxDifficulty());
            minContainerSize.setValue(props.getMinContainerSize());
            maxContainerSize.setValue(props.getMaxContainerSize());
            minRating.setValue(props.getMinRating());
            maxRating.setValue(props.getMaxRating());
            minFavPoints.setValue(props.getMinFavPoints());
            maxFavPoints.setValue(props.getMaxFavPoints());

            String propsCacheTypes = "'" + props.cacheTypes.replace(",", "','") + "'";
            for (int i = 0; i < cacheTypes.getChildLength(); i++) {
                FilterSetListViewItem itm = cacheTypes.getChild(i);
                if (itm.getFilterSetEntry().getCacheType() != null) {
                    if (props.cacheTypes.length() == 0) {
                        itm.setValue(true);
                    } else {
                        int ct = itm.getFilterSetEntry().getCacheType().ordinal();
                        itm.setValue(propsCacheTypes.contains("'" + ct + "'"));
                    }
                }
            }

            for (int i = 0; i < attributes.getChildLength(); i++) {
                attributes.getChild(i).setValue(props.attributes[i + 1]);
            }

        }

        private void fillFilterSetList() {

            // add General
            FilterSetListViewItem general = addTitleItem(Translation.get("General"));
            notAvailable = general.addChild(addItem(Sprites.getSprite("disabled"), Translation.get("disabled"), THREE_STATE_ITEM));
            archived = general.addChild(addItem(Sprites.getSprite("not-available"), Translation.get("archived"), THREE_STATE_ITEM));
            finds = general.addChild(addItem(Sprites.getSprite("log0icon"), Translation.get("myfinds"), THREE_STATE_ITEM));
            own = general.addChild(addItem(Sprites.getSprite("star"), Translation.get("myowncaches"), THREE_STATE_ITEM));
            containsTravelBugs = general.addChild(addItem(Sprites.getSprite("tb"), Translation.get("withtrackables"), THREE_STATE_ITEM));
            favorites = general.addChild(addItem(Sprites.getSprite("favorit"), Translation.get("Favorites"), THREE_STATE_ITEM));
            hasUserData = general.addChild(addItem(Sprites.getSprite("userdata"), Translation.get("hasuserdata"), THREE_STATE_ITEM));
            listingChanged = general.addChild(addItem(Sprites.getSprite(Sprites.IconName.warningIcon.name()), Translation.get("ListingChanged"), THREE_STATE_ITEM));
            withManualWaypoint = general.addChild(addItem(Sprites.getSprite(Sprites.IconName.manualWayPoint.name()), Translation.get("manualWayPoint"), THREE_STATE_ITEM));
            hasCorrectedCoordinates = general.addChild(addItem(Sprites.getSprite("hasCorrectedCoordinates"), Translation.get("hasCorrectedCoordinates"), THREE_STATE_ITEM));

            // add D/T
            FilterSetListViewItem dt = addTitleItem("D / T" + "\n" + "GC-Vote");
            minDifficulty = dt.addChild(addNumericItem(Sprites.Stars.toArray(), Translation.get("minDifficulty"), 1, 5, 1, 0.5f));
            maxDifficulty = dt.addChild(addNumericItem(Sprites.Stars.toArray(), Translation.get("maxDifficulty"), 1, 5, 5, 0.5f));
            minTerrain = dt.addChild(addNumericItem(Sprites.Stars.toArray(), Translation.get("minTerrain"), 1, 5, 1, 0.5f));
            maxTerrain = dt.addChild(addNumericItem(Sprites.Stars.toArray(), Translation.get("maxTerrain"), 1, 5, 5, 0.5f));
            minContainerSize = dt.addChild(addNumericItem(Sprites.SizesIcons.toArray(), Translation.get("minContainerSize"), 0, 4, 0, 1));
            maxContainerSize = dt.addChild(addNumericItem(Sprites.SizesIcons.toArray(), Translation.get("maxContainerSize"), 0, 4, 4, 1));
            minRating = dt.addChild(addNumericItem(Sprites.Stars.toArray(), Translation.get("minRating"), 0, 5, 0, 0.5f));
            maxRating = dt.addChild(addNumericItem(Sprites.Stars.toArray(), Translation.get("maxRating"), 0, 5, 5, 0.5f));
            minFavPoints = dt.addChild(addIntegerItem(Sprites.getSprite(Sprites.IconName.FavPoi), Translation.get("minFavPoints")));
            maxFavPoints = dt.addChild(addIntegerItem(Sprites.getSprite(Sprites.IconName.FavPoi), Translation.get("maxFavPoints")));


            // add CacheTypes
            cacheTypes = addTitleItem("Cache Types");
            //add selectAllCacheTypes/selectNoCacheTypes button item
            FilterSetListViewItem setAllCacheTypes = addItem(null, "", SELECT_ALL_ITEM);
            setAllCacheTypes.setSelectAllHandler(new ISelectAllHandler() {
                @Override
                public void selectAllCacheTypes() {
                    for (int i = 0; i < cacheTypes.getChildLength(); i++) {
                        FilterSetListViewItem itm = cacheTypes.getChild(i);
                        itm.setChecked();
                    }
                }

                @Override
                public void selectNoCacheTypes() {
                    for (int i = 0; i < cacheTypes.getChildLength(); i++) {
                        FilterSetListViewItem itm = cacheTypes.getChild(i);
                        itm.unCheck();
                    }
                }
            });
            cacheTypes.addChild(setAllCacheTypes);
            for (GeoCacheType c : GeoCacheType.caches()) {
                cacheTypes.addChild(addCacheTypeItem(c)).setChecked();
            }
            attributes = addTitleItem("Attribute");
            for (int i = 1; i < Attribute.values().length; i++) {
                attributes.addChild(addItem(Sprites.getSprite("att-" + i + "-1Icon"), Translation.get("att_" + i + "_1"), THREE_STATE_ITEM));
            }
        }

        private FilterSetListViewItem addTitleItem(String name) {
            if (filterSetEntries == null) {
                filterSetEntries = new ArrayList<>();
                filterSetListViewItems = new ArrayList<>();
            }
            FilterSetEntry tmp = new FilterSetEntry(name, null, COLLAPSE_BUTTON_ITEM);
            filterSetEntries.add(tmp);
            FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.itemRec, index++, tmp);
            filterSetListViewItems.add(v);
            v.setClickHandler((v1, x, y, pointer, button) -> {
                // only one or none should be active
                if (activeCollapseButton == null) {
                    activeCollapseButton = (FilterSetListViewItem) v1;
                } else {
                    if (activeCollapseButton == v1) {
                        // active one clicked
                        activeCollapseButton = null;
                    } else {
                        // not the active clicked, close the active one
                        collapseButton_Clicked(activeCollapseButton);
                        // prepare open the clicked one
                        activeCollapseButton = (FilterSetListViewItem) v1;
                    }
                }
                collapseButton_Clicked((FilterSetListViewItem) v1);
                return false;
            });
            return v;
        }

        private FilterSetListViewItem addItem(Sprite icon, String name, int itemType) {
            if (filterSetEntries == null) {
                filterSetEntries = new ArrayList<>();
                filterSetListViewItems = new ArrayList<>();
            }
            FilterSetEntry tmp = new FilterSetEntry(name, icon, itemType);
            filterSetEntries.add(tmp);
            FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.itemRec, index++, tmp);
            v.setInvisible();
            filterSetListViewItems.add(v);
            return v;
        }

        private FilterSetListViewItem addNumericItem(Sprite[] Icons, String Name, double i, double j, double k, double f) {
            if (filterSetEntries == null) {
                filterSetEntries = new ArrayList<>();
                filterSetListViewItems = new ArrayList<>();
            }
            //String Name, Sprite[] Icons, int itemType, double min = i, double max = j, double iniValue = k, double Step = f
            FilterSetListViewItem.FilterSetEntry tmp = new FilterSetListViewItem.FilterSetEntry(Name, Icons, NUMERIC_ITEM, i, j, k, f);
            filterSetEntries.add(tmp);
            FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.itemRec, index++, tmp);
            // initial mit GONE
            v.setInvisible();
            filterSetListViewItems.add(v);
            return v;
        }

        private FilterSetListViewItem addIntegerItem(Sprite icon, String name) {
            if (filterSetEntries == null) {
                filterSetEntries = new ArrayList<>();
                filterSetListViewItems = new ArrayList<>();
            }
            FilterSetListViewItem.FilterSetEntry tmp = new FilterSetListViewItem.FilterSetEntry(name, icon, NUMERIC_INT_ITEM, -1, 10000, 0, 1.0);
            filterSetEntries.add(tmp);
            FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.itemRec, index++, tmp);
            v.setInvisible();
            filterSetListViewItems.add(v);
            return v;
        }

        private FilterSetListViewItem addCacheTypeItem(GeoCacheType cacheType) {
            Sprite icon = Sprites.getSprite("big" + cacheType.name());
            String name = cacheType.name();
            if (filterSetEntries == null) {
                filterSetEntries = new ArrayList<>();
                filterSetListViewItems = new ArrayList<>();
            }
            FilterSetEntry tmp = new FilterSetEntry(cacheType, name, icon, CHECK_ITEM);
            filterSetEntries.add(tmp);
            FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.itemRec, index++, tmp);
            v.setInvisible();
            filterSetListViewItems.add(v);
            return v;
        }

        private void collapseButton_Clicked(FilterSetListViewItem item) {
            item.toggleChildViewState();
            notifyDataSetChanged();
            invalidate();
        }

        @Override
        public boolean onTouchDown(int x, int y, int pointer, int button) {
            super.onTouchDown(x, y, pointer, button);
            synchronized (childs) {
                for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext(); ) {
                    GL_View_Base view = iterator.next();
                    if (view.isVisible()) {
                        if (view.contains(x, y)) {
                            ((FilterSetListViewItem) view).lastItemTouchPos = new Vector2(x - view.getX(), y - view.getY());
                            return true; // only one item is clicked
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
            super.onTouchDragged(x, y, pointer, KineticPan);
            for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext(); ) {
                GL_View_Base view = iterator.next();
                ((FilterSetListViewItem) view).lastItemTouchPos = null;
            }
            return false;
        }

        private class FilterSetAdapter implements Adapter {
            @Override
            public int getCount() {
                return filterSetEntries.size();
            }

            @Override
            public ListViewItemBase getView(int position) {
                FilterSetListViewItem v = filterSetListViewItems.get(position);
                if (!v.isVisible())
                    return null;
                return v;
            }

            @Override
            public float getItemSize(int position) {
                FilterSetListViewItem v = filterSetListViewItems.get(position);
                if (!v.isVisible())
                    return 0;
                return v.getHeight();
            }
        }

    }

    private static class CategoryListView extends V_ListView {

        private ArrayList<CategoryEntry> categoryEntries;
        private ArrayList<CategoryListViewItem> categoryListViewItems;

        CategoryListView(CB_RectF rec) {
            super(rec, "");
            setHasInvisibleItems();
            fillCategoryList();
            setDisposeFlag(false);
            setAdapter(null);
            setAdapter(new CategoryEntryAdapter(categoryEntries, categoryListViewItems));
        }

        FilterProperties updateFilterProperties(FilterProperties filter) {
            // Set Category State
            if (categoryListViewItems != null) {
                for (CategoryListViewItem tmp : categoryListViewItems) {
                    GpxFilename file = tmp.categoryEntry.getFile();
                    for (int i = 0, n = CoreData.categories.size(); i < n; i++) {
                        Category cat = CoreData.categories.get(i);
                        int index = cat.indexOf(file);
                        if (index != -1) {
                            cat.get(index).checked = tmp.categoryEntry.getState() == 1;
                        } else {
                            if (tmp.getCategoryEntry().getCat() != null) {
                                if (cat == tmp.getCategoryEntry().getCat()) {
                                    cat.pinned = tmp.getCategoryEntry().getCat().pinned;
                                }
                            }
                        }
                    }
                }
            }
            return CoreData.categories.updateFilterProperties(filter);
        }

        private void fillCategoryList() {

            CoreData.categories.readFromFilter(tmpFilterProps);

            int Index = 0;

            for (int i = 0, n = CoreData.categories.size(); i < n; i++) {
                Category cat = CoreData.categories.get(i);
                CategoryListViewItem CollapseItem = addCategoryCollapseItem(Index++, Sprites.getSprite(Sprites.IconName.docIcon.name()), cat);

                for (GpxFilename File : cat) {
                    CollapseItem.addChild(addCategoryItem(Index++, Sprites.getSprite(Sprites.IconName.docIcon.name()), File));
                }
            }

            // lCategories is filled now we set the checked attr
            if (categoryEntries != null) {
                for (CategoryEntry tmp : categoryEntries) {
                    GpxFilename file = tmp.getFile();
                    if (file != null) {
                        tmp.setState(file.checked ? 1 : 0);
                    }

                }
            }

        }

        private CategoryListViewItem addCategoryItem(int Index, Sprite Icon, GpxFilename file) {
            if (categoryEntries == null) {
                categoryEntries = new ArrayList<>();
                categoryListViewItems = new ArrayList<>();
            }
            CategoryEntry tmp = new CategoryEntry(file, Icon, CHECK_ITEM);
            categoryEntries.add(tmp);
            CategoryListViewItem v = new CategoryListViewItem(EditFilterSettings.itemRec, Index, tmp);
            v.setInvisible();
            v.setClickHandler(this::onCategoryListViewItemClicked);
            categoryListViewItems.add(v);
            return v;
        }

        private CategoryListViewItem addCategoryCollapseItem(int Index, Sprite Icon, Category cat) {
            if (categoryEntries == null) {
                categoryEntries = new ArrayList<>();
                categoryListViewItems = new ArrayList<>();
            }
            CategoryEntry tmp = new CategoryEntry(cat, Icon, COLLAPSE_BUTTON_ITEM);
            categoryEntries.add(tmp);

            CategoryListViewItem v = new CategoryListViewItem(EditFilterSettings.itemRec, Index, tmp);
            categoryListViewItems.add(v);

            v.setClickHandler((v1, X, Y, pointer, button) -> {
                CB_RectF hitRec = new CB_RectF(v1);
                hitRec.setY(0);

                CB_RectF plusBtnHitRec = new CB_RectF(hitRec.getWidth() - hitRec.getHeight(), 0, hitRec.getHeight(), hitRec.getMaxY());
                CB_RectF minusBtnHitRec = new CB_RectF(hitRec.getX(), 0, hitRec.getHeight(), hitRec.getMaxY());

                float lastTouchX = ((CategoryListViewItem) v1).lastItemTouchPos.x;
                float lastTouchY = ((CategoryListViewItem) v1).lastItemTouchPos.y;

                if (((CategoryListViewItem) v1).getCategoryEntry().getItemType() == COLLAPSE_BUTTON_ITEM) {
                    if (plusBtnHitRec.contains(lastTouchX, lastTouchY)) {
                        ((CategoryListViewItem) v1).plusClick();
                        if (categoryEntries != null) {
                            for (CategoryEntry tmp1 : categoryEntries) {
                                GpxFilename file = tmp1.getFile();
                                if (file != null) {
                                    tmp1.setState(file.checked ? 1 : 0);
                                }
                            }
                        }
                        // updateFilterProperties();
                    } else if (minusBtnHitRec.contains(lastTouchX, lastTouchY)) {
                        ((CategoryListViewItem) v1).minusClick();
                        // updateFilterProperties();
                    } else {
                        collapseButton_Clicked((CategoryListViewItem) v1);
                        notifyDataSetChanged();
                    }
                    /*
                } else {
                    if (plusBtnHitRec.contains(lastTouchX, lastTouchY)) {
                        // updateFilterProperties();
                    }
                 */
                }
                return true;
            });

            return v;
        }

        private void collapseButton_Clicked(CategoryListViewItem item) {
            item.toggleChildViewState();
            notifyDataSetChanged();
            invalidate();
        }

        @Override
        public boolean onTouchDown(int x, int y, int pointer, int button) {

            super.onTouchDown(x, y, pointer, button);
            synchronized (childs) {
                for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext(); ) {

                    GL_View_Base view = iterator.next();

                    // Invisible Views can not be clicked!
                    if (!view.isVisible())
                        continue;
                    if (view.contains(x, y)) {

                        ((CategoryListViewItem) view).lastItemTouchPos = new Vector2(x - view.getX(), y - view.getY());

                    }

                }
            }

            return true;
        }

        private boolean onCategoryListViewItemClicked(GL_View_Base v1, int lastTouchX, int lastTouchY, int pointer, int button) {
            CB_RectF hitRec = new CB_RectF(v1);
            hitRec.setY(0);

            CB_RectF plusBtnHitRec = new CB_RectF(hitRec.getWidth() - hitRec.getHeight(), 0, hitRec.getHeight(), hitRec.getMaxY());
            CB_RectF minusBtnHitRec = new CB_RectF(hitRec.getX(), 0, hitRec.getHeight(), hitRec.getMaxY());

            float lastItemTouchX = ((CategoryListViewItem) v1).lastItemTouchPos.x;
            float lastItemTouchY = ((CategoryListViewItem) v1).lastItemTouchPos.y;

            if (plusBtnHitRec.contains(lastItemTouchX, lastItemTouchY)) {
                ((CategoryListViewItem) v1).plusClick();
                if (categoryEntries != null) {
                    for (CategoryEntry tmp1 : categoryEntries) {
                        GpxFilename file1 = tmp1.getFile();
                        if (file1 != null) {
                            tmp1.setState(file1.checked ? 1 : 0);
                        }

                    }
                }
                // updateFilterProperties();
            } else if (minusBtnHitRec.contains(lastItemTouchX, lastItemTouchY)) {
                ((CategoryListViewItem) v1).minusClick();
                // updateFilterProperties();
            }

            // updateFilterProperties();

            return true;
        }

        private static class CategoryEntry {
            private final GpxFilename mFile;
            private final int mItemType;
            private final Category mCat;
            private final Sprite mIcon;
            private int mState = 0;

            // itemType is always CHECK_ITEM -> GpxFilename mFile
            CategoryEntry(GpxFilename file, Sprite Icon, int itemType) {
                mCat = null;
                mFile = file;
                mIcon = Icon;
                mItemType = itemType;
            }

            // itemType is always COLLAPSE_BUTTON_ITEM -> Category mCat
            CategoryEntry(Category cat, Sprite Icon, int itemType) {
                mCat = cat;
                mFile = null;
                mIcon = Icon;
                mItemType = itemType;
            }

            public GpxFilename getFile() {
                return mFile;
            }

            public Sprite getIcon() {
                return mIcon;
            }

            public int getState() {
                return mState;
            }

            public void setState(int State) {
                mState = State;
            }

            int getItemType() {
                return mItemType;
            }

            void plusClick() {
                if (mItemType == COLLAPSE_BUTTON_ITEM && mCat != null) {
                    // collapse Button chk clicked
                    int checked = mCat.getCheckState();
                    if (checked == 0) {
                        // none selected, so check all
                        for (GpxFilename tmp : mCat) {
                            tmp.checked = true;
                        }
                    } else {
                        // one or more selected, so check none
                        for (GpxFilename tmp : mCat) {
                            tmp.checked = false;
                        }
                    }
                } else {
                    stateClick();
                }
            }

            void minusClick() {
                if (mItemType == COLLAPSE_BUTTON_ITEM && mCat != null) {
                    CategoryDAO.getInstance().setPinned(mCat, !mCat.pinned);
                }
            }

            void stateClick() {
                mState += 1;
                if (mItemType == CHECK_ITEM || mItemType == COLLAPSE_BUTTON_ITEM) {
                    if (mState > 1)
                        mState = 0;
                } else if (mItemType == THREE_STATE_ITEM) {
                    if (mState > 1)
                        mState = -1;
                }

                if (mItemType == CHECK_ITEM) {
                    if (mFile != null)
                        mFile.checked = mState != 0;
                }
            }

            String getCatName() {
                if (mCat != null)
                    return mCat.gpxFileName;
                return "";
            }

            Category getCat() {
                return mCat;
            }

        }

        private static class CategoryEntryAdapter implements Adapter {

            private final ArrayList<CategoryEntry> categoryEntries;
            private final ArrayList<CategoryListViewItem> categoryListViewItems;

            CategoryEntryAdapter(ArrayList<CategoryEntry> categoryEntries, ArrayList<CategoryListViewItem> categoryListViewItems) {
                this.categoryEntries = categoryEntries;
                this.categoryListViewItems = categoryListViewItems;
            }

            @Override
            public int getCount() {
                if (categoryEntries == null)
                    return 0;
                return categoryEntries.size();
            }

            @Override
            public ListViewItemBase getView(int position) {
                if (categoryListViewItems == null)
                    return null;
                CategoryListViewItem v = categoryListViewItems.get(position);
                if (!v.isVisible())
                    return null;

                return v;
            }

            @Override
            public float getItemSize(int position) {
                return EditFilterSettings.itemRec.getHeight();
            }
        }

        private static class CategoryListViewItem extends ListViewItemBackground {
            private final SimpleDateFormat postFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm ", Locale.US);
            // static Member
            private final ArrayList<CategoryListViewItem> mChildList = new ArrayList<>();
            CategoryEntry categoryEntry;
            Vector2 lastItemTouchPos;
            // private Member
            float left;
            float top;
            private Sprite chkOff;
            private Sprite chkOn;
            private Sprite chkNo;
            private Sprite chkBox;
            private CB_RectF lPinBounds;
            private CB_RectF rBounds;
            private CB_RectF rChkBounds;

            // Draw Methods
            private NinePatch btnBack;
            private NinePatch btnBack_pressed;
            private Sprite sPinOn;
            private Sprite sPinOff;
            private float margin = 0;
            private BitmapFontCache EntryName;
            private BitmapFontCache EntryDate;
            private BitmapFontCache EntryCount;

            CategoryListViewItem(CB_RectF rec, int Index, CategoryEntry fne) {
                super(rec, Index, "");

                categoryEntry = fne;

            }

            CategoryEntry getCategoryEntry() {
                return categoryEntry;
            }

            public CategoryListViewItem addChild(CategoryListViewItem item) {
                mChildList.add(item);
                return item;
            }

            void toggleChildViewState() {
                if (mChildList != null && mChildList.size() > 0) {
                    boolean newState = !mChildList.get(0).isVisible();

                    for (CategoryListViewItem tmp : mChildList) {
                        tmp.setVisible(newState);
                    }
                }

            }

            @Override
            protected void render(Batch batch) {
                if (categoryEntry.getItemType() != COLLAPSE_BUTTON_ITEM)
                    super.render(batch);

                if (isPressed) {
                    isPressed = GL_Input.that.getIsTouchDown();
                }

                // initial
                left = getLeftWidth();
                top = getHeight() - getTopHeight();

                if (rBounds == null || rChkBounds == null || lPinBounds == null) {
                    rBounds = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);// =
                    // right
                    // Button
                    // bounds
                    float halfSize = rBounds.getWidth() / 4;

                    rChkBounds = rBounds.scaleCenter(0.8f);
                    lPinBounds = new CB_RectF(rChkBounds);
                    lPinBounds.offset(-(getWidth() - (halfSize * 2) - rChkBounds.getWidth()), 0);
                }

                switch (categoryEntry.getItemType()) {
                    case COLLAPSE_BUTTON_ITEM:
                        drawCollapseButtonItem(batch);
                        break;
                    case CHECK_ITEM:
                        drawChkItem(batch);
                        break;
                    case THREE_STATE_ITEM:
                        drawThreeStateItem(batch);
                        break;

                }
                // draw Name
                if (EntryName == null) {

                    GpxFilename file = categoryEntry.getFile();

                    String Name;
                    String Date;
                    String Count;

                    if (file != null) {
                        Name = file.gpxFileName;
                        Date = postFormat.format(file.importedDate);
                        Count = String.valueOf(file.numberOfGeocaches);
                    } else {
                        Name = categoryEntry.getCatName();
                        Date = postFormat.format(categoryEntry.getCat().LastImported());
                        Count = String.valueOf(categoryEntry.getCat().CacheCount());
                    }

                    Count += " Caches";

                    EntryName = new BitmapFontCache(Fonts.getNormal());
                    EntryName.setColor(COLOR.getFontColor());
                    EntryName.setText(Name, left + UiSizes.getInstance().getMargin(), top);

                    top = margin + margin + Fonts.measureForSmallFont(Count).height;

                    EntryDate = new BitmapFontCache(Fonts.getSmall());
                    EntryDate.setColor(COLOR.getFontColor());
                    EntryDate.setText(Date, left + UiSizes.getInstance().getMargin(), top);

                    float measure = Fonts.measure(Count).width;
                    EntryCount = new BitmapFontCache(Fonts.getSmall());
                    EntryCount.setColor(COLOR.getFontColor());
                    EntryCount.setText(Count, rBounds.getX() - margin - measure, top);

                }

                if (EntryName != null)
                    EntryName.draw(batch);
                if (EntryCount != null)
                    EntryCount.draw(batch);
                if (EntryDate != null)
                    EntryDate.draw(batch);

                // draw Count
                // ActivityUtils.drawStaticLayout(batch, layoutEntryCount, left, top);

                // draw Import Date
                top += 52;
                // ActivityUtils.drawStaticLayout(batch, layoutEntryDate, left, top);

            }

            private void drawCollapseButtonItem(Batch batch) {

                if (isPressed) {
                    if (btnBack_pressed == null) {
                        btnBack_pressed = new NinePatch(Sprites.getSprite("btn-pressed"), 16, 16, 16, 16);
                    }

                    btnBack_pressed.draw(batch, 0, 0, getWidth(), getHeight());

                } else {
                    if (btnBack == null) {
                        btnBack = new NinePatch(Sprites.getSprite(Sprites.IconName.btnNormal.name()), 16, 16, 16, 16);
                    }

                    btnBack.draw(batch, 0, 0, getWidth(), getHeight());

                }

                drawPin(batch);
                drawChkItem(batch);

            }

            private void drawPin(Batch batch) {
                margin = UiSizes.getInstance().getMargin();
                float iconHeight = getHeight() * 0.6f;
                float iconWidth = getHeight() * 0.6f;

                if (getCategoryEntry().getCat().pinned) {
                    if (sPinOn == null) {
                        sPinOn = Sprites.getSprite("pin-icon");
                        sPinOn.setBounds(left, UiSizes.getInstance().getMargin(), iconWidth, iconHeight);
                    }

                    sPinOn.draw(batch);
                } else {
                    if (sPinOff == null) {
                        sPinOff = Sprites.getSprite("pin-icon-disable");
                        sPinOff.setBounds(left, UiSizes.getInstance().getMargin(), iconWidth, iconHeight);
                    }
                    sPinOff.draw(batch);

                }

                left += iconWidth + UiSizes.getInstance().getMargin();

            }

            private void drawChkItem(Batch batch) {
                if (categoryEntry == null)
                    return;

                drawRightChkBox(batch);

                int ChkState;
                if (categoryEntry.getItemType() == COLLAPSE_BUTTON_ITEM) {
                    ChkState = categoryEntry.getCat().getCheckState();
                } else {
                    ChkState = categoryEntry.getState();
                }

                if (ChkState == 1) {
                    if (chkOn == null) {
                        chkOn = Sprites.getSprite("check-on");
                        chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
                    }
                    chkOn.draw(batch);
                } else if (ChkState == -1) {
                    if (chkOff == null) {
                        chkOff = Sprites.getSprite("check-disable");
                        chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
                    }
                    chkOff.draw(batch);
                }
            }

            private void drawThreeStateItem(Batch batch) {
                drawRightChkBox(batch);

                if (categoryEntry.getCat().getCheckState() == 1) {
                    if (chkOn == null) {
                        chkOn = Sprites.getSprite("check-on");
                        chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
                    }

                    chkOn.draw(batch);
                } else if (categoryEntry.getCat().getCheckState() == 0) {
                    if (chkNo == null) {
                        chkNo = Sprites.getSprite(Sprites.IconName.DELETE.name());
                        chkNo.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
                    }
                    chkNo.draw(batch);
                }
            }

            private void drawRightChkBox(Batch batch) {

                if (rBounds == null || rChkBounds == null) {
                    rBounds = new CB_RectF(getWidth() - getHeight() - margin, margin, getHeight() - margin, getHeight() - margin);// = right Button
                    // bounds

                    rChkBounds = rBounds.scaleCenter(0.8f);
                }

                if (chkBox == null) {
                    chkBox = Sprites.getSprite("check-off");

                    chkBox.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

                }

                chkBox.draw(batch);

            }

            void plusClick() {
                categoryEntry.plusClick();
            }

            void minusClick() {
                categoryEntry.minusClick();
            }

            public void setValue(int value) {
                categoryEntry.setState(value);
            }

            public void setValue(boolean b) {
                categoryEntry.setState(b ? 1 : 0);
            }

            @Override
            public CategoryListViewItem getChild(int i) {
                return mChildList.get(i);
            }

            public boolean getBoolean() {
                return categoryEntry.getState() != 0;
            }

        }

    }

}
