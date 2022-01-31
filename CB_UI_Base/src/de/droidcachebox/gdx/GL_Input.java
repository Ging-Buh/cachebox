package de.droidcachebox.gdx;

import static java.lang.Character.CONTROL;
import static de.droidcachebox.gdx.GL.FRAME_RATE_ACTION;
import static de.droidcachebox.gdx.GL.FRAME_RATE_FAST_ACTION;
import static de.droidcachebox.settings.AllSettings.longClickTime;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import java.util.Collections;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import de.droidcachebox.KeyCodes;
import de.droidcachebox.Platform;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.SelectionMarker;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.Point;

public class GL_Input implements InputProcessor {
    private static final int MAX_KINETIC_SCROLL_DISTANCE = 100;
    private static final boolean TOUCH_DEBUG = true;
    public static GL_Input that;
    private int MouseX = 0;
    private int MouseY = 0;
    private boolean isTouchDown;
    private boolean touchDraggedActive;
    private Point touchDraggedCorrect = new Point(0, 0);
    private SortedMap<Integer, TouchDownPointer> touchDownPos = Collections.synchronizedSortedMap((new TreeMap<>()));
    private long mLongClickTime;
    private Timer longClickTimer;
    private long lastClickTime = 0;
    private Point lastClickPoint = null;

    public GL_Input() {
        isTouchDown = false;
        touchDraggedActive = false;
        mLongClickTime = longClickTime.getValue();
        that = this;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        // InputProcessor Implementation touchDown
        return onTouchDownBase(x, y, pointer, button);
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        // InputProcessor Implementation touchDragged
        return onTouchDraggedBase(x, y, pointer);
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        // InputProcessor Implementation mouseMoved
        MouseX = x;
        MouseY = y;
        return onTouchDraggedBase(x, y, -1);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // InputProcessor Implementation scrolled
        float scrollSizeX = (UiSizes.getInstance().getClickToleranz() + 10) * amountX;
        float scrollSizeY = (UiSizes.getInstance().getClickToleranz() + 10) * amountY;
        int Pointer = (scrollSizeX > 0) ? GL_View_Base.MOUSE_WHEEL_POINTER_UP : GL_View_Base.MOUSE_WHEEL_POINTER_DOWN;
        onTouchDownBase(MouseX, MouseY, Pointer, -1);
        onTouchDraggedBase((int) (MouseX - scrollSizeX), (int) (MouseY - scrollSizeY), Pointer);
        onTouchUpBase((int) (MouseX - scrollSizeX), (int) (MouseY - scrollSizeY), Pointer, -1);
        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        // InputProcessor Implementation touchUp
        return onTouchUpBase(x, y, pointer, button);
    }

