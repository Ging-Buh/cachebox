package CB_Core.Types;

import CB_Core.DAO.TrackableListDAO;

import java.util.ArrayList;

public class TBList extends ArrayList<Trackable> {

    /**
     * generated Versions Id
     */
    private static final long serialVersionUID = 1185835304121883107L;

    public void writeToDB() {
        TrackableListDAO.WriteToDatabase(this);
    }
}
