package CB_Core.Solver.Functions;

import CB_Core.TranslationEngine.Translation;
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
		return Translation.Get("solverFuncIntersection");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescIntersection");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 4)
		{
			return Translation.Get("solverErrParamCount", "4", "$solverFuncIntersection");
		}
		Coordinate[] coord = new Coordinate[4];
		for (int i = 0; i < 4; i++)
		{
			coord[i] = new Coordinate(parameter[i]);
			if (!coord[i].Valid) return Translation.Get("solverErrParamType", "$solverFuncIntersection", String.valueOf(i + 1),
					"$coordinate", "$coordinate", parameter[i]);
		}
		try
		{
			return Coordinate.Intersection(coord[0], coord[1], coord[2], coord[3]).FormatCoordinate();
		}
		catch (Exception ex)
		{
			String s = coord[0].FormatCoordinate() + " / " + coord[1].FormatCoordinate() + " -> " + coord[2].FormatCoordinate() + " / "
					+ coord[3].FormatCoordinate();
			return Translation.Get("StdError", "$solverFuncIntersection", ex.getMessage(), s);
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