package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionAlphaSum extends Function
{
	public FunctionAlphaSum()
	{
		Names.add("AlphaSum");
		Names.add("AS");
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncAlphaSum");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescAlphaSum");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
		}
		int result = 0;
		if (parameter[0].length() == 0) return "0";
		parameter[0] = parameter[0].toLowerCase();
		for (char c : parameter[0].toCharArray())
		{
			result += (int) c - (int) ('a') + 1;
		}
		return String.valueOf(result);
	}
}
