package CB_Core.Solver.Functions;

import CB_Core.TranslationEngine.Translation;
import CB_Locator.Coordinate;

public class FunctionDistance extends Function
{

	private static final long serialVersionUID = -7861925988066369903L;

	public FunctionDistance()
	{
		Names.add(new LacalNames("Distance", "en"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncDistance");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescDistance");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 2)
		{
			return Translation.Get("solverErrParamCount", "2", "$solverFuncDistance");
		}
		Coordinate[] coord = new Coordinate[2];
		for (int i = 0; i < 2; i++)
		{
			coord[i] = new Coordinate(parameter[i]);
			if (!coord[i].isValid()) return Translation.Get("solverErrParamType", "$solverFuncDistance", String.valueOf(i + 1),
					"$coordinate", "$coordinate", parameter[i]);
		}
		float[] dist = new float[2];
		try
		{
			Coordinate.distanceBetween(coord[0].getLatitude(), coord[0].getLongitude(), coord[1].getLatitude(), coord[1].getLongitude(),
					dist);
			return String.valueOf(dist[0]);
		}
		catch (Exception ex)
		{
			return Translation.Get("StdError", "$solverFuncDistance", ex.getMessage(),
					coord[0].FormatCoordinate() + " -> " + coord[1].FormatCoordinate());
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