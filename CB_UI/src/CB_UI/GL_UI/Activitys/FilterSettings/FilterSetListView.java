package CB_UI.GL_UI.Activitys.FilterSettings;

import CB_Core.Attributes;
import CB_Core.CacheTypes;
import CB_Core.FilterProperties;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;

public class FilterSetListView extends V_ListView {

    public static final int COLLAPSE_BUTTON_ITEM = 0;
    public static final int CHECK_ITEM = 1;
    public static final int THREE_STATE_ITEM = 2;
    public static final int NUMERIC_ITEM = 3;
    public static boolean mustSaveFilter = false;
    private static FilterSetListViewItem NotAvailable;
    private static FilterSetListViewItem Archived;
    private static FilterSetListViewItem Finds;
    private static FilterSetListViewItem Own;
    private static FilterSetListViewItem ContainsTravelBugs;
    private static FilterSetListViewItem Favorites;
    private static FilterSetListViewItem HasUserData;
    private static FilterSetListViewItem ListingChanged;
    private static FilterSetListViewItem WithManualWaypoint;
    private static FilterSetListViewItem hasCorrectedCoordinates;
    private static FilterSetListViewItem minTerrain;
    private static FilterSetListViewItem maxTerrain;
    private static FilterSetListViewItem minDifficulty;
    private static FilterSetListViewItem maxDifficulty;
    private static FilterSetListViewItem minContainerSize;
    private static FilterSetListViewItem maxContainerSize;
    private static FilterSetListViewItem minRating;
    private static FilterSetListViewItem maxRating;
    private static FilterSetListViewItem types;
    private static FilterSetListViewItem attribs;
    int index = 0;
    private ArrayList<FilterSetEntry> lFilterSets;
    private ArrayList<FilterSetListViewItem> lFilterSetListViewItems;

    public FilterSetListView(CB_RectF rec) {
        super(rec, "");
        this.setHasInvisibleItems(true);
        fillFilterSetList();

        this.setBaseAdapter(null);
        CustomAdapter lvAdapter = new CustomAdapter(lFilterSets, lFilterSetListViewItems);
        this.setBaseAdapter(lvAdapter);
        this.setDisposeFlag(false);
    }

