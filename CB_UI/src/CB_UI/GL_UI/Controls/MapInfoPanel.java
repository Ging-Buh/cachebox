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
import CB_UI.Settings.CB_UI_Settings;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_Utils.Util.UnitFormatter;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class MapInfoPanel extends CB_View_Base {

    private Image compass_frame;
    private Image compas_scale;
    private Image arrow;
    private Image CoordSymbol;
    private Label lblSpeed;
    private Label lblDistance;
    private Label lblLatitude;
    private Label lblLongitude;
    private Coordinate aktCoord;
    private CoordType lastCoordType = CoordType.NULL;

    private final MapViewBase parentMapView;

    public enum CoordType {
	NULL, GPS, Cache, Map
    }

    public void setCoord(Coordinate Coord) {
	if (Coord != null && lblLatitude != null && lblLongitude != null) {
	    if (aktCoord == null || !aktCoord.equals(Coord)) {
		aktCoord = Coord;
		lblLatitude.setText(UnitFormatter.FormatLatitudeDM(Coord.getLatitude()));
		lblLongitude.setText(UnitFormatter.FormatLongitudeDM(Coord.getLongitude()));

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
		CoordSymbol.setDrawable(new SpriteDrawable(SpriteCacheBase.getThemedSprite("cache-icon")));
		break;
	    case GPS:
		CoordSymbol.setDrawable(new SpriteDrawable(SpriteCacheBase.getThemedSprite("satellite")));
		break;
	    case Map:
		CoordSymbol.setDrawable(new SpriteDrawable(SpriteCacheBase.getThemedSprite("map")));
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

    private float aktDistance = -1;

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

    private float aktHeading = 0;
    private float aktBearing = 0;

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

    public MapInfoPanel(CB_RectF rec, String Name, MapViewBase parentMapView) {
	super(rec, Name);
	registerSkinChangedEvent();
	this.parentMapView = parentMapView;

    }

    @Override
    protected void Initial() {
	this.removeChilds();

	setBackground(SpriteCacheBase.InfoBack);

	// initial Image

	CB_RectF CompassRec = new CB_RectF(0, 0, this.getHeight(), this.getHeight());

	compass_frame = new Image(CompassRec, "Compass_Frame", false);
	compass_frame.setDrawable(SpriteCacheBase.Compass.get(2));
	compass_frame.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
	compass_frame.setScale(0.80f);
	this.addChild(compass_frame);

	compas_scale = new Image(CompassRec, "Compass_Scale", false);
	compas_scale.setDrawable(SpriteCacheBase.Compass.get(3));
	compas_scale.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
	compas_scale.setScale(0.80f);
	this.addChild(compas_scale);

	arrow = new Image(CompassRec, "Compass_Arrow", false);
	setArrowDrawable(true);
	arrow.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
	arrow.setScale(0.50f);
	this.addChild(arrow);

	float margin = GL_UISizes.margin;

	lblSpeed = new Label(this.ScaleCenter(0.4f), "lblSpeed");
	lblSpeed.setFont(Fonts.getSmall());
	lblSpeed.setPos(new Vector2(CompassRec.getWidth() + margin, this.getHeight() * 0.1f));
	lblSpeed.setText("---");
	this.addChild(lblSpeed);

	lblDistance = new Label(this.ScaleCenter(0.4f), "lblDistance");
	lblDistance.setFont(Fonts.getBig());
	lblDistance.setPos(new Vector2(CompassRec.getWidth() + margin, CompassRec.getWidth() / 2));
	lblDistance.setText("---");
	this.addChild(lblDistance);

	lblLatitude = new Label(this.ScaleCenter(0.4f), "lblLatitude");
	lblLatitude.setFont(Fonts.getSmall());
	lblLatitude.setPos(new Vector2(this.getWidth() - lblLatitude.getWidth() - rightBorder, CompassRec.getWidth() / 2));
	lblLatitude.setText("---");
	this.addChild(lblLatitude);

	lblLongitude = new Label(this.ScaleCenter(0.4f), "lblLongitude");
	lblLongitude.setFont(Fonts.getSmall());
	lblLongitude.setPos(new Vector2(this.getWidth() - lblLongitude.getWidth() - rightBorder, this.getHeight() * 0.1f));
	lblLongitude.setText("---");
	this.addChild(lblLongitude);

	CoordSymbol = new Image((new CB_RectF(0, 0, this.getHeight(), this.getHeight())).ScaleCenter(0.62f), "CoordSymbol", false);
	CoordSymbol.setX(this.getWidth() - CoordSymbol.getWidth() - (rightBorder / 3));
	CoordSymbol.setDrawable(new SpriteDrawable(SpriteCacheBase.getThemedSprite("cache-icon")));
	this.addChild(CoordSymbol);
	CoordType tmp = lastCoordType;
	lastCoordType = CoordType.NULL;
	setCoordType(tmp);
    }

    private boolean lastUsedCompass = Locator.UseMagneticCompass();

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
	Sprite arrowSprite = new Sprite(SpriteCacheBase.Arrows.get(arrowId));
	arrowSprite.setRotation(0);// reset rotation
	arrowSprite.setOrigin(0, 0);
	arrow.setDrawable(new SpriteDrawable(arrowSprite));
    }

    @Override
    protected void SkinIsChanged() {
	Initial();
    }

}
