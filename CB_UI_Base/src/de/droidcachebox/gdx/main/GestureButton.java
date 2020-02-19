package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.GestureHelp;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.main.CB_ActionButton.GestureDirection;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;

import static de.droidcachebox.gdx.Sprites.getSprite;
import static de.droidcachebox.gdx.math.GL_UISizes.mainButtonSize;

/**
 * this is the class for the lower 5 buttons, the main buttons
 * it handles the menu shown by clicking/longclicking and the actions executed on clicking the menu point
 * <p>
 * a LongClicks menu points shows the "actions" (cb_actionButtons) that can be selected/executed for this button.
 * also shows a hint picture for the defined gestures, if gestures are enabled by configuration
 * <p>
 * the first click on the button executes the "default" action (if defined), or the last action depends on configuration "rememberLastAction".
 * the second click on the button shows the "context menu" of the previous selected action (aktActionView) and the "actions" of the LongClick menu
 * e.g. if the shown view is the spoilerView (last time clicked), the context menu shown is "reload spoiler" and "start pictureApp"
 * and then the  actions "description", "Waypoints", "Hints", "Spoiler", ...
 *
 * @author Longri
 */
public class GestureButton extends CB_Button {

    private static Sprite mContextMenuSprite;
    private static Sprite mFilteredContextMenuSprite;
    private final ArrayList<CB_ActionButton> cb_actionButtons;
    private AbstractShowAction aktActionView = null;
    private GestureHelp help;
    private Point downPos = null;
    private boolean useDescriptiveCB_Buttons;
    private boolean rememberLastAction;
    private Image mButtonImage;
    private final OnClickListener onClickListener = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
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
                        compoundMenu.setTitle(viewContextMenu.getTitle());
                        if (viewContextMenu != null) {
                            compoundMenu.addItems(viewContextMenu.getItems());
                            // compoundMenu.addOnItemClickListeners(viewContextMenu.getOnItemClickListeners());
                            // add divider
                            compoundMenu.addDivider();
                            // add MoreMenu
                            compoundMenu.addMoreMenu(viewContextMenu.getMoreMenu(), viewContextMenu.getTextLeftMoreMenu(), viewContextMenu.getTextRightMoreMenu());
                        }
                    }
                    // then the Long Click menu
                    Menu LongClickMenu = getLongClickMenu();
                    compoundMenu.addItems(LongClickMenu.getItems());
                    // compoundMenu.addOnItemClickListeners(LongClickMenu.getOnItemClickListeners());
                    // and show
                    if (compoundMenu.reorganizeIndexes() > 0) {
                        compoundMenu.show();
                    }
                    else {
                        // what a problem on reorganizing
                        Log.err("GestureButton","Error reorganizing menu index");
                    }
                    return true; // only show the menu
                }
            }

            boolean actionExecuted = false;
            if (aktActionView != null && rememberLastAction) {
                for (CB_ActionButton ba : cb_actionButtons) {
                    AbstractAction action = ba.getAction();
                    if (aktActionView.getTitleTranlationId().equals(action.getTitleTranlationId())) {
                        action.execute();
                        aktActionView = (AbstractShowAction) action;
                        setButton(aktActionView.getIcon(), aktActionView.getTitleTranlationId());
                        actionExecuted = true;
                        break;
                    }
                }
            }
            else {
                // if the (last) action of this button is not visible,
                // the default action is executed
                for (CB_ActionButton ba : cb_actionButtons) {
                    if (ba.isDefault()) {
                        AbstractAction action = ba.getAction();
                        if (action != null) {
                            action.execute();
                            // ?
                            if (action instanceof AbstractShowAction) {
                                aktActionView = (AbstractShowAction) action;
                                setButton(aktActionView.getIcon(), aktActionView.getTitleTranlationId());
                            }
                            actionExecuted = true;
                            break;
                        }
                    }
                }
            }

            // if no default action defined, show LongClickMenu
            if (!actionExecuted) {
                Menu compoundMenu = new Menu("compoundMenu");
                // then the Long Click menu
                Menu LongClickMenu = getLongClickMenu();
                if (LongClickMenu != null) {
                    compoundMenu.addItems(LongClickMenu.getItems(),true);
                }
                // and show
                if (compoundMenu.reorganizeIndexes() > 0) {
                    compoundMenu.show();
                }
                else {
                    // what a problem on reorganizing
                }
                return true; // only show the menu

            }

            return true;
        }
    };

    private final OnClickListener longClickListener = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
            // MessageBox.Show("Button " + Me.getName() + " recivet a LongClick Event");
            // Wenn diesem Button mehrere Actions zugeordnet sind dann wird nach einem Lang-Click ein Menü angezeigt aus dem eine dieser
            // Actions gewählt werden kann

            if (cb_actionButtons.size() > 1) {
                getLongClickMenu().show();
            } else if (cb_actionButtons.size() == 1) {
                // nur eine Action dem Button zugeordnet -> diese Action gleich ausführen
                CB_ActionButton ba = cb_actionButtons.get(0);
                AbstractAction action = ba.getAction();
                if (action != null) {
                    action.execute();
                    aktActionView = (AbstractShowAction) action;
                    setButton(aktActionView.getIcon(), aktActionView.getTitleTranlationId());
                }
            }

            // Show Gester Help

            if (help != null) {
                CB_RectF rec = GestureButton.this.thisWorldRec;
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

    public GestureButton(CB_RectF rec, boolean rememberLastAction, String Name) {
        super(rec, Name);
        useDescriptiveCB_Buttons = true;
        this.rememberLastAction = rememberLastAction;
        cb_actionButtons = new ArrayList<>();
        setClickHandler(onClickListener);
        // setOnLongClickListener(longClickListener);
        drawableNormal = new SpriteDrawable(getSprite("button"));
        drawablePressed = new SpriteDrawable(getSprite("btn-pressed"));
        drawableDisabled = null;
        drawableFocused = new SpriteDrawable(getSprite("btn-pressed"));
        isFiltered = false;
        vAlignment = CB_Label.VAlignment.BOTTOM;
    }

    public GestureButton(CB_RectF rec, boolean rememberLastAction, String Name, ButtonSprites sprites) {
        super(rec, Name);
        useDescriptiveCB_Buttons = false;
        this.rememberLastAction = rememberLastAction;
        cb_actionButtons = new ArrayList<>();
        setClickHandler(onClickListener);
        // setOnLongClickListener(longClickListener);
        setButtonSprites(sprites);
        isFiltered = false;
        vAlignment = CB_Label.VAlignment.BOTTOM;
    }

    public static void refreshContextMenuSprite() {
        mContextMenuSprite = null;
        mFilteredContextMenuSprite = null;
    }

    private void setButton(Sprite icon, String name) {
        if (useDescriptiveCB_Buttons) {
            mButtonImage.setDrawable(new SpriteDrawable(icon));
            if (name != null) {
                name = Translation.get(name);
                setText(name.substring(0, Math.min(5, name.length())), Fonts.getSmall(), null);
            } else
                setText("", Fonts.getSmall(), null);
        }
    }

    public void addAction(AbstractAction action, boolean defaultAction) {
        CB_ActionButton Action = new CB_ActionButton(action, defaultAction);
        addAction(Action);
    }

    public void addAction(AbstractAction action, boolean defaultAction, GestureDirection gestureDirection) {
        CB_ActionButton Action = new CB_ActionButton(action, defaultAction, gestureDirection);
        addAction(Action);
    }

    private void addAction(CB_ActionButton Action) {
        if (useDescriptiveCB_Buttons) {
            if (mButtonImage == null) {
                mButtonImage = new Image(this.scaleCenter(0.6f), "mButtonImage", false);
                mButtonImage.setClickable(false);
                mButtonImage.setDrawable(new SpriteDrawable(Action.getIcon()));
                addChild(mButtonImage);
                if (Action.getAction() instanceof AbstractShowAction) {
                    setButton(Action.getAction().getIcon(), Action.getAction().getTitleTranlationId());
                }
            }
        }

        cb_actionButtons.add(Action);

        // disable Gesture ?
        if (!CB_UI_Base_Settings.gestureOn.getValue())
            Action.setGestureDirection(GestureDirection.None);

        GestureDirection gestureDirection = Action.getGestureDirection();
        if (gestureDirection != GestureDirection.None) {
            if (help == null) {
                float h = mainButtonSize.getHeight() * 2;
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
        for (CB_ActionButton cb_actionButton : cb_actionButtons) {
            AbstractAction action = cb_actionButton.getAction();
            if (action == null)
                continue;
            MenuItem mi = cm.addMenuItem(action.getTitleTranlationId(), action.getTitleExtension(), null, (v, x, y, pointer, button)->{
                cm.close();
                MenuItem clickedItem = (MenuItem) v;
                AbstractAction btnAction = (AbstractAction) clickedItem.getData();
                btnAction.execute();
                if (btnAction instanceof AbstractShowAction) {
                    aktActionView = (AbstractShowAction) btnAction;
                    setButton(aktActionView.getIcon(), aktActionView.getTitleTranlationId());
                }
                GL.that.closeToast();
                return true;
            });
            mi.setData(action);
            if (cb_actionButton.getGestureDirection() != GestureDirection.None) {
                String direction;
                switch (cb_actionButton.getGestureDirection()) {
                    case Up:
                        direction = Translation.get("up");
                        break;
                    case Down:
                        direction = Translation.get("down");
                        break;
                    case Left:
                        direction = Translation.get("left");
                        break;
                    default:
                        direction = Translation.get("right");
                }
                mi.setTitle(mi.getTitle() + " (" + Translation.get("wipe") + " " + direction  + ")");
            }
            mi.setDisabled(action.getEnabled() && aktActionView != action);
            // mi.setVisible(aktActionView != action); // there will be a hole
            mi.setCheckable(action.getIsCheckable());
            mi.setChecked(action.getIsChecked());
            Sprite icon = action.getIcon();
            if (icon != null)
                mi.setIcon(new SpriteDrawable(action.getIcon()));
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
        for (CB_ActionButton ba : cb_actionButtons) {
            if (ba.getGestureDirection() == direction) {
                AbstractAction action = ba.getAction();
                if (action != null) {
                    action.execute();
                    if (action instanceof AbstractShowAction) {
                        aktActionView = (AbstractShowAction) action;
                        setButton(aktActionView.getIcon(), aktActionView.getTitleTranlationId());
                    }
                    break;
                }

            }
        }
        isDragged = false;
        return true;
    }

    public void setCurrentView(CB_View_Base View) {
        for (CB_ActionButton ba : cb_actionButtons) {
            AbstractAction action = ba.getAction();
            AbstractShowAction ActionView = null;
            if (action != null) {
                if (action instanceof AbstractShowAction)
                    ActionView = (AbstractShowAction) action;
                if (ActionView != null && ActionView.getView() == View) {
                    aktActionView = ActionView;
                    setButton(aktActionView.getIcon(), aktActionView.getTitleTranlationId());
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
