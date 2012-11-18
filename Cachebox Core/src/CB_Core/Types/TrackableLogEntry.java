package CB_Core.Types;

import java.util.Date;

public class TrackableLogEntry
{
	private int Id;
	private long TrackableId;
	private long CacheId;
	private String GcCode;      
	private boolean LogIsEncoded; 
	private String LogText;
	private int LogTypeId;
	private String LoggedByName;
	private Date visitDate;

}
