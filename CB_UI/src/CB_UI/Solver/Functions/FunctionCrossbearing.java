package CB_UI.Solver.Functions;

import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Solver.DataTypes.DataType;
import CB_Utils.MathUtils.CalculationType;

public class FunctionCrossbearing extends Function
{

	private static final long serialVersionUID = 4233730654010706806L;

	public FunctionCrossbearing()
	{
		Names.add(new LacalNames("Crossbearing", "en"));
		Names.add(new LacalNames("Kreuzpeilung", "de"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncCrossbearing");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescCrossbearing");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 4)
		{
			return Translation.Get("solverErrParamCount", "4", "$solverFuncBearing");
		}
		Coordinate[] coord = new Coordinate[2];
		double[] angle = new double[2];
		for (int i = 0; i < 2; i++)
		{
			coord[i] = new Coordinate(parameter[i * 2]);
			if (!coord[i].isValid()) return Translation.Get("solverErrParamType", "$solverFuncCrossbearing", String.valueOf(i * 2 + 1),
					"$coordinate", "$coordinate", parameter[i * 2]);
			try
			{
				angle[i] = Double.valueOf(parameter[i * 2 + 1]);
			}
			catch (Exception ex)
			{
				return Translation.Get("solverErrParamType", "$solverFuncCrossbearing", String.valueOf(i * 2 + 2), "$angle", "$number",
						parameter[i * 2 + 1]);
			}
		}

		try
		{
			return Coordinate.Crossbearing(CalculationType.ACCURATE, coord[0], angle[0], coord[1], angle[1]).FormatCoordinate();
		}
		catch (Exception ex)
		{
			return Translation.Get("StdError", "$solverFuncCrossbearing", ex.getMessage(),
					coord[0].FormatCoordinate() + " -> " + coord[1].FormatCoordinate());
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
			return DataType.Float;
		case 2:
			return DataType.Coordinate;
		case 3:
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

}