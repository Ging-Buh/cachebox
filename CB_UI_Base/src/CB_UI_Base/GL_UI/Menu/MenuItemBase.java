package CB_UI_Base.GL_UI.Menu;

import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;

public class MenuItemBase extends ListViewItemBackground {

	protected Object data = null;

	public MenuItemBase(CB_RectF rec, int Index, String Name) {
		super(rec, Index, Name);
	}

	@Override
	protected void SkinIsChanged() {

	}

}
