package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.Types.Trackable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TrackableListViewItem extends ListViewItemBackground
{
	private Image img;
	private Label lblName;

	public TrackableListViewItem(CB_RectF rec, int Index, Trackable TB)
	{
		super(rec, Index, TB.getName());

		float hw = this.getHeight() - this.getTopHeight() - this.getBottomHeight();

		img = new Image(this.getLeftWidth(), this.getBottomHeight(), hw, hw, "IconImage");
		img.setImageURL(TB.getIconUrl());
		this.addChild(img);

		lblName = new Label(img.getMaxX() + UI_Size_Base.that.getMargin(), img.getY(), this.width - img.getMaxX()
				- UI_Size_Base.that.getMargin(), img.getHeight(), "Label Name");
		lblName.setWrappedText(TB.getName());
		this.addChild(lblName);
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);

	}

}
