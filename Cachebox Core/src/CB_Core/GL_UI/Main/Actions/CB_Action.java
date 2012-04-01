package CB_Core.GL_UI.Main.Actions;

public class CB_Action
{
	protected String name;
	protected int id;

	public CB_Action(String name, int id)
	{
		super();
		this.name = name;
		this.id = id;
	}

	public void Execute()
	{
		return;
	}

	public String getName()
	{
		return name;
	}

	public int getId()
	{
		return id;
	}

	// hiermit kann der Menüpunkt enabled oder disabled werden
	public boolean getEnabled()
	{
		return true;
	}
}
