package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionAlphaPos extends Function {

    public FunctionAlphaPos()
    {
      Names.add("AlphaPos");
      Names.add("AP");
    }

	@Override
	public String getName() {
		return GlobalCore.Translations.Get("solverFuncAlphaPos");
//		return "AlphaPos";
	}

	@Override
	public String getDescription() {
		return GlobalCore.Translations.Get("solverDescAlphaPos");
//		return "Position des ersten Zeichens im Alphabet";
	}

	@Override
	public String Calculate(String[] parameter) {
		if (parameter.length != 1)
      	{
			return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
//			return "Diese Funktion benötigt %s Parameter".replace("%s", "1");
      	}
		String wert = parameter[0].trim().toLowerCase();
		if (wert == "")
			return "0";
		char c = wert.charAt(0);
		int result = (int)c - (int)('a') + 1;
		return String.valueOf(result);
	}
}
