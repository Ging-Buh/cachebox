package CB_Core.Solver.Functions;

import CB_Core.GlobalCore;

public class FunctionRot13 extends Function
{
    public FunctionRot13()
    {
      Names.add("Rot13");
    }

	@Override
	public String getName()
	{
		return GlobalCore.Translations.Get("solverFuncRot13"); 
	}

	@Override
	public String getDescription()
	{
		return GlobalCore.Translations.Get("solverDescRot13");
	}

	@Override
	public String Calculate(String[] parameter)
	{
	      if (parameter.length != 1)
	      {
	        return GlobalCore.Translations.Get("solverErrParamCount").replace("%s", "1");
	      }
	      return GlobalCore.Rot13(parameter[0]);
	}

}
