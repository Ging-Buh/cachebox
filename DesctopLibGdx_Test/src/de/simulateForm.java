package de;

import java.awt.Button;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.Locator.Locator;
import CB_Core.Map.Descriptor.TrackPoint;
import CB_Core.Map.RouteOverlay;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.Color;

public class simulateForm extends Frame implements ActionListener, WindowListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3001260409970853805L;

	private Label lblGPX;
	private TextField txt;
	private Button pushButton5;

	public simulateForm(String s)
	{
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

		File dir = new File(Config.settings.TrackFolder.getValue());
		String[] files = dir.list();
		if (!(files == null))
		{
			if (files.length > 0)
			{
				for (String file : files)
				{
					if (file.equalsIgnoreCase("simulation.gpx"))
					{
						// Simmulations GPX gefunden Punkte Laden

						file = Config.settings.TrackFolder.getValue() + "/" + file;
						loadSimulateRoute(file);
					}
				}
			}
		}

	}

	@Override
	public void windowActivated(WindowEvent arg0)
	{

	}

	@Override
	public void windowClosed(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("Load GPX"))
		{
			FileDialog filedia = new FileDialog(this, "Öffnen");
			// filedia.setDirectory(initialPath);
			filedia.setFile("*.gpx");
			filedia.show();
			String filename = filedia.getDirectory() + filedia.getFile();
			if (filename != null)
			{
				loadSimulateRoute(filename);
			}
			filedia.dispose();
		}

		else if (event.getActionCommand().equals("Send GPS Signal"))
		{
			// Parse Coordinate
			Coordinate pos = new Coordinate(txt.getText());
			if (pos != null)
			{
				Locator Loc = new Locator();
				Bearing += 5;
				Loc.setLocation(pos.Latitude, pos.Longitude, 100, true, 175, true, Bearing, 95, "GPS");
				PositionChangedEventList.PositionChanged(Loc);
			}

		}

		else if (event.getActionCommand().equals("Start simulate"))
		{
			simulateGpsWithGpxFile();
			pushButton5.setLabel("Stop simulate");

		}

		else if (event.getActionCommand().equals("Stop simulate"))
		{
			BreakSimulate = true;
			pushButton5.setLabel("Start simulate");

		}

	}

	private static int Bearing = 45;

	private void loadSimulateRoute(String Path)
	{

		simulationRoute = RouteOverlay.LoadRoute(Path, Color.BLACK, 0);

		// TODO set GPX File Name to lblGPX
		if (simulationRoute != null && simulationRoute.Name != null)
		{
			trackPointIndex = 0;
			int idx = simulationRoute.FileName.lastIndexOf("\\");

			String Name = "";

			if (idx == -1) Name = simulationRoute.FileName;
			else
				Name = simulationRoute.FileName.substring(idx + 1);

			lblGPX.setText(Name);
		}
		else
		{
			lblGPX.setText("");
		}

		this.layout();
	}

	private static RouteOverlay.Track simulationRoute = null;

	private static void simulateGpsWithGpxFile()
	{

		if (simulationRoute != null)
		{
			// Run simulation
			BreakSimulate = false;
			runSimulation();

		}

	}

	private static int trackPointIndex = 0;
	private static int trackPointIndexEnd = 0;
	private static Date trackStartTime;

	private static boolean BreakSimulate = false;

	private static void runSimulation()
	{

		if (BreakSimulate) return;

		trackPointIndexEnd = simulationRoute.Points.size() - 1;
		trackStartTime = simulationRoute.Points.get(trackPointIndex).TimeStamp;
		long nextTimeStamp = (simulationRoute.Points.get(trackPointIndex + 1).TimeStamp.getTime() - simulationRoute.Points
				.get(trackPointIndex).TimeStamp.getTime());

		nextTimeStamp /= 8; // ein wenig schneller ablaufen lassen

		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				TrackPoint trk = simulationRoute.Points.get(trackPointIndex);
				Coordinate pos = new Coordinate(trk.Y, trk.X);
				Locator Loc = new Locator();

				Loc.setLocation(pos.Latitude, pos.Longitude, 100, true, 50, true, (float) trk.Direction, 95, "GPS");
				PositionChangedEventList.PositionChanged(Loc);

				if (trackPointIndex < trackPointIndexEnd - 2)
				{
					trackPointIndex++;
					runSimulation();
				}
			}
		};
		timer.schedule(task, nextTimeStamp);

	}

}
