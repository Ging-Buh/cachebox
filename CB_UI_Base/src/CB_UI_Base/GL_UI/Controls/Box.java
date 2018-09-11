/*
 * Copyright (C) 2011-2012 team-cachebox.de
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

package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Eine leere Box, die Controls(GL_View_Base) aufnehmen kann
 *
 * @author Longri
 */
public class Box extends CB_View_Base {

    private final Color borderColor = Color.BLACK;
    private float borderSize = 0;
    private Sprite borderSprite;

    public Box() {
        super("Box");
    }

    public Box(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    public Box(float Width, float Height, String Name) {
        super(0f, 0f, Width, Height, Name);
    }

    public Box(float width, float height) {
        super(0f, 0f, width, height, "");
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);
        if (getBorderSize() > 0)
            drawBorder(batch);
    }

    protected void drawBorder(Batch batch) {
        if (borderSprite == null) {
            try {
                GL.that.RunOnGLWithThreadCheck(new IRunOnGL() {

                    @Override
                    public void run() {

                        int w = (int) getWidth();
                        int h = (int) getHeight();

                        Pixmap borderRegPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                        borderRegPixmap.setColor(borderColor);

                        int borderAsInt = Math.round(Box.this.borderSize);

                        for (int i = 0; i < borderAsInt + 1; i++) {
                            borderRegPixmap.drawRectangle(i, i, ((int) getWidth()) - (i), ((int) getHeight()) - (i));
                        }

                        borderSprite = new Sprite(new Texture(borderRegPixmap, Pixmap.Format.RGBA8888, false),
                                (int) getWidth(), (int) getHeight());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (borderSprite != null)
            batch.draw(borderSprite, 0, 0);

    }

    public float getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(float borderSize) {

        this.borderSize = borderSize;
        leftBorder = borderSize;
        rightBorder = borderSize;
        topBorder = borderSize;
        bottomBorder = borderSize;
    }
}
