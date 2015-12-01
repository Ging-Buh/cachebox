package CB_Core.Solver;

import java.util.ArrayList;

public class Functions extends ArrayList<Function>
{
	private static final long serialVersionUID = 132452456262L;

	String Name;

	public Functions(String name)
	{
		this.Name = name;
	}

	public boolean InsertEntities(TempEntity tEntity, EntityList entities)
	{
		for (Function function : this)
		{
			if (function.InsertEntities(tEntity, entities)) return true;
		}
		return false;
	}

	public boolean isFunction(String s)
	{
		for (Function function : this)
		{
			if (function.isFunction(s)) return true;
		}
		return false;
	}

	public Function getFunction(String s)
	{
		for (Function function : this)
		{
			if (function.isFunction(s)) return function;
		}
		return null;
	}

	public String getName()
	{
		return Name;
	}
}
