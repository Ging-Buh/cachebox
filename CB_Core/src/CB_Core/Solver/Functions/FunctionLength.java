package CB_Core.Solver.Functions;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionLength extends Function
{
	private static final long serialVersionUID = -7915834072364391848L;

	public FunctionLength()
	{
		Names.add(new LacalNames("Length", "en"));
		Names.add(new LacalNames("Länge", "de"));
		Names.add(new LacalNames("Len", "en"));
		Names.add(new LacalNames("Len", "de"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncLength");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescLength");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncLength");
		}
		return String.valueOf(parameter[0].length());
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
		switch (i)
		{
		case 0:
			return "solverParamText";
		default:
			return super.getParamName(i);
		}
	}
}
