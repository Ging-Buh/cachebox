package de.droidcachebox.Solver.Functions;

import java.util.TreeMap;

import de.droidcachebox.Solver.EntityList;
import de.droidcachebox.Solver.TempEntity;

public class FunctionCategories extends TreeMap<String, Functions> {

	private static final long serialVersionUID = 1L;

	public FunctionCategories()
    {
      Functions functions = new Functions("solverGroupText");
/*      functions.Add(new FunctionAlphaSum());*/
      functions.add(new FunctionAlphaPos());
/*      functions.Add(new FunctionHandyCode());
      functions.Add(new FunctionHandySum());
      functions.Add(new FunctionLength());
      functions.Add(new FunctionReverse());
      functions.Add(new FunctionRot13());*/
      this.put(functions.Name, functions);
      functions = new Functions("solverGroupNumbers");
/*      functions.Add(new FunctionQuersumme());
      functions.Add(new FunctionIQuersumme());
      functions.Add(new FunctionQuerprodukt());
      functions.Add(new FunctionIQuerprodukt());
      functions.Add(new FunctionRom2Dec());
      functions.Add(new FunctionPrimenumber());
      functions.Add(new FunctionPrimeIndex());
      functions.Add(new FunctionInt());
      functions.Add(new FunctionRound());
      functions.Add(new FunctionPi());*/
      this.put(functions.Name, functions);
      functions = new Functions("solverGroupCoordinates");
/*      functions.Add(new FunctionProjection());
      functions.Add(new FunctionIntersection());
      functions.Add(new FunctionCrossbearing());
      functions.Add(new FunctionBearing());
      functions.Add(new FunctionDistance());*/
      this.put(functions.Name, functions);

    }

    public boolean InsertEntities(TempEntity tEntity, EntityList entities)
    {
      for (Functions functions : this.values())
      {
        if (functions.InsertEntities(tEntity, entities))
          return true;
      }
      return false;
    }

    public boolean isFunction(String s)
    {
      for (Functions functions : this.values())
      {
        if (functions.isFunction(s))
          return true;
      }
      return false;
    }

}
