package CB_UI.GL_UI.Activitys;

import CB_Core.CB_Core_Settings;
import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Controls.CoordinateButton;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_Utils.http.Webb;
import org.json.JSONArray;
import org.json.JSONObject;

public class SearchCoordinates extends ActivityBase {
    private CB_Button btnOK;
    private CB_Button btnCancel;
    private ScrollBox scrollBox;
    private EditTextField edtCity;
    private EditTextField edtStreet;
    private CoordinateButton.ICoordinateChangedListener callBack;

    public SearchCoordinates() {
        super(ActivityBase.ActivityRec(), "SearchCoordinates");
        createControls();
    }

    private void createControls() {
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        this.initRow(BOTTOMUP);
        this.addNext(btnOK);
        this.addLast(btnCancel);
        scrollBox = new ScrollBox(0, getAvailableHeight());
        scrollBox.setBackground(this.getBackground());
        this.addLast(scrollBox);
        Box box = new Box(scrollBox.getInnerWidth(), 0); // height will be adjusted after containing all controls
        scrollBox.addChild(box);

        CB_Label lblCity = new CB_Label(Translation.get("city"));
        lblCity.setWidth(Fonts.Measure(lblCity.getText()).width);
        box.addNext(lblCity, FIXED);
        edtCity = new EditTextField(this, "edtCity");
        box.addLast(edtCity);
        CB_Label lblStreet = new CB_Label(Translation.get("street"));
        lblStreet.setWidth(Fonts.Measure(lblStreet.getText()).width);
        box.addNext(lblStreet, FIXED);
        edtStreet = new EditTextField(this, "edtStreet");
        box.addLast(edtStreet);

        box.adjustHeight();
        scrollBox.setVirtualHeight(box.getHeight());

        btnOK.setOnClickListener((v, x, y, pointer, button) -> {
            btnOK.disable();
            GL.that.postAsync(() -> {
                JSONArray fetchedLocations = fetchLocations();
                Menu menuLocation;
                if (fetchedLocations.length() > 0) {
                    menuLocation = new Menu("LocationMenuTitle");
                    for (int ii = 0; ii < fetchedLocations.length(); ii++) {
                        JSONObject jPoi = (JSONObject) fetchedLocations.get(ii);
                        String description = jPoi.optString("display_name", "");
                        description = description.replace(",", "\n");
                        Coordinate pos = new Coordinate(jPoi.optDouble("lat", 0), jPoi.optDouble("lon", 0));
                        menuLocation.addMenuItem("", description, null, () -> {
                            callBack.coordinateChanged(pos);
                            finish();
                        });
                    }
                    menuLocation.show();
                }
            });
            return true;
        });

        btnCancel.setOnClickListener((v, x, y, pointer, button) -> {
            finish();
            return true;
        });

    }

    public void doShow(CoordinateButton.ICoordinateChangedListener callBack) {
        this.callBack = callBack;
        show();
    }

    private JSONArray fetchLocations() {
        return Webb.create()
                .get("https://nominatim.openstreetmap.org/search")
                .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                .param("city", edtCity.getText())
                .param("street", edtStreet.getText())
                .param("format", "json")
                .ensureSuccess()
                .asJsonArray()
                .getBody();
    }

    @Override
    public void dispose() {

    }
}