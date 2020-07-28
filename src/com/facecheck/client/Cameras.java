package com.facecheck.client;

import com.facecheck.db.dbList;
import com.facecheck.db.dbRow;
import com.facecheck.tools.Utils;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
