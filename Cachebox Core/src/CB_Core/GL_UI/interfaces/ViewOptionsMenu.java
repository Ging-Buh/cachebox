package CB_Core.GL_UI.interfaces;

/**
 * Das Interface, welches jedes CB View enthällt.
 * 
 * @author Longri
 */
public interface ViewOptionsMenu
{
	public int GetMenuId();

	public void OnShow();

	public void OnHide();

	public void OnFree();

	public int GetContextMenuId();

	// TODO diese Methoden müssen noch implementiert werden, sobald das Context Menu im Core geschrieben wurde.

	// public void ActivityResult(int requestCode, int resultCode, Intent data);
	// public boolean ItemSelected(MenuItem item);
	// public void BeforeShowMenu(Menu menu);
	// public void BeforeShowContextMenu(Menu menu);
	// public boolean ContextMenuItemSelected(MenuItem item);
}
