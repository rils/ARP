/**
 * this deals with adb connection,command,output etc
 * @author Mohammed Rilwan April 2016.
 * 
 */
package com.ours.tester;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;

public class ADB {
	public static final String LOG_TAG = "mytester";
	public String file_location;
	public static boolean isStartNewLogFile = true;

	private AndroidDebugBridge mAndroidDebugBridge;
	public static IDevice[] mDevices;
	public static IDevice mDevice;
	public static FileOutputReceiver fReciever;
	public static boolean isLogCancelled = false;
	private static String myTesterEventScript_file = "/data/local/tmp/myTesterEventScript.mes";
	static final String mySendEventAdbLocation = "/data/local/tmp/mysendevent";
	public static final String mySendEventLocation = Paths.get("")
			.toAbsolutePath().toString()
			+ File.separator + "mysendevent";

	public boolean initialize() {
		boolean success = true;

		String adbLocation;

		adbLocation = System.getenv("ANDROID_HOME");
		if (adbLocation != null) {
			adbLocation += File.separator + "platform-tools";
		}

		if (success) {
			if ((adbLocation != null) && (adbLocation.length() != 0)) {
				adbLocation += File.separator + "adb";
			} else {
				adbLocation = "adb";
			}
			System.out.println("adb path is " + adbLocation);
			AndroidDebugBridge.init(false);
			mAndroidDebugBridge = AndroidDebugBridge.createBridge(adbLocation,
					true);
			if (mAndroidDebugBridge == null) {
				success = false;
			}
		}

		if (success) {

			int count = 0;
			while (!mAndroidDebugBridge.hasInitialDeviceList()) {
				try {
					Thread.sleep(100);
					count++;
				} catch (InterruptedException e) {
				}
				if (count > 100) {
					success = false;
					break;
				}
			}
		}

		if (!success) {
			terminate();
		}
		return success;
	}

	public void terminate() {
		AndroidDebugBridge.terminate();
	}

	public IDevice[] getDevices() {
		IDevice[] devices = null;
		if (mAndroidDebugBridge != null) {
			devices = mAndroidDebugBridge.getDevices();
		}
		return devices;
	}

	public String[] adbDevices() {

		mDevices = getDevices();

		if (mDevices.length == 0) {
			System.out.println("No connected devices!");
			return null;
		}
		String[] list = new String[mDevices.length + 1];
		if (mDevices != null) {
			for (int i = 0; i < mDevices.length; i++) {
				list[i] = mDevices[i].toString();
			}
			mDevice = mDevices[0];
		}
		list[mDevices.length] = "All Devices";
		return list;
	}

	public String getDeviceName() {

		return mDevice.getName();
	}

	public IDevice getMyDevice() {

		return mDevice;
	}

	private class CollectingOutputReceiver extends MultiLineReceiver {

		private StringBuffer mOutputBuffer = new StringBuffer();

		public String getOutput() {
			return mOutputBuffer.toString();
		}

		@Override
		public void processNewLines(String[] lines) {
			for (String line : lines) {
				mOutputBuffer.append(line);
				mOutputBuffer.append("\n");
			}
		}

		public boolean isCancelled() {
			return false;
		}
	}

	public String shell(String command) throws IOException {

		Log.i(LOG_TAG, String.format("adb shell %s", command));
		System.out.println("adb shell " + command);
		CollectingOutputReceiver receiver = new CollectingOutputReceiver();

		try {
			mDevice.executeShellCommand(command, receiver);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdbCommandRejectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShellCommandUnresponsiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String output = receiver.getOutput();
		Log.i(LOG_TAG, String.format("Result: %s", output));
		return output;
	}

	public class FileOutputReceiver extends MultiLineReceiver {

		private StringBuffer mOutputBuffer = new StringBuffer();

		public void getOutputToFile() {

			if (ADB.isStartNewLogFile) {
				file_location = Paths.get("").toAbsolutePath().toString()
						+ File.separator
						+ new SimpleDateFormat("yyyyMMddhhmmss")
								.format(new Date()) + "_events.mel";
				System.out.println("New logging");
			}
			try {

				FileWriter fw = new FileWriter(file_location, true);
				fw.write(mOutputBuffer.toString());
				fw.close();
				System.out.println("Written to file " + file_location);
			} catch (IOException e) {
				System.out.println("Cant open file in this location: "
						+ file_location);
				e.printStackTrace();
			}

		}

		@Override
		public void processNewLines(String[] lines) {
			for (String line : lines) {
				mOutputBuffer.append(line);
				mOutputBuffer.append("\n");
			}
		}

		public boolean isCancelled() {
			return isLogCancelled;
		}
	}

	public FileOutputReceiver startlogGetEventToFile() {

		isLogCancelled = false;
		System.out.println("Start Logging...");
		String command = "getevent -t";
		FileOutputReceiver fReceiver = new FileOutputReceiver();
		System.out.println("Before " + isStartNewLogFile);
		try {
			mDevice.executeShellCommand(command, fReceiver);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdbCommandRejectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShellCommandUnresponsiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("From startloggin " + isStartNewLogFile);
		fReceiver.getOutputToFile();
		fReceiver.flush();
		fReceiver.isCancelled();
		fReceiver.done();
		return fReceiver;
	}

	public String stoplogGetEventToFile() {

		isLogCancelled = true;

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isStartNewLogFile = true;
		System.out.println("Stop: getevent logging done\n" + file_location);
		return file_location;
	}

	public static boolean isStartNewLogFile() {
		return isStartNewLogFile;
	}

	public void setStartNewLogFile(boolean isStartNewLogFile) {
		ADB.isStartNewLogFile = isStartNewLogFile;
	}

	public String resumelogGetEventToFile() {

		isStartNewLogFile = false;
		System.out.println("Resume getevent logging " + file_location);
		startlogGetEventToFile();
		return file_location;
	}

	public String pauselogGetEventToFile() {

		isLogCancelled = true;

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Pause: getevent logging done\n" + file_location);
		return file_location;
	}

	public void runSendEventScript(String mes_script_location)
			throws IOException {

		String cmd = "sh " + mes_script_location;
		shell(cmd);
	}

	public void initmySendEvent() throws IOException {

		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("mysendevent");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("mysendevent");
			byte[] buf = new byte[2048];
			int r = is.read(buf);
			while (r != -1) {
				fos.write(buf, 0, r);
				r = is.read(buf);
			}
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		adbPush(mySendEventLocation, mySendEventAdbLocation);
		shell("chmod 777 " + mySendEventAdbLocation);
	}

	public void convertPushPlayEventScript(String mel_file_location,
			boolean play_only) {
		String mes_script_file;
		System.out.println("convertpushPlay" + mel_file_location);

		try {
			if (!play_only) {
				mes_script_file = new Utils()
						.convertgetEventToSendEvent(mel_file_location);
				adbPush(mes_script_file, myTesterEventScript_file);
			}
			runSendEventScript(myTesterEventScript_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void adbPush(String src, String dst) {

		System.out.println("adb push: " + src + "to " + dst);
		try {
			mDevice.pushFile(src, dst);
		} catch (SyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdbCommandRejectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
