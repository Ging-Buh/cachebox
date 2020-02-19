/*
 * Copyright (c) 2007 by the University of Applied Sciences Northwestern Switzerland (FHNW)
 *
 * This program can be redistributed or modified under the terms of the
 * GNU General Public License as published by the Free Software Foundation.
 * This program is distributed without any warranty or implied warranty
 * of merchantability or fitness for a particular purpose.
 *
 * See the GNU General Public License for more details.
 */

package ch.fhnw.imvs.gpssimulator;

import ch.fhnw.imvs.gpssimulator.components.*;
import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.nmea.*;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.PlatformUIBase.IReturnAbstractFile;
import de.droidcachebox.PlatformUIBase.Methods;
import de.droidcachebox.database.SQLiteInterface;
import de.droidcachebox.settings.SettingBase;
import de.droidcachebox.utils.FileFactory;
import org.apache.log4j.Logger;
import org.mapsforge.map.swing.view.MapPanel;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class SimulatorMain {

    private static final UUID SERVICE_UUID = new UUID("0000110100001000800000805f9b34fb", false);
    private static final String SERVICE_NAME = "GPSSimulator";
    private static final String SERVICE_URL = "btspp://localhost:" + SERVICE_UUID + ";name=" + SERVICE_NAME + ";authorize=false;authenticate=false;encrypt=false";
    public static Preferences prefs = Preferences.userNodeForPackage(ch.fhnw.imvs.gpssimulator.SimulatorMain.class);
    private static StreamConnectionNotifier connectionNotifier;
    private static boolean closing = false;

    private static BluetoothPanel bluetooth;

    private static List<NMEASentence> nmeaTypes = new ArrayList<NMEASentence>();

    public static JFrame createFrame() throws IOException {
        JFrame f = new JFrame("GPS Simulator");
        f.setLayout(new BorderLayout());

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    closing = true;
                    connectionNotifier.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (NullPointerException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        nmeaTypes.add(new GGA());
        nmeaTypes.add(new GSA());
        nmeaTypes.add(new RMC());
        nmeaTypes.add(new GLL());

        box.add(new GeneralPanel(nmeaTypes)); // add general component
        box.add(new LocationPanel()); // add location component
        box.add(new CoursePanel()); // add course component
        box.add(new QualityPanel()); // add quality component
        box.add(new XMLPanel()); // add XML waypoints component

        bluetooth = new BluetoothPanel();
        box.add(bluetooth);

        JPanel rightbox = new JPanel();
        rightbox.setLayout(new BoxLayout(rightbox, BoxLayout.Y_AXIS));
        MapPanel mapPanel = new MapPanel();

        mapPanel.setVisible(true);
        rightbox.add(mapPanel);

        rightbox.setVisible(true);

        f.add(box, BorderLayout.WEST);
        f.add(rightbox, BorderLayout.EAST);

        JLabel title = new JLabel("GPS Simulator", JLabel.CENTER);
        title.setFont(new Font(null, Font.BOLD, 18));
        f.add(title, BorderLayout.NORTH);

        GPSData.start();

        PlatformUIBase.setMethods(new Methods() {

            @Override
            public SettingBase<?> readPlatformSetting(SettingBase<?> setting) {
                return null;
            }

            @Override
            public void writePlatformSetting(SettingBase<?> setting) {
            }

            @Override
            public boolean isOnline() {
                return false;
            }

            @Override
            public boolean isGPSon() {
                return false;
            }

            @Override
            public void vibrate() {

            }

            @Override
            public boolean isTorchAvailable() {
                return false;
            }

            @Override
            public boolean isTorchOn() {
                return false;
            }

            @Override
            public void switchTorch() {

            }

            @Override
            public void switchToGpsMeasure() {

            }

            @Override
            public void switchtoGpsDefault() {

            }

            @Override
            public void getApiKey() {

            }

            @Override
            public void callUrl(String url) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

                if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {

                    System.err.println("Desktop doesn't support the browse action (fatal)");
                    System.exit(1);
                }

                try {

                    java.net.URI uri = new java.net.URI(url);
                    desktop.browse(uri);
                } catch (Exception e) {

                    System.err.println(e.getMessage());
                }

            }

            @Override
            public void handleExternalRequest() {

            }

            @Override
            public String removeHtmlEntyties(String text) {
                return null;
            }

            @Override
            public void startPictureApp(String file) {

            }

            @Override
            public SQLiteInterface getSQLInstance() {
                return null;
            }

            @Override
            public void freeSQLInstance(SQLiteInterface sqlInstance) {
            }

            @Override
            public void getFile(String initialPath, final String extension, String TitleText, String ButtonText, IReturnAbstractFile returnListener) {

                final String ext = extension.replace("*", "");

                JFileChooser chooser = new JFileChooser();

                chooser.setCurrentDirectory(new java.io.File(initialPath));
                chooser.setDialogTitle(TitleText);

                FileFilter filter = new FileFilter() {

                    @Override
                    public String getDescription() {

                        return extension;
                    }

                    @Override
                    public boolean accept(File f) {
                        if (f.getAbsolutePath().endsWith(ext))
                            return true;
                        return false;
                    }
                };

                chooser.setFileFilter(filter);

                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    if (returnListener != null)
                        returnListener.returns(FileFactory.createFile(chooser.getSelectedFile().getAbsolutePath()));
                    System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
                }

            }

            @Override
            public void getFolder(String initialPath, String TitleText, String ButtonText, IReturnAbstractFile returnListener) {

                JFileChooser chooser = new JFileChooser();

                chooser.setCurrentDirectory(new java.io.File(initialPath));
                chooser.setDialogTitle(TitleText);

                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    if (returnListener != null)
                        returnListener.returns(FileFactory.createFile(chooser.getSelectedFile().getAbsolutePath()));
                    System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
                }

            }

            @Override
            public void quit() {
            }

        });

        return f;
    }

    public static void startListener() throws IOException {
        try {
            LocalDevice local = LocalDevice.getLocalDevice();

            System.out.println("Bluetooth address: " + local.getBluetoothAddress());

            connectionNotifier = (javax.microedition.io.StreamConnectionNotifier) Connector.open(SERVICE_URL);
            System.out.println("Service " + SERVICE_URL + " started.");

            while (true) {
                System.out.println("waiting for connections...");
                StreamConnection streamConnection = connectionNotifier.acceptAndOpen();

                System.out.println("Connection accepted.");
                ConnectionHandler connection = new ConnectionHandler(streamConnection);
                bluetooth.addConnection(connection);

                Thread t = new Thread(connection);
                t.start();
            }
        } catch (BluetoothStateException e) {
            System.out.println("Error: No Bluetooth device available.");
            // System.exit(1);
        } catch (Exception e) {
            if (!closing) {
                e.printStackTrace();
                // System.exit(1);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        JFrame f = createFrame();
        f.pack();
        f.setResizable(false);
        f.setVisible(true);

        // startListener();
    }

    public static class ConnectionHandler implements Runnable {

        static Logger log4j = Logger.getLogger("root");
        private volatile static int globalConnectionNumber = 0;
        private final StreamConnection streamConnection;
        private DataOutputStream dos = null;
        private volatile boolean running = true;
        private volatile int connectionNumber;

        public ConnectionHandler(StreamConnection streamConnection) {
            super();
            this.connectionNumber = ConnectionHandler.getGlobalConnectionNumber();
            this.streamConnection = streamConnection;
        }

        private synchronized static int getGlobalConnectionNumber() {
            return globalConnectionNumber++;
        }

        public int getConnectionNumber() {
            return connectionNumber;
        }

        @Override
        public String toString() {
            return "" + connectionNumber;
        }

        public void stopRunning() {
            running = false;
        }

        @Override
        public void run() {
            try {
                dos = new DataOutputStream(streamConnection.openDataOutputStream());

                while (running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    for (NMEASentence s : nmeaTypes) {
                        if (s.isLinePrinted()) {
                            writeString(s.getSentence());
                        }
                    }
                }
            } catch (IOException e1) {
                try {
                    streamConnection.close();
                } catch (IOException e2) {
                }
                bluetooth.removeConnection(this);

                System.err.println("Connector: " + e1);
                System.err.println("Removing this client from delivery!");
            }
        }

        private void writeString(String string) throws IOException {
            dos.write('$');
            dos.write(string.getBytes());
            dos.write('\r');
            dos.write('\n');
            dos.flush();
            log4j.info("$" + string);
        }
    }
}
