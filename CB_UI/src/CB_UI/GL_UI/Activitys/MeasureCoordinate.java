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
package CB_UI.GL_UI.Activitys;

import CB_Core.Types.MeasuredCoord;
import CB_Core.Types.MeasuredCoordList;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Map.Descriptor;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Controls.SatBarChart;
import CB_UI_Base.Events.PlatformUIBase;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.CB_Button;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Math.PointD;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.concurrent.atomic.AtomicBoolean;

public class MeasureCoordinate extends ActivityBase implements PositionChangedEvent {
    private final int projectionZoom = 18;// 18;
    // Erdradius / anzahl Kacheln = Meter pro Kachel
    private final double metersPerTile = 6378137.0 / Math.pow(2, projectionZoom);
    private final MeasuredCoordList mMeasureList = new MeasuredCoordList();
    private CB_Button bOK = null;
    private CB_Label lblMeasureCount;
    private CB_Label lblMeasureCoord;
    private CB_Label lblDescMeasureCoord;
    private int MeasureCount = 0;
    private Sprite drawing = null;
    private Pixmap drawingPixmap = null;
    private Texture drawingTexture = null;
    private SatBarChart chart;
    private ICoordReturnListener mCoordReturnListener;
    private AtomicBoolean inRepaint = new AtomicBoolean(false);
    private boolean redraw = true;

    public MeasureCoordinate(CB_RectF rec, String Name, ICoordReturnListener listener) {
        super(rec, Name);
        mCoordReturnListener = listener;

        MeasuredCoord.Referenz = Locator.getInstance().getMyPosition(ProviderType.GPS);

        if (MeasuredCoord.Referenz == null) {
            MeasuredCoord.Referenz = new CoordinateGPS(0, 0);
        }

        iniOkCancel();
        iniLabels();

        iniChart();

    }

    private void iniOkCancel() {
        CB_RectF btnRec = new CB_RectF(leftBorder, this.getBottomHeight(), innerWidth / 2, UiSizes.getInstance().getButtonHeight());
        bOK = new CB_Button(btnRec, "OkButton");

        btnRec.setX(bOK.getMaxX());
        CB_Button bCancel = new CB_Button(btnRec, "CancelButton");

        bOK.setText(Translation.get("ok"));
        bCancel.setText(Translation.get("cancel"));

        this.addChild(bOK);
        this.addChild(bCancel);

        bOK.addClickHandler((v, x, y, pointer, button) -> {
            if (mCoordReturnListener != null) {
                synchronized (mMeasureList) {
                    GL.that.RunOnGL(() -> mCoordReturnListener.returnCoord(mMeasureList.getAccuWeightedAverageCoord()));

                }
            }
            finish();
            return true;
        });

        bCancel.addClickHandler((v, x, y, pointer, button) -> {
            if (mCoordReturnListener != null)
                mCoordReturnListener.returnCoord(null);
            finish();
            return true;
        });

    }

    private void iniLabels() {
        float y = bOK.getMaxY() + innerWidth + (margin * 3);
        float w = Math.max(Fonts.Measure(Translation.get("MeasureCoord")).width, Fonts.Measure(Translation.get("MeasureCount")).width);
        CB_RectF rec = new CB_RectF(leftBorder + margin, y, w, MeasuredLabelHeight);
        CB_RectF rec2 = new CB_RectF(rec.getMaxX() + margin, y, innerWidth - w - margin, MeasuredLabelHeight);

        CB_Label lblDescMeasureCount = new CB_Label(this.name + " lblDescMeasureCount", rec, Translation.get("MeasureCount"));

        lblMeasureCount = new CB_Label(rec2);

        rec2.setY(lblMeasureCount.getMaxY() + margin);
        rec.setY(lblMeasureCount.getMaxY() + margin);

        lblDescMeasureCoord = new CB_Label(this.name + " lblDescMeasureCoord", rec, Translation.get("MeasureCoord"));

        lblMeasureCoord = new CB_Label(rec2);

        this.addChild(lblDescMeasureCount);
        this.addChild(lblMeasureCount);
        this.addChild(lblDescMeasureCoord);
        this.addChild(lblMeasureCoord);
    }

    private void iniChart() {
        float w = innerWidth - margin - margin;
        float h = this.getHeight() - lblDescMeasureCoord.getMaxY() - this.getTopHeight() - margin;

        CB_RectF rec = new CB_RectF(leftBorder + margin, lblDescMeasureCoord.getMaxY() + margin, w, h);
        chart = new SatBarChart(rec, "");
        this.addChild(chart);
    }

    @Override
    protected void finish() {
        if (chart != null)
            chart.dispose();
        chart = null;
        disposeTexture();
        GL.that.removeRenderView(this);
        super.finish();
    }

