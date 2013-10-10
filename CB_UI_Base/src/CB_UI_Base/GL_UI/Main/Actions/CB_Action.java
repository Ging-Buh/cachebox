package CB_UI_Base.GL_UI.Main.Actions;

import CB_Utils.Log.Logger;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action
{

	protected String name;
	protected int id;
	protected String nameExtention = "";

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

	public CB_Action(String name, String nameExtention, int id)
	{
		super();
		this.name = name;
		this.id = id;
		this.nameExtention = nameExtention;
	}

	public void CallExecute()
	{
		Logger.LogCat("ACTION => " + name + " execute");
		Execute();
	}

	protected void Execute()
	{
		return;
	}

	public String getName()
	{
		return name;
	}

	public String getNameExtention()
	{
		return nameExtention;
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
