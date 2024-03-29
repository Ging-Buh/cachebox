/*
 * Copyright (C) 2014 team-cachebox.de
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
package de.droidcachebox.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.graphics.mapsforge.GDXBitmap;
import de.droidcachebox.gdx.graphics.mapsforge.GDXMatrix;
import de.droidcachebox.utils.CB_List;

/**
 * @author Longri
 */
public class VectorDrawable implements GDXBitmap, Drawable, Disposable {

    public static final float FBO_SCALER = 1;// 2.5f;
    public final static HSV_Color TRANSPARENT = new HSV_Color(0, 0, 0, 0);
    /**
     * @uml.property name="width"
     */
    private final int DEFAULT_WIDTH;
    /**
     * @uml.property name="height"
     */
    private final int DEFAULT_HEIGHT;
    /**
     * @uml.property name="isDisposed"
     */
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    /**
     * @uml.property name="fBO_DrawingTime"
     */
    public long FBO_DrawingTime = -1;
    /**
     * @uml.property name="fBOisDrawed"
     */
    public boolean FBOisDrawed = false;
    /**
     * @uml.property name="background"
     * @uml.associationEnd
     */
    private ColorDrawable background;
    /**
     * @uml.property name="drawableList"
     */
    private CB_List<MatrixDrawable> drawableList;
    /**
     * @uml.property name="rotateDrawableList"
     */
    private SortedRotateList rotateDrawableList;
    /**
     * @uml.property name="m_fboEnabled"
     */
    private boolean m_fboEnabled = true;
    /**
     * @uml.property name="m_fbo"
     * @uml.associationEnd
     */
    private FrameBuffer m_fbo = null;
    /**
     * @uml.property name="m_fboRegion"
     * @uml.associationEnd
     */
    private TextureRegion m_fboRegion = null;
    /**
     * @uml.property name="flipY"
     */
    private boolean flipY = false;
    /**
     * @uml.property name="flipX"
     */
    private boolean flipX = false;
    /**
     * @uml.property name="runOnGlSetted"
     */
    private boolean RunOnGlSetted = false;

    public VectorDrawable(int width, int height, HSV_Color backgroundColor) {
        this.background = new ColorDrawable(backgroundColor);
        this.drawableList = new CB_List<MatrixDrawable>();
        this.rotateDrawableList = new SortedRotateList();
        this.DEFAULT_WIDTH = width;
        this.DEFAULT_HEIGHT = height;
    }

    public VectorDrawable(int width, int height) {
        this.drawableList = new CB_List<MatrixDrawable>();
        this.rotateDrawableList = new SortedRotateList();
        this.background = new ColorDrawable(TRANSPARENT);
        this.DEFAULT_WIDTH = width;
        this.DEFAULT_HEIGHT = height;
    }

