package nonGuiClasses.fromOpenGpx;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LocalDateTime 
{
	
	private static DateFormat mdfUSDate = new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss");
	private static DateFormat mdfEuroDate = new SimpleDateFormat("dd.MM.yyyy'T'HH:mm:ss");

	
	/**
	 * 
	 * @param dateString
	 * @return
	 */
	public static Date parseString(String dateString)
	{
		final DateFormat standardFormat = DateFormat.getDateInstance();
		Date dateTime = getDate(dateString, standardFormat);
		if (dateTime == null) dateTime = getDate(dateString, mdfEuroDate);
		if (dateTime == null) dateTime = getDate(dateString, mdfUSDate);
		return dateTime;
	}	

	/**
	 * 
	 * @param dateString
	 * @param format
	 * @return
	 */
	private static Date getDate(String dateString, DateFormat format)
	{
		Date dateTime = null;
		try 
		{
			dateTime = format.parse(dateString);
		} 
		catch (ParseException ex)
		{
			
			// ex.printStackTrace();
		}
		return dateTime;
		
	}
}
