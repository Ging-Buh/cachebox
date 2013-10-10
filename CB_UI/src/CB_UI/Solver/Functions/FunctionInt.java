package CB_UI.Solver.Functions;

import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionInt extends Function
{
	private static final long serialVersionUID = -4677935521343499858L;

	public FunctionInt()
	{
		Names.add(new LacalNames("Int", "en"));
		Names.add(new LacalNames("Ganzzahl", "de"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncInt");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescInt");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncInt");
		}
		double number = 0;
		try
		{
			number = Double.valueOf(parameter[0].trim());
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncInt", "1", "$value", "$number", parameter[0]);
		}
		return String.valueOf((int) number);
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
