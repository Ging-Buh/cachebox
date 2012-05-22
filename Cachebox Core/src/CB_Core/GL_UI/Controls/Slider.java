package CB_Core.GL_UI.Controls;

import CB_Core.Config;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Handler;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Slider extends CB_View_Base implements SelectedCacheEvent
{
	private final int ANIMATION_TIME = 50;// 50;
	private static Slider that;
	private QuickButtonList quickButtonList;

	private Label mLblCacheName;
	private Box mSlideBox;
	private int QuickButtonMaxHeight;
	private int QuickButtonHeight;

	private boolean swipeUp = false;
	private boolean swipeDown = false;

	private boolean AnimationIsRunning = false;
	private final double AnimationMulti = 1.4;
	private int AnimationDirection = -1;
	private int AnimationTarget = 0;
	private boolean isKinetigPan = false;

	private Handler handler = new Handler();

	private float yPos = 0;

	public Slider(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		SelectedCacheEventList.Add(this);
		this.setClickable(true);

		QuickButtonMaxHeight = UiSizes.getQuickButtonListHeight();

		quickButtonList = new QuickButtonList(new CB_RectF(0, this.height - QuickButtonMaxHeight, this.width, QuickButtonMaxHeight),
				"QuickButtonList");
		this.addChild(quickButtonList);

		mSlideBox = new Box(new CB_RectF(-15, 100, this.width + 30, UiSizes.getInfoSliderHeight()), "SlideBox");
		mSlideBox.setBackground(new NinePatch(SpriteCache.ToggleBtn.get(0), 16, 16, 16, 16));
		mLblCacheName = new Label(new CB_RectF(20, 0, this.width - 30, mSlideBox.getHeight()), "CacheNameLbl");
		mLblCacheName.setPos(30, 0);
		mLblCacheName.setHAlignment(HAlignment.CENTER);
		mSlideBox.addChild(mLblCacheName);
		this.addChild(mSlideBox);

	}

	@Override
	protected void Initial()
	{
		setSliderPos(this.height - mSlideBox.getHeight());
		ActionUp();
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);
		// renderDebugInfo(batch);
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		if (cache != null)
		{
			mLblCacheName.setText(cache.Name);
		}

	}

	private void setSliderPos(float value)
	{
		if (value == yPos || mSlideBox == null) return;

		yPos = value;
		mSlideBox.setY(value);
		setQuickButtonListHeight();
		GL_Listener.glListener.renderOnce(this.name);
	}

	private void setQuickButtonListHeight()
	{
		if (Config.settings.quickButtonShow.getValue())
		{
			if (this.height - mSlideBox.getMaxY() < QuickButtonMaxHeight)
			{
				quickButtonList.setHeight(this.height - mSlideBox.getMaxY());
				quickButtonList.setY(this.height - quickButtonList.getHeight());
				TabMainView.that.setContentMaxY(mSlideBox.getY());
			}
			else
			{
				quickButtonList.setHeight(QuickButtonMaxHeight);
				quickButtonList.setY(this.height - quickButtonList.getHeight());
				TabMainView.that.setContentMaxY(this.height - quickButtonList.getHeight() - mSlideBox.getHeight());
			}
		}
		else
		{
			quickButtonList.setHeight(0);
		}
	}

	int debugInt = 0;

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		if (KineticPan)
		{
			GL_Listener.glListener.StopKinetic(x, y, pointer, true);
			isKinetigPan = true;
			return onTouchUp(x, y, pointer, 0);
		}

		float newY = y - mSlideBox.getHeight() - touchYoffset;
		setSliderPos(newY);
		return true;
	}

	private float touchYoffset = 0;

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		debugInt = y;
		isKinetigPan = false;
		oneTouchUP = false;
		AnimationIsRunning = false;
		if (mSlideBox.contains(x, y))
		{
			touchYoffset = y - mSlideBox.getMaxY();
			return true;
		}

		return false;
	}

	private boolean oneTouchUP = false;

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		if (isKinetigPan)
		{
			if (oneTouchUP) return true;
			oneTouchUP = true;
		}

		ActionUp();
		return true;
	}

	public void ActionUp() // Slider zurück scrolllen lassen
	{
		// Logger.LogCat("ActionUP");

		boolean QuickButtonShow = Config.settings.quickButtonShow.getValue();

		// check if QuickButtonList snap in
		if (this.height - mSlideBox.getMaxY() >= (QuickButtonMaxHeight * 0.5) && QuickButtonShow)
		{
			QuickButtonHeight = QuickButtonMaxHeight;
			Config.settings.quickButtonLastShow.setValue(true);
			Config.AcceptChanges();
		}
		else
		{
			QuickButtonHeight = 0;
			Config.settings.quickButtonLastShow.setValue(false);
			Config.AcceptChanges();
		}

		if (swipeUp || swipeDown)
		{
			if (swipeUp)
			{
				startAnimationTo(QuickButtonShow ? QuickButtonHeight : 0);
			}
			else
			{
				startAnimationTo((int) (height - mSlideBox.getHeight()));
			}
			swipeUp = swipeDown = false;

		}
		else
		{
			if (yPos > height * 0.7)
			{
				startAnimationTo((int) (height - mSlideBox.getHeight() - (QuickButtonShow ? QuickButtonHeight : 0)));
			}
			else
			{
				startAnimationTo(0);

			}
		}
	}

	private void startAnimationTo(int newYPos)
	{
		if (yPos == newYPos) return; // wir brauchen nichts Animieren

		// Logger.LogCat("Start Animation To " + newYPos);

		AnimationIsRunning = true;
		AnimationTarget = newYPos;
		if (yPos > newYPos) AnimationDirection = -1;
		else
			AnimationDirection = 1;
		handler.postDelayed(AnimationTask, ANIMATION_TIME);
	}

	Runnable AnimationTask = new Runnable()
	{

		@Override
		public void run()
		{

			if (!AnimationIsRunning) return; // Animation wurde abgebrochen

			int newValue = 0;
			if (AnimationDirection == -1)
			{
				float tmp = yPos - AnimationTarget;
				if (tmp <= 0)// Div 0 vehindern
				{
					setPos_onUI(AnimationTarget);
					AnimationIsRunning = false;
				}

				newValue = (int) (yPos - (tmp / AnimationMulti));
				if (newValue <= AnimationTarget)
				{
					setPos_onUI(AnimationTarget);
					AnimationIsRunning = false;
				}
				else
				{
					setPos_onUI(newValue);
					handler.postDelayed(AnimationTask, ANIMATION_TIME);
				}
			}
			else
			{
				float tmp = AnimationTarget - yPos;
				if (tmp <= 0)// Div 0 vehindern
				{
					setPos_onUI(AnimationTarget);
					AnimationIsRunning = false;
				}
				else
				{
					newValue = (int) (yPos + (tmp / AnimationMulti));
					if (newValue >= AnimationTarget)
					{
						setPos_onUI(AnimationTarget);
						AnimationIsRunning = false;
					}
					else
					{
						setPos_onUI(newValue);
						handler.postDelayed(AnimationTask, ANIMATION_TIME);
					}
				}

			}

		}

	};

	private void setPos_onUI(final int newValue)
	{

		this.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				setSliderPos(newValue);
			}
		});

	}

	private void renderDebugInfo(SpriteBatch batch)
	{
		if (false) return;

		String str = String.valueOf(debugInt);
		Fonts.getNormal().draw(batch, str, 20, 120);

		str = "fps: " + Gdx.graphics.getFramesPerSecond();
		Fonts.getNormal().draw(batch, str, 20, 100);

		str = String.valueOf(touchYoffset);
		Fonts.getNormal().draw(batch, str, 20, 80);

		// str = "lTiles: " + loadedTiles.size() + " - qTiles: " + queuedTiles.size();
		// Fonts.getNormal().draw(batch, str, 20, 60);
		//
		// str = "TrackPoi: " + RouteOverlay.AllTrackPoints + " -  " + RouteOverlay.ReduceTrackPoints + " [" + RouteOverlay.DrawedLineCount
		// + "]";
		// Fonts.getNormal().draw(batch, str, 20, 40);
		//
		// str = "lastMove: " + lastMovement.x + " - " + lastMovement.y;
		// Fonts.getNormal().draw(batch, str, 20, 20);

	}

	public static void setAndroidSliderPos(int pos)
	{
		if (that != null)
		{
			that.setSliderPos(that.height - pos);
		}
	}

	public static boolean setAndroidSliderHeight(int height)
	{
		if (that != null && that.mSlideBox != null)
		{
			that.mSlideBox.setHeight(height);
			return true;
		}
		return false;
	}

}
