package de.droidcachebox.gdx.controls.list;

import de.droidcachebox.gdx.CB_View_Base;

public interface IScrollbarParent {
    CB_View_Base getView();

    boolean isDraggable();

    float getScrollPos();

    float getAllListSize();

    void setListPos(float value);

    float getFirstItemSize();

    float getLastItemSize();

    void chkSlideBack();

}
