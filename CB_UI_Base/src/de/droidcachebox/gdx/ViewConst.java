package de.droidcachebox.gdx;

import de.droidcachebox.gdx.ViewID.UI_Pos;
import de.droidcachebox.gdx.ViewID.UI_Type;

public class ViewConst {

    public static final ViewID DESCRIPTION_VIEW = new ViewID(ViewID.DESCRIPTION_VIEW, UI_Type.Android, UI_Pos.Left, UI_Pos.Right);

    public static final ViewID NAVIGATE_TO = new ViewID(ViewID.NAVIGATE_TO, UI_Type.Activity, null, null);
    public static final ViewID VOICE_REC = new ViewID(ViewID.VOICE_REC, UI_Type.Activity, null, null);
    public static final ViewID TAKE_PHOTO = new ViewID(ViewID.TAKE_PHOTO, UI_Type.Activity, null, null);
    public static final ViewID VIDEO_REC = new ViewID(ViewID.VIDEO_REC, UI_Type.Activity, null, null);
    public static final ViewID Share = new ViewID(ViewID.WhatsApp, UI_Type.Activity, null, null);
}
