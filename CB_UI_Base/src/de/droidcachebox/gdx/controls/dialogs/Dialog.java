/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.gdx.controls.dialogs;

import static de.droidcachebox.settings.AllSettings.nightMode;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;

import java.util.ArrayList;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.ParentInfo;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.DialogElement;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.SelectionMarker;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.CB_List;

public abstract class Dialog extends CB_View_Base {
    protected static float margin = -1;
    private static boolean lastNightMode = false;
    private static float mTitleOffset = 6;
    private static NinePatch mTitle9patch;
    private static NinePatch mHeader9patch;
    private static NinePatch mCenter9patch;
    private static NinePatch mFooter9patch;
    protected final Box contentBox;
    private final CB_List<GL_View_Base> contentChildren = new CB_List<>();
    private final ArrayList<GL_View_Base> overlayForTextMarker = new ArrayList<>();
    private final ArrayList<GL_View_Base> overlay = new ArrayList<>();
    protected String callerName = "";
    protected boolean doNotRenderDialogBackground = false;
    protected float mTitleHeight = 0;
    protected float mTitleWidth = 100;
    protected boolean mHasTitle = false;
    protected float mHeaderHeight;
    protected float mFooterHeight;
    protected String title;
    private CB_Label titleLabel;

    public Dialog(CB_RectF rec, String name) {
        super(rec, name);
        // constructor without title and footer
        mHeaderHeight = calcHeaderHeight();
        mFooterHeight = mHeaderHeight;

        if (margin <= 0)
            margin = UiSizes.getInstance().getMargin();

        if (mTitle9patch == null || mHeader9patch == null || mCenter9patch == null || mFooter9patch == null || lastNightMode != nightMode.getValue()) {
            int pW = (int) (Sprites.Dialog.get(DialogElement.footer.ordinal()).getWidth() / 8);
            mTitle9patch = new NinePatch(Sprites.Dialog.get(DialogElement.title.ordinal()), pW, (pW * 12 / 8), pW, pW);
            mHeader9patch = new NinePatch(Sprites.Dialog.get(DialogElement.header.ordinal()), pW, pW, pW, 3);
            mCenter9patch = new NinePatch(Sprites.Dialog.get(DialogElement.center.ordinal()), pW, pW, 1, 1);
            mFooter9patch = new NinePatch(Sprites.Dialog.get(DialogElement.footer.ordinal()), pW, pW, 3, pW);
            mTitleOffset = pW;
            lastNightMode = nightMode.getValue();
        }

        leftBorder = mCenter9patch.getLeftWidth();
        rightBorder = mCenter9patch.getRightWidth();
        topBorder = mHeader9patch.getTopHeight();
        bottomBorder = mFooter9patch.getBottomHeight();
        innerWidth = getWidth() - leftBorder - rightBorder;
        innerHeight = getHeight() - topBorder - bottomBorder;
        margin = UiSizes.getInstance().getMargin();
        contentBox = new Box(scaleCenter(0.95f), "contentBox");
        super.addChild(contentBox);

    }

    public static float calcHeaderHeight() {
        return (Fonts.measure("T").height) / 2;
    }

    public static float calcFooterHeight(boolean hasButtons) {
        if (margin <= 0)
            margin = UiSizes.getInstance().getMargin();

        return hasButtons ? UiSizes.getInstance().getButtonHeight() + margin : calcHeaderHeight();
    }

    public static Size calcMsgBoxSize(String text, boolean hasTitle, boolean hasButtons, boolean hasRemember) {
        if (text == null) text = "";
        if (margin <= 0)
            margin = UiSizes.getInstance().getMargin();

        float width = (((UiSizes.getInstance().getButtonWidthWide() + margin) * 3) + margin);
        if (width * 1.2 < UiSizes.getInstance().getWindowWidth())
            width *= 1.2f;

        float msgWidth = (width * 0.95f) - 5 - UiSizes.getInstance().getButtonHeight();

        float height = Fonts.measureWrapped(text, msgWidth).height + (margin * 4);

        if (hasTitle) {
            height += getTitleHeight();
        }
        height += calcFooterHeight(hasButtons);
        if (hasRemember)
            height = (height + UiSizes.getInstance().getChkBoxSize().getHeight());
        height = (height + calcHeaderHeight());

        // define min Height
        height = Math.max(height, UiSizes.getInstance().getButtonHeight() * 2.5f);

        // define max Height
        height = Math.min(height, UiSizes.getInstance().getWindowHeight() * 0.95f);

        return new Size((int) width, (int) height);
    }

    public static float getTitleHeight() {
        GlyphLayout titleBounds = Fonts.measure("T");
        float h = (titleBounds.height * 3);
        return h + margin * 2;
    }

    @Override
    public GL_View_Base addChild(GL_View_Base view) {
        // add everything to contentBox except TextMarker

        if (view instanceof SelectionMarker) {
            overlayForTextMarker.add(view);
            contentBox.addChildDirect(view);
        } else {
            if (contentBox != null) {
                contentBox.addChildDirect(view);
            } else {
                if (contentChildren != null)
                    contentChildren.add(view);
            }
        }

        return view;
    }

