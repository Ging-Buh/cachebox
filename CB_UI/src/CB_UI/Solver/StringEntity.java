package CB_UI.Solver;

import java.util.ArrayList;

public class StringEntity extends Entity {
    String Wert;
    public StringEntity(int id, String wert)
    {
    	super(id);
    	this.Wert = wert;
    }

    @Override
    public void GetAllEntities(ArrayList<Entity> list)
    {
    }

    @Override
    public void ReplaceTemp(Entity source, Entity dest)
    {
    }

    @Override
    public String Berechne()
    {
      return Wert;
    }
    
    @Override
    public String ToString()
    {
      return "S:" + Id + ":(" + Wert + ")";
    }
}
