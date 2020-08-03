package com.facecheck.client;

import com.facecheck.db.dbList;
import com.facecheck.db.dbRow;
import com.facecheck.tools.AppInfo;
import com.facecheck.tools.Utils;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextField;

import org.json.JSONObject;

/**
 *
 * @author user
 */
public class Cameras {
	public static dbList loadData(dbRow filter) {
		try {
			return Launcher.getDatabaseConnection().loadData(filter, "cameras", "name");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static dbRow getMember(String id) {
		try {
			return Launcher.getDatabaseConnection().fetchByID(id, "cameras", "id");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static dbList loadLogData() {
		try {
			return Launcher.getDatabaseConnection().loadLogData("logs");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static dbRow getPID(String id) {
		try {
			return Launcher.getDatabaseConnection().fetchByID(id, "configuration", "id");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static dbRow getUserToken() {
		try {
			return Launcher.getDatabaseConnection().fetchUserToken("user");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static dbRow fetchCameraStatus(String id) {
		try {
			return Launcher.getDatabaseConnection().fetchCameraStatus(id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static dbList searchByName(String name) {
		try {
			String sql = String.format("SELECT * FROM cameras WHERE LOWER(%s) LIKE '%%%s%%' ORDER BY %s ASC", "nama",
					Utils.escapeSQLVar(name).toLowerCase(), "nama");
			return Launcher.getDatabaseConnection().getList(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return new dbList();
		}
	}

	public static boolean delete(String id) {
		try {
			String sql = String.format("DELETE FROM cameras WHERE id = '%s'", Utils.escapeSQLVar(id));
			return Launcher.getDatabaseConnection().nonTransactQuery(sql) > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean updatePID(String PID) {
		try {
			String sql = String.format("UPDATE configuration SET value='%s' WHERE id = '1'", Utils.escapeSQLVar(PID));
			return Launcher.getDatabaseConnection().nonTransactQuery(sql) > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean updateCameraStatus(String Status) {
		try {
			String sql = String.format("UPDATE cameras SET status='%s'", Utils.escapeSQLVar(Status));
			return Launcher.getDatabaseConnection().nonTransactQuery(sql) > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public static boolean updateCameraStatusByID(String cameraID, String Status, String updated_by) {
		try {
			Date date = new Date(); 
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			System.out.println(formatter.format(date));
			String date_time = formatter.format(date);
			
			String sql = String.format("UPDATE cameras SET status='%s', last_update_time='%s', last_update_by='%s' WHERE camera_id='%s'", 
					Utils.escapeSQLVar(Status), Utils.escapeSQLVar(date_time), Utils.escapeSQLVar(updated_by), Utils.escapeSQLVar(cameraID));
			if(Launcher.getDatabaseConnection().nonTransactQuery(sql) > 0) {
				String sync_status = "0";
				if(Utils.checkInternetAvailable()) {
					// System.out.println("Status Update Camera Post API started");
					URL url = new URL(AppInfo.BASE_URL + AppInfo.CAMERA_STATUS_UPDATE);
					Map<String, String> params = new ConcurrentHashMap<String, String>();
					params.put("camera_id", cameraID);
					params.put("status", Status);
					StringBuilder postData = new StringBuilder();
		
					byte[] postDataBytes = Utils.setPostDataBytes(params, postData);
					params.clear();
					String response = Utils.getResponse(postDataBytes, url, true);
					// System.out.println("Status Update Camera Post API stopped");
					// System.out.println(response);
					if (new JSONObject(response).get("status").equals("success")) {
						// System.out.println(Status + " Status Update - success");
						sync_status = "1";
						
					} else {
						// System.out.println(Status + " Status Update - error");
					}
				}
				
				Map<String, JTextField> entries = new HashMap<String, JTextField>();
				entries.put("camera_id", new JTextField());
				entries.put("status", new JTextField());
				entries.put("status_time", new JTextField());
				entries.put("updated_by", new JTextField());
				entries.put("reason", new JTextField());
				entries.put("sync_status", new JTextField());
				
				dbRow values = new dbRow();
				for (String name : entries.keySet()) {
					values.put(name, entries.get(name).getText());
				}
				Utils.stdout(values);
				// System.out.println("Data Values" + values);
				
				values.put("camera_id", cameraID);
				values.put("status", Status);
				values.put("status_time", date_time);
				values.put("updated_by", updated_by);
				values.put("reason", "");
				values.put("sync_status", sync_status);
				// System.out.println("Camera Logs Data Values" + values);
				
				int saved = Launcher.getDatabaseConnection().save("camera_logs", "id", values);
				
				/*if (saved > 0) {
					System.out.println("Camera Logs Data saved");
				} else {
					System.out.println("Camera Logs Data not saved.");
				}*/
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean deleteUser() {
		try {
			String sql = "DELETE FROM user";
			return Launcher.getDatabaseConnection().nonTransactQuery(sql) > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean deleteLogs() {
		try {
			String sql = "DELETE FROM logs";
			return Launcher.getDatabaseConnection().nonTransactQuery(sql) > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
