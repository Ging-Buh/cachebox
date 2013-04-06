package CB_Core.GL_UI.Main;

import java.util.ArrayList;

import CB_Core.GL_UI.ButtonSprites;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.GestureHelp;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Main.CB_ActionButton.GestureDirection;
import CB_Core.GL_UI.Main.Actions.CB_Action;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Map.Point;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class CB_Button extends Button implements OnClickListener
{

	private ArrayList<CB_ActionButton> mButtonActions;
	private CB_Action_ShowView aktActionView = null;
	private GestureHelp help;
	private boolean GestureIsOn = true;

	public CB_Button(CB_RectF rec, String Name, ArrayList<CB_ActionButton> ButtonActions)
	{
		super(rec, Name);
		mButtonActions = ButtonActions;
		this.setOnClickListener(this);
		this.setOnLongClickListener(longClickListner);
	}

	public CB_Button(CB_RectF rec, String Name)
	{
		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
		this.setOnClickListener(this);
		this.setOnLongClickListener(longClickListner);
	}

	public CB_Button(CB_RectF rec, String Name, ButtonSprites sprites)
	{
		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
		this.setOnClickListener(this);
		this.setOnLongClickListener(longClickListner);
		this.setButtonSprites(sprites);
	}

	public void addAction(CB_ActionButton Action)
	{
		mButtonActions.add(Action);
		GestureDirection gestureDirection = Action.getGestureDirection();
		if (gestureDirection != GestureDirection.None)
		{
			if (help == null)
			{
				float h = GL_UISizes.BottomButtonHeight * 2;
				help = new GestureHelp(new SizeF(h, h), "help");
			}

			NinePatch ninePatch = null;
			if (this.drawableNormal instanceof NinePatchDrawable)
			{
				ninePatch = ((NinePatchDrawable) this.drawableNormal).getPatch();
			}
			else if (this.drawableNormal instanceof SpriteDrawable)
			{
				int p = SpriteCache.patch;
				Sprite s = ((SpriteDrawable) this.drawableNormal).getSprite();
				ninePatch = new NinePatch(s, p, p, p, p);
			}

			help.addBtnIcon(ninePatch);

			if (gestureDirection == GestureDirection.Up)
			{
				help.addUp(Action.getIcon());
			}
			else if (gestureDirection == GestureDirection.Down)
			{
				help.addDown(Action.getIcon());
			}
			else if (gestureDirection == GestureDirection.Left)
			{
				help.addLeft(Action.getIcon());
			}
			else if (gestureDirection == GestureDirection.Right)
			{
				help.addRight(Action.getIcon());
			}
		}
	}

	@Override
	protected void Initial()
	{

	}

	private OnClickListener longClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			// GL_MsgBox.Show("Button " + Me.getName() + " recivet a LongClick Event");
			// Wenn diesem Button mehrere Actions zugeordnet sind dann wird nach einem Lang-Click ein Menü angezeigt aus dem eine dieser
			// Actions gewählt werden kann

			if (mButtonActions.size() > 1)
			{
				getLongClickMenu().Show();
			}
			else if (mButtonActions.size() == 1)
			{
				// nur eine Action dem Button zugeordnet -> diese Action gleich ausführen
				CB_ActionButton ba = mButtonActions.get(0);
				CB_Action action = ba.getAction();
				if (action != null)
				{
					action.CallExecute();
					if (action instanceof CB_Action_ShowView) aktActionView = (CB_Action_ShowView) action;
					// else
					// aktActionView = null;
				}
			}

			// Show Gester Help

			if (help != null)
			{
				CB_RectF rec = CB_Button.this.ThisWorldRec;

				help.setPos(rec.getX(), rec.getMaxY());
				GL.that.Toast(help, 2000);
			}

			return true;
		}
	};

	private Menu getLongClickMenu()
	{
		Menu cm = new Menu("Name");

		cm.addItemClickListner(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				int mId = ((MenuItem) v).getMenuItemId();

				for (CB_ActionButton ba : mButtonActions)
				{
					if (ba.getAction().getId() == mId)
					{
						CB_Action action = ba.getAction();

						action.CallExecute();
						if (action instanceof CB_Action_ShowView) aktActionView = (CB_Action_ShowView) action;

						GL.that.closeToast();
						break;
					}
				}

				return true;
			}
		});

		for (CB_ActionButton ba : mButtonActions)
		{
			CB_Action action = ba.getAction();
			if (action == null) continue;
			MenuItem mi = cm.addItem(action.getId(), action.getName(), action.getNameExtention());
			mi.setEnabled(action.getEnabled());
			mi.setCheckable(action.getIsCheckable());
			mi.setChecked(action.getIsChecked());
			Sprite icon = action.getIcon();
			if (icon != null) mi.setIcon(new SpriteDrawable(action.getIcon()));
			else
				icon = null;
		}
		return cm;
	}

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
	{

		// Einfacher Click -> alle Actions durchsuchen, ob die aktActionView darin enthalten ist und diese sichtbar ist
		if ((aktActionView != null) && (aktActionView.HasContextMenu()))
		{
			for (CB_ActionButton ba : mButtonActions)
			{
				if (ba.getAction() == aktActionView)
				{
					if (aktActionView.getView().isVisible())
					{
						// Dieses View ist aktuell das Sichtbare
						// -> ein Click auf den Menü-Button zeigt das Contextmenü
						// if (aktActionView.ShowContextMenu()) return true;

						if (aktActionView.HasContextMenu())
						{
							// das View Context Menü mit dem LongKlick Menü zusammen führen!

							Menu viewContextMenu = aktActionView.getContextMenu();

							// Menu zusammen stellen!
							// zuerst das View Context Menu
							Menu compoundMenu = new Menu("compoundMenu");

							compoundMenu.addItems(viewContextMenu.getItems());

							compoundMenu.addItemClickListner(viewContextMenu.getItemClickListner());

							// add divider
							compoundMenu.addDivider();

							Menu LongClickMenu = getLongClickMenu();
							compoundMenu.addItems(LongClickMenu.getItems());
							compoundMenu.addItemClickListner(LongClickMenu.getItemClickListner());

							compoundMenu.reorganizeIndexes();

							compoundMenu.Show();

							return true;
						}

					}
				}
			}
		}
		// Einfacher Click -> Default Action starten

		boolean actionExecuted = false;
		for (CB_ActionButton ba : mButtonActions)
		{
			if (ba.isDefaultAction())
			{
				CB_Action action = ba.getAction();
				if (action != null)
				{
					action.CallExecute();
					if (action instanceof CB_Action_ShowView) aktActionView = (CB_Action_ShowView) action;
					actionExecuted = true;
					break;
				}
			}
		}

		// wenn keine Default Action defeniert ist, dann einen LongClick (Zeige ContextMenu) ausführen
		if (!actionExecuted)
		{
			OnClickListener listner = this.getOnLongClickListner();
			if (listner != null)
			{
				return listner.onClick(v, x, y, pointer, button);
			}
		}

		return true;
	}

	public void performClick()
	{
		onClick(null, 0, 0, 0, 0);
	}

	// ---------- überschreiben des isPresed, weil dies zur Anzeige der Activen View benutzt wird ---------

	protected static Sprite menuSprite;

	public static void reloadMenuSprite()
	{
		menuSprite = null;
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		boolean hasContextMenu = false;

		if (aktActionView != null && aktActionView.getView() != null)
		{
			isPressed = aktActionView.getView().isVisible();
			hasContextMenu = aktActionView.HasContextMenu();
		}
		super.render(batch);

		if (hasContextMenu && isPressed)
		{
			// draw Menu Sprite
			if (menuSprite == null)
			{
				float iconWidth = this.width / 5f;
				float iconHeight = this.height / 2.3f;
				float Versatz = this.height / 38f;

				menuSprite = new Sprite(SpriteCache.Icons.get(IconName.menu_37.ordinal()));
				menuSprite.setBounds(this.width - iconWidth - Versatz, Versatz, iconWidth, iconHeight);
			}

			if (menuSprite != null) menuSprite.draw(batch);
		}
	}

	// --------------------------------------------------------------------------------------------------

	// Auswertung der Finger-Gesten zum Schnellzugriff auf einige ButtonActions
	private boolean isDragged = false;
	private Point downPos = null;

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		isDragged = false;
		downPos = new Point(x, y);
		boolean ret = super.onTouchDown(x, y, pointer, button);

		return (GestureIsOn) ? ret : false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		super.onTouchDragged(x, y, pointer, KineticPan);

		if (!GestureIsOn) return false;

		if (KineticPan) GL.that.StopKinetic(x, y, pointer, true);
		isDragged = true;
		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		boolean result = super.onTouchUp(x, y, pointer, button);

		if (!isDragged) return (GestureIsOn) ? result : true;
		// Logger.LogCat("CB_Button onTouchUP()");
		int dx = x - downPos.x;
		int dy = y - downPos.y;
		GestureDirection direction = GestureDirection.Up;
		if (Math.abs(dx) > Math.abs(dy))
		{
			if (dx > 0) direction = GestureDirection.Right;
			else
				direction = GestureDirection.Left;
		}
		else
		{
			if (dy > 0) direction = GestureDirection.Up;
			else
				direction = GestureDirection.Down;
		}
		for (CB_ActionButton ba : mButtonActions)
		{
			if (ba.getGestureDirection() == direction)
			{
				CB_Action action = ba.getAction();
				if (action != null)
				{
					action.CallExecute();
					if (action instanceof CB_Action_ShowView) aktActionView = (CB_Action_ShowView) action;
					// else
					// aktActionView = null;
					break;
				}

			}
		}
		isDragged = false;
		return true;
	}

	public CB_Button disableGester()
	{
		GestureIsOn = false;
		return this;
	}

	public CB_Button enableGester()
	{
		GestureIsOn = true;
		return this;
	}

	public void setActView(CB_View_Base View)
	{
		for (CB_ActionButton ba : mButtonActions)
		{
			CB_Action action = ba.getAction();
			CB_Action_ShowView ActionView = null;
			if (action != null)
			{
				if (action instanceof CB_Action_ShowView) ActionView = (CB_Action_ShowView) action;
				if (ActionView != null && ActionView.getView() == View)
				{
					aktActionView = ActionView;
					break;
				}
			}

		}
	}

}
