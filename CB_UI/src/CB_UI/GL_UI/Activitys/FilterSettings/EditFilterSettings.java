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
package CB_UI.GL_UI.Activitys.FilterSettings;

import CB_Core.CacheListChangedEventList;
import CB_Core.DAO.CacheListDAO;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GlobalCore;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Dialogs.StringInputBox;
import CB_UI_Base.GL_UI.Controls.Dialogs.WaitDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Log.Log;

import java.util.Timer;
import java.util.TimerTask;

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
    private Button btnAddPreset;
    private CB_RectF ListViewRec;

    public EditFilterSettings(CB_RectF rec, String Name) {
        super(rec, Name);
        that = this;
        ItemRec = new CB_RectF(leftBorder, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 1.1f);

        tmpFilterProps = FilterInstances.getLastFilter();

        float myWidth = this.getWidth() - leftBorder;

        Button bOK = new Button(leftBorder / 2, leftBorder, myWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");

        bOK.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
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
            }
        });

        this.addChild(bOK);

        Button bCancel = new Button(bOK.getMaxX(), leftBorder, myWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

        bCancel.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                finish();
                return true;
            }
        });

        this.addChild(bCancel);

        float topButtonY = this.getHeight() - leftBorder - UI_Size_Base.that.getButtonHeight();

        contentBox = new Box(new CB_RectF(0, bOK.getMaxY(), this.getWidth(), topButtonY - bOK.getMaxY()), "contentBox");
        contentBox.setBackground(Sprites.activityBackground);
        this.addChild(contentBox);

        CB_RectF MTBRec = new CB_RectF(leftBorder / 2, topButtonY, myWidth / 4, UI_Size_Base.that.getButtonHeight());

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

        String sPre = Translation.Get("preset");
        String sSet = Translation.Get("setting");
        String sCat = Translation.Get("category");
        String sTxt = Translation.Get("text");

        btPre.initialOn_Off_ToggleStates(sPre, sPre);
        btSet.initialOn_Off_ToggleStates(sSet, sSet);
        btCat.initialOn_Off_ToggleStates(sCat, sCat);
        btTxt.initialOn_Off_ToggleStates(sTxt, sTxt);

        btPre.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchVisibility(0);
                return true;
            }
        });

        btSet.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchVisibility(1);
                return true;
            }
        });

        btCat.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchVisibility(2);
                return true;
            }
        });

        btPre.setOnStateChangedListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(GL_View_Base v, int State) {
                if (State == 1)
                    switchVisibility(0);
            }
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
        btTxt.setOnStateChangedListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(GL_View_Base v, int State) {
                if (State == 1)
                    switchVisibility(3);
            }
        });

        // Translations
        bOK.setText(Translation.Get("ok"));
        bCancel.setText(Translation.Get("cancel"));

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
        pd = WaitDialog.ShowWait(Translation.Get("FilterCaches"));

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (Database.Data.Query) {
                        String sqlWhere = props.getSqlWhere(Config.GcLogin.getValue());
                        Log.info(log, "Main.ApplyFilter: " + sqlWhere);
                        Database.Data.Query.clear();
                        CacheListDAO cacheListDAO = new CacheListDAO();
                        cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
                        GlobalCore.checkSelectedCacheValid();
                    }
                    CacheListChangedEventList.Call();
                    pd.dismis();
                    TabMainView.that.filterSetChanged();

                    // Notify Map
                    if (MapView.that != null)
                        MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);

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
            }

        };

        thread.start();

    }

    private void initialPresets() {
        CB_RectF rec = new CB_RectF(leftBorder, margin, innerWidth, UI_Size_Base.that.getButtonHeight());
        btnAddPreset = new Button(rec, "AddPresetButon");
        btnAddPreset.setText(Translation.Get("AddOwnFilterPreset"));
        btnAddPreset.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                addUserPreset();
                return true;
            }
        });
        contentBox.addChild(btnAddPreset);

        CB_RectF preRec = new CB_RectF(ListViewRec);
        preRec.setHeight(ListViewRec.getHeight() - UI_Size_Base.that.getButtonHeight() - margin);
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
            if (PresetListViewItem.chkPresetFilter(v.getEntry().getFilterProperties(), tmpFilterProps)) {
                exist = true;
                existName = v.getEntry().getName();
            }
        }

        if (exist) {
            GL_MsgBox.Show(Translation.Get("PresetExist") + GlobalCore.br + GlobalCore.br + "\"" + existName + "\"", null, MessageBoxButtons.OK, MessageBoxIcon.Warning, new OnMsgBoxClickListener() {

                @Override
                public boolean onClick(int which, Object data) {
                    that.show();
                    return true;
                }
            });
            return;
        }

        StringInputBox.Show(WrapType.SINGLELINE, Translation.Get("NewUserPreset"), Translation.Get("InsNewUserPreset"), "UserPreset", new OnMsgBoxClickListener() {

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
                        aktFilter = aktFilter.substring(0, pos);

                        uF += text + ";" + aktFilter + "#";
                        Config.UserFilter.setValue(uF);
                        Config.AcceptChanges();
                        mPresetListView.fillPresetList();
                        mPresetListView.notifyDataSetChanged();
                        that.show();
                        break;
                    case 2: // cancel clicket
                        that.show();
                        break;
                    case 3:
                        that.show();
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
}
