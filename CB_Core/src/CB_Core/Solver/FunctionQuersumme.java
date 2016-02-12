package CB_Core.Solver;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionQuersumme extends Function {
	private static final long serialVersionUID = 3128106685827884337L;

	public FunctionQuersumme(Solver solver) {
		super(solver);
		Names.add(new LocalNames("Crosstotal", "en"));
		Names.add(new LocalNames("Quersumme", "de"));
		Names.add(new LocalNames("CT", "en"));
		Names.add(new LocalNames("QS", "de"));
	}

	@Override
	public String getName() {
		return Translation.Get("solverFuncCrosstotal");
	}

	@Override
	public String getDescription() {
		return Translation.Get("solverDescCrosstotal");
	}

	@Override
	public String Calculate(String[] parameter) {
		if (parameter.length != 1) {
			return Translation.Get("solverErrParamCount", "1", "$solverFuncCrosstotal");
		}
		String wert = parameter[0].trim();
		int result = 0;
		for (char c : wert.toCharArray()) {
			int i = c - 48;
			if ((i >= 0) && (i <= 9))
				result += i;
		}
		return String.valueOf(result);
	}

	@Override
	public int getAnzParam() {
		return 1;
	}

	@Override
	public boolean needsTextArgument() {
		return false;
	}

	@Override
	public DataType getParamType(int i) {
		switch (i) {
		case 0:
			return DataType.Integer;
		default:
			return DataType.None;
		}
	}

	@Override
	public DataType getReturnType() {
		return DataType.Integer;
	}

	@Override
	public String getParamName(int i) {
		switch (i) {
		case 0:
			return "solverParamInteger";
		default:
			return super.getParamName(i);
		}
	}
}
