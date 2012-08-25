package CB_Core.GL_UI.Activitys.FilterSettings;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class FilterSetListView extends V_ListView
{

	public static FilterSetEntry aktFilterSetEntry;
	public static final int COLLABSE_BUTTON_ITEM = 0;
	public static final int CHECK_ITEM = 1;
	public static final int THREE_STATE_ITEM = 2;
	public static final int NUMERICK_ITEM = 3;
	// public static float lastTouchX;
	// public static float lastTouchY;
	public static int windowW = 0;
	public static int windowH = 0;

	private static FilterSetListViewItem NotAvailable;
	private static FilterSetListViewItem Archived;
	private static FilterSetListViewItem Finds;
	private static FilterSetListViewItem Own;
	private static FilterSetListViewItem ContainsTravelBugs;
	private static FilterSetListViewItem Favorites;
	private static FilterSetListViewItem HasUserData;
	private static FilterSetListViewItem ListingChanged;
	private static FilterSetListViewItem WithManualWaypoint;
	private static FilterSetListViewItem minTerrain;
	private static FilterSetListViewItem maxTerrain;
	private static FilterSetListViewItem minDifficulty;
	private static FilterSetListViewItem maxDifficulty;
	private static FilterSetListViewItem minContainerSize;
	private static FilterSetListViewItem maxContainerSize;
	private static FilterSetListViewItem minRating;
	private static FilterSetListViewItem maxRating;
	private static FilterSetListViewItem Types;
	private static FilterSetListViewItem Attr;
	private static FilterSetListViewItem AttrNegative;

	private ArrayList<FilterSetEntry> lFilterSets;
	private ArrayList<FilterSetListViewItem> lFilterSetListViewItems;
	private CustomAdapter lvAdapter;

	public static boolean mustSaveFilter = false;

	public static class FilterSetEntry
	{
		private String mName;
		private Sprite mIcon;
		private Sprite[] mIconArray;
		private int mState = 0;
		private int mItemType;
		private int ID;
		private static int IdCounter;

		private double mNumerickMax;
		private double mNumerickMin;
		private double mNumerickStep;
		private double mNumerickState;

		public FilterSetEntry(String Name, Sprite Icon, int itemType)
		{
			mName = Name;
			mIcon = Icon;
			mItemType = itemType;
			ID = IdCounter++;
		}

		public FilterSetEntry(String Name, Sprite[] Icons, int itemType, double min, double max, double iniValue, double Step)
		{
			mName = Name;
			mIconArray = Icons;
			mItemType = itemType;
			mNumerickMin = min;
			mNumerickMax = max;
			mNumerickState = iniValue;
			mNumerickStep = Step;
			ID = IdCounter++;
		}

		public void setState(int State)
		{
			mState = State;
		}

		public void setState(float State)
		{
			mNumerickState = State;
		}

		public String getName()
		{
			return mName;
		}

		public Sprite getIcon()
		{
			if (mItemType == NUMERICK_ITEM)
			{
				try
				{
					double ArrayMultiplier = (mIconArray.length > 5) ? 2 : 1;

					return mIconArray[(int) (mNumerickState * ArrayMultiplier)];
				}
				catch (Exception e)
				{
				}

			}
			return mIcon;
		}

		public int getState()
		{
			return mState;
		}

		public int getItemType()
		{
			return mItemType;
		}

		public int getID()
		{
			return ID;
		}

		public double getNumState()
		{
			return mNumerickState;
		}

		public void plusClick()
		{
			mNumerickState += mNumerickStep;
			if (mNumerickState > mNumerickMax) mNumerickState = mNumerickMin;
		}

		public void minusClick()
		{
			mNumerickState -= mNumerickStep;
			if (mNumerickState < 0) mNumerickState = mNumerickMax;
		}

		public void stateClick()
		{
			mState += 1;
			if (mItemType == FilterSetListView.CHECK_ITEM)
			{
				if (mState > 1) mState = 0;
			}
			else if (mItemType == FilterSetListView.THREE_STATE_ITEM)
			{
				if (mState > 1) mState = -1;
			}
		}

	}

	public FilterSetListView(CB_RectF rec)
	{
		super(rec, "");
		this.setHasInvisibleItems(true);
		fillFilterSetList();

		this.setBaseAdapter(null);
		lvAdapter = new CustomAdapter(lFilterSets, lFilterSetListViewItems);
		this.setBaseAdapter(lvAdapter);

	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);
		if (mustSaveFilter) SetFilter();
	}

	private void SetFilter()
	{
		EditFilterSettings.tmpFilterProps = FilterSetListView.SaveFilterProperties();
	}

	public class CustomAdapter implements Adapter
	{

		private ArrayList<FilterSetEntry> filterSetList;
		private ArrayList<FilterSetListViewItem> lFilterSetListViewItems;

		public CustomAdapter(ArrayList<FilterSetEntry> lFilterSets, ArrayList<FilterSetListViewItem> FilterSetListViewItems)
		{
			this.filterSetList = lFilterSets;
			this.lFilterSetListViewItems = FilterSetListViewItems;
		}

		public int getCount()
		{
			return filterSetList.size();
		}

		public Object getItem(int position)
		{
			return filterSetList.get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			FilterSetListViewItem v = lFilterSetListViewItems.get(position);
			if (v.getVisibility() == CB_View_Base.INVISIBLE) return null;// new FieldNoteViewItem(EditFilterSettings.ItemRec, position,
																			// null);

			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			FilterSetListViewItem v = lFilterSetListViewItems.get(position);
			if (v.getVisibility() == CB_View_Base.INVISIBLE) return 0;
			return v.getHeight();
		}
	}

	public void onShow()
	{
		if (EditFilterSettings.tmpFilterProps != null && !EditFilterSettings.tmpFilterProps.ToString().equals(""))
		{
			LoadFilterProperties(EditFilterSettings.tmpFilterProps);
		}

	}

	private void fillFilterSetList()
	{

		// add General
		FilterSetListViewItem General = addFilterSetCollabseItem(null, GlobalCore.Translations.Get("General"), COLLABSE_BUTTON_ITEM);
		NotAvailable = General.addChild(addFilterSetItem(SpriteCache.getThemedSprite("disabled"), GlobalCore.Translations.Get("disabled"),
				THREE_STATE_ITEM));
		Archived = General.addChild(addFilterSetItem(SpriteCache.getThemedSprite("not-available"), GlobalCore.Translations.Get("archived"),
				THREE_STATE_ITEM));
		Finds = General.addChild(addFilterSetItem(SpriteCache.getThemedSprite("log0icon"), GlobalCore.Translations.Get("myfinds"),
				THREE_STATE_ITEM));
		Own = General.addChild(addFilterSetItem(SpriteCache.getThemedSprite("star"), GlobalCore.Translations.Get("myowncaches"),
				THREE_STATE_ITEM));
		ContainsTravelBugs = General.addChild(addFilterSetItem(SpriteCache.getThemedSprite("tb"),
				GlobalCore.Translations.Get("withtrackables"), THREE_STATE_ITEM));
		Favorites = General.addChild(addFilterSetItem(SpriteCache.getThemedSprite("favorit"), GlobalCore.Translations.Get("Favorites"),
				THREE_STATE_ITEM));
		HasUserData = General.addChild(addFilterSetItem(SpriteCache.getThemedSprite("userdata"),
				GlobalCore.Translations.Get("hasuserdata"), THREE_STATE_ITEM));
		ListingChanged = General.addChild(addFilterSetItem(SpriteCache.getThemedSprite("warning-icon"),
				GlobalCore.Translations.Get("ListingChanged"), THREE_STATE_ITEM));
		WithManualWaypoint = General.addChild(addFilterSetItem(SpriteCache.getThemedSprite("big16icon"),
				GlobalCore.Translations.Get("manualwaypoint"), THREE_STATE_ITEM));

		// add D/T
		FilterSetListViewItem DT = addFilterSetCollabseItem(null, "D / T" + String.format("%n") + "GC-Vote", COLLABSE_BUTTON_ITEM);
		minDifficulty = DT.addChild(addFilterSetItem(SpriteCache.Stars.toArray(), GlobalCore.Translations.Get("minDifficulty"),
				NUMERICK_ITEM, 1, 5, 1, 0.5f));
		maxDifficulty = DT.addChild(addFilterSetItem(SpriteCache.Stars.toArray(), GlobalCore.Translations.Get("maxDifficulty"),
				NUMERICK_ITEM, 1, 5, 5, 0.5f));
		minTerrain = DT.addChild(addFilterSetItem(SpriteCache.Stars.toArray(), GlobalCore.Translations.Get("minTerrain"), NUMERICK_ITEM, 1,
				5, 1, 0.5f));
		maxTerrain = DT.addChild(addFilterSetItem(SpriteCache.Stars.toArray(), GlobalCore.Translations.Get("maxTerrain"), NUMERICK_ITEM, 1,
				5, 5, 0.5f));
		minContainerSize = DT.addChild(addFilterSetItem(SpriteCache.SizesIcons.toArray(), GlobalCore.Translations.Get("minContainerSize"),
				NUMERICK_ITEM, 0, 4, 0, 1));
		maxContainerSize = DT.addChild(addFilterSetItem(SpriteCache.SizesIcons.toArray(), GlobalCore.Translations.Get("maxContainerSize"),
				NUMERICK_ITEM, 0, 4, 4, 1));
		minRating = DT.addChild(addFilterSetItem(SpriteCache.Stars.toArray(), GlobalCore.Translations.Get("minRating"), NUMERICK_ITEM, 0,
				5, 0, 0.5f));
		maxRating = DT.addChild(addFilterSetItem(SpriteCache.Stars.toArray(), GlobalCore.Translations.Get("maxRating"), NUMERICK_ITEM, 0,
				5, 5, 0.5f));

		// add CacheTypes
		Types = addFilterSetCollabseItem(null, "Cache Types", COLLABSE_BUTTON_ITEM);
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(0), "Traditional", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(1), "Multi-Cache", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(2), "Mystery", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(3), "Webcam Cache", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(4), "Earthcache", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(5), "Event", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(6), "Mega Event", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(7), "Cache In Trash Out", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(8), "Virtual Cache", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(9), "Letterbox", CHECK_ITEM));
		Types.addChild(addFilterSetItem(SpriteCache.BigIcons.get(10), "Wherigo", CHECK_ITEM));

		// add Attributes
		Attr = addFilterSetCollabseItem(null, "Attributes", COLLABSE_BUTTON_ITEM);
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-1-1Icon"), GlobalCore.Translations.Get("att_1_1"), THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-2-1Icon"), GlobalCore.Translations.Get("att_2_1"), THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-3-1Icon"), GlobalCore.Translations.Get("att_3_1"), THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-4-1Icon"), GlobalCore.Translations.Get("att_4_1"), THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-5-1Icon"), GlobalCore.Translations.Get("att_5_1"), THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-6-1Icon"), GlobalCore.Translations.Get("att_6_1"), THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-7-1Icon"), GlobalCore.Translations.Get("att_7_1"), THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-8-1Icon"), GlobalCore.Translations.Get("att_8_1"), THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-9-1Icon"), GlobalCore.Translations.Get("att_9_1"), THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-10-1Icon"), GlobalCore.Translations.Get("att_10_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-11-1Icon"), GlobalCore.Translations.Get("att_11_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-12-1Icon"), GlobalCore.Translations.Get("att_12_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-13-1Icon"), GlobalCore.Translations.Get("att_13_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-14-1Icon"), GlobalCore.Translations.Get("att_14_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-15-1Icon"), GlobalCore.Translations.Get("att_15_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-16-1Icon"), GlobalCore.Translations.Get("att_16_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-17-1Icon"), GlobalCore.Translations.Get("att_17_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-18-1Icon"), GlobalCore.Translations.Get("att_18_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-19-1Icon"), GlobalCore.Translations.Get("att_19_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-20-1Icon"), GlobalCore.Translations.Get("att_20_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-21-1Icon"), GlobalCore.Translations.Get("att_21_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-22-1Icon"), GlobalCore.Translations.Get("att_22_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-23-1Icon"), GlobalCore.Translations.Get("att_23_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-24-1Icon"), GlobalCore.Translations.Get("att_24_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-25-1Icon"), GlobalCore.Translations.Get("att_25_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-26-1Icon"), GlobalCore.Translations.Get("att_26_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-27-1Icon"), GlobalCore.Translations.Get("att_27_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-28-1Icon"), GlobalCore.Translations.Get("att_28_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-29-1Icon"), GlobalCore.Translations.Get("att_29_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-30-1Icon"), GlobalCore.Translations.Get("att_30_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-31-1Icon"), GlobalCore.Translations.Get("att_31_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-32-1Icon"), GlobalCore.Translations.Get("att_32_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-33-1Icon"), GlobalCore.Translations.Get("att_33_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-34-1Icon"), GlobalCore.Translations.Get("att_34_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-35-1Icon"), GlobalCore.Translations.Get("att_35_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-36-1Icon"), GlobalCore.Translations.Get("att_36_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-37-1Icon"), GlobalCore.Translations.Get("att_37_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-38-1Icon"), GlobalCore.Translations.Get("att_38_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-39-1Icon"), GlobalCore.Translations.Get("att_39_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-40-1Icon"), GlobalCore.Translations.Get("att_40_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-41-1Icon"), GlobalCore.Translations.Get("att_41_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-42-1Icon"), GlobalCore.Translations.Get("att_42_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-43-1Icon"), GlobalCore.Translations.Get("att_43_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-44-1Icon"), GlobalCore.Translations.Get("att_44_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-45-1Icon"), GlobalCore.Translations.Get("att_45_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-46-1Icon"), GlobalCore.Translations.Get("att_46_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-47-1Icon"), GlobalCore.Translations.Get("att_47_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-48-1Icon"), GlobalCore.Translations.Get("att_48_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-49-1Icon"), GlobalCore.Translations.Get("att_49_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-50-1Icon"), GlobalCore.Translations.Get("att_50_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-51-1Icon"), GlobalCore.Translations.Get("att_51_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-52-1Icon"), GlobalCore.Translations.Get("att_52_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-53-1Icon"), GlobalCore.Translations.Get("att_53_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-54-1Icon"), GlobalCore.Translations.Get("att_54_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-55-1Icon"), GlobalCore.Translations.Get("att_55_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-56-1Icon"), GlobalCore.Translations.Get("att_56_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-57-1Icon"), GlobalCore.Translations.Get("att_57_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-58-1Icon"), GlobalCore.Translations.Get("att_58_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-59-1Icon"), GlobalCore.Translations.Get("att_59_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-60-1Icon"), GlobalCore.Translations.Get("att_60_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-61-1Icon"), GlobalCore.Translations.Get("att_61_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-62-1Icon"), GlobalCore.Translations.Get("att_62_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-63-1Icon"), GlobalCore.Translations.Get("att_63_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-64-1Icon"), GlobalCore.Translations.Get("att_64_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-65-1Icon"), GlobalCore.Translations.Get("att_65_1"),
				THREE_STATE_ITEM));
		Attr.addChild(addFilterSetItem(SpriteCache.getThemedSprite("att-66-1Icon"), GlobalCore.Translations.Get("att_66_1"),
				THREE_STATE_ITEM));

	}

	int index = 0;

	private FilterSetListViewItem addFilterSetItem(Sprite[] Icons, String Name, int ItemType, double i, double j, double k, double f)
	{

		if (lFilterSets == null)
		{
			lFilterSets = new ArrayList<FilterSetListView.FilterSetEntry>();
			lFilterSetListViewItems = new ArrayList<FilterSetListViewItem>();
		}
		FilterSetEntry tmp = new FilterSetEntry(Name, Icons, ItemType, i, j, k, f);
		lFilterSets.add(tmp);

		FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.ItemRec, index++, tmp);
		// inital mit GONE
		v.setVisibility(CB_View_Base.INVISIBLE);
		lFilterSetListViewItems.add(v);
		return v;

	}

	private FilterSetListViewItem addFilterSetItem(Sprite Icon, String Name, int ItemType)
	{
		if (lFilterSets == null)
		{
			lFilterSets = new ArrayList<FilterSetListView.FilterSetEntry>();
			lFilterSetListViewItems = new ArrayList<FilterSetListViewItem>();
		}
		FilterSetEntry tmp = new FilterSetEntry(Name, Icon, ItemType);
		lFilterSets.add(tmp);

		FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.ItemRec, index++, tmp);
		// inital mit GONE
		v.setVisibility(CB_View_Base.INVISIBLE);
		lFilterSetListViewItems.add(v);
		return v;
	}

	private FilterSetListViewItem addFilterSetCollabseItem(Sprite Icon, String Name, int ItemType)
	{
		if (lFilterSets == null)
		{
			lFilterSets = new ArrayList<FilterSetListView.FilterSetEntry>();
			lFilterSetListViewItems = new ArrayList<FilterSetListViewItem>();
		}
		FilterSetEntry tmp = new FilterSetEntry(Name, Icon, ItemType);
		lFilterSets.add(tmp);
		;
		FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.ItemRec, index++, tmp);
		lFilterSetListViewItems.add(v);

		v.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				collabseButton_Clicked((FilterSetListViewItem) v);
				return false;
			}
		});

		return v;
	}

	private void collabseButton_Clicked(FilterSetListViewItem item)
	{
		item.toggleChildeViewState();
		this.notifyDataSetChanged();
		this.invalidate();
	}

	public void LoadFilterProperties(FilterProperties props)
	{
		NotAvailable.setValue(props.NotAvailable);
		Archived.setValue(props.Archived);
		Finds.setValue(props.Finds);
		Own.setValue(props.Own);
		ContainsTravelBugs.setValue(props.ContainsTravelbugs);
		Favorites.setValue(props.Favorites);
		HasUserData.setValue(props.HasUserData);
		ListingChanged.setValue(props.ListingChanged);
		WithManualWaypoint.setValue(props.WithManualWaypoint);

		minTerrain.setValue(props.MinTerrain);
		maxTerrain.setValue(props.MaxTerrain);
		minDifficulty.setValue(props.MinDifficulty);
		maxDifficulty.setValue(props.MaxDifficulty);
		minContainerSize.setValue(props.MinContainerSize);
		maxContainerSize.setValue(props.MaxContainerSize);
		minRating.setValue(props.MinRating);
		maxRating.setValue(props.MaxRating);

		for (int i = 0; i < 11; i++)
			Types.getChild(i).setValue(props.cacheTypes[i]);

		for (int i = 0; i < Attr.getChildLength(); i++)
		{
			if (i < props.attributesFilter.length) Attr.getChild(i).setValue(props.attributesFilter[i]);
		}

	}

	public static FilterProperties SaveFilterProperties()
	{
		FilterProperties props = new FilterProperties();
		props.NotAvailable = NotAvailable.getChecked();
		props.Archived = Archived.getChecked();
		props.Finds = Finds.getChecked();
		props.Own = Own.getChecked();
		props.ContainsTravelbugs = ContainsTravelBugs.getChecked();
		props.Favorites = Favorites.getChecked();
		props.HasUserData = HasUserData.getChecked();
		props.ListingChanged = ListingChanged.getChecked();
		props.WithManualWaypoint = WithManualWaypoint.getChecked();

		props.MinDifficulty = minDifficulty.getValue();
		props.MaxDifficulty = maxDifficulty.getValue();
		props.MinTerrain = minTerrain.getValue();
		props.MaxTerrain = maxTerrain.getValue();
		props.MinContainerSize = minContainerSize.getValue();
		props.MaxContainerSize = maxContainerSize.getValue();
		props.MinRating = minRating.getValue();
		props.MaxRating = maxRating.getValue();

		for (int i = 0; i < 11; i++)
			props.cacheTypes[i] = Types.getChild(i).getBoolean();

		for (int i = 0; i < Attr.getChildLength(); i++)
		{
			if (i < props.attributesFilter.length) props.attributesFilter[i] = Attr.getChild(i).getChecked();
		}

		return props;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		super.onTouchDown(x, y, pointer, button);
		synchronized (childs)
		{
			// for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
			for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
			{
				// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
				GL_View_Base view = iterator.next();

				// Invisible Views can not be clicked!
				if (!view.isVisible()) continue;
				if (view.contains(x, y))
				{

					((FilterSetListViewItem) view).lastItemTouchPos = new Vector2(x - view.getPos().x, y - view.getPos().y);

				}

			}
		}

		return true;
	}

}
