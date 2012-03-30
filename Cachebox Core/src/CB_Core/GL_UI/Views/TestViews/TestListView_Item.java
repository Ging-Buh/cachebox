package CB_Core.GL_UI.Views.TestViews;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.math.Vector2;

public class TestListView_Item extends ListViewItemBase
{

	public TestListView_Item(CB_RectF rec, int Index, CharSequence Name)
	{
		super(rec, Index, Name);
		// TODO Auto-generated constructor stub
	}

	public TestListView_Item(int Index, String string, Boolean backGroundChanger, CharSequence Name)
	{
		super(new CB_RectF(0, 0, 100, 100), Index, Name);
		this.string = string;
	}

	private Label lbl;
	private String string;

	@Override
	protected void Initial()
	{
		lbl = new Label(CB_RectF.ScaleCenter(this, 0.9f), "Label for " + this.getName());
		lbl.setFont(Fonts.getNormal());
		lbl.setPos(new Vector2(5, 2.5f));
		lbl.setText(string);
		this.addChild(lbl);
	}

}
