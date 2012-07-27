package CB_Core.GL_UI.Activitys;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ActivityBase extends Dialog
{
	protected ActivityBase that;
	protected float Left;
	protected float Right;
	protected float Top;
	protected float Bottom;
	protected float MesuredLabelHeight;
	protected float ButtonHeight;

	public ActivityBase(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		this.setBackground(new NinePatch(SpriteCache.getThemedSprite("activity-back"), 16, 16, 16, 16));

		Left = nineBackground.getLeftWidth();
		Right = nineBackground.getRightWidth();
		Top = nineBackground.getTopHeight();
		Bottom = nineBackground.getBottomHeight();
		MesuredLabelHeight = Fonts.Mesure("T").height * 1.5f;
		ButtonHeight = UiSizes.getButtonHeight();
	}

	@Override
	protected void SkinIsChanged()
	{
		this.setBackground(new NinePatch(SpriteCache.getThemedSprite("activity-back"), 16, 16, 16, 16));
	}

	@Override
	public GL_View_Base addChild(GL_View_Base view)
	{
		this.addChildDirekt(view);

		return view;
	}

	@Override
	public void removeChilds()
	{
		this.removeChildsDirekt();
	}

	@Override
	protected void Initial()
	{
		// do not call super, it wants clear childs
	}

	protected void finish()
	{
		GL_Listener.glListener.closeActivity();
	}

	@Override
	public void onShow()
	{
		// register for TextField render, the most Activitys have an Textfield
		GL_Listener.glListener.addRenderView(this, GL_Listener.FRAME_RATE_TEXT_FIELD);
	}

	public void show()
	{
		GL_Listener.glListener.showActivity(this);
	}

	public static CB_RectF ActivityRec()
	{
		float w = Math.min(UiSizes.getSmallestWidth(), UiSizes.getWindowHeight() * 0.66f);

		return new CB_RectF(0, 0, w, UiSizes.getWindowHeight());
	}

	@Override
	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{
		// clear dialog BackGrounds
		if (mHeader9patch != null) mHeader9patch = null;
		if (mFooter9patch != null) mFooter9patch = null;
		if (mCenter9patch != null) mCenter9patch = null;
		if (mTitle9patch != null) mTitle9patch = null;

		super.renderChilds(batch, parentInfo);
	}

}
