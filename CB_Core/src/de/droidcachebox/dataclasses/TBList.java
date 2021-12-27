package de.droidcachebox.dataclasses;

import java.util.ArrayList;

import de.droidcachebox.database.TrackableListDAO;

public class TBList extends ArrayList<Trackable> {

    /**
     * generated Versions Id
     */
    private static final long serialVersionUID = 1185835304121883107L;

    public void writeToDB() {
        TrackableListDAO.WriteToDatabase(this);
    }
}
