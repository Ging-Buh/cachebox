package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Events.KeyboardFocusChangedEvent;
import CB_Core.Events.KeyboardFocusChangedEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public class NotesView extends CB_View_Base implements SelectedCacheEvent
{
	NotesView that;
	EditWrapedTextField edNotes;
	Cache aktCache;
	boolean mustLoadNotes;

	public NotesView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		mustLoadNotes = true;
		edNotes = new EditWrapedTextField(this, this, TextFieldType.MultiLineWraped, "Note");
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
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		aktCache = cache;
		mustLoadNotes = true;
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		chkFocus(GL.that.getKeyboardFocus());
	}
}
