package CB_Core.Solver;

import java.util.ArrayList;

import CB_Core.Solver.Functions.Function;

public class FunctionEntity extends Entity
{

	private Function function;
	private Entity entity;

	public FunctionEntity(Solver solver, int id, Function function, Entity entity)
	{
		super(solver, id);
		this.function = function;
		this.entity = entity;
	}

	@Override
	public void ReplaceTemp(Entity source, Entity dest)
	{
		if (entity == source) entity = dest;
	}

	@Override
	public void GetAllEntities(ArrayList<Entity> list)
	{
		list.add(entity);
	}

	@Override
	public String Berechne()
	{
		String[] str;
		if (entity instanceof ParameterEntity)
		{
			str = ((ParameterEntity) entity).GetParameter();
		}
		else
		{
			String argument = entity.Berechne();
			str = new String[1];
			str[0] = argument;
		}
		for (String s : str)
		{
			if (Solver.isError(s))
			{
				return s; // einer der aufrufenden Parameter ist eine Fehlermeldung -> Fehlermeldung direkt ausgeben!
			}
		}
		return function.Calculate(str);
	}

	@Override
	public String ToString()
	{
		return "F:" + function + "(" + entity.ToString() + ")";
	}
}
