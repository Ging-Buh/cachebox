package de.droidcachebox.gdx.activities;

import com.badlogic.gdx.graphics.Color;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.EditTextFieldBase.TextFieldListener;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.converter.UTMConvert;
import de.droidcachebox.utils.log.Log;

import java.io.FileOutputStream;

public class EditCoord extends ActivityBase {
    private static final String log = "EditCoord";
    private final UTMConvert convert = new UTMConvert();
    private final EditTextField invisibleTextField = new EditTextField(this, "invisibleTextField");
    private final String utmTest = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    CB_Button Leertaste; // additional to numeric input (for "deleting" input)
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
    OnClickListener mtbClicked = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            MultiToggleButton mtb = (MultiToggleButton) v;
            mtb.setState(1);
            showPage((Integer) mtb.getTag());
            return true;
        }
    };
    OnClickListener bDecDoubleClicked = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            // cause doubleclick always follows a simple click (in current ACB implementation),
            // the next 3 lines are not necessary.
            // I leave them, to be correct, if implemtation is changed sometime
            MultiToggleButton mtb = (MultiToggleButton) v;
            mtb.setState(1);
            showPage((Integer) mtb.getTag());
            // write a file for Nissan Connect
            // LON,LAT,Waypoint
            String dirFileName = Config.mWorkPath + "/User/nissan.csv";
            File txtFile = FileFactory.createFile(dirFileName);
            FileOutputStream writer;
            try {
                String coordsToWrite = coord.getLongitude() + "," + coord.getLatitude() + ",";
                writer = txtFile.getFileOutputStream();
                writer.write(("LON,LAT,Waypoint\r\n").getBytes("UTF-8"));
                if (GlobalCore.getSelectedWaypoint() == null) {
                    writer.write((coordsToWrite + GlobalCore.getSelectedCache().getGcCode() + "\r\n").getBytes("UTF-8"));
                } else {
                    writer.write((coordsToWrite + GlobalCore.getSelectedWaypoint().getGcCode() + "\r\n").getBytes("UTF-8"));
                }
                writer.flush();
                writer.close();
            } catch (Exception e) {
            }
            return true;
        }
    };

    public EditCoord(CB_RectF rec, String Name, Coordinate mActCoord, ReturnListener returnListener) {
        super(rec, Name);
        coord = mActCoord;
        cancelCoord = coord.copy();
        mReturnListener = returnListener;

        bDec = new MultiToggleButton("bDec");
        bMin = new MultiToggleButton("bMin");
        bSec = new MultiToggleButton("bSec");
        bUtm = new MultiToggleButton("bUtm");
        this.addNext(bDec);
        this.addNext(bMin);
        this.addNext(bSec);
        this.addLast(bUtm);
        bDec.initialOn_Off_ToggleStates("Dec", "Dec");
        bMin.initialOn_Off_ToggleStates("Min", "Min");
        bSec.initialOn_Off_ToggleStates("Sec", "Sec");
        bUtm.initialOn_Off_ToggleStates("UTM", "UTM");

        CB_Button btnOK = new CB_Button("btnOK");
        CB_Button btnCancel = new CB_Button("btnCancel");
        this.initRow(BOTTOMUP);
        this.addNext(btnOK);
        this.addLast(btnCancel);
        btnCancel.setText(Translation.get("cancel"));
        btnOK.setText(Translation.get("ok"));

        pnlNumPad = new Box(innerWidth, this.getAvailableHeight(), "pnlNumPad");
        this.createNumPad(pnlNumPad);
        this.addLast(pnlNumPad);

        this.pnlD = new Box(innerWidth, this.getAvailableHeight(), "pnlD");
        this.createD(this.pnlD);
        this.addLast(this.pnlD);

        pnlDM = new Box(pnlD.copy(), "pnlDM");
        this.createDM(this.pnlDM);
        this.addChild(pnlDM);

        pnlDMS = new Box(pnlD.copy(), "pnlDMS");
        this.createDMS(this.pnlDMS);
        this.addChild(pnlDMS);

        pnlUTM = new Box(pnlD.copy(), "pnlUTM");
        this.createUTM(pnlUTM);
        this.addChild(pnlUTM);

        btnOK.setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (!parseView()) {
                    GL.that.Toast("Invalid COORD", 2000);
                    return true;
                }

                if (mReturnListener != null) {
                    GL.that.RunOnGL(() -> finish());

                    mReturnListener.returnCoord(coord);
                } else {
                    GL.that.RunOnGL(() -> finish());
                }
                return true;
            }
        });
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (mReturnListener != null) {
                GL.that.closeActivity();
                mReturnListener.returnCoord(cancelCoord);
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

        this.btnDLat = new CB_Button[9]; // N_48[.]46270[°]
        this.btnDLon = new CB_Button[9]; // E009[.]28468[°]
        for (int i = 0; i < 9; i++) {
            this.btnDLat[i] = new CB_Button(this, "btnDLat" + i);
            this.btnDLon[i] = new CB_Button(this, "btnDLon" + i);
        }

        // Lat
        for (int i = 0; i < 4; i++) {
            panel.addNext(this.btnDLat[i]);
        }
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f); // [.]
        for (int i = 4; i < 9; i++) {
            panel.addNext(this.btnDLat[i]);
        }
        panel.addLast(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        // Lon
        for (int i = 0; i < 4; i++) {
            panel.addNext(this.btnDLon[i]);
        }
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        for (int i = 4; i < 9; i++) {
            panel.addNext(this.btnDLon[i]);
        }
        panel.addLast(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        this.setClickHandlers(this.btnDLat, this.btnDLon);
    }

    private void createDM(Box panel) {

        this.btnDMLat = new CB_Button[9]; // N_48[°]29[.]369
        this.btnDMLon = new CB_Button[9]; // E__9[°]15[.]807
        for (int i = 0; i < 9; i++) {
            this.btnDMLat[i] = new CB_Button(this, "btnDMLat" + i);
            this.btnDMLon[i] = new CB_Button(this, "btnDMLon" + i);
        }

        // Lat
        for (int i = 0; i < 4; i++) {
            panel.addNext(this.btnDMLat[i]);
        }
        panel.addNext(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMLat[4]);
        panel.addNext(this.btnDMLat[5]);
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMLat[6]);
        panel.addNext(this.btnDMLat[7]);
        panel.addLast(this.btnDMLat[8]);
        // Lon
        for (int i = 0; i < 4; i++) {
            panel.addNext(this.btnDMLon[i]);
        }
        panel.addNext(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMLon[4]);
        panel.addNext(this.btnDMLon[5]);
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMLon[6]);
        panel.addNext(this.btnDMLon[7]);
        panel.addLast(this.btnDMLon[8]);

        this.setClickHandlers(this.btnDMLat, this.btnDMLon);
    }

    private void createDMS(Box panel) {

        this.btnDMSLat = new CB_Button[10]; // N_48[°]28[']56[.]16["]
        this.btnDMSLon = new CB_Button[10]; // E__9[°]19[']40[.]14["]
        for (int i = 0; i < 10; i++) {
            this.btnDMSLat[i] = new CB_Button(this, "btnDMSLat" + i);
            this.btnDMSLon[i] = new CB_Button(this, "btnDMSLon" + i);
        }

        // Lat
        for (int i = 0; i < 4; i++) {
            panel.addNext(this.btnDMSLat[i]);
        }
        panel.addNext(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMSLat[4]);
        panel.addNext(this.btnDMSLat[5]);
        panel.addNext(new CB_Label("'").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMSLat[6]);
        panel.addNext(this.btnDMSLat[7]);
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMSLat[8]);
        panel.addLast(this.btnDMSLat[9]);
        // leave it because of small screen size
        // panel.addLast(new Label("\"").setFont(Fonts.getBig()));

        // Lon
        for (int i = 0; i < 4; i++) {
            panel.addNext(this.btnDMSLon[i]);
        }
        panel.addNext(new CB_Label("°").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMSLon[4]);
        panel.addNext(this.btnDMSLon[5]);
        panel.addNext(new CB_Label("'").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMSLon[6]);
        panel.addNext(this.btnDMSLon[7]);
        panel.addNext(new CB_Label(".").setFont(Fonts.getBig()), 0.5f);
        panel.addNext(this.btnDMSLon[8]);
        panel.addLast(this.btnDMSLon[9]);
        // leave it because of small screen size
        // panel.addLast(new Label("\"").setFont(Fonts.getBig()));

        this.setClickHandlers(this.btnDMSLat, this.btnDMSLon);
    }

    private void createUTM(Box panel) {

        this.btnUTMLat = new CB_Button[8]; // N < 10,000,000
        this.btnUTMLon = new CB_Button[8]; // E > 160,000 and < 834,000 (2 unsichtbar)
        this.btnUTMZone = new CB_Button[4]; // Zone 2stellig + 1 unsichtbar

        for (int i = 0; i < 8; i++) {
            this.btnUTMLat[i] = new CB_Button(this, "btnUTMLat" + i);
            this.btnUTMLon[i] = new CB_Button(this, "btnUTMLon" + i);
        }
        for (int i = 0; i < 4; i++) {
            this.btnUTMZone[i] = new CB_Button(this, "btnUTMZone" + i);
        }

        // Lon
        Box pnlOstwert = new Box(panel.getInnerWidth(), this.btnUTMLon[0].getHeight(), "pnlOstwert");
        panel.addNext(new CB_Label("OstW"), -0.2f);// no Translation
        panel.addLast(pnlOstwert); // erst die Breite bestimmen und dann darauf verteilen
        for (int i = 0; i < 7; i++) {
            pnlOstwert.addNext(this.btnUTMLon[i]);
        }
        pnlOstwert.addLast(this.btnUTMLon[7]);

        // Lat
        Box pnlNordwert = new Box(panel.getInnerWidth(), this.btnUTMLat[0].getHeight(), "pnlNordwert");
        pnlNordwert.adjustHeight();
        panel.addNext(new CB_Label("NordW"), -0.2f);// no Translation
        panel.addLast(pnlNordwert); // erst die Breite bestimmen und dann darauf verteilen
        for (int i = 0; i < 7; i++) {
            pnlNordwert.addNext(this.btnUTMLat[i]);
        }
        pnlNordwert.addLast(this.btnUTMLat[7]);

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

        this.setUTMClickHandlers(this.btnUTMLat, this.btnUTMLon, this.btnUTMZone);
    }

    private void setClickHandlers(CB_Button[] bLat, CB_Button[] bLon) {
        // N/S
        bLat[0].setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                CB_Button btn = (CB_Button) v;
                if (btn.getText().equals("N"))
                    btn.setText("S");
                else
                    btn.setText("N");
                return true;
            }
        });
        // E/W
        bLon[0].setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                CB_Button btn = (CB_Button) v;
                if (btn.getText().equals("E"))
                    btn.setText("W");
                else
                    btn.setText("E");
                return true;
            }
        });

        for (int i = 1; i < bLat.length; i++) // must have same length for Lat and Lon
        {
            bLat[i].setClickHandler(new OnClickListener() {
                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    CB_Button btn = (CB_Button) v;
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
                }
            });
            bLon[i].setClickHandler(new OnClickListener() {
                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    CB_Button btn = (CB_Button) v;
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
                }
            });
        }
    }

    private void setUTMClickHandlers(CB_Button[] bLat, CB_Button[] bLon, CB_Button[] bZone) {
        // the clicked button accepts the next input from Numpad

        for (int i = 0; i < bLat.length; i++) {
            bLat[i].setClickHandler(new OnClickListener() {
                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    CB_Button btn = (CB_Button) v;
                    // Focus setzen;
                    EditCoord parent = (EditCoord) btn.getParent();
                    // Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
                    int l = btn.getName().length() - 1;
                    int f = Integer.parseInt(btn.getName().substring(l)); // 0..7
                    parent.setUTMFocus(f + 6); // 6..13
                    return true;
                }
            });
        }

        for (int i = 0; i < (bLon.length - 2); i++) {
            bLon[i].setClickHandler(new OnClickListener() {
                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    CB_Button btn = (CB_Button) v;
                    // Focus setzen;
                    EditCoord parent = (EditCoord) btn.getParent();
                    // Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
                    int l = btn.getName().length() - 1;
                    int f = Integer.parseInt(btn.getName().substring(l)); // 0..5
                    parent.setUTMFocus(f); // 0..5
                    return true;
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            bZone[i].setClickHandler(new OnClickListener() {
                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    CB_Button btn = (CB_Button) v;
                    // Focus setzen;
                    EditCoord parent = (EditCoord) btn.getParent();
                    // Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
                    int l = btn.getName().length() - 1;
                    int f = Integer.parseInt(btn.getName().substring(l)); // 0..2
                    parent.setUTMFocus(f + 8 + 6); // 14,15,16
                    return true;
                }
            });
        }

    }

    private void createNumPad(Box panel) {
        // NumPad for edit of the Lat- / Lon- Buttons
        CB_Button[] btnNumpad;
        btnNumpad = new CB_Button[10];
        CB_Button dummy1 = new CB_Button("dummy1");
        dummy1.setInvisible();
        Leertaste = new CB_Button(this, "Leertaste");
        Leertaste.setInvisible();

        for (int i = 0; i < 10; i++) {
            btnNumpad[i] = new CB_Button(this, "btnNumpad" + i);
            btnNumpad[i].setClickHandler(new OnClickListener() {
                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    CB_Button btn = (CB_Button) v;
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
                }
            });

        }

        Leertaste.setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                CB_Button btn = (CB_Button) v;
                EditCoord parent = (EditCoord) btn.getParent();
                numPadtoUTMButton(parent, "");
                return true;
            }
        });

        panel.initRow(BOTTOMUP);
        panel.addNext(dummy1); // dummy links
        panel.addNext(btnNumpad[0]);
        panel.addLast(Leertaste); // Leertaste rechts, nur bei UTM-Eingabe sichtbar
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
            btnNumpad[i].setText(String.format("%1d", i));
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
        if (this.aktPage == newPage)
            return;

        if (aktPage >= 0) {
            parseView(); // setting coord
        }

        this.pnlD.setInvisible();
        this.pnlDM.setInvisible();
        this.pnlDMS.setInvisible();
        this.pnlUTM.setInvisible();
        this.pnlNumPad.setVisible();
        // keyboard ausblenden
        GL.that.setFocusedEditTextField(null);
        this.Leertaste.setInvisible();

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
                    s = s + String.format("%09.5f", Math.abs(coord.getLatitude())).replace(",", ".").replace(".", "");
                    for (int i = 0; i < 9; i++) {
                        btnDLat[i].setText(s.substring(i, (i + 1)));
                    }
                    btnDLat[1].setInvisible(); // nur 2 Stellen Grad
                    // Lon
                    if (coord.getLongitude() >= 0)
                        s = "E";
                    else
                        s = "W";
                    s = s + String.format("%09.5f", Math.abs(coord.getLongitude())).replace(",", ".").replace(".", "");
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

                    s = s + String.format("%03d", (int) deg);
                    s = s + String.format("%02d", (int) min);
                    s = s + String.format("%03d", (int) (0.5 + (min - (int) min) * 1000)); // gerundet
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
                    s = s + String.format("%03d", (int) deg);
                    s = s + String.format("%02d", (int) min);
                    s = s + String.format("%03d", (int) (0.5 + (min - (int) min) * 1000)); // gerundet
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

                    s = s + String.format("%03d", (int) deg);
                    s = s + String.format("%02d", imin);
                    s = s + String.format("%02d", (int) sec);
                    s = s + String.format("%02d", (int) (0.5 + (sec - (int) sec) * 100)); // gerundet
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
                    s = s + String.format("%03d", (int) deg);
                    s = s + String.format("%02d", imin);
                    s = s + String.format("%02d", (int) sec);
                    s = s + String.format("%02d", (int) (0.5 + (sec - (int) sec) * 100)); // gerundet
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
                    Leertaste.setVisible();

                    convert.iLatLon2UTM(coord.getLatitude(), coord.getLongitude());
                    String nording = String.format("%d", (int) (convert.UTMNorthing + 0.5f));
                    String easting = String.format("%d", (int) (convert.UTMEasting + 0.5f));
                    String zone = String.format("%02d", convert.iUTM_Zone_Num);
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
        if (this.focus < nrOfButtons) {
            bLat[this.focus].setText(bLat[this.focus].getText());
        } else {
            bLon[this.focus - nrOfButtons].setText(bLon[this.focus - nrOfButtons].getText());
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
        int nextFocus = this.focus + 1;
        if (nextFocus == bLat.length)
            nextFocus = this.focusStartLon; // jump
        // TODO action if behind last : autosave or beginfirst (discuss)
        if (nextFocus == bLat.length + bLon.length)
            nextFocus = 2;
        this.focus = setFocus(bLat, bLon, nextFocus);
    }

    private void setUTMFocus(int newFocus) {
        setUTMbtnTextColor(this.focus, COLOR.getFontColor());
        setUTMbtnTextColor(newFocus, COLOR.getHighLightFontColor());
        if (newFocus == 6 + 8 + 3 - 1) {
            // keyboard einblenden
            GL.that.setFocusedEditTextField(invisibleTextField);
            invisibleTextField.setTextFieldListener(new TextFieldListener() {

                @Override
                public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight) {
                }

                @Override
                public void keyTyped(EditTextFieldBase textField, char key) {
                    String k = String.valueOf(key).toUpperCase();
                    if (utmTest.contains(k)) {
                        btnUTMZone[2].setText(k);
                        setNextUTMFocus();
                    }

                }
            });
            // Numpad ausblenden
            this.pnlNumPad.setInvisible();
        } else {
            // keyboard ausblenden
            GL.that.setFocusedEditTextField(null);
            // Numpad einblenden
            this.pnlNumPad.setVisible();
        }
        this.focus = newFocus;
    }

    private void setUTMbtnTextColor(int f, Color c) {
        if (f < 6) {
            this.btnUTMLon[f].setText(this.btnUTMLon[f].getText(), c);
        } else {
            f = f - 6;
            if (f < 8) {
                this.btnUTMLat[f].setText(this.btnUTMLat[f].getText(), c);
            } else {
                f = f - 8;
                this.btnUTMZone[f].setText(this.btnUTMZone[f].getText(), c);
            }
        }
    }

    private void setNextUTMFocus() {
        int nextFocus = this.focus + 1;
        if (nextFocus >= 6 + 8 + 3)
            nextFocus = 0;
        setUTMFocus(nextFocus);
    }

    private boolean parseView() {
        String sCoord = "";
        switch (aktPage) {
            case 0:
                sCoord = this.btnDLat[0].getText() + this.btnDLat[2].getText() + this.btnDLat[3].getText() + ".";
                for (int i = 4; i < 9; i++)
                    sCoord += this.btnDLat[i].getText();
                sCoord += " " + this.btnDLon[0].getText() + this.btnDLon[1].getText() + this.btnDLon[2].getText() + this.btnDLon[3].getText() + ".";
                for (int i = 4; i < 9; i++)
                    sCoord += this.btnDLon[i].getText();
                break;
            case 1:
                sCoord = this.btnDMLat[0].getText(); // N/S
                sCoord += this.btnDMLat[2].getText() + this.btnDMLat[3].getText() + "\u00B0 "; // Deg
                sCoord += this.btnDMLat[4].getText() + this.btnDMLat[5].getText() + "."; // Min 1
                sCoord += this.btnDMLat[6].getText() + this.btnDMLat[7].getText() + this.btnDMLat[8].getText() + "\u0027 "; // Min 2
                sCoord += this.btnDMLon[0].getText(); // W/E
                sCoord += this.btnDMLon[1].getText() + this.btnDMLon[2].getText() + this.btnDMLon[3].getText() + "\u00B0 "; // Deg
                sCoord += this.btnDMLon[4].getText() + this.btnDMLon[5].getText() + "."; // Min 1
                sCoord += this.btnDMLon[6].getText() + this.btnDMLon[7].getText() + this.btnDMLon[8].getText() + "\u0027"; // Min 2
                break;
            case 2:
                sCoord = this.btnDMSLat[0].getText(); // N/S
                sCoord += this.btnDMSLat[2].getText() + this.btnDMSLat[3].getText() + "\u00B0 "; // Deg
                sCoord += this.btnDMSLat[4].getText() + this.btnDMSLat[5].getText() + "\u0027 "; // Min
                sCoord += this.btnDMSLat[6].getText() + this.btnDMSLat[7].getText() + "."; // Sec 1
                sCoord += this.btnDMSLat[8].getText() + this.btnDMSLat[9].getText() + "\" "; // Sec 2
                sCoord += this.btnDMSLon[0].getText(); // W/E
                sCoord += this.btnDMSLon[1].getText() + this.btnDMSLon[2].getText() + this.btnDMSLon[3].getText() + "\u00B0 "; // Deg
                sCoord += this.btnDMSLon[4].getText() + this.btnDMSLon[5].getText() + "\u0027 "; // Min
                sCoord += this.btnDMSLon[6].getText() + this.btnDMSLon[7].getText() + "."; // Sec 1
                sCoord += this.btnDMSLon[8].getText() + this.btnDMSLon[9].getText() + "\""; // Sec 2
                break;
            case 3:
                sCoord = "";
                for (int i = 0; i < 3; i++) {
                    sCoord += this.btnUTMZone[i].getText();
                }
                sCoord += " ";
                for (int i = 0; i < this.btnUTMLon.length; i++) {
                    if (this.btnUTMLon.length > 0)
                        sCoord += this.btnUTMLon[i].getText();
                }
                sCoord += " ";
                for (int i = 0; i < this.btnUTMLat.length; i++) {
                    if (this.btnUTMLat.length > 0)
                        sCoord += this.btnUTMLat[i].getText();
                }
                sCoord = sCoord.replace("null", ""); // TODO ??? warum kommt hier der Text "null" von getText() ?
                break;
        }

        CoordinateGPS newCoord = new CoordinateGPS(sCoord);
        Log.info(log, "Buttons of aktPage " + aktPage + " = '" + sCoord + "'" + " --> " + newCoord.FormatCoordinate());
        if (newCoord.isValid()) {
            coord = newCoord;
            return true;
        } else
            return false;
    }

    public interface ReturnListener {
        public void returnCoord(Coordinate coord);
    }
}
