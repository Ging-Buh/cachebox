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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.WrapType;
import de.droidcachebox.database.Draft;
import de.droidcachebox.database.GeoCacheLogType;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DraftViewItem extends ListViewItemBackground {
    private static final String sKlasse = "DraftViewItem";
    private static NinePatch backheader;
    private static float headHeight;
    public boolean headerClicked;
    private Draft draft;
    private Image ivTyp;
    private Image ivCacheType;

    public DraftViewItem(CB_RectF rec, int Index, Draft fromDraft) {
        super(rec, Index, "");
        draft = fromDraft;
        if (draft == null) {
            CB_Button btnLoadMore = new CB_Button(Translation.get("LoadMore"));
            btnLoadMore.setWidth(getWidth());
            btnLoadMore.setClickHandler((v, x, y, pointer, button) -> {
                if (mOnClickListener != null)
                    mOnClickListener.onClick(v, x, y, pointer, button);
                return true;
            });
            addLast(btnLoadMore);
        } else {
            backGroundIsInitialized = false;
            float measuredLabelHeight = Fonts.Measure("T").height * 1.5f;
            headHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + (UiSizes.getInstance().getMargin());

            Box header = new Box(getWidth(), headHeight);
            header.setClickHandler((v, x, y, pointer, button) -> {
                headerClicked = true;
                return false;
            });
            headerClicked = false;
            // private void iniImage() {
            ivTyp = new Image(getLeftWidth(), getHeight() - (headHeight / 2) - (UiSizes.getInstance().getButtonHeight() / 1.5f / 2), UiSizes.getInstance().getButtonHeight() / 1.5f, UiSizes.getInstance().getButtonHeight() / 1.5f, "", false);
            header.addNext(ivTyp, FIXED);
            ivTyp.setDrawable(getTypeIcon(draft));
            // private void iniDateLabel() {
            SimpleDateFormat postFormater = new SimpleDateFormat("dd.MMM (HH:mm)", Locale.US);
            String foundNumber = "";
            if (draft.foundNumber > 0) {
                foundNumber = "#" + draft.foundNumber + " @ ";
            }
            String dateString = foundNumber + postFormater.format(draft.timestamp);
            float DateLength = 100;

            try {
                DateLength = Fonts.Measure(dateString).width;
            } catch (Exception ex) {
                Log.err(sKlasse, "iniDateLabel", ex);
            }

            CB_Label lblDate = new CB_Label(" lblDate", getWidth() - getRightWidth() - DateLength, getHeight() - (headHeight / 2) - (measuredLabelHeight / 2), DateLength, measuredLabelHeight);
            lblDate.setFont(Fonts.getNormal());
            lblDate.setText(dateString);
            header.addLast(lblDate);
            lblDate.setHAlignment(CB_Label.HAlignment.RIGHT);
            addLast(header);
            // private void iniCacheTypeImage() {
            ivCacheType = new Image(getLeftWidth() + UiSizes.getInstance().getMargin(), getHeight() - headHeight - (UiSizes.getInstance().getButtonHeight()) - UiSizes.getInstance().getMargin(), UiSizes.getInstance().getButtonHeight(),
                    UiSizes.getInstance().getButtonHeight(), "", false);
            addChild(ivCacheType);

            if (draft.isTbDraft) {
                ivCacheType.setImageURL(draft.TbIconUrl);
            } else {
                ivCacheType.setDrawable(new SpriteDrawable(Sprites.getSprite("big" + GeoCacheType.values()[draft.cacheType].name())));
            }
            // private void iniCacheNameLabel() {
            CB_RectF rectF = new CB_RectF(ivCacheType.getMaxX() + UiSizes.getInstance().getMargin(), getHeight() - headHeight - measuredLabelHeight - UiSizes.getInstance().getMargin(),
                    getWidth() - ivCacheType.getMaxX() - (UiSizes.getInstance().getMargin() * 2), measuredLabelHeight);
            EditTextField mCacheName = new EditTextField(rectF, this, "lblCacheName");
            mCacheName.setText(draft.isTbDraft ? draft.TbName : draft.CacheName);
            mCacheName.setEditable(false);
            mCacheName.setBackground(null, null);
            mCacheName.showFromLineNo(0);
            mCacheName.setCursorPosition(0);
            addChild(mCacheName);
            //private void iniGcCodeLabel() {
            rectF = new CB_RectF(mCacheName.getX(), mCacheName.getY() - measuredLabelHeight - UiSizes.getInstance().getMargin(), getWidth() - ivCacheType.getMaxX() - (UiSizes.getInstance().getMargin() * 2), measuredLabelHeight);
            EditTextField mGcCode = new EditTextField(rectF, this, "lblGcCode");
            mGcCode.setText(draft.gcCode);
            mGcCode.setEditable(false);
            mGcCode.setBackground(null, null);
            addChild(mGcCode);
            // private void iniCommentLabel() {
            rectF = new CB_RectF(getLeftWidth() + UiSizes.getInstance().getMargin(), 0, getWidth() - getLeftWidth() - getRightWidth() - (UiSizes.getInstance().getMargin() * 2),
                    getHeight() - (getHeight() - mGcCode.getY()) - UiSizes.getInstance().getMargin());
            EditTextField mComment = new EditTextField(rectF, this, "Comment");
            mComment.setWrapType(WrapType.WRAPPED);
            mComment.setText(draft.comment);
            mComment.setEditable(false);
            mComment.setClickable(false);
            mComment.setBackground(null, null);
            mComment.showFromLineNo(0);
            mComment.setCursorPosition(0);
            addChild(mComment);

        }
    }

    public static Drawable getTypeIcon(Draft fne) {
        GeoCacheLogType type = fne.type;
        if (fne.isTbDraft) {
            Sprite spr = null;
            if (type == GeoCacheLogType.discovered)
                spr = Sprites.getSprite(IconName.TBDISCOVER.name());
            if (type == GeoCacheLogType.dropped_off)
                spr = Sprites.getSprite(IconName.TBDROP.name());
            if (type == GeoCacheLogType.grab_it)
                spr = Sprites.getSprite(IconName.TBGRAB.name());
            if (type == GeoCacheLogType.retrieve)
                spr = Sprites.getSprite(IconName.TBPICKED.name());
            if (type == GeoCacheLogType.visited)
                spr = Sprites.getSprite(IconName.TBVISIT.name());
            if (type == GeoCacheLogType.note)
                spr = Sprites.getSprite(IconName.TBNOTE.name());
            if (spr == null)
                return null;
            return new SpriteDrawable(spr);
        } else {
            return new SpriteDrawable(Sprites.LogIcons.get(fne.typeIcon));
        }
    }

    @Override
    protected void initialize() {
        backheader = new NinePatch(Sprites.getSprite("listrec-header"), 8, 8, 8, 8);
        super.initialize();
    }

    @Override
    public void render(Batch batch) {
        if (draft == null) {
            return;
        }

        Color color = batch.getColor();
        float oldAlpha = color.a;
        float oldRed = color.r;
        float oldGreen = color.g;
        float oldBlue = color.b;

        boolean uploaded = false;
        if (draft.uploaded)
            uploaded = true;

        if (uploaded) {
            color.a = 0.5f;
            color.r = 0.6f;
            color.g = 0.65f;
            color.b = 0.6f;
            batch.setColor(color);
        }

        super.render(batch);
        if (backheader != null) {
            backheader.draw(batch, 0, getHeight() - headHeight, getWidth(), headHeight);
        } else {
            resetIsInitialized();
        }

        if (uploaded) {
            ivTyp.setColor(color);
            ivCacheType.setColor(color);
            color.a = oldAlpha;
            color.r = oldRed;
            color.g = oldGreen;
            color.b = oldBlue;
            batch.setColor(color);
        }

    }
}
