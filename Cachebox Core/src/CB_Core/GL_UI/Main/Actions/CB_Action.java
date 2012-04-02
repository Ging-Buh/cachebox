package CB_Core.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action
{

	public static int AID_TEST1 = -1;
	public static int AID_TEST2 = -2;

	public static int AID_SHOW_MAP = 0;
	public static int AID_SHOW_HINT = 1;

	protected String name;
	protected int id;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            = Translation ID
	 * @param id
	 *            = Action ID ( AID_xxxx )
	 */
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

	/**
	 * hiermit kann der Menüpunkt enabled oder disabled werden
	 * 
	 * @return
	 */
	public boolean getEnabled()
	{
		return true;
	}

	public Sprite getIcon()
	{
		return null;
	}

	public boolean getIsCheckable()
	{
		return false;
	}

	public boolean getIsChecked()
	{
		return false;
	}

}
