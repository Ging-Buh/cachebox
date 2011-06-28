package de.droidcachebox.Solver.Functions;

public class FunctionAlphaPos extends Function {

    public FunctionAlphaPos()
    {
      Names.add("AlphaPos");
      Names.add("AP");
    }

	@Override
	public String getName() {
//		return Global.Translations.Get("solverFuncAlphaPos");
		return "AlphaPos";
	}

	@Override
	public String getDescription() {
//		return Global.Translations.Get("solverDescAlphaPos");
		return "Position des ersten Zeichens im Alphabet";
	}

	@Override
	public String Calculate(String[] parameter) {
		if (parameter.length != 1)
      	{
//			return Global.Translations.Get("solverErrParamCount").Replace("%s", "1");
			return "Diese Funktion benötigt %s Parameter".replace("%s", "1");
      	}
		String wert = parameter[0].trim().toLowerCase();
		if (wert == "")
			return "0";
		char c = wert.charAt(0);
		int result = (int)c - (int)('a') + 1;
		return String.valueOf(result);
	}
}
