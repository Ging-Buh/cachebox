package CB_UI.Solver.Functions;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Solver.DataTypes.DataType;

public class FunctionPrimenumber extends FunctionPrimeBase
{
	private static final long serialVersionUID = 9017206001889511182L;

	public FunctionPrimenumber()
	{
		Names.add(new LacalNames("Primenumber", "en"));
		Names.add(new LacalNames("Primzahl", "de"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncPrimenumber");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescPrimenumber");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncPrimenumber");
		}
		String wert = parameter[0].trim();
		int number = 0;
		try
		{
			number = Integer.valueOf(wert);
		}
		catch (Exception ex)
		{
			return Translation.Get("solverErrParamType", "$solverFuncPrimenumber", "1", "$value", "$number", parameter[0]);
		}
		int anz = 0;
		int akt = 0;
		do
		{
			akt++;
			if (IsPrimeNumber(akt)) anz++;
		}
		while (anz < number);
		return String.valueOf(akt);
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
