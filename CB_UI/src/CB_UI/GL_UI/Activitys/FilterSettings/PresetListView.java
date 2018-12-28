package CB_UI.GL_UI.Activitys.FilterSettings;

import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Settings.SettingString;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;

public class PresetListView extends V_ListView {

    public static final FilterProperties[] presets = new FilterProperties[]{ //
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
    public static PresetEntry aktPreset;
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
        mPresetEntriesAdd("GrabTB", IconName.TBGRAB.name(), FilterInstances.WITHTB);
        mPresetEntriesAdd("DropTB", IconName.TBDROP.name(), FilterInstances.DROPTB);
        mPresetEntriesAdd("Highlights", "star", FilterInstances.HIGHLIGHTS);
        mPresetEntriesAdd("Favorites", "favorit", FilterInstances.FAVORITES);
        mPresetEntriesAdd("PrepareToArchive", IconName.DELETE.name(), FilterInstances.TOARCHIVE);
        mPresetEntriesAdd("ListingChanged", IconName.warningIcon.name(), FilterInstances.LISTINGCHANGED);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fillItemList();
    }

    private void fillItemList() {

        int index = 0;
        for (PresetEntry entry : mPresetEntries) {
            PresetListViewItem v = new PresetListViewItem(EditFilterSettings.ItemRec, index, entry);

            v.setOnClickListener(new OnClickListener() {

                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

                    int itemIndex = ((PresetListViewItem) v).getIndex();

                    for (PresetListViewItem presetListViewItem : mPresetListViewItems) {
                        ((ListViewItemBase) presetListViewItem).isSelected = false;
                    }

                    if (itemIndex < presets.length) {
                        EditFilterSettings.tmpFilterProps = new FilterProperties(presets[itemIndex].toString());
                    } else {
                        // User Preset
                        try {
                            String userEntrys[] = Config.UserFilter.getValue().split(SettingString.STRING_SPLITTER);
                            int i = itemIndex - presets.length;

                            int pos = userEntrys[i].indexOf(";");
                            String filter = userEntrys[i].substring(pos + 1);
                            if (filter.endsWith("#")) filter = filter.substring(0, filter.length() - 1);
                            EditFilterSettings.tmpFilterProps = new FilterProperties(filter);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    // reset TxtFilter
                    TextFilterView.that.setFilterString("", 0);
                    return true;

                }
            });

            v.setOnLongClickListener(new OnClickListener() {

                @Override
                public boolean onClick(final GL_View_Base v, int x, int y, int pointer, int button) {
                    final int delItemIndex = ((PresetListViewItem) v).getIndex();

                    GL.that.closeActivity();
                    GL_MsgBox.Show(Translation.Get("?DelUserPreset"), Translation.Get("DelUserPreset"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, new OnMsgBoxClickListener() {

                        @Override
                        public boolean onClick(int which, Object data) {
                            switch (which) {
                                case 1: // ok Clicked

                                    if (delItemIndex < presets.length) {
                                        return false; // Don't delete System Presets
                                    } else {
                                        try {
                                            String userEntrys[] = Config.UserFilter.getValue().split(SettingString.STRING_SPLITTER);

                                            int i = presets.length;
                                            String newUserEntris = "";
                                            for (String entry : userEntrys) {
                                                if (i++ != delItemIndex)
                                                    newUserEntris += entry + SettingString.STRING_SPLITTER;
                                            }
                                            Config.UserFilter.setValue(newUserEntris);
                                            Config.AcceptChanges();
                                            EditFilterSettings.that.mPresetListView.fillPresetList();
                                            EditFilterSettings.that.mPresetListView.notifyDataSetChanged();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                    TabMainView.actionShowFilter.Execute();
                                    break;
                                case 2: // cancel clicked
                                    TabMainView.actionShowFilter.Execute();
                                    break;
                                case 3:
                                    TabMainView.actionShowFilter.Execute();
                                    break;
                            }

                            return true;
                        }

                    });

                    return true;
                }
            });

            mPresetListViewItems.add(v);
            index++;
        }
    }

    private void mPresetEntriesAdd(String name, String icon, FilterProperties PresetFilter) {
        mPresetEntries.add(new PresetEntry(Translation.Get(name), Sprites.getSprite(icon), PresetFilter));
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
            return EditFilterSettings.ItemRec.getHeight();
        }
    }
}
