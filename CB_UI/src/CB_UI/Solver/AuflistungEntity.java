package CB_UI.Solver;

import java.util.ArrayList;

public class AuflistungEntity extends Entity {
    ArrayList<Entity> Liste = new ArrayList<Entity>();
    public AuflistungEntity(int id)
    {
    	super(id);
    }

    @Override
    public void ReplaceTemp(Entity source, Entity dest)
    {
      for (int i = 0; i < Liste.size(); i++)
      {
        Entity entity = Liste.get(i);
        if (entity == source)
        {
          Liste.remove(i);
          Liste.add(i, dest);
        }
      }
    }

    @Override
    public void GetAllEntities(ArrayList<Entity> list)
    {
      for (Entity entity : Liste)
        list.add(entity);
    }

    @Override
    public String Berechne()
    {
      String result = "";
      for (Entity entity : Liste)
      {
        result += entity.Berechne();
      }
      return result;
    }

    @Override
    public String ToString()
    {
      String result = "A" + Id + ":(";
      for (Entity entity : Liste)
        result += entity.ToString() + ",";
      result = result.substring(0, result.length() - 1);
      result += ")";
      return result;
    }
}
