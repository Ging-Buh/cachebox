package CB_Core.Solver.Functions;

import CB_Core.TranslationEngine.Translation;

public class FunctionHandyCode extends Function
{
	public FunctionHandyCode()
	{
		Names.add(new LacalNames("PhoneCode", "en"));
		Names.add(new LacalNames("HandyCode", "de"));
		Names.add(new LacalNames("PC", "en"));
		Names.add(new LacalNames("HC", "de"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncPhoneCode");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescPhoneCode");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncPhoneCode");
		}
		String wert = parameter[0].trim().toLowerCase();
		if (wert.length() == 0) return "0";
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
