/*
 * Copyright (C) 2015 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.gdx.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.LinkedHashMap;

import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.ImageLoader;
import de.droidcachebox.gdx.controls.ZoomButtons;
import de.droidcachebox.gdx.graphics.ColorDrawable;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.graphics.KineticPan;
import de.droidcachebox.gdx.graphics.KineticZoom;
import de.droidcachebox.gdx.main.MainViewBase;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.PointL;
import de.droidcachebox.utils.log.Log;

/**
 * @author Longri
 */
public class ImageActivity extends ActivityBase {

    private static final String sClass = "ImageActivity";
    private static final int ZOOM_TIME = 1000;
    private static final int MAX_MAP_ZOOM = 22;
    private final PointL screenCenterW = new PointL(0, 0);
    private final PointL screenCenterT = new PointL(0, 0);
    private final LinkedHashMap<Integer, Point> fingerDown = new LinkedHashMap<>();
    protected InputState inputState = InputState.Idle;
    private Image img;
    private ZoomButtons zoomBtn;
    private KineticZoom kineticZoom = null;
    private KineticPan kineticPan = null;
    private OrthographicCamera camera;
    private int aktZoom;
    private int imageHeading = 0;
    private int mapIntHeight;
    private float lastDynamicZoom = -1;
    private Image deleteImage;

