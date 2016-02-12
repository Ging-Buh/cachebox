package CB_Core.Types;

import java.io.Serializable;

public class WaypointDetail implements Serializable {
	private static final long serialVersionUID = -3177862382324983452L;

	// / Lösung einer QTA
	private byte[] Clue;
	// / Kommentartext
	private byte[] Description;

	public int checkSum = 0; // for replication

	public WaypointDetail() {

	}

	public String getDescription() {
		if (Description == null)
			return Waypoint.EMPTY_STRING;
		return new String(Description, Waypoint.UTF_8);
	}

	public void setDescription(String description) {
		if (description == null) {
			Description = null;
			return;
		}
		Description = description.getBytes(Waypoint.UTF_8);
	}

	public String getClue() {
		if (Clue == null)
			return Waypoint.EMPTY_STRING;
		return new String(Clue, Waypoint.UTF_8);
	}

	public void setClue(String clue) {
		if (clue == null) {
			Clue = null;
			return;
		}
		Clue = clue.getBytes(Waypoint.UTF_8);
	}

	public void setCheckSum(int i) {
		checkSum = i;
	}

	public void dispose() {

	}

}
