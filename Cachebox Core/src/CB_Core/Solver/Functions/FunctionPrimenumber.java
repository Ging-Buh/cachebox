package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;
import CB_Core.Solver.Functions.Function.LacalNames;

public class FunctionPrimenumber extends Function
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

	private boolean IsPrimeNumber(long testNumber)
	{
		if (testNumber < 2) return false;
		if (testNumber == 2) return true;
		// 2 explizit testen, da die Schliefe an 3 startet
		if (testNumber % 2 == 0) return false;

		long upperBorder = (long) Math.round(Math.sqrt(testNumber));
		// Alle ungeraden Zahlen bis zur Wurzel pruefen
		for (long i = 3; i <= upperBorder; i = i + 2)
			if (testNumber % i == 0) return false;
		return true;
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
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
