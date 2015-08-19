/*
 * Cordova plugin to play with bluetooth printer tsp650ll
 * @author : nareshu
 * @email : naresh.uppada@gmail.com
 */
package com.bt.printer;

import java.util.TimeZone;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;

public class TSP650ll extends CordovaPlugin {

	public static final String CHECKSTATUS = "CheckStatus";
	public static final String PRINTHELLO = "printHello";
	

	public Boolean trackerStarted = false;
	public Boolean debugModeEnabled = false;
	private static Context mContext;

	/**
	 * Constructor.
	 */
	public TSP650ll() {
	}

	/*
	 * Init blue tooth printer plugin
	 */
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
	}


	public boolean execute(String action, JSONArray arguments,
            CallbackContext callbackContext) throws JSONException {
		mContext = this.cordova.getActivity();

		try {
			if (CHECKSTATUS.equals(action)) {
				Context context = this.cordova.getActivity();
				TSP650ll.CheckStatus(context, "BT:Star Micronics", "mini");
				callbackContext.success();
				return true;
			}
			else if (PRINTHELLO.equals(action)) {

				Context context = this.cordova.getActivity();
				TSP650ll.printHello(context, "BT:Star Micronics",
						"mini", "3inch (80mm)");
				callbackContext.success();
				return true;
			} 
			else{
				callbackContext.error("Invalid action");
				return false;
			}
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
	}

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------

	private static boolean sendCommand(Context context, String portName,
			String portSettings, ArrayList<byte[]> byteList) {
		boolean result = true;
		StarIOPort port = null;
		try {

			port = StarIOPort.getPort(portName, portSettings, 20000, context);

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}



			/* Start of Begin / End Checked Block Sample code */
			StarPrinterStatus status = port.beginCheckedBlock();

			if (true == status.offline) {
				throw new StarIOPortException("A printer is offline");
			}

			byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
			port.writePort(commandToSendToPrinter, 0,
					commandToSendToPrinter.length);

			port.setEndCheckedBlockTimeoutMillis(30000);
			status = port.endCheckedBlock();

			if (true == status.coverOpen) {
				throw new StarIOPortException("Printer cover is open");
			} else if (true == status.receiptPaperEmpty) {
				throw new StarIOPortException("Receipt paper is empty");
			} else if (true == status.offline) {
				throw new StarIOPortException("Printer is offline");
			}
			
		} catch (StarIOPortException e) {
			result = false;
			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Failure");
			alert.setMessage(e.getMessage());
			alert.setCancelable(false);
			alert.show();
		} finally {
			if (port != null) {
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {
				}
			}
		}

		return result;
	}
	
	public static boolean printHello(Context context, String portName,
			String portSettings, String strPrintArea) {
		ArrayList<byte[]> list = new ArrayList<byte[]>();

		if (strPrintArea.equals("3inch (80mm)")) {
			byte[] outputByteBuffer = null;

			list.add(new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area
																

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment

			

			list.add(("\nHello World\n" + "Testing\n" + "Print\n\n").getBytes());


			list.add("\n\n\n\n".getBytes());
		} 

		return sendCommand(context, portName, portSettings, list);
	}


	private static void ShowAlert(String Title, String Message) {
		Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setNegativeButton("Ok", null);
		AlertDialog alert = dialog.create();
		alert.setTitle(Title);
		alert.setMessage(Message);
		alert.setCancelable(false);
		alert.show();

	}




	// portSettings = "mini";
	// String portName = BT:<DeviceName>;
	// context = this
	public static void CheckStatus(Context context, String portName,
			String portSettings) {
		StarIOPort port = null;
		try {
			
			port = StarIOPort.getPort(portName, portSettings, 10000, context);
			

			// A sleep is used to get time for the socket to completely open
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}

			StarPrinterStatus status = port.retreiveStatus();

			if (status.offline == false) {
				Builder dialog = new AlertDialog.Builder(context);
				dialog.setNegativeButton("Ok", null);
				AlertDialog alert = dialog.create();
				alert.setTitle("Printer");
				alert.setMessage("Printer is Online");
				alert.setCancelable(false);
				alert.show();
			} else {
				String message = "Printer is offline";
				if (status.receiptPaperEmpty == true) {
					message += "\nPaper is Empty";
				}
				if (status.coverOpen == true) {
					message += "\nCover is Open";
				}
				Builder dialog = new AlertDialog.Builder(context);
				dialog.setNegativeButton("Ok", null);
				AlertDialog alert = dialog.create();
				alert.setTitle("Printer");
				alert.setMessage(message);
				alert.setCancelable(false);
				alert.show();
			}
		} catch (StarIOPortException e) {
			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Failure");
			alert.setMessage("Failed to connect to printer");
			alert.setCancelable(false);
			alert.show();
		} finally {
			if (port != null) {
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {
				}
			}
		}
	}


	/*
	 * To be done
	 */
	//printReciept
	//print Text :" Hello World" for Testing .



	private static byte[] convertFromListByteArrayTobyteArray(
			List<byte[]> ByteArray) {
		int dataLength = 0;
		for (int i = 0; i < ByteArray.size(); i++) {
			dataLength += ByteArray.get(i).length;
		}

		int distPosition = 0;
		byte[] byteArray = new byte[dataLength];
		for (int i = 0; i < ByteArray.size(); i++) {
			System.arraycopy(ByteArray.get(i), 0, byteArray, distPosition,
					ByteArray.get(i).length);
			distPosition += ByteArray.get(i).length;
		}

		return byteArray;
	}

	/**
	 * Get the device's Universally Unique Identifier (UUID).
	 * 
	 * @return
	 */
	public String getUuid() {
		String uuid = Settings.Secure.getString(this.cordova.getActivity()
				.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		return uuid;
	}

	public String getModel() {
		String model = android.os.Build.MODEL;
		return model;
	}

	public String getProductName() {
		String productname = android.os.Build.PRODUCT;
		return productname;
	}

	public String getManufacturer() {
		String manufacturer = android.os.Build.MANUFACTURER;
		return manufacturer;
	}

	/**
	 * Get the OS version.
	 * 
	 * @return
	 */
	public String getOSVersion() {
		String osversion = android.os.Build.VERSION.RELEASE;
		return osversion;
	}

	public String getSDKVersion() {
		@SuppressWarnings("deprecation")
		String sdkversion = android.os.Build.VERSION.SDK;
		return sdkversion;
	}

	public String getTimeZoneID() {
		TimeZone tz = TimeZone.getDefault();
		return (tz.getID());
	}

}
