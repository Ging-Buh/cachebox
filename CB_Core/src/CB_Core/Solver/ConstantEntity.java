package CB_Core.Solver;

import java.util.ArrayList;

public class ConstantEntity extends Entity
{

	double wert;

	public ConstantEntity(Solver solver, int id, double wert)
	{
		super(solver, id);
		this.wert = wert;
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
		String value = String.valueOf(wert);
		if (Math.round(wert) == wert)
		{
			long iv = (long) Math.round(wert);
			value = String.valueOf(iv);
		}
		return value;
	}

	@Override
	public String ToString()
	{
		String value = String.valueOf(wert);
		if (Math.round(wert) == wert) value = String.valueOf((int) (Math.round(wert)));
		return "C" + Id + ":(" + value + ")";
	}

}
