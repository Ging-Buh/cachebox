package CB_UI.Solver.Functions;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Solver.DataTypes.DataType;

public class FunctionProjection extends Function
{

	private static final long serialVersionUID = -6013883020785631158L;

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
		Coordinate coord = new CoordinateGPS(parameter[0]);
		if (!coord.isValid())
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

		Coordinate result = CoordinateGPS.Project(coord.getLatitude(), coord.getLongitude(), angle, distance);
		if (!result.isValid()) return Translation.Get("InvalidCoordinate", "$solverFuncProjection",
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

	@Override
	public DataType getParamType(int i)
	{
		switch (i)
		{
		case 0:
			return DataType.Coordinate;
		case 1:
			return DataType.Float;
		case 2:
			return DataType.Float;
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
			return "solverParamCoordinate";
		case 1:
			return "solverParamDistance";
		case 2:
			return "solverParamAngle";
		default:
			return super.getParamName(i);
		}
	}
}