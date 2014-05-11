package CB_Core.Solver.Functions;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

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
		return Translation.Get("solverFuncICrosstotal".hashCode());
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescICrosstotal".hashCode());
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
			return Translation.Get("solverErrParamCount".hashCode(), "1", "$solverFuncICrosstotal");
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

	@Override
	public DataType getParamType(int i)
	{
		switch (i)
		{
		case 0:
			return DataType.Integer;
		default:
			return DataType.None;
		}
	}

	@Override
	public DataType getReturnType()
	{
		return DataType.Integer;
	}

	@Override
	public String getParamName(int i)
	{
		switch (i)
		{
		case 0:
			return "solverParamInteger";
		default:
			return super.getParamName(i);
		}
	}
}
