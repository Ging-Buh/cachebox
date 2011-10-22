package CB_Core.Solver.Functions;

import java.io.Serializable;
import java.util.ArrayList;

import CB_Core.Solver.EntityList;
import CB_Core.Solver.FunctionEntity;
import CB_Core.Solver.TempEntity;

public abstract class Function implements Serializable {

    private static final long serialVersionUID = 3322289615650829139L;
	public String Name() { return getName(); }
    public ArrayList<String> Names = new ArrayList<String>();
    public String Description() { return getDescription(); }

    public abstract String getName();
    public abstract String getDescription();
    public abstract String Calculate(String[] parameter);
    public abstract int getAnzParam();
    public abstract boolean needsTextArgument();

    private boolean checkIsFunction(String function, TempEntity tEntity, EntityList entities)
    {
      try
      {
        function = function.toLowerCase();
        int pos = tEntity.Text.toLowerCase().indexOf(function.toLowerCase());
        if (pos < 0)
          return false;
        int pos1 = pos + function.length();  // 1. #
        if (!(tEntity.Text.charAt(pos1) == '#'))
          return false;
        if (pos1 + 1 >= tEntity.Text.length())
          return false;
        int pos2 = tEntity.Text.toLowerCase().indexOf("#", pos1 + 1);
        if (pos2 < pos1)
          return false;
        if (pos2 != tEntity.Text.length() - 1)
          return false;
        if (pos == 0)
        {
          // Insert new Entity 
          TempEntity rechts = new TempEntity(-1, tEntity.Text.substring(pos1, pos2 + 1));
          entities.Insert(rechts);
          FunctionEntity fEntity = new FunctionEntity(-1, this, rechts);
          String var = entities.Insert(fEntity);
          tEntity.Text = var;
          return true;
        }
        else
          return false;
      }
      catch (Exception ex)
      {
        return false;
      }
    }

    public boolean InsertEntities(TempEntity tEntity, EntityList entities)
    {
      if (checkIsFunction(Name(), tEntity, entities))
        return true;
      for (String name2 : Names)
      {
        if (checkIsFunction(name2, tEntity, entities))
          return true;
      }
      return false;
    }
    public boolean isFunction(String s)
    {
      if (Name().toLowerCase().equals(s.toLowerCase()))
        return true;
      for (String name2 : Names)
      {
        if (name2.toLowerCase().equals(s.toLowerCase()))
          return true;
      }
      return false;
    }

}
