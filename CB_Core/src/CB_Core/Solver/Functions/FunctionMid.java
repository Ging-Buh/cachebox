package CB_Core.Solver.Functions;

import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Solver;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionMid extends Function
{
	private static final long serialVersionUID = 3727854231542597267L;

	public FunctionMid(Solver solver)
	{
		super(solver);
		Names.add(new LocalNames("Mid", "en"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncMid");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescMid");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if ((parameter.length < 2) || (parameter.length > 3))
		{
			return Translation.Get("solverErrParamCount", "2-3", "$solverFuncMid");
		}
		String Wert = parameter[0].trim();
		int iPos, iCount;
		try
		{
			iPos = Integer.valueOf(parameter[1].trim());
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncMid", "2", "$Position", "$number", parameter[1]);
		}
		try
		{
			if (parameter.length == 2) iCount = 1;
			else
				iCount = Integer.valueOf(parameter[2].trim());
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncMid", "5", "$count", "$number", parameter[2]);
		}
		if (iPos > Wert.length())
		{
			return Translation.Get("PosGtLength", "$solverFuncMid", String.valueOf(iPos), Wert);
		}
		return Wert.substring(iPos - 1, iPos - 1 + iCount);
	}

	@Override
	public int getAnzParam()
	{
		return 3;
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
		case 1:
		case 2:
			return DataType.Integer;
		default:
			return DataType.None;
		}
	}

	@Override
	public DataType getReturnType()
	{
		return DataType.String;
	}

	@Override
	public String getParamName(int i)
	{
		switch (i)
		{
		case 0:
			return "solverParamText";
		case 1:
		case 2:
			return "solverParamInteger";
		default:
			return super.getParamName(i);
		}
	}
}
