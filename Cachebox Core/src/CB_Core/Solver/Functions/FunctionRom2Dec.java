package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionRom2Dec extends Function
{
	public FunctionRom2Dec()
	{
		Names.add("Rom2Dec");
	}

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncRom2Dec");
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescRom2Dec");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
		}
		String wert = parameter[0].trim();
		String ziffern = "IVXLCDM";
		int[] werte = new int[]
			{ 1, 5, 10, 50, 100, 500, 1000 };
		int result = 0;
		int i, idx0 = 0, idx1 = 0;

		wert = wert.toUpperCase();
		try
		{
			if (wert.length() > 1)
			{
				for (i = 0; i < wert.length() - 1; i++)
				{
					idx0 = ziffern.indexOf(wert.charAt(i + 0), 0);
					idx1 = ziffern.indexOf(wert.charAt(i + 1), 0);

					if (idx0 < idx1)
					{
						result -= werte[idx0];
					}
					else
					{
						result += werte[idx0];
					}
				}
				result += werte[idx1];
			}
			else
			{
				result = werte[ziffern.indexOf(wert.charAt(0), 0)];
			}
		}
		catch (Exception ex)
		{
			return "Error";
		}
		return String.valueOf(result);
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
