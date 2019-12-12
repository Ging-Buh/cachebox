/*
 * Copyright (C) 2015 team-cachebox.de
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
package de.droidcachebox.gdx.views;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Clipboard;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.WrapType;
import de.droidcachebox.database.LogEntry;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.popups.ICopyPaste;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

import java.text.SimpleDateFormat;

public class LogViewItem extends ListViewItemBackground implements ICopyPaste {
    public static BitmapFontCache cacheNamePaint;
    private static NinePatch backheader;
    private static float MeasuredLabelHeight = 0;
    private static float headHeight;
    private LogEntry logEntry;
    private Image ivTyp;
    private CB_Label lblFinder;
    private CB_Label lblDate;
    private EditTextField mComment;
    private float secondTab = 0;
    private Clipboard clipboard = PlatformUIBase.getClipboard();

    public LogViewItem(CB_RectF rec, int Index, LogEntry logEntry) {
        super(rec, Index, "");
        this.setLongClickable(true);
        this.logEntry = logEntry;
        backGroundIsInitialized = false;
        MeasuredLabelHeight = Fonts.Measure("T").height * 1.5f;
        headHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + (UiSizes.getInstance().getMargin());

        iniImage();
        iniFoundLabel();
        iniDateLabel();
        iniCommentLabel();
        setClickHandler((v1, x, y, pointer, button) -> {
            // ShowLogs.getInstance().getContextMenu().show();
            return true;
        });
    }



    private void iniImage() {
        ivTyp = new Image(getLeftWidth(), this.getHeight() - (headHeight / 2) - (UiSizes.getInstance().getButtonHeight() / 1.5f / 2), UiSizes.getInstance().getButtonHeight() / 1.5f, UiSizes.getInstance().getButtonHeight() / 1.5f, "", false);
        this.addChild(ivTyp);
        ivTyp.setDrawable(new SpriteDrawable(Sprites.LogIcons.get(logEntry.logTypes.getIconID())));
        secondTab = ivTyp.getMaxX() + (UiSizes.getInstance().getMargin() * 2);
    }

    private void iniFoundLabel() {
        lblFinder = new CB_Label(this.name + " lblFinder", secondTab, this.getHeight() - (headHeight / 2) - (MeasuredLabelHeight / 2), getWidth() - secondTab - getRightWidth() - UiSizes.getInstance().getMargin(), MeasuredLabelHeight, logEntry.finder);
        this.addChild(lblFinder);
    }

    private void iniDateLabel() {
        // SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
        SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy");
        String dateString = postFormater.format(logEntry.logDate);
        float DateLength = Fonts.Measure(dateString).width;

        lblDate = new CB_Label(this.name + " lblDate", this.getWidth() - getRightWidth() - DateLength, this.getHeight() - (headHeight / 2) - (MeasuredLabelHeight / 2), DateLength, MeasuredLabelHeight, dateString);
        this.addChild(lblDate);
    }

    // static Member

    private void iniCommentLabel() {
        CB_RectF rectF = new CB_RectF(getLeftWidth(), 0, this.getWidth() - getLeftWidthStatic() - getRightWidthStatic() - (UiSizes.getInstance().getMargin() * 2), this.getHeight() - headHeight - UiSizes.getInstance().getMargin());
        mComment = new EditTextField(rectF, this, "mComment");
        mComment.setWrapType(WrapType.WRAPPED);
        mComment.setText(logEntry.logText);
        mComment.setEditable(false);
        mComment.setClickable(false);
        mComment.setBackground(null, null);
        mComment.showFromLineNo(0);
        mComment.setCursorPosition(0);
        this.addChild(mComment);
    }

    @Override
    protected void initialize() {
        backheader = new NinePatch(Sprites.getSprite("listrec-header"), 8, 8, 8, 8);
        super.initialize();
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
        if (backheader != null) {
            backheader.draw(batch, 0, this.getHeight() - headHeight, this.getWidth(), headHeight);
        } else {
            resetInitial();
        }

    }

    public boolean onTouchDown(int x, int y, int pointer, int button) {

        isPressed = true;

        return false;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        isPressed = false;

        return false;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        isPressed = false;

        return false;
    }

    @Override
    public String pasteFromClipboard() {
        return null;
    }

    @Override
    public String copyToClipboard() {
        clipboard.setContents(this.logEntry.logText);
        return this.logEntry.logText;
    }

    @Override
    public String cutToClipboard() {
        return null;
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
