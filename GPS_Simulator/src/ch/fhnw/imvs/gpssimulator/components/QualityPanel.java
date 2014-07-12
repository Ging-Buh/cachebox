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

package ch.fhnw.imvs.gpssimulator.components;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.data.GPSData.FixType;
import ch.fhnw.imvs.gpssimulator.data.GPSData.Status;
import ch.fhnw.imvs.gpssimulator.data.GPSDataListener;

@SuppressWarnings("serial")
public class QualityPanel extends JPanel implements GPSDataListener
{

	// combo box for the states A / V
	private final JComboBox<Status> status = new JComboBox<Status>();

	// combo box for the number of satellites
	private final JComboBox<Integer> satellites = new JComboBox<Integer>();

	// combo box for the quality (invalid, GPS, DGPS, ...)
	private final JComboBox<String> quality = new JComboBox<String>();

	// combo box for the fix type (invalid, 2d, 3d)
	private final JComboBox<String> fixType = new JComboBox<String>();

	// spinners for DOP parameters
	private final JSpinner hdop = new JSpinner();
	private final JSpinner vdop = new JSpinner();
	private final JSpinner pdop = new JSpinner();

	// checkbox whether pdop should depend on hdop and vdop
	private final JCheckBox automaticPDOP = new JCheckBox();

	// checkbox whether status should depend on number of satellites
	private final JCheckBox automaticStatus = new JCheckBox();

	@Override
	public void valueChanged()
	{
		satellites.setSelectedIndex(GPSData.getSatellites());
		hdop.setValue(GPSData.getHDOP());
		vdop.setValue(GPSData.getVDOP());
		if (automaticPDOP.isSelected())
		{
			double hd = GPSData.getHDOP();
			double vd = GPSData.getVDOP();
			double pd = Math.round(10 * Math.sqrt(hd * hd + vd * vd)) / 10.0;
			pdop.setValue(pd);
			GPSData.setPDOP(pd);
		}
		else
		{
			pdop.setValue(GPSData.getPDOP());
		}
		if (automaticStatus.isSelected())
		{
			GPSData.Status s = GPSData.getSatellites() < 4 ? GPSData.Status.V : GPSData.Status.A;
			status.setSelectedItem(s);
			GPSData.setStatus(s);
		}
		else
		{
			status.setSelectedItem(GPSData.getStatus());
		}
		int q = GPSData.getQuality();
		quality.setSelectedIndex(q > 2 ? q - 3 : q);

		FixType ft = GPSData.getFixType();
		fixType.setSelectedIndex(ft.ordinal());
	}

	public QualityPanel()
	{
		GPSData.addChangeListener(this);
		this.setBorder(BorderFactory.createTitledBorder("Quality"));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel p1 = new JPanel(new FlowLayout());
		p1.add(new JLabel("Status:"));
		p1.add(status);
		p1.add(new JLabel("Bound to # of satellites?"));
		p1.add(automaticStatus);
		this.add(p1);

		status.addItem(GPSData.Status.A);
		status.addItem(GPSData.Status.V);
		status.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				GPSData.setStatus((GPSData.Status) status.getSelectedItem());
			}
		});

		automaticStatus.setSelected(false);
		automaticStatus.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				status.setEnabled(!automaticStatus.isSelected());
				valueChanged();
			}
		});

		JPanel p2 = new JPanel(new FlowLayout());
		p2.add(new JLabel("Satellites:", JLabel.RIGHT));
		p2.add(satellites);
		p2.add(new JLabel("Quality:", JLabel.RIGHT));
		p2.add(quality);
		this.add(p2);

		for (int i = 0; i <= 12; i++)
		{
			satellites.addItem(i);
		}
		satellites.setSelectedIndex(GPSData.getSatellites());
		satellites.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				GPSData.setSatellites(satellites.getSelectedIndex());
			}
		});

		p2.add(new JLabel("Fix-Type:", JLabel.RIGHT));
		p2.add(fixType);

		fixType.addItem("no fix"); // 0
		fixType.addItem("2D"); // 1
		fixType.addItem("3D"); // 2

		fixType.setSelectedIndex(2);
		fixType.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int index = fixType.getSelectedIndex();
				if (index == 0) GPSData.setFixType(FixType.FIX_NONE);
				else if (index == 1) GPSData.setFixType(FixType.FIX_2D);
				else if (index == 2) GPSData.setFixType(FixType.FIX_3D);
				else
					throw new IllegalStateException();
			}
		});

		quality.addItem("invalid"); // 0
		quality.addItem("GPS"); // 1
		quality.addItem("DGPS"); // 2
		quality.addItem("Estimated"); // 6
		quality.addItem("Manual"); // 7
		quality.addItem("Simulation"); // 8

		quality.setSelectedIndex(GPSData.getQuality());
		quality.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int index = quality.getSelectedIndex();
				if (index > 2) index += 3;
				GPSData.setQuality(index);
			}
		});

		this.add(new JPanel());

		JPanel p3 = new JPanel(new GridLayout(2, 6));
		p3.add(new JLabel("HDOP: ", JLabel.RIGHT));
		p3.add(hdop);
		p3.add(new JLabel("VDOP: ", JLabel.RIGHT));
		p3.add(vdop);
		p3.add(new JLabel("PDOP: ", JLabel.RIGHT));
		p3.add(pdop);
		p3.add(new JLabel(""));
		p3.add(new JLabel(""));
		p3.add(new JLabel(""));
		p3.add(new JLabel(""));
		p3.add(new JLabel("fixed?", JLabel.RIGHT));
		p3.add(automaticPDOP);
		this.add(p3);

		hdop.setModel(new SpinnerNumberModel(GPSData.getHDOP(), 0, 10, 0.1));
		hdop.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				Double tmp = (Double) hdop.getValue();
				GPSData.setHDOP(tmp.doubleValue());
			}
		});

		vdop.setModel(new SpinnerNumberModel(GPSData.getVDOP(), 0, 10, 0.1));
		vdop.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				Double tmp = (Double) vdop.getValue();
				GPSData.setVDOP(tmp.doubleValue());
			}
		});

		pdop.setEnabled(false);
		pdop.setModel(new SpinnerNumberModel(GPSData.getPDOP(), 0, 10, 0.1));
		pdop.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				Double tmp = (Double) pdop.getValue();
				GPSData.setPDOP(tmp.doubleValue());
			}
		});

		automaticPDOP.setSelected(true);
		automaticPDOP.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				pdop.setEnabled(!automaticPDOP.isSelected());
				valueChanged();
			}
		});
	}

}