package de.droidcachebox.gdx.controls.list;

public interface Adapter {

    /**
     * How many items are in the dataSet represented by this Adapter.
     *
     * @return Count of items.
     */
    int getCount();

    /**
     * Get a View that displays the data at the specified position in the dataSet.
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @return A View corresponding to the data at the specified position.
     */
    ListViewItemBase getView(int position);

    /**
     * will be called for each position in advance before first call of getView</br>
     * !!! so, if the view is created within the getView, there must be a fixed ItemSize </br>
     * !!! otherwise (if the view is created in advance (!do not dispose)), the use of the real size is possible </br>
     * !!! creation of view in advance is suitable, if not too much elements. Be aware of memory usage </br>
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @return the height for V_ListView, the width for H_ListView
     */
    float getItemSize(int position);

}
