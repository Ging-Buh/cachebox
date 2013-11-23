package CB_UI.Solver.Functions;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Solver.DataTypes.DataType;
import CB_Utils.Util.UnitFormatter;

public class FunctionRot13 extends Function
{
	private static final long serialVersionUID = 172122667088261676L;

	public FunctionRot13()
	{
		Names.add(new LacalNames("Rot13", "en"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncRot13");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescRot13");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncRot13");
		}
		return UnitFormatter.Rot13(parameter[0]);
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
		return DataType.String;
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
