package CB_UI.Solver.Functions;

import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Solver.DataTypes.DataType;
import CB_Utils.MathUtils.CalculationType;

public class FunctionBearing extends Function
{

	private static final long serialVersionUID = -85879423478038052L;

	public FunctionBearing()
	{
		Names.add(new LacalNames("Bearing", "en"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncBearing");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescBearing");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 2)
		{
			return Translation.Get("solverErrParamCount", "2", "$solverFuncBearing");
		}
		Coordinate[] coord = new Coordinate[2];
		for (int i = 0; i < 2; i++)
		{
			coord[i] = new Coordinate(parameter[i]);
			if (!coord[i].isValid()) return Translation.Get("solverErrParamType", "$solverFuncBearing", String.valueOf(i + 1),
					"$coordinate", "$coordinate", parameter[i]);
		}
		try
		{
			double bearing = Coordinate.Bearing(CalculationType.ACCURATE, coord[0], coord[1]);
			if (bearing < 0) bearing = bearing + 360;
			return String.valueOf(bearing);
		}
		catch (Exception ex)
		{
			return Translation.Get("StdError", "$solverFuncBearing", ex.getMessage(),
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

	@Override
	public DataType getParamType(int i)
	{
		switch (i)
		{
		case 0:
			return DataType.Coordinate;
		case 1:
			return DataType.Coordinate;
		default:
			return DataType.None;
		}
	}

	@Override
	public DataType getReturnType()
	{
		return DataType.Float;
	}

}