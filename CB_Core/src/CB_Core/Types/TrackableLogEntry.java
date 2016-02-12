package CB_Core.Types;

import java.util.Date;

public class TrackableLogEntry {
	protected int Id;
	protected long TrackableId;
	protected long CacheId;
	protected String GcCode;
	protected boolean LogIsEncoded;
	protected String LogText;
	protected int LogTypeId;
	protected String LoggedByName;
	protected Date visitDate;

}
