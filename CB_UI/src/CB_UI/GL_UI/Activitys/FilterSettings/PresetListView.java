package CB_UI.GL_UI.Activitys.FilterSettings;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Settings.SettingString;

public class PresetListView extends V_ListView {

    public static PresetEntry aktPreset;

    private ArrayList<PresetEntry> lPresets;
    public ArrayList<PresetListViewItem> lItem;
    private CustomAdapter lvAdapter;

    public static final FilterProperties[] presets = new FilterProperties[] { //
	    FilterInstances.ALL, //
	    FilterInstances.ACTIVE, //
	    FilterInstances.QUICK, //
	    FilterInstances.WITHTB, //
	    FilterInstances.DROPTB, //
	    FilterInstances.HIGHLIGHTS, //
	    FilterInstances.FAVORITES, //
	    FilterInstances.TOARCHIVE, //
	    FilterInstances.LISTINGCHANGED, //
    };

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

    public PresetListView(CB_RectF rec) {
	super(rec, "");
	this.setHasInvisibleItems(true);
	fillPresetList();
	this.setDisposeFlag(false);
	this.setBaseAdapter(null);
	lvAdapter = new CustomAdapter(lPresets);
	this.setBaseAdapter(lvAdapter);

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

	    ListViewItemBase v = lItem.get(position);

	    return v;
	}

	@Override
	public float getItemSize(int position) {
	    return EditFilterSettings.ItemRec.getHeight();
	}
    }

    public void fillPresetList() {
	if (lPresets != null)
	    lPresets.clear();
	if (lItem != null)
	    lItem.clear();

	addPresetItem(SpriteCacheBase.getThemedSprite("earth"), Translation.Get("AllCaches"), FilterInstances.ALL);
	addPresetItem(SpriteCacheBase.getThemedSprite("log0icon"), Translation.Get("AllCachesToFind"), FilterInstances.ACTIVE);
	addPresetItem(SpriteCacheBase.getThemedSprite("big0icon"), Translation.Get("QuickCaches"), FilterInstances.QUICK);
	addPresetItem(SpriteCacheBase.getThemedSprite("tb-grab"), Translation.Get("GrabTB"), FilterInstances.WITHTB);
	addPresetItem(SpriteCacheBase.getThemedSprite("tb-drop"), Translation.Get("DropTB"), FilterInstances.DROPTB);
	addPresetItem(SpriteCacheBase.getThemedSprite("star"), Translation.Get("Highlights"), FilterInstances.HIGHLIGHTS);
	addPresetItem(SpriteCacheBase.getThemedSprite("favorit"), Translation.Get("Favorites"), FilterInstances.FAVORITES);
	addPresetItem(SpriteCacheBase.getThemedSprite("delete"), Translation.Get("PrepareToArchive"), FilterInstances.TOARCHIVE);
	addPresetItem(SpriteCacheBase.getThemedSprite("warning-icon"), Translation.Get("ListingChanged"), FilterInstances.LISTINGCHANGED);

	// add User Presets
	if (!Config.UserFilter.getValue().equalsIgnoreCase("")) {
	    String userEntrys[] = Config.UserFilter.getValue().split(SettingString.STRING_SPLITTER);
	    try {
		for (String entry : userEntrys) {
		    int pos = entry.indexOf(";");
		    String name = entry.substring(0, pos);
		    String filter = entry.substring(pos + 1);
		    addPresetItem(SpriteCacheBase.getThemedSprite("userdata"), name, new FilterProperties(filter));
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	fillItemList();
    }

    private void fillItemList() {
	if (lItem == null) {
	    lItem = new ArrayList<PresetListViewItem>();
	}

	int index = 0;
	for (PresetEntry entry : lPresets) {
	    PresetListViewItem v = new PresetListViewItem(EditFilterSettings.ItemRec, index, entry);

	    v.setOnClickListener(new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

		    int itemIndex = ((PresetListViewItem) v).getIndex();

		    for (PresetListViewItem item : lItem) {
			((ListViewItemBase) item).isSelected = false;
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
					EditFilterSettings.that.lvPre.fillPresetList();
					EditFilterSettings.that.lvPre.notifyDataSetChanged();
				    } catch (Exception e) {
					e.printStackTrace();
				    }

				}
				EditFilterSettings.that.show();
				break;
			    case 2: // cancel clicket
				EditFilterSettings.that.show();
				break;
			    case 3:
				EditFilterSettings.that.show();
				break;
			    }

			    return true;
			}

		    });

		    return true;
		}
	    });

	    lItem.add(v);
	    index++;
	}
    }

    private void addPresetItem(Sprite Icon, String Name, FilterProperties PresetFilter) {
	if (lPresets == null)
	    lPresets = new ArrayList<PresetListView.PresetEntry>();
	lPresets.add(new PresetEntry(Name, Icon, PresetFilter));
    }

    @Override
    public void setVisible(boolean On) {
	super.setVisible(On);
	if (On)
	    chkIsPreset();
    }

    private void chkIsPreset() {
	for (PresetListViewItem item : lItem) {
	    ((ListViewItemBase) item).isSelected = false;
	}

	this.setBaseAdapter(null);
	lvAdapter = new CustomAdapter(lPresets);
	this.setBaseAdapter(lvAdapter);

    }
}
