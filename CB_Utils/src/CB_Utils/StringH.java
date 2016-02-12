package CB_Utils;

/**
 * String Helper class
 * 
 * @author Longri
 */
public class StringH {
	/**
	 * Returns true, if the given string empty or NULL
	 * 
	 * @param string
	 * @return
	 */
	public static boolean isEmpty(String string) {
		if (string == null)
			return true;
		if (string.length() == 0)
			return true;
		return false;
	}
}
