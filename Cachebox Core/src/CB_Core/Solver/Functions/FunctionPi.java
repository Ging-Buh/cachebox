package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionPi extends Function
{
	public FunctionPi()
	{
		Names.add(new LacalNames("Pi", "en"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncPi");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescPi");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if ((parameter.length != 1) || (parameter[0].trim() != "")) return GlobalCore.Translations.Get("solverErrParamCount", "0",
				"$solverFuncPi");
		return String.valueOf(Math.PI);
	}

	@Override
	public int getAnzParam()
	{
		return 0;
	}

	@Override
	public boolean needsTextArgument()
	{
		return false;
	}

}
