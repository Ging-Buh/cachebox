package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;
import CB_Core.Types.Coordinate;

public class FunctionProjection extends Function
{

	public FunctionProjection()
	{
		Names.add(new LacalNames("Projection", "en"));
		Names.add(new LacalNames("Projektion", "de"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncProjection");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescProjection");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 3)
		{
			String s = GlobalCore.Translations.Get("solverErrParamCount", "3", "$solverFuncProjection");
			return s;
		}
		Coordinate coord = new Coordinate(parameter[0]);
		if (!coord.Valid)
		{
			return GlobalCore.Translations.Get("solverErrParamType", "$solverFuncProjection", "1", "$coordinate", "$coordinate",
					parameter[0]);
		}
		double distance;
		double angle;
		try
		{
			distance = Double.valueOf(parameter[1]);
		}
		catch (Exception ex)
		{
			return GlobalCore.Translations.Get("solverErrParamType", "$solverFuncProjection", "2", "$distance", "$number", parameter[1]);
		}
		try
		{
			angle = Double.valueOf(parameter[2]);
		}
		catch (Exception ex)
		{
			return GlobalCore.Translations.Get("solverErrParamType", "$solverFuncProjection", "3", "$angle", "$number", parameter[2]);
		}

		Coordinate result = Coordinate.Project(coord.Latitude, coord.Longitude, angle, distance);
		if (!result.Valid) return GlobalCore.Translations.Get("InvalidCoordinate", "$solverFuncProjection",
				"Lat: " + String.valueOf(coord.Latitude) + ", Lon: " + String.valueOf(coord.Longitude));
		return result.FormatCoordinate();
	}

	@Override
	public int getAnzParam()
	{
		return 3;
	}

	@Override
	public boolean needsTextArgument()
	{
		return true;
	}

}