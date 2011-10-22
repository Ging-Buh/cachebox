package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionLength extends Function
{
	public FunctionLength()
	{
		Names.add("Length");
		Names.add("Länge");
		Names.add("Len");
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncLength");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescLength");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
		}
		return String.valueOf(parameter[0].length());
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