    private void disposeTexture() {
        try {
            if (drawingPixmap != null)
                drawingPixmap.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (drawingTexture != null)
                drawingTexture.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        drawing = null;
        drawingPixmap = null;
        drawingTexture = null;
    }

    @Override
    protected void initialize() {
        repaintPreview();
    }

    @Override
    protected void render(Batch batch) {

        if (drawing != null)
            drawing.draw(batch);

        if (redraw)
            repaintPreview();

    }

    private void repaintPreview() {
        if (inRepaint.get())
            return;
        inRepaint.set(true);

        disposeTexture();

        CB_RectF panelRec = new CB_RectF(leftBorder, bOK.getMaxY(), innerWidth, innerWidth);

        int w = (int) panelRec.getWidth();
        int h = (int) panelRec.getHeight();

        drawingPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        drawingPixmap.setColor(Color.LIGHT_GRAY);
        drawingPixmap.fillRectangle(0, 0, (int) panelRec.getWidth(), (int) panelRec.getHeight());

        int centerX = (int) panelRec.getHalfWidth();
        int centerY = (int) panelRec.getHalfHeight();

        float minPix = Math.min(panelRec.getWidth(), panelRec.getHeight());

        try {
            synchronized (mMeasureList) {

                if (mMeasureList.size() > 0) {
                    // Gemittelter Punkt der GPS-Messungen
                    double medianLat = MeasuredCoord.Referenz.getLatitude();
                    double medianLon = MeasuredCoord.Referenz.getLongitude();

                    MeasuredCoordList sortetdList = (MeasuredCoordList) mMeasureList.clone();
                    sortetdList.sort();

                    double peakLat = Math.max(Math.abs(sortetdList.get(0).getLatitude() - medianLat), Math.abs(sortetdList.get(sortetdList.size() - 1).getLatitude() - medianLat));
                    double peakLon = Math.max(Math.abs(sortetdList.get(0).getLongitude() - medianLon), Math.abs(sortetdList.get(sortetdList.size() - 1).getLongitude() - medianLon));

                    // Umrechnung in XY
                    double medianX = Descriptor.LongitudeToTileX(projectionZoom, medianLon);
                    double medianY = Descriptor.LatitudeToTileY(projectionZoom, medianLat);

                    double extremeX = Descriptor.LongitudeToTileX(projectionZoom, peakLon + medianLon);
                    double extremeY = Descriptor.LatitudeToTileY(projectionZoom, peakLat + medianLat);

                    double peakX = Math.abs(extremeX - medianX);
                    double peakY = Math.abs(extremeY - medianY);

                    double maxPeak = Math.max(peakX, peakY);

                    double factor = 1;
                    if (maxPeak > 0)
                        factor = minPix / maxPeak;

                    factor /= 2;

                    int x = centerX;
                    int y = centerY;

                    // Track zeichnen

                    for (int i = 1; i < mMeasureList.size(); i++) {

                        PointD lastDrawEntry = Descriptor.projectCoordinate(mMeasureList.get(i - 1).getLatitude(), mMeasureList.get(i - 1).getLongitude(), projectionZoom);

                        int lastX = (int) (centerX + (lastDrawEntry.X - medianX) * factor);
                        int lastY = (int) (centerY - (lastDrawEntry.Y - medianY) * factor);

                        PointD thisDrawEntry = Descriptor.projectCoordinate(mMeasureList.get(i).getLatitude(), mMeasureList.get(i).getLongitude(), projectionZoom);

                        x = (int) (centerX + (thisDrawEntry.X - medianX) * factor);
                        y = (int) (centerY - (thisDrawEntry.Y - medianY) * factor);

                        drawingPixmap.setColor(Color.RED);
                        drawingPixmap.drawLine(lastX, lastY, x, y);

                    }

                    drawingPixmap.setColor(Color.BLUE);
                    drawingPixmap.drawCircle(x, y, 4);
                }
            }
            //
            int m2 = (int) ((4 * minPix) / metersPerTile);
            int m4 = m2 * 2;

            drawingPixmap.setColor(Color.BLACK);
            drawingPixmap.drawCircle(centerX, centerY, m2);
            drawingPixmap.drawCircle(centerX, centerY, m4);

            drawingPixmap.drawLine(centerX, 0, centerX, (int) panelRec.getHeight());
            drawingPixmap.drawLine(0, centerY, (int) panelRec.getWidth(), centerY);

            drawingTexture = new Texture(drawingPixmap);

            drawing = new Sprite(drawingTexture, (int) panelRec.getWidth(), (int) panelRec.getHeight());
            drawing.setX(leftBorder);
            drawing.setY(bOK.getMaxY() + this.getBottomHeight());

            inRepaint.set(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        redraw = false;

        GL.that.renderOnce();
    }

    @Override
    public void PositionChanged() {
        synchronized (mMeasureList) {

            if (MeasureCount == 0)
                lblMeasureCoord.setText("");

            MeasureCount++;
            mMeasureList.add(new MeasuredCoord(Locator.getInstance().getLocation(ProviderType.GPS).toCordinate()));

            lblMeasureCount.setText(MeasureCount + "/" + mMeasureList.size());

            // nach jeder 10. Messung die Liste Aufräumen
            if (mMeasureList.size() % 10 == 0) {
                mMeasureList.setAverage();
                mMeasureList.clearDiscordantValue();
                lblMeasureCoord.setText(mMeasureList.toString());
            }

            redraw = true;
            GL.that.renderOnce();
        }
    }

    @Override
    public String getReceiverName() {
        return "MeasureCoordinate";
    }

    @Override
    public void onShow() {
        PositionChangedEventList.Add(this);
        if (chart != null) {
            chart.onShow();
            chart.setDrawWithAlpha(false);
            PlatformUIBase.switchToGpsMeasure();
        }

    }

    @Override
    public void onHide() {
        PositionChangedEventList.Remove(this);
        if (chart != null)
            chart.onHide();
        PlatformUIBase.switchToGpsDefault();
    }

    @Override
    public void OrientationChanged() {
    }

    @Override
    public Priority getPriority() {
        return Priority.Normal;
    }

    @Override
    public void SpeedChanged() {
    }

    public interface ICoordReturnListener {
        void returnCoord(Coordinate coord);
    }

}
