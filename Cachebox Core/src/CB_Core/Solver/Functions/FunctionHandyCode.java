package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionHandyCode extends Function
{
	public FunctionHandyCode()
	{
		Names.add("PhoneCode");
		Names.add("HandyCode");
		Names.add("PC");
		Names.add("HC");
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncPhoneCode");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescPhoneCode");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
		}
		String wert = parameter[0].trim().toLowerCase();
		if (wert == "") return "0";
		char c = wert.charAt(0);
		int i = (int) c - (int) ('a') + 1;
		i -= 3;
		if (i <= 0) return "2";
		i -= 3;
		if (i <= 0) return "3";
		i -= 3;
		if (i <= 0) return "4";
		i -= 3;
		if (i <= 0) return "5";
		i -= 3;
		if (i <= 0) return "6";
		i -= 4;
		if (i <= 0) return "7";
		i -= 3;
		if (i <= 0) return "8";
		i -= 4;
		if (i <= 0) return "9";
		return "0";
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
