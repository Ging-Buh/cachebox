package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionLength extends Function
{
	public FunctionLength()
	{
		Names.add(new LacalNames("Length", "en"));
		Names.add(new LacalNames("Länge", "de"));
		Names.add(new LacalNames("Len", "en"));
		Names.add(new LacalNames("Len", "de"));
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
			return GlobalCore.Translations.Get("solverErrParamCount", "1");
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
