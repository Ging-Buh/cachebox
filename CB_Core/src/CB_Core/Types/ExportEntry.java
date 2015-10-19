package CB_Core.Types;

import java.io.Serializable;

import CB_Core.Replication.ChangeType;
import de.cb.sqlite.CoreCursor;

public class ExportEntry implements Serializable
{
	private static final long serialVersionUID = 1571599258092289354L;

	public long id;
	public ChangeType changeType;
	public long cacheId;
	public String wpGcCode;
	public int solverCheckSum;
	public int notesCheckSum;
	public int wpCoordCheckSum;
	public transient String cacheName; // nicht serialisieren
	public Waypoint waypoint;
	public String solver;
	public String note;
	public boolean toExport;

	ExportEntry(CoreCursor reader)
	{
		id = reader.getLong(0);
		changeType = ChangeType.values()[reader.getInt(1)];
		cacheId = reader.getLong(2);
		wpGcCode = reader.getString(3);
		solverCheckSum = reader.getInt(4);
		notesCheckSum = reader.getInt(5);
		wpCoordCheckSum = reader.getInt(6);
		cacheName = reader.getString(7);
		toExport = true;
	}

	public void setExport(boolean isChecked)
	{
		toExport = isChecked;
	}

}
