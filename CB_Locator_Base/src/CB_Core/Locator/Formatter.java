package CB_Core.Locator;

public class Formatter
{
	public static String FormatLatitudeDM(double latitude)
	{
		return FormatDM(latitude, "N", "S");
	}

	public static String FormatLongitudeDM(double longitude)
	{
		return FormatDM(longitude, "E", "W");
	}

	static String FormatDM(double coord, String positiveDirection, String negativeDirection)
	{
		int deg = (int) coord;
		double frac = coord - deg;
		double min = frac * 60;

		String result = Math.abs(deg) + "\u00B0 " + String.format("%.3f", Math.abs(min));

		if (coord < 0) result += negativeDirection;
		else
			result += positiveDirection;

		return result;
	}

	/**
	 * Returns a readable String from given speed value
	 * 
	 * @param kmh
	 *            sped value as float
	 * @param ImperialUnits
	 *            True for using Imperial Units
	 * @return
	 */
	public static String SpeedString(float kmh, boolean ImperialUnits)
	{
		if (ImperialUnits) return SpeedStringImperial(kmh);
		else
			return SpeedStringMetric(kmh);
	}

	private static String SpeedStringMetric(float kmh)
	{
		return String.format("%.2f km/h", kmh);
	}

	private static String SpeedStringImperial(float kmh)
	{
		return String.format("%.2f mph", kmh / 1.6093f);
	}

}
