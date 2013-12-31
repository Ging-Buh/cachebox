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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.data.GPSDataListener;
import ch.fhnw.imvs.gpssimulator.nmea.NMEASentence;

@SuppressWarnings("serial")
public class GeneralPanel extends JPanel implements GPSDataListener {

	public void valueChanged() {
		mode.setSelectedItem(GPSData.getMode());
	}

	private JComboBox mode = new JComboBox();

	private List<NMEASentence> sentences;

	public GeneralPanel(List<NMEASentence> list) {
		GPSData.addChangeListener(this);
		this.setBorder(BorderFactory.createTitledBorder("General"));
	
		this.sentences = list;
		
		this.setLayout(new FlowLayout());
		
		JPanel p1 = new JPanel(new GridLayout(0,3));
		p1.add(new JLabel(""));
		p1.add(new JLabel("Line"));
		p1.add(new JLabel("Data"));
		for(final NMEASentence s : list){
			final JCheckBox line = new JCheckBox("", true);
			final JCheckBox data = new JCheckBox("", true);
			s.setLinePrinted(true);
			s.setPrintContent(true);
			line.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					s.setLinePrinted(line.isSelected());
				}		
			});
			data.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					s.setPrintContent(data.isSelected());
				}		
			});
			p1.add(new JLabel(s.getName()));
			p1.add(line);
			p1.add(data);
		}
		this.add(p1);

    	final JCheckBox checkSum = new JCheckBox("", true);
		for(NMEASentence s : list){
			s.setPrintChecksum(true);
		}
    	checkSum.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				boolean printChecksum = checkSum.isSelected();
				for(NMEASentence s : sentences) s.setPrintChecksum(printChecksum);
			}
    	});
    	
    	for(GPSData.Mode m : GPSData.Mode.values())	mode.addItem(m);

    	mode.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent e){
    			GPSData.setMode((GPSData.Mode)mode.getSelectedItem());
    		}
    	});

    	JPanel p2 = new JPanel();
    	p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
    	p2.add(new JLabel("Add Check Sum: "));
    	p2.add(checkSum);
    	
    	JPanel p3 = new JPanel();
    	p3.setLayout(new GridLayout(0,1));
    	p3.add(p2);
    	p3.add(new JLabel("Mode:", JLabel.LEFT));
    	p3.add(mode);
    	
    	this.add(p3);
	}
}
