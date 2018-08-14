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
package CB_Locator.Map;

import CB_UI_Base.Events.invalidateTextureEvent;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ZoomScale extends CB_View_Base implements invalidateTextureEvent {
    private final boolean isVisible = true;
    private final int timeToFadeOut = 5000; // 5Sec
    private final int fadeStep = 50; // 100 mSec
    private final ZoomScale THIS;
    Timer timer;
    private int minzoom = 6;
    private int maxzoom = 20;
    private float zoom = 13;
    private CB_RectF ScaleDrawRec;
    private Date timeLastAction = new Date();
    private boolean fadeOut = false;
    private boolean fadeIn = false;
    private float FadeValue = 1.0f;
    private Sprite CachedScaleSprite = null;
    private Pixmap CachedScalePixmap = null;
    private Texture CachedScaleTexture = null;
    private CB_RectF ValueRec;
    private int topRow;
    private int bottomRow = 1;
    private int centerColumn;
    private int halfWidth;
    // private float dist = 20;
    private int lineHeight = 10;
    private float numSteps;
    private float grundY;
    private CB_RectF storedRec;

    public ZoomScale(CB_RectF rec, String Name, int minzoom, int maxzoom, float zoom) {
        super(rec, Name);
        this.minzoom = minzoom;
        this.maxzoom = maxzoom;
        this.zoom = zoom;
        THIS = this;
    }

    @Override
    protected void render(Batch batch) {
        if (this.getWidth() < 1 || this.getHeight() < 1)
            return;

        int valueRecHeight = (int) (this.getWidth() / 2);

        if (ScaleDrawRec == null) {
            ScaleDrawRec = this.copy();
            ScaleDrawRec.setHeight(this.getHeight() - valueRecHeight);
            ScaleDrawRec.setPos(new Vector2(0, valueRecHeight / 2));
        }

        if (!isVisible)
            return;
        checkFade();

        // Draw Scale

        Sprite scale = drawSprite(ScaleDrawRec);
        if (scale != null) {
            scale.setY(valueRecHeight / 2);
            scale.draw(batch, FadeValue);
        }

        // Draw Value Background
        if (ValueRec != null) {
            Sprite valueBack;
            valueBack = Sprites.ZoomValueBack;
            valueBack.setBounds(ValueRec.getX() + 1.5f, ValueRec.getY(), ValueRec.getWidth(), ValueRec.getHeight());
            valueBack.draw(batch, FadeValue);
        }

        int intZoom = (int) zoom;

        try {
            com.badlogic.gdx.graphics.Color c = COLOR.getFontColor();
            Fonts.getNormal().setColor(c.r, c.g, c.b, FadeValue);
            Fonts.getNormal().draw(batch, String.valueOf(intZoom), ValueRec.getX() + (ValueRec.getWidth() / 3), ValueRec.getY() + ValueRec.getHeight() / 1.15f);
            Fonts.getNormal().setColor(c.r, c.g, c.b, 1f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        invalidateTextureEventList.Add(this);
    }

    public void setZoom(float value) {
        zoom = value;
        resetFadeOut();
    }

    public void setMaxZoom(int value) {
        maxzoom = value;
        ValueRec = null;
        CachedScaleSprite = null;
    }

    public void setMinZoom(int value) {
        minzoom = value;
        ValueRec = null;
        CachedScaleSprite = null;
    }

    /**
     * Zeichnet die Scala in eine Bitmap, damit diese als Sprite benutzt werden kann!
     *
     * @param rect
     */
    private Sprite drawSprite(CB_RectF rect) {
        if (rect == null)
            return null;

        if (storedRec == null || !(storedRec.equals(rect))) {
            storedRec = rect.copy();
            ValueRec = null;
        }

        int y = 0;

        if (ValueRec == null) {
            topRow = (int) rect.getHeight() - 2;
            bottomRow = 2;
            centerColumn = (int) (rect.getWidth() / 2);
            halfWidth = (int) (rect.getWidth() / 4);
            lineHeight = 10;
            numSteps = maxzoom - minzoom;
            grundY = rect.getY() - halfWidth;

            // dist = (bottomRow - topRow) / numSteps;

            y = (int) ((1 - ((zoom) - minzoom) / numSteps) * (bottomRow - topRow)) + topRow;

            ValueRec = new CB_RectF(rect.getX() + GL_UISizes.infoShadowHeight + centerColumn - rect.getWidth() / 2 - lineHeight / 2, grundY + y, rect.getWidth(), rect.getWidth() / 2);
        } else {
            y = (int) ((1 - ((zoom) - minzoom) / numSteps) * (bottomRow - topRow)) + topRow;
            ValueRec.setY(grundY + y);
        }

        if (CachedScaleSprite != null)
            return CachedScaleSprite;

        disposeTexture();

        // int w = getNextHighestPO2((int) getWidth());
        // int h = getNextHighestPO2((int) getHeight());

        int w = (int) getWidth();
        int h = (int) getHeight();

        CachedScalePixmap = new Pixmap(w, h, Pixmap.Format.RGBA4444);
        CachedScalePixmap.setColor(0f, 0f, 0f, 1f);

        CachedScalePixmap.drawLine(centerColumn, bottomRow, centerColumn, topRow);

        for (int i = minzoom; i <= maxzoom; i++) {
            y = (int) ((1 - (i - minzoom) / numSteps) * (bottomRow - topRow)) + topRow;
            CachedScalePixmap.drawRectangle(3, y, (int) getWidth() - 3, 1);

        }

        CachedScaleTexture = new Texture(CachedScalePixmap);

        CachedScaleSprite = new Sprite(CachedScaleTexture, (int) rect.getWidth(), (int) rect.getHeight());

        return CachedScaleSprite;

    }

    private void disposeTexture() {
        if (CachedScalePixmap != null)
            CachedScalePixmap.dispose();
        if (CachedScaleTexture != null)
            CachedScaleTexture.dispose();
        CachedScaleTexture = null;
        CachedScalePixmap = null;
        CachedScaleSprite = null;
    }

    /**
     * Irgend eine Zoom Funktion ausgef�hrt, also FadeOut zur�ck setzen und die Scala Einblenden!
     */
    public void resetFadeOut() {
        // Log.d("CACHEBOX", "Reset Fade Out");
        if (fadeIn && !fadeOut) {
            fadeIn = false;
            FadeValue = 1.0f;
        } else if (!this.isVisible()) {
            // Log.d("CACHEBOX", "Start Fade In");
            this.setVisible(true);
            fadeIn = true;
            FadeValue = 0f;
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

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                GL.that.addRenderView(THIS, GL.FRAME_RATE_ACTION);
                cancelTimerToFadeOut();
            }
        };
        try {
            timer.schedule(task, timeToFadeOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkFade() {
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
                if (FadeValue <= 0f) {
                    // Log.d("CACHEBOX", "Ende Fade Out");
                    FadeValue = 0f;
                    fadeOut = false;
                    this.setVisible(false);
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

    public boolean isShown() {
        return isVisible;
    }

    @Override
    public void onStop() {
        super.onStop();
        CachedScaleSprite = null;
    }

    @Override
    public void onResized(CB_RectF rec) {
        // Log.debug(log, "ZoomScale Rezised");
        ScaleDrawRec = null;
        storedRec = null;
        CachedScaleSprite = null;
    }

    @Override
    protected void SkinIsChanged() {
        CachedScaleSprite = null;
        ValueRec = null;
        storedRec = null;
    }

    @Override
    public void setVisible(boolean On) {
        super.setVisible(On);

        cancelTimerToFadeOut();

    }

    @Override
    public void invalidateTexture() {
        ScaleDrawRec = null;
        storedRec = null;
        disposeTexture();
    }

    @Override
    public void onHide() {
        GL.that.removeRenderView(this);
    }

}
