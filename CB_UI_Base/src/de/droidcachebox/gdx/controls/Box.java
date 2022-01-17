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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.math.CB_RectF;

/**
 * Eine leere Box, die Controls(GL_View_Base) aufnehmen kann
 *
 * @author Longri
 */
public class Box extends CB_View_Base {

    private final Color borderColor = Color.BLACK;
    private final float borderSize = 0;
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

    private void drawBorder(Batch batch) {
        if (borderSprite == null) {
            try {
                GL.that.runOnGLWithThreadCheck(() -> {

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
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (borderSprite != null)
            batch.draw(borderSprite, 0, 0);

    }

    private float getBorderSize() {
        return borderSize;
    }

}
