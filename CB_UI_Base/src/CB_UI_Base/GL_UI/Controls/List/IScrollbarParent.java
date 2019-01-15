package CB_UI_Base.GL_UI.Controls.List;

import CB_UI_Base.GL_UI.CB_View_Base;

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
