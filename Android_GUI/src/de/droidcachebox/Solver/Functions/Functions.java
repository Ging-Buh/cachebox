package de.droidcachebox.Solver.Functions;

import java.util.ArrayList;

import de.droidcachebox.Solver.EntityList;
import de.droidcachebox.Solver.TempEntity;

public class Functions extends ArrayList<Function> {
	private static final long serialVersionUID = 1L;

    String Name;
    public Functions(String name)
    {
      this.Name = name;
    }
    
    public boolean InsertEntities(TempEntity tEntity, EntityList entities)
    {
      for (Function function : this)
      {
        if (function.InsertEntities(tEntity, entities))
          return true;
      }
      return false;
    }
    public boolean isFunction(String s)
    {
      for (Function function : this)
      {
        if (function.isFunction(s))
          return true;
      }
      return false;
    }
	
}
