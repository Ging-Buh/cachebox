package de.droidcachebox.gdx.activities;

import org.json.JSONArray;
import org.json.JSONObject;

import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.settings.CB_Core_Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.http.Webb;

public class SearchCoordinates extends ActivityBase {
    private CB_Button btnOK;
    private CB_Button btnCancel;
    private ScrollBox scrollBox;
    private EditTextField edtCity;
    private EditTextField edtStreet;

    public SearchCoordinates() {
        super("SearchCoordinates");
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

        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            btnOK.disable();
            GL.that.postAsync(() -> {
                JSONArray fetchedLocations = fetchLocations();
                Menu menuLocation;
                if (fetchedLocations.length() > 0) {
                    menuLocation = new Menu("LocationMenuTitle") {
                        public void onHide() {
                            btnOK.enable();
                        }
                    };
                    menuLocation.onHide();
                    for (int ii = 0; ii < fetchedLocations.length(); ii++) {
                        JSONObject jPoi = (JSONObject) fetchedLocations.get(ii);
                        String description = jPoi.optString("display_name", "");
                        description = description.replace(",", "\n");
                        Coordinate pos = new Coordinate(jPoi.optDouble("lat", 0), jPoi.optDouble("lon", 0));
                        menuLocation.addMenuItem("", description, null, () -> {
                            callBack(pos);
                            // finish();
                        });
                    }
                    menuLocation.show();
                } else {
                    btnOK.enable();
                }
            });
            return true;
        });

        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            finish();
            return true;
        });

    }

    public void callBack(Coordinate coordinate) {
    }

    public void doShow() {
        show();
    }

    public void doFinish() {
        finish();
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