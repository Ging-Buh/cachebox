package CB_Core.GL_UI.Views;

import java.awt.Paint;
import java.text.SimpleDateFormat;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.Types.FieldNoteEntry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class FieldNoteViewItem extends ListViewItemBackground
{
	private static NinePatch backheader;
	private FieldNoteEntry fieldnote;
	private Image ivTyp;
	private Label lblFounds;
	private Label lblDate;
	private Image ivCacheType;
	private Label lblCacheName;
	private Label lblGcCode;
	private Label lblComment;

	private float secondTab = 0;
	private static float MeasuredLabelHeight = 0;

	public FieldNoteViewItem(CB_RectF rec, int Index, FieldNoteEntry fieldnote)
	{
		super(rec, Index, "");

		this.fieldnote = fieldnote;
		mBackIsInitial = false;
		MeasuredLabelHeight = Fonts.Measure("T").height * 1.5f;
		headHeight = (UI_Size_Base.that.getButtonHeight() / 1.5f) + (Dialog.margin);

		iniImage();
		iniFoundLabel();
		iniDateLabel();
		iniCacheTypeImage();
		iniCacheNameLabel();
		iniGcCodeLabel();
		iniCommentLabel();
	}

	private void iniImage()
	{
		ivTyp = new Image(getLeftWidth(), this.height - (headHeight / 2) - (UI_Size_Base.that.getButtonHeight() / 1.5f / 2),
				UI_Size_Base.that.getButtonHeight() / 1.5f, UI_Size_Base.that.getButtonHeight() / 1.5f, "");
		this.addChild(ivTyp);
		ivTyp.setDrawable(new SpriteDrawable(SpriteCache.LogIcons.get(fieldnote.typeIcon)));
		secondTab = ivTyp.getMaxX() + (Dialog.margin * 2);
	}

	private void iniFoundLabel()
	{
		lblFounds = new Label(secondTab, this.height - (headHeight / 2) - (MeasuredLabelHeight / 2), width - secondTab - getRightWidth()
				- Dialog.margin, MeasuredLabelHeight, "");
		lblFounds.setFont(Fonts.getNormal());
		lblFounds.setText(fieldnote.typeString);
		this.addChild(lblFounds);
	}

	private void iniDateLabel()
	{
		// SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
		SimpleDateFormat postFormater = new SimpleDateFormat("dd.MMM (HH:mm)");
		String dateString = postFormater.format(fieldnote.timestamp);
		float DateLength = 100;

		try
		{
			DateLength = Fonts.Measure(dateString).width;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		lblDate = new Label(this.width - getRightWidth() - DateLength, lblFounds.getY(), DateLength, MeasuredLabelHeight, "");
		lblDate.setFont(Fonts.getNormal());
		lblDate.setText(dateString);
		this.addChild(lblDate);
	}

	private void iniCacheTypeImage()
	{
		ivCacheType = new Image(getLeftWidth() + Dialog.margin, this.height - headHeight - (UI_Size_Base.that.getButtonHeight())
				- Dialog.margin, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), "");
		this.addChild(ivCacheType);
		ivCacheType.setDrawable(new SpriteDrawable(SpriteCache.BigIcons.get(fieldnote.cacheType)));
		secondTab = ivTyp.getMaxX() + (Dialog.margin * 3);

	}

	private void iniCacheNameLabel()
	{
		lblCacheName = new Label(ivCacheType.getMaxX() + Dialog.margin, this.height - headHeight - MeasuredLabelHeight - Dialog.margin,
				this.width - ivCacheType.getMaxX() - (Dialog.margin * 2), MeasuredLabelHeight, "");
		lblCacheName.setFont(Fonts.getNormal());
		lblCacheName.setText(fieldnote.CacheName);
		this.addChild(lblCacheName);

	}

	private void iniGcCodeLabel()
	{
		lblGcCode = new Label(lblCacheName.getX(), lblCacheName.getY() - MeasuredLabelHeight - Dialog.margin, this.width
				- ivCacheType.getMaxX() - (Dialog.margin * 2), MeasuredLabelHeight, "");
		lblGcCode.setFont(Fonts.getNormal());
		lblGcCode.setText(fieldnote.gcCode);
		this.addChild(lblGcCode);

	}

	private void iniCommentLabel()
	{
		lblComment = new Label(getLeftWidth() + Dialog.margin, 0, this.width - getLeftWidth() - getRightWidth() - (Dialog.margin * 2),
				this.height - (this.height - lblGcCode.getY()) - Dialog.margin, "");
		lblComment.setFont(Fonts.getNormal());
		lblComment.setWrappedText(fieldnote.comment);
		this.addChild(lblComment);

	}

	@Override
	protected void Initial()
	{
		backheader = new NinePatch(SpriteCache.getThemedSprite("listrec-header"), 8, 8, 8, 8);
		super.Initial();
	}

	// static Member
	public static Paint Linepaint;
	public static Paint KopfPaint;
	public static Paint TextPaint;
	private static float headHeight;

	public static BitmapFontCache cacheNamePaint;

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void render(SpriteBatch batch)
	{
		Color color = batch.getColor();
		float oldAlpha = color.a;
		float oldRed = color.r;
		float oldGreen = color.g;
		float oldBlue = color.b;

		if (fieldnote.uploaded)
		{
			color.a = 0.5f;
			color.r = 0.6f;
			color.g = 0.65f;
			color.b = 0.6f;
			batch.setColor(color);
		}

		super.render(batch);
		if (backheader != null)
		{
			backheader.draw(batch, 0, this.height - headHeight, this.width, headHeight);
		}
		else
		{
			resetInitial();
		}

		if (fieldnote.uploaded)
		{
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
