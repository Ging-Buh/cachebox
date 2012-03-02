package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.ArrowView;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MapInfoPanel;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_Core.GL_UI.Controls.ZoomButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Enthält die TestContols
 * 
 * @author Longri
 */
public class TestView extends GL_View_Base
{
	private Image image;
	private Label lbl;
	private ZoomButtons btnZoom;
	private MultiToggleButton togBtn;
	private MapInfoPanel info;

	public TestView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		GL_Listener.glListener.addRenderView(this, GL_Listener.FRAME_RATE_IDLE);

		// Initial TestView
		ArrowView testView = new ArrowView(300, 300, 100, 100, "Test_ArrowView");
		this.addChild(testView);

		// Initial TestView
		btnZoom = new ZoomButtons(GL_UISizes.ZoomBtn, this, "ZoomButtons");
		btnZoom.setClickable(true);

		btnZoom.setOnClickListenerDown(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				image.setSize(new SizeF(image.getWidth() - 5, image.getHeight() - 5));
				return false;
			}
		});

		btnZoom.setOnClickListenerUp(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				image.setSize(new SizeF(image.getWidth() + 5, image.getHeight() + 5));
				return false;
			}
		});

		this.addChild(btnZoom);

		// initial Image
		image = new Image(300, 400, 128, 128, "Test_Image");
		image.setImage("data/cb_test.png");
		this.addChild(image);

		Button btn = new Button(300, 100, 200, 64, this, "Test_Btn");
		this.addChild(btn);
		btn.setText("Button");
		btn.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				image.setPos(new Vector2(Me.getCrossPos().x - image.getWidth(), Me.getCrossPos().y - image.getHeight()));
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

		Button btn2 = new Button(300, 200, 64, 64, this, "Test_BtnDis");
		this.addChild(btn2);
		btn2.disable();
		btn2.setText("DISABLED");

		// Dieser Listner sollte nicht ausgeführt werden, da der Button Disabled ist!
		btn2.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// image.setPos(new Vector2(Me.getPos().x, Me.getCrossPos().y - image.getHeight()));
				return true;
			}
		});

		// Label Tests
		lbl = new Label(10, 410, 270, 30, "Test_Lbl");
		lbl.setFont(Fonts.get16());
		lbl.setText("Single Line Text");
		lbl.setHAlignment(HAlignment.CENTER);
		this.addChild(lbl);

		togBtn = new MultiToggleButton(GL_UISizes.Toggle, this, "toggle");

		togBtn.addState("Free", Color.GRAY);
		togBtn.addState("GPS", Color.GREEN);
		togBtn.addState("Lock", Color.RED);
		togBtn.addState("Car", Color.YELLOW);
		togBtn.setLastStateWithLongClick(true);
		togBtn.setOnStateChangedListner(new OnStateChangeListener()
		{

			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				lbl.setText("State Change {" + v.getName() + "} State:" + State);
			}
		});
		this.addChild(togBtn);

		String br = System.getProperty("line.separator");
		String s = "Ein Multiline Text!" + br;
		s += "Zeile 2" + br;
		s += "Zeile 2" + br;

		Label lbl2 = new Label(10, 310, 250, 70, "Test_Lbl2");
		lbl2.setFont(Fonts.get18());
		lbl2.setMultiLineText(s);
		this.addChild(lbl2);

		s = "Ein Wraped Text, welcher automatisch umgebrochen wird, wenn dieser zu lang für das Label ist." + br + br;
		s += "Es wird aber auch ein Line Breake innerhalb des Textes erkannt und zusätzlich umgebrochen." + br;

		Label lbl3 = new Label(10, 100, 270, 200, "Test_Lbl3");
		lbl3.setFont(Fonts.get18());
		lbl3.setWrappedText(s);

		NinePatch back = new NinePatch(SpriteCache.uiAtlas.findRegion("shaddowrect"), 8, 8, 8, 8);

		lbl3.setBackground(back);

		lbl3.setTextMargin(10);

		this.addChild(lbl3);

		// MapInfoPanel
		info = (MapInfoPanel) this.addChild(new MapInfoPanel(GL_UISizes.Info, "InfoPanel"));

		requestLayout();
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		String str = "fps: " + Gdx.graphics.getFramesPerSecond();
		Fonts.get18().draw(batch, str, 20, 100);

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		requestLayout();
	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
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

	@Override
	public void onParentRezised(CB_RectF rec)
	{
		this.setSize(rec.getSize());
	}

	private void requestLayout()
	{
		Logger.LogCat("TestView clacLayout()");
		float margin = GL_UISizes.margin;
		info.setPos(new Vector2(margin, (float) (this.height - margin - info.getHeight())));
		togBtn.setPos(new Vector2((float) (this.width - margin - togBtn.getWidth()), this.height - margin - togBtn.getHeight()));

		GL_Listener.glListener.renderOnce(this);
	}

}
