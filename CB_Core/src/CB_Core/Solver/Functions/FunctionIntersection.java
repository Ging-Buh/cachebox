package CB_Core.Solver.Functions;

import CB_Core.Solver.DataTypes.DataType;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionIntersection extends Function
{

	private static final long serialVersionUID = 8472007835430135995L;

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
			coord[i] = new CoordinateGPS(parameter[i]);
			if (!coord[i].isValid()) return Translation.Get("solverErrParamType", "$solverFuncIntersection", String.valueOf(i + 1),
					"$coordinate", "$coordinate", parameter[i]);
		}
		try
		{
			return CoordinateGPS.Intersection(coord[0], coord[1], coord[2], coord[3]).FormatCoordinate();
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

	@Override
	public DataType getParamType(int i)
	{
		switch (i)
		{
		case 0:
			return DataType.Coordinate;
		case 1:
			return DataType.Coordinate;
		case 2:
			return DataType.Coordinate;
		case 3:
			return DataType.Coordinate;
		default:
			return DataType.None;
		}
	}

	@Override
	public DataType getReturnType()
	{
		return DataType.Coordinate;
	}

	@Override
	public String getParamName(int i)
	{
		switch (i)
		{
		case 0:
		case 1:
		case 2:
		case 3:
			return "solverParamCoordinate";
		default:
			return super.getParamName(i);
		}
	}

}