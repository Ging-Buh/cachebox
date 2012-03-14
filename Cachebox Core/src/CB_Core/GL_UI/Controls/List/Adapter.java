package CB_Core.GL_UI.Controls.List;


public interface Adapter
{

	/**
	 * How many items are in the data set represented by this Adapter.
	 * 
	 * @return Count of items.
	 */
	int getCount();

	/**
	 * Get a View that displays the data at the specified position in the data set.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the item whose view we want.
	 * @return A View corresponding to the data at the specified position.
	 */
	ListViewItemBase getView(int position);

}
