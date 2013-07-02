package CB_Core.Solver.Functions;

import CB_Core.TranslationEngine.Translation;

// ************************************************************************
// ********************** Iterierte (einstellige) Quersumme (Iterated CrossTotal) **************************
// ************************************************************************
public class FunctionIQuersumme extends Function
{
	private static final long serialVersionUID = -1727934349667230259L;

	public FunctionIQuersumme()
	{
		Names.add(new LacalNames("ICrosstotal", "en"));
		Names.add(new LacalNames("IQuersumme", "de"));
		Names.add(new LacalNames("ICT", "en"));
		Names.add(new LacalNames("IQS", "de"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncICrosstotal");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescICrosstotal");
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
			return Translation.Get("solverErrParamCount", "1", "$solverFuncICrosstotal");
		}
		String wert = parameter[0].trim();
		while (wert.length() > 1)
		{
			wert = Qs(wert);
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
