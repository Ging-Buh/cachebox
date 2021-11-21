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
package de.droidcachebox.gdx.controls;

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
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.CB_List;

public abstract class Dialog extends CB_View_Base {
    protected static float margin = -1;
    private static boolean lastNightMode = false;
    private static float mTitleVersatz = 6;
    private static NinePatch mTitle9patch;
    private static NinePatch mHeader9patch;
    private static NinePatch mCenter9patch;
    private static NinePatch mFooter9patch;
    protected String callerName = "";
    protected boolean dontRenderDialogBackground = false;
    protected float mTitleHeight = 0;
    protected float mTitleWidth = 100;
    protected boolean mHasTitle = false;
    protected float mHeaderHeight;
    protected float mFooterHeight;
    private String mTitle;
    private CB_Label titleLabel;
    private Box mContent;
    private CB_List<GL_View_Base> contentChilds = new CB_List<>();
    private ArrayList<GL_View_Base> overlayForTextMarker = new ArrayList<>();
    private ArrayList<GL_View_Base> overlay = new ArrayList<>();

    public Dialog(CB_RectF rec, String Name) {
        super(rec, Name);
        // ctor without title and footer
        mHeaderHeight = calcHeaderHeight();
        mFooterHeight = mHeaderHeight;

        if (margin <= 0)
            margin = UiSizes.getInstance().getMargin();

        try {
            if (Sprites.Dialog.get(DialogElement.footer.ordinal()) == null)
                return;// noch nicht initialisiert!
        } catch (Exception e) {
            return;
        } // noch nicht initialisiert!

        if (mTitle9patch == null || mHeader9patch == null || mCenter9patch == null || mFooter9patch == null || lastNightMode != nightMode.getValue()) {
            int pW = (int) (Sprites.Dialog.get(DialogElement.footer.ordinal()).getWidth() / 8);
            mTitle9patch = new NinePatch(Sprites.Dialog.get(DialogElement.title.ordinal()), pW, (pW * 12 / 8), pW, pW);
            mHeader9patch = new NinePatch(Sprites.Dialog.get(DialogElement.header.ordinal()), pW, pW, pW, 3);
            mCenter9patch = new NinePatch(Sprites.Dialog.get(DialogElement.center.ordinal()), pW, pW, 1, 1);
            mFooter9patch = new NinePatch(Sprites.Dialog.get(DialogElement.footer.ordinal()), pW, pW, 3, pW);
            mTitleVersatz = pW;
            lastNightMode = nightMode.getValue();
        }

        leftBorder = mCenter9patch.getLeftWidth();
        rightBorder = mCenter9patch.getRightWidth();
        topBorder = mHeader9patch.getTopHeight();
        bottomBorder = mFooter9patch.getBottomHeight();
        innerWidth = getWidth() - leftBorder - rightBorder;
        innerHeight = getHeight() - topBorder - bottomBorder;

        reziseContentBox();
    }

    public static float calcHeaderHeight() {
        return (Fonts.Measure("T").height) / 2;
    }

    public static float calcFooterHeight(boolean hasButtons) {
        if (margin <= 0)
            margin = UiSizes.getInstance().getMargin();

        return hasButtons ? UiSizes.getInstance().getButtonHeight() + margin : calcHeaderHeight();
    }

