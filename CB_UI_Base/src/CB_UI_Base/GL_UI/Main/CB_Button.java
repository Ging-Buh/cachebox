package CB_UI_Base.GL_UI.Main;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_UI_Base.GL_UI.ButtonSprites;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.GestureHelp;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.CB_ActionButton.GestureDirection;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Math.Point;

/**
 * 
 * @author Longri
 *
 */
public class CB_Button extends Button implements OnClickListener {

	private final ArrayList<CB_ActionButton> mButtonActions;
	private CB_Action_ShowView aktActionView = null;
	private GestureHelp help;
	private boolean GestureIsOn = true;

	public CB_Button(CB_RectF rec, String Name, ArrayList<CB_ActionButton> ButtonActions) {
		super(rec, Name);
		mButtonActions = ButtonActions;
		this.setOnClickListener(this);
		this.setOnLongClickListener(longClickListener);
	}

	public CB_Button(CB_RectF rec, String Name) {
		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
		this.setOnClickListener(this);
		this.setOnLongClickListener(longClickListener);
	}

	public CB_Button(CB_RectF rec, String Name, ButtonSprites sprites) {

		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
		this.setOnClickListener(this);
		this.setOnLongClickListener(longClickListener);
		this.setButtonSprites(sprites);
	}

	public void addAction(CB_ActionButton Action) {
		mButtonActions.add(Action);

		// disable Gesture ?
		if (!CB_UI_Base_Settings.GestureOn.getValue())
			Action.setGestureDirection(GestureDirection.None);

		GestureDirection gestureDirection = Action.getGestureDirection();
		if (gestureDirection != GestureDirection.None) {
			if (help == null) {
				float h = GL_UISizes.BottomButtonHeight * 2;
				help = new GestureHelp(new SizeF(h, h), "help");
			}

			NinePatch ninePatch = null;
			if (this.drawableNormal instanceof NinePatchDrawable) {
				ninePatch = ((NinePatchDrawable) this.drawableNormal).getPatch();
			} else if (this.drawableNormal instanceof SpriteDrawable) {
				int p = SpriteCacheBase.patch;
				Sprite s = ((SpriteDrawable) this.drawableNormal).getSprite();
				ninePatch = new NinePatch(s, p, p, p, p);
			}

			help.addBtnIcon(ninePatch);

			if (gestureDirection == GestureDirection.Up) {
				help.addUp(Action.getIcon());
			} else if (gestureDirection == GestureDirection.Down) {
				help.addDown(Action.getIcon());
			} else if (gestureDirection == GestureDirection.Left) {
				help.addLeft(Action.getIcon());
			} else if (gestureDirection == GestureDirection.Right) {
				help.addRight(Action.getIcon());
			}
		}
	}

	@Override
	protected void Initial() {

	}

	private final OnClickListener longClickListener = new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			// GL_MsgBox.Show("Button " + Me.getName() + " recivet a LongClick Event");
			// Wenn diesem Button mehrere Actions zugeordnet sind dann wird nach einem Lang-Click ein Menü angezeigt aus dem eine dieser
			// Actions gewählt werden kann

			if (mButtonActions.size() > 1) {
				getLongClickMenu().Show();
			} else if (mButtonActions.size() == 1) {
				// nur eine Action dem Button zugeordnet -> diese Action gleich ausführen
				CB_ActionButton ba = mButtonActions.get(0);
				CB_Action action = ba.getAction();
				if (action != null) {
					action.CallExecute();
					if (action instanceof CB_Action_ShowView)
						aktActionView = (CB_Action_ShowView) action;
					// else
					// aktActionView = null;
				}
			}

			// Show Gester Help

			if (help != null) {
				CB_RectF rec = CB_Button.this.thisWorldRec;
				if (rec != null) {
					help.setPos(rec.getX(), rec.getMaxY());
					GL.that.Toast(help, 2000);
				}
			}

