package de.droidcachebox.Solver.Functions;

import java.util.ArrayList;

import de.droidcachebox.Solver.EntityList;
import de.droidcachebox.Solver.FunctionEntity;
import de.droidcachebox.Solver.TempEntity;

public abstract class Function {

    public String Name() { return getName(); }
    public ArrayList<String> Names = new ArrayList<String>();
    public String Description() { return getDescription(); }

    public Function()
    {
    }

    public abstract String getName();
    public abstract String getDescription();
    public abstract String Calculate(String[] parameter);

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
