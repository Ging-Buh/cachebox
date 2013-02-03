package CB_Core.Solver.Functions;

import CB_Core.TranslationEngine.Translation;
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
		return Translation.Get("solverFuncProjection");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescProjection");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 3)
		{
			String s = Translation.Get("solverErrParamCount", "3", "$solverFuncProjection");
			return s;
		}
		Coordinate coord = new Coordinate(parameter[0]);
		if (!coord.Valid)
		{
			return Translation.Get("solverErrParamType", "$solverFuncProjection", "1", "$coordinate", "$coordinate", parameter[0]);
		}
		double distance;
		double angle;
		try
		{
			distance = Double.valueOf(parameter[1]);
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncProjection", "2", "$distance", "$number", parameter[1]);
		}
		try
		{
			angle = Double.valueOf(parameter[2]);
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncProjection", "3", "$angle", "$number", parameter[2]);
		}

		Coordinate result = Coordinate.Project(coord.getLatitude(), coord.getLongitude(), angle, distance);
		if (!result.Valid) return Translation.Get("InvalidCoordinate", "$solverFuncProjection",
				"Lat: " + String.valueOf(coord.getLatitude()) + ", Lon: " + String.valueOf(coord.getLongitude()));
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