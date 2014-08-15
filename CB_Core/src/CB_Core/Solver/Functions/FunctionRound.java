package CB_Core.Solver.Functions;

import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Solver;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionRound extends Function
{
	private static final long serialVersionUID = 3669660135984610039L;

	public FunctionRound(Solver solver)
	{
		super(solver);
		Names.add(new LacalNames("Round", "en"));
		Names.add(new LacalNames("Runden", "de"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncRound");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescRound");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 2)
		{
			return Translation.Get("solverErrParamCount", "2", "$solverFuncRound");
		}
		double number = 0;
		try
		{
			number = Double.valueOf(parameter[0].trim());
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncRound", "1", "$value", "$number", parameter[0]);
		}
		int digits = 0;
		try
		{
			digits = Integer.valueOf(parameter[1].trim());
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncRound", "2", "$value", "$number", parameter[1]);
		}
		return String.format("%." + String.valueOf(digits) + "f", number);
	}

	@Override
	public int getAnzParam()
	{
		return 2;
	}

	@Override
	public boolean needsTextArgument()
	{
		return false;
	}

	@Override
	public String getParamName(int i)
	{
		switch (i)
		{
		case 0:
			return "solverParamNumber";
		case 1:
			return "solverParamDecimalPlaces";
		default:
			return super.getParamName(i);
		}
	}

	@Override
	public DataType getReturnType()
	{
		return DataType.Float;
	}

	@Override
	public DataType getParamType(int i)
	{
		switch (i)
		{
		case 0:
			return DataType.Float;
		default:
			return DataType.None;
		}
	}

}
