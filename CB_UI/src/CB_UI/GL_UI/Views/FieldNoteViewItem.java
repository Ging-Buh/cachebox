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

import java.text.SimpleDateFormat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_Core.CacheTypes;
import CB_Core.LogTypes;
import CB_Core.Types.FieldNoteEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class FieldNoteViewItem extends ListViewItemBackground {
	private static NinePatch backheader;
	private FieldNoteEntry fieldnote;
	private Image ivTyp;
	private Label lblDate;
	private Image ivCacheType;
	private EditTextField mCacheName;
	private EditTextField mGcCode;
	private EditTextField mComment;

	private static float MeasuredLabelHeight = 0;

	public FieldNoteViewItem(CB_RectF rec, int Index, FieldNoteEntry fieldnote) {
		super(rec, Index, "");

		this.fieldnote = fieldnote;
		mBackIsInitial = false;
		MeasuredLabelHeight = Fonts.Measure("T").height * 1.5f;
		headHeight = (UI_Size_Base.that.getButtonHeight() / 1.5f) + (UI_Size_Base.that.getMargin());

		iniImage();
		iniDateLabel();
		iniCacheTypeImage();
		iniCacheNameLabel();
		iniGcCodeLabel();
		iniCommentLabel();

		if (this.fieldnote == null) {
			Button btnLoadMore = new Button(Translation.Get("LoadMore"));
			btnLoadMore.setWidth(this.getWidth());
			btnLoadMore.setOnClickListener(new OnClickListener() {

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
					if (FieldNoteViewItem.this.mOnClickListener != null)
						FieldNoteViewItem.this.mOnClickListener.onClick(v, x, y, pointer, button);
					return true;
				}
			});
			this.addChild(btnLoadMore);
		}
	}

	private void iniImage() {
		if (this.fieldnote == null)
			return;
		ivTyp = new Image(getLeftWidth(), this.getHeight() - (headHeight / 2) - (UI_Size_Base.that.getButtonHeight() / 1.5f / 2), UI_Size_Base.that.getButtonHeight() / 1.5f, UI_Size_Base.that.getButtonHeight() / 1.5f, "", false);
		this.addChild(ivTyp);
		ivTyp.setDrawable(getTypeIcon(this.fieldnote));
	}

	public static Drawable getTypeIcon(FieldNoteEntry fne) {
		LogTypes type = fne.type;

		if (fne.isTbFieldNote) {

			Sprite spr = null;

			if (type == LogTypes.discovered)
				spr = Sprites.getSprite(IconName.TBDISCOVER.name());
			if (type == LogTypes.dropped_off)
				spr = Sprites.getSprite(IconName.TBDROP.name());
			if (type == LogTypes.grab_it)
				spr = Sprites.getSprite(IconName.TBGRAB.name());
			if (type == LogTypes.retrieve)
				spr = Sprites.getSprite(IconName.TBPICKED.name());
			if (type == LogTypes.visited)
				spr = Sprites.getSprite(IconName.TBVISIT.name());
			if (type == LogTypes.note)
				spr = Sprites.getSprite(IconName.TBNOTE.name());
			if (spr == null)
				return null;
			return new SpriteDrawable(spr);
		} else {
			return new SpriteDrawable(Sprites.LogIcons.get(fne.typeIcon));
		}
	}

	private void iniDateLabel() {
		if (this.fieldnote == null)
			return;
		// SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
		SimpleDateFormat postFormater = new SimpleDateFormat("dd.MMM (HH:mm)");
		String foundNumber = "";
		if (fieldnote.foundNumber > 0) {
			foundNumber = "#" + fieldnote.foundNumber + " @ ";
		}
		String dateString = foundNumber + postFormater.format(fieldnote.timestamp);
		float DateLength = 100;

		try {
			DateLength = Fonts.Measure(dateString).width;
		} catch (Exception e) {

			e.printStackTrace();
		}

		lblDate = new Label(this.name + " lblDate", this.getWidth() - getRightWidth() - DateLength, this.getHeight() - (headHeight / 2) - (MeasuredLabelHeight / 2), DateLength, MeasuredLabelHeight);
		lblDate.setFont(Fonts.getNormal());
		lblDate.setText(dateString);
		this.addChild(lblDate);
	}

	private void iniCacheTypeImage() {
		if (this.fieldnote == null)
			return;
		ivCacheType = new Image(getLeftWidth() + UI_Size_Base.that.getMargin(), this.getHeight() - headHeight - (UI_Size_Base.that.getButtonHeight()) - UI_Size_Base.that.getMargin(), UI_Size_Base.that.getButtonHeight(),
				UI_Size_Base.that.getButtonHeight(), "", false);
		this.addChild(ivCacheType);

		if (fieldnote.isTbFieldNote) {
			ivCacheType.setImageURL(fieldnote.TbIconUrl);
		} else {
			ivCacheType.setDrawable(new SpriteDrawable(Sprites.getSprite("big" + CacheTypes.values()[fieldnote.cacheType].name())));
		}
	}

	private void iniCacheNameLabel() {
		if (this.fieldnote == null)
			return;
		CB_RectF rectF = new CB_RectF(ivCacheType.getMaxX() + UI_Size_Base.that.getMargin(), this.getHeight() - headHeight - MeasuredLabelHeight - UI_Size_Base.that.getMargin(),
				this.getWidth() - ivCacheType.getMaxX() - (UI_Size_Base.that.getMargin() * 2), MeasuredLabelHeight);
		mCacheName = new EditTextField(rectF, this, this.name + " lblCacheName");
		mCacheName.setText(fieldnote.isTbFieldNote ? fieldnote.TbName : fieldnote.CacheName);
		mCacheName.setEditable(false);
		mCacheName.setBackground(null, null);
		mCacheName.showFromLineNo(0);
		mCacheName.setCursorPosition(0);
		this.addChild(mCacheName);
	}

	private void iniGcCodeLabel() {
		if (this.fieldnote == null)
			return;
		CB_RectF rectF = new CB_RectF(mCacheName.getX(), mCacheName.getY() - MeasuredLabelHeight - UI_Size_Base.that.getMargin(), this.getWidth() - ivCacheType.getMaxX() - (UI_Size_Base.that.getMargin() * 2), MeasuredLabelHeight);
		mGcCode = new EditTextField(rectF, this, this.name + " lblGcCode");
		mGcCode.setText(fieldnote.gcCode);
		mGcCode.setEditable(false);
		mGcCode.setBackground(null, null);
		this.addChild(mGcCode);
	}

	private void iniCommentLabel() {
		if (this.fieldnote == null)
			return;
		CB_RectF rectF = new CB_RectF(getLeftWidth() + UI_Size_Base.that.getMargin(), 0, this.getWidth() - getLeftWidth() - getRightWidth() - (UI_Size_Base.that.getMargin() * 2),
				this.getHeight() - (this.getHeight() - mGcCode.getY()) - UI_Size_Base.that.getMargin());
		mComment = new EditTextField(rectF, this, this.name + " lblComment");
		mComment.setWrapType(WrapType.WRAPPED);
		mComment.setText(fieldnote.comment);
		mComment.setEditable(false);
		mComment.setBackground(null, null);
		mComment.showFromLineNo(0);
		mComment.setCursorPosition(0);
		this.addChild(mComment);

	}

	@Override
	protected void Initial() {
		backheader = new NinePatch(Sprites.getSprite("listrec-header"), 8, 8, 8, 8);
		super.Initial();
	}

	// // static Member
	// public static Paint Linepaint;
	// public static Paint KopfPaint;
	// public static Paint TextPaint;
	private static float headHeight;

	public static BitmapFontCache cacheNamePaint;

	@Override
	public void render(Batch batch) {
		if (fieldnote == null) {
			// super.render(batch);
			return;
		}

		Color color = batch.getColor();
		float oldAlpha = color.a;
		float oldRed = color.r;
		float oldGreen = color.g;
		float oldBlue = color.b;

		boolean uploaded = false;
		if (fieldnote.uploaded)
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
			backheader.draw(batch, 0, this.getHeight() - headHeight, this.getWidth(), headHeight);
		} else {
			resetInitial();
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
