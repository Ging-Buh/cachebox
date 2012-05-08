package CB_Core.GL_UI.Views.TestViews;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialogs.SolverDialog;
import CB_Core.GL_UI.Controls.Dialogs.SolverDialog.SloverBackStringListner;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.TextField;
import CB_Core.GL_UI.libGdx_Controls.WrappedTextField;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Enthält die TestContols
 * 
 * @author Longri
 */
public class TestView extends CB_View_Base
{
	private Button showSolverDialogButton;
	private SolverDialog solverDialog;

	private CB_Core.GL_UI.libGdx_Controls.TextField textField;
	private CB_Core.GL_UI.libGdx_Controls.WrappedTextField wrappedTextField;

	public static final String br = "¶" + System.getProperty("line.separator");

	public static final String splashMsg = "Team" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br
			+ "Groundspeak Inc. Used with permission" + br + " " + br + "7.Zeile";

	public TestView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		CB_RectF r2 = new CB_RectF(20, 350, 250, 250);
		wrappedTextField = new WrappedTextField(r2, "TestWrappedTextField");
		this.addChild(wrappedTextField);

		wrappedTextField.setText(splashMsg);

		CB_RectF r3 = new CB_RectF(20, 600, 250, 40);
		textField = new TextField(r3, "TestTextField");
		this.addChild(textField);

		textField.setText("Single Line Text");

		// ######## Solver Dialog #####################################################
		CB_RectF r4 = new CB_RectF(20, 250, 250, 63);
		showSolverDialogButton = new Button(r4, "SolverDialogButton");
		showSolverDialogButton.setFont(Fonts.getSmall());
		showSolverDialogButton.setText("Solver Dialog with Single Line Text");
		this.addChild(showSolverDialogButton);

		final SloverBackStringListner backListner = new SloverBackStringListner()
		{

			@Override
			public void BackString(String backString)
			{
				textField.setText(backString);
			}
		};

		showSolverDialogButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Show Dialog
				CB_RectF rec = GL_UISizes.UI_Left.copy();
				if (GlobalCore.isTab)
				{
					// da der Linke Tab bei einem Tablett nicht so Breit ist wie auf einem Phone,
					// Verdoppeln wir hier die Breite (sieht besser aus)
					rec.setWidth(rec.getWidth() * 2);
				}

				String SolverString = textField.getText();

				solverDialog = new SolverDialog(rec, "SolverDialog", SolverString);

				solverDialog.show(backListner);

				return true;
			}
		});

		// ####### END Solver Dialog ##################################################

		requestLayout();
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		// drawHausVomNikolaus(batch);

		renderDebugInfo(batch);
	}

	String str;

	private void drawHausVomNikolaus(SpriteBatch batch)
	{
		Sprite ArrowSprite = SpriteCache.Arrows.get(5);

		ArrowSprite.setColor(Color.BLACK);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 100, 100, 300, 300);

		ArrowSprite.setColor(Color.RED);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 300, 300, 100, 300);

		ArrowSprite.setColor(Color.BLUE);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 100, 300, 100, 100);

		ArrowSprite.setColor(Color.CYAN);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 100, 100, 300, 100);

		ArrowSprite.setColor(Color.ORANGE);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 300, 100, 300, 300);

		ArrowSprite.setColor(Color.MAGENTA);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 300, 300, 200, 400);

		ArrowSprite.setColor(Color.YELLOW);
		DrawUtils.drawSpriteLine(batch, ArrowSprite, 1f, 200, 400, 100, 300);
	}

	private void renderDebugInfo(SpriteBatch batch)
	{
		str = "Coursor Pos:" + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorPos) + "/"
				+ String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugRealCursorPos);
		Fonts.getNormal().draw(batch, str, 20, 120);

		str = "LineCount: " + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugLineCount);
		Fonts.getNormal().draw(batch, str, 20, 100);

		str = "L:" + String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorLine) + " R:"
				+ String.valueOf(CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCursorRow);
		Fonts.getNormal().draw(batch, str, 20, 80);

		String ch = CB_Core.GL_UI.libGdx_Controls.derived.WrappedTextField.debugCharBeforCursor;

		str = "Vor Cursor: " + ch;
		Fonts.getNormal().draw(batch, str, 20, 60);

		// str = "TrackPoi: " + RouteOverlay.AllTrackPoints + " -  " + RouteOverlay.ReduceTrackPoints + " [" + RouteOverlay.DrawedLineCount
		// + "]";
		// Fonts.getNormal().draw(batch, str, 20, 40);
		//
		str = "fps: " + Gdx.graphics.getFramesPerSecond();
		Fonts.getNormal().draw(batch, str, 20, 20);

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		requestLayout();
	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{
		this.setSize(rec.getSize());
	}

	private void requestLayout()
	{

		GL_Listener.glListener.renderOnce(this.getName() + " requestLayout");
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelöst wird.
	}

	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		return true;
	}

}
