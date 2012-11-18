package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionAlphaSum extends Function
{
	public FunctionAlphaSum()
	{
		Names.add(new LacalNames("AlphaSum", "en"));
		Names.add(new LacalNames("AS", "en"));
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
			return GlobalCore.Translations.Get("solverErrParamCount", "1", "$solverFuncAlphaSum");
		}
		int result = 0;
		if (parameter[0].length() == 0) return "0";
		parameter[0] = parameter[0].toLowerCase();
		for (char c : parameter[0].toCharArray())
		{
			if ((c >= 'a') && (c <= 'z')) result += (int) c - (int) ('a') + 1;
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
		return true;
	}

}
