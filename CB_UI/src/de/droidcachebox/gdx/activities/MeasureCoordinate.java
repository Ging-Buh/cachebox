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
package de.droidcachebox.gdx.activities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.SatBarChart;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.CBLocation.ProviderType;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.PointD;
import de.droidcachebox.utils.UnitFormatter;

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
    private final ICoordReturnListener mCoordReturnListener;
    private final AtomicBoolean inRepaint = new AtomicBoolean(false);
    private boolean redraw = true;

    public MeasureCoordinate(String Name, ICoordReturnListener listener) {
        super(Name);
        mCoordReturnListener = listener;

        MeasuredCoordList.MeasuredCoord.Referenz = Locator.getInstance().getMyPosition(ProviderType.GPS);

        if (MeasuredCoordList.MeasuredCoord.Referenz == null) {
            MeasuredCoordList.MeasuredCoord.Referenz = new CoordinateGPS(0, 0);
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

        bOK.setClickHandler((v, x, y, pointer, button) -> {
            if (mCoordReturnListener != null) {
                synchronized (mMeasureList) {
                    GL.that.RunOnGL(() -> mCoordReturnListener.returnCoord(mMeasureList.getAccuWeightedAverageCoord()));
                }
            }
            finish();
            return true;
        });

        bCancel.setClickHandler((v, x, y, pointer, button) -> {
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
    public void finish() {
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

        CB_RectF panelRec = new CB_RectF(leftBorder, bOK.getMaxY(), innerWidth);

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
                    double medianLat = MeasuredCoordList.MeasuredCoord.Referenz.getLatitude();
                    double medianLon = MeasuredCoordList.MeasuredCoord.Referenz.getLongitude();

                    MeasuredCoordList sortetdList = (MeasuredCoordList) mMeasureList.clone();
                    sortetdList.sort();

                    double peakLat = Math.max(Math.abs(sortetdList.get(0).getLatitude() - medianLat), Math.abs(sortetdList.get(sortetdList.size() - 1).getLatitude() - medianLat));
                    double peakLon = Math.max(Math.abs(sortetdList.get(0).getLongitude() - medianLon), Math.abs(sortetdList.get(sortetdList.size() - 1).getLongitude() - medianLon));

                    // Umrechnung in XY
                    double medianX = Descriptor.longitudeToTileX(projectionZoom, medianLon);
                    double medianY = Descriptor.latitudeToTileY(projectionZoom, medianLat);

                    double extremeX = Descriptor.longitudeToTileX(projectionZoom, peakLon + medianLon);
                    double extremeY = Descriptor.latitudeToTileY(projectionZoom, peakLat + medianLat);

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

                        PointD lastDrawEntry = projectCoordinate(mMeasureList.get(i - 1).getLatitude(), mMeasureList.get(i - 1).getLongitude(), projectionZoom);

                        int lastX = (int) (centerX + (lastDrawEntry.x - medianX) * factor);
                        int lastY = (int) (centerY - (lastDrawEntry.y - medianY) * factor);

                        PointD thisDrawEntry = projectCoordinate(mMeasureList.get(i).getLatitude(), mMeasureList.get(i).getLongitude(), projectionZoom);

                        x = (int) (centerX + (thisDrawEntry.x - medianX) * factor);
                        y = (int) (centerY - (thisDrawEntry.y - medianY) * factor);

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

    /**
     * Projiziert die übergebene Koordinate in den Tile Space
     *
     * @param latitude       Breitengrad
     * @param longitude      Längengrad
     * @param projectionZoom zoom
     * @return PointD
     */
    private PointD projectCoordinate(double latitude, double longitude, int projectionZoom) {
        double latRad = latitude * MathUtils.DEG_RAD;
        return new PointD((longitude + 180.0) / 360.0 * Math.pow(2, projectionZoom),
                (1 - Math.log(Math.tan(latRad) + (1.0 / Math.cos(latRad))) / Math.PI) / 2 * Math.pow(2, projectionZoom));
    }

    @Override
    public void positionChanged() {
        synchronized (mMeasureList) {

            if (MeasureCount == 0)
                lblMeasureCoord.setText("");

            MeasureCount++;
            mMeasureList.add(new MeasuredCoordList.MeasuredCoord(Locator.getInstance().getLocation(ProviderType.GPS).toCordinate()));

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
        PositionChangedListeners.addListener(this);
        if (chart != null) {
            chart.onShow();
            chart.setDrawWithAlpha(false);
            PlatformUIBase.switchToGpsMeasure();
        }
    }

    @Override
    public void onHide() {
        PositionChangedListeners.removeListener(this);
        if (chart != null)
            chart.onHide();
        PlatformUIBase.switchToGpsDefault();
    }

    @Override
    public void orientationChanged() {
    }

    @Override
    public Priority getPriority() {
        return Priority.Normal;
    }

    @Override
    public void speedChanged() {
    }

    public interface ICoordReturnListener {
        void returnCoord(Coordinate coord);
    }
    /**
     * Eine ArrayList<MeasuredCoord> welche die gemessenen Koordinaten aufnimmt, sortiert, Ausreißer eliminiert und über die Methode
     * "getMeasuredAverageCoord()" eine Durchschnitts Koordinate zurück gibt.
     *
     * @author Longri
     */
    private static class MeasuredCoordList extends ArrayList<MeasuredCoordList.MeasuredCoord> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * Gibt die Durchschnittliche Koordinate dieser Liste zurück.
         *
         * @return Coordinate
         */
        Coordinate getMeasuredAverageCoord() {

            Coordinate ret;

            if (this.size() == 0) {
                ret = new CoordinateGPS(0, 0);
                ret.setValid(false);

                return ret;
            }

            synchronized (this) {
                Iterator<MeasuredCoord> iterator = this.iterator();

                double sumLatitude = 0;
                double sumLongitude = 0;

                do {
                    MeasuredCoord tmp = iterator.next();
                    sumLatitude += tmp.getLatitude();
                    sumLongitude += tmp.getLongitude();
                } while (iterator.hasNext());

                ret = new CoordinateGPS(sumLatitude / this.size(), sumLongitude / this.size());
                ret.setValid(true);

            }

            return ret;
        }

        /**
         * Gibt die Durchschnittliche Koordinate dieser Liste zurück. Wobei die Genauigkeit der gemessenen Koordinaten berücksichtigt wird!
         *
         * @return Coordinate
         */
        Coordinate getAccuWeightedAverageCoord() {
            // berechne Coord nach Genauigkeits Wichtung
            return getMeasuredAverageCoord(); // Vorerst, bis die Wichtung fertig
            // ist!
        }

        /**
         * überschreibt die add Methode um bei einer Listen Größe > 3 <br>
         * die MeasuredCoord.Referenz auf den Durchschnitt der Liste zu setzen.
         */
        @Override
        public boolean add(MeasuredCoord measuredCoord) {
            boolean ret;

            synchronized (this) {
                ret = super.add(measuredCoord);

                if (this.size() > 3) {
                    MeasuredCoord.Referenz = this.getMeasuredAverageCoord();
                }
            }
            return ret;
        }

        /**
         * Sortiert die Koordinaten nach Entfernung zu MeasuredCoord.Referenz welche im ersten Schritt auf den Durchschnitt gesetzt wird.
         */
        public void sort() {
            synchronized (this) {
                MeasuredCoord.Referenz = this.getMeasuredAverageCoord();
                Collections.sort(this);
            }
        }

        /**
         * Setzt die Statisch Referenz Koordinate von MeasuredCoord auf die errechnete durchnitliche Koordinate
         */
        void setAverage() {
            MeasuredCoord.Referenz = this.getMeasuredAverageCoord();
        }

        /**
         * Löscht die Ausreißer Werte, welche eine Distanz von mehr als 3m zur Referenz Koordinate haben.
         */
        void clearDiscordantValue() {
            boolean ready;
            synchronized (this) {
                do {
                    ready = true;

                    this.setAverage();
                    Iterator<MeasuredCoord> iterator = this.iterator();
                    do {
                        MeasuredCoord tmp = iterator.next();
                        if (tmp.Distance(MathUtils.CalculationType.ACCURATE) > 3) {
                            this.remove(tmp);
                            ready = false;
                            break;
                        }
                    } while (iterator.hasNext());
                } while (!ready);
                this.setAverage();
            }
        }

        public String toString() {
            String ret = "";
            if (this.getAccuWeightedAverageCoord().isValid()) {
                ret = UnitFormatter.FormatLatitudeDM(this.getAccuWeightedAverageCoord().getLatitude()) + " / " + UnitFormatter.FormatLongitudeDM(this.getAccuWeightedAverageCoord().getLongitude());
            }

            return ret;
        }
        static class MeasuredCoord implements Comparable<MeasuredCoord> {
            /**
             * Die Referenz Coordinate, auf die sich der Vergleich bezieht. <br>
             * Je grösser der Abstand zur Referenz Coordinate desto höher der Index in einer Liste.
             */
            static Coordinate Referenz;
            private double Latitude;
            private double Longitude;
            private float Accuracy;

            MeasuredCoord(CoordinateGPS coord) {
                Latitude = coord.getLatitude();
                Longitude = coord.getLongitude();
                Accuracy = coord.getAccuracy();
            }

            /**
             * Gibt die Latitude dieser Koordinate zurück
             *
             * @return double
             */
            public double getLatitude() {
                return Latitude;
            }

            /**
             * Gibt die getLongitude dieser Koordinate zurück
             *
             * @return double
             */
            public double getLongitude() {
                return Longitude;
            }

            /**
             * Gibt die Genauigkeit dieser gemessenen Koordinate zurück!
             *
             * @return float
             */
            public float getAccuracy() {
                return Accuracy;
            }

            @Override
            public int compareTo(MeasuredCoord o2) {
                float dist1 = this.Distance(MathUtils.CalculationType.ACCURATE);
                float dist2 = o2.Distance(MathUtils.CalculationType.ACCURATE);

                float acc1 = this.Accuracy;
                float acc2 = o2.Accuracy;

                if (dist1 < dist2) {
                    return -1;
                } else if (dist1 == dist2) {
                    // Wenn die Distanzen gleich sind werden noch die Genauigkeitswerte
                    // verglichen!
                    return (Float.compare(acc1, acc2));
                } else {
                    return 1;
                }

            }

            /**
             * Gibt die Entfernung zur Referenz Position als Float zurück
             *
             * @return Entfernung zur übergebenen User Position als Float
             */
            public float Distance(MathUtils.CalculationType type) {
                float[] dist = new float[4];
                MathUtils.computeDistanceAndBearing(type, this.Latitude, this.Longitude, Referenz.getLatitude(), Referenz.getLongitude(), dist);

                return dist[0];
            }

        }
    }

}
