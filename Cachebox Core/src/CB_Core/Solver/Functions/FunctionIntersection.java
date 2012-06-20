package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;
import CB_Core.Types.Coordinate;

public class FunctionIntersection extends Function
{

	public FunctionIntersection()
	{
		Names.add(new LacalNames("Intersection", "en"));
		Names.add(new LacalNames("Schnittpunkt", "de"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncIntersection");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescIntersection");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 4)
		{
			return GlobalCore.Translations.Get("solverErrParamCount", "4");
		}
		Coordinate[] coord = new Coordinate[4];
		for (int i = 0; i < 4; i++)
		{
			coord[i] = new Coordinate(parameter[i]);
			if (!coord[i].Valid) return "Parameter " + String.valueOf(i + 1) + " must be a valid Coordinate!";
		}
		try
		{
			return Coordinate.Intersection(coord[0], coord[1], coord[2], coord[3]).FormatCoordinate();
		}
		catch (Exception ex)
		{
			return ex.getMessage();
		}
	}

	@Override
	public int getAnzParam()
	{
		return 4;
	}

	@Override
	public boolean needsTextArgument()
	{
		return false;
	}
}