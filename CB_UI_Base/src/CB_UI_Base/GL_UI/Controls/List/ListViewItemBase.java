package CB_UI_Base.GL_UI.Controls.List;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.Math.CB_RectF;

public abstract class ListViewItemBase extends CB_View_Base implements Comparable<ListViewItemBase> {

    public boolean isSelected = false;
    protected int mIndex;

    /**
     * Constructor
     *
     * @param rec
     * @param Index Index in der List
     * @param Name
     */
    public ListViewItemBase(CB_RectF rec, int Index, String Name) {
        super(rec, Name);
        mIndex = Index;
        this.setClickable(true);
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int Index) {
        mIndex = Index;
    }

    @Override
    public int compareTo(ListViewItemBase another) {
        if (mIndex == another.mIndex)
            return 0;

        if (mIndex < another.mIndex)
            return -1;

        return 1;
    }

    @Override
    public String toString() {
        return "ListItem:" + mIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof ListViewItemBase) {
            return mIndex == ((ListViewItemBase) obj).mIndex;
        }

        return false;
    }

}
