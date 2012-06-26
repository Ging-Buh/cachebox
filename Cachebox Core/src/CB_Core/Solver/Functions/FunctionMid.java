package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionMid extends Function
{

	public FunctionMid()
	{
		Names.add(new LacalNames("Mid", "en"));
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncMid");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescMid");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if ((parameter.length < 2) || (parameter.length > 3))
		{
			return GlobalCore.Translations.Get("solverErrParamCount", "2-3", "$solverFuncMid");
		}
		String Wert = parameter[0].trim();
		int iPos, iCount;
		try
		{
			iPos = Integer.valueOf(parameter[1].trim());
		}
		catch (Exception ex)
		{
			return GlobalCore.Translations.Get("solverErrParamType", "$solverFuncMid", "2", "$Position", "$number", parameter[1]);
		}
		try
		{
			if (parameter.length == 2) iCount = 1;
			else
				iCount = Integer.valueOf(parameter[2].trim());
		}
		catch (Exception ex)
		{
			return GlobalCore.Translations.Get("solverErrParamType", "$solverFuncMid", "5", "$count", "$number", parameter[2]);
		}
		if (iPos > Wert.length())
		{
			return GlobalCore.Translations.Get("PosGtLength", "$solverFuncMid", String.valueOf(iPos), Wert);
		}
		return Wert.substring(iPos - 1, iPos - 1 + iCount);
	}

	@Override
	public int getAnzParam()
	{
		return 2;
	}

	@Override
	public boolean needsTextArgument()
	{
		return true;
	}

}
