/**
 * The about dialog
 * @author Mohammed Rilwan April 2016.
 * 
 */
package com.ours.tester;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class About extends JDialog {

	public About(Frame owner, boolean modal) {
		super(owner, modal);

		setTitle("About Android Record N Play");
		setBounds(0, 0, 239, 118);
		setResizable(false);

		JLabel labelApp = new JLabel("Android Record N Play Version 1.0.3");

		JTextField labelUrl = new JTextField("https://github.com/rils/ARP");
		JTextField labelReport = new JTextField("bugs?:rilwan:enjoy!");
		labelReport.setBorder(new EmptyBorder(0, 0, 0, 0));
		labelReport.setEditable(false);
		labelUrl.setEditable(false);
		labelUrl.setBorder(new EmptyBorder(0, 0, 0, 0));
		labelUrl.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mouseEntered(MouseEvent arg0) {

			}

			public void mouseClicked(MouseEvent arg0) {
				JTextField textField = (JTextField) arg0.getSource();
				textField.selectAll();
			}
		});

		// OK
		JButton buttonOK = new JButton("OK");
		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		// Container
		Container container1 = new Container();
		FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, 5, 5);
		container1.setLayout(flowLayout);
		container1.add(labelApp);
		container1.add(labelUrl);
		container1.add(labelReport);

		Container containger = getContentPane();
		containger.add(container1, BorderLayout.CENTER);
		containger.add(buttonOK, BorderLayout.SOUTH);

		// buttons
		{
			AbstractAction actionOK = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					onOK();
				}
			};
			AbstractAction actionCancel = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					onCancel();
				}
			};

			JComponent targetComponent = getRootPane();
			InputMap inputMap = targetComponent.getInputMap();
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
					"Cancel");
			targetComponent.setInputMap(
					JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
			targetComponent.getActionMap().put("OK", actionOK);
			targetComponent.getActionMap().put("Cancel", actionCancel);
		}
	}

	private void onOK() {
		dispose();
	}

	private void onCancel() {
		dispose();
	}
}
