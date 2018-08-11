package CB_Utils;

// import org.jsoup.*;

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

    public static String JsoupParse(String s) {
        String ret = "";
        if (s != null)
            if (!("null".equals(s)))
                ret = s;
                // ret = Jsoup.parse(s).text(); // todo Loader problem
        return ret;
    }
}
