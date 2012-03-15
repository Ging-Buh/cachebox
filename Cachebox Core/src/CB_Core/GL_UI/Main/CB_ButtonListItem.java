package CB_Core.GL_UI.Main;

import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

import com.badlogic.gdx.math.Vector2;

public class CB_ButtonListItem extends ListViewItemBase
{

	public CB_ButtonListItem(CB_RectF rec, int Index, CharSequence Name)
	{
		super(rec, Index, Name);
		btn = new Button(this, "Button");
	}

	public CB_ButtonListItem(int Index, CB_Button Button, CharSequence Name)
	{
		super(new CB_RectF(0, 0, 100, 100), Index, Name);
		mCB_Button = Button;
		btn = new Button(this, "Button");
	}

	CB_Button mCB_Button;
	Button btn;

	@Override
	protected void Initial()
	{
		btn.setPos(new Vector2(0, 0));
		btn.setSize(GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);
		this.addChild(btn);

	}

}
