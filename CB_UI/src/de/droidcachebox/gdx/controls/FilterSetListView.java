package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.Attributes;
import de.droidcachebox.database.CacheTypes;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.translation.Translation;

import java.util.ArrayList;
import java.util.Iterator;

public class FilterSetListView extends V_ListView {

    public static final int COLLAPSE_BUTTON_ITEM = 0;
    public static final int CHECK_ITEM = 1;
    public static final int THREE_STATE_ITEM = 2;
    public static final int NUMERIC_ITEM = 3;
    public static final int NUMERIC_INT_ITEM = 4;
    public static final int SELECT_ALL_ITEM = 5;
    public static boolean mustSaveFilter = false;
    // the collapse buttons
    private static FilterSetListViewItem activeCollapseButton; // only one should be active
    private static FilterSetListViewItem general;
    private static FilterSetListViewItem dt;
    private static FilterSetListViewItem types;
    private static FilterSetListViewItem attribs;
    //
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
    private static FilterSetListViewItem minFavPoints;
    private static FilterSetListViewItem maxFavPoints;
    int index = 0;
    private ArrayList<FilterSetEntry> lFilterSets;
    private ArrayList<FilterSetListViewItem> lFilterSetListViewItems;

    public FilterSetListView(CB_RectF rec) {
        super(rec, "FilterSetListView");
        this.setHasInvisibleItems(true);
        fillFilterSetList();
        this.setBaseAdapter(new CustomAdapter(lFilterSets, lFilterSetListViewItems));
        this.setDisposeFlag(false);
    }

    private FilterProperties getFilterProperties() {
        FilterProperties props = new FilterProperties();
        props.notAvailable = NotAvailable.getChecked();
        props.archived = Archived.getChecked();
        props.finds = Finds.getChecked();
        props.own = Own.getChecked();
        props.containsTravelbugs = ContainsTravelBugs.getChecked();
        props.favorites = Favorites.getChecked();
        props.hasUserData = HasUserData.getChecked();
        props.listingChanged = ListingChanged.getChecked();
        props.withManualWaypoint = WithManualWaypoint.getChecked();
        props.hasCorrectedCoordinates = hasCorrectedCoordinates.getChecked();

        props.minDifficulty = minDifficulty.getValue();
        props.maxDifficulty = maxDifficulty.getValue();
        props.minTerrain = minTerrain.getValue();
        props.maxTerrain = maxTerrain.getValue();
        props.minContainerSize = minContainerSize.getValue();
        props.maxContainerSize = maxContainerSize.getValue();
        props.minRating = minRating.getValue();
        props.maxRating = maxRating.getValue();
        props.minFavPoints = minFavPoints.getValue();
        props.maxFavPoints = maxFavPoints.getValue();

        for (int i = 1; i < types.getChildLength(); i++) {
            FilterSetListViewItem itm = types.getChild(i);
            int ct = itm.getFilterSetEntry().cacheType.ordinal();
            props.mCacheTypes[ct] = itm.getBoolean();
        }

        for (int i = 0; i < attribs.getChildLength(); i++) {
            props.mAttributes[i + 1] = attribs.getChild(i).getChecked();
        }

        return props;
    }

