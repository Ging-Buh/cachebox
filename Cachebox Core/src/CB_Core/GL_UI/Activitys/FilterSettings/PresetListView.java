package CB_Core.GL_UI.Activitys.FilterSettings;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class PresetListView extends V_ListView
{

	public static PresetEntry aktPreset;

	private ArrayList<PresetEntry> lPresets;
	public ArrayList<PresetListViewItem> lItem;
	private CustomAdapter lvAdapter;

	public class PresetEntry
	{
		private String mName;
		private Sprite mIcon;
		private String mPresetString;

		public PresetEntry(String Name, Sprite Icon, String PresetString)
		{
			mName = Name;
			mIcon = Icon;
			mPresetString = PresetString;
		}

		public String getName()
		{
			return mName;
		}

		public Sprite getIcon()
		{
			return mIcon;
		}

		public String getPresetString()
		{
			return mPresetString;
		}
	}

	public PresetListView(CB_RectF rec)
	{
		super(rec, "");
		this.setHasInvisibleItems(true);
		fillPresetList();

		this.setBaseAdapter(null);
		lvAdapter = new CustomAdapter(lPresets);
		this.setBaseAdapter(lvAdapter);

	}

	public class CustomAdapter implements Adapter
	{

		private ArrayList<PresetEntry> presetList;

		public CustomAdapter(ArrayList<PresetEntry> lPresets)
		{

			this.presetList = lPresets;
		}

		public int getCount()
		{
			return presetList.size();
		}

		public Object getItem(int position)
		{
			return presetList.get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public ListViewItemBase getView(final int position)
		{

			ListViewItemBase v = lItem.get(position);

			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			return EditFilterSettings.ItemRec.getHeight();
		}
	}

	public void fillPresetList()
	{
		if (lPresets != null) lPresets.clear();
		if (lItem != null) lItem.clear();

		addPresetItem(SpriteCache.getThemedSprite("earth"), Translation.Get("AllCaches"), FilterProperties.presets[0]);
		addPresetItem(SpriteCache.getThemedSprite("log0icon"), Translation.Get("AllCachesToFind"), FilterProperties.presets[1]);
		addPresetItem(SpriteCache.getThemedSprite("big0icon"), Translation.Get("QuickCaches"), FilterProperties.presets[2]);
		addPresetItem(SpriteCache.getThemedSprite("GrabTB"), Translation.Get("GrabTB"), FilterProperties.presets[3]);
		addPresetItem(SpriteCache.getThemedSprite("DropTB"), Translation.Get("DropTB"), FilterProperties.presets[4]);
		addPresetItem(SpriteCache.getThemedSprite("star"), Translation.Get("Highlights"), FilterProperties.presets[5]);
		addPresetItem(SpriteCache.getThemedSprite("favorit"), Translation.Get("Favorites"), FilterProperties.presets[6]);
		addPresetItem(SpriteCache.getThemedSprite("delete"), Translation.Get("PrepareToArchive"), FilterProperties.presets[7]);
		addPresetItem(SpriteCache.getThemedSprite("warning-icon"), Translation.Get("ListingChanged"), FilterProperties.presets[8]);

		// add User Presets
		if (!Config.settings.UserFilter.getValue().equalsIgnoreCase(""))
		{
			String userEntrys[] = Config.settings.UserFilter.getValue().split("#");
			for (String entry : userEntrys)
			{
				int pos = entry.indexOf(";");
				String name = entry.substring(0, pos);
				String filter = entry.substring(pos + 1);
				addPresetItem(SpriteCache.getThemedSprite("userdata"), name, filter);
			}
		}

		fillItemList();
	}

	private void fillItemList()
	{
		if (lItem == null)
		{
			lItem = new ArrayList<PresetListViewItem>();
		}

		int index = 0;
		for (PresetEntry entry : lPresets)
		{
			PresetListViewItem v = new PresetListViewItem(EditFilterSettings.ItemRec, index, entry);

			v.setOnClickListener(new OnClickListener()
			{

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{

					int itemIndex = ((PresetListViewItem) v).getIndex();

					for (PresetListViewItem item : lItem)
					{
						((ListViewItemBase) item).isSelected = false;
					}

					if (itemIndex < FilterProperties.presets.length)
					{
						EditFilterSettings.tmpFilterProps = new FilterProperties(FilterProperties.presets[itemIndex]);
					}
					else
					{
						// User Preset
						String userEntrys[] = Config.settings.UserFilter.getValue().split("#");
						int i = itemIndex - FilterProperties.presets.length;

						int pos = userEntrys[i].indexOf(";");
						String filter = userEntrys[i].substring(pos + 1);
						EditFilterSettings.tmpFilterProps = new FilterProperties(filter);

					}

					// reset TxtFilter
					TextFilterView.that.setFilterString("", 0);
					return true;

				}
			});

			v.setOnLongClickListener(new OnClickListener()
			{

				@Override
				public boolean onClick(final GL_View_Base v, int x, int y, int pointer, int button)
				{
					final int delItemIndex = ((PresetListViewItem) v).getIndex();

					GL.that.closeActivity();
					GL_MsgBox.Show(Translation.Get("?DelUserPreset"), Translation.Get("DelUserPreset"), MessageBoxButtons.YesNo,
							MessageBoxIcon.Question, new OnMsgBoxClickListener()
							{

								@Override
								public boolean onClick(int which)
								{
									switch (which)
									{
									case 1: // ok Clicket

										if (delItemIndex < FilterProperties.presets.length)
										{
											return false; // Don't delete System Presets
										}
										else
										{
											String userEntrys[] = Config.settings.UserFilter.getValue().split("#");

											int i = FilterProperties.presets.length;
											String newUserEntris = "";
											for (String entry : userEntrys)
											{
												if (i++ != delItemIndex) newUserEntris += entry + "#";
											}
											Config.settings.UserFilter.setValue(newUserEntris);
											Config.AcceptChanges();
											EditFilterSettings.that.lvPre.fillPresetList();
											EditFilterSettings.that.lvPre.notifyDataSetChanged();

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

	private void addPresetItem(Sprite Icon, String Name, String PresetString)
	{
		if (lPresets == null) lPresets = new ArrayList<PresetListView.PresetEntry>();
		lPresets.add(new PresetEntry(Name, Icon, PresetString));
	}

	@Override
	public void setVisible(boolean On)
	{
		super.setVisible(On);
		if (On) chkIsPreset();
	}

	private void chkIsPreset()
	{
		for (PresetListViewItem item : lItem)
		{
			((ListViewItemBase) item).isSelected = false;
		}
	}
}
