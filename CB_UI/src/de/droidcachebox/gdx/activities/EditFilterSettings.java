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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.*;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreSettingsForward;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.MultiToggleButton.OnStateChangeListener;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.dialogs.WaitDialog;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBox.OnMsgBoxClickListener;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.main.menuBtn3.ShowMap;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.settings.SettingString;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import static de.droidcachebox.locator.map.MapViewBase.INITIAL_WP_LIST;

public class EditFilterSettings extends ActivityBase {
    private static final String log = "EditFilterSettings";
    public static EditFilterSettings that;
    public static CB_RectF ItemRec;
    public static FilterProperties tmpFilterProps;
    static WaitDialog pd;
    private static FilterProperties props;
    public PresetListView mPresetListView;
    private MultiToggleButton btPre;
    private MultiToggleButton btSet;
    private MultiToggleButton btCat;
    private MultiToggleButton btTxt;
    private Box contentBox;
    private FilterSetListView mFilterSetListView;
    private CategorieListView mCategorieListView;
    private TextFilterView mTextFilterView;
    private CB_Button btnAddPreset;
    private CB_RectF ListViewRec;

    public EditFilterSettings(CB_RectF rec, String Name) {
        super(rec, Name);
        that = this;
        ItemRec = new CB_RectF(leftBorder, 0, innerWidth, UiSizes.getInstance().getButtonHeight() * 1.1f);

        tmpFilterProps = FilterInstances.getLastFilter();

        float myWidth = this.getWidth() - leftBorder;

        CB_Button bOK = new CB_Button(leftBorder / 2, leftBorder, myWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Button");

        bOK.addClickHandler((v, x, y, pointer, button) -> {
            finish();

            Timer t = new Timer();
            TimerTask postTask = new TimerTask() {

                @Override
                public void run() {
                    mCategorieListView.SetCategory();
                    FilterInstances.setLastFilter(tmpFilterProps);

                    // Text Filter ?
                    String txtFilter = mTextFilterView.getFilterString();
                    if (txtFilter.length() > 0) {
                        int FilterMode = mTextFilterView.getFilterState();
                        if (FilterMode == 0)
                            FilterInstances.getLastFilter().filterName = txtFilter;
                        else if (FilterMode == 1)
                            FilterInstances.getLastFilter().filterGcCode = txtFilter;
                        else if (FilterMode == 2)
                            FilterInstances.getLastFilter().filterOwner = txtFilter;
                    } else {
                        FilterInstances.getLastFilter().filterName = "";
                        FilterInstances.getLastFilter().filterGcCode = "";
                        FilterInstances.getLastFilter().filterOwner = "";
                    }

                    ApplyFilter(FilterInstances.getLastFilter());

                    // Save selected filter (new JSON Format)
                    // wont save History at the Moment
                    // Marker must be removed, else isFiltered is shown
                    // wont change the LastFilter
                    if (FilterInstances.getLastFilter().isHistory) {
                        FilterProperties tmp = new FilterProperties(FilterInstances.getLastFilter().toString());
                        tmp.isHistory = false;
                        Config.FilterNew.setValue(tmp.toString());
                    } else {
                        Config.FilterNew.setValue(FilterInstances.getLastFilter().toString());
                    }
                    Config.AcceptChanges();
                }
            };

            t.schedule(postTask, 300);

            return true;
        });

        this.addChild(bOK);

        CB_Button bCancel = new CB_Button(bOK.getMaxX(), leftBorder, myWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Button");

        bCancel.addClickHandler((v, x, y, pointer, button) -> {
            finish();
            return true;
        });

        this.addChild(bCancel);

        float topButtonY = this.getHeight() - leftBorder - UiSizes.getInstance().getButtonHeight();

        contentBox = new Box(new CB_RectF(0, bOK.getMaxY(), this.getWidth(), topButtonY - bOK.getMaxY()), "contentBox");
        contentBox.setBackground(Sprites.activityBackground);
        this.addChild(contentBox);

        CB_RectF MTBRec = new CB_RectF(leftBorder / 2, topButtonY, myWidth / 4, UiSizes.getInstance().getButtonHeight());

        btPre = new MultiToggleButton(MTBRec, "btPre");
        btSet = new MultiToggleButton(MTBRec, "btSet");
        btCat = new MultiToggleButton(MTBRec, "btCat");
        btTxt = new MultiToggleButton(MTBRec, "btTxt");

        // btPre.setX(leftBorder);
        btSet.setX(btPre.getMaxX());
        btCat.setX(btSet.getMaxX());
        btTxt.setX(btCat.getMaxX());

        this.addChild(btPre);
        this.addChild(btSet);
        this.addChild(btCat);
        this.addChild(btTxt);

        String sPre = Translation.get("preset");
        String sSet = Translation.get("setting");
        String sCat = Translation.get("category");
        String sTxt = Translation.get("text");

        btPre.initialOn_Off_ToggleStates(sPre, sPre);
        btSet.initialOn_Off_ToggleStates(sSet, sSet);
        btCat.initialOn_Off_ToggleStates(sCat, sCat);
        btTxt.initialOn_Off_ToggleStates(sTxt, sTxt);

        btPre.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchVisibility(0);
                return true;
            }
        });

        btSet.addClickHandler((v, x, y, pointer, button) -> {
            switchVisibility(1);
            return true;
        });

        btCat.addClickHandler((v, x, y, pointer, button) -> {
            switchVisibility(2);
            return true;
        });

