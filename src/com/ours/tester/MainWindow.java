package com.ours.tester;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import com.android.ddmlib.IDevice;

public class MainWindow extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -770355362650071463L;
	public static ADB mADB = new ADB();
	private Utils mUtils;
	public static IDevice[] mDevices;
	public static IDevice mDevice;
	private String devices[];

	private static boolean isPlaying = false;
	private static boolean isRecording = false;
	private static boolean isPause = false;
	private static boolean isResumed = false;

	private static Thread RecordThread;
	private static Thread PlayThread;

	private String eventFilePath;
	private String lastOpenPath;
	private String eventFileToPlay;

	private JButton btnRecord = new JButton("Rec");
	private JButton btnStop = new JButton("Stop");
	private JButton btnOpen = new JButton("Open");
	private JButton btnSave = new JButton("Save");
	private JComboBox comboBox;

	private final JButton btnPlay = new JButton("Play");
	private final JLabel lbl_action = new JLabel("");
	private final JTextArea text_Curfile = new JTextArea("");
	private final JSpinner spinner = new JSpinner();

	/**
	 * Create the frame.
	 */
	public MainWindow() {

		super("Record and Play");

		mUtils = new Utils();

		if (!mADB.initialize()) {
			JOptionPane
					.showMessageDialog(
							this,
							"Could not find adb, please install Android SDK and set adb path",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		devices = mADB.adbDevices();
		if (devices == null) {
			JOptionPane
					.showMessageDialog(
							this,
							"No Devices Conencted, Please Run again by conencting atleast once android device.",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		try {
			mADB.initmySendEvent();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setTitle("Record N Play");
		setSize(329, 187);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0, 0.0, 0.0,
				1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 1.0,
				Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		JLabel lblSelectYourDevice = new JLabel("Select Your Device");
		GridBagConstraints gbc_lblSelectYourDevice = new GridBagConstraints();
		gbc_lblSelectYourDevice.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSelectYourDevice.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectYourDevice.gridx = 0;
		gbc_lblSelectYourDevice.gridy = 0;
		getContentPane().add(lblSelectYourDevice, gbc_lblSelectYourDevice);

		GridBagConstraints gbc_btnRecord = new GridBagConstraints();
		gbc_btnRecord.fill = GridBagConstraints.HORIZONTAL;
		btnRecord.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		gbc_btnRecord.insets = new Insets(0, 0, 5, 0);
		gbc_btnRecord.gridx = 9;
		gbc_btnRecord.gridy = 0;
		getContentPane().add(btnRecord, gbc_btnRecord);

		comboBox = new JComboBox(devices);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 5;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;
		getContentPane().add(comboBox, gbc_comboBox);

		btnStop.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		btnStop.setEnabled(false);
		GridBagConstraints gbc_btnStop = new GridBagConstraints();
		gbc_btnStop.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStop.insets = new Insets(0, 0, 5, 0);
		gbc_btnStop.gridx = 9;
		gbc_btnStop.gridy = 1;
		getContentPane().add(btnStop, gbc_btnStop);

		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
		btnSave.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		btnSave.setEnabled(false);
		gbc_btnSave.insets = new Insets(0, 0, 5, 0);
		gbc_btnSave.gridx = 9;
		gbc_btnSave.gridy = 2;
		getContentPane().add(btnSave, gbc_btnSave);

		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.fill = GridBagConstraints.HORIZONTAL;
		btnOpen.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		gbc_btnOpen.insets = new Insets(0, 0, 5, 0);
		gbc_btnOpen.gridx = 9;
		gbc_btnOpen.gridy = 3;
		getContentPane().add(btnOpen, gbc_btnOpen);

		GridBagConstraints gbc_lbl_nowPlaying = new GridBagConstraints();
		gbc_lbl_nowPlaying.fill = GridBagConstraints.HORIZONTAL;
		gbc_lbl_nowPlaying.anchor = GridBagConstraints.SOUTH;
		gbc_lbl_nowPlaying.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_nowPlaying.gridx = 0;
		gbc_lbl_nowPlaying.gridy = 3;
		lbl_action.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		getContentPane().add(lbl_action, gbc_lbl_nowPlaying);

		GridBagConstraints gbc_lblCurfile = new GridBagConstraints();
		gbc_lblCurfile.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCurfile.anchor = GridBagConstraints.NORTH;
		gbc_lblCurfile.insets = new Insets(0, 0, 0, 5);
		gbc_lblCurfile.gridx = 0;
		gbc_lblCurfile.gridy = 4;
		text_Curfile.setEditable(false);
		text_Curfile.setWrapStyleWord(true);
		text_Curfile.setLineWrap(true);
		text_Curfile.setOpaque(false);
		text_Curfile.setFocusable(false);
		text_Curfile.setBackground(UIManager.getColor("Label.background"));
		text_Curfile.setFont(UIManager.getFont("Label.font"));
		text_Curfile.setBorder(UIManager.getBorder("Label.border"));
		text_Curfile.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		getContentPane().add(text_Curfile, gbc_lblCurfile);

		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.gridx = 8;
		gbc_spinner.gridy = 4;

		spinner.setEnabled(false);
		spinner.setValue(1);
		spinner.setToolTipText("Play count: " + spinner.getValue());
		getContentPane().add(spinner, gbc_spinner);

		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.fill = GridBagConstraints.HORIZONTAL;
		btnPlay.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		gbc_btnPlay.gridx = 9;
		gbc_btnPlay.gridy = 4;
		getContentPane().add(btnPlay, gbc_btnPlay);
		btnPlay.setEnabled(false);

		btnRecord.addActionListener(this);
		btnStop.addActionListener(this);
		btnPlay.addActionListener(this);
		btnSave.addActionListener(this);
		btnOpen.addActionListener(this);
		comboBox.addActionListener(this);

		// pack();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof JButton) {
			JButton button = (JButton) source;
			if (button == btnRecord) {
				if (isPause) {
					resumeRecording();
				} else if (isRecording)
					pauseRecording();
				else
					record();
			} else if (button == btnStop)
				stop();
			else if (button == btnOpen)
				openFile();
			else if (button == btnSave)
				saveFile();
			else if (button == btnPlay)
				play();

		}
		if (source instanceof JComboBox) {
			JComboBox cb = (JComboBox) event.getSource();

			if (cb.getSelectedItem().toString().contains("All"))
				mADB.mDevice = mADB.mDevices[cb.getSelectedIndex() - 1];
			else
				mADB.mDevice = mADB.mDevices[cb.getSelectedIndex()];

		}
	}

	private void record() {

		isRecording = true;
		isResumed = false;

		RecordThread = new Thread(new Runnable() {

			public void run() {

				btnRecord.setText("Pause");
				btnStop.setEnabled(true);
				btnOpen.setEnabled(false);
				btnPlay.setEnabled(false);
				lbl_action.setText("Recording...");
				mADB.startlogGetEventToFile();
			}
		});
		System.out.println("out run Device: " + mADB.getDeviceName());
		RecordThread.start();
	}

	private void stop() {

		isPause = false;
		isRecording = false;
		isPlaying = false;
		isResumed = false;

		btnRecord.setText("Rec");
		btnPlay.setEnabled(true);
		btnRecord.setEnabled(true);
		btnSave.setEnabled(true);
		btnStop.setEnabled(false);
		btnOpen.setEnabled(true);
		spinner.setEnabled(true);
		lbl_action.setText("Saved to:");
		// RecordThread.interrupt();
		eventFileToPlay = mADB.stoplogGetEventToFile();
		text_Curfile.setText(eventFileToPlay);
	}

	private void pauseRecording() {

		btnRecord.setText("Resume");
		isPause = true;
		isPlaying = false;
		isRecording = false;
		isResumed = false;

		btnPlay.setEnabled(false);
		btnRecord.setEnabled(true);
		btnSave.setEnabled(true);
		btnStop.setEnabled(true);

		mADB.pauselogGetEventToFile();
		// RecordThread.interrupt();
	}

	private void resumeRecording() {

		isPause = false;
		isPlaying = false;
		isRecording = true;
		isResumed = true;
		btnRecord.setText("Pause");
		btnSave.setEnabled(false);
		// RecordThread.interrupt();
		// mADB.updatelogGetEventToFile();
		// RecordThread.start();
		mADB.setStartNewLogFile(false);
		record();
	}

	private void play() {

		PlayThread = new Thread(new Runnable() {

			public void run() {

				isPlaying = true;
				btnPlay.setEnabled(false);
				btnRecord.setEnabled(false);
				btnSave.setEnabled(false);
				btnStop.setEnabled(true);
				System.out.println(eventFileToPlay);
				lbl_action.setText("Now Running:");
				text_Curfile.setText(eventFileToPlay);
				int count = (Integer) spinner.getValue();
				for (int i = 1; i <= count; i++) {
					lbl_action.setText("Now Running: x " + i);
					mADB.convertPushPlayEventScript(eventFileToPlay);
				}
				System.out.println("End Play thread");
				resetControls();
				lbl_action.setText("");
			}
		});
		System.out.println("Now Playing at: " + mADB.getDeviceName());
		PlayThread.start();
		// btnStop.setEnabled(false);
	}

	private void resetControls() {

		isPause = false;
		isRecording = false;
		isPlaying = false;
		isResumed = false;

		btnRecord.setText("Rec");
		btnPlay.setEnabled(true);
		btnRecord.setEnabled(true);
		btnSave.setEnabled(true);
		btnStop.setEnabled(false);
		btnOpen.setEnabled(true);
		// RecordThread.interrupt();
		// eventFileToPlay = mADB.stoplogGetEventToFile();
	}

	private void saveFile() {
		JFileChooser fileChooser = null;

		if (lastOpenPath != null && !lastOpenPath.equals("")) {
			fileChooser = new JFileChooser(lastOpenPath);
		} else {
			fileChooser = new JFileChooser(Paths.get("").toAbsolutePath()
					.toString());
		}

		FileFilter evtFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "mTester Event file (*.MEL)";
			}

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				} else {
					return file.getName().toLowerCase().endsWith(".mel");
				}
			}
		};

		fileChooser.setFileFilter(evtFilter);
		fileChooser.setDialogTitle("Save Event File");
		fileChooser.setAcceptAllFileFilterUsed(true);

		int userChoice = fileChooser.showSaveDialog(this);
		if (userChoice == JFileChooser.APPROVE_OPTION) {
			eventFilePath = fileChooser.getSelectedFile().getAbsolutePath();
			Path dst = FileSystems.getDefault().getPath(eventFilePath);
			Path src = FileSystems.getDefault().getPath(mADB.file_location);

			try {
				Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("saved to " + eventFilePath);
			lbl_action.setText("File Saved As:");
			text_Curfile.setText(eventFilePath);
			mADB.file_location = eventFilePath;
		}
	}

	private void openFile() {
		JFileChooser fileChooser = null;

		if (lastOpenPath != null && !lastOpenPath.equals("")) {
			fileChooser = new JFileChooser(lastOpenPath);
		} else {
			fileChooser = new JFileChooser(Paths.get("").toAbsolutePath()
					.toString());
		}

		FileFilter evtFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "Event file (*.MEL)";
			}

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				} else {
					return file.getName().toLowerCase().endsWith(".mel");
				}
			}
		};

		fileChooser.setFileFilter(evtFilter);
		fileChooser.setDialogTitle("Open Event File");
		fileChooser.setAcceptAllFileFilterUsed(true);

		int userChoice = fileChooser.showOpenDialog(this);
		if (userChoice == JFileChooser.APPROVE_OPTION) {
			eventFilePath = fileChooser.getSelectedFile().getAbsolutePath();
			lastOpenPath = fileChooser.getSelectedFile().getParent();
			System.out.println(eventFilePath);
			eventFileToPlay = eventFilePath;
		}
		lbl_action.setText("Current File:");
		text_Curfile.setText(fileChooser.getSelectedFile().getName());
		btnPlay.setEnabled(true);
		spinner.setEnabled(true);
	}

	public static void interrupt() throws IOException {

		System.out.println("Interrupted");
		int i = 0;
		try {
			Thread.sleep(1000);
			System.out.println(i++);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			System.out.println("NOW interupted");
			if (isResumed) {

				System.out
						.println("NOW interupted because of resume, start getevent process here with append");
			} else if (isPause) {
				System.out
						.println("NOW interupted because of pause, suspend getevent process here.");

			} else {
				System.out.println("!!!!");

			}
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			public void run() {
				MainWindow ex = new MainWindow();
				ex.setVisible(true);
			}
		});
	}
}
