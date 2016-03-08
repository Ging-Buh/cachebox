package CB_UI.GL_UI.Controls;

import java.util.ConcurrentModificationException;

import CB_UI.Config;
import CB_UI.GL_UI.Main.Actions.QuickButton.QuickActions;
import CB_UI.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.H_ListView;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Util.MoveableList;

public class QuickButtonList extends H_ListView {

	private final float btnHeight;
	private float btnYPos;

	public static QuickButtonList that;

	public QuickButtonList(CB_RectF rec, String Name) {
		super(rec, Name);
		that = this;
		btnHeight = UiSizes.that.getQuickButtonListHeight() * 0.93f;
		setBackground(Sprites.ButtonBack);

		CB_RectF btnRec = new CB_RectF(0, 0, btnHeight, btnHeight);

		btnYPos = this.getHalfHeight() - btnRec.getHalfHeight();

		this.setBaseAdapter(new CustomAdapter());
		this.setDisposeFlag(false);

		registerSkinChangedEvent();
	}

	@Override
	public void onResized(CB_RectF rec) {
		super.onResized(rec);
		btnYPos = this.getHalfHeight() - btnHeight / 2;

		if (quickButtonList != null && !quickButtonList.isEmpty())
			for (int i = 0; i > quickButtonList.size(); i++) {
				setButtonYPos(i);
			}

	}

	@Override
	public void Initial() {
		super.Initial();
		chkIsDraggable();
	}

	private void chkIsDraggable() {
		if (quickButtonList != null) {
			if (this.getMaxItemCount() < quickButtonList.size()) {
				this.setDraggable();
			} else {
				this.setUnDraggable();
			}
		}
	}

	@Override
	public boolean click(int x, int y, int pointer, int button) {
		// send Event to Buttons
		synchronized (childs) {
			for (int i = 0, n = childs.size(); i < n; i++) {
				GL_View_Base btn = childs.get(i);
				if (btn != null) {
					btn.onTouchUp(x, y, pointer, button);
					if (btn.contains(x, y)) {
						return btn.click(x, y, pointer, button);
					}
				}
			}
		}
		return super.click(x, y, pointer, button);
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button) {
		synchronized (this.childs) {
			for (int i = 0, n = childs.size(); i < n; i++) {
				GL_View_Base btn = childs.get(i);
				if (btn != null)
					btn.onTouchDown(x, y, pointer, button);
			}
		}
		return super.onTouchDown(x, y, pointer, button);
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button) {
		synchronized (this.childs) {
			for (int i = 0, n = childs.size(); i < n; i++) {
				GL_View_Base btn = childs.get(i);
				if (btn != null)
					btn.onTouchUp(x, y, pointer, button);
			}
		}
		return super.onTouchUp(x, y, pointer, button);
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {

		synchronized (this.childs) {

			try {
				for (int i = 0, n = childs.size(); i < n; i++) {
					GL_View_Base btn = childs.get(i);
					if (btn != null)
						btn.onTouchDragged(x, y, pointer, KineticPan);
				}
			} catch (ConcurrentModificationException e) {
				return false;
			}
		}

		return super.onTouchDragged(x, y, pointer, KineticPan);
	}

	public static MoveableList<QuickButtonItem> quickButtonList;

	public class CustomAdapter implements Adapter {

		public CustomAdapter() {
			readQuickButtonItemsList();

		}

		public long getItemId(int position) {
			return position;
		}

		public ListViewItemBase getView(int position) {

			if (quickButtonList == null)
				return null;

			QuickButtonItem v = setButtonYPos(position);
			return v;
		}

		@Override
		public int getCount() {
			if (quickButtonList == null)
				return 0;
			return quickButtonList.size();
		}

		@Override
		public float getItemSize(int position) {
			return btnHeight;
		}
	}

	private QuickButtonItem setButtonYPos(int position) {
		QuickButtonItem v = quickButtonList.get(position);
		v.setSize(btnHeight, btnHeight);
		v.setY(btnYPos);// center btn on y direction
		return v;
	}

	private void readQuickButtonItemsList() {
		if (quickButtonList == null) {
			String ConfigActionList = Config.quickButtonList.getValue();
			String[] ConfigList = ConfigActionList.split(",");
			quickButtonList = QuickActions.getListFromConfig(ConfigList, btnHeight);
		}
		chkIsDraggable();
	}

	@Override
	public void notifyDataSetChanged() {
		quickButtonList = null;
		readQuickButtonItemsList();
		super.notifyDataSetChanged();
	}

	@Override
	protected void SkinIsChanged() {
		quickButtonList = null;
		readQuickButtonItemsList();
		setBackground(Sprites.ButtonBack);
		reloadItems();
		ListViewItemBackground.ResetBackground();
	}

}
