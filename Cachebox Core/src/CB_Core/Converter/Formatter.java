package CB_Core.Converter;

public class Formatter 
{
	public static String FormatLatitudeDM(double latitude) {
        return FormatDM(latitude, "N", "S");
	}
    
	public static String FormatLongitudeDM(double longitude) {
        return FormatDM(longitude, "E", "W");
	}
	
	static String FormatDM(double coord, String positiveDirection, String negativeDirection)
    {
        int deg = (int)coord;
        double frac = coord - deg;
        double min = frac * 60;

        String result = Math.abs(deg) + "\u00B0 " + String.format("%.3f", Math.abs(min));

        if (coord < 0)
            result += negativeDirection;
        else
            result += positiveDirection;

        return result;
    }


}
