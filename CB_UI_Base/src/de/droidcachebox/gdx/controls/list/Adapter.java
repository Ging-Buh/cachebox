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
     * Gibt die Größe zur Berechnung der Position eines Items zurück.</br> </br>Für V_ListView => die Höhe </br>Für H_ListView => die Breite
     * will be called for each position in advance before first call of getView
     *
     * @param position ?
     * @return ?
     */
    float getItemSize(int position);

}
