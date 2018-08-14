package CB_Core.DAO;

import CB_Core.Database;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;
import CB_Utils.Log.Log;
import de.cb.sqlite.CoreCursor;

import java.util.Iterator;

public class TrackableListDAO {
    private static final String log = "TrackableListDAO";

    public static void WriteToDatabase(TbList trackableList) {

        TrackableDAO tDAO = new TrackableDAO();

        Iterator<Trackable> iterator = trackableList.iterator();
        Log.info(log, "TrackableListDAO WriteToDatabase Size:" + trackableList.size());

        if (iterator != null && iterator.hasNext()) {
            do {
                try {
                    Trackable tb = iterator.next();

                    Trackable tbDB = tDAO.getFromDbByGcCode(tb.getGcCode());

                    if (tbDB == null) {
                        Log.info(log, "TrackableListDAO WriteToDatabase :" + tb.getName());
                        tDAO.WriteToDatabase(tb);
                    } else {
                        Log.info(log, "TrackableListDAO UpdateDatabase :" + tb.getName());
                        tDAO.UpdateDatabase(tb);
                    }
                } catch (Exception exc) {
                    Log.err(log, "TrackableListDAO WriteToDatabase", exc);
                }

            } while (iterator.hasNext());
        }

        Log.info(log, "TrackableListDAO WriteToDatabase done.");
    }

    public static TbList ReadTbList(String where) {
        TbList trackableList = new TbList();
        CoreCursor reader = Database.FieldNotes.rawQuery("select Id ,Archived ,GcCode ,CacheId ,CurrentGoal ,CurrentOwnerName ,DateCreated ,Description ,IconUrl ,ImageUrl ,Name ,OwnerName ,Url,TypeName, Home,TravelDistance   from Trackable", null);
        reader.moveToFirst();

        while (!reader.isAfterLast()) {
            trackableList.add(new Trackable(reader));
            reader.moveToNext();
        }
        reader.close();
        return trackableList;
    }

    /**
     * Deleate all TBs
     */
    public static void clearDB() {
        Database.FieldNotes.delete("Trackable", "", null);
    }

}
