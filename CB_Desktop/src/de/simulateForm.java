package de;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.locator.CBLocation;
import de.droidcachebox.locator.CBLocation.ProviderType;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.GPS;
import de.droidcachebox.locator.GpsStateChangeEventList;
import de.droidcachebox.locator.GpsStrength;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.menu.menuBtn3.executes.TrackList;
import de.droidcachebox.menu.menuBtn3.executes.TrackListView;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.FileFactory;

public class simulateForm extends Frame implements ActionListener, WindowListener {

    /**
     *
     */
    private static final long serialVersionUID = -3001260409970853805L;
    private static Checkbox chekRealSpeed;
    private static float speed = 50;
    private static int Bearing = 45;
    private static Track simulationRoute = null;
    private static int trackPointIndex = 0;
    private static int trackPointIndexEnd = 0;
    private static boolean BreakSimulate = false;
    private static boolean NetworkSend = false;
    private final Label lblGPX;
    private final TextField txt, speedTxt;
    private final Button pushButton5, sendSpeed;

    public simulateForm(String s) {
        super(s);
        setBackground(java.awt.Color.LIGHT_GRAY);
        setLayout(new FlowLayout());
        addWindowListener(this); // listen for events on this Window

        Label lbl = new Label("Coord:");
        add(lbl);

        txt = new TextField();
        txt.addActionListener(this);
        txt.setText("N 52 27.130  E 13 33.117");
        txt.setSize(200, 30);
        add(txt);

        Button pushButton = new Button("Send GPS Signal");
        add(pushButton);
        pushButton.addActionListener(this); // listen for Button press

        lblGPX = new Label("simulation.gpx               ");
        add(lblGPX);

        Button pushButton4 = new Button("Load GPX");
        add(pushButton4);
        pushButton4.addActionListener(this); // listen for Button press

        pushButton5 = new Button("Start simulate");
        add(pushButton5);
        pushButton5.addActionListener(this); // listen for Button press

        chekRealSpeed = new Checkbox("Simulate real speed");
        add(chekRealSpeed);

        speedTxt = new TextField();
        speedTxt.addActionListener(this);
        speedTxt.setText(String.valueOf(speed));
        speedTxt.setSize(200, 30);
        add(speedTxt);

        sendSpeed = new Button("Set Speed");
        add(sendSpeed);
        sendSpeed.addActionListener(this); // listen for Button press

        AbstractFile dir = FileFactory.createFile(Settings.TrackFolder.getValue());
        String[] files = dir.list();
        if (!(files == null)) {
            if (files.length > 0) {
                for (String file : files) {
                    if (file.equalsIgnoreCase("simulation.gpx")) {
                        // Simmulations GPX gefunden Punkte Laden

                        file = Settings.TrackFolder.getValue() + "/" + file;
                        loadSimulateRoute(file);
                    }
                }
            }
        }

    }

    private static void simulateGpsWithGpxFile() {

        if (simulationRoute != null) {
            // Run simulation
            BreakSimulate = false;
            runSimulation();

        }

    }

    private static void runSimulation() {

        if (BreakSimulate) {
            if (!NetworkSend) {

                NetworkSend = true;
            }

            return;
        }

        NetworkSend = false;

        trackPointIndexEnd = simulationRoute.getTrackPoints().size() - 1;
        long nextTimeStamp = (simulationRoute.getTrackPoints().get(trackPointIndex + 1).date.getTime() - simulationRoute.getTrackPoints().get(trackPointIndex).date.getTime());

        if (!chekRealSpeed.getState())
            nextTimeStamp /= 8; // ein wenig schneller ablaufen lassen?

        if (nextTimeStamp < 0)
            nextTimeStamp = 10;

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                TrackPoint trk = simulationRoute.getTrackPoints().get(trackPointIndex);
                Coordinate pos = new CoordinateGPS(trk.y, trk.x);
                Locator.getInstance().setNewLocation(new CBLocation(pos.getLatitude(), pos.getLongitude(), 100, true, speed, true, (float) trk.direction, 95, ProviderType.GPS));

                DesktopMain.compassheading = (float) trk.direction;

                if (trackPointIndex < trackPointIndexEnd - 2) {
                    trackPointIndex++;
                    runSimulation();
                }
            }
        };
        timer.schedule(task, nextTimeStamp);

    }

    @Override
    public void windowActivated(WindowEvent arg0) {

    }

    @Override
    public void windowClosed(WindowEvent arg0) {

    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
    }

    @Override
    public void windowOpened(WindowEvent arg0) {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("Load GPX")) {
            FileDialog filedia = new FileDialog(this, "öffnen");
            // filedia.setDirectory(initialPath);
            filedia.setFile("*.gpx");
            filedia.show();
            String filename = filedia.getDirectory() + filedia.getFile();
            loadSimulateRoute(filename);
            filedia.dispose();
        } else if (event.getActionCommand().equals("Send GPS Signal")) {
            // Parse Coordinate
            Coordinate pos = new CoordinateGPS(txt.getText());

            Bearing += 5;

            Locator.getInstance().setNewLocation(new CBLocation(pos.getLatitude(), pos.getLongitude(), 100, true, 2, true, Bearing, 95, ProviderType.GPS));

            CB_List<GpsStrength> satList = new CB_List<GpsStrength>(8);

            satList.add(new GpsStrength(true, 120));
            satList.add(new GpsStrength(true, 100));
            satList.add(new GpsStrength(true, 75));
            satList.add(new GpsStrength(true, 50));
            satList.add(new GpsStrength(true, 30));
            satList.add(new GpsStrength(true, 10));
            satList.add(new GpsStrength(false, 0));
            satList.add(new GpsStrength(false, 10));
            GPS.setSatList(satList);
            GpsStateChangeEventList.Call();

        } else if (event.getActionCommand().equals("Start simulate")) {
            NetworkSend = false;
            simulateGpsWithGpxFile();
            pushButton5.setLabel("Stop simulate");

        } else if (event.getActionCommand().equals("Stop simulate")) {
            BreakSimulate = true;
            pushButton5.setLabel("Start simulate");

        } else if (event.getActionCommand().equals("Set Speed")) {

            float d = Float.parseFloat(speedTxt.getText());

            speed = d / 3600 * 1000;

        }
    }

    @SuppressWarnings("deprecation")
    private void loadSimulateRoute(String path) {

        TrackListView.getInstance().readFromGpxFile(FileFactory.createFile(path));
        // !!! one file may have more than one route : get last added
        simulationRoute = TrackList.getInstance().getTrack(TrackList.getInstance().getNumberOfTracks() - 1);
        // Don't display loaded simulate route
        simulationRoute.setVisible(false);

        // set GPX File Name to lblGPX
        if (simulationRoute != null && simulationRoute.getName() != null) {
            trackPointIndex = 0;
            int idx = simulationRoute.getFileName().lastIndexOf("\\");

            String Name = "";

            if (idx == -1)
                Name = simulationRoute.getFileName();
            else
                Name = simulationRoute.getFileName().substring(idx + 1);

            lblGPX.setText(Name);
        } else {
            lblGPX.setText("");
        }

        this.layout();
    }
}
