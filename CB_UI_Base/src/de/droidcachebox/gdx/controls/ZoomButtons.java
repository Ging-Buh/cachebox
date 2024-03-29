/*
 * Copyright (C) 2011-2022 team-cachebox.de
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

package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;

public class ZoomButtons extends CB_View_Base {

    private final int timeToFadeOut = 13000; // 7Sec
    boolean virtualVisible = false;
    Timer timer;
    private int minzoom = 6;
    // private CB_RectF BtnDrawRec;
    private int maxzoom = 20;
    private int zoom = 13;
    private CB_RectF HitRecUp;
    private CB_RectF HitRecDown;
    private Date timeLastAction = new Date();
    private boolean fadeOut = false;
    private boolean fadeIn = false;
    private float FadeValue = 1.0f;
    private float minimummFadeValue = 0;
    private boolean dontFadeOut = false;

    // # Constructors
    private boolean portrait = false;
    private OnClickListener mOnClickListenerUp;
    private OnClickListener mOnClickListenerDown;
    private boolean firstDraw = true;

    /*
    public ZoomButtons(float X, float Y, float Width, float Height, String Name) {
        super(X, Y, Width, Height, Name);
        onResized(this);
        resetFadeOut();
    }
     */

    public ZoomButtons(CB_RectF rec, GL_View_Base view, String name) {
        super(rec, view, name);
        onResized(this);
        resetFadeOut();
    }

    /*
    public ZoomButtons() {
        // Initial ZoomButtons without any Drawables
        super("");
        withoutDrawing = true;
    }
     */

    public void setOnClickListenerUp(OnClickListener l) {
        this.setClickable(true);
        mOnClickListenerUp = l;
    }

    public void setOnClickListenerDown(OnClickListener l) {
        this.setClickable(true);
        mOnClickListenerDown = l;
    }

    /*
    public void disableFadeOut() {
        dontFadeOut = true;
    }

    public void enableFadeOut() {
        dontFadeOut = false;
    }
     */

    public void setMinimumFadeValue(float value) {
        minimummFadeValue = value;
    }

    /*
    public boolean FadeOutIsEnabled() {
        return !dontFadeOut;
    }

    public void setPortrait() {
        portrait = true;
        onResized(this);
    }

    public void setPortrait(boolean value) {
        portrait = value;
        onResized(this);
    }

     */

    @Override
    public boolean click(int x, int y, int pointer, int button) {

        boolean behandelt = false;

        if (mOnClickListenerUp != null) {
            if (HitRecUp.contains(x, y)) {

                resetFadeOut();
                if (zoom < maxzoom) {
                    zoom++;
                    // Log.debug(log, "ZoomButton OnClick UP (" + zoom + ")");
                    mOnClickListenerUp.onClick(this, x, y, pointer, button);
                }
                behandelt = true;
            }
        }

        if (mOnClickListenerUp != null && !behandelt) {
            if (HitRecDown.contains(x, y)) {
                resetFadeOut();
                if (zoom > minzoom) {
                    zoom--;
                    // Log.debug(log, "ZoomButton OnClick Down (" + zoom + ")");
                    mOnClickListenerDown.onClick(this, x, y, pointer, button);
                }
                behandelt = true;
            }
        }

        return behandelt;
    }

    /*
    public boolean hitTest(Vector2 pos) {
        if (zoom != maxzoom) {
            if (HitRecUp != null) {
                if (HitRecUp.contains(pos.x, pos.y)) {
                    // if (FadeValue > 0.4f)
                    ZoomAdd(1);
                    resetFadeOut();
                    return true;
                }
            }
        }

        if (zoom != minzoom) {
            if (HitRecDown != null) {
                if (HitRecDown.contains(pos.x, pos.y)) {
                    // if (FadeValue > 0.4f)
                    ZoomAdd(-1);
                    resetFadeOut();
                    return true;
                }
            }
        }
        return false;
    }
     */

    public boolean touchDownTest(Vector2 pos) {
        if (HitRecUp != null) {
            if (HitRecUp.contains(pos.x, pos.y)) {
                onTouchUp = true;
                resetFadeOut();
                return true;
            }
        }
        if (HitRecDown != null) {
            if (HitRecDown.contains(pos.x, pos.y)) {
                onTouchDown = true;
                resetFadeOut();
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(Batch batch) {
        /*
        boolean withoutDrawing = false;
        if (withoutDrawing)
            return;
         */
        super.render(batch);

        if (firstDraw) {
            resetFadeOut();
            firstDraw = false;
        }

        if (!this.isVisible())
            return;
        // Log.d("CACHEBOX", "in=" + fadeIn + " out=" + fadeOut + " Fade=" + FadeValue);
        checkFade();

        // draw down button
        Sprite btnDown;
        if (zoom == minzoom) {
            btnDown = Sprites.ZoomBtn.get(2);// disabled
        } else {
            btnDown = Sprites.ZoomBtn.get(onTouchDown ? 1 : 0);
        }

        float hw = HitRecDown.getWidth();
        float hh = HitRecDown.getHeight();
        float hx = HitRecDown.getX();
        float hy = HitRecDown.getY();
        float offX = 0;
        float offY = 0;

        if (portrait) {
            float e = btnDown.getWidth() / 2;
            float f = btnDown.getHeight() / 2;

            btnDown.setOrigin(e, f);
            btnDown.setRotation(90);

            hw = hh;
            hh = HitRecDown.getWidth();

            // caclc offset
            offX = -(e - f);
            offY = -(f - e);
        } else {
            btnDown.setRotation(0f);
        }

        btnDown.setBounds(hx + offX, hy + offY, hw, hh);
        btnDown.draw(batch, FadeValue);

        // draw up button
        Sprite btnUp;
        if (zoom == maxzoom) {
            btnUp = Sprites.ZoomBtn.get(5);// disabled
        } else {
            btnUp = Sprites.ZoomBtn.get(onTouchUp ? 4 : 3);
        }

        hw = HitRecUp.getWidth();
        hh = HitRecUp.getHeight();
        hx = HitRecUp.getX();
        hy = HitRecUp.getY();

        if (portrait) {
            float e = btnUp.getWidth() / 2;
            float f = btnUp.getHeight() / 2;
            btnUp.setOrigin(e, f);
            btnUp.setRotation(90);

            hw = hh;
            hh = HitRecUp.getWidth();
        } else {
            btnUp.setRotation(0f);
        }

        btnUp.setBounds(hx + offX, hy + offY, hw, hh);
        btnUp.draw(batch, FadeValue);
    }

    /*
    public void ZoomAdd(int value) {

        zoom += value;
        if (zoom > maxzoom)
            zoom = maxzoom;
        if (zoom < minzoom)
            zoom = minzoom;

        // //Log.d("CACHEBOX", "ZoomAdd" + zoom);
    }
     */

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int value) {
        zoom = value;
        if (zoom > maxzoom)
            zoom = maxzoom;
        if (zoom < minzoom)
            zoom = minzoom;
    }

    public int getMaxZoom() {
        return maxzoom;
    }

    public void setMaxZoom(int value) {
        if (minzoom > value) {
            try {
                throw new Exception("value out of range minzoom > maxzoom");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        maxzoom = value;
    }

    public int getMinZoom() {
        return minzoom;
    }

    public void setMinZoom(int value) {
        if (maxzoom < value) {
            try {
                throw new Exception("value out of range minzoom > maxzoom");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        minzoom = value;
    }

    /**
     * Irgend eine Taste gedr�ckt, also FadeOut zur�ck setzen
     */
    public void resetFadeOut() {
        // Log.d("CACHEBOX", "Reset Fade Out");
        if (fadeIn && !fadeOut) {
            fadeIn = false;
            FadeValue = 1.0f;
        } else if (!virtualVisible) {
            // Log.d("CACHEBOX", "Start Fade In");
            this.setVisible(true);
            virtualVisible = true;
            fadeIn = true;
            FadeValue = minimummFadeValue;
        }
        if (fadeOut) {
            fadeOut = false;
            FadeValue = 1.0f;
        }

        timeLastAction = new Date();
        startTimerToFadeOut();
    }

    private void cancelTimerToFadeOut() {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void startTimerToFadeOut() {

        cancelTimerToFadeOut();

        if (dontFadeOut)
            return;

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                GL.that.addRenderView(ZoomButtons.this, GL.FRAME_RATE_ACTION);
                cancelTimerToFadeOut();
            }
        };
        timer.schedule(task, timeToFadeOut);
    }

    private void checkFade() {

        if (dontFadeOut) {
            fadeOut = false;
            fadeIn = false;
            FadeValue = 1f;
            return;
        }

        // 100 mSec
        int fadeStep = 50;
        if (!fadeOut && !fadeIn && !this.isVisible()) {
            GL.that.removeRenderView(this);
        } else if (!fadeOut && !fadeIn && this.isVisible()) {
            Date now = new Date();
            if (now.getTime() - timeLastAction.getTime() > timeToFadeOut) {
                // Log.d("CACHEBOX", "Start Fade Out");
                // Zeit abgelaufen start Fade Out
                fadeOut = true;
                timeLastAction = new Date();
            }
        } else if (fadeOut) {
            Date now = new Date();
            if (now.getTime() - timeLastAction.getTime() > fadeStep) {
                FadeValue -= 0.05f;
                if (FadeValue <= minimummFadeValue) {
                    // Log.d("CACHEBOX", "Ende Fade Out");
                    FadeValue = minimummFadeValue;
                    fadeOut = false;
                    // this.setVisibility(INVISIBLE);
                    virtualVisible = false;
                    GL.that.removeRenderView(this);
                }
                timeLastAction = new Date();
            }
        } else if (fadeIn) {
            Date now = new Date();
            if (now.getTime() - timeLastAction.getTime() > fadeStep) {
                FadeValue += 0.1f;
                if (FadeValue >= 1f) {
                    // Log.d("CACHEBOX", "Ende Fade In");
                    FadeValue = 1f;
                    fadeIn = false;
                    GL.that.removeRenderView(this);
                }
                timeLastAction = new Date();
            }
        }
    }

    @Override
    public void onResized(CB_RectF rec) {
        // rect auf Teilen in zwei gleich gro�e
        HitRecUp = new CB_RectF(rec);
        HitRecUp.setPos(0, 0); // setze auf 0,0
        HitRecDown = new CB_RectF(rec);
        HitRecDown.setPos(0, 0); // setze auf 0,0

        if (portrait) {
            HitRecUp.setHeight(rec.getHeight() / 2f);
            HitRecDown.setHeight(rec.getHeight() / 2f);
            HitRecUp.setPos(0, HitRecDown.getHeight());
        } else {
            HitRecUp.setWidth(rec.getWidth() / 2f);
            HitRecDown.setWidth(rec.getWidth() / 2f);
            HitRecUp.setPos(HitRecDown.getWidth(), 0);
        }

    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        return touchDownTest(new Vector2(x, y));
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        onTouchUp = onTouchDown = false;
        return true;
    }

    @Override
    public void setVisible(boolean On) {
        super.setVisible(On);

        cancelTimerToFadeOut();
    }

    @Override
    public void onHide() {
        GL.that.removeRenderView(this);
    }
}