    public static FilterProperties SaveFilterProperties() {
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
        props.hasCorrectedCoordinates = hasCorrectedCoordinates.getChecked();

        props.MinDifficulty = minDifficulty.getValue();
        props.MaxDifficulty = maxDifficulty.getValue();
        props.MinTerrain = minTerrain.getValue();
        props.MaxTerrain = maxTerrain.getValue();
        props.MinContainerSize = minContainerSize.getValue();
        props.MaxContainerSize = maxContainerSize.getValue();
        props.MinRating = minRating.getValue();
        props.MaxRating = maxRating.getValue();

        for (int i = 0; i < types.getChildLength(); i++) {
            FilterSetListViewItem itm = types.getChild(i);
            int ct = itm.getFilterSetEntry().cacheType.ordinal();
            props.mCacheTypes[ct] = itm.getBoolean();
        }

        for (int i = 0; i < attribs.getChildLength(); i++) {
            props.mAttributes[i + 1] = attribs.getChild(i).getChecked();
        }

        return props;
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);
        if (mustSaveFilter)
            SetFilter();
    }

    private void SetFilter() {
        EditFilterSettings.tmpFilterProps = FilterSetListView.SaveFilterProperties();
    }

    @Override
    public void onShow() {
        if (EditFilterSettings.tmpFilterProps != null && !EditFilterSettings.tmpFilterProps.toString().equals("")) {
            LoadFilterProperties(EditFilterSettings.tmpFilterProps);
        }
    }

    private void fillFilterSetList() {

        // add General
        FilterSetListViewItem general = addFilterSetCollapseItem(null, Translation.Get("General"), COLLAPSE_BUTTON_ITEM);
        NotAvailable = general.addChild(addFilterSetItem(Sprites.getSprite("disabled"), Translation.Get("disabled"), THREE_STATE_ITEM));
        Archived = general.addChild(addFilterSetItem(Sprites.getSprite("not-available"), Translation.Get("archived"), THREE_STATE_ITEM));
        Finds = general.addChild(addFilterSetItem(Sprites.getSprite("log0icon"), Translation.Get("myfinds"), THREE_STATE_ITEM));
        Own = general.addChild(addFilterSetItem(Sprites.getSprite("star"), Translation.Get("myowncaches"), THREE_STATE_ITEM));
        ContainsTravelBugs = general.addChild(addFilterSetItem(Sprites.getSprite("tb"), Translation.Get("withtrackables"), THREE_STATE_ITEM));
        Favorites = general.addChild(addFilterSetItem(Sprites.getSprite("favorit"), Translation.Get("Favorites"), THREE_STATE_ITEM));
        HasUserData = general.addChild(addFilterSetItem(Sprites.getSprite("userdata"), Translation.Get("hasuserdata"), THREE_STATE_ITEM));
        ListingChanged = general.addChild(addFilterSetItem(Sprites.getSprite(IconName.warningIcon.name()), Translation.Get("ListingChanged"), THREE_STATE_ITEM));
        WithManualWaypoint = general.addChild(addFilterSetItem(Sprites.getSprite(IconName.manualwaypoint.name()), Translation.Get("manualwaypoint"), THREE_STATE_ITEM));
        hasCorrectedCoordinates = general.addChild(addFilterSetItem(Sprites.getSprite("hasCorrectedCoordinates"), Translation.Get("hasCorrectedCoordinates"), THREE_STATE_ITEM));

        // add D/T
        FilterSetListViewItem dt = addFilterSetCollapseItem(null, "D / T" + "\n" + "GC-Vote", COLLAPSE_BUTTON_ITEM);
        minDifficulty = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.Get("minDifficulty"), NUMERIC_ITEM, 1, 5, 1, 0.5f));
        maxDifficulty = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.Get("maxDifficulty"), NUMERIC_ITEM, 1, 5, 5, 0.5f));
        minTerrain = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.Get("minTerrain"), NUMERIC_ITEM, 1, 5, 1, 0.5f));
        maxTerrain = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.Get("maxTerrain"), NUMERIC_ITEM, 1, 5, 5, 0.5f));
        minContainerSize = dt.addChild(addFilterSetItem(Sprites.SizesIcons.toArray(), Translation.Get("minContainerSize"), NUMERIC_ITEM, 0, 4, 0, 1));
        maxContainerSize = dt.addChild(addFilterSetItem(Sprites.SizesIcons.toArray(), Translation.Get("maxContainerSize"), NUMERIC_ITEM, 0, 4, 4, 1));
        minRating = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.Get("minRating"), NUMERIC_ITEM, 0, 5, 0, 0.5f));
        maxRating = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.Get("maxRating"), NUMERIC_ITEM, 0, 5, 5, 0.5f));

        // add CacheTypes
        types = addFilterSetCollapseItem(null, "Cache Types", COLLAPSE_BUTTON_ITEM);
        for (int i = 0; i < CacheTypes.caches().length; i++) {
            types.addChild(addFilterSetItem(CacheTypes.caches()[i]));
        }
        /**
         types.addChild(addFilterSetItem(CacheTypes.Traditional, Sprites.BigIcons.get(0), "Traditional", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Multi, Sprites.BigIcons.get(1), "Multi-Cache", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Mystery, Sprites.BigIcons.get(2), "Mystery", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Camera, Sprites.BigIcons.get(3), "Webcam Cache", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Earth, Sprites.BigIcons.get(4), "Earthcache", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Event, Sprites.BigIcons.get(5), "Event", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.MegaEvent, Sprites.BigIcons.get(6), "Mega Event", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.CITO, Sprites.BigIcons.get(7), "Cache In Trash Out", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Virtual, Sprites.BigIcons.get(8), "Virtual Cache", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Letterbox, Sprites.BigIcons.get(9), "Letterbox", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Wherigo, Sprites.BigIcons.get(10), "Wherigo", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Munzee, Sprites.BigIcons.get(25), "Munzee", CHECK_ITEM));
         types.addChild(addFilterSetItem(CacheTypes.Giga, Sprites.MapIcons.get(27), "Giga", CHECK_ITEM));
         **/

        // add Attributes
        attribs = addFilterSetCollapseItem(null, "Attributes", COLLAPSE_BUTTON_ITEM);
        for (int i = 1; i < Attributes.values().length; i++) {
            attribs.addChild(addFilterSetItem(Sprites.getSprite("att-" + i + "-1Icon"), Translation.Get("att_" + i + "_1"), THREE_STATE_ITEM));
        }

    }

    private FilterSetListViewItem addFilterSetItem(Sprite[] Icons, String Name, int ItemType, double i, double j, double k, double f) {
        if (lFilterSets == null) {
            lFilterSets = new ArrayList<>();
            lFilterSetListViewItems = new ArrayList<>();
        }
        //String Name, Sprite[] Icons, int itemType, double min = i, double max = j, double iniValue = k, double Step = f
        FilterSetEntry tmp = new FilterSetEntry(Name, Icons, ItemType, i, j, k, f);
        lFilterSets.add(tmp);

        FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.ItemRec, index++, tmp);
        // inital mit GONE
        v.setInvisible();
        lFilterSetListViewItems.add(v);
        return v;

    }

    private FilterSetListViewItem addFilterSetItem(Sprite Icon, String Name, int ItemType) {
        if (lFilterSets == null) {
            lFilterSets = new ArrayList<>();
            lFilterSetListViewItems = new ArrayList<>();
        }
        FilterSetEntry tmp = new FilterSetEntry(Name, Icon, ItemType);
        lFilterSets.add(tmp);

        FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.ItemRec, index++, tmp);
        // inital mit GONE
        v.setInvisible();
        lFilterSetListViewItems.add(v);
        return v;
    }

    private FilterSetListViewItem addFilterSetItem(CacheTypes cacheType) {
        Sprite icon = Sprites.getSprite("big" + cacheType.name());
        String name = cacheType.name();
        int itemType = CHECK_ITEM;
        if (lFilterSets == null) {
            lFilterSets = new ArrayList<>();
            lFilterSetListViewItems = new ArrayList<>();
        }
        FilterSetEntry tmp = new FilterSetEntry(cacheType, name, icon, itemType);
        lFilterSets.add(tmp);

        FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.ItemRec, index++, tmp);
        // inital mit GONE
        v.setInvisible();
        lFilterSetListViewItems.add(v);
        return v;
    }

    private FilterSetListViewItem addFilterSetCollapseItem(Sprite Icon, String Name, int ItemType) {
        if (lFilterSets == null) {
            lFilterSets = new ArrayList<>();
            lFilterSetListViewItems = new ArrayList<>();
        }
        FilterSetEntry tmp = new FilterSetEntry(Name, Icon, ItemType);
        lFilterSets.add(tmp);

        FilterSetListViewItem v = new FilterSetListViewItem(EditFilterSettings.ItemRec, index++, tmp);
        lFilterSetListViewItems.add(v);

        v.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                collapseButton_Clicked((FilterSetListViewItem) v);
                return false;
            }
        });

        return v;
    }

    private void collapseButton_Clicked(FilterSetListViewItem item) {
        item.toggleChildeViewState();
        this.notifyDataSetChanged();
        this.invalidate();
    }

    public void LoadFilterProperties(FilterProperties props) {
        NotAvailable.setValue(props.NotAvailable);
        Archived.setValue(props.Archived);
        Finds.setValue(props.Finds);
        Own.setValue(props.Own);
        ContainsTravelBugs.setValue(props.ContainsTravelbugs);
        Favorites.setValue(props.Favorites);
        HasUserData.setValue(props.HasUserData);
        ListingChanged.setValue(props.ListingChanged);
        WithManualWaypoint.setValue(props.WithManualWaypoint);
        hasCorrectedCoordinates.setValue(props.hasCorrectedCoordinates);

        minTerrain.setValue(props.MinTerrain);
        maxTerrain.setValue(props.MaxTerrain);
        minDifficulty.setValue(props.MinDifficulty);
        maxDifficulty.setValue(props.MaxDifficulty);
        minContainerSize.setValue(props.MinContainerSize);
        maxContainerSize.setValue(props.MaxContainerSize);
        minRating.setValue(props.MinRating);
        maxRating.setValue(props.MaxRating);

        for (int i = 0; i < types.getChildLength(); i++) {
            FilterSetListViewItem itm = types.getChild(i);
            int ct = itm.getFilterSetEntry().cacheType.ordinal();
            itm.setValue(props.mCacheTypes[ct]);
        }

        for (int i = 0; i < attribs.getChildLength(); i++) {
            attribs.getChild(i).setValue(props.mAttributes[i + 1]);
        }

    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {

        super.onTouchDown(x, y, pointer, button);
        synchronized (childs) {
            // for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
            for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext(); ) {
                // Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
                GL_View_Base view = iterator.next();

                // Invisible Views can not be clicked!
                if (!view.isVisible())
                    continue;
                if (view.contains(x, y)) {

                    ((FilterSetListViewItem) view).lastItemTouchPos = new Vector2(x - view.getX(), y - view.getY());

                }

            }
        }

        return true;
    }

    public static class FilterSetEntry {
        private static int IdCounter;
        private final String mName;
        private final int mItemType;
        private final int ID;
        private Sprite mIcon;
        private Sprite[] mIconArray;
        private int mState = 0;
        private double mNumericMax;
        private double mNumericMin;
        private double mNumericStep;
        private double mNumericState;
        private CacheTypes cacheType;

        public FilterSetEntry(String name, Sprite icon, int itemType) {
            mName = name;
            mIcon = icon;
            mItemType = itemType;
            ID = IdCounter++;
        }

        public FilterSetEntry(String Name, Sprite[] Icons, int itemType, double min, double max, double iniValue, double Step) {
            mName = Name;
            mIconArray = Icons;
            mItemType = itemType;
            mNumericMin = min;
            mNumericMax = max;
            mNumericState = iniValue;
            mNumericStep = Step;
            ID = IdCounter++;
        }

        public FilterSetEntry(CacheTypes enumType, String name, Sprite icon, int itemType) {
            mName = name;
            mIcon = icon;
            mItemType = itemType;
            cacheType = enumType;
            ID = IdCounter++;
        }

        public void setState(int State) {
            mState = State;
        }

        public String getName() {
            return mName;
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
            mNumericState += mNumericStep;
            if (mNumericState > mNumericMax)
                mNumericState = mNumericMin;
        }

        public void minusClick() {
            mNumericState -= mNumericStep;
            if (mNumericState < 0)
                mNumericState = mNumericMax;
        }

        public void stateClick() {
            mState += 1;
            if (mItemType == FilterSetListView.CHECK_ITEM) {
                if (mState > 1)
                    mState = 0;
            } else if (mItemType == FilterSetListView.THREE_STATE_ITEM) {
                if (mState > 1)
                    mState = -1;
            }
        }

    }

    public class CustomAdapter implements Adapter {

        private final ArrayList<FilterSetEntry> filterSetList;
        private final ArrayList<FilterSetListViewItem> lFilterSetListViewItems;

        public CustomAdapter(ArrayList<FilterSetEntry> lFilterSets, ArrayList<FilterSetListViewItem> FilterSetListViewItems) {
            this.filterSetList = lFilterSets;
            this.lFilterSetListViewItems = FilterSetListViewItems;
        }

        @Override
        public int getCount() {
            return filterSetList.size();
        }

        public Object getItem(int position) {
            return filterSetList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public ListViewItemBase getView(int position) {
            FilterSetListViewItem v = lFilterSetListViewItems.get(position);
            if (!v.isVisible())
                return null;
            return v;
        }

        @Override
        public float getItemSize(int position) {
            FilterSetListViewItem v = lFilterSetListViewItems.get(position);
            if (!v.isVisible())
                return 0;
            return v.getHeight();
        }
    }

}
