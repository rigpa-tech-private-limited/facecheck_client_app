package com.facecheck.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import javax.swing.JOptionPane;

import com.facecheck.client.Cameras;

/**
 *
 * @author user
 */
public class Utils {

    public static void stdout(Object o) {
        System.out.println(o);
    }
    public static String escapeSQLVar(String sql) {
        return escapeSQLVar(sql, true);
    }

    // source, stackoverflow.com
    public static String escapeSQLVar(String sql, boolean escapeDoubleQuotes) {
        StringBuilder sBuilder = new StringBuilder(sql.length() * 11/10);

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
        JOptionPane.showMessageDialog(null, msg,
                "Alert", JOptionPane.INFORMATION_MESSAGE);
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
            str = JOptionPane.showInputDialog(null, title, AppInfo.APP_NAME,
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            str = (String) JOptionPane.showInputDialog(null, title, AppInfo.APP_NAME,
                    JOptionPane.INFORMATION_MESSAGE, null, null, def);
        }
        return str;
    }

    public static boolean confirm(String msg) {
        Object[] btn = new String[]{"Yes", "Cancel"};
        int opt = chooseOption(btn, msg, "Delete");
        return opt == 0;
    }

    public static int chooseOption(Object[] buttons, String msg, String title) {
        int opt = JOptionPane.showOptionDialog(null, msg,
                title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null,
                buttons, null);
        return opt;
    }
    
    
    /**
	 * This method will convert the map data into byte array payload in order to send
	 * in a post request
	 * 
	 * @param params
	 * 		  post data payload
	 * 
	 * @param postData
	 * 		  convert the map data into string payload
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

	/**
	 * This method will make post request and parse the obtained response
	 * and send it back
	 * 
	 * @param postDataBytes
	 * 		  contains payload data in byte array
	 * 
	 * @param url
	 * 		  URL of the post request
	 * 
	 * @return response of post request
	 * 
	 * @throws IOException
	 * 
	 */
	public static String getResponse(byte[] postDataBytes, URL url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		try {
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
//      String[] command = {"xterm", "-e", "/home/rigpa/Documents/aws-kinesis/amazon-kinesis-video-streams-producer-sdk-cpp/build/kvs_gstreamer_multistream_sample", "/home/rigpa/Desktop/inputs","&"};
    	String[] command = {"/home/rigpa/Documents/aws-kinesis/amazon-kinesis-video-streams-producer-sdk-cpp/build/kvs_gstreamer_multistream_sample", "/home/rigpa/Desktop/inputs", "&"};

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(new File(System.getProperty("user.home")));

        try {
            System.out.println("processBuilder has started :( ");
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success");
                System.out.println(output);
            } else {
                System.out.println("Something abnormal has haapened :( ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopVideoStream(String pid) throws InterruptedException {
    	String[] command = {"kill", "-9", pid};

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(new File(System.getProperty("user.home")));

        try {
            System.out.println("processBuilder has started :( ");
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success");
                System.out.println(output);
            } else {
                System.out.println("Something abnormal has haapened :( ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getPIDVideoStream() throws InterruptedException {
    	
    	
    	String[] command = {"pidof", "kvs_gstreamer_multistream_sample"};

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(new File(System.getProperty("user.home")));

        try {
            System.out.println("processBuilder has started :( ");
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            String Pid = "";

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                Pid = line;
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success");
                System.out.println(output);
                if(Cameras.updatePID(Pid)) {
                	System.out.println("PID updated");
                } else {
                	System.out.println("PID not updated");                	
                }
            } else {
                System.out.println("Something abnormal has haapened :( ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
