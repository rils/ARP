/**
 * Android Record N Play.
 * This records the input events from gpio(hard key) and touchscreen,touchkey(back,menu buttom)
 * saves as mel(mytester event log) file and convert to mes(mytesteter event script).
 * playback mes script by a modified sendevent binary.
 * More Info at -https://github.com/rils/ARP/wiki
 * @author Mohammed Rilwan April 2016.
 * 
 */
package com.ours.tester;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

import com.android.ddmlib.IDevice;
import java.awt.Color;

public class MainWindow extends JFrame implements ActionListener {

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
	private static boolean canIPlay = true;

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
	private JPopupMenu mPopupMenu;
	private final JLabel lblCount = new JLabel("Delay between play");
	private final JSpinner spinner_delay = new JSpinner();
	private final JLabel lblDealy = new JLabel("Count");

	/**
	 * Create the the main frame.
	 */
	public MainWindow() {

		super("Android Record and Play");
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				MainWindow.class.getResource("icon.png")));

		mUtils = new Utils();

		// for help menu
		initializeMenu();

		// initialize adb
		if (!mADB.initialize()) {
			showDialogMessage(
					"Could not find adb, please install Android SDK and set adb path",
					"error", true);
			System.exit(0);
		}

		// initialize devices
		devices = selectDevcies(false);

		// copy mysendevent to /data/local/tmp/
		try {
			mADB.initmySendEvent();
		} catch (IOException e) {
			e.printStackTrace();
			showDialogMessage(
					"No write permission on jar file location, Copy jar file to some other location and run again",
					"error", true);
		}

		setTitle("Android Record N Play");
		setSize(324, 198);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 1.0, 1.0, 0.0,
				0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };

		getContentPane().setLayout(gridBagLayout);
		
				JLabel lblSelectYourDevice = new JLabel("Select Your Device");
				GridBagConstraints gbc_lblSelectYourDevice = new GridBagConstraints();
				gbc_lblSelectYourDevice.anchor = GridBagConstraints.EAST;
				gbc_lblSelectYourDevice.insets = new Insets(0, 0, 5, 5);
				gbc_lblSelectYourDevice.gridx = 0;
				gbc_lblSelectYourDevice.gridy = 0;
				getContentPane().add(lblSelectYourDevice, gbc_lblSelectYourDevice);

		GridBagConstraints gbc_btnRecord = new GridBagConstraints();
		gbc_btnRecord.fill = GridBagConstraints.BOTH;
		btnRecord.setToolTipText("Start Record Input Actions");
		btnRecord.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		gbc_btnRecord.insets = new Insets(0, 0, 5, 5);
		gbc_btnRecord.gridx = 9;
		gbc_btnRecord.gridy = 0;
		getContentPane().add(btnRecord, gbc_btnRecord);

		btnRecord.addActionListener(this);

		comboBox = new JComboBox(devices);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 6;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;
		getContentPane().add(comboBox, gbc_comboBox);

		btnStop.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		btnStop.setEnabled(false);
		btnStop.setToolTipText("Stop Recording");
		GridBagConstraints gbc_btnStop = new GridBagConstraints();
		gbc_btnStop.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStop.insets = new Insets(0, 0, 5, 5);
		gbc_btnStop.gridx = 9;
		gbc_btnStop.gridy = 1;
		getContentPane().add(btnStop, gbc_btnStop);
		btnStop.addActionListener(this);

		GridBagConstraints gbc_lbl_nowPlaying = new GridBagConstraints();
		gbc_lbl_nowPlaying.fill = GridBagConstraints.HORIZONTAL;
		gbc_lbl_nowPlaying.anchor = GridBagConstraints.SOUTH;
		gbc_lbl_nowPlaying.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_nowPlaying.gridx = 0;
		gbc_lbl_nowPlaying.gridy = 2;
		lbl_action.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		getContentPane().add(lbl_action, gbc_lbl_nowPlaying);

		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
		btnSave.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		btnSave.setEnabled(false);
		btnSave.setToolTipText("Save Last Recorded File");
		gbc_btnSave.insets = new Insets(0, 0, 5, 5);
		gbc_btnSave.gridx = 9;
		gbc_btnSave.gridy = 2;
		getContentPane().add(btnSave, gbc_btnSave);
		btnSave.addActionListener(this);

		GridBagConstraints gbc_lblCurfile = new GridBagConstraints();
		gbc_lblCurfile.gridwidth = 5;
		gbc_lblCurfile.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCurfile.anchor = GridBagConstraints.NORTH;
		gbc_lblCurfile.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurfile.gridx = 0;
		gbc_lblCurfile.gridy = 3;
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

		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.fill = GridBagConstraints.HORIZONTAL;
		btnOpen.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		btnOpen.setToolTipText("Open a .mel to play");
		gbc_btnOpen.insets = new Insets(0, 0, 5, 5);
		gbc_btnOpen.gridx = 9;
		gbc_btnOpen.gridy = 3;
		getContentPane().add(btnOpen, gbc_btnOpen);
		btnOpen.addActionListener(this);

		GridBagConstraints gbc_lblCount = new GridBagConstraints();
		gbc_lblCount.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCount.insets = new Insets(0, 0, 0, 5);
		gbc_lblCount.gridx = 0;
		gbc_lblCount.gridy = 4;
		lblCount.setFont(new Font("Tahoma", Font.PLAIN, 12));
		getContentPane().add(lblCount, gbc_lblCount);

		GridBagConstraints gbc_spinner_delay = new GridBagConstraints();
		gbc_spinner_delay.insets = new Insets(0, 0, 0, 5);
		gbc_spinner_delay.gridx = 1;
		gbc_spinner_delay.gridy = 4;
		spinner_delay.setEnabled(true);
		spinner_delay.setBackground(Color.LIGHT_GRAY);
		spinner_delay.setForeground(Color.LIGHT_GRAY);
		getContentPane().add(spinner_delay, gbc_spinner_delay);

		GridBagConstraints gbc_lblDealy = new GridBagConstraints();
		gbc_lblDealy.insets = new Insets(0, 0, 0, 5);
		gbc_lblDealy.gridx = 7;
		gbc_lblDealy.gridy = 4;
		lblDealy.setFont(new Font("Tahoma", Font.PLAIN, 12));
		getContentPane().add(lblDealy, gbc_lblDealy);

		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.gridx = 8;
		gbc_spinner.gridy = 4;
		spinner.setEnabled(false);
		spinner.setForeground(Color.LIGHT_GRAY);
		spinner.setBackground(Color.LIGHT_GRAY);
		spinner.setValue(1);
		spinner.setToolTipText("Play count: " + spinner.getValue());
		getContentPane().add(spinner, gbc_spinner);

		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.anchor = GridBagConstraints.SOUTH;
		gbc_btnPlay.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPlay.insets = new Insets(0, 0, 0, 5);
		btnPlay.setFont(new Font("Sans", Font.ROMAN_BASELINE, 14));
		btnPlay.setToolTipText("Play Current File");
		gbc_btnPlay.gridx = 9;
		gbc_btnPlay.gridy = 4;
		getContentPane().add(btnPlay, gbc_btnPlay);
		btnPlay.setEnabled(false);
		btnPlay.addActionListener(this);
		comboBox.addActionListener(this);

		getContentPane().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					mPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		// pack();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = MainWindow.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
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
			} else if (button == btnStop) {
				if (isPlaying)
					canIPlay = false;
				else
					stop();
			} else if (button == btnOpen)
				openFile();
			else if (button == btnSave)
				saveFile();
			else if (button == btnPlay)
				play();
		}
		/*
		 * This is fake "all device" :D . When i get time will add support to
		 * run same script in all connected device.
		 */
		if (source instanceof JComboBox) {
			JComboBox cb = (JComboBox) event.getSource();

			if (cb.getSelectedItem().toString().contains("All"))
				mADB.mDevice = mADB.mDevices[cb.getSelectedIndex() - 1];
			else
				mADB.mDevice = mADB.mDevices[cb.getSelectedIndex()];
		}
	}

	private String[] selectDevcies(boolean show_popup) {

		devices = mADB.adbDevices();
		if (devices == null) {
			showDialogMessage(
					"No Devices Connected, Please run again by connecting atleast one android device.",
					"error", true);
		}
		if (show_popup)
			comboBox.showPopup();
		return devices;
	}

	private void showDialogMessage(String message, String type, boolean exit) {

		int errorType = JOptionPane.ERROR_MESSAGE;
		String title = "Error";
		/*
		 * Add more here later if required
		 */
		if (type.contentEquals("error")) {
			errorType = JOptionPane.ERROR_MESSAGE;
			title = "Error";
		}

		JOptionPane.showMessageDialog(this, message, title, errorType);
		if (exit)
			System.exit(0);
	}

	private void initializeMenu() {
		mPopupMenu = new JPopupMenu();

		addMenuItem("Refresh Devices");
		addMenuItem("About ARP");

		/*
		 * Add later
		 */
		mPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}

	private void addMenuItem(String menuItem_label) {

		JMenuItem menuItem = new JMenuItem(menuItem_label);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMenuItem Item = (JMenuItem) e.getSource();
				if (Item.getText().contains("About")) {
					about();
				} else if (Item.getText().contains("Refresh"))
					selectDevcies(true);
			}
		});
		mPopupMenu.add(menuItem);
		mPopupMenu.addSeparator();
	}

	private void about() {
		About dialog = new About(this, true);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
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

	/**
	 * This happens at stop a record.
	 */
	private void stop() {

		isPause = false;
		isResumed = false;

		btnRecord.setText("Rec");
		btnPlay.setEnabled(true);
		btnRecord.setEnabled(true);
		btnSave.setEnabled(true);
		btnStop.setEnabled(false);
		btnOpen.setEnabled(true);
		spinner.setEnabled(true);
		if (isRecording)
			lbl_action.setText("Saved to:");
		isRecording = false;
		isPlaying = false;
		// RecordThread.interrupt();
		eventFileToPlay = mADB.stoplogGetEventToFile();
		text_Curfile.setText(eventFileToPlay);
	}

	/**
	 * at pause a record.
	 */
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

	/**
	 * at play.
	 */
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
				int delay = (Integer) spinner_delay.getValue();
				spinner.setToolTipText("Play count: " + spinner.getValue());
				lbl_action.setText("Current File:");
				boolean playOnly = false;
				for (int i = 1; i <= count; i++) {
					lbl_action.setText("Now Running: x " + i);
					mADB.convertPushPlayEventScript(eventFileToPlay, playOnly);
					playOnly = true;
					if (!canIPlay) {
						System.out.println("Stop button: break Play thread");
						canIPlay = true;
						lbl_action.setText("Played " + i + " times");
						break;
					}
					try {
						System.out.println("wait: " + delay
								+ " seconds before next play");
						Thread.sleep(delay * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				lbl_action.setText("Played " + count + " times");
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
			if (!eventFilePath.endsWith("mel"))
				eventFilePath = eventFilePath + ".mel";
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
			eventFileToPlay = eventFilePath;
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
			text_Curfile.setText(eventFilePath);
			btnPlay.setEnabled(true);
			spinner.setEnabled(true);
			lbl_action.setText("Current File:");
		}

	}

	/**
	 * Launch the application here.
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
