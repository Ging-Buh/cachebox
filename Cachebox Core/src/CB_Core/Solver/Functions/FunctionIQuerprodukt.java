package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionIQuerprodukt extends Function
{
	public FunctionIQuerprodukt()
	{
		Names.add(new LacalNames("ICrossproduct", "en"));
		Names.add(new LacalNames("IQuerprodukt", "de"));
		Names.add(new LacalNames("ICP", "en"));
		Names.add(new LacalNames("IQP", "de"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncICrossproduct");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescICrossproduct");
	}

	private String Qp(String wert)
	{
		int result = 1;
		for (char c : wert.toCharArray())
		{
			int i = (int) c - 48;
			if ((i >= 0) && (i <= 9)) result *= i;
		}
		return String.valueOf(result);
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount", "1");
		}
		String wert = parameter[0].trim();
		while (wert.length() > 1)
		{
			wert = Qp(wert);
		}
		return wert;
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
