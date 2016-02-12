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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.fhnw.imvs.gpssimulator.SimulatorMain.ConnectionHandler;

@SuppressWarnings("serial")
public class BluetoothPanel extends JPanel {

	private final JComboBox<ConnectionHandler> bluetoothConnections;
	private final JButton closeConnection;

	public BluetoothPanel() {
		this.setBorder(BorderFactory.createTitledBorder("Bluetooth Devices"));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel aroundBluetooth = new JPanel(new FlowLayout());

		bluetoothConnections = new JComboBox<ConnectionHandler>();

		aroundBluetooth.add(new JLabel("Connection Number:"));
		aroundBluetooth.add(bluetoothConnections);
		this.add(aroundBluetooth);

		JPanel aroundCloseConnection = new JPanel();

		closeConnection = new JButton("Close Connection");
		closeConnection.setEnabled(false);
		closeConnection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (bluetoothConnections) {
					ConnectionHandler connection = (ConnectionHandler) bluetoothConnections.getSelectedItem();
					connection.stopRunning();
					bluetoothConnections.removeItem(connection);
					if (bluetoothConnections.getItemCount() == 0) {
						closeConnection.setEnabled(false);
					}
				}

			}
		});
		aroundCloseConnection.add(closeConnection);
		this.add(aroundCloseConnection);
	}

	public synchronized void addConnection(ConnectionHandler connection) {
		bluetoothConnections.addItem(connection);
		closeConnection.setEnabled(true);
	}

	public synchronized void removeConnection(ConnectionHandler connection) {
		bluetoothConnections.removeItem(connection);
		if (bluetoothConnections.getItemCount() == 0) {
			closeConnection.setEnabled(false);
		}
	}
}