			return true;
		}
	};

	private Menu getLongClickMenu() {
		Menu cm = new Menu("Name");

		cm.addOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

				int mId = ((MenuItem) v).getMenuItemId();

				for (CB_ActionButton ba : mButtonActions) {
					if (ba.getAction().getId() == mId) {
						CB_Action action = ba.getAction();

						action.CallExecute();
						if (action instanceof CB_Action_ShowView)
							aktActionView = (CB_Action_ShowView) action;

						GL.that.closeToast();
						break;
					}
				}

				return true;
			}
		});

		for (CB_ActionButton ba : mButtonActions) {
			CB_Action action = ba.getAction();
			if (action == null)
				continue;
			MenuItem mi = cm.addItem(action.getId(), action.getName(), action.getNameExtention());
			mi.setEnabled(action.getEnabled());
			mi.setCheckable(action.getIsCheckable());
			mi.setChecked(action.getIsChecked());
			Sprite icon = action.getIcon();
			if (icon != null)
				mi.setIcon(new SpriteDrawable(action.getIcon()));
			else
				icon = null;
		}
		return cm;
	}

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

		// Einfacher Click -> alle Actions durchsuchen, ob die aktActionView darin enthalten ist und diese sichtbar ist
		if ((aktActionView != null) && (aktActionView.hasContextMenu())) {
			for (CB_ActionButton ba : mButtonActions) {
				if (ba.getAction() == aktActionView) {
					if (aktActionView.getView() != null && aktActionView.getView().isVisible()) {
						// Dieses View ist aktuell das Sichtbare
						// -> ein Click auf den Menü-Button zeigt das Contextmenü
						// if (aktActionView.ShowContextMenu()) return true;

						if (aktActionView.hasContextMenu()) {
							// das View Context Menü mit dem LongKlick Menü zusammen führen!

							// Menu zusammen stellen!
							// zuerst das View Context Menu
							Menu compoundMenu = new Menu("compoundMenu");

							Menu viewContextMenu = aktActionView.getContextMenu();
							if (viewContextMenu != null) {
								compoundMenu.addItems(viewContextMenu.getItems());
								compoundMenu.addOnClickListeners(viewContextMenu.getOnItemClickListeners());

								// add divider
								compoundMenu.addDivider();

								// add MoreMenu
								compoundMenu.addMoreMenu(viewContextMenu.getMoreMenu(), viewContextMenu.getTextLeftMoreMenu(), viewContextMenu.getTextRightMoreMenu());
							}

							Menu LongClickMenu = getLongClickMenu();
							if (LongClickMenu != null) {
								compoundMenu.addItems(LongClickMenu.getItems());
								compoundMenu.addOnClickListeners(LongClickMenu.getOnItemClickListeners());

							}

							if (compoundMenu.reorganizeIndexes() > 0) {
								compoundMenu.Show();
							}
							return true;
						}

					}
				}
			}
		}
		// Einfacher Click -> Default Action starten

		boolean actionExecuted = false;
		for (CB_ActionButton ba : mButtonActions) {
			if (ba.isDefaultAction()) {
				CB_Action action = ba.getAction();
				if (action != null) {
					action.CallExecute();
					if (action instanceof CB_Action_ShowView)
						aktActionView = (CB_Action_ShowView) action;
					actionExecuted = true;
					break;
				}
			}
		}

		// wenn keine Default Action defeniert ist, dann einen LongClick (Zeige ContextMenu) ausführen
		if (!actionExecuted) {
			OnClickListener listener = this.getOnLongClickListener();
			if (listener != null) {
				return listener.onClick(v, x, y, pointer, button);
			}
		}

		return true;
	}

	@Override
	public void performClick() {
		onClick(null, 0, 0, 0, 0);
	}

	// ---------- überschreiben des isPresed, weil dies zur Anzeige der Activen View benutzt wird ---------

	protected static Sprite menuSprite;
	protected static Sprite menuSpriteFilterd;

	public static void reloadMenuSprite() {
		menuSprite = null;
		menuSpriteFilterd = null;
	}

	@Override
	protected void render(Batch batch) {
		boolean hasContextMenu = false;

		try {
			if (aktActionView != null && aktActionView.getView() != null) {
				isFocused = aktActionView.getView().isVisible();
				hasContextMenu = aktActionView.hasContextMenu();
			} else {
				isFocused = false;
				hasContextMenu = false;
			}
		} catch (Exception e) {
		}

		super.render(batch);

		if (hasContextMenu && isFocused) {

			// draw Menu Sprite
			if (menuSprite == null || menuSpriteFilterd == null) {
				float iconWidth = this.getWidth() / 5f;
				float iconHeight = this.getHeight() / 2.3f;
				float VersatzX = this.getHeight() / 20f;
				float VersatzY = this.getHeight() / 30f;

				menuSprite = new Sprite(SpriteCacheBase.Icons.get(IconName.menu_37.ordinal()));
				menuSprite.setBounds(this.getWidth() - iconWidth - VersatzX, VersatzY, iconWidth, iconHeight);

				menuSpriteFilterd = new Sprite(SpriteCacheBase.Icons.get(IconName.menuFilterd_65.ordinal()));
				menuSpriteFilterd.setBounds(this.getWidth() - iconWidth - VersatzX, VersatzY, iconWidth, iconHeight);

			}

			boolean isFilterd = false;

			if (!isFilterd && menuSprite != null)
				menuSprite.draw(batch);
			if (isFilterd && menuSpriteFilterd != null)
				menuSpriteFilterd.draw(batch);
		}
	}

	// --------------------------------------------------------------------------------------------------

	// Auswertung der Finger-Gesten zum Schnellzugriff auf einige ButtonActions
	private boolean isDragged = false;
	private Point downPos = null;

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button) {

		isDragged = false;
		downPos = new Point(x, y);
		boolean ret = super.onTouchDown(x, y, pointer, button);

		return (GestureIsOn) ? ret : false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
		super.onTouchDragged(x, y, pointer, KineticPan);

		if (!GestureIsOn)
			return false;

		if (KineticPan)
			GL.that.StopKinetic(x, y, pointer, true);
		isDragged = true;
		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button) {
		boolean result = super.onTouchUp(x, y, pointer, button);

		if (!isDragged)
			return (GestureIsOn) ? result : true;
		// log.debug("CB_Button onTouchUP()");
		int dx = x - downPos.x;
		int dy = y - downPos.y;
		GestureDirection direction = GestureDirection.Up;
		if (Math.abs(dx) > Math.abs(dy)) {
			if (dx > 0)
				direction = GestureDirection.Right;
			else
				direction = GestureDirection.Left;
		} else {
			if (dy > 0)
				direction = GestureDirection.Up;
			else
				direction = GestureDirection.Down;
		}
		for (CB_ActionButton ba : mButtonActions) {
			if (ba.getGestureDirection() == direction) {
				CB_Action action = ba.getAction();
				if (action != null) {
					action.CallExecute();
					if (action instanceof CB_Action_ShowView)
						aktActionView = (CB_Action_ShowView) action;
					// else
					// aktActionView = null;
					break;
				}

			}
		}
		isDragged = false;
		return true;
	}

	public CB_Button disableGester() {
		GestureIsOn = false;
		return this;
	}

	public CB_Button enableGester() {
		GestureIsOn = true;
		return this;
	}

	public void setActView(CB_View_Base View) {
		for (CB_ActionButton ba : mButtonActions) {
			CB_Action action = ba.getAction();
			CB_Action_ShowView ActionView = null;
			if (action != null) {
				if (action instanceof CB_Action_ShowView)
					ActionView = (CB_Action_ShowView) action;
				if (ActionView != null && ActionView.getView() == View) {
					aktActionView = ActionView;
					break;
				}
			}

		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}
