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
			return GlobalCore.Translations.Get("solverErrParamCount", "3");
		}
		Coordinate coord = new Coordinate(parameter[0]);
		if (!coord.Valid)
		{
			return "Parameter 1 (coord) must be a valid Coordinate!";
		}
		double distance;
		double angle;
		try
		{
			distance = Double.valueOf(parameter[1]);
		}
		catch (Exception ex)
		{
			return "Parameter 2 (distance) must be number!";
		}
		try
		{
			angle = Double.valueOf(parameter[2]);
		}
		catch (Exception ex)
		{
			return "Parameter 3 (angle) must be number!";
		}

		Coordinate result = Coordinate.Project(coord.Latitude, coord.Longitude, angle, distance);
		if (!result.Valid) return "Error: Projection";

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