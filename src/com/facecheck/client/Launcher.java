package com.facecheck.client;

import com.facecheck.db.DB;
import com.facecheck.tools.AppInfo;
import com.facecheck.tools.Utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.UIManager;

import org.json.JSONObject;

/**
 *
 * @author user
 */
public class Launcher {
	private static DB databaseConnection;

	public static DB getDatabaseConnection() {
		return databaseConnection;
	}

	private static LoginFrame frame;

	public static LoginFrame getFrame() {
		return frame;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {

		}
		
		// database connection information
		Map<String, String> dbInfo = new HashMap<String, String>();

		dbInfo.put("dbHost", "localhost");
		dbInfo.put("dbPort", "3306");
		dbInfo.put("dbUser", "root");
		dbInfo.put("dbPass", "facecheck");
		dbInfo.put("dbName", "facecheck_client");

		databaseConnection = new DB(dbInfo);

		if (!databaseConnection.connectDB()) {
			Utils.alert("Database connection failed.");
			return;
		} else {
			System.out.println("Database connection success.");
		}

		frame = new LoginFrame();
		frame.setVisible(true);
	}

	public static void getCamerasFromServer() {
		try {
			System.out.println("GET Camera Post API started");
			URL url = new URL(AppInfo.BASE_URL + AppInfo.GET_CAMERAS);
			Map<String, String> params = new ConcurrentHashMap<String, String>();
			StringBuilder postData = new StringBuilder();

			byte[] postDataBytes = Utils.setPostDataBytes(params, postData);
			params.clear();
			String response = Utils.getResponse(postDataBytes, url, true);
			System.out.println("GET Camera Post API stopped");
			System.out.println(response);
			if (new JSONObject(response).get("status").equals("success")) {
				System.out.println("status - success");
			} else {
				System.out.println("status - error");
			}
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
	}
}
