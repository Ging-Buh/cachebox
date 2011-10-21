package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionQuersumme extends Function
{
	public FunctionQuersumme()
	{
		Names.add("Crosstotal");
		Names.add("Quersumme");
		Names.add("CT");
		Names.add("QS");
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncCrosstotal");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescCrosstotal");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
		}
		String wert = parameter[0].trim();
		int result = 0;
		for (char c : wert.toCharArray())
		{
			int i = (int) c - 48;
			if ((i >= 0) && (i <= 9)) result += i;
		}
		return String.valueOf(result);
	}

}