    public void setFilterProperties(FilterProperties props) {
        NotAvailable.setValue(props.notAvailable);
        Archived.setValue(props.archived);
        Finds.setValue(props.finds);
        Own.setValue(props.own);
        ContainsTravelBugs.setValue(props.containsTravelbugs);
        Favorites.setValue(props.favorites);
        HasUserData.setValue(props.hasUserData);
        ListingChanged.setValue(props.listingChanged);
        WithManualWaypoint.setValue(props.withManualWaypoint);
        hasCorrectedCoordinates.setValue(props.hasCorrectedCoordinates);

        minTerrain.setValue(props.minTerrain);
        maxTerrain.setValue(props.maxTerrain);
        minDifficulty.setValue(props.minDifficulty);
        maxDifficulty.setValue(props.maxDifficulty);
        minContainerSize.setValue(props.minContainerSize);
        maxContainerSize.setValue(props.maxContainerSize);
        minRating.setValue(props.minRating);
        maxRating.setValue(props.maxRating);
        minFavPoints.setValue(props.minFavPoints);
        maxFavPoints.setValue(props.maxFavPoints);

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
    protected void render(Batch batch) {
        super.render(batch);
        if (mustSaveFilter)
            de.droidcachebox.gdx.activities.EditFilterSettings.tmpFilterProps = getFilterProperties();
    }

    @Override
    public void onShow() {
        if (de.droidcachebox.gdx.activities.EditFilterSettings.tmpFilterProps != null && !de.droidcachebox.gdx.activities.EditFilterSettings.tmpFilterProps.toString().equals("")) {
            setFilterProperties(de.droidcachebox.gdx.activities.EditFilterSettings.tmpFilterProps);
        }
    }

    private void fillFilterSetList() {

        // add General
        general = addFilterSetCollapseItem(null, Translation.get("General"), COLLAPSE_BUTTON_ITEM);
        NotAvailable = general.addChild(addFilterSetItem(Sprites.getSprite("disabled"), Translation.get("disabled"), THREE_STATE_ITEM));
        Archived = general.addChild(addFilterSetItem(Sprites.getSprite("not-available"), Translation.get("archived"), THREE_STATE_ITEM));
        Finds = general.addChild(addFilterSetItem(Sprites.getSprite("log0icon"), Translation.get("myfinds"), THREE_STATE_ITEM));
        Own = general.addChild(addFilterSetItem(Sprites.getSprite("star"), Translation.get("myowncaches"), THREE_STATE_ITEM));
        ContainsTravelBugs = general.addChild(addFilterSetItem(Sprites.getSprite("tb"), Translation.get("withtrackables"), THREE_STATE_ITEM));
        Favorites = general.addChild(addFilterSetItem(Sprites.getSprite("favorit"), Translation.get("Favorites"), THREE_STATE_ITEM));
        HasUserData = general.addChild(addFilterSetItem(Sprites.getSprite("userdata"), Translation.get("hasuserdata"), THREE_STATE_ITEM));
        ListingChanged = general.addChild(addFilterSetItem(Sprites.getSprite(IconName.warningIcon.name()), Translation.get("ListingChanged"), THREE_STATE_ITEM));
        WithManualWaypoint = general.addChild(addFilterSetItem(Sprites.getSprite(IconName.manualWayPoint.name()), Translation.get("manualWayPoint"), THREE_STATE_ITEM));
        hasCorrectedCoordinates = general.addChild(addFilterSetItem(Sprites.getSprite("hasCorrectedCoordinates"), Translation.get("hasCorrectedCoordinates"), THREE_STATE_ITEM));

        // add D/T
        dt = addFilterSetCollapseItem(null, "D / T" + "\n" + "GC-Vote", COLLAPSE_BUTTON_ITEM);
        minDifficulty = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.get("minDifficulty"), NUMERIC_ITEM, 1, 5, 1, 0.5f));
        maxDifficulty = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.get("maxDifficulty"), NUMERIC_ITEM, 1, 5, 5, 0.5f));
        minTerrain = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.get("minTerrain"), NUMERIC_ITEM, 1, 5, 1, 0.5f));
        maxTerrain = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.get("maxTerrain"), NUMERIC_ITEM, 1, 5, 5, 0.5f));
        minContainerSize = dt.addChild(addFilterSetItem(Sprites.SizesIcons.toArray(), Translation.get("minContainerSize"), NUMERIC_ITEM, 0, 4, 0, 1));
        maxContainerSize = dt.addChild(addFilterSetItem(Sprites.SizesIcons.toArray(), Translation.get("maxContainerSize"), NUMERIC_ITEM, 0, 4, 4, 1));
        minRating = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.get("minRating"), NUMERIC_ITEM, 0, 5, 0, 0.5f));
        maxRating = dt.addChild(addFilterSetItem(Sprites.Stars.toArray(), Translation.get("maxRating"), NUMERIC_ITEM, 0, 5, 5, 0.5f));
        minFavPoints = dt.addChild(addFilterSetItem(Sprites.getSprite(IconName.FavPoi), Translation.get("minFavPoints"), NUMERIC_INT_ITEM, -1, 10000, 0, 1.0f));
        maxFavPoints = dt.addChild(addFilterSetItem(Sprites.getSprite(IconName.FavPoi), Translation.get("maxFavPoints"), NUMERIC_INT_ITEM, -1, 10000, 0, 1.0f));


        // add CacheTypes
        {
            // create categories button types
            types = addFilterSetCollapseItem(null, "Cache Types", COLLAPSE_BUTTON_ITEM);

            //add selectAll/deselectAll button item
            FilterSetListViewItem selectAllItem = addFilterSetItem(null, "", SELECT_ALL_ITEM);

            selectAllItem.setSelectAllHandler(new ISelectAllHandler() {
                @Override
                public void selectAll() {
                    for (int i = 0; i < types.getChildLength(); i++) {
                        FilterSetListViewItem itm = types.getChild(i);
                        itm.check();
                    }
                }

                @Override
                public void deselectAll() {
                    for (int i = 0; i < types.getChildLength(); i++) {
                        FilterSetListViewItem itm = types.getChild(i);
                        itm.unCheck();
                    }
                }
            });

            types.addChild(selectAllItem);

            for (int i = 0; i < CacheTypes.caches().length; i++) {
                types.addChild(addFilterSetItem(CacheTypes.caches()[i])).check();
            }
        }


        // add Attributes
        attribs = addFilterSetCollapseItem(null, "Attributes", COLLAPSE_BUTTON_ITEM);
        for (int i = 1; i < Attributes.values().length; i++) {
            attribs.addChild(addFilterSetItem(Sprites.getSprite("att-" + i + "-1Icon"), Translation.get("att_" + i + "_1"), THREE_STATE_ITEM));
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

        FilterSetListViewItem v = new FilterSetListViewItem(de.droidcachebox.gdx.activities.EditFilterSettings.ItemRec, index++, tmp);
        // initial mit GONE
        v.setInvisible();
        lFilterSetListViewItems.add(v);
        return v;
    }

    private FilterSetListViewItem addFilterSetItem(Sprite Icon, String Name, int ItemType, double i, double j, double k, double f) {
        if (lFilterSets == null) {
            lFilterSets = new ArrayList<>();
            lFilterSetListViewItems = new ArrayList<>();
        }
        //String Name, Sprite[] Icons, int itemType, double min = i, double max = j, double iniValue = k, double Step = f
        FilterSetEntry tmp = new FilterSetEntry(Name, Icon, ItemType, i, j, k, f);
        lFilterSets.add(tmp);

        FilterSetListViewItem v = new FilterSetListViewItem(de.droidcachebox.gdx.activities.EditFilterSettings.ItemRec, index++, tmp);
        // initial mit GONE
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

        FilterSetListViewItem v = new FilterSetListViewItem(de.droidcachebox.gdx.activities.EditFilterSettings.ItemRec, index++, tmp);
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

        FilterSetListViewItem v = new FilterSetListViewItem(de.droidcachebox.gdx.activities.EditFilterSettings.ItemRec, index++, tmp);
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

        v.setClickHandler((v1, x, y, pointer, button) -> {
            // only one or none should be active
            if (activeCollapseButton != null) {
                if (activeCollapseButton == v1) {
                    activeCollapseButton = null;
                } else {
                    collapseButton_Clicked(activeCollapseButton);
                    activeCollapseButton = (FilterSetListViewItem) v1;
                }
            } else {
                activeCollapseButton = (FilterSetListViewItem) v1;
            }
            collapseButton_Clicked((FilterSetListViewItem) v1);
            return false;
        });

        return v;
    }

    private void collapseButton_Clicked(FilterSetListViewItem item) {
        item.toggleChildViewState();
        this.notifyDataSetChanged();
        this.invalidate();
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

        public FilterSetEntry(String Name, Sprite icon, int itemType, double min, double max, double iniValue, double Step) {
            mName = Name;
            mIcon = icon;
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

        public void setState(int State) {
            mState = State;
        }

        public void setState(double State) {
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
            if (mNumericState > mNumericMax) {
                if (mItemType == FilterSetListView.NUMERIC_INT_ITEM) {
                    mNumericState = mNumericMax;
                } else {
                    mNumericState = mNumericMin;
                }
            }
        }

        public void minusClick() {
            mNumericState -= mNumericStep;
            if (mNumericState < 0) {
                if (mItemType == FilterSetListView.NUMERIC_INT_ITEM) {
                    mNumericState = mNumericMin;
                } else {
                    mNumericState = mNumericMax;
                }
            }
        }

        public void plusPlusClick() {
            if (mNumericState < 0) mNumericState++;
            mNumericState += (mNumericStep * 10);
            if (mNumericState > mNumericMax) {
                if (mItemType == FilterSetListView.NUMERIC_INT_ITEM) {
                    mNumericState = mNumericMax;
                } else {
                    mNumericState = mNumericMin;
                }
            }
        }

        public void minusMinusClick() {
            mNumericState -= (mNumericStep * 10);
            if (mNumericState == 0) mNumericState = -1;
            if (mNumericState < 0) {
                if (mItemType == FilterSetListView.NUMERIC_INT_ITEM) {
                    mNumericState = mNumericMin;
                } else {
                    mNumericState = mNumericMax;
                }
            }
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
