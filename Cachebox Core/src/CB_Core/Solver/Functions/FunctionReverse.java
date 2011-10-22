package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;
import CB_Core.Solver.Functions.Function.LacalNames;

public class FunctionReverse extends Function
{
	public FunctionReverse()
	{
		Names.add(new LacalNames("Reverse", "en"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncReverse");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescReverse");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
		}
		String result = "";
		for (char c : parameter[0].toCharArray())
			result = c + result;
		return result;
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
