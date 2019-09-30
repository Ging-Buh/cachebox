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
package CB_UI.GL_UI.Views;

import CB_Core.Types.LogEntry;
import CB_UI.GlobalCore;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.interfaces.ICopyPaste;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Clipboard;

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
    private Clipboard clipboard = GlobalCore.getDefaultClipboard();

    public LogViewItem(CB_RectF rec, int Index, LogEntry logEntry) {
        super(rec, Index, "");
        this.setLongClickable(true);
        this.logEntry = logEntry;
        mBackIsInitial = false;
        MeasuredLabelHeight = Fonts.Measure("T").height * 1.5f;
        headHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + (UiSizes.getInstance().getMargin());

        iniImage();
        iniFoundLabel();
        iniDateLabel();
        iniCommentLabel();
    }

    private void iniImage() {
        ivTyp = new Image(getLeftWidth(), this.getHeight() - (headHeight / 2) - (UiSizes.getInstance().getButtonHeight() / 1.5f / 2), UiSizes.getInstance().getButtonHeight() / 1.5f, UiSizes.getInstance().getButtonHeight() / 1.5f, "", false);
        this.addChild(ivTyp);
        ivTyp.setDrawable(new SpriteDrawable(Sprites.LogIcons.get(logEntry.Type.getIconID())));
        secondTab = ivTyp.getMaxX() + (UiSizes.getInstance().getMargin() * 2);
    }

    private void iniFoundLabel() {
        lblFinder = new CB_Label(this.name + " lblFinder", secondTab, this.getHeight() - (headHeight / 2) - (MeasuredLabelHeight / 2), getWidth() - secondTab - getRightWidth() - UiSizes.getInstance().getMargin(), MeasuredLabelHeight, logEntry.Finder);
        this.addChild(lblFinder);
    }

    private void iniDateLabel() {
        // SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
        SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy");
        String dateString = postFormater.format(logEntry.Timestamp);
        float DateLength = Fonts.Measure(dateString).width;

        lblDate = new CB_Label(this.name + " lblDate", this.getWidth() - getRightWidth() - DateLength, this.getHeight() - (headHeight / 2) - (MeasuredLabelHeight / 2), DateLength, MeasuredLabelHeight, dateString);
        this.addChild(lblDate);
    }

    // static Member

    private void iniCommentLabel() {
        CB_RectF rectF = new CB_RectF(getLeftWidth(), 0, this.getWidth() - getLeftWidthStatic() - getRightWidthStatic() - (UiSizes.getInstance().getMargin() * 2), this.getHeight() - headHeight - UiSizes.getInstance().getMargin());
        mComment = new EditTextField(rectF, this, "mComment");
        mComment.setWrapType(WrapType.WRAPPED);
        mComment.setText(logEntry.Comment);
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
        clipboard.setContents(this.logEntry.Comment);
        return this.logEntry.Comment;
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
