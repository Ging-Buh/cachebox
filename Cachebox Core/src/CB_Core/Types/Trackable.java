package CB_Core.Types;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import CB_Core.Interfaces.DAO.ITrackableDAO;
import CB_Core.Log.Logger;

public class Trackable implements Comparable<Trackable>
{

	private long Id = -1;
	private boolean Archived;
	private String GcCode = "";
	private long CacheId;
	private String CurrentGoal = "";
	private String CurrentOwnerName = "";
	private Date DateCreated;
	private String Description;
	private String IconUrl = "";
	private String ImageUrl = "";
	private String Name = "";
	private String OwnerName = "";
	private String Url = "";

	/**
	 * DAO Constructor <br>
	 * Der Constructor, der ein Trackable über eine DB Abfrage erstellt!
	 * 
	 * @param dao
	 */
	public Trackable(ITrackableDAO dao)
	{
		Id = dao.Id;
		Archived = dao.Archived;
		GcCode = dao.GcCode;
		CacheId = dao.CacheId;
		CurrentGoal = dao.CurrentGoal;
		CurrentOwnerName = dao.CurrentOwnerName;
		DateCreated = dao.DateCreated;
		Description = dao.Description;
		IconUrl = dao.IconUrl;
		ImageUrl = dao.ImageUrl;
		Name = dao.Name;
		OwnerName = dao.OwnerName;
		Url = dao.Url;
	}

	public Trackable(JSONObject JObj)
	{

		try
		{
			Archived = JObj.getBoolean("Archived");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			GcCode = JObj.getString("Code");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			CacheId = JObj.getInt("CacheID");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			CurrentGoal = JObj.getString("CurrentGoal");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject jOwner;
		try
		{
			jOwner = JObj.getJSONObject("CurrentOwner");
			CurrentOwnerName = jOwner.getString("UserName");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			String dateCreated = JObj.getString("DateCreated");
			int date1 = dateCreated.indexOf("/Date(");
			int date2 = dateCreated.indexOf("-");
			String date = (String) dateCreated.subSequence(date1 + 6, date2);
			DateCreated = new Date(Long.valueOf(date));
		}
		catch (Exception exc)
		{
			Logger.Error("Constructor Trackable", "", exc);
		}
		try
		{
			Description = JObj.getString("Description");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			IconUrl = JObj.getString("IconUrl");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ImageUrl = JObj.getString("CachesLeft");
		try
		{
			Name = JObj.getString("Name");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			jOwner = JObj.getJSONObject("OriginalOwner");
			OwnerName = jOwner.getString("UserName");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			Url = JObj.getString("Url");
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * Getter
	 */

	public long getId()
	{
		return Id;
	}

	public boolean getArchived()
	{
		return Archived;
	}

	public String getGcCode()
	{
		return GcCode;
	}

	public long CacheId()
	{
		return CacheId;
	}

	public String getCurrentGoal()
	{
		return CurrentGoal;
	}

	public String getCurrentOwner()
	{
		return CurrentOwnerName;
	}

	public Date getDateCreated()
	{
		return DateCreated;
	}

	public String getDescription()
	{
		return Description;
	}

	public String getName()
	{
		return Name;
	}

	public String getUrl()
	{
		return Url;
	}

	/*
	 * Setter
	 */

	/*
	 * Methods
	 */

	/**
	 * Generiert eine Eindeutige ID aus den ASCII values des GcCodes. <br>
	 * Damit lässt sich dieser TB schneller in der DB finden.
	 * 
	 * @return long
	 */
	public static long GenerateTBId(String GcCode)
	{
		long result = 0;
		char[] dummy = GcCode.toCharArray();
		byte[] byteDummy = new byte[8];
		for (int i = 0; i < 8; i++)
		{
			if (i < GcCode.length()) byteDummy[i] = (byte) dummy[i];
			else
				byteDummy[i] = 0;
		}
		for (int i = 7; i >= 0; i--)
		{
			result *= 256;
			result += byteDummy[i];
		}
		return result;
	}

	/*
	 * Overrides
	 */

	@Override
	public int compareTo(Trackable T2)
	{
		return Name.compareToIgnoreCase(T2.Name);
	}

}
