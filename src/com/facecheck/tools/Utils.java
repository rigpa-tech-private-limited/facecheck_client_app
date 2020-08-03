package com.facecheck.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.facecheck.client.CameraList;
import com.facecheck.client.Cameras;
import com.facecheck.client.Launcher;
import com.facecheck.db.dbRow;

/**
 *
 * @author user
 */
public class Utils {
	private static Map<String, JTextField> entries;

	public static void stdout(Object o) {
		System.out.println(o);
	}

	public static String escapeSQLVar(String sql) {
		return escapeSQLVar(sql, true);
	}

	// source, stackoverflow.com
	public static String escapeSQLVar(String sql, boolean escapeDoubleQuotes) {
		StringBuilder sBuilder = new StringBuilder(sql.length() * 11 / 10);

		int stringLength = sql.length();

		for (int i = 0; i < stringLength; ++i) {
			char c = sql.charAt(i);

			switch (c) {
			case 0: /* Must be escaped for 'mysql' */
				sBuilder.append('\\');
				sBuilder.append('0');

				break;

			case '\n': /* Must be escaped for logs */
				sBuilder.append('\\');
				sBuilder.append('n');

				break;

			case '\r':
				sBuilder.append('\\');
				sBuilder.append('r');

				break;

			case '\\':
				sBuilder.append('\\');
				sBuilder.append('\\');

				break;

			case '\'':
				sBuilder.append('\\');
				sBuilder.append('\'');

				break;

			case '"': /* Better safe than sorry */
				if (escapeDoubleQuotes) {
					sBuilder.append('\\');
				}

				sBuilder.append('"');

				break;

			case '\032': /* This gives problems on Win32 */
				sBuilder.append('\\');
				sBuilder.append('Z');

				break;

			case '\u00a5':
			case '\u20a9':
				// escape characters interpreted as backslash by mysql
				// fall through

			default:
				sBuilder.append(c);
			}
		}

		return sBuilder.toString();
	}

