package CB_UI.GL_UI.Views;

import CB_Core.DB.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheLite;
import CB_Core.Types.Waypoint;
import CB_UI.GlobalCore;
import CB_UI.Events.SelectedCacheEvent;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

public class NotesView extends CB_View_Base implements SelectedCacheEvent
{
	NotesView that;
	EditTextField edNotes;
	Cache aktCache;
	boolean mustLoadNotes;

	public NotesView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		mustLoadNotes = true;
		edNotes = new EditTextField(this, this, WrapType.WRAPPED, "Note");
		edNotes.setZeroPos();
		this.addChild(edNotes);
		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
		SelectedCacheEventList.Add(this);

		KeyboardFocusChangedEventList.Add(new KeyboardFocusChangedEvent()
		{

			@Override
			public void KeyboardFocusChanged(EditTextFieldBase focus)
			{
				chkFocus(focus);
			}
		});
	}

	private void chkFocus(EditTextFieldBase focus)
	{
		if (focus == edNotes)
		{
			edNotes.setHeight(NotesView.this.getHalfHeight());
			edNotes.setY(NotesView.this.getHalfHeight());
		}
		else
		{
			edNotes.setHeight(NotesView.this.getHeight());
			edNotes.setY(0);
		}
	}

	@Override
	public void onShow()
	{
		chkFocus(GL.that.getKeyboardFocus());

		if (mustLoadNotes)
		{
			String text = aktCache != null ? Database.GetNote(aktCache) : "";
			if (text == null) text = "";
			edNotes.setText(text);
			mustLoadNotes = false;
		}
	}

	@Override
	public void onHide()
	{
		// Save changed Note text
		String text = edNotes.getText().toString();
		if (text != null)
		{
			try
			{
				Database.SetNote(aktCache, text);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void Initial()
	{

	}

	private void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (aktCache != cache)
		{
			mustLoadNotes = true;
			aktCache = cache;
		}
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	@Override
	public void SelectedCacheChanged(CacheLite cache, Waypoint waypoint)
	{
		aktCache = new Cache(cache);
		mustLoadNotes = true;
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		chkFocus(GL.that.getKeyboardFocus());
	}
}
