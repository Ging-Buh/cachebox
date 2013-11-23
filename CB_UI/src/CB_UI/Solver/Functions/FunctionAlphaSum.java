package CB_UI.Solver.Functions;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Solver.DataTypes.DataType;

public class FunctionAlphaSum extends Function
{
	private static final long serialVersionUID = -6962880870313633795L;

	public FunctionAlphaSum()
	{
		Names.add(new LacalNames("AlphaSum", "en"));
		Names.add(new LacalNames("AS", "en"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncAlphaSum");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescAlphaSum");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncAlphaSum");
		}
		int result = 0;
		if (parameter[0].length() == 0) return "0";
		parameter[0] = parameter[0].toLowerCase();
		for (char c : parameter[0].toCharArray())
		{
			if ((c >= 'a') && (c <= 'z')) result += (int) c - (int) ('a') + 1;
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

	@Override
	public DataType getParamType(int i)
	{
		switch (i)
		{
		case 0:
			return DataType.String;
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
		return "solverParamText";
	}
}
