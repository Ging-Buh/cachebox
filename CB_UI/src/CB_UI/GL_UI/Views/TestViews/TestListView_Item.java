package CB_UI.GL_UI.Views.TestViews;

import com.badlogic.gdx.math.Vector2;

import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.Math.CB_RectF;

public class TestListView_Item extends ListViewItemBase {

	public TestListView_Item(CB_RectF rec, int Index, String Name) {
		super(rec, Index, Name);
	}

	public TestListView_Item(int Index, String string, Boolean backGroundChanger, String Name) {
		super(new CB_RectF(0, 0, 100, 100), Index, Name);
		this.string = string;
	}

	private Label lbl;
	private String string;

	@Override
	protected void Initial() {
		lbl = new Label(this.name + " lbl", CB_RectF.ScaleCenter(this, 0.9f), "Label for " + this.name);
		lbl.setFont(Fonts.getNormal());
		lbl.setPos(new Vector2(5, 2.5f));
		lbl.setText(string);
		this.addChild(lbl);
	}

	@Override
	protected void SkinIsChanged() {

	}

}
