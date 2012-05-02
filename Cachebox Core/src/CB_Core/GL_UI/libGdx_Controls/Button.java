package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.Math.CB_RectF;

public class Button extends LibGdx_Host_Control
{

	private com.badlogic.gdx.scenes.scene2d.ui.Button mButton;

	public Button(CB_RectF rec, String Name, CB_Core.GL_UI.ButtonSprites sprites)
	{

		super(rec, new com.badlogic.gdx.scenes.scene2d.ui.Button(sprites.getNormal(), sprites.getPressed()), Name);

		mButton = (com.badlogic.gdx.scenes.scene2d.ui.Button) getActor();
	}

}