    public void Flip(boolean X, boolean Y) {
        flipX = X;
        flipY = Y;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {// Draw only not rotate

        if (isDisposed.get()) {
            return;
        }
        final Matrix4 oriMatrix = GL.that.getPolygonSpriteBatch().getProjectionMatrix().cpy();
        Matrix4 thisDrawMatrix = oriMatrix.cpy();
        thisDrawMatrix.translate(x, y, 0);

        drawFbo(batch, x, y, width, height, oriMatrix, thisDrawMatrix);
        GL.that.getPolygonSpriteBatch().setProjectionMatrix(oriMatrix);
    }

    public void draw(Batch batch, float x, float y, final float width, final float height, float rotated) {

        if (isDisposed.get()) {
            return;
        }

        final Matrix4 oriMatrix = GL.that.getPolygonSpriteBatch().getProjectionMatrix().cpy();
        Matrix4 thisDrawMatrix = oriMatrix.cpy();
        thisDrawMatrix.translate(x, y, 0);

        drawFbo(batch, x, y, width, height, oriMatrix, thisDrawMatrix);

        for (MatrixDrawable drw : rotateDrawableList) {

            Matrix4 matrix = thisDrawMatrix.cpy();
            GDXMatrix drwMatrix = new GL_Matrix(drw.matrix);
            matrix.mul(drwMatrix.getMatrix4().cpy());
            GL.that.getPolygonSpriteBatch().setProjectionMatrix(matrix);
            drw.drawable.draw(GL.that.getPolygonSpriteBatch(), 0, 0, width, height, -rotated * MathUtils.degreesToRadians);
        }
        GL.that.getPolygonSpriteBatch().setProjectionMatrix(oriMatrix);
    }

    private void drawFbo(Batch batch, float x, float y, final float width, final float height, final Matrix4 oriMatrix, Matrix4 thisDrawMatrix) {
        final int fboScalerWidth = (int) (this.DEFAULT_WIDTH * FBO_SCALER);
        final int fboScalerHeight = (int) (this.DEFAULT_HEIGHT * FBO_SCALER);
        if (!RunOnGlSetted && m_fboEnabled && m_fboRegion == null) {
            RunOnGlSetted = true;

            GL.that.runOnGL((Runnable) () -> {
                synchronized (isDisposed) {

                    if (isDisposed.get()) {
                        return;
                    }

                    try {
                        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

                        long start = System.currentTimeMillis();

                        m_fbo = new FrameBuffer(Format.RGBA8888, fboScalerWidth, fboScalerHeight, false);
                        m_fboRegion = new TextureRegion(m_fbo.getColorBufferTexture());
                        m_fboRegion.flip(flipX, flipY);

                        m_fbo.begin();

                        // clear screen
                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

                        GL.that.getPolygonSpriteBatch().setColor(new Color(Color.WHITE));

                        GL.that.getPolygonSpriteBatch().begin();

                        Matrix4 matrix = new Matrix4().setToOrtho2D(0, 0, width, height);
                        matrix.scale(FBO_SCALER, FBO_SCALER, 1);
                        GL.that.getPolygonSpriteBatch().setProjectionMatrix(matrix);

                        // draw Background
                        GL.that.getPolygonSpriteBatch().disableBlending();
                        background.draw(GL.that.getPolygonSpriteBatch(), 0, 0, fboScalerWidth, fboScalerHeight);
                        GL.that.getPolygonSpriteBatch().enableBlending();
                        int count = 0;

                        for (int i = 0, n = drawableList.size(); i < n; i++) {
                            MatrixDrawable drw = drawableList.get(i);
                            if (count++ > 2500) {
                                GL.that.getPolygonSpriteBatch().flush();
                                count = 0;
                            }
                            matrix = new Matrix4().setToOrtho2D(0, 0, width, height);
                            if (drw.matrix != null) {

                                matrix.mul(drw.matrix.getMatrix4());
                            }

                            GL.that.getPolygonSpriteBatch().setProjectionMatrix(matrix);
                            drw.drawable.draw(GL.that.getPolygonSpriteBatch(), 0, 0, width, height, 0);
                        }

                        if (m_fbo != null) {
                            GL.that.getPolygonSpriteBatch().end();
                            m_fbo.end();
                            m_fboEnabled = false;
                        }

                        FBOisDrawed = true;
                        FBO_DrawingTime = System.currentTimeMillis() - start;
                        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
                        GL.that.getPolygonSpriteBatch().setProjectionMatrix(oriMatrix);

                        m_fboEnabled = false;
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            });

        }

        if (m_fboRegion != null) {

            // TODO clear and release the drawables that drawed on m_fboRegion
            // if first drawing of m_fboRegion

            batch.draw(m_fboRegion, x, y, width, height);
        } else {

            int count = 0;

            for (int i = 0, n = drawableList.size(); i < n; i++) {
                MatrixDrawable drw = drawableList.get(i);
                if (!drw.reaelDraw)
                    continue;
                if (count++ > 2500) {
                    GL.that.getPolygonSpriteBatch().flush();
                    count = 0;
                }
                Matrix4 matrix = thisDrawMatrix.cpy();
                if (drw.matrix != null)
                    matrix.mul(drw.matrix.getMatrix4());

                GL.that.getPolygonSpriteBatch().setProjectionMatrix(matrix);

                drw.drawable.draw(GL.that.getPolygonSpriteBatch(), 0, 0, width, height, 0);
            }

        }
    }

    public void addDrawable(IRotateDrawable drw, boolean canRealDraw) {
        addDrawable(drw, new GL_Matrix(), canRealDraw);
    }

    public void addDrawable(IRotateDrawable drw, GDXMatrix matrix, boolean canRealDraw) {
        drawableList.add(new MatrixDrawable(drw, matrix, canRealDraw));
    }

    @Override
    public void compress(OutputStream outputStream) throws IOException {

    }

    @Override
    public void incrementRefCount() {

    }

    @Override
    public void decrementRefCount() {

    }

    /**
     * @return
     * @uml.property name="height"
     */
    @Override
    public int getHeight() {
        return this.DEFAULT_HEIGHT;
    }

    /**
     * @return
     * @uml.property name="width"
     */
    @Override
    public int getWidth() {
        return this.DEFAULT_WIDTH;
    }

    @Override
    public void scaleTo(int width, int height) {

    }

    @Override
    public void setBackgroundColor(int color) {
        this.background = new ColorDrawable(new HSV_Color(color));
    }

    @Override
    public void recycle() {

    }

    @Override
    public void getPixels(int[] maskBuf, int i, int w, int j, int y, int w2, int k) {

    }

    @Override
    public void setPixels(int[] maskedContentBuf, int i, int w, int j, int y, int w2, int k) {

    }

    public SortedRotateList getRotateDrawables() {
        if (rotateDrawableList.isEmpty())
            return null;
        return rotateDrawableList;
    }

    public void clearDrawables() {
        drawableList.clear();
    }

    public void setBackgroundColor(HSV_Color color) {
        this.background = new ColorDrawable(color);
    }

    public void addRotateDrawable(IRotateDrawable drw) {
        rotateDrawableList.add(new MatrixDrawable(drw, new GL_Matrix(), true));
    }

    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void dispose() {
        synchronized (isDisposed) {
            if (isDisposed.get())
                return;
            if (rotateDrawableList != null) {
                for (MatrixDrawable drw : rotateDrawableList) {
                    drw.dispose();
                }
                rotateDrawableList.clear();
                rotateDrawableList = null;
            }

            if (drawableList != null) {
                for (int i = 0, n = drawableList.size(); i < n; i++) {
                    drawableList.get(i).dispose();

                }
                drawableList.clear();
                drawableList = null;
            }

            background = null;
            if (m_fboRegion != null)
                m_fboRegion.getTexture().dispose();
            m_fboRegion = null;
            if (m_fbo != null)
                m_fbo.dispose();
            m_fbo = null;
            isDisposed.set(true);
        }
    }

    // implements Drawable

    @Override
    public float getBottomHeight() {

        return 0;
    }

    @Override
    public void setBottomHeight(float arg0) {

    }

    @Override
    public float getLeftWidth() {

        return 0;
    }

    @Override
    public void setLeftWidth(float arg0) {

    }

    @Override
    public float getMinHeight() {

        return 0;
    }

    @Override
    public void setMinHeight(float arg0) {

    }

    @Override
    public float getMinWidth() {

        return 0;
    }

    @Override
    public void setMinWidth(float arg0) {

    }

    @Override
    public float getRightWidth() {

        return 0;
    }

    @Override
    public void setRightWidth(float arg0) {

    }

    @Override
    public float getTopHeight() {

        return 0;
    }

    @Override
    public void setTopHeight(float arg0) {

    }

    // FIXME create BitmapDrawable from FMBO

    @Override
    public Texture getTexture() {

        return null;
    }

    @Override
    public boolean isDestroyed() {
        // TODO Auto-generated method stub
        return false;
    }

}
