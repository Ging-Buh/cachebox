package CB_Core.Util;

public class UnitFormatter
{

	private static boolean mUseImperialUnits = false;

	public static void setUseImperialUnits(boolean useImperialUnits)
	{
		mUseImperialUnits = useImperialUnits;
	}

	public static boolean getUseImperialUnits()
	{
		return mUseImperialUnits;
	}

	// public static boolean ImperialUnits = Config.settings.ImperialUnits.getValue();

	// / <summary>
	// / Erzeugt eine für den Menschen lesbare Form der Distanz
	// / </summary>
	// / <param name="distance"></param>
	// / <returns></returns>
	public static String DistanceString(float distance)
	{
		if (mUseImperialUnits) return DistanceStringImperial(distance);
		else
			return DistanceStringMetric(distance);
	}

	// / <summary>
	// / Erzeugt eine für den Menschen lesbare Form der Distanz
	// / </summary>
	// / <param name="distance"></param>
	// / <returns></returns>
	public static String DistanceStringMetric(float distance)
	{

		if (distance <= 500) return String.format("%.0f", distance) + " m";

		if (distance < 10000) return String.format("%.2f", distance / 1000) + " km";

		return String.format("%.0f", distance / 1000) + " km";
	}

	// / <summary>
	// / Erzeugt eine für den Menschen lesbare Form der Distanz
	// / </summary>
	// / <param name="distance"></param>
	// / <returns></returns>
	public static String DistanceStringImperial(float distance)
	{

		float yards = distance / 0.9144f;
		float miles = yards / 1760;

		if (yards < 1000) return String.format("%.0f", yards) + "yd";

		if (miles < 10) return String.format("%.2f", miles) + "mi";

		return String.format("%.1f", miles) + "mi";

	}

	public static String SpeedString(float kmh)
	{
		if (mUseImperialUnits) return SpeedStringImperial(kmh);
		else
			return SpeedStringMetric(kmh);
	}

	public static String SpeedStringMetric(float kmh)
	{
		return String.format("%.2f km/h", kmh);
	}

	public static String SpeedStringImperial(float kmh)
	{
		return String.format("%.2f mph", kmh / 1.6093f);
	}

	private static String FormatDM(double coord, String positiveDirection, String negativeDirection)
	{
		int deg = (int) coord;
		double frac = coord - deg;
		double min = frac * 60;

		String result = Math.abs(deg) + "\u00B0  " + String.format("%.3f", Math.abs(min));

		result += " ";

		if (coord < 0) result += negativeDirection;
		else
			result += positiveDirection;

		return result;
	}

	public static String FormatLatitudeDM(double latitude)
	{
		return FormatDM(latitude, "N", "S");
	}

	public static String FormatLongitudeDM(double longitude)
	{
		return FormatDM(longitude, "E", "W");
	}

	public static String Rot13(String message)
	{
		String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lookup = "nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM";

		String result = "";

		for (int i = 0; i < message.length(); i++)
		{
			String curChar = message.substring(i, i + 1);
			int idx = alphabet.indexOf(curChar);

			if (idx < 0) result += curChar;
			else
				result += lookup.substring(idx, idx + 1);
		}

		return result;

	}

}
