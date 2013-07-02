package CB_Core.Solver.Functions;

import java.util.ArrayList;

import CB_Core.Solver.EntityList;
import CB_Core.Solver.TempEntity;

public class Functions extends ArrayList<Function> {
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
	
    
    public String getName()
    {
    	return Name;
    }
}
