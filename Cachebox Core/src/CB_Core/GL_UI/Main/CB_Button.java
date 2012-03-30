package CB_Core.GL_UI.Main;

import java.util.ArrayList;

import CB_Core.GL_UI.ButtonSprites;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.Math.CB_RectF;

public class CB_Button extends Button
{

	ArrayList<CB_ActionButton> mButtonActions;

	public CB_Button(CB_RectF rec, String Name, ArrayList<CB_ActionButton> ButtonActions)
	{
		super(rec, Name);
		mButtonActions = ButtonActions;
		setOnClickListner();
		setOnLongClickListner();
	}

	public CB_Button(CB_RectF rec, String Name)
	{
		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
		setOnClickListner();
		setOnLongClickListner();
	}

	public CB_Button(CB_RectF rec, String Name, ButtonSprites sprites)
	{
		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
		setOnClickListner();
		setOnLongClickListner();
		this.setButtonSprites(sprites);
	}

	public void addAction(CB_ActionButton Action)
	{
		mButtonActions.add(Action);
	}

	@Override
	protected void Initial()
	{

	}

	private void setOnLongClickListner()
	{
		this.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				GL_MsgBox.Show("Button " + Me.getName() + " recivet a LongClick Event");
				return false;
			}
		});
	}

	private void setOnClickListner()
	{
		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				GL_MsgBox.Show("Button " + Me.getName() + " recivet a Click Event");
				return true;
			}
		});
	}

}
