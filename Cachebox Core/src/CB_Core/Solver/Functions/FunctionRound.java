package CB_Core.Solver.Functions;

import CB_Core.TranslationEngine.Translation;

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
		return Translation.Get("solverFuncRound");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescRound");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 2)
		{
			return Translation.Get("solverErrParamCount", "2", "$solverFuncRound");
		}
		double number = 0;
		try
		{
			number = Double.valueOf(parameter[0].trim());
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncRound", "1", "$value", "$number", parameter[0]);
		}
		int digits = 0;
		try
		{
			digits = Integer.valueOf(parameter[1].trim());
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncRound", "2", "$value", "$number", parameter[1]);
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
