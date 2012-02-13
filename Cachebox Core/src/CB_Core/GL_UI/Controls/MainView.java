package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class MainView extends GL_View_Base
{
	private Image image;

	private MainView Me;

	public MainView(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);

		Me = this;

		Logger.LogCat("Construct MainView " + X + "/" + Y + "/" + "/" + Width + "/" + Height);

		// Initial TestView
		TestView testView = new TestView(100, 200, 200, 200);
		this.addChild(testView);

		// Initial TestView
		ZoomButtons btnZoom = new ZoomButtons(20, 20, 200, 75);
		this.addChild(btnZoom);

		// initial Image
		image = new Image(200, 400, 128, 128);
		image.setImage("data/cb_test.png");
		this.addChild(image);

		Button btn = new Button(300, 100, 200, 64);
		this.addChild(btn);

		btn.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				image.setPos(new Vector2(Me.crossPos.x - image.getWidth(), Me.crossPos.y - image.getHeight()));
				return true;
			}
		});

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				image.setPos(new Vector2(x - image.getWidth() / 2, y - image.getHeight() / 2));
				return true;
			}
		});

		Button btn2 = new Button(300, 200, 64, 64);
		this.addChild(btn2);
		btn2.disable();

		// Dieser Listner sollte nicht ausgeführt werden, da der Button Disabled ist!
		btn2.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				image.setPos(new Vector2(Me.Pos.x, Me.crossPos.y - image.getHeight()));
				return true;
			}
		});
	}

	@Override
	public void render(SpriteBatch batch)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// hier erstmal nichts machen
		return true;
	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}
}