    public ImageActivity(Image selectionImage) {
        super("ImageActivity");

        float wh = 30 * UiSizes.getInstance().getScale();
        deleteImage = new Image(getWidth() - wh, getHeight() - wh, wh, wh, "", false);
        deleteImage.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.closeIcon.name())));
        addChild(deleteImage);
        deleteImage.setClickHandler((view, x, y, pointer, button) -> {
            ImageActivity.this.finish();
            return true;
        });
        setClickable(true);

        setBackground(new ColorDrawable(new HSV_Color(Color.BLACK)));
        img = selectionImage;
        mapIntHeight = (int) getHeight();
        screenCenterW.set((long) (getHalfWidth()), (long) -(getHalfHeight())); //-(img.getImageLoader().getSpriteHeight() / 2);

        screenCenterT.set(0, 0);

        // initial Zoom Buttons
        zoomBtn = new ZoomButtons(GL_UISizes.zoomBtn, this, "ZoomButtons");
        zoomBtn.setX(getWidth() - (zoomBtn.getWidth() + UiSizes.getInstance().getMargin()));
        zoomBtn.setMinimumFadeValue(0.3f);
        zoomBtn.setMaxZoom(MAX_MAP_ZOOM);
        zoomBtn.setMinZoom(0);
        zoomBtn.setZoom(0);
        zoomBtn.setOnClickListenerDown((view, x, y, pointer, button) -> {
            kineticZoom = new KineticZoom(camera.zoom, getPosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System.currentTimeMillis() + ZOOM_TIME);
            GL.that.addRenderView(ImageActivity.this, GL.FRAME_RATE_ACTION);
            GL.that.renderOnce();
            GL.that.renderOnce();
            return true;
        });
        zoomBtn.setOnClickListenerUp((view, x, y, pointer, button) -> {
            kineticZoom = new KineticZoom(camera.zoom, getPosFactor(zoomBtn.getZoom()), System.currentTimeMillis(), System.currentTimeMillis() + ZOOM_TIME);
            GL.that.addRenderView(ImageActivity.this, GL.FRAME_RATE_ACTION);
            GL.that.renderOnce();
            GL.that.renderOnce();
            return true;
        });
        addChild(zoomBtn);

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        aktZoom = zoomBtn.getZoom();
        camera.zoom = getPosFactor(aktZoom);
        camera.position.set(0, 0, 0);

    }

    private static long getPosFactor(float zoom) {
        return (long) Math.pow(2.0, MAX_MAP_ZOOM - zoom);
    }

    @Override
    public void onShow() {
        GL.that.addRenderView(this, GL.FRAME_RATE_IDLE);
    }

    @Override
    public void onHide() {
        GL.that.removeRenderView(this);
    }

    @Override
    public void render(Batch batch) {

        imageHeading = Gdx.input.getRotation();
        float x = Gdx.input.getAccelerometerX();
        float y = Gdx.input.getAccelerometerY();
        float z = Gdx.input.getAccelerometerZ();

        //  (|Gpz| < 0.5g) AND (Gpx > 0.5g) AND (|Gpy| < 0.4g): Change orientation to Left
        //  (|Gpz| < 0.5g) AND (Gpx < -0.5g) AND (|Gpy| < 0.4g): Change orientation to Right
        //  (|Gpz| < 0.5g) AND (Gpy > 0.5g) AND (|Gpx| < 0.4g): Change orientation to Bottom
        //  (|Gpz| < 0.5g) AND (Gpy < -0.5g) AND (|Gpx| < 0.4g): Change orientation to Top.

        if (z < 5 && x > 5 && y < 4) {
            imageHeading = -90;
            //	    Log.debug(log, "LEFT");
        } else if (z < 5 && x < -5 && y < 4) {
            imageHeading = 90;
            //	    Log.debug(log, "RIGHT");
            //} else if (z < 5 && y > 5 && x < 4) {
            //	    Log.debug(log, "BOTTOM");
        } else {
            imageHeading = 0;
            //	    Log.debug(log, "TOP");
        }

        // do not rotate until there is a button
        imageHeading = 0;

        super.render(batch);
        boolean reduceFps = ((kineticZoom != null) || ((kineticPan != null) && (kineticPan.getStarted())));
        if (kineticZoom != null) {
            camera.zoom = kineticZoom.getAktZoom();
            // float tmpZoom = mapTileLoader.convertCameraZommToFloat(camera);
            // aktZoom = (int) tmpZoom;

            int zoom = MAX_MAP_ZOOM;
            float tmpZoom = camera.zoom;
            float faktor = 1.5f;

            while (tmpZoom > faktor) {
                tmpZoom /= 2;
                zoom--;
            }
            aktZoom = zoom;

            if (kineticZoom.getFertig()) {
                GL.that.removeRenderView(this);
                kineticZoom = null;
            } else
                reduceFps = false;

        }

        if ((kineticPan != null) && (kineticPan.getStarted())) {
            if (kineticPan.getFertig()) {
                kineticPan = null;
            } else
                reduceFps = false;
        }

        if (reduceFps) {
            GL.that.removeRenderView(this);
        }
        Matrix4 mat = batch.getProjectionMatrix();
        renderImage(batch);
        batch.setProjectionMatrix(mat);
    }

    private void renderImage(Batch batch) {
        //	batch.disableBlending();

        float faktor = camera.zoom;
        float dx = thisWorldRec.getCenterPosX() - MainViewBase.mainView.getCenterPosX();
        float dy = thisWorldRec.getCenterPosY() - MainViewBase.mainView.getCenterPosY();
        camera.position.set(0, 0, 0);
        float dxr;
        float dyr;

        camera.up.x = 0;
        camera.up.y = 1;
        camera.up.z = 0;
        camera.rotate(-imageHeading, 0, 0, 1);
        double angle = imageHeading * MathUtils.DEG_RAD;
        dxr = (float) (Math.cos(angle) * dx + Math.sin(angle) * dy);
        dyr = (float) (-Math.sin(angle) * dx + Math.cos(angle) * dy);

        camera.translate(-dxr * faktor, -dyr * faktor, 0);
        camera.update();
        Matrix4 mat = camera.combined;
        batch.setProjectionMatrix(mat);

        Drawable drw = img.getDrawable();

        if (drw != null) {

            float drawwidth = getWidth();
            float drawHeight = getHeight();

            ImageLoader imageLoader = img.getImageLoader();

            if (imageLoader.getSpriteWidth() > 0 && imageLoader.getSpriteHeight() > 0) {
                float proportionWidth = getWidth() / imageLoader.getSpriteWidth();
                float proportionHeight = getHeight() / imageLoader.getSpriteHeight();

                float proportion = Math.min(proportionWidth, proportionHeight);

                drawwidth = imageLoader.getSpriteWidth() * proportion;
                drawHeight = imageLoader.getSpriteHeight() * proportion;
            }

            long posFactor = getPosFactor(0);

            float xPos = -(screenCenterW.getX() * posFactor) - (screenCenterT.getX() * camera.zoom);
            float yPos = (screenCenterW.getY() * posFactor) - (screenCenterT.getY() * camera.zoom);
            float xSize = drawwidth * posFactor;
            float ySize = drawHeight * posFactor;

            drw.draw(batch, xPos, yPos, xSize, ySize);
        }
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {

        if (pointer == MOUSE_WHEEL_POINTER_UP || pointer == MOUSE_WHEEL_POINTER_DOWN) {
            lastTouchPos = new Vector2(x, y);
            return true;
        }

        y = mapIntHeight - y;
        // debugString = "touchDown " + x + " - " + y;
        if (inputState == InputState.Idle) {
            fingerDown.clear();
            inputState = InputState.IdleDown;
            fingerDown.put(pointer, new Point(x, y));
        } else {
            fingerDown.put(pointer, new Point(x, y));
            if (fingerDown.size() == 2)
                inputState = InputState.Zoom;
        }

        return true;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {

        if (pointer == MOUSE_WHEEL_POINTER_UP || pointer == MOUSE_WHEEL_POINTER_DOWN) {
            // Mouse wheel scrolling => Zoom in/out

            if (lastDynamicZoom == -1)
                lastDynamicZoom = zoomBtn.getZoom();

            float div = lastTouchPos.x - x;

            float zoomValue = div / 100f;

            int maxZoom = MAX_MAP_ZOOM;
            int minZoom = 0;
            float dynZoom = (lastDynamicZoom - zoomValue);

            if (dynZoom > maxZoom)
                dynZoom = maxZoom;
            if (dynZoom < minZoom)
                dynZoom = minZoom;

            if (lastDynamicZoom != dynZoom) {

                Log.debug(sClass, "Mouse Zoom:" + div + "/" + zoomValue + "/" + dynZoom);

                lastDynamicZoom = dynZoom;
                zoomBtn.setZoom((int) lastDynamicZoom);
                inputState = InputState.Idle;

                kineticZoom = new KineticZoom(camera.zoom, getPosFactor(lastDynamicZoom), System.currentTimeMillis(), System.currentTimeMillis() + ZOOM_TIME);

                GL.that.addRenderView(this, GL.FRAME_RATE_ACTION);
                GL.that.renderOnce();

            }

            return true;
        }

        try {
            y = mapIntHeight - y;
            // debugString = "touchDragged: " + x + " - " + y;
            // debugString = "touchDragged " + inputState.toString();
            if (inputState == InputState.IdleDown) {
                // es wurde 1x gedr�ckt -> testen, ob ein gewisser Minimum Bereich verschoben wurde
                Point p = fingerDown.get(pointer);
                if (p != null) {
                    // if ((Math.abs(p.x - x) > 10) || (Math.abs(p.y - y) > 10)) // this check is not necessary because this is already
                    // checked in GL.java
                    {
                        inputState = InputState.Pan;
                        // GL_Listener.glListener.addRenderView(this, frameRateAction);
                        GL.that.renderOnce();
                        // xxx startTimer(frameRateAction);
                        // xxx ((GLSurfaceView) MapViewGL.ViewGl).requestRender();
                    }
                    return false;
                }
            }
            if (inputState == InputState.Button) {
                // wenn ein Button gedr�ckt war -> beim Verschieben nichts machen!!!
                return false;
            }

            if ((inputState == InputState.Pan) && (fingerDown.size() == 1)) {

                // Fadein ZoomButtons!
                zoomBtn.resetFadeOut();

                GL.that.renderOnce();

                Point lastPoint = (Point) fingerDown.values().toArray()[0];

                synchronized (screenCenterT) {
                    double angle = imageHeading * MathUtils.DEG_RAD;
                    int dx = (lastPoint.x - x);
                    int dy = (y - lastPoint.y);
                    int dxr = (int) (Math.cos(angle) * dx + Math.sin(angle) * dy);
                    int dyr = (int) (-Math.sin(angle) * dx + Math.cos(angle) * dy);
                    // debugString = dx + " - " + dy + " - " + dxr + " - " + dyr;

                    // Pan stufenlos anpassen an den aktuell g�ltigen Zoomfaktor
                    float tmpZoom = camera.zoom;
                    float ffaktor = 1.5f;
                    while (tmpZoom > ffaktor) {
                        tmpZoom /= 2;
                    }
                    screenCenterT.set(screenCenterT.getX() + dxr, screenCenterT.getY() + dyr);
                }

                lastPoint.x = x;
                lastPoint.y = y;
            } else if ((inputState == InputState.Zoom) && (fingerDown.size() == 2)) {
                Point p1 = (Point) fingerDown.values().toArray()[0];
                Point p2 = (Point) fingerDown.values().toArray()[1];
                float originalDistance = (float) Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));

                if (fingerDown.containsKey(pointer)) {
                    // neue Werte setzen
                    fingerDown.get(pointer).x = x;
                    fingerDown.get(pointer).y = y;
                    p1 = (Point) fingerDown.values().toArray()[0];
                    p2 = (Point) fingerDown.values().toArray()[1];
                }
                float currentDistance = (float) Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
                float ratio = originalDistance / currentDistance;
                camera.zoom = camera.zoom * ratio;

                if (camera.zoom < getPosFactor(zoomBtn.getMaxZoom())) {
                    camera.zoom = getPosFactor(zoomBtn.getMaxZoom());
                }
                if (camera.zoom > getPosFactor(zoomBtn.getMinZoom())) {
                    camera.zoom = getPosFactor(zoomBtn.getMinZoom());
                }

                lastDynamicZoom = camera.zoom;

                int zoom = MAX_MAP_ZOOM;
                float tmpZoom = camera.zoom;
                float faktor = 1.5f;
                while (tmpZoom > faktor) {
                    tmpZoom /= 2;
                    zoom--;
                }
                aktZoom = zoom;

                zoomBtn.setZoom(aktZoom);
                return false;
            }
            return true;
        } catch (Exception ex) {
            Log.err(sClass, "-onTouchDragged Error", ex);
        }

        return false;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {

        if (pointer == MOUSE_WHEEL_POINTER_UP || pointer == MOUSE_WHEEL_POINTER_DOWN) {
            return true;
        }

        if (inputState == InputState.IdleDown) {
            // pressed but not moved
            fingerDown.remove(pointer);
            inputState = InputState.Idle;
            // -> Buttons testen

            // auf Button Clicks nur reagieren, wenn aktuell noch kein Finger gedr�ckt ist!!!
            if (kineticPan != null)
                // bei FingerKlick (wenn Idle) sofort das kinetische Scrollen stoppen
                kineticPan = null;

            return false;
        }

        fingerDown.remove(pointer);
        if (fingerDown.size() == 1)
            inputState = InputState.Pan;
        else if (fingerDown.size() == 0) {
            inputState = InputState.Idle;
            GL.that.renderOnce();

            if ((kineticZoom == null) && (kineticPan == null))
                GL.that.removeRenderView(this);

            if (kineticPan != null)
                kineticPan.start();
        }
        return true;
    }

    @Override
    public void dispose() {
        img.dispose();
        img = null;

        deleteImage.dispose();
        deleteImage = null;
    }

    public enum InputState {
        Idle, IdleDown, Button, Pan, Zoom, PanAutomatic, ZoomAutomatic
    }
}
