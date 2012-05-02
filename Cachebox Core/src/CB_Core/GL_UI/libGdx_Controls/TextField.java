package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.Math.CB_RectF;

public class TextField extends LibGdx_Host_Control
{

	private com.badlogic.gdx.scenes.scene2d.ui.TextField mTextField;

	public TextField(CB_RectF rec, String Name)
	{

		super(rec, new com.badlogic.gdx.scenes.scene2d.ui.TextField(Style.getTextFieldStyle()), Name);

		mTextField = (com.badlogic.gdx.scenes.scene2d.ui.TextField) getActor();
	}

}
