package CB_Core.GL_UI;

import CB_Core.GL_UI.ViewID.UI_Pos;
import CB_Core.GL_UI.ViewID.UI_Type;

public class ViewConst
{
	public static final ViewID MAP_VIEW = new ViewID(ViewID.MAP_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID CACHE_LIST_VIEW = new ViewID(ViewID.CACHE_LIST_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Left);
	public static final ViewID LOG_VIEW = new ViewID(ViewID.LOG_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Left);
	public static final ViewID DESCRIPTION_VIEW = new ViewID(ViewID.DESCRIPTION_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID SPOILER_VIEW = new ViewID(ViewID.SPOILER_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID NOTES_VIEW = new ViewID(ViewID.NOTES_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID SOLVER_VIEW = new ViewID(ViewID.SOLVER_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID COMPASS_VIEW = new ViewID(ViewID.COMPASS_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Left);
	public static final ViewID FIELD_NOTES_VIEW = new ViewID(ViewID.FIELD_NOTES_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID ABOUT_VIEW = new ViewID(ViewID.ABOUT_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Left);
	public static final ViewID JOKER_VIEW = new ViewID(ViewID.JOKER_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Left);
	public static final ViewID TRACK_LIST_VIEW = new ViewID(ViewID.TRACK_LIST_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Left);
	public static final ViewID TB_LIST_VIEW = new ViewID(ViewID.TB_LIST_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Left);
	public static final ViewID WAYPOINT_VIEW = new ViewID(ViewID.WAYPOINT_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Left);

	public static final ViewID TEST_VIEW = new ViewID(ViewID.TEST_VIEW, UI_Type.OpenGl, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID CREDITS_VIEW = new ViewID(ViewID.CREDITS_VIEW, UI_Type.OpenGl, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID GL_MAP_VIEW = new ViewID(ViewID.GL_MAP_VIEW, UI_Type.OpenGl, UI_Pos.Left, UI_Pos.Right);
	public static final ViewID MAP_CONTROL_TEST_VIEW = new ViewID(ViewID.MAP_CONTROL_TEST_VIEW, UI_Type.OpenGl, UI_Pos.Left, UI_Pos.Right);

	public static final ViewID SETTINGS = new ViewID(ViewID.SETTINGS, UI_Type.Activity, null, null);
	public static final ViewID FILTER_SETTINGS = new ViewID(ViewID.FILTER_SETTINGS, UI_Type.Activity, null, null);
	public static final ViewID IMPORT = new ViewID(ViewID.IMPORT, UI_Type.Activity, null, null);
}
