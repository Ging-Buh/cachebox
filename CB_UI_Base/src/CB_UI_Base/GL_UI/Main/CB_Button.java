package CB_UI_Base.GL_UI.Main;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.*;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.GestureHelp;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_Listener.GL_Input;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Main.CB_ActionButton.GestureDirection;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Math.Point;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

import static CB_UI_Base.GL_UI.Sprites.getSprite;
import static CB_UI_Base.Math.GL_UISizes.MainBtnSize;

/**
 * this is the class for the lower 5 buttons, the main buttons
 * it handles the menu shown by clicking/longclicking and the actions executed on clicking the menu point
 * <p>
 * a LongClicks menu points shows the "actions" (mButtonActions) that can be selected/executed for this button.
 * also shows a hint picture for the defined gestures, if gestures are enabled by configuration
 * <p>
 * the first click on the button executes the "default" action (if defined), or the last action depends on configuration "rememberLastAction".
 * the second click on the button shows the "context menu" of the previous selected action (aktActionView) and the "actions" of the LongClick menu
 * e.g. if the shown view is the spoilerView (last time clicked), the context menu shown is "reload spoiler" and "start pictureApp"
 * and then the  actions "description", "Waypoints", "Hints", "Spoiler", ...
 *
 * @author Longri
 */
public class CB_Button extends Button {

    private static Sprite mContextMenuSprite;
    private static Sprite mFilteredContextMenuSprite;
    private final ArrayList<CB_ActionButton> mButtonActions;
    private CB_Action_ShowView aktActionView = null;
    private GestureHelp help;
    private Point downPos = null;
    private boolean useDescriptiveCB_Buttons;
    private boolean rememberLastAction;
    private Image mButtonImage;
    private final OnClickListener onClickListener = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            // create the menu for the executed action aktActionView != null and view is visible
            // for CB_Action_ShowActivity the view is always null and stays invisible, no menu is shown. you have to long click.
            // in very early previous versions only the context menu is shown.
            // For another action, you always had to do another Long Click.
            if ((aktActionView != null)) {
                if (aktActionView.getView() != null && aktActionView.getView().isVisible()) {
                    // this view is the visible (==> second click) -> show contextmenu + longmenu
                    Menu compoundMenu = new Menu("compoundMenu");
                    if (aktActionView.hasContextMenu()) {
                        // first the context menu, if exists
                        Menu viewContextMenu = aktActionView.getContextMenu();
                        if (viewContextMenu != null) {
                            compoundMenu.addItems(viewContextMenu.getItems());
                            compoundMenu.addOnClickListeners(viewContextMenu.getOnItemClickListeners());
                            // add divider
                            compoundMenu.addDivider();
                            // add MoreMenu ! oh ups, what is this
                            compoundMenu.addMoreMenu(viewContextMenu.getMoreMenu(), viewContextMenu.getTextLeftMoreMenu(), viewContextMenu.getTextRightMoreMenu());
                        }
                    }
                    // then the Long Click menu
                    Menu LongClickMenu = getLongClickMenu();
                    if (LongClickMenu != null) {
                        compoundMenu.addItems(LongClickMenu.getItems());
                        compoundMenu.addOnClickListeners(LongClickMenu.getOnItemClickListeners());
                    }
                    // and show
                    if (compoundMenu.reorganizeIndexes() > 0) {
                        compoundMenu.Show();
                    }
                    else {
                        // what a problem on reorganizing
                    }
                    return true; // only show the menu
                }
            }

            boolean actionExecuted = false;
            if (aktActionView != null && rememberLastAction) {
                for (CB_ActionButton ba : mButtonActions) {
                    CB_Action action = ba.getAction();
                    if (aktActionView.getName().equals(action.getName())) {
                        action.Execute();
                        aktActionView = (CB_Action_ShowView) action;
                        setButton(aktActionView.getIcon(), aktActionView.getName());
                        actionExecuted = true;
                        break;
                    }
                }
            }
            else {
                // if the (last) action of this button is not visible,
                // the default action is executed
                for (CB_ActionButton ba : mButtonActions) {
                    if (ba.isDefault()) {
                        CB_Action action = ba.getAction();
                        if (action != null) {
                            action.Execute();
                            // ?
                            if (action instanceof CB_Action_ShowView) {
                                aktActionView = (CB_Action_ShowView) action;
                                setButton(aktActionView.getIcon(), aktActionView.getName());
                            }
                            actionExecuted = true;
                            break;
                        }
                    }
                }
            }

            // if no default action defined, perform a LongClick
            if (!actionExecuted) {
                OnClickListener listener = getOnLongClickListener();
                if (listener != null) {
                    return listener.onClick(v, x, y, pointer, button);
                }
            }

