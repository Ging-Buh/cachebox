package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;
import CB_Core.Types.Coordinate;

public class FunctionBearing extends Function
{

	public FunctionBearing()
	{
		Names.add(new LacalNames("Bearing", "en"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncBearing");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescBearing");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 2)
		{
			return GlobalCore.Translations.Get("solverErrParamCount", "2");
		}
		try
		{
			Coordinate[] coord = new Coordinate[2];
			for (int i = 0; i < 2; i++)
			{
				coord[i] = new Coordinate(parameter[i]);
				if (!coord[i].Valid) return "Parameter " + String.valueOf(i + 1) + " must be a Coordinate!";
			}
			double bearing = Coordinate.Bearing(coord[0], coord[1]);
			if (bearing < 0) bearing = bearing + 360;
			return String.valueOf(bearing);
		}
		catch (Exception ex)
		{
			return ex.getMessage();
		}
	}

	@Override
	public int getAnzParam()
	{
		return 2;
	}

	@Override
	public boolean needsTextArgument()
	{
		return false;
	}
}