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
			return GlobalCore.Translations.Get("solverErrParamCount", "2-3");
		}
		String Wert = parameter[0].trim();
		int iPos, iCount;
		try
		{
			iPos = Integer.valueOf(parameter[1].trim());
		}
		catch (Exception ex)
		{
			return "Parameter 2 (Position) must be number!";
		}
		try
		{
			if (parameter.length == 2) iCount = 1;
			else
				iCount = Integer.valueOf(parameter[2].trim());
		}
		catch (Exception ex)
		{
			return "Parameter 3 (Count) must be number!";
		}
		if (iPos > Wert.length())
		{
			return "Position must be less than length of string";
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
