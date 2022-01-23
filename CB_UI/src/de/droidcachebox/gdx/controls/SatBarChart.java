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
package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.locator.GPS;
import de.droidcachebox.locator.GpsStateChangeEvent;
import de.droidcachebox.locator.GpsStateChangeEventList;
import de.droidcachebox.locator.GpsStrength;

public class SatBarChart extends CB_View_Base implements GpsStateChangeEvent {

    private boolean drawWithAlpha = false;
    private Image[] bar = null;
    private boolean redraw = true;

    public SatBarChart(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    public void setDrawWithAlpha(boolean value) {
        drawWithAlpha = value;
        redraw = true;
    }

    @Override
    protected void render(Batch batch) {
        if (redraw)
            setSatStrength();
    }

    private void setSatStrength() {
        float minH = (Sprites.bar.getBottomHeight() / 2) + Sprites.bar.getTopHeight();

        float w = (this.getWidth() / 14);
        boolean small = Sprites.bar.getMinWidth() > w * 1.2f;
        if (small) {
            w = (this.getWidth() / 12);
        }

        // calc Colors
        Color red = Color.RED.cpy();
        Color grn = Color.GREEN.cpy();
        Color gry = Color.LIGHT_GRAY.cpy();

        if (drawWithAlpha) {
            red.a = 0.4f;
            grn.a = 0.4f;
            gry.a = 0.4f;
        }

        if (bar == null) {

            float iniHeight = small ? Sprites.barSmall.getTopHeight() : Sprites.bar.getTopHeight();

            w += 1;
            bar = new Image[14];
            bar[0] = new Image(new CB_RectF(0, 0, w, iniHeight), "", false);
            bar[1] = new Image(new CB_RectF(bar[0].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[2] = new Image(new CB_RectF(bar[1].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[3] = new Image(new CB_RectF(bar[2].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[4] = new Image(new CB_RectF(bar[3].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[5] = new Image(new CB_RectF(bar[4].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[6] = new Image(new CB_RectF(bar[5].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[7] = new Image(new CB_RectF(bar[6].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[8] = new Image(new CB_RectF(bar[7].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[9] = new Image(new CB_RectF(bar[8].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[10] = new Image(new CB_RectF(bar[9].getMaxX() - 1, 0, w, iniHeight), "", false);
            bar[11] = new Image(new CB_RectF(bar[10].getMaxX() - 1, 0, w, iniHeight), "", false);
            if (!small)
                bar[12] = new Image(new CB_RectF(bar[11].getMaxX() - 1, 0, w, iniHeight), "", false);
            if (!small)
                bar[13] = new Image(new CB_RectF(bar[12].getMaxX() - 1, 0, w, iniHeight), "", false);

            for (Image tmp : bar) {
                if (tmp != null) {
                    tmp.setDrawable(small ? Sprites.barSmall_0 : Sprites.bar_0);
                    this.addChild(tmp);
                }
            }
        }

        int count = 0;
        if (GPS.getSatList() != null) {
            for (int i = 0, n = GPS.getSatList().size(); i < n; i++) {
                GpsStrength tmp;
                try {
                    tmp = GPS.getSatList().get(i);
                } catch (Exception e) {
                    break;
                }

                try {
                    // set bar height
                    if (bar[count] != null) {
                        float barHeight = Math.min((tmp.getStrength() * 3 / 100) * this.getHeight(), this.getHeight());

                        if (barHeight < minH) {
                            barHeight = small ? Sprites.barSmall.getTopHeight() : Sprites.bar.getTopHeight();
                            bar[count].setDrawable(small ? Sprites.barSmall_0 : Sprites.bar_0);
                        } else {
                            bar[count].setDrawable(small ? Sprites.barSmall : Sprites.bar);
                        }

                        bar[count].setHeight(barHeight);

                        // // set bar color
                        if (tmp.getFixed()) {
                            bar[count].setColor(grn);
                        } else {
                            bar[count].setColor(red);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                count++;
                if (count >= 13)
                    break;
            }
        }

        // gray other bars
        if (count < 14) {
            for (int i = count; i <= 13; i++) {
                if (bar[i] != null)
                    bar[i].setColor(gry);
            }
        }

        redraw = false;
        GL.that.renderOnce();

    }

    @Override
    public void gpsStateChanged() {
        redraw = true;
        GL.that.renderOnce();
    }

    @Override
    public void dispose() {
        GpsStateChangeEventList.remove(this);
    }

    @Override
    public void onShow() {
        GpsStateChangeEventList.add(this);
        redraw = true;
    }

    @Override
    public void onHide() {
        GpsStateChangeEventList.remove(this);
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        redraw = true;
    }
}
