package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;
import CB_Core.Types.Coordinate;

public class FunctionCrossbearing extends Function
{

	public FunctionCrossbearing()
	{
		Names.add(new LacalNames("Crossbearing", "en"));
		Names.add(new LacalNames("Kreuzpeilung", "de"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncCrossbearing");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescCrossbearing");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 4)
		{
			return GlobalCore.Translations.Get("solverErrParamCount", "4");
		}
		try
		{
			Coordinate[] coord = new Coordinate[2];
			double[] angle = new double[2];
			for (int i = 0; i < 2; i++)
			{
				coord[i] = new Coordinate(parameter[i * 2]);
				if (!coord[i].Valid) return "Parameter " + String.valueOf(i * 2 + 1) + " must be a Coordinate!";
				try
				{
					angle[i] = Double.valueOf(parameter[i * 2 + 1]);
				}
				catch (Exception ex)
				{
					return "Parameter " + String.valueOf(i * 2 + 2) + " must be a number!";
				}
			}

			return Coordinate.Crossbearing(coord[0], angle[0], coord[1], angle[1]).FormatCoordinate();
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