package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;
import CB_Core.Solver.Functions.Function.LacalNames;

public class FunctionQuerprodukt extends Function
{
	public FunctionQuerprodukt()
	{
		Names.add(new LacalNames("Crossproduct", "en"));
		Names.add(new LacalNames("Querprodukt", "de"));
		Names.add(new LacalNames("CP", "en"));
		Names.add(new LacalNames("QP", "de"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncCrossproduct");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescCrossprocuct");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
		}
		String wert = parameter[0].trim();
		int result = 1;
		for (char c : wert.toCharArray())
		{
			int i = (int) c - 48;
			if ((i >= 0) && (i <= 9)) result *= i;
		}
		return String.valueOf(result);
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
