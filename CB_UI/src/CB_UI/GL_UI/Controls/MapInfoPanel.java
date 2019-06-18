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
package CB_UI.GL_UI.Controls;

import CB_Core.Api.LiveMapQue;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Locator.Map.MapViewBase;
import CB_UI.CB_UI_Settings;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_Utils.Util.UnitFormatter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

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
    private boolean lastUsedCompass = Locator.UseMagneticCompass();

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

                if (CB_UI_Settings.LiveMapEnabeld.getValue() && !this.parentMapView.isCarMode())
                    LiveMapQue.quePosition(Coord);

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
                lblDistance.setText(UnitFormatter.DistanceString(distance));
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
    protected void Initial() {
        this.removeChilds();

        setBackground(Sprites.InfoBack);

        // initial Image

        CB_RectF CompassRec = new CB_RectF(0, 0, this.getHeight(), this.getHeight());

        compass_frame = new Image(CompassRec, "Compass_Frame", false);
        compass_frame.setDrawable(Sprites.Compass.get(2));
        compass_frame.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
        compass_frame.setScale(0.80f);
        this.addChild(compass_frame);

        compas_scale = new Image(CompassRec, "Compass_Scale", false);
        compas_scale.setDrawable(Sprites.Compass.get(3));
        compas_scale.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
        compas_scale.setScale(0.80f);
        this.addChild(compas_scale);

        arrow = new Image(CompassRec, "Compass_Arrow", false);
        setArrowDrawable(true);
        arrow.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
        arrow.setScale(0.50f);
        this.addChild(arrow);

        float margin = GL_UISizes.margin;

        lblSpeed = new CB_Label(ScaleCenter(0.4f));
        lblSpeed.setFont(Fonts.getSmall());
        lblSpeed.setPos(new Vector2(CompassRec.getWidth() + margin, this.getHeight() * 0.1f));
        lblSpeed.setText("---");
        this.addChild(lblSpeed);

        lblDistance = new CB_Label(ScaleCenter(0.4f));
        lblDistance.setFont(Fonts.getBig());
        lblDistance.setPos(new Vector2(CompassRec.getWidth() + margin, CompassRec.getWidth() / 2));
        lblDistance.setText("---");
        this.addChild(lblDistance);

        lblLatitude = new CB_Label(ScaleCenter(0.4f));
        lblLatitude.setFont(Fonts.getSmall());
        lblLatitude.setPos(new Vector2(this.getWidth() - lblLatitude.getWidth() - rightBorder, CompassRec.getWidth() / 2));
        lblLatitude.setText("---");
        this.addChild(lblLatitude);

        lblLongitude = new CB_Label(ScaleCenter(0.4f));
        lblLongitude.setFont(Fonts.getSmall());
        lblLongitude.setPos(new Vector2(this.getWidth() - lblLongitude.getWidth() - rightBorder, this.getHeight() * 0.1f));
        lblLongitude.setText("---");
        this.addChild(lblLongitude);

        CoordSymbol = new Image((new CB_RectF(0, 0, this.getHeight(), this.getHeight())).ScaleCenter(0.62f), "CoordSymbol", false);
        CoordSymbol.setX(this.getWidth() - CoordSymbol.getWidth() - (rightBorder / 3));
        CoordSymbol.setDrawable(new SpriteDrawable(Sprites.getSprite("cache-icon")));
        this.addChild(CoordSymbol);
        CoordType tmp = lastCoordType;
        lastCoordType = CoordType.NULL;
        setCoordType(tmp);
    }

    private void setArrowDrawable() {
        setArrowDrawable(false);
    }

    private void setArrowDrawable(boolean forceSet) {
        boolean tmp = Locator.UseMagneticCompass();
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
    protected void SkinIsChanged() {
        Initial();
    }

    public enum CoordType {
        NULL, GPS, Cache, Map
    }

}
