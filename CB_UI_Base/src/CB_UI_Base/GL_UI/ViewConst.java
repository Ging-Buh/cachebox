package CB_UI_Base.GL_UI;

import CB_UI_Base.GL_UI.ViewID.UI_Pos;
import CB_UI_Base.GL_UI.ViewID.UI_Type;

public class ViewConst {
    public static final ViewID MAP_VIEW = new ViewID(ViewID.MAP_VIEW, UI_Type.OpenGl, UI_Pos.Left, UI_Pos.Right);

    public static final ViewID DESCRIPTION_VIEW = new ViewID(ViewID.DESCRIPTION_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Right);

    public static final ViewID NAVIGATE_TO = new ViewID(ViewID.NAVIGATE_TO, UI_Type.Activity, null, null);
    public static final ViewID VOICE_REC = new ViewID(ViewID.VOICE_REC, UI_Type.Activity, null, null);
    public static final ViewID TAKE_PHOTO = new ViewID(ViewID.TAKE_PHOTO, UI_Type.Activity, null, null);
    public static final ViewID VIDEO_REC = new ViewID(ViewID.VIDEO_REC, UI_Type.Activity, null, null);

}
