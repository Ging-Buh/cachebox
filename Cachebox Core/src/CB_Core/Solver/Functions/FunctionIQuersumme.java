package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

// ************************************************************************
// ********************** Iterierte (einstellige) Quersumme (Iterated CrossTotal) **************************
// ************************************************************************
public class FunctionIQuersumme extends Function
{
	public FunctionIQuersumme()
	{
		Names.add("ICrosstotal");
		Names.add("IQuersumme");
		Names.add("ICT");
		Names.add("IQS");
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncICrosstotal");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescICrosstotal");
	}

	private String Qs(String wert)
	{
		int result = 0;
		for (char c : wert.toCharArray())
		{
			int i = (int) c - 48;
			if ((i >= 0) && (i <= 9)) result += i;
		}
		return String.valueOf(result);
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
		}
		String wert = parameter[0].trim();
		while (wert.length() > 1)
		{
			wert = Qs(wert);
		}
		return wert;
	}

}