            return true;
        }
    };
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
                    action.Execute();
                    aktActionView = (CB_Action_ShowView) action;
                    setButton(aktActionView.getIcon(), aktActionView.getName());
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
    private boolean isFiltered;
    private boolean GestureIsOn = true;
    private boolean isDragged = false;

    public CB_Button(CB_RectF rec, boolean rememberLastAction, String Name) {
        super(rec, Name);
        useDescriptiveCB_Buttons = true;
        this.rememberLastAction = rememberLastAction;
        mButtonActions = new ArrayList<>();
        setOnClickListener(onClickListener);
        setOnLongClickListener(longClickListener);
        drawableNormal = new SpriteDrawable(getSprite("button"));
        drawablePressed = new SpriteDrawable(getSprite("btn-pressed"));
        drawableDisabled = null;
        drawableFocused = new SpriteDrawable(getSprite("btn-pressed"));
        isFiltered = false;
        vAlignment = Label.VAlignment.BOTTOM;
    }

    public CB_Button(CB_RectF rec, boolean rememberLastAction, String Name, ButtonSprites sprites) {
        super(rec, Name);
        useDescriptiveCB_Buttons = false;
        this.rememberLastAction = rememberLastAction;
        mButtonActions = new ArrayList<>();
        setOnClickListener(onClickListener);
        setOnLongClickListener(longClickListener);
        setButtonSprites(sprites);
        isFiltered = false;
        vAlignment = Label.VAlignment.BOTTOM;
    }

    public static void refreshContextMenuSprite() {
        mContextMenuSprite = null;
        mFilteredContextMenuSprite = null;
    }

    private void setButton(Sprite icon, String name) {
        if (useDescriptiveCB_Buttons) {
            mButtonImage.setDrawable(new SpriteDrawable(icon));
            if (name != null) {
                name = Translation.Get(name);
                setText(name.substring(0, Math.min(5, name.length())), Fonts.getSmall(), null);
            } else
                setText("", Fonts.getSmall(), null);
        }
    }

    public void addAction(CB_ActionButton Action) {
        if (useDescriptiveCB_Buttons) {
            if (mButtonImage == null) {
                mButtonImage = new Image(this.ScaleCenter(0.6f), "mButtonImage", false);
                mButtonImage.setClickable(false);
                mButtonImage.setDrawable(new SpriteDrawable(Action.getIcon()));
                addChild(mButtonImage);
                if (Action.getAction() instanceof CB_Action_ShowView) {
                    setButton(Action.getAction().getIcon(), Action.getAction().getName());
                }
            }
        }

        mButtonActions.add(Action);

        // disable Gesture ?
        if (!CB_UI_Base_Settings.GestureOn.getValue())
            Action.setGestureDirection(GestureDirection.None);

        GestureDirection gestureDirection = Action.getGestureDirection();
        if (gestureDirection != GestureDirection.None) {
            if (help == null) {
                float h = MainBtnSize.getHeight() * 2;
                help = new GestureHelp(new SizeF(h, h), "help");
            }

            NinePatch ninePatch = null;
            if (this.drawableNormal instanceof NinePatchDrawable) {
                ninePatch = ((NinePatchDrawable) this.drawableNormal).getPatch();
            } else if (this.drawableNormal instanceof SpriteDrawable) {
                int p = Sprites.patch;
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

    private Menu getLongClickMenu() {
        Menu cm = new Menu("Name");
        cm.addOnClickListener((v, x, y, pointer, button) -> {
            int mId = ((MenuItem) v).getMenuItemId();
            for (CB_ActionButton ba : mButtonActions) {
                if (ba.getAction().getId() == mId) {
                    CB_Action action = ba.getAction();
                    action.Execute();
                    if (action instanceof CB_Action_ShowView) {
                        aktActionView = (CB_Action_ShowView) action;
                        setButton(aktActionView.getIcon(), aktActionView.getName());
                    }

                    GL.that.closeToast();
                    break;
                }
            }
            return true;
        });

        for (CB_ActionButton ba : mButtonActions) {
            CB_Action action = ba.getAction();
            if (action == null)
                continue;
            MenuItem mi = cm.addItem(action.getId(), action.getName(), action.getNameExtension());
            // if (CB_UI_Base_Settings.GestureOn.getValue()) {            }
            if (ba.getGestureDirection() != GestureDirection.None) {
                String direction;
                switch (ba.getGestureDirection()) {
                    case Up:
                        direction = Translation.Get("up");
                        break;
                    case Down:
                        direction = Translation.Get("down");
                        break;
                    case Left:
                        direction = Translation.Get("left");
                        break;
                    default:
                        direction = Translation.Get("right");
                }
                mi.setTitle(mi.getTitle() + " (" + Translation.Get("wipe") + " " + direction  + ")");
            }
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
    public void performClick() {
        onClickListener.onClick(null, 0, 0, 0, 0);
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

            if (mContextMenuSprite == null || mFilteredContextMenuSprite == null) {
                float iconWidth = this.getWidth() / 5f;
                float iconHeight = this.getHeight() / 2.3f;
                float VersatzX = this.getHeight() / 20f;
                float VersatzY = this.getHeight() / 30f;

                mContextMenuSprite = new Sprite(Sprites.getSprite(IconName.cmIcon.name()));
                mContextMenuSprite.setBounds(this.getWidth() - iconWidth - VersatzX, VersatzY, iconWidth, iconHeight);

                mFilteredContextMenuSprite = new Sprite(Sprites.getSprite(IconName.MENUFILTERED.name()));
                mFilteredContextMenuSprite.setBounds(this.getWidth() - iconWidth - VersatzX, VersatzY, iconWidth, iconHeight);

            }

            if (!isFiltered && mContextMenuSprite != null)
                mContextMenuSprite.draw(batch);
            if (isFiltered && mFilteredContextMenuSprite != null)
                mFilteredContextMenuSprite.draw(batch);
        }
    }

    public void isFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
    }

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
            GL_Input.that.StopKinetic(x, y, pointer, true);
        isDragged = true;
        return true;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        boolean result = super.onTouchUp(x, y, pointer, button);

        if (!isDragged)
            return (GestureIsOn) ? result : true;
        int dx = x - downPos.x;
        int dy = y - downPos.y;
        GestureDirection direction;
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
                    action.Execute();
                    if (action instanceof CB_Action_ShowView) {
                        aktActionView = (CB_Action_ShowView) action;
                        setButton(aktActionView.getIcon(), aktActionView.getName());
                    }
                    break;
                }

            }
        }
        isDragged = false;
        return true;
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
                    setButton(aktActionView.getIcon(), aktActionView.getName());
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