	public static void alert(String msg) {
		JOptionPane.showMessageDialog(null, msg, "Alert", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {

		}
	}

	public static String prompt(String title) {
		return Utils.prompt(title, "");
	}

	public static String prompt(String title, String def) {
		String str = null;
		if (def.trim().equals("")) {
			str = JOptionPane.showInputDialog(null, title, AppInfo.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
		} else {
			str = (String) JOptionPane.showInputDialog(null, title, AppInfo.APP_NAME, JOptionPane.INFORMATION_MESSAGE,
					null, null, def);
		}
		return str;
	}

	public static boolean confirm(String msg) {
		Object[] btn = new String[] { "Yes", "Cancel" };
		int opt = chooseOption(btn, msg, "Delete");
		return opt == 0;
	}

	public static int chooseOption(Object[] buttons, String msg, String title) {
		int opt = JOptionPane.showOptionDialog(null, msg, title, JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, buttons, null);
		return opt;
	}

	/**
	 * This method will convert the map data into byte array payload in order to
	 * send in a post request
	 * 
	 * @param params   post data payload
	 * 
	 * @param postData convert the map data into string payload
	 * 
	 * @return byte array of post data to send as payload in post request
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] setPostDataBytes(Map<String, String> params, StringBuilder postData)
			throws UnsupportedEncodingException {

		for (Map.Entry<String, String> param : params.entrySet()) {
			if (postData.length() != 0)
				postData.append("&");

			postData.append(URLEncoder.encode(param.getKey(), "UTF-8")); // TODO Auto-generated catch block
			postData.append("=");
			postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
		}

		return postData.toString().getBytes("UTF-8");
	}
	
	public static boolean checkInternetAvailable() {
        try { 
            URL checkurl = new URL("https://www.google.com/"); 
            URLConnection connection = checkurl.openConnection(); 
            connection.connect();
            // System.out.println("Internet connection available.");
            return true;
        } 
        catch (Exception e) { 
            System.out.println("Internet connection not available"); 
            return false;
        }
	}

	/**
	 * This method will make post request and parse the obtained response and send
	 * it back
	 * 
	 * @param postDataBytes contains payload data in byte array
	 * 
	 * @param url           URL of the post request
	 * 
	 * @return response of post request
	 * 
	 * @throws IOException
	 * 
	 */
	public static String getResponse(byte[] postDataBytes, URL url, boolean incldeToken) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		try {
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			if (incldeToken) {
				String userToken = "";
				dbRow data = Cameras.getUserToken();
				// System.out.println("User data : " + data);
				if (data != null) {
					for (String field : data.keySet()) {
						if (field.equalsIgnoreCase("token")) {
							userToken = data.get("token");
							if (userToken != "") {
								// System.out.println("Authorization userToken : " + userToken);
								con.setRequestProperty("Authorization", "Bearer " + userToken);
							}
						}
					}
				}
			}

			con.setRequestProperty("Content-Length", String.valueOf(postDataBytes));
			con.setDoOutput(true);
			con.getOutputStream().write(postDataBytes);

			Reader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

			StringBuilder sb = new StringBuilder();
			for (int c; (c = in.read()) >= 0;)
				sb.append((char) c);
			return sb.toString();
		} finally {
			if (con != null)
				con.disconnect();
		}

	}

	public static void startVideoStream() throws InterruptedException {
//      String[] command = {"xterm", "-e", "/home/rigpa/Documents/aws-kinesis/amazon-kinesis-video-streams-producer-sdk-cpp/build/kvs_gstreamer_multistream_sample","&"};
		String[] command = {
				"/home/rigpa/Documents/aws-kinesis/amazon-kinesis-video-streams-producer-sdk-cpp/build/kvs_gstreamer_multistream_sample",
				"&" };
//		String[] command = { "pwd" };

		ProcessBuilder processBuilder = new ProcessBuilder(command);

		processBuilder.directory(new File(System.getProperty("user.home")));

		try {
			// System.out.println("StartStream Process has started :( "+ "\n");
			
			entries = new HashMap<String, JTextField>();
			entries.put("log_time", new JTextField());
			entries.put("message", new JTextField());
			entries.put("process_name", new JTextField());
			entries.put("type", new JTextField());
			
			dbRow values = new dbRow();
			for (String name : entries.keySet()) {
				values.put(name, entries.get(name).getText());
			}
			Utils.stdout(values);
			// System.out.println("DataValues" + values);
			Date date = new Date(); 
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			System.out.println(formatter.format(date));
			String date_time = formatter.format(date);
			
			values.put("log_time", date_time);
			values.put("message", "Starting Streaming Service..");
			values.put("process_name", "Stream Service");
			values.put("type", "stream");
			// System.out.println("Logs DataValues" + values);
			
			int saved = Launcher.getDatabaseConnection().save("logs", "id", values);
			
			/*if (saved > 0) {
				System.out.println("Logs Data saved");
			} else {
				System.out.println("Logs Data not saved.");
			}*/
			
			Process process = processBuilder.start();

			StringBuilder output = new StringBuilder();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;

			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
				output.append(line + "\n");
			}

			int exitVal = process.waitFor();
			if (exitVal == 0) {
				System.out.println("StartStream Process has completed"+ "\n");
				// System.out.println(output);
			} else {
				System.out.println("Something abnormal has happend in StartStream Process"+ "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void stopVideoStream(String pid) throws InterruptedException {
		String[] command = { "kill", "-9", pid };

		ProcessBuilder processBuilder = new ProcessBuilder(command);

		processBuilder.directory(new File(System.getProperty("user.home")));

		try {
			System.out.println("StopStream Process has started :( "+ "\n");
			Process process = processBuilder.start();

			StringBuilder output = new StringBuilder();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;

			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
				output.append(line + "\n");
			}

			int exitVal = process.waitFor();
			if (exitVal == 0) {
				System.out.println("StopStream Process has completed"+ "\n");
				//System.out.println(output);
				
				entries = new HashMap<String, JTextField>();
				entries.put("log_time", new JTextField());
				entries.put("message", new JTextField());
				entries.put("process_name", new JTextField());
				entries.put("type", new JTextField());
				
				dbRow values = new dbRow();
				for (String name : entries.keySet()) {
					values.put(name, entries.get(name).getText());
				}
				Utils.stdout(values);
				//System.out.println("DataValues" + values);
				Date date = new Date(); 
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				System.out.println(formatter.format(date));
				String date_time = formatter.format(date);
				
				values.put("log_time", date_time);
				values.put("message", "Streaming Service PID("+pid+") has stopped");
				values.put("process_name", "Stream Service");
				values.put("type", "stream");
				//System.out.println("Logs DataValues" + values);
				
				int saved = Launcher.getDatabaseConnection().save("logs", "id", values);
				
				/*if (saved > 0) {
					System.out.println("Logs Data saved");
				} else {
					System.out.println("Logs Data not saved.");
				}*/
			} else {
				System.out.println("Something abnormal has happend in StopStream Process"+ "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getPIDVideoStream() throws InterruptedException {

		String[] command = { "pidof", "kvs_gstreamer_multistream_sample" };

		ProcessBuilder processBuilder = new ProcessBuilder(command);

		processBuilder.directory(new File(System.getProperty("user.home")));

		try {
			System.out.println("Getting pID process has started :( "+ "\n");
			Process process = processBuilder.start();

			StringBuilder output = new StringBuilder();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			String Pid = "";

			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
				Pid = line;
				output.append(line + "\n");
			}

			int exitVal = process.waitFor();
			if (exitVal == 0) {
				System.out.println("pID process has Completed"+ "\n");
				System.out.println(output);
				if (Cameras.updatePID(Pid)) {
					//System.out.println("PID has updated"+ "\n");
					
					entries = new HashMap<String, JTextField>();
					entries.put("log_time", new JTextField());
					entries.put("message", new JTextField());
					entries.put("process_name", new JTextField());
					entries.put("type", new JTextField());
					
					dbRow values = new dbRow();
					for (String name : entries.keySet()) {
						values.put(name, entries.get(name).getText());
					}
					Utils.stdout(values);
					// System.out.println("DataValues" + values);
					Date date = new Date(); 
					SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					System.out.println(formatter.format(date));
					String date_time = formatter.format(date);
					
					values.put("log_time", date_time);
					values.put("message", "Streaming Service PID("+Pid+") has started");
					values.put("process_name", "Stream Service");
					values.put("type", "stream");
					// System.out.println("Logs DataValues" + values);
					
					int saved = Launcher.getDatabaseConnection().save("logs", "id", values);
					
					/*if (saved > 0) {
						System.out.println("Logs Data saved");
					} else {
						System.out.println("Logs Data not saved.");
					}*/
				} else {
					//System.out.println("PID has not updated"+ "\n");
				}
			} else {
				System.out.println("Something abnormal has happend in pID process"+ "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
