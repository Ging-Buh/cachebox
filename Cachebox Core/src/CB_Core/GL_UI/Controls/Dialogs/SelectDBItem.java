package CB_Core.GL_UI.Controls.Dialogs;

import java.io.File;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.Math.CB_RectF;

public class SelectDBItem extends ListViewItemBase
{

	Label nameLabel;
	Label countLabel;
	protected static final float left = 20;

	protected static float mLabelHeight = -1;
	protected static float mLabelYPos = -1;
	protected static float mLabelWidth = -1;

	public SelectDBItem(CB_RectF rec, int Index, File file, String count)
	{
		super(rec, Index, file.getName());

		if (mLabelHeight == -1)
		{
			mLabelHeight = height * 0.7f;
			mLabelYPos = (height - mLabelHeight) / 2;
			mLabelWidth = width - (left * 2);
		}

		nameLabel = new Label(left, mLabelYPos, width, mLabelHeight, "NameLabel");
		nameLabel.setFont(Fonts.getBig());
		nameLabel.setVAlignment(VAlignment.TOP);
		nameLabel.setText(file.getName());
		this.addChild(nameLabel);

		countLabel = new Label(left, mLabelYPos, width, mLabelHeight, "NameLabel");
		countLabel.setFont(Fonts.getBubbleNormal());
		countLabel.setVAlignment(VAlignment.BOTTOM);
		countLabel.setText(count);
		this.addChild(countLabel);

		this.setClickable(true);
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		return true;
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

}
