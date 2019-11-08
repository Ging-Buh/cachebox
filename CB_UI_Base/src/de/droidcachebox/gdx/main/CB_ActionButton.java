package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_ActionButton {
    private final AbstractAction action;
    private final boolean defaultAction;
    private GestureDirection gestureDirection;

    public CB_ActionButton(AbstractAction action, boolean defaultAction, GestureDirection gestureDirection) {
        this.action = action;
        this.defaultAction = defaultAction;
        this.gestureDirection = gestureDirection;
    }

    public CB_ActionButton(AbstractAction action, boolean defaultAction) {
        this(action, defaultAction, GestureDirection.None);
    }

    public AbstractAction getAction() {
        return action;
    }

    public boolean isDefault() {
        return defaultAction;
    }

    public boolean getEnabled() {
        if (action == null)
            return false;
        return action.getEnabled();
    }

    public GestureDirection getGestureDirection() {
        return gestureDirection;
    }

    public void setGestureDirection(GestureDirection gesture) {
        gestureDirection = gesture;
    }

    public Sprite getIcon() {
        return action.getIcon();
    }

    public enum GestureDirection {
        None, Right, Up, Left, Down
    }
}
