package CB_Core.GL_UI;

/**
 * Stellt die Identifizierung einer Menu Eintrags dar.
 * 
 * @author Longri
 */
public class MenuID
{
	public final static int TRACK_LIST_CREATE = 0;
	public final static int TRACK_LIST_LOAD = 1;
	public final static int TRACK_LIST_DELETE = 2;
	public final static int TRACK_LIST_P2P = 3;
	public final static int TRACK_LIST_PROJECT = 4;
	public final static int TRACK_LIST_CIRCLE = 5;
	public final static int SHOW_WP_CONTEXT_MENU = 6;
	public final static int SHOW_FIELD_NOTE_CONTEXT_MENU = 7;
	public final static int SHOW_TB_CONTEXT_MENU = 8;

	private int id;
	private String name;

	public MenuID(int ID, String Name)
	{
		id = ID;
		name = Name;
	}

	public MenuID(int ID)
	{
		id = ID;
		name = "";
	}

	public int getID()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}
}
