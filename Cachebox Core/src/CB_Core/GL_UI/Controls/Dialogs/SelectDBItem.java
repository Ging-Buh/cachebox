package CB_Core.GL_UI.Controls.Dialogs;

import java.io.File;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.NinePatch;

public class SelectDBItem extends ListViewItemBase
{

	Label nameLabel;
	float left = 20;

	public SelectDBItem(CB_RectF rec, int Index, File file)
	{
		super(rec, Index, file.getName());

		nameLabel = new Label(left, 0, width, height, "NameLabel");
		nameLabel.setText(file.getName());
		this.addChild(nameLabel);
		this.setClickable(true);
	}

	@Override
	protected void Initial()
	{
		setBackground();

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		return true;
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

	private void setBackground()
	{

		Boolean BackGroundChanger = ((this.getIndex() % 2) == 1);

		if (isSelected)
		{
			this.setBackground(new NinePatch(SpriteCache.getThemedSprite("listrec_selected"), 20, 20, 20, 20));
		}
		else if (BackGroundChanger)
		{
			this.setBackground(new NinePatch(SpriteCache.getThemedSprite("listrec_first"), 20, 20, 20, 20));
		}
		else
		{
			this.setBackground(new NinePatch(SpriteCache.getThemedSprite("listrec_secend"), 20, 20, 20, 20));
		}

		GL_Listener.glListener.renderOnce(this.getName() + " SetBackGround");
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

}