    public static Size calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon, boolean hasRemember) {
        if (margin <= 0)
            margin = UiSizes.getInstance().getMargin();

        float Width = (((UiSizes.getInstance().getButtonWidthWide() + margin) * 3) + margin);
        if (Width * 1.2 < UiSizes.getInstance().getWindowWidth())
            Width *= 1.2f;

        float MsgWidth = (Width * 0.95f) - 5 - UiSizes.getInstance().getButtonHeight();

        float MeasuredTextHeight = Fonts.measureWrapped(Text, MsgWidth).height + (margin * 4);

        float Height = (hasIcon ? Math.max(MeasuredTextHeight, UiSizes.getInstance().getButtonHeight() + (margin * 5)) : (int) MeasuredTextHeight);

        if (hasTitle) {
            Height += getTitleHeight();
        }
        Height += calcFooterHeight(hasButtons);
        if (hasRemember)
            Height = (Height + UiSizes.getInstance().getChkBoxSize().getHeight());
        Height = (Height + calcHeaderHeight());

        // min Height festlegen
        Height = Math.max(Height, UiSizes.getInstance().getButtonHeight() * 2.5f);

        // max Height festlegen
        Height = Math.min(Height, UiSizes.getInstance().getWindowHeight() * 0.95f);

        return new Size((int) Width, (int) Height);
    }

    public static float getTitleHeight() {
        GlyphLayout titleBounds = Fonts.Measure("T");
        float h = (titleBounds.height * 3);
        return h + margin * 2;
    }

    @Override
    public GL_View_Base addChild(GL_View_Base view) {
        // die Childs in die Box umleiten ausser TextMarker

        if (view instanceof SelectionMarker) {
            overlayForTextMarker.add(view);
            mContent.addChildDirect(view);

            if (mContent != null) {
                mContent.addChildDirect(view);
            } else {
                childs.add(view);
            }

        } else {
            if (mContent != null) {
                mContent.addChildDirect(view);
            } else {
                if (contentChilds != null)
                    contentChilds.add(view);
            }
        }

        return view;
    }

    @Override
    public void removeChild(GL_View_Base view) {
        if (view instanceof SelectionMarker) {
            overlayForTextMarker.remove(view);
            if (mContent != null) {
                mContent.removeChildDirect(view);
            } else {
                childs.remove(view);
            }

        } else {
            if (mContent != null) {
                mContent.removeChildDirect(view);
            } else {
                if (contentChilds != null)
                    contentChilds.remove(view);
            }
        }

    }

    @Override
    public void removeChilds() {
        if (contentChilds != null)
            contentChilds.clear();
        if (mContent != null)
            mContent.removeChilds();
    }

    @Override
    protected void initialize() {
        initialDialog();
        isInitialized = true;
    }

    protected void initialDialog() {
        if (mContent != null) {
            // InitialDialog wurde schon aufgerufen!!!
            return;
        }
        super.removeChildsDirect();

        mContent = new Box(scaleCenter(0.95f), "Dialog Content Box");

        // debug mContent.setBackground(new ColorDrawable(Color.RED));

        reziseContentBox();

        for (int i = 0; i < contentChilds.size(); i++) {
            GL_View_Base view = contentChilds.get(i);
            if (view != null && !view.isDisposed())
                mContent.addChildDirect(view);
        }

        super.addChild(mContent);

        if (overlayForTextMarker.size() > 0) {
            for (GL_View_Base view : overlayForTextMarker) {
                mContent.addChildDirect(view);
            }
        }
    }

    private void reziseContentBox() {

        if (margin <= 0)
            margin = UiSizes.getInstance().getMargin();

        if (mContent == null) {
            initialDialog();
            return;
        }

        mTitleHeight = 0;
        if (mTitle != null && !mTitle.equals("")) {
            mHasTitle = true;

            if (titleLabel == null) {
                titleLabel = new CB_Label(mTitle);
            } else {
                if (!titleLabel.getText().equals(mTitle)) {
                    titleLabel.setText(mTitle);
                }
            }
            titleLabel.setWidth(titleLabel.getTextWidth() + leftBorder + rightBorder);
            initRow();
            addLast(titleLabel, FIXED);

            mTitleHeight = titleLabel.getHeight();
            mTitleWidth = titleLabel.getWidth();
            mTitleWidth += rightBorder + leftBorder; // sonst sieht es blöd aus
        }

        mContent.setWidth(getWidth() * 0.95f);
        mContent.setHeight((getHeight() - mHeaderHeight - mFooterHeight - mTitleHeight - margin));
        float centerversatzX = getHalfWidth() - mContent.getHalfWidth();
        float centerversatzY = mFooterHeight;// halfHeight - mContent.getHalfHeight();
        mContent.setPos(centerversatzX, centerversatzY);

    }

    @Override
    public void renderChildren(final Batch batch, ParentInfo parentInfo) {
        if (isDisposed())
            return;
        batch.flush();

        try {
            if (mHeader9patch != null && !dontRenderDialogBackground) {
                mHeader9patch.draw(batch, 0, getHeight() - mTitleHeight - mHeaderHeight, getWidth(), mHeaderHeight);
            }
            if (mFooter9patch != null && !dontRenderDialogBackground) {
                mFooter9patch.draw(batch, 0, 0, getWidth(), mFooterHeight + 2);
            }
            if (mCenter9patch != null && !dontRenderDialogBackground) {
                mCenter9patch.draw(batch, 0, mFooterHeight, getWidth(), (getHeight() - mFooterHeight - mHeaderHeight - mTitleHeight) + 3.5f);
            }

            if (mHasTitle) {
                if (mTitleWidth < getWidth()) {
                    if (mTitle9patch != null && !dontRenderDialogBackground) {
                        mTitle9patch.draw(batch, 0, getHeight() - mTitleHeight - mTitleVersatz, mTitleWidth, mTitleHeight);
                    }
                } else {
                    if (mHeader9patch != null && !dontRenderDialogBackground) {
                        mHeader9patch.draw(batch, 0, getHeight() - mTitleHeight - mTitleVersatz, mTitleWidth, mTitleHeight);
                    }
                }
            }

            batch.flush();
        } catch (Exception ignored) {
        }

        if (isDisposed())
            return;

        super.renderChildren(batch, parentInfo);

        try {
            if (overlay != null) {
                for (GL_View_Base view : overlay) {
                    try {
                        // do not call view.render(batch), else contained childs aren't called
                        if (view != null && view.isVisible()) {

                            if (childsInvalidate)
                                view.invalidate();

                            getMyInfoForChild().setParentInfo(myParentInfo);
                            getMyInfoForChild().setWorldDrawRec(intersectRec);

                            getMyInfoForChild().add(view.getX(), view.getY());

                            batch.setProjectionMatrix(getMyInfoForChild().Matrix());
                            nDepthCounter++;

                            view.renderChildren(batch, getMyInfoForChild());
                            nDepthCounter--;
                            batch.setProjectionMatrix(myParentInfo.Matrix());
                        }

                    } catch (java.util.ConcurrentModificationException e) {
                        // da die Liste nicht mehr gültig ist, brechen wir hier den Iterator ab
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

    }

    public SizeF getContentSize() {
        reziseContentBox();
        return mContent.getSize();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
        reziseContentBox();
    }

    public void addChildToOverlay(GL_View_Base view) {
        overlay.add(view);
    }

    public void removeChildsFromOverlay() {
        overlay.clear();
    }

    // always automatically called on changing size
    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        reziseContentBox();
    }

    public void setFooterHeight(float newFooterHeight) {
        mFooterHeight = newFooterHeight;
        reziseContentBox();
    }

    @Override
    public String toString() {
        return getName() + " X,Y/Width,Height = " + getX() + "," + getY() + "/" + getWidth() + "," + getHeight() + " created by: " + callerName;
    }

    protected void setCallerName(String newCallerName) {
        callerName = newCallerName;
    }

    @Override
    public void dispose() {
        mTitle = null;
        callerName = null;

        if (titleLabel != null)
            titleLabel.dispose();
        titleLabel = null;

        if (mContent != null)
            mContent.dispose();
        mContent = null;

        if (contentChilds != null) {
            for (int i = 0; i < contentChilds.size(); i++) {
                GL_View_Base v = contentChilds.get(i);
                if (v != null && !v.isDisposed())
                    v.dispose();
            }
            contentChilds.clear();
        }
        contentChilds = null;

        if (overlayForTextMarker != null) {
            for (GL_View_Base v : overlayForTextMarker) {
                if (v != null)
                    v.dispose();
            }
            overlayForTextMarker.clear();
        }
        overlayForTextMarker = null;

        if (overlay != null) {
            for (GL_View_Base v : overlay) {
                if (v != null)
                    v.dispose();
            }
            overlay.clear();
        }
        overlay = null;

        super.dispose();
    }
}
