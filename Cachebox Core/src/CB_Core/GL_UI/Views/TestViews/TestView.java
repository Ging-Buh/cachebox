package CB_Core.GL_UI.Views.TestViews;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.DrawUtils;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Activitys.EditWaypoint;
import CB_Core.GL_UI.Activitys.EditWaypoint.ReturnListner;
import CB_Core.GL_UI.Activitys.ProjectionCoordinate;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CoordinateButton;
import CB_Core.GL_UI.Controls.Dialogs.SolverDialog;
import CB_Core.GL_UI.Controls.Dialogs.WaitDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Enthält die TestContols
 * 
 * @author Longri
 */
public class TestView extends CB_View_Base
{
	private Button TestButton;
	private SolverDialog solverDialog;

	private CB_Core.GL_UI.libGdx_Controls.TextField textField;
	private CB_Core.GL_UI.libGdx_Controls.WrappedTextField wrappedTextField;

	public static final String br = "¶" + System.getProperty("line.separator");

	public static final String splashMsg = "Team" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br
			+ "Groundspeak Inc. Used with permission" + br + " " + br + "7.Zeile";

	public static final String FONT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;,{}\"´`'<>";

	private int fontsize = 18;

	Button btn1;
	Button btn2;
	WaitDialog wd;

	public TestView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		this.setClickable(true);

		setBackground(SpriteCache.ListBack);

		bf = new BitmapFontCache(Fonts.getBig());
		bf.setText("BF Test", 10, 300);

		// ####################################################

		CB_RectF btnRec = new CB_RectF(0, 200, UiSizes.getButtonWidth() * 2, UiSizes.getButtonHeight());

		btn1 = new Button(btnRec, "");
		btn2 = new Button(btnRec, "");

		btn1.setX(20);
		btn2.setX(btn1.getMaxX());

		this.addChild(btn1);
		this.addChild(btn2);

		btn1.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				Coordinate pos = new Coordinate("N 52 27.354  E 13 30.690");
				ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), "Projection", pos,
						new CB_Core.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
						{

							@Override
							public void returnCoord(Coordinate coord)
							{
								// TODO Auto-generated method stub

							}
						}, false);

				pC.show();

				return true;
			}
		});

		btn2.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (GlobalCore.SelectedCache() != null)
				{
					if (GlobalCore.SelectedCache().waypoints.size() > 0)
					{
						Waypoint wp = GlobalCore.SelectedCache().waypoints.get(0);
						EditWaypoint EdWp = new EditWaypoint(ActivityBase.ActivityRec(), "EditWP", wp, new ReturnListner()
						{

							@Override
							public void returnedWP(Waypoint wp)
							{
								// TODO Auto-generated method stub

							}
						});
						EdWp.show();
					}
					else
					{
						GL_Listener.glListener.Toast("Der Cache hat keine WP´s", 200);
					}
				}
				return true;
			}
		});

		requestLayout();

		// Coord edit

		Coordinate pos = new Coordinate("N 52 27.354  E 13 30.690");
		CoordinateButton cBtn = new CoordinateButton(new CB_RectF(50, 120, this.width - 100, 65), "CoordButton", pos);
		this.addChild(cBtn);

	}

	OnMsgBoxClickListener click = new OnMsgBoxClickListener()
	{

		@Override
		public boolean onClick(int which)
		{
			// TODO Auto-generated method stub
			return false;
		}
	};

	BitmapFontCache bf;
	BitmapFontCache ttf;

	@Override
	protected void render(SpriteBatch batch)
	{
		// drawHausVomNikolaus(batch);

		if (bf != null) bf.draw(batch);
		if (ttf != null) ttf.draw(batch);

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

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

}
