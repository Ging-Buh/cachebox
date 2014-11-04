package CB_Utils;

/**
 * String Helper class
 * 
 * @author Longri
 */
public class StringH
{
	/**
	 * Returns true, if the given string empty or NULL
	 * 
	 * @param string
	 * @return
	 */
	public static boolean isEmpty(String string)
	{
		if (string == null) return true;
		if (string.length() == 0) return true;
		return false;
	}

	/**
	 * Get the Name of Class, Name of method and the linenumber of th Caller.
	 * 
	 * @return
	 */
	public static String getCallerName()
	{
		return getCallerName(1);
	}

	/**
	 * Get the Name of Class, Name of method and the linenumber of th Caller. For the given deep.
	 * 
	 * @param i
	 * @return
	 */
	public static String getCallerName(int i)
	{
		String ret = "NoInfo";

		try
		{
			StackTraceElement Caller = Thread.currentThread().getStackTrace()[3 + i];
			String Name = Caller.getClassName();
			String Methode = Caller.getMethodName();
			int Line = Caller.getLineNumber();
			ret = Name + "." + Methode + " [Line:" + Line + "]";
		}
		catch (Exception e)
		{

		}

		return ret;
	}
}
