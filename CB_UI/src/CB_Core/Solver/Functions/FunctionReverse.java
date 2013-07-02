package CB_Core.Solver.Functions;

import CB_Core.TranslationEngine.Translation;

public class FunctionReverse extends Function
{
	private static final long serialVersionUID = 9169402073615894654L;

	public FunctionReverse()
	{
		Names.add(new LacalNames("Reverse", "en"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncReverse");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescReverse");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncReverse");
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