        btPre.setOnStateChangedListener((v, State) -> {
            if (State == 1)
                switchVisibility(0);
        });

        btSet.setOnStateChangedListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(GL_View_Base v, int State) {
                if (State == 1)
                    switchVisibility(1);
            }
        });
        btCat.setOnStateChangedListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(GL_View_Base v, int State) {
                if (State == 1)
                    switchVisibility(2);
            }
        });
        btTxt.setOnStateChangedListener((v, State) -> {
            if (State == 1)
                switchVisibility(3);
        });

        // Translations
        bOK.setText(Translation.get("ok"));
        bCancel.setText(Translation.get("cancel"));

        ListViewRec = new CB_RectF(0, margin, this.getWidth(), btPre.getY() - bOK.getMaxY() - margin - margin);

        initialPresets();
        initialSettings();
        initialCategorieView();
        fillListViews();
        initialTextView();

        switchVisibility(0);

    }

    public static void ApplyFilter(final FilterProperties Props) {

        props = Props;
        pd = WaitDialog.ShowWait(Translation.get("FilterCaches"));

        new Thread(() -> {
            try {
                synchronized (Database.Data.cacheList) {
                    String sqlWhere = props.getSqlWhere(Config.GcLogin.getValue());
                    Log.info(log, "Main.ApplyFilter: " + sqlWhere);
                    Database.Data.cacheList.clear();
                    CacheListDAO cacheListDAO = new CacheListDAO();
                    cacheListDAO.ReadCacheList(Database.Data.cacheList, sqlWhere, false, Config.ShowAllWaypoints.getValue());
                    GlobalCore.checkSelectedCacheValid();
                }
                CacheListChangedListeners.getInstance().cacheListChanged();
                pd.dismis();
                ViewManager.that.filterSetChanged();

                // Notify Map
                ShowMap.getInstance().normalMapView.setNewSettings(INITIAL_WP_LIST);

                // Save selected filter (new JSON Format)
                // wont save History at the Moment
                // Marker must be removed, else isFiltered is shown
                // wont change the LastFilter
                if (FilterInstances.getLastFilter().isHistory) {
                    FilterProperties tmp = new FilterProperties(FilterInstances.getLastFilter().toString());
                    tmp.isHistory = false;
                    Config.FilterNew.setValue(tmp.toString());
                } else {
                    Config.FilterNew.setValue(FilterInstances.getLastFilter().toString());
                }
                Config.AcceptChanges();
            } catch (Exception e) {
                pd.dismis();
            }
        }).start();

    }

    private void initialPresets() {
        CB_RectF rec = new CB_RectF(leftBorder, margin, innerWidth, UiSizes.getInstance().getButtonHeight());
        btnAddPreset = new CB_Button(rec, "AddPresetButon");
        btnAddPreset.setText(Translation.get("AddOwnFilterPreset"));
        btnAddPreset.addClickHandler(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                addUserPreset();
                return true;
            }
        });
        contentBox.addChild(btnAddPreset);

        CB_RectF preRec = new CB_RectF(ListViewRec);
        preRec.setHeight(ListViewRec.getHeight() - UiSizes.getInstance().getButtonHeight() - margin);
        preRec.setY(btnAddPreset.getMaxY() + margin);

        mPresetListView = new PresetListView(preRec);
        contentBox.addChild(mPresetListView);
    }

    private void initialSettings() {
        mFilterSetListView = new FilterSetListView(ListViewRec);
        contentBox.addChild(mFilterSetListView);

    }

    private void initialCategorieView() {
        mCategorieListView = new CategorieListView(ListViewRec);
        contentBox.addChild(mCategorieListView);
    }

    private void initialTextView() {
        mTextFilterView = new TextFilterView(ListViewRec, "TextFilterView");
        contentBox.addChild(mTextFilterView);
    }

    private void fillListViews() {
    }

    private void switchVisibility() {
        if (btPre.getState() == 1) {
            mFilterSetListView.setInvisible();
            mPresetListView.setVisible();
            mCategorieListView.setInvisible();
            mTextFilterView.setInvisible();
            btnAddPreset.setVisible();
            if (mCategorieListView != null)
                mCategorieListView.SetCategory();
            mPresetListView.onShow();
        }

        if (btSet.getState() == 1) {
            mPresetListView.setInvisible();
            mFilterSetListView.setVisible();
            mCategorieListView.setInvisible();
            mTextFilterView.setInvisible();
            btnAddPreset.setInvisible();
            if (mCategorieListView != null)
                mCategorieListView.SetCategory();
            mFilterSetListView.onShow();
        }
        if (btCat.getState() == 1) {
            mPresetListView.setInvisible();
            mFilterSetListView.setInvisible();
            mCategorieListView.setVisible();
            mTextFilterView.setInvisible();
            btnAddPreset.setInvisible();
            mCategorieListView.onShow();
        }
        if (btTxt.getState() == 1) {
            mPresetListView.setInvisible();
            mFilterSetListView.setInvisible();
            mCategorieListView.setInvisible();
            mTextFilterView.setVisible();
            btnAddPreset.setInvisible();
            mTextFilterView.onShow();
        }

    }

    private void switchVisibility(int state) {
        if (state == 0) {
            btPre.setState(1);
            btSet.setState(0);
            btCat.setState(0);
            btTxt.setState(0);
        }
        if (state == 1) {
            btPre.setState(0);
            btSet.setState(1);
            btCat.setState(0);
            btTxt.setState(0);
        }
        if (state == 2) {
            btPre.setState(0);
            btSet.setState(0);
            btCat.setState(1);
            btTxt.setState(0);
        }
        if (state == 3) {
            btPre.setState(0);
            btSet.setState(0);
            btCat.setState(0);
            btTxt.setState(1);
        }

        switchVisibility();
    }

    private void addUserPreset() {
        GL.that.closeActivity();

        // Check if Preset exist
        boolean exist = false;
        String existName = "";
        for (PresetListViewItem v : mPresetListView.mPresetListViewItems) {
            if (v.getEntry().getFilterProperties().equals(tmpFilterProps)) {
                exist = true;
                existName = v.getEntry().getName();
            }
        }

        if (exist) {
            MessageBox.show(Translation.get("PresetExist") + GlobalCore.br + GlobalCore.br + "\"" + existName + "\"", null, MessageBoxButtons.OK, MessageBoxIcon.Warning, new OnMsgBoxClickListener() {

                @Override
                public boolean onClick(int which, Object data) {
                    de.droidcachebox.main.menuBtn1.contextmenus.EditFilterSettings.getInstance().Execute();
                    return true;
                }
            });
            return;
        }

        StringInputBox.Show(WrapType.SINGLELINE, Translation.get("NewUserPreset"), Translation.get("InsNewUserPreset"), "UserPreset", new OnMsgBoxClickListener() {

            @Override
            public boolean onClick(int which, Object data) {
                String text = StringInputBox.editText.getText();
                // Behandle das ergebniss
                switch (which) {
                    case 1: // ok Clicket
                        String uF = Config.UserFilter.getValue();
                        String aktFilter = tmpFilterProps.toString();

                        // Category Filterungen aus Filter entfernen
                        int pos = aktFilter.indexOf("^");
                        int posE = aktFilter.indexOf("\"", pos);
                        String after = aktFilter.substring(posE);
                        aktFilter = aktFilter.substring(0, pos) + after;

                        uF += text + ";" + aktFilter + "#";
                        Config.UserFilter.setValue(uF);
                        Config.AcceptChanges();
                        mPresetListView.fillPresetList();
                        mPresetListView.notifyDataSetChanged();
                        de.droidcachebox.main.menuBtn1.contextmenus.EditFilterSettings.getInstance().Execute();
                        break;
                    case 2: // cancel clicked
                        de.droidcachebox.main.menuBtn1.contextmenus.EditFilterSettings.getInstance().Execute();
                        break;
                    case 3:
                        de.droidcachebox.main.menuBtn1.contextmenus.EditFilterSettings.getInstance().Execute();
                        break;
                }

                return true;
            }
        });
    }

    @Override
    public void onShow() {
        //	tmpFilterProps = FilterInstances.LastFilter;

        if (mPresetListView != null) {
            mPresetListView.notifyDataSetChanged();
        }

        // Load and set TxtFilter
        if (mTextFilterView != null) {
            if (FilterInstances.getLastFilter().filterName.length() > 0)
                mTextFilterView.setFilterString(FilterInstances.getLastFilter().filterName, 0);
            else if (FilterInstances.getLastFilter().filterGcCode.length() > 0)
                mTextFilterView.setFilterString(FilterInstances.getLastFilter().filterGcCode, 1);
            else if (FilterInstances.getLastFilter().filterOwner.length() > 0)
                mTextFilterView.setFilterString(FilterInstances.getLastFilter().filterOwner, 2);
        }
    }

    private static class CategorieListView extends V_ListView {

        static final int COLLAPSE_BUTTON_ITEM = 0;
        static final int CHECK_ITEM = 1;
        static final int THREE_STATE_ITEM = 2;
        static final int NUMERIC_ITEM = 3;
        private final CustomAdapter lvAdapter;
        private ArrayList<CategorieEntry> lCategories;
        private ArrayList<CategorieListViewItem> lCategorieListViewItems;
        OnClickListener onItemClickListener = new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int lastTouchX, int lastTouchY, int pointer, int button) {
                CB_RectF HitRec = v.copy();
                HitRec.setY(0);

                CB_RectF plusBtnHitRec = new CB_RectF(HitRec.getWidth() - HitRec.getHeight(), 0, HitRec.getHeight(), HitRec.getMaxY());
                CB_RectF minusBtnHitRec = new CB_RectF(HitRec.getX(), 0, HitRec.getHeight(), HitRec.getMaxY());

                float lastItemTouchX = ((CategorieListViewItem) v).lastItemTouchPos.x;
                float lastItemTouchY = ((CategorieListViewItem) v).lastItemTouchPos.y;

                if (plusBtnHitRec.contains(lastItemTouchX, lastItemTouchY)) {
                    ((CategorieListViewItem) v).plusClick();
                    if (lCategories != null) {
                        for (CategorieEntry tmp : lCategories) {
                            GpxFilename file = tmp.getFile();
                            if (file != null) {
                                tmp.setState(file.Checked ? 1 : 0);
                            }

                        }
                    }
                    SetCategory();
                } else if (minusBtnHitRec.contains(lastItemTouchX, lastItemTouchY)) {
                    ((CategorieListViewItem) v).minusClick();
                    SetCategory();
                }

                SetCategory();

                return true;
            }
        };

        public CategorieListView(CB_RectF rec) {
            super(rec, "");
            this.setHasInvisibleItems(true);
            fillCategorieList();
            this.setDisposeFlag(false);
            this.setBaseAdapter(null);
            lvAdapter = new CustomAdapter(lCategories, lCategorieListViewItems);
            this.setBaseAdapter(lvAdapter);

        }

        public void SetCategory() {
            // Set Categorie State
            if (lCategorieListViewItems != null) {
                for (CategorieListViewItem tmp : lCategorieListViewItems) {
                    GpxFilename file = tmp.categorieEntry.getFile();

                    for (int i = 0, n = CoreSettingsForward.Categories.size(); i < n; i++) {
                        Category cat = CoreSettingsForward.Categories.get(i);
                        int index = cat.indexOf(file);
                        if (index != -1) {

                            cat.get(index).Checked = (tmp.categorieEntry.getState() == 1) ? true : false;

                        } else {
                            if (tmp.getCategorieEntry().getCat() != null) {
                                if (cat == tmp.getCategorieEntry().getCat()) {
                                    cat.pinned = tmp.getCategorieEntry().getCat().pinned;
                                }

                            }

                        }

                    }

                }
            }
            CoreSettingsForward.Categories.WriteToFilter(de.droidcachebox.gdx.activities.EditFilterSettings.tmpFilterProps);

        }

        private void fillCategorieList() {

            CoreSettingsForward.Categories.ReadFromFilter(de.droidcachebox.gdx.activities.EditFilterSettings.tmpFilterProps);

            int Index = 0;

            for (int i = 0, n = CoreSettingsForward.Categories.size(); i < n; i++) {
                Category cat = CoreSettingsForward.Categories.get(i);
                CategorieListViewItem CollapseItem = addCategorieCollapseItem(Index++, Sprites.getSprite(Sprites.IconName.docIcon.name()), cat, COLLAPSE_BUTTON_ITEM);

                for (GpxFilename File : cat) {
                    CollapseItem.addChild(addCategorieItem(Index++, Sprites.getSprite(Sprites.IconName.docIcon.name()), File, CHECK_ITEM));
                }
            }

            // lCategories is filled now we set the checked attr
            if (lCategories != null) {
                for (CategorieEntry tmp : lCategories) {
                    GpxFilename file = tmp.getFile();
                    if (file != null) {
                        tmp.setState(file.Checked ? 1 : 0);
                    }

                }
            }

        }

        private CategorieListViewItem addCategorieItem(int Index, Sprite Icon, GpxFilename file, int ItemType) {
            if (lCategories == null) {
                lCategories = new ArrayList<>();
                lCategorieListViewItems = new ArrayList<>();
            }
            CategorieEntry tmp = new CategorieEntry(file, Icon, ItemType);
            lCategories.add(tmp);
            CategorieListViewItem v = new CategorieListViewItem(de.droidcachebox.gdx.activities.EditFilterSettings.ItemRec, Index, tmp);
            // inital mit INVISIBLE
            v.setInvisible();
            v.addClickHandler(onItemClickListener);
            lCategorieListViewItems.add(v);
            return v;
        }

        private CategorieListViewItem addCategorieCollapseItem(int Index, Sprite Icon, Category cat, int ItemType) {
            if (lCategories == null) {
                lCategories = new ArrayList<>();
                lCategorieListViewItems = new ArrayList<>();
            }
            CategorieEntry tmp = new CategorieEntry(cat, Icon, ItemType);
            lCategories.add(tmp);

            CategorieListViewItem v = new CategorieListViewItem(de.droidcachebox.gdx.activities.EditFilterSettings.ItemRec, Index, tmp);
            lCategorieListViewItems.add(v);

            v.addClickHandler(new OnClickListener() {

                @Override
                public boolean onClick(GL_View_Base v, int X, int Y, int pointer, int button) {
                    CB_RectF HitRec = v.copy();
                    HitRec.setY(0);

                    CB_RectF plusBtnHitRec = new CB_RectF(HitRec.getWidth() - HitRec.getHeight(), 0, HitRec.getHeight(), HitRec.getMaxY());
                    CB_RectF minusBtnHitRec = new CB_RectF(HitRec.getX(), 0, HitRec.getHeight(), HitRec.getMaxY());

                    float lastTouchX = ((CategorieListViewItem) v).lastItemTouchPos.x;
                    float lastTouchY = ((CategorieListViewItem) v).lastItemTouchPos.y;

                    if (((CategorieListViewItem) v).getCategorieEntry().getItemType() == COLLAPSE_BUTTON_ITEM) {
                        if (plusBtnHitRec.contains(lastTouchX, lastTouchY)) {
                            ((CategorieListViewItem) v).plusClick();
                            if (lCategories != null) {
                                for (CategorieEntry tmp : lCategories) {
                                    GpxFilename file = tmp.getFile();
                                    if (file != null) {
                                        tmp.setState(file.Checked ? 1 : 0);
                                    }

                                }
                            }
                            SetCategory();
                        } else if (minusBtnHitRec.contains(lastTouchX, lastTouchY)) {
                            ((CategorieListViewItem) v).minusClick();
                            SetCategory();
                        } else {
                            collapseButton_Clicked((CategorieListViewItem) v);
                            notifyDataSetChanged();
                        }

                    } else {
                        if (plusBtnHitRec.contains(lastTouchX, lastTouchY)) {
                            SetCategory();
                        }
                    }

                    return true;
                }
            });

            return v;
        }

        private void collapseButton_Clicked(CategorieListViewItem item) {
            item.toggleChildeViewState();
            this.notifyDataSetChanged();
            this.invalidate();
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

                        ((CategorieListViewItem) view).lastItemTouchPos = new Vector2(x - view.getX(), y - view.getY());

                    }

                }
            }

            return true;
        }

        public class CustomAdapter implements Adapter {

            private final ArrayList<CategorieEntry> categorieList;
            private final ArrayList<CategorieListViewItem> lCategoriesListViewItems;

            public CustomAdapter(ArrayList<CategorieEntry> lCategories, ArrayList<CategorieListViewItem> CategorieListViewItems) {
                this.categorieList = lCategories;
                this.lCategoriesListViewItems = CategorieListViewItems;
            }

            @Override
            public int getCount() {
                if (categorieList == null)
                    return 0;
                return categorieList.size();
            }

            public Object getItem(int position) {
                if (categorieList == null)
                    return null;
                return categorieList.get(position);
            }

            public long getItemId(int position) {
                return position;
            }

            @Override
            public ListViewItemBase getView(int position) {
                if (lCategoriesListViewItems == null)
                    return null;
                CategorieListViewItem v = lCategoriesListViewItems.get(position);
                if (!v.isVisible())
                    return null;

                return v;
            }

            @Override
            public float getItemSize(int position) {
                return de.droidcachebox.gdx.activities.EditFilterSettings.ItemRec.getHeight();
            }
        }

        public class CategorieListViewItem extends ListViewItemBackground {
            private final SimpleDateFormat postFormater = new SimpleDateFormat("dd/MM/yyyy hh:mm ");
            private Sprite chkOff;
            private Sprite chkOn;
            private Sprite chkNo;
            private Sprite chkBox;
            private CB_RectF lPinBounds;
            private CB_RectF rBounds;
            private CB_RectF rChkBounds;
            private float halfSize = 0;
            private NinePatch btnBack;
            private NinePatch btnBack_pressed;
            private Sprite sPinOn;
            private Sprite sPinOff;
            private float margin = 0;

            // Draw Methods

            // static Member
            private final ArrayList<CategorieListViewItem> mChildList = new ArrayList<>();
            public CategorieEntry categorieEntry;
            public Vector2 lastItemTouchPos;
            // private Member
            float left;
            float top;
            private BitmapFontCache EntryName;
            private BitmapFontCache EntryDate;
            private BitmapFontCache EntryCount;

            public CategorieListViewItem(CB_RectF rec, int Index, CategorieEntry fne) {
                super(rec, Index, "");

                this.categorieEntry = fne;

            }

            public CategorieEntry getCategorieEntry() {
                return categorieEntry;
            }

            public CategorieListViewItem addChild(CategorieListViewItem item) {
                mChildList.add(item);
                return item;
            }

            public void toggleChildeViewState() {
                if (mChildList != null && mChildList.size() > 0) {
                    boolean newState = !mChildList.get(0).isVisible();

                    for (CategorieListViewItem tmp : mChildList) {
                        tmp.setVisible(newState);
                    }
                }

            }

            @Override
            protected void render(Batch batch) {
                if (this.categorieEntry.getItemType() != FilterSetListView.COLLAPSE_BUTTON_ITEM)
                    super.render(batch);

                if (isPressed) {
                    isPressed = GL_Input.that.getIsTouchDown();
                }

                // initial
                left = getLeftWidth();
                top = this.getHeight() - this.getTopHeight();

                if (rBounds == null || rChkBounds == null || lPinBounds == null) {
                    rBounds = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);// =
                    // right
                    // Button
                    // bounds
                    halfSize = rBounds.getWidth() / 4;

                    rChkBounds = rBounds.ScaleCenter(0.8f);
                    lPinBounds = new CB_RectF(rChkBounds);
                    lPinBounds.offset(-(getWidth() - (halfSize * 2) - rChkBounds.getWidth()), 0);
                }

                // boolean selected = false;
                // if (this.categorieEntry == aktCategorieEntry) selected = true;

                switch (categorieEntry.getItemType()) {
                    case FilterSetListView.COLLAPSE_BUTTON_ITEM:
                        drawCollapseButtonItem(batch);
                        break;
                    case FilterSetListView.CHECK_ITEM:
                        drawChkItem(batch);
                        break;
                    case FilterSetListView.THREE_STATE_ITEM:
                        drawThreeStateItem(batch);
                        break;

                }
                // draw Name
                if (EntryName == null) {

                    GpxFilename file = categorieEntry.getFile();

                    String Name = "";
                    String Date = "";
                    String Count = "";

                    if (file != null) {
                        Name = file.GpxFileName;
                        Date = postFormater.format(file.Imported);
                        Count = String.valueOf(file.CacheCount);
                    } else {
                        Name = categorieEntry.getCatName();
                        Date = postFormater.format(categorieEntry.getCat().LastImported());
                        Count = String.valueOf(categorieEntry.getCat().CacheCount());
                    }

                    Count += " Caches";

                    EntryName = new BitmapFontCache(Fonts.getNormal());
                    EntryName.setColor(COLOR.getFontColor());
                    EntryName.setText(Name, left + UiSizes.getInstance().getMargin(), top);

                    top = margin + margin + Fonts.MeasureSmall(Count).height;

                    EntryDate = new BitmapFontCache(Fonts.getSmall());
                    EntryDate.setColor(COLOR.getFontColor());
                    EntryDate.setText(Date, left + UiSizes.getInstance().getMargin(), top);

                    float measure = Fonts.Measure(Count).width;
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

                if (this.isPressed) {
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
                float iconHeight = this.getHeight() * 0.6f;
                float iconWidth = iconHeight;

                if (this.getCategorieEntry().getCat().pinned) {
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
                if (this.categorieEntry == null)
                    return;

                drawIcon(batch);
                drawRightChkBox(batch);

                int ChkState;
                if (this.categorieEntry.getItemType() == FilterSetListView.COLLAPSE_BUTTON_ITEM) {
                    ChkState = this.categorieEntry.getCat().getCheck();
                } else {
                    ChkState = this.categorieEntry.getState();
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
                drawIcon(batch);
                drawRightChkBox(batch);

                if (this.categorieEntry.getCat().getCheck() == 1) {
                    if (chkOn == null) {
                        chkOn = Sprites.getSprite("check-on");
                        chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
                    }

                    chkOn.draw(batch);
                } else if (this.categorieEntry.getCat().getCheck() == 0) {
                    if (chkNo == null) {
                        chkNo = Sprites.getSprite(Sprites.IconName.DELETE.name());
                        chkNo.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
                    }
                    chkNo.draw(batch);
                }
            }

            private void drawIcon(Batch batch) {
                // if (categorieEntry.getIcon() != null) ActivityUtils.PutImageTargetHeight(batch, categorieEntry.getIcon(), left, top,
                // UiSizes.getIconSize());
                // left += UiSizes.getIconAddCorner();

            }

            private void drawRightChkBox(Batch batch) {

                if (rBounds == null || rChkBounds == null) {
                    rBounds = new CB_RectF(getWidth() - getHeight() - margin, margin, getHeight() - margin, getHeight() - margin);// = right Button
                    // bounds

                    rChkBounds = rBounds.ScaleCenter(0.8f);
                }

                if (chkBox == null) {
                    chkBox = Sprites.getSprite("check-off");

                    chkBox.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

                }

                chkBox.draw(batch);

            }

            public void plusClick() {
                this.categorieEntry.plusClick();
            }

            public void minusClick() {
                this.categorieEntry.minusClick();
            }

            public void stateClick() {
                this.categorieEntry.stateClick();
            }

            public int getChecked() {
                return categorieEntry.getState();
            }

            public float getValue() {
                return (float) categorieEntry.getNumState();
            }

            public void setValue(int value) {

                this.categorieEntry.setState(value);

            }

            public void setValue(float value) {
                this.categorieEntry.setState(value);

            }

            public void setValue(boolean b) {
                this.categorieEntry.setState(b ? 1 : 0);
            }

            @Override
            public CategorieListViewItem getChild(int i) {
                return mChildList.get(i);
            }

            public int getChildLength() {
                return mChildList.size();
            }

            public boolean getBoolean() {
                if (categorieEntry.getState() == 0)
                    return false;

                return true;
            }

        }

        public static class CategorieEntry {
            private static int IdCounter;
            private final GpxFilename mFile;
            private final int mItemType;
            private final int ID;
            private Category mCat;
            private Sprite mIcon;
            private Sprite[] mIconArray;
            private int mState = 0;
            private double mNumericMax;
            private double mNumericStep;
            private double mNumericState;

            public CategorieEntry(GpxFilename file, Sprite Icon, int itemType) {
                mCat = null;
                mFile = file;
                mIcon = Icon;
                mItemType = itemType;
                ID = IdCounter++;

            }

            public CategorieEntry(Category cat, Sprite Icon, int itemType) {
                mCat = cat;
                mFile = null;
                mIcon = Icon;
                mItemType = itemType;
                ID = IdCounter++;

            }

            public CategorieEntry(GpxFilename file, Sprite[] Icons, int itemType, double min, double max, double iniValue, double Step) {
                mFile = file;
                mIconArray = Icons;
                mItemType = itemType;
                mNumericMax = max;
                mNumericState = iniValue;
                mNumericStep = Step;
                ID = IdCounter++;
            }

            public GpxFilename getFile() {
                return mFile;
            }

            public Sprite getIcon() {
                if (mItemType == NUMERIC_ITEM) {
                    try {
                        double ArrayMultiplier = (mIconArray.length > 5) ? 2 : 1;

                        return mIconArray[(int) (mNumericState * ArrayMultiplier)];
                    } catch (Exception e) {
                    }

                }
                return mIcon;
            }

            public int getState() {
                return mState;
            }

            public void setState(int State) {
                mState = State;
            }

            public void setState(float State) {
                mNumericState = State;
            }

            public int getItemType() {
                return mItemType;
            }

            public int getID() {
                return ID;
            }

            public double getNumState() {
                return mNumericState;
            }

            public void plusClick() {

                if (mItemType == COLLAPSE_BUTTON_ITEM) {
                    // collabs Button chk clicked
                    int State = mCat.getCheck();
                    if (State == 0) {// keins ausgewählt, also alle anwählen

                        for (GpxFilename tmp : mCat) {
                            tmp.Checked = true;
                        }

                    } else {// einer oder mehr ausgewählt, also alle abwählen

                        for (GpxFilename tmp : mCat) {
                            tmp.Checked = false;
                        }

                    }
                } else {
                    stateClick();
                }

            }

            public void minusClick() {
                if (mItemType == COLLAPSE_BUTTON_ITEM) {
                    // Collabs Button Pin Clicked
                    CategoryDAO dao = new CategoryDAO();
                    dao.SetPinned(this.mCat, !this.mCat.pinned);
                    // this.mCat.pinned = !this.mCat.pinned;

                } else {
                    mNumericState -= mNumericStep;
                    if (mNumericState < 0)
                        mNumericState = mNumericMax;
                }
            }

            public void stateClick() {

                mState += 1;
                if (mItemType == CHECK_ITEM || mItemType == COLLAPSE_BUTTON_ITEM) {
                    if (mState > 1)
                        mState = 0;
                } else if (mItemType == THREE_STATE_ITEM) {
                    if (mState > 1)
                        mState = -1;
                }

                if (mItemType == CHECK_ITEM) {
                    if (mState == 0)
                        this.mFile.Checked = false;
                    else
                        this.mFile.Checked = true;
                }
            }

            public String getCatName() {
                return mCat.GpxFilename;
            }

            public Category getCat() {
                return mCat;
            }

        }

    }

    static final FilterProperties[] presets = new FilterProperties[]{ //
            FilterInstances.HISTORY, //
            FilterInstances.ACTIVE, //
            FilterInstances.QUICK, //
            FilterInstances.BEGINNER, //
            FilterInstances.WITHTB, //
            FilterInstances.DROPTB, //
            FilterInstances.HIGHLIGHTS, //
            FilterInstances.FAVORITES, //
            FilterInstances.TOARCHIVE, //
            FilterInstances.LISTINGCHANGED, //
            FilterInstances.ALL, //
    };

    class PresetListView extends V_ListView {

        public ArrayList<PresetListViewItem> mPresetListViewItems;
        private ArrayList<PresetEntry> mPresetEntries;
        private CustomAdapter lvAdapter;

        public PresetListView(CB_RectF rec) {
            super(rec, "");
            this.setHasInvisibleItems(true);
            fillPresetList();
            this.setDisposeFlag(false);
            this.setBaseAdapter(null);
            lvAdapter = new CustomAdapter(mPresetEntries);
            this.setBaseAdapter(lvAdapter);

        }

        public void fillPresetList() {
            if (mPresetEntries != null)
                mPresetEntries.clear();
            else
                mPresetEntries = new ArrayList<PresetEntry>();
            if (mPresetListViewItems != null)
                mPresetListViewItems.clear();
            else
                mPresetListViewItems = new ArrayList<PresetListViewItem>();

            FilterInstances.HISTORY.isHistory = true;

            mPresetEntriesAdd("HISTORY", "HISTORY", FilterInstances.HISTORY);
            mPresetEntriesAdd("AllCachesToFind", "log0icon", FilterInstances.ACTIVE);
            mPresetEntriesAdd("QuickCaches", "QuickCaches", FilterInstances.QUICK);
            mPresetEntriesAdd("BEGINNER", "BEGINNER", FilterInstances.BEGINNER);
            mPresetEntriesAdd("GrabTB", Sprites.IconName.TBGRAB.name(), FilterInstances.WITHTB);
            mPresetEntriesAdd("DropTB", Sprites.IconName.TBDROP.name(), FilterInstances.DROPTB);
            mPresetEntriesAdd("Highlights", "star", FilterInstances.HIGHLIGHTS);
            mPresetEntriesAdd("Favorites", "favorit", FilterInstances.FAVORITES);
            mPresetEntriesAdd("PrepareToArchive", Sprites.IconName.DELETE.name(), FilterInstances.TOARCHIVE);
            mPresetEntriesAdd("ListingChanged", Sprites.IconName.warningIcon.name(), FilterInstances.LISTINGCHANGED);
            mPresetEntriesAdd("AllCaches", "earth", FilterInstances.ALL);

            // add User Presets from Config.UserFilter
            if (!Config.UserFilter.getValue().equalsIgnoreCase("")) {
                String userEntrys[] = Config.UserFilter.getValue().split(SettingString.STRING_SPLITTER);
                try {
                    for (String entry : userEntrys) {
                        int pos = entry.indexOf(";");
                        String name = entry.substring(0, pos);
                        String filter = entry.substring(pos + 1);
                        if (filter.endsWith("#")) filter = filter.substring(0, filter.length() - 1);
                        FilterProperties fp = new FilterProperties(filter);
                        mPresetEntries.add(new PresetEntry(name, Sprites.getSprite("userdata"), fp));
                    }
                } catch (Exception ex) {
                    Log.err("PresetListView", "", ex);
                }
            }

            fillItemList();
        }

        private void fillItemList() {

            int index = 0;
            for (PresetEntry entry : mPresetEntries) {
                PresetListViewItem v = new PresetListViewItem(ItemRec, index, entry);

                v.addClickHandler(new OnClickListener() {

                    @Override
                    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

                        int itemIndex = ((PresetListViewItem) v).getIndex();

                        for (PresetListViewItem presetListViewItem : mPresetListViewItems) {
                            ((ListViewItemBase) presetListViewItem).isSelected = false;
                        }

                        if (itemIndex < presets.length) {
                            tmpFilterProps = new FilterProperties(presets[itemIndex].toString());
                        } else {
                            // User Preset
                            try {
                                String userEntrys[] = Config.UserFilter.getValue().split(SettingString.STRING_SPLITTER);
                                int i = itemIndex - presets.length;

                                int pos = userEntrys[i].indexOf(";");
                                String filter = userEntrys[i].substring(pos + 1);
                                if (filter.endsWith("#")) filter = filter.substring(0, filter.length() - 1);
                                tmpFilterProps = new FilterProperties(filter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        // reset TxtFilter
                        TextFilterView.that.setFilterString("", 0);
                        return true;

                    }
                });

                v.setOnLongClickListener((v1, x, y, pointer, button) -> {
                    final int delItemIndex = ((PresetListViewItem) v1).getIndex();
                    GL.that.closeActivity();
                    MessageBox.show(Translation.get("?DelUserPreset"), Translation.get("DelUserPreset"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, (which, data) -> {
                        switch (which) {
                            case 1: // ok Clicked

                                if (delItemIndex < presets.length) {
                                    return false; // Don't delete System Presets
                                } else {
                                    try {
                                        String userEntrys[] = Config.UserFilter.getValue().split(SettingString.STRING_SPLITTER);

                                        int i = presets.length;
                                        String newUserEntris = "";
                                        for (String entry1 : userEntrys) {
                                            if (i++ != delItemIndex)
                                                newUserEntris += entry1 + SettingString.STRING_SPLITTER;
                                        }
                                        Config.UserFilter.setValue(newUserEntris);
                                        Config.AcceptChanges();
                                        that.mPresetListView.fillPresetList();
                                        that.mPresetListView.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                                de.droidcachebox.main.menuBtn1.contextmenus.EditFilterSettings.getInstance().Execute();
                                break;
                            case 2: // cancel clicked
                                de.droidcachebox.main.menuBtn1.contextmenus.EditFilterSettings.getInstance().Execute();
                                break;
                            case 3:
                                de.droidcachebox.main.menuBtn1.contextmenus.EditFilterSettings.getInstance().Execute();
                                break;
                        }

                        return true;
                    });

                    return true;
                });

                mPresetListViewItems.add(v);
                index++;
            }
        }

        private void mPresetEntriesAdd(String name, String icon, FilterProperties PresetFilter) {
            mPresetEntries.add(new PresetEntry(Translation.get(name), Sprites.getSprite(icon), PresetFilter));
        }

        @Override
        public void setVisible(boolean On) {
            super.setVisible(On);
            if (On)
                chkIsPreset();
        }

        private void chkIsPreset() {
            for (PresetListViewItem item : mPresetListViewItems) {
                ((ListViewItemBase) item).isSelected = false;
            }

            this.setBaseAdapter(null);
            lvAdapter = new CustomAdapter(mPresetEntries);
            this.setBaseAdapter(lvAdapter);

        }

        public class PresetEntry {
            private final String mName;
            private final Sprite mIcon;
            private FilterProperties filterProperties;

            public PresetEntry(String Name, Sprite Icon, FilterProperties PresetFilter) {
                mName = Name;
                mIcon = Icon;
                // mPresetString = PresetString;
                filterProperties = PresetFilter;
            }

            public String getName() {
                return mName;
            }

            public Sprite getIcon() {
                return mIcon;
            }

            public FilterProperties getFilterProperties() {
                return filterProperties;
            }

            public void setFilterProperties(FilterProperties filterProperties) {
                this.filterProperties = filterProperties;
            }

        }

        public class CustomAdapter implements Adapter {

            private final ArrayList<PresetEntry> presetList;

            public CustomAdapter(ArrayList<PresetEntry> lPresets) {

                this.presetList = lPresets;
            }

            @Override
            public int getCount() {
                return presetList.size();
            }

            public Object getItem(int position) {
                return presetList.get(position);
            }

            public long getItemId(int position) {
                return position;
            }

            @Override
            public ListViewItemBase getView(final int position) {

                ListViewItemBase v = mPresetListViewItems.get(position);

                return v;
            }

            @Override
            public float getItemSize(int position) {
                return de.droidcachebox.gdx.activities.EditFilterSettings.ItemRec.getHeight();
            }
        }

    }


    public class PresetListViewItem extends ListViewItemBackground {
        private final PresetListView.PresetEntry mPresetEntry;
        BitmapFontCache EntryName;
        float left = 0;
        float top = 0;

        public PresetListViewItem(CB_RectF rec, int Index, PresetListView.PresetEntry fne) {
            super(rec, Index, fne.getName());
            this.mPresetEntry = fne;
        }

        @Override
        protected void render(Batch batch) {
            if (this.isDisposed())
                return;

            if (tmpFilterProps != null) {
                if (mPresetEntry.getFilterProperties().equals(tmpFilterProps)) {
                    isSelected = !de.droidcachebox.gdx.activities.EditFilterSettings.tmpFilterProps.isExtendedFilter();
                }
            }

            super.render(batch);

            if (isPressed) {
                isPressed = GL_Input.that.getIsTouchDown();
            }

            // initial
            left = getLeftWidth();
            top = (this.getHeight() + Fonts.getNormal().getLineHeight()) / 2f; //this.getTopHeight();

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
            if (mPresetEntry.getIcon() != null) {
                float iconHeight = this.getHeight() * 0.8f;
                float iconWidth = iconHeight;
                float y = (this.getHeight() - iconHeight) / 2f; // UI_Size_Base.that.getMargin()
                mPresetEntry.getIcon().setBounds(left, y, iconWidth, iconHeight);
                mPresetEntry.getIcon().draw(batch);
                left = left + iconWidth + y + getLeftWidth();
            }
        }

        public PresetListView.PresetEntry getEntry() {
            return mPresetEntry;
        }
    }

}
