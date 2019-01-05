package CB_UI.GL_UI.Activitys;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.SQLiteInterface;

public class Import_GSAK extends ActivityBase {

    private String mPath;
    private String mDatabaseName;
    private SQLiteInterface sql;

    public Import_GSAK() {
        super("Import_GSAK");
        mPath = Config.GSAKLastUsedDatabasePath.getValue();
        if (mPath.length() == 0) {
            mPath = Config.mWorkPath + "/User";
        }
        PlatformConnector.getFile(mPath, "*.db3", Translation.Get("GSAkTitleSelectDB"), Translation.Get("GSAKButtonSelectDB"), PathAndName -> {
            File file = FileFactory.createFile(PathAndName);
            mPath = file.getParent();
            mDatabaseName = file.getName();
            Config.GSAKLastUsedDatabasePath.setValue(mPath);
            Config.AcceptChanges();
        });
        if (mPath.length() > 0) {
            sql = PlatformConnector.getSQLInstance();
            if (sql.openReadOnly(mPath + "/" + mDatabaseName)) {
               CoreCursor reader = sql.rawQuery("select * from Caches",null);
                while (!reader.isAfterLast()) {
                    String GcCode = reader.getString("Code");
                    String Name = reader.getString("Name");
                }
                reader.close();
            }
            PlatformConnector.freeSQLInstance(sql);
        }
    }

}
