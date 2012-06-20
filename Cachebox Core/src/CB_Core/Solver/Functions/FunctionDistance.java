package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;
import CB_Core.Types.Coordinate;

public class FunctionDistance extends Function
{

	public FunctionDistance()
	{
		Names.add(new LacalNames("Distance", "en"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncDistance");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescDistance");
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
			float[] dist = new float[2];
			Coordinate.distanceBetween(coord[0].Latitude, coord[0].Longitude, coord[1].Latitude, coord[1].Longitude, dist);
			return String.valueOf(dist[0]);
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