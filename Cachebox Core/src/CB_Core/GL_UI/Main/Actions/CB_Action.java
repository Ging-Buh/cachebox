package CB_Core.GL_UI.Main.Actions;

import CB_Core.Log.Logger;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action
{

	public static int AID_TEST1 = -1;
	public static int AID_TEST2 = -2;

	public static int AID_SHOW_MAP = 0;
	public static int AID_SHOW_HINT = 1;
	public static int AID_SHOW_CACHELIST = 2;
	public static int AID_SHOW_CACHELIST_CONTEXT_MENU = 3;
	public static int AID_SHOW_COMPASS = 3;
	public static int AID_SHOW_CREDITS = 4;
	public static int AID_SHOW_DESCRIPTION = 5;
	public static int AID_SHOW_FIELDNOTES = 6;
	public static int AID_SHOW_JOKERS = 7;
	public static int AID_SHOW_LOGS = 8;
	public static int AID_SHOW_NOTES = 9;
	public static int AID_SHOW_SOLVER = 10;
	public static int AID_SHOW_SPOILER = 11;
	public static int AID_SHOW_TRACKABLELIST = 12;
	public static int AID_SHOW_TRACKLIST = 13;
	public static int AID_SHOW_WAYPOINTS = 14;
	public static int AID_SHOW_SETTINGS = 15;
	public static int AID_TRACKLIST_CREATE = 16;
	public static int AID_TRACKLIST_LOAD = 17;
	public static int AID_TRACKLIST_DELETE = 18;
	public static int AID_SHOW_FILTER_SETTINGS = 19;
	public static int AID_NAVIGATE_TO = 20;
	public static int AID_TRACK_REC = 21;
	public static int AID_VOICE_REC = 22;
	public static int AID_TAKE_PHOTO = 23;
	public static int AID_VIDEO_REC = 24;
	public static int AID_DELETE_CACHES = 25;
	public static int AID_PARKING = 26;
	public static int AID_DAY_NIGHT = 27;
	public static int AID_LOCK = 28;
	public static int AID_QUIT = 29;
	public static int AID_SHOW_ABOUT = 30;

	protected String name;
	protected int id;
	protected String nameExtention = "";

	/**
	 * Constructor
	 * 
	 * @param name
	 *            = Translation ID
	 * @param id
	 *            = Action ID ( AID_xxxx )
	 */
	public CB_Action(String name, int id)
	{
		super();
		this.name = name;
		this.id = id;
	}

	public CB_Action(String name, String nameExtention, int id)
	{
		super();
		this.name = name;
		this.id = id;
		this.nameExtention = nameExtention;
	}

	public void CallExecute()
	{
		Logger.LogCat("ACTION => " + name + " execute");
		Execute();
	}

	protected void Execute()
	{
		return;
	}

	public String getName()
	{
		return name;
	}

	public String getNameExtention()
	{
		return nameExtention;
	}

	public int getId()
	{
		return id;
	}

	/**
	 * hiermit kann der Menüpunkt enabled oder disabled werden
	 * 
	 * @return
	 */
	public boolean getEnabled()
	{
		return true;
	}

	public Sprite getIcon()
	{
		return null;
	}

	public boolean getIsCheckable()
	{
		return false;
	}

	public boolean getIsChecked()
	{
		return false;
	}

}
