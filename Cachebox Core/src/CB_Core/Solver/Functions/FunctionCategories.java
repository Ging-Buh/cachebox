package CB_Core.Solver.Functions;

import java.util.TreeMap;

import CB_Core.Solver.EntityList;
import CB_Core.Solver.TempEntity;

public class FunctionCategories extends TreeMap<String, Functions>
{

	private static final long serialVersionUID = 1L;

	public FunctionCategories()
	{
		Functions functions = new Functions("solverGroupText");
		functions.add(new FunctionAlphaSum());
		functions.add(new FunctionAlphaPos());
		functions.add(new FunctionHandyCode());
		functions.add(new FunctionHandySum());
		functions.add(new FunctionLength());
		functions.add(new FunctionReverse());
		functions.add(new FunctionRot13());
		functions.add(new FunctionMid());
		this.put(functions.Name, functions);
		functions = new Functions("solverGroupNumbers");
		functions.add(new FunctionQuersumme());
		functions.add(new FunctionIQuersumme());
		functions.add(new FunctionQuerprodukt());
		functions.add(new FunctionIQuerprodukt());
		functions.add(new FunctionRom2Dec());
		functions.add(new FunctionPrimenumber());
		functions.add(new FunctionInt());
		functions.add(new FunctionRound());
		functions.add(new FunctionPrimeIndex());
		functions.add(new FunctionPi());
		this.put(functions.Name, functions);
		functions = new Functions("solverGroupCoordinates");
		functions.add(new FunctionProjection());
		functions.add(new FunctionIntersection());
		functions.add(new FunctionCrossbearing());
		functions.add(new FunctionBearing());
		functions.add(new FunctionDistance());
		this.put(functions.Name, functions);

	}

	public boolean InsertEntities(TempEntity tEntity, EntityList entities)
	{
		for (Functions functions : this.values())
		{
			if (functions.InsertEntities(tEntity, entities)) return true;
		}
		return false;
	}

	public boolean isFunction(String s)
	{
		for (Functions functions : this.values())
		{
			if (functions.isFunction(s)) return true;
		}
		return false;
	}

}
