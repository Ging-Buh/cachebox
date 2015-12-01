package CB_Core.Solver.Functions;

import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Solver;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;

public class FunctionDistance extends Function
{

	private static final long serialVersionUID = -7861925988066369903L;

	public FunctionDistance(Solver solver)
	{
		super(solver);
		Names.add(new LocalNames("Distance", "en"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncDistance".hashCode());
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescDistance".hashCode());
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 2)
		{
			return Translation.Get("solverErrParamCount".hashCode(), "2", "$solverFuncDistance");
		}
		Coordinate[] coord = new Coordinate[2];
		for (int i = 0; i < 2; i++)
		{
			coord[i] = new CoordinateGPS(parameter[i]);
			if (!coord[i].isValid()) return Translation.Get("solverErrParamType".hashCode(), "$solverFuncDistance", String.valueOf(i + 1),
					"$coordinate", "$coordinate", parameter[i]);
		}
		float[] dist = new float[2];
		try
		{
			MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, coord[0].getLatitude(), coord[0].getLongitude(),
					coord[1].getLatitude(), coord[1].getLongitude(), dist);
			return String.valueOf(dist[0]);
		}
		catch (Exception ex)
		{
			return Translation.Get("StdError".hashCode(), "$solverFuncDistance", ex.getMessage(), coord[0].FormatCoordinate() + " -> "
					+ coord[1].FormatCoordinate());
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

	@Override
	public String getParamName(int i)
	{
		switch (i)
		{
		case 0:
		case 1:
			return "solverParamCoordinate";
		default:
			return super.getParamName(i);
		}
	}
}