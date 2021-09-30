package de.droidcachebox.gdx.activities;

import com.badlogic.gdx.graphics.Color;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.EditTextField.TextFieldListener;
import de.droidcachebox.gdx.controls.MultiToggleButton;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.converter.UTMConvert;
import de.droidcachebox.utils.log.Log;

public class EditCoord extends ActivityBase {
    private static final String log = "EditCoord";
    private final UTMConvert convert = new UTMConvert();
    private final EditTextField invisibleTextField = new EditTextField(this, "invisibleTextField");
    private final String utmTest = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private CB_Button leerTaste; // additional to numeric input (for "deleting" input)
    private int aktPage = -1; // Deg-Min

    // Allgemein
    private Coordinate cancelCoord;
    private Coordinate coord;
    private ReturnListener mReturnListener;
    private MultiToggleButton bDec;
    private MultiToggleButton bMin;
    private MultiToggleButton bSec;
    private MultiToggleButton bUtm;
    private int focus; // Nr of Button for next Input
    // for UTM is : 0..5=Ostwert,6..13=Nordwert,14,15=zone,16=zoneletter
    private int focusStartLon; // jump to this first input digit on Lon - input
    private Box pnlNumPad;
    // Deg : N_48.46270° E009.28468°
    private Box pnlD;
    private CB_Button[] btnDLat;
    private CB_Button[] btnDLon;
    // Deg - Min : N_48°27.762' E009°17.081'
    private Box pnlDM;
    private CB_Button[] btnDMLat;
    private CB_Button[] btnDMLon;
    // Deg - Min - Sec : N_48°28'56.16" E009°19'40.14"
    private Box pnlDMS;
    private CB_Button[] btnDMSLat;
    private CB_Button[] btnDMSLon;
    // Utm
    private Box pnlUTM;
    private CB_Button[] btnUTMLat;
    private CB_Button[] btnUTMLon;
    private CB_Button[] btnUTMZone;
    private OnClickListener mtbClicked = (view, x, y, pointer, button) -> {
        MultiToggleButton mtb = (MultiToggleButton) view;
        mtb.setState(1);
        showPage((Integer) mtb.getTag());
        return true;
    };
    private OnClickListener bDecDoubleClicked = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
            // cause doubleclick always follows a simple click (in current ACB implementation),
            // the next 3 lines are not necessary.
            // I leave them, to be correct, if implemtation is changed sometime
            MultiToggleButton mtb = (MultiToggleButton) view;
            mtb.setState(1);
            showPage((Integer) mtb.getTag());
            // write a file for Nissan Connect
            // LON,LAT,Waypoint
            String dirFileName = Config.workPath + "/User/nissan.csv";
            AbstractFile txtAbstractFile = FileFactory.createFile(dirFileName);
            FileOutputStream writer;
            try {
                String coordsToWrite = coord.getLongitude() + "," + coord.getLatitude() + ",";
                writer = txtAbstractFile.getFileOutputStream();
                writer.write(("LON,LAT,Waypoint\r\n").getBytes(StandardCharsets.UTF_8));
                if (GlobalCore.getSelectedWayPoint() == null) {
                    writer.write((coordsToWrite + GlobalCore.getSelectedCache().getGeoCacheCode() + "\r\n").getBytes(StandardCharsets.UTF_8));
                } else {
                    writer.write((coordsToWrite + GlobalCore.getSelectedWayPoint().getWaypointCode() + "\r\n").getBytes(StandardCharsets.UTF_8));
                }
                writer.flush();
                writer.close();
            } catch (Exception ignored) {
            }
            return true;
        }
    };

    public EditCoord(String Name, Coordinate mActCoord, ReturnListener returnListener) {
        super(Name);
        coord = mActCoord;
        cancelCoord = coord.copy();
        mReturnListener = returnListener;

        bDec = new MultiToggleButton("bDec");
        bMin = new MultiToggleButton("bMin");
        bSec = new MultiToggleButton("bSec");
        bUtm = new MultiToggleButton("bUtm");
        addNext(bDec);
        addNext(bMin);
        addNext(bSec);
        addLast(bUtm);
        bDec.initialOn_Off_ToggleStates("Dec", "Dec");
        bMin.initialOn_Off_ToggleStates("Min", "Min");
        bSec.initialOn_Off_ToggleStates("Sec", "Sec");
        bUtm.initialOn_Off_ToggleStates("UTM", "UTM");

        CB_Button btnOK = new CB_Button("btnOK");
        CB_Button btnCancel = new CB_Button("btnCancel");
        initRow(BOTTOMUP);
        addNext(btnOK);
        addLast(btnCancel);
        btnCancel.setText(Translation.get("cancel"));
        btnOK.setText(Translation.get("ok"));

        pnlNumPad = new Box(innerWidth, getAvailableHeight(), "pnlNumPad");
        createNumPad(pnlNumPad);
        addLast(pnlNumPad);

        pnlD = new Box(innerWidth, getAvailableHeight(), "pnlD");
        createD(pnlD);
        addLast(pnlD);

        pnlDM = new Box(pnlD, "pnlDM");
        createDM(pnlDM);
        addChild(pnlDM);

        pnlDMS = new Box(pnlD, "pnlDMS");
        createDMS(pnlDMS);
        addChild(pnlDMS);

        pnlUTM = new Box(pnlD, "pnlUTM");
        createUTM(pnlUTM);
        addChild(pnlUTM);

        btnOK.setClickHandler((view, x, y, pointer, button) -> {
            if (!parseView()) {
                GL.that.toast("Invalid COORD");
                return true;
            }

            if (mReturnListener != null) {
                GL.that.RunOnGL(this::finish);

                mReturnListener.returnCoordinate(coord);
            } else {
                GL.that.RunOnGL(this::finish);
            }
            return true;
        });
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (mReturnListener != null) {
                GL.that.closeActivity();
                mReturnListener.returnCoordinate(cancelCoord);
            } else
                GL.that.closeActivity();
            return true;
        });

    }

    @Override
    protected void initialize() {

        bDec.setTag(0);
        bDec.setClickHandler(mtbClicked);
        bDec.setOnDoubleClickListener(bDecDoubleClicked);

        bMin.setTag(1);
        bMin.setClickHandler(mtbClicked);

        bSec.setTag(2);
        bSec.setClickHandler(mtbClicked);

        bUtm.setTag(3);
        bUtm.setClickHandler(mtbClicked);

        bMin.setState(1);
        showPage(1);

    }

    private void createD(Box panel) {

        btnDLat = new CB_Button[9]; // N_48[.]46270[°]
        btnDLon = new CB_Button[9]; // E009[.]28468[°]
        for (int i = 0; i < 9; i++) {
            btnDLat[i] = new CB_Button(this, "btnDLat" + i);
            btnDLon[i] = new CB_Button(this, "btnDLon" + i);
        }

        // Lat
        for (int i = 0; i < 4; i++) {
            panel.addNext(btnDLat[i]);
        }
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f); // [.]
        for (int i = 4; i < 9; i++) {
            panel.addNext(btnDLat[i]);
        }
        panel.addLast(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        // Lon
        for (int i = 0; i < 4; i++) {
            panel.addNext(btnDLon[i]);
        }
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        for (int i = 4; i < 9; i++) {
            panel.addNext(btnDLon[i]);
        }
        panel.addLast(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        setClickHandlers(btnDLat, btnDLon);
    }

    private void createDM(Box panel) {

        btnDMLat = new CB_Button[9]; // N_48[°]29[.]369
        btnDMLon = new CB_Button[9]; // E__9[°]15[.]807
        for (int i = 0; i < 9; i++) {
            btnDMLat[i] = new CB_Button(this, "btnDMLat" + i);
            btnDMLon[i] = new CB_Button(this, "btnDMLon" + i);
        }

        // Lat
        for (int i = 0; i < 4; i++) {
            panel.addNext(btnDMLat[i]);
        }
        panel.addNext(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMLat[4]);
        panel.addNext(btnDMLat[5]);
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMLat[6]);
        panel.addNext(btnDMLat[7]);
        panel.addLast(btnDMLat[8]);
        // Lon
        for (int i = 0; i < 4; i++) {
            panel.addNext(btnDMLon[i]);
        }
        panel.addNext(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMLon[4]);
        panel.addNext(btnDMLon[5]);
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMLon[6]);
        panel.addNext(btnDMLon[7]);
        panel.addLast(btnDMLon[8]);

        setClickHandlers(btnDMLat, btnDMLon);
    }

    private void createDMS(Box panel) {

        btnDMSLat = new CB_Button[10]; // N_48[°]28[']56[.]16["]
        btnDMSLon = new CB_Button[10]; // E__9[°]19[']40[.]14["]
        for (int i = 0; i < 10; i++) {
            btnDMSLat[i] = new CB_Button(this, "btnDMSLat" + i);
            btnDMSLon[i] = new CB_Button(this, "btnDMSLon" + i);
        }

        // Lat
        for (int i = 0; i < 4; i++) {
            panel.addNext(btnDMSLat[i]);
        }
        panel.addNext(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMSLat[4]);
        panel.addNext(btnDMSLat[5]);
        panel.addNext(new CB_Label("'").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMSLat[6]);
        panel.addNext(btnDMSLat[7]);
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMSLat[8]);
        panel.addLast(btnDMSLat[9]);
        // leave it because of small screen size
        // panel.addLast(new Label("\"").setFont(Fonts.getBig()));

        // Lon
        for (int i = 0; i < 4; i++) {
            panel.addNext(btnDMSLon[i]);
        }
        panel.addNext(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMSLon[4]);
        panel.addNext(btnDMSLon[5]);
        panel.addNext(new CB_Label("'").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMSLon[6]);
        panel.addNext(btnDMSLon[7]);
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(btnDMSLon[8]);
        panel.addLast(btnDMSLon[9]);
        // leave it because of small screen size
        // panel.addLast(new Label("\"").setFont(Fonts.getBig()));

        setClickHandlers(btnDMSLat, btnDMSLon);
    }

    private void createUTM(Box panel) {

        btnUTMLat = new CB_Button[8]; // N < 10,000,000
        btnUTMLon = new CB_Button[8]; // E > 160,000 and < 834,000 (2 unsichtbar)
        btnUTMZone = new CB_Button[4]; // Zone 2stellig + 1 unsichtbar

        for (int i = 0; i < 8; i++) {
            btnUTMLat[i] = new CB_Button(this, "btnUTMLat" + i);
            btnUTMLon[i] = new CB_Button(this, "btnUTMLon" + i);
        }
        for (int i = 0; i < 4; i++) {
            btnUTMZone[i] = new CB_Button(this, "btnUTMZone" + i);
        }

        // Lon
        Box pnlOstwert = new Box(panel.getInnerWidth(), btnUTMLon[0].getHeight(), "pnlOstwert");
        panel.addNext(new CB_Label("OstW"), -0.2f);// no Translation
        panel.addLast(pnlOstwert); // erst die Breite bestimmen und dann darauf verteilen
        for (int i = 0; i < 7; i++) {
            pnlOstwert.addNext(btnUTMLon[i]);
        }
        pnlOstwert.addLast(btnUTMLon[7]);

        // Lat
        Box pnlNordwert = new Box(panel.getInnerWidth(), btnUTMLat[0].getHeight(), "pnlNordwert");
        pnlNordwert.adjustHeight();
        panel.addNext(new CB_Label("NordW"), -0.2f);// no Translation
        panel.addLast(pnlNordwert); // erst die Breite bestimmen und dann darauf verteilen
        for (int i = 0; i < 7; i++) {
            pnlNordwert.addNext(btnUTMLat[i]);
        }
        pnlNordwert.addLast(btnUTMLat[7]);

        // Zone
        Box pnlZone = new Box(panel.getInnerWidth(), btnUTMZone[0].getHeight(), "pnlZone");
        panel.addNext(new CB_Label("Zone"), -0.2f);// no Translation
        panel.addLast(pnlZone); // erst die Breite bestimmen und dann darauf verteilen
        pnlZone.addNext(btnUTMZone[0]);
        pnlZone.addNext(btnUTMZone[1]);
        pnlZone.addNext(btnUTMZone[2]);
        pnlZone.addLast(btnUTMZone[3], 4f);

        btnUTMLon[6].setInvisible();
        btnUTMLon[7].setInvisible();
        btnUTMZone[3].setInvisible();

        setUTMClickHandlers(btnUTMLat, btnUTMLon, btnUTMZone);
    }

    private void setClickHandlers(CB_Button[] bLat, CB_Button[] bLon) {
        // N/S
        bLat[0].setClickHandler((view, x, y, pointer, button) -> {
            CB_Button btn = (CB_Button) view;
            if (btn.getText().equals("N"))
                btn.setText("S");
            else
                btn.setText("N");
            return true;
        });
        // E/W
        bLon[0].setClickHandler((view, x, y, pointer, button) -> {
            CB_Button btn = (CB_Button) view;
            if (btn.getText().equals("E"))
                btn.setText("W");
            else
                btn.setText("E");
            return true;
        });

        for (int i = 1; i < bLat.length; i++) // must have same length for Lat and Lon
        {
            bLat[i].setClickHandler((view, x, y, pointer, button) -> {
                CB_Button btn = (CB_Button) view;
                // Focus setzen;
                EditCoord parent = (EditCoord) btn.getParent();
                // Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
                int l = btn.getName().length() - 1;
                int f = Integer.parseInt(btn.getName().substring(l));
                switch (parent.aktPage) {
                    case 0:
                        parent.focus = parent.setFocus(parent.btnDLat, parent.btnDLon, f);
                        break;
                    case 1:
                        parent.focus = parent.setFocus(parent.btnDMLat, parent.btnDMLon, f);
                        break;
                    case 2:
                        parent.focus = parent.setFocus(parent.btnDMSLat, parent.btnDMSLon, f);
                        break;
                }
                return true;
            });
            bLon[i].setClickHandler((view, x, y, pointer, button) -> {
                CB_Button btn = (CB_Button) view;
                // Focus setzen;
                EditCoord parent = (EditCoord) btn.getParent();
                // Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
                int l = btn.getName().length() - 1;
                int f = Integer.parseInt(btn.getName().substring(l));
                switch (parent.aktPage) {
                    case 0:
                        parent.focus = parent.setFocus(parent.btnDLat, parent.btnDLon, f + 9);
                        break;
                    case 1:
                        parent.focus = parent.setFocus(parent.btnDMLat, parent.btnDMLon, f + 9);
                        break;
                    case 2:
                        parent.focus = parent.setFocus(parent.btnDMSLat, parent.btnDMSLon, f + 10);
                        break;
                }
                return true;
            });
        }
    }

    private void setUTMClickHandlers(CB_Button[] bLat, CB_Button[] bLon, CB_Button[] bZone) {
        // the clicked button accepts the next input from Numpad

        for (CB_Button cb_button : bLat) {
            cb_button.setClickHandler((view, x, y, pointer, button) -> {
                CB_Button btn = (CB_Button) view;
                // Focus setzen;
                EditCoord parent = (EditCoord) btn.getParent();
                // Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
                int l = btn.getName().length() - 1;
                int f = Integer.parseInt(btn.getName().substring(l)); // 0..7
                parent.setUTMFocus(f + 6); // 6..13
                return true;
            });
        }

        for (int i = 0; i < (bLon.length - 2); i++) {
            bLon[i].setClickHandler((view, x, y, pointer, button) -> {
                CB_Button btn = (CB_Button) view;
                // Focus setzen;
                EditCoord parent = (EditCoord) btn.getParent();
                // Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
                int l = btn.getName().length() - 1;
                int f = Integer.parseInt(btn.getName().substring(l)); // 0..5
                parent.setUTMFocus(f); // 0..5
                return true;
            });
        }

        for (int i = 0; i < 3; i++) {
            bZone[i].setClickHandler((view, x, y, pointer, button) -> {
                CB_Button btn = (CB_Button) view;
                // Focus setzen;
                EditCoord parent = (EditCoord) btn.getParent();
                // Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
                int l = btn.getName().length() - 1;
                int f = Integer.parseInt(btn.getName().substring(l)); // 0..2
                parent.setUTMFocus(f + 8 + 6); // 14,15,16
                return true;
            });
        }

    }

    private void createNumPad(Box panel) {
        // NumPad for edit of the Lat- / Lon- Buttons
        CB_Button[] btnNumpad;
        btnNumpad = new CB_Button[10];
        CB_Button dummy1 = new CB_Button("dummy1");
        dummy1.setInvisible();
        leerTaste = new CB_Button(this, "Leertaste");
        leerTaste.setInvisible();

        for (int i = 0; i < 10; i++) {
            btnNumpad[i] = new CB_Button(this, "btnNumpad" + i);
            btnNumpad[i].setClickHandler((view, x, y, pointer, button) -> {
                CB_Button btn = (CB_Button) view;
                EditCoord parent = (EditCoord) btn.getParent();
                switch (parent.aktPage) {
                    case 0:
                        if (parent.focus < 9) {
                            parent.btnDLat[parent.focus].setText(btn.getText());
                        } else {
                            parent.btnDLon[parent.focus - 9].setText(btn.getText());
                        }
                        parent.setNextFocus(parent.btnDLat, parent.btnDLon);
                        break;
                    case 1:
                        if (parent.focus < 9) {
                            parent.btnDMLat[parent.focus].setText(btn.getText());
                        } else {
                            parent.btnDMLon[parent.focus - 9].setText(btn.getText());
                        }
                        parent.setNextFocus(parent.btnDMLat, parent.btnDMLon);
                        break;
                    case 2:
                        if (parent.focus < 10) {
                            parent.btnDMSLat[parent.focus].setText(btn.getText());
                        } else {
                            parent.btnDMSLon[parent.focus - 10].setText(btn.getText());
                        }
                        parent.setNextFocus(parent.btnDMSLat, parent.btnDMSLon);
                        break;
                    case 3:
                        numPadtoUTMButton(parent, btn.getText());
                        break;
                }
                return true;
            });

        }

        leerTaste.setClickHandler((view, x, y, pointer, button) -> {
            CB_Button btn = (CB_Button) view;
            EditCoord parent = (EditCoord) btn.getParent();
            numPadtoUTMButton(parent, "");
            return true;
        });

        panel.initRow(BOTTOMUP);
        panel.addNext(dummy1); // dummy links
        panel.addNext(btnNumpad[0]);
        panel.addLast(leerTaste); // Leertaste rechts, nur bei UTM-Eingabe sichtbar
        panel.addNext(btnNumpad[7]);
        panel.addNext(btnNumpad[8]);
        panel.addLast(btnNumpad[9]);
        panel.addNext(btnNumpad[4]);
        panel.addNext(btnNumpad[5]);
        panel.addLast(btnNumpad[6]);
        panel.addNext(btnNumpad[1]);
        panel.addNext(btnNumpad[2]);
        panel.addLast(btnNumpad[3]);
        for (int i = 0; i < 10; i++) {
            btnNumpad[i].setText(String.format(Locale.US, "%1d", i));
        }
        panel.adjustHeight();
    }

    private void numPadtoUTMButton(EditCoord them, String value) {
        int f = them.focus;
        if (f < 6) // 0..5
        {
            them.btnUTMLon[f].setText(value);
        } else {
            f = f - 6; // 6..13
            if (f < 8) {
                them.btnUTMLat[f].setText(value);
            } else {
                f = f - 8; // 14,15 -- > 0,1 (16 -> 2 ist zoneletter)
                them.btnUTMZone[f].setText(value);
            }
        }
        them.setNextUTMFocus(); // weiter zum n°chsten Eingabebutton
    }

    private void showPage(int newPage) {
        if (aktPage == newPage)
            return;

        if (aktPage >= 0) {
            parseView(); // setting coord
        }

        pnlD.setInvisible();
        pnlDM.setInvisible();
        pnlDMS.setInvisible();
        pnlUTM.setInvisible();
        pnlNumPad.setVisible();
        // keyboard ausblenden
        GL.that.setFocusedEditTextField(null);
        leerTaste.setInvisible();

        setButtonValues(newPage);
        aktPage = newPage;
    }

    private void setButtonValues(final int newPage) {

        GL.that.RunOnGL(() -> {

            String s;
            switch (newPage) {
                case 0:
                    bDec.setState(1);
                    bMin.setState(0);
                    bSec.setState(0);
                    bUtm.setState(0);

                    // Lat
                    if (coord.getLatitude() >= 0)
                        s = "N";
                    else
                        s = "S";
                    s = s + String.format(Locale.US, "%09.5f", Math.abs(coord.getLatitude())).replace(",", ".").replace(".", "");
                    for (int i = 0; i < 9; i++) {
                        btnDLat[i].setText(s.substring(i, (i + 1)));
                    }
                    btnDLat[1].setInvisible(); // nur 2 Stellen Grad
                    // Lon
                    if (coord.getLongitude() >= 0)
                        s = "E";
                    else
                        s = "W";
                    s = s + String.format(Locale.US, "%09.5f", Math.abs(coord.getLongitude())).replace(",", ".").replace(".", "");
                    for (int i = 0; i < 9; i++) {
                        btnDLon[i].setText(s.substring(i, (i + 1)));
                    }

                    focus = setFocus(btnDLat, btnDLon, 4); // erste Nachkommastelle N / S
                    focusStartLon = 13;

                    pnlD.setVisible();

                    break;
                case 1:
                    bDec.setState(0);
                    bMin.setState(1);
                    bSec.setState(0);
                    bUtm.setState(0);

                    // Lat
                    if (coord.getLatitude() >= 0)
                        s = "N";
                    else
                        s = "S";
                    double deg = (int) Math.abs(coord.getLatitude());
                    double frac = Math.abs(coord.getLatitude()) - deg;
                    double min = frac * 60;

                    s = s + String.format(Locale.US, "%03d", (int) deg);
                    s = s + String.format(Locale.US, "%02d", (int) min);
                    s = s + String.format(Locale.US, "%03d", (int) (0.5 + (min - (int) min) * 1000)); // gerundet
                    for (int i = 0; i < 9; i++) {
                        btnDMLat[i].setText(s.substring(i, (i + 1)));
                    }
                    btnDMLat[1].setInvisible(); // nur 2 Stellen Grad
                    // Lon
                    if (coord.getLongitude() >= 0)
                        s = "E";
                    else
                        s = "W";
                    deg = (int) Math.abs(coord.getLongitude());
                    frac = Math.abs(coord.getLongitude()) - deg;
                    min = frac * 60;
                    s = s + String.format(Locale.US, "%03d", (int) deg);
                    s = s + String.format(Locale.US, "%02d", (int) min);
                    s = s + String.format(Locale.US, "%03d", (int) (0.5 + (min - (int) min) * 1000)); // gerundet
                    for (int i = 0; i < 9; i++) {
                        btnDMLon[i].setText(s.substring(i, (i + 1)));
                    }

                    focus = setFocus(btnDMLat, btnDMLon, 6); // erste Nachkommastelle N / S
                    focusStartLon = 15;

                    pnlDM.setVisible();

                    break;
                case 2:
                    bDec.setState(0);
                    bMin.setState(0);
                    bSec.setState(1);
                    bUtm.setState(0);

                    // Lat
                    if (coord.getLatitude() >= 0)
                        s = "N";
                    else
                        s = "S";

                    deg = (int) Math.abs(coord.getLatitude());
                    frac = Math.abs(coord.getLatitude()) - deg;
                    min = frac * 60;
                    int imin = (int) min;
                    frac = min - imin;
                    double sec = frac * 60;

                    s = s + String.format(Locale.US, "%03d", (int) deg);
                    s = s + String.format(Locale.US, "%02d", imin);
                    s = s + String.format(Locale.US, "%02d", (int) sec);
                    s = s + String.format(Locale.US, "%02d", (int) (0.5 + (sec - (int) sec) * 100)); // gerundet
                    for (int i = 0; i < 10; i++) {
                        btnDMSLat[i].setText(s.substring(i, (i + 1)));
                    }
                    btnDMSLat[1].setInvisible(); // nur 2 Stellen Grad

                    // Lon
                    if (coord.getLongitude() >= 0)
                        s = "E";
                    else
                        s = "W";
                    deg = (int) Math.abs(coord.getLongitude());
                    frac = Math.abs(coord.getLongitude()) - deg;
                    min = frac * 60;
                    imin = (int) min;
                    frac = min - imin;
                    sec = frac * 60;
                    s = s + String.format(Locale.US, "%03d", (int) deg);
                    s = s + String.format(Locale.US, "%02d", imin);
                    s = s + String.format(Locale.US, "%02d", (int) sec);
                    s = s + String.format(Locale.US, "%02d", (int) (0.5 + (sec - (int) sec) * 100)); // gerundet
                    for (int i = 0; i < 10; i++) {
                        btnDMSLon[i].setText(s.substring(i, (i + 1)));
                    }

                    focus = setFocus(btnDMSLat, btnDMSLon, 6); // erste Nachkommastelle N / S
                    focusStartLon = 16;
                    pnlDMS.setVisible();

                    break;
                case 3:
                    bDec.setState(0);
                    bMin.setState(0);
                    bSec.setState(0);
                    bUtm.setState(1);
                    leerTaste.setVisible();

                    convert.iLatLon2UTM(coord.getLatitude(), coord.getLongitude());
                    String nording = String.format(Locale.US, "%d", (int) (convert.UTMNorthing + 0.5f));
                    String easting = String.format(Locale.US, "%d", (int) (convert.UTMEasting + 0.5f));
                    String zone = String.format(Locale.US, "%02d", convert.iUTM_Zone_Num);
                    String UTMZoneLetter = convert.sUtmLetterActual(coord.getLatitude());
                    for (int i = 0; i < nording.length(); i++) {
                        btnUTMLat[i].setText(nording.substring(i, (i + 1)));
                    }
                    for (int i = nording.length(); i < 8; i++) {
                        btnUTMLat[i].setText("");
                    }
                    for (int i = 0; i < easting.length(); i++) {
                        btnUTMLon[i].setText(easting.substring(i, (i + 1)));
                    }
                    for (int i = easting.length(); i < 8; i++) {
                        btnUTMLon[i].setText("");
                    }
                    for (int i = 0; i < 2; i++) {
                        btnUTMZone[i].setText(zone.substring(i, (i + 1)));
                    }
                    btnUTMZone[2].setText(UTMZoneLetter);

                    setUTMFocus(0);

                    pnlUTM.setVisible();
                    //

                    break;
            }

        });

    }

    private int setFocus(CB_Button[] bLat, CB_Button[] bLon, int newFocus) {
        int nrOfButtons = bLat.length;
        // highlighted to normal
        if (focus < nrOfButtons) {
            bLat[focus].setText(bLat[focus].getText());
        } else {
            bLon[focus - nrOfButtons].setText(bLon[focus - nrOfButtons].getText());
        }
        // normal to highlighted showing next input change
        if (newFocus < nrOfButtons) {
            bLat[newFocus].setText(bLat[newFocus].getText(), COLOR.getHighLightFontColor());
        } else {
            bLon[newFocus - nrOfButtons].setText(bLon[newFocus - nrOfButtons].getText(), COLOR.getHighLightFontColor());
        }
        return newFocus;
    }

    private void setNextFocus(CB_Button[] bLat, CB_Button[] bLon) {
        int nextFocus = focus + 1;
        if (nextFocus == bLat.length)
            nextFocus = focusStartLon; // jump
        // ? discuss action if behind last : autosave or beginfirst
        if (nextFocus == bLat.length + bLon.length)
            nextFocus = 2;
        focus = setFocus(bLat, bLon, nextFocus);
    }

    private void setUTMFocus(int newFocus) {
        setUTMbtnTextColor(focus, COLOR.getFontColor());
        setUTMbtnTextColor(newFocus, COLOR.getHighLightFontColor());
        if (newFocus == 6 + 8 + 3 - 1) {
            // keyboard einblenden
            GL.that.setFocusedEditTextField(invisibleTextField);
            invisibleTextField.setTextFieldListener(new TextFieldListener() {

                @Override
                public void lineCountChanged(EditTextField textField, int lineCount, float textHeight) {
                }

                @Override
                public void keyTyped(EditTextField textField, char key) {
                    String k = String.valueOf(key).toUpperCase();
                    if (utmTest.contains(k)) {
                        btnUTMZone[2].setText(k);
                        setNextUTMFocus();
                    }

                }
            });
            // Numpad ausblenden
            pnlNumPad.setInvisible();
        } else {
            // keyboard ausblenden
            GL.that.setFocusedEditTextField(null);
            // Numpad einblenden
            pnlNumPad.setVisible();
        }
        focus = newFocus;
    }

    private void setUTMbtnTextColor(int f, Color c) {
        if (f < 6) {
            btnUTMLon[f].setText(btnUTMLon[f].getText(), c);
        } else {
            f = f - 6;
            if (f < 8) {
                btnUTMLat[f].setText(btnUTMLat[f].getText(), c);
            } else {
                f = f - 8;
                btnUTMZone[f].setText(btnUTMZone[f].getText(), c);
            }
        }
    }

    private void setNextUTMFocus() {
        int nextFocus = focus + 1;
        if (nextFocus >= 6 + 8 + 3)
            nextFocus = 0;
        setUTMFocus(nextFocus);
    }

    private boolean parseView() {
        StringBuilder sCoord = new StringBuilder();
        switch (aktPage) {
            case 0:
                sCoord = new StringBuilder(btnDLat[0].getText() + btnDLat[2].getText() + btnDLat[3].getText() + ".");
                for (int i = 4; i < 9; i++)
                    sCoord.append(btnDLat[i].getText());
                sCoord.append(" ").append(btnDLon[0].getText()).append(btnDLon[1].getText()).append(btnDLon[2].getText()).append(btnDLon[3].getText()).append(".");
                for (int i = 4; i < 9; i++)
                    sCoord.append(btnDLon[i].getText());
                break;
            case 1:
                sCoord = new StringBuilder(btnDMLat[0].getText()); // N/S
                sCoord.append(btnDMLat[2].getText()).append(btnDMLat[3].getText()).append("\u00B0 "); // Deg
                sCoord.append(btnDMLat[4].getText()).append(btnDMLat[5].getText()).append("."); // Min 1
                sCoord.append(btnDMLat[6].getText()).append(btnDMLat[7].getText()).append(btnDMLat[8].getText()).append("\u0027 "); // Min 2
                sCoord.append(btnDMLon[0].getText()); // W/E
                sCoord.append(btnDMLon[1].getText()).append(btnDMLon[2].getText()).append(btnDMLon[3].getText()).append("\u00B0 "); // Deg
                sCoord.append(btnDMLon[4].getText()).append(btnDMLon[5].getText()).append("."); // Min 1
                sCoord.append(btnDMLon[6].getText()).append(btnDMLon[7].getText()).append(btnDMLon[8].getText()).append("\u0027"); // Min 2
                break;
            case 2:
                sCoord = new StringBuilder(btnDMSLat[0].getText()); // N/S
                sCoord.append(btnDMSLat[2].getText()).append(btnDMSLat[3].getText()).append("\u00B0 "); // Deg
                sCoord.append(btnDMSLat[4].getText()).append(btnDMSLat[5].getText()).append("\u0027 "); // Min
                sCoord.append(btnDMSLat[6].getText()).append(btnDMSLat[7].getText()).append("."); // Sec 1
                sCoord.append(btnDMSLat[8].getText()).append(btnDMSLat[9].getText()).append("\" "); // Sec 2
                sCoord.append(btnDMSLon[0].getText()); // W/E
                sCoord.append(btnDMSLon[1].getText()).append(btnDMSLon[2].getText()).append(btnDMSLon[3].getText()).append("\u00B0 "); // Deg
                sCoord.append(btnDMSLon[4].getText()).append(btnDMSLon[5].getText()).append("\u0027 "); // Min
                sCoord.append(btnDMSLon[6].getText()).append(btnDMSLon[7].getText()).append("."); // Sec 1
                sCoord.append(btnDMSLon[8].getText()).append(btnDMSLon[9].getText()).append("\""); // Sec 2
                break;
            case 3:
                sCoord = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    sCoord.append(btnUTMZone[i].getText());
                }
                sCoord.append(" ");
                for (CB_Button cb_button : btnUTMLon) {
                    sCoord.append(cb_button.getText());
                }
                sCoord.append(" ");
                for (CB_Button cb_button : btnUTMLat) {
                    sCoord.append(cb_button.getText());
                }
                sCoord = new StringBuilder(sCoord.toString().replace("null", "")); // warum kommt hier der Text "null" von getText() ?
                break;
        }

        CoordinateGPS newCoord = new CoordinateGPS(sCoord.toString());
        Log.info(log, "Buttons of aktPage " + aktPage + " = '" + sCoord + "'" + " --> " + newCoord.formatCoordinate());
        if (newCoord.isValid()) {
            coord = newCoord;
            return true;
        } else
            return false;
    }

    public interface ReturnListener {
        void returnCoordinate(Coordinate coordinate);
    }
}
