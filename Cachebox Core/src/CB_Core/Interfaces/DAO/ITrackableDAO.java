package CB_Core.Interfaces.DAO;

import java.util.Date;



/**
 * Interface für die Zugriffe auf die DatenBank 
 * @author Longri
 *
 */
public interface ITrackableDAO 
{
	
	public long Id = -1;
	public boolean Archived = false;
	public String GcCode = "";
	public long CacheId = -1;
	public String CurrentGoal = "";
	public String CurrentOwnerName = "";
	public Date DateCreated = new Date();
	public String Description = "";
	public String IconUrl = "";
	public String ImageUrl = "";
	public String Name = "";
	public String OwnerName = "";
	public String Url = "";
	
	public ITrackableDAO readFromDB(long id);
	
	public void writeToDB();
}
