package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionPrimeIndex extends FunctionPrimeBase
{
	public FunctionPrimeIndex()
	{
		Names.add(new LacalNames("PrimeIndex", "en"));
		Names.add(new LacalNames("PrimIndex", "de"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncPrimeIndex");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescPrimeIndex");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount", "1");
		}
		int number = 0;
		try
		{
			number = Integer.valueOf(parameter[0].trim());
		}
		catch (Exception ex)
		{
			return ex.getMessage();
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

}
