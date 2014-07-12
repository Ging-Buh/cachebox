package CB_Core.Solver.Functions;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionPrimeIndex extends FunctionPrimeBase
{
	private static final long serialVersionUID = -3417894947271978934L;

	public FunctionPrimeIndex()
	{
		Names.add(new LacalNames("PrimeIndex", "en"));
		Names.add(new LacalNames("PrimIndex", "de"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncPrimeIndex");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescPrimeIndex");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncPrimeIndex");
		}
		int number = 0;
		try
		{
			number = Integer.valueOf(parameter[0].trim());
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncPrimeIndex", "1", "$value", "$number", parameter[0]);
		}
		if (!IsPrimeNumber(number)) return "0";
		int anz = 0;
		int akt = 0;
		while (number >= akt)
		{
			if (IsPrimeNumber(akt))
			{
				anz++;
			}
			akt++;
		}
		return String.valueOf(anz);
	}

	@Override
	public int getAnzParam()
	{
		return 1;
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
			return DataType.Integer;
		default:
			return DataType.None;
		}
	}

	@Override
	public DataType getReturnType()
	{
		return DataType.Integer;
	}

	@Override
	public String getParamName(int i)
	{
		switch (i)
		{
		case 0:
			return "solverParamInteger";
		default:
			return super.getParamName(i);
		}
	}
}