    @Override
    public void removeChild(GL_View_Base child) {
        if (child instanceof SelectionMarker) {
            overlayForTextMarker.remove(child);
            if (contentBox != null) {
                contentBox.removeChildDirect(child);
            } else {
                childs.remove(child);
            }

        } else {
            if (contentBox != null) {
                contentBox.removeChildDirect(child);
            } else {
                if (contentChildren != null)
                    contentChildren.remove(child);
            }
        }

    }

    @Override
    public void removeChildren() {
        if (contentChildren != null)
            contentChildren.clear();
        if (contentBox != null)
            contentBox.removeChildren();
    }

    protected void initialDialog() {

        resizeContentBox();

        for (int i = 0; i < contentChildren.size(); i++) {
            GL_View_Base view = contentChildren.get(i);
            if (view != null && !view.isDisposed())
                contentBox.addChildDirect(view);
        }

        super.addChild(contentBox);

        if (overlayForTextMarker.size() > 0) {
            for (GL_View_Base view : overlayForTextMarker) {
                contentBox.addChildDirect(view);
            }
        }
    }

    private void resizeContentBox() {

        mTitleHeight = 0;
        if (title != null && !title.equals("")) {
            mHasTitle = true;

            if (titleLabel == null) {
                titleLabel = new CB_Label(title);
                titleLabel.setHAlignment(CB_Label.HAlignment.CENTER);
            } else {
                if (!titleLabel.getText().equals(title)) {
                    titleLabel.setText(title);
                }
            }
            titleLabel.setWidth(titleLabel.getTextWidth() + leftBorder + rightBorder);
            initRow();
            addLast(titleLabel, FIXED);

            mTitleHeight = titleLabel.getHeight();
            mTitleWidth = titleLabel.getWidth();
            mTitleWidth += rightBorder + leftBorder; // else looks bad
        }

        contentBox.setHeight((getHeight() - mHeaderHeight - mFooterHeight - mTitleHeight - margin));
        float centerOffsetX = getHalfWidth() - contentBox.getHalfWidth();
        float centerOffsetY = mFooterHeight; // halfHeight - mContent.getHalfHeight();
        contentBox.setPos(centerOffsetX, centerOffsetY);

    }

    @Override
    public void renderChildren(final Batch batch, ParentInfo parentInfo) {
        if (isDisposed)
            return;
        batch.flush();

        try {
            if (mHeader9patch != null && !doNotRenderDialogBackground) {
                mHeader9patch.draw(batch, 0, getHeight() - mTitleHeight - mHeaderHeight, getWidth(), mHeaderHeight);
            }
            if (mFooter9patch != null && !doNotRenderDialogBackground) {
                mFooter9patch.draw(batch, 0, 0, getWidth(), mFooterHeight + 2);
            }
            if (mCenter9patch != null && !doNotRenderDialogBackground) {
                mCenter9patch.draw(batch, 0, mFooterHeight, getWidth(), (getHeight() - mFooterHeight - mHeaderHeight - mTitleHeight) + 3.5f);
            }

            if (mHasTitle) {
                if (mTitleWidth < getWidth()) {
                    if (mTitle9patch != null && !doNotRenderDialogBackground) {
                        mTitle9patch.draw(batch, 0, getHeight() - mTitleHeight - mTitleOffset, mTitleWidth, mTitleHeight);
                    }
                } else {
                    if (mHeader9patch != null && !doNotRenderDialogBackground) {
                        mHeader9patch.draw(batch, 0, getHeight() - mTitleHeight - mTitleOffset, mTitleWidth, mTitleHeight);
                    }
                }
            }

            batch.flush();
        } catch (Exception ignored) {
        }

        if (isDisposed)
            return;

        super.renderChildren(batch, parentInfo);

        try {
            if (overlay != null) {
                for (GL_View_Base view : overlay) {
                    try {
                        // do not call view.render(batch), else contained children aren't called
                        if (view != null && view.isVisible()) {

                            if (childsInvalidate)
                                view.invalidate();

                            myInfoForChild.setParentInfo(myParentInfo);
                            myInfoForChild.setWorldDrawRec(intersectRec);

                            myInfoForChild.add(view.getX(), view.getY());

                            batch.setProjectionMatrix(myInfoForChild.Matrix());
                            nDepthCounter++;

                            view.renderChildren(batch, myInfoForChild);
                            nDepthCounter--;
                            batch.setProjectionMatrix(myParentInfo.Matrix());
                        }

                    } catch (java.util.ConcurrentModificationException e) {
                        // cause the list is no longer correct, we cancel here
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

    }

    public SizeF getContentSize() {
        resizeContentBox();
        return contentBox.getSize();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        resizeContentBox();
    }

    public void addChildToOverlay(GL_View_Base view) {
        overlay.add(view);
    }

    public void clearOverlay() {
        overlay.clear();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        contentBox.setWidth(getWidth() * 0.95f);
        resizeContentBox();
    }

    public void setFooterHeight(float newFooterHeight) {
        mFooterHeight = newFooterHeight;
        resizeContentBox();
    }

    @Override
    public String toString() {
        return getName() + " X,Y/Width,Height = " + getX() + "," + getY() + "/" + getWidth() + "," + getHeight() + " created by: " + callerName;
    }

    protected void setCallerName(String newCallerName) {
        callerName = newCallerName;
    }
}
