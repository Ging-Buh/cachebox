package CB_Core.Types;

import java.util.ArrayList;
import CB_Core.DAO.TrackableListDAO;

public class TbList extends ArrayList<Trackable> {

	/**
	 * generated Versions Id
	 */
	private static final long serialVersionUID = 1185835304121883107L;

	public void writeToDB() {
		TrackableListDAO.WriteToDatabase(this);
	}
}
