package de.droidcachebox.database;

import java.util.Iterator;

import de.droidcachebox.utils.log.Log;

public class TrackableListDAO {
    private static final String log = "TrackableListDAO";

    public static void WriteToDatabase(TBList trackableList) {

        TrackableDAO tDAO = new TrackableDAO();

        Iterator<Trackable> iterator = trackableList.iterator();
        Log.info(log, "TrackableListDAO writeToDatabase Size:" + trackableList.size());
        if (iterator.hasNext()) {
            do {
                try {
                    Trackable trackable = iterator.next();
                    Trackable tbDB = tDAO.getFromDbByGcCode(trackable.getTbCode());
                    if (tbDB == null) {
                        Log.info(log, "TrackableListDAO writeToDatabase :" + trackable.getName());
                        tDAO.writeToDatabase(trackable);
                    } else {
                        Log.info(log, "TrackableListDAO updateDatabase :" + trackable.getName());
                        tDAO.updateDatabase(trackable);
                    }
                } catch (Exception exc) {
                    Log.err(log, "TrackableListDAO writeToDatabase", exc);
                }
            } while (iterator.hasNext());
        }
        Log.info(log, "TrackableListDAO writeToDatabase done.");
    }

    /**
     * Deleate all TBs
     */
    public static void clearDB() {
        DraftsDatabase.Drafts.sql.delete("Trackable", "", null);
    }

}