    @Override
    public boolean keyTyped(char character) {
        // InputProcessor Implementation keyTyped

        if (character == KeyCodes.KEYCODE_BACK) {
            return GL.that.closeCurrentDialogOrActivity();
        }

        if (Character.getType(character) == CONTROL) {
            //check if coursor up/down/left/rigt clicked
            if (Character.getNumericValue(character) == -1) {
                if (!(character == EditTextField.BACKSPACE //
                        || character == EditTextField.DELETE //
                        || character == EditTextField.ENTER_ANDROID //
                        || character == EditTextField.ENTER_DESKTOP //
                        || character == EditTextField.TAB) //
                ) {
                    return true;
                }
            }
        }

        if (GL.that.getFocusedEditTextField() != null) {
            GL.that.getFocusedEditTextField().keyTyped(character);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int value) {
        // InputProcessor Implementation keyUp
        if (value == Input.Keys.BACK) {
            if (!GL.that.closeCurrentDialogOrActivity()) {
                Platform.quit();
            }
            return true;
        }
        if (GL.that.getFocusedEditTextField() != null) {
            // return GL.that.getFocusedEditTextField().keyUp(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int value) {
        // InputProcessor Implementation keyDown
        if (GL.that.getFocusedEditTextField() != null) {
            GL.that.getFocusedEditTextField().keyDown(value);
            return true;
        }
        return false;
    }

    // TouchEreignisse die von der View gesendet werden
    // hier wird entschieden, wann TouchDown, TouchDragged, TouchUp und Clicked, LongClicked Ereignisse gesendet werden müssen
    public boolean onTouchDownBase(int x, int y, int pointer, int button) {
        GL.that.resetAmbiantMode();

        isTouchDown = true;
        touchDraggedActive = false;
        touchDraggedCorrect = new Point(0, 0);

        GL_View_Base view = GL.that.touchActiveView(x, y, pointer, button);
        if (view == null)
            return false;

        // wenn dieser TouchDown ausserhalb einer TextView war, dann reset TextFieldFocus
        if (GL.that.getFocusedEditTextField() != null) {
            if (!(view instanceof EditTextField) && !(view instanceof SelectionMarker) && !(view instanceof CB_Button) && GL.that.popUpIsHidden()) {
                GL.that.setFocusedEditTextField(null);
            }
        }

        if (touchDownPos.containsKey(pointer)) {
            // für diesen Pointer ist aktuell ein kinetisches Pan aktiv -> dieses abbrechen
            StopKinetic(x, y, pointer, false);
        }

        // down Position merken
        touchDownPos.put(pointer, new TouchDownPointer(pointer, new Point(x, y), view));

        // chk if LongClickable
        if (view.isLongClickable()) {
            startLongClickTimer(pointer, x, y);
        } else {
            cancelLongClickTimer();
        }

        GL.that.renderOnce(true);

        return true;
    }

    public boolean onTouchDraggedBase(int x, int y, int pointer) {

        CB_View_Base testingView = GL.that.getActiveView();

        if (!touchDownPos.containsKey(pointer)) {
            // für diesen Pointer ist kein touchDownPos gespeichert -> dürfte nicht passieren!!!
            return false;
        }

        TouchDownPointer first = touchDownPos.get(pointer);

        try {
            Point akt = new Point(x, y);
            if (touchDraggedActive || (distance(akt, first.point) > first.view.getClickTolerance())) {
                if (pointer != GL_View_Base.MOUSE_WHEEL_POINTER_UP && pointer != GL_View_Base.MOUSE_WHEEL_POINTER_DOWN) {
                    // Nachdem die ClickToleranz überschritten wurde
                    // wird jetzt hier die Verschiebung gemerkt.
                    // Diese wird dann immer von den Positionen abgezogen,
                    // damit der erste Sprung bei der Verschiebung
                    // nachem die Toleranz überschriten wurde
                    // nicht mehr auftritt.
                    if (!touchDraggedActive) {
                        touchDraggedCorrect = new Point(x - first.point.x, y - first.point.y);
                    }
                    x -= touchDraggedCorrect.x;
                    y -= touchDraggedCorrect.y;
                }

                // merken, dass das Dragging aktiviert wurde, bis der Finger wieder losgelassen wird
                touchDraggedActive = true;
                // zu weit verschoben -> Long-Click detection stoppen
                cancelLongClickTimer();
                // touchDragged Event an das View, das den onTouchDown bekommen hat
                boolean behandelt = first.view.touchDragged(x - (int) first.view.thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.thisWorldRec.getY(), pointer, false);
                if (TOUCH_DEBUG) {
                    // Log.debug(log, "GL_Listener => onTouchDraggedBase : " + behandelt);
                    if (!behandelt && first.view.getParent() != null) {
                        // Wenn der Parent eine ScrollBox hat -> Scroll-Events dahin weiterleiten
                        first.view.getParent().touchDragged(x - (int) first.view.getParent().thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.getParent().thisWorldRec.getY(), pointer, false);
                    }
                }
                if (touchDownPos.size() == 1) {
                    if (first.kineticPan == null)
                        first.kineticPan = new KineticPan();
                    first.kineticPan.setLast(System.currentTimeMillis(), x, y);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean onTouchUpBase(int x, int y, int pointer, int button) {
        isTouchDown = false;
        cancelLongClickTimer();

        CB_View_Base testingView = GL.that.getActiveView();

        if (!touchDownPos.containsKey(pointer)) {
            // für diesen Pointer ist kein touchDownPos gespeichert -> dürfte nicht passieren!!!
            return false;
        }

        TouchDownPointer first = touchDownPos.get(pointer);

        try {
            Point akt = new Point(x, y);
            if (distance(akt, first.point) < first.view.getClickTolerance()) {
                // Finger wurde losgelassen ohne viel Bewegung
                if (first.view.isClickable()) {
                    // Testen, ob dies ein Doppelklick ist
                    long mDoubleClickTime = 500;
                    if (first.view.isDoubleClickable() && (System.currentTimeMillis() < lastClickTime + mDoubleClickTime) && (lastClickPoint != null) && (distance(akt, lastClickPoint) < first.view.getClickTolerance())) {
                        boolean handled = first.view.doubleClick(x - (int) first.view.thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.thisWorldRec.getY(), pointer, button);
                        if (handled)
                            Platform.vibrate();

                        lastClickTime = 0;
                        lastClickPoint = null;
                    } else {
                        // normaler Click
                        boolean handled = first.view.click(x - (int) first.view.thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.thisWorldRec.getY(), pointer, button);
                        if (handled)
                            Platform.vibrate();

                        lastClickTime = System.currentTimeMillis();
                        lastClickPoint = akt;
                    }
                }
            } else {
                x -= touchDraggedCorrect.x;
                y -= touchDraggedCorrect.y;
            }
        } catch (Exception ignored) {
        }

        try {
            if (first.kineticPan != null) {
                first.kineticPan.start();
                first.startKinetic(x - (int) first.view.thisWorldRec.getX(), (int) testingView.getHeight() - y - (int) first.view.thisWorldRec.getY());
            } else {
                // onTouchUp immer auslösen
                first.view.touchUp(x, (int) testingView.getHeight() - y, pointer, button);
                touchDownPos.remove(pointer);
            }
        } catch (Exception ignored) {
        }

        return true;
    }

    Point getTouchDownPos() {
        if (isTouchDown) {
            if (touchDownPos.size() > 0)
                return touchDownPos.get(0).point;
            else return null;
        } else {
            return null;
        }
    }

    public void StopKinetic(int x, int y, int pointer, boolean forceTouchUp) {
        TouchDownPointer first = touchDownPos.get(pointer);
        if (first != null) {
            first.stopKinetic();
            first.kineticPan = null;
            if (forceTouchUp)
                first.view.touchUp(x, y, pointer, 0);
        }
    }

    private void startLongClickTimer(final int pointer, final int x, final int y) {
        cancelLongClickTimer();

        longClickTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!touchDownPos.containsKey(pointer))
                    return;
                // für diesen Pointer ist kein touchDownPos gespeichert ->
                // dürfte nicht passieren!!!
                TouchDownPointer first = touchDownPos.get(pointer);
                Point akt = new Point(x, y);
                if (distance(akt, first.point) < first.view.getClickTolerance()) {
                    if (first.view.isLongClickable()) {
                        boolean handled = first.view.longClick(x - (int) first.view.thisWorldRec.getX(), (int) GL.that.getChild().getHeight() - y - (int) first.view.thisWorldRec.getY(), pointer, 0);
                        // Log.debug(log, "GL_Listener => onLongClick : " + first.view.getName());
                        // für diesen TouchDownn darf kein normaler Click mehr ausgeführt werden
                        touchDownPos.remove(pointer);
                        // onTouchUp nach Long-Click direkt auslösen
                        first.view.touchUp(x, (int) GL.that.getChild().getHeight() - y, pointer, 0);
                        // Log.debug(log, "GL_Listener => onTouchUpBase : " + first.view.getName());
                        if (handled)
                            Platform.vibrate();
                    }
                }
            }
        };
        longClickTimer.schedule(task, mLongClickTime);
    }

    private void cancelLongClickTimer() {
        if (longClickTimer != null) {
            longClickTimer.cancel();
            longClickTimer = null;
        }
    }

    // Abstand zweier Punkte
    private int distance(Point p1, Point p2) {
        return (int) Math.round(Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2)));
    }

    private int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.round(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    public boolean getIsTouchDown() {
        return isTouchDown;
    }

    public class TouchDownPointer {
        private final int pointer;
        private final GL_View_Base view;
        public Point point;
        private KineticPan kineticPan;
        private Timer timer;

        TouchDownPointer(int pointer, Point point, GL_View_Base view) {
            this.pointer = pointer;
            this.point = point;
            this.view = view;
            this.kineticPan = null;
        }

        void startKinetic(final int x, final int y) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (kineticPan != null) {
                        Point pan = kineticPan.getAktPan();
                        try {
                            if (kineticPan.fertig) {
                                // Log.debug(log, "KineticPan fertig");
                                view.touchUp(x - pan.x, y - pan.y, pointer, 0);
                                touchDownPos.remove(pointer);
                                kineticPan = null;
                                this.cancel();
                                timer = null;
                            }
                        } catch (Exception e) {
                            touchDownPos.remove(pointer);
                            kineticPan = null;
                            this.cancel();
                            timer = null;
                        }
                        view.touchDragged(x - pan.x, y - pan.y, pointer, true);
                    }
                }
            }, 0, FRAME_RATE_FAST_ACTION);
        }

        void stopKinetic() {
            if (timer != null) {
                timer.cancel();
                timer = null;
                kineticPan = null;
            }
        }
    }

    protected class KineticPan {
        // benutze den Abstand der letzten 5 Positionsänderungen
        final int anzPoints = 6;
        private final int[] x = new int[anzPoints];
        private final int[] y = new int[anzPoints];
        private final long[] ts = new long[anzPoints];
        int anzPointsUsed;
        private boolean started;
        private boolean fertig;
        private int diffX;
        private int diffY;
        private long diffTs;
        private long startTs;
        private long endTs;
        private int lastX = 0;
        private int lastY = 0;

        public KineticPan() {
            fertig = false;
            started = false;
            diffX = 0;
            diffY = 0;
            for (int i = 0; i < anzPoints; i++) {
                x[i] = 0;
                y[i] = 0;
                ts[i] = 0;
            }
            anzPointsUsed = 0;
        }

        void setLast(long aktTs, int aktX, int aktY) {
            if ((anzPointsUsed > 0) && (ts[0] < aktTs - 500)) {
                // wenn seit der letzten Verschiebung mehr Zeit Vergangen ist -> bisherige gemerkte Verschiebungen löschen
                anzPointsUsed = 0;
                started = false;
                return;
            }

            anzPointsUsed++;
            if (TOUCH_DEBUG)
                // Log.debug(log, "AnzUsedPoints: " + anzPointsUsed);
                if (anzPointsUsed > anzPoints)
                    anzPointsUsed = anzPoints;
            for (int i = anzPoints - 2; i >= 0; i--) {
                x[i + 1] = x[i];
                y[i + 1] = y[i];
                ts[i + 1] = ts[i];
            }
            x[0] = aktX;
            y[0] = aktY;
            ts[0] = aktTs;

            for (int i = 1; i < anzPoints; i++) {
                if (x[i] == 0)
                    x[i] = x[i - 1];
                if (y[i] == 0)
                    y[i] = y[i - 1];
                if (ts[i] == 0)
                    ts[i] = ts[i - 1];
            }
            diffX = x[anzPointsUsed - 1] - aktX;
            diffY = aktY - y[anzPointsUsed - 1];
            diffTs = aktTs - ts[anzPointsUsed - 1];

            if (diffTs > 0) {
                diffX = (int) ((float) diffX / FRAME_RATE_ACTION * diffTs);
                diffY = (int) ((float) diffY / FRAME_RATE_ACTION * diffTs);
            }
            // if (TOUCH_DEBUG)
            // Log.debug(log, "diffx = " + diffX + " - diffy = " + diffY);

            // debugString = x[2] + " - " + x[1] + " - " + x[0];
        }

        public boolean getStarted() {
            return started;
        }

        public void start() {
            anzPointsUsed = Math.max(anzPointsUsed, 1);
            if (ts[0] < System.currentTimeMillis() - 200) {
                // kinematisches Scrollen nur, wenn seit der letzten Verschiebung kaum Zeit vergangen ist
                fertig = true;
                return;
            }
            startTs = System.currentTimeMillis();
            int abstand = (int) Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

            endTs = startTs + 1000 + abstand * 15 / anzPointsUsed;
            // if (endTs > startTs + 6000) endTs = startTs + 6000; // max. Zeit festlegen
            if (TOUCH_DEBUG)
                // Log.debug(log, "endTs - startTs: " + String.valueOf(endTs - startTs));
                // endTs = startTs + 5000;
                started = true;
        }

        Point getAktPan() {
            anzPointsUsed = Math.max(anzPointsUsed, 1);
            Point result = new Point(0, 0);

            long aktTs = System.currentTimeMillis();
            float faktor = (float) (aktTs - startTs) / (float) (endTs - startTs);
            // Log.debug(log, "Faktor: " + faktor);
            faktor = com.badlogic.gdx.math.Interpolation.pow5Out.apply(faktor);
            // faktor = com.badlogic.gdx.math.Interpolation.pow5Out.apply(faktor);
            // Log.debug(log, "Faktor2: " + faktor);
            if (faktor >= 1) {
                fertig = true;
                faktor = 1;
            }

            result.x = (int) ((float) diffX / anzPointsUsed * (1 - faktor)) + lastX;
            result.y = (int) ((float) diffY / anzPointsUsed * (1 - faktor)) + lastY;

            if ((result.x == lastX) && (result.y == lastY)) {
                // wenn keine Nennenswerten Änderungen mehr gemacht werden dann einfach auf fertig schalten
                fertig = true;
                faktor = 1;
                result.x = (int) ((float) diffX / anzPointsUsed * (1 - faktor)) + lastX;
                result.y = (int) ((float) diffY / anzPointsUsed * (1 - faktor)) + lastY;
            }
            double abstand = distance(lastX, lastY, result.x, result.y);
            if (abstand > MAX_KINETIC_SCROLL_DISTANCE) {
                double fkt = MAX_KINETIC_SCROLL_DISTANCE / abstand;
                result.x = (int) ((result.x - lastX) * fkt + lastX);
                result.y = (int) ((result.y - lastY) * fkt + lastY);
            }

            lastX = result.x;
            lastY = result.y;
            return result;
        }
    }

}
