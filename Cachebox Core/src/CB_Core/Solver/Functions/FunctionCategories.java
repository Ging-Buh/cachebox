package CB_Core.Solver.Functions;

import java.util.TreeMap;

import CB_Core.Solver.EntityList;
import CB_Core.Solver.TempEntity;

public class FunctionCategories extends TreeMap<String, Functions> {

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
      this.put(functions.Name, functions);
      functions = new Functions("solverGroupNumbers");
      functions.add(new FunctionQuersumme());
      functions.add(new FunctionIQuersumme());
/*      functions.Add(new FunctionQuerprodukt());
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
