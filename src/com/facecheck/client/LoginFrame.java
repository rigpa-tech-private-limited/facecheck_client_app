package com.facecheck.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

import com.facecheck.db.dbRow;
import com.facecheck.tools.AppInfo;
import com.facecheck.tools.Utils;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private static MainFrame frame;
	private String PID = "";

	public static MainFrame getFrame() {
		return frame;
	}

	private Map<String, JTextField> entries;

	/**
	 * Create the frame.
	 */
	public LoginFrame() {
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("Login");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		lblNewLabel.setBounds(6, 41, 444, 25);
		contentPane.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Username");
		lblNewLabel_1.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblNewLabel_1.setBounds(90, 98, 100, 16);
		contentPane.add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Password");
		lblNewLabel_2.setBounds(90, 144, 100, 16);
		contentPane.add(lblNewLabel_2);

		txtUsername = new JTextField();
		txtUsername.setBounds(219, 93, 188, 26);
		contentPane.add(txtUsername);
		txtUsername.setColumns(10);

		txtPassword = new JPasswordField();
		txtPassword.setBounds(219, 139, 188, 26);
		contentPane.add(txtPassword);

		JButton btnNewButton = new JButton("Submit");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String userName = txtUsername.getText();
				String password = txtPassword.getText();
				try {
					System.out.println("User Login Post API started");
					URL url = new URL(AppInfo.BASE_URL + AppInfo.USER_LOGIN);
					Map<String, String> params = new ConcurrentHashMap<String, String>();
					params.put("email", userName);
					params.put("password", password);
					StringBuilder postData = new StringBuilder();

					byte[] postDataBytes = Utils.setPostDataBytes(params, postData);
					params.clear();
					String response = Utils.getResponse(postDataBytes, url, false);
					System.out.println("User Login Post API stopped");
					System.out.println(response);
					if (new JSONObject(response).get("status").equals("success")) {
						JSONObject obj = new JSONObject(response);
						JSONObject usrData = obj.getJSONObject("data");
						System.out.println("usrData - ");
						System.out.println(usrData);
						String token = obj.getString("token");
						String aws_key = usrData.getString("key");
						String aws_secret = usrData.getString("secret");
						entries = new HashMap<String, JTextField>();
						entries.put("email", new JTextField());
						entries.put("password", new JTextField());
						entries.put("token", new JTextField());
						entries.put("aws_key", new JTextField());
						entries.put("aws_secret", new JTextField());
						entries.put("logged_in", new JTextField());
						if (Cameras.deleteUser()) {
							System.out.println("usr data deleted");
							LoginFrame.this.save(userName, password, token, aws_key, aws_secret);
						} else {
							Utils.alert("User data not deleted");
						}
					} else {
						System.out.println("status - error");
						Utils.alert("Invalid credentials.");
					}
				} catch (Exception e1) {
					System.out.println("Error " + e1.getMessage());
					Utils.alert("Check your connection. Try again later.");
				}
			}
		});
		btnNewButton.setBounds(219, 188, 117, 29);
		contentPane.add(btnNewButton);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	public void save(String email, String password, String token, String aws_key, String aws_secret) {
		dbRow values = getEntries();
		Utils.stdout(values);
		System.out.println("DataValues" + values);

		values.put("email", email);
		values.put("password", password);
		values.put("token", token);
		values.put("aws_key", aws_key);
		values.put("aws_secret", aws_secret);
		values.put("logged_in", "yes");
		System.out.println("User DataValues" + values);
		int saved = Launcher.getDatabaseConnection().save("user", "id", values);
		if (saved > 0) {
			setVisible(false);
			frame = new MainFrame();
			frame.setVisible(true);
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent windowEvent) {
					if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to close this window?",
							"Close Window?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						dbRow data = Cameras.getPID("1");
						System.out.println("PID data : " + data);
						if (data == null) {
							return;
						}
						for (String field : data.keySet()) {
							System.out.println("PID field : " + field);
							System.out.println("PID value : " + data.get("value"));
							if (field.equalsIgnoreCase("value")) {
								PID = data.get("value");
								System.out.println("PIDvalue : " + PID);
								if (PID != "") {
									try {
										Utils.stopVideoStream(PID);
										System.exit(0);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
								}
							}
						}
					}
				}
			});

			frame.setContent(new CameraList(), "Cameras");
			dispose();
		} else {
			Utils.alert("Check your connection. Try again later.");
		}

	}

	private dbRow getEntries() {
		dbRow values = new dbRow();
		for (String name : entries.keySet()) {
			values.put(name, entries.get(name).getText());
		}

		return values;
	}

}
