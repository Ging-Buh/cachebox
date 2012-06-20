package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionPrimenumber extends FunctionPrimeBase
{
	public FunctionPrimenumber()
	{
		Names.add(new LacalNames("Primenumber", "en"));
		Names.add(new LacalNames("Primzahl", "de"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncPrimenumber");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescPrimenumber");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount", "1");
		}
		String wert = parameter[0].trim();
		int number = 0;
		try
		{
			number = Integer.valueOf(wert);
		}
		catch (Exception ex)
		{
			return ex.getMessage();
		}
		int anz = 0;
		int akt = 0;
		do
		{
			akt++;
			if (IsPrimeNumber(akt)) anz++;
		}
		while (anz < number);
		return String.valueOf(akt);
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
