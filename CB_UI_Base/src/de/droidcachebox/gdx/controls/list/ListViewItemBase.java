package de.droidcachebox.gdx.controls.list;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;

public abstract class ListViewItemBase extends CB_View_Base implements Comparable<ListViewItemBase> {

    public boolean isSelected = false;
    protected int mIndex;

    public ListViewItemBase(CB_RectF rec, int index, String name) {
        super(rec, name);
        mIndex = index;
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
        return Integer.compare(mIndex, another.mIndex);
    }

    @Override
    public String toString() {
        return "ListItem:" + mIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof ListViewItemBase)
            return mIndex == ((ListViewItemBase) obj).mIndex;
        return false;
    }

}
