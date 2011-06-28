package nonGuiClasses.fromOpenGpx;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * 
 * @author Martin Preishuber (opengpx)
 *
 */
public class ISODateTime 
{
	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	
	
	/**
	 * 
	 * @param isoDateString
	 * @return
	 */
	public static Date parseString(String isoDateString)
	{
		Date dateTime = null;
		try 
		{
			final DateFormat iSO8601Local = new SimpleDateFormat (DATE_FORMAT_PATTERN);
			dateTime = iSO8601Local.parse(isoDateString);
		} 
		catch (ParseException ex)
		{
				
			ex.printStackTrace();
		}
		return dateTime;
	}	
}
