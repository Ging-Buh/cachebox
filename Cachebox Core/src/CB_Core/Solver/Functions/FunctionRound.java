package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionRound extends Function
{
	public FunctionRound()
	{
		Names.add(new LacalNames("Round", "en"));
		Names.add(new LacalNames("Runden", "de"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncRound");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescRound");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 2)
		{
			return GlobalCore.Translations.Get("solverErrParamCount", "2");
		}
		double number = 0;
		try
		{
			number = Double.valueOf(parameter[0].trim());
		}
		catch (Exception ex)
		{
			return "1. Parameter must be a number!";
		}
		int digits = 0;
		try
		{
			digits = Integer.valueOf(parameter[1].trim());
		}
		catch (Exception ex)
		{
			return "2. Parameter must be an integer!";
		}
		return String.format("%." + String.valueOf(digits) + "f", number);
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
