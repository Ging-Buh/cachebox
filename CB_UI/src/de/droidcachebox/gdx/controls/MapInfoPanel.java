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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import de.droidcachebox.core.LiveMapQue;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.MapViewBase;
import de.droidcachebox.settings.CB_UI_Settings;
import de.droidcachebox.utils.UnitFormatter;

public class MapInfoPanel extends CB_View_Base {

    private final MapViewBase parentMapView;
    private Image compass_frame;
    private Image compas_scale;
    private Image arrow;
    private Image CoordSymbol;
    private CB_Label lblSpeed;
    private CB_Label lblDistance;
    private CB_Label lblLatitude;
    private CB_Label lblLongitude;
    private Coordinate aktCoord;
    private CoordType lastCoordType = CoordType.NULL;
    private float aktDistance = -1;
    private float aktHeading = 0;
    private float aktBearing = 0;
    private boolean lastUsedCompass = Locator.getInstance().UseMagneticCompass();

    public MapInfoPanel(CB_RectF rec, String Name, MapViewBase parentMapView) {
        super(rec, Name);
        registerSkinChangedEvent();
        this.parentMapView = parentMapView;

    }

    public void setCoord(Coordinate Coord) {
        if (Coord != null && lblLatitude != null && lblLongitude != null) {
            if (aktCoord == null || !aktCoord.equals(Coord)) {
                aktCoord = Coord;
                try {
                    lblLatitude.setText(UnitFormatter.FormatLatitudeDM(Coord.getLatitude()));
                    lblLongitude.setText(UnitFormatter.FormatLongitudeDM(Coord.getLongitude()));
                } catch (Exception ignored) {
                }

                if (CB_UI_Settings.liveMapEnabled.getValue() && !parentMapView.isCarMode())
                    LiveMapQue.getInstance().quePosition(Coord);

                GL.that.renderOnce();
            }

        }
    }

    public void setCoordType(CoordType type) {
        if (CoordSymbol == null) {
            // store type in lastCoordType to be initialized later
            lastCoordType = type;
            return;
        }
        if (lastCoordType != type) {
            lastCoordType = type;
            switch (type) {
                case Cache:
                    CoordSymbol.setDrawable(new SpriteDrawable(Sprites.getSprite("cache-icon")));
                    break;
                case GPS:
                    CoordSymbol.setDrawable(new SpriteDrawable(Sprites.getSprite("satellite")));
                    break;
                case Map:
                    CoordSymbol.setDrawable(new SpriteDrawable(Sprites.getSprite("map")));
                    break;
                case NULL:
                    CoordSymbol.setDrawable(null);
                    break;
            }
        }
    }

    public void setSpeed(String speed) {

        if (lblSpeed == null)
            return;
        if (lblSpeed.getText().equals(speed))
            return;

        lblSpeed.setText(speed);
    }

    public void setDistance(float distance) {
        if (lblDistance == null)
            return;
        if (aktDistance == distance)
            return;
        aktDistance = distance;
        try {
            if (distance == -1)
                lblDistance.setText("?");
            else
                lblDistance.setText(UnitFormatter.distanceString(distance));
            GL.that.renderOnce();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBearing(float Heading, float Bearing) {
        if ((aktHeading == Heading) && (aktBearing == Bearing))
            return;
        aktHeading = Heading;
        aktBearing = Bearing;
        if (arrow != null && compas_scale != null) {
            setArrowDrawable();
            arrow.setRotate(-Heading);
            compas_scale.setRotate(Bearing);
            GL.that.renderOnce();
        }
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);
    }

    @Override
    protected void initialize() {
        removeChilds();

        setBackground(Sprites.infoBack);

        // initial Image

        CB_RectF CompassRec = new CB_RectF(0, 0, getHeight(), getHeight());

        compass_frame = new Image(CompassRec, "Compass_Frame", false);
        compass_frame.setDrawable(Sprites.Compass.get(2));
        compass_frame.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
        compass_frame.setScale(0.80f);
        addChild(compass_frame);

        compas_scale = new Image(CompassRec, "Compass_Scale", false);
        compas_scale.setDrawable(Sprites.Compass.get(3));
        compas_scale.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
        compas_scale.setScale(0.80f);
        addChild(compas_scale);

        arrow = new Image(CompassRec, "Compass_Arrow", false);
        setArrowDrawable(true);
        arrow.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
        arrow.setScale(0.50f);
        addChild(arrow);

        float margin = GL_UISizes.margin;

        lblSpeed = new CB_Label(scaleCenter(0.4f));
        lblSpeed.setFont(Fonts.getSmall());
        lblSpeed.setPos(CompassRec.getWidth() + margin, getHeight() * 0.1f);
        lblSpeed.setText("---");
        addChild(lblSpeed);

        lblDistance = new CB_Label(scaleCenter(0.4f));
        lblDistance.setFont(Fonts.getBig());
        lblDistance.setPos(CompassRec.getWidth() + margin, CompassRec.getWidth() / 2);
        lblDistance.setText("---");
        addChild(lblDistance);

        lblLatitude = new CB_Label(scaleCenter(0.4f));
        lblLatitude.setFont(Fonts.getSmall());
        lblLatitude.setPos(getWidth() - lblLatitude.getWidth() - rightBorder, CompassRec.getWidth() / 2);
        lblLatitude.setText("---");
        addChild(lblLatitude);

        lblLongitude = new CB_Label(scaleCenter(0.4f));
        lblLongitude.setFont(Fonts.getSmall());
        lblLongitude.setPos(getWidth() - lblLongitude.getWidth() - rightBorder, getHeight() * 0.1f);
        lblLongitude.setText("---");
        addChild(lblLongitude);

        CoordSymbol = new Image((new CB_RectF(0, 0, getHeight(), getHeight())).scaleCenter(0.62f), "CoordSymbol", false);
        CoordSymbol.setX(getWidth() - CoordSymbol.getWidth() - (rightBorder / 3));
        CoordSymbol.setDrawable(new SpriteDrawable(Sprites.getSprite("cache-icon")));
        addChild(CoordSymbol);
        CoordType tmp = lastCoordType;
        lastCoordType = CoordType.NULL;
        setCoordType(tmp);
    }

    private void setArrowDrawable() {
        setArrowDrawable(false);
    }

    private void setArrowDrawable(boolean forceSet) {
        boolean tmp = Locator.getInstance().UseMagneticCompass();
        if (!forceSet && tmp == lastUsedCompass)
            return;// no change required
        lastUsedCompass = tmp;
        int arrowId = 0;
        if (lastUsedCompass) {
            arrowId = 0;
        } else {
            arrowId = 2;
        }
        Sprite arrowSprite = new Sprite(Sprites.Arrows.get(arrowId));
        arrowSprite.setRotation(0);// reset rotation
        arrowSprite.setOrigin(0, 0);
        arrow.setDrawable(new SpriteDrawable(arrowSprite));
    }

    @Override
    protected void skinIsChanged() {
        initialize();
    }

    public enum CoordType {
        NULL, GPS, Cache, Map
    }

}
