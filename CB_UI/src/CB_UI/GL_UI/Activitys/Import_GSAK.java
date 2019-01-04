package CB_UI.GL_UI.Activitys;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Label;

public class Import_GSAK extends ActivityBase {

    private Label lblSelectDB;
    private EditTextField edtSelectDB;

    public Import_GSAK() {
        super("Import_GSAK");
        // todo or last used
        PlatformConnector.getFile(Config.mWorkPath + "/User", "*.db3", Translation.Get("GSAkTitleSelectDB"), Translation.Get("GSAKButtonSelectDB"), new PlatformConnector.IgetFileReturnListener() {
            @Override
            public void returnFile(String Path) {
                if (Path != null) {

                }
            }
        });
        edtSelectDB = new EditTextField(this, "*" + Translation.Get(""));
    }

}
