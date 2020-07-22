package com.facecheck.client;

import com.facecheck.db.dbList;
import com.facecheck.db.dbRow;
import com.facecheck.tools.AppInfo;
import com.facecheck.tools.Utils;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

//import com.googlecode.javacv.CanvasFrame;
//import com.googlecode.javacv.OpenCVFrameGrabber;
//import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 *
 * @author user
 */
public class CameraList extends JPanel {

	public CameraList() {
		super(new BorderLayout());
		initComponents();
		loadAllData();
	}

	private JTable table;
	private JPanel toolbar;
	private String PID = "";
	public FFmpegFrameGrabber streamGrabber;
	public CanvasFrame canvasFrame;
	private void initComponents() {
		String[] fields = "id, Name, URL(RTSP), Camera ID, Stream Name".split(", ");
		DefaultTableModel tm = new DefaultTableModel(null, fields) {
			@Override
			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		table = new JTable() {
			@Override
			protected void paintComponent(Graphics g) {
				try {
					super.paintComponent(g);
					if (getRowCount() == 0) {
						Graphics2D g2d = (Graphics2D) g;
						Font prev = g.getFont();
						Font italic = prev.deriveFont(Font.ITALIC);
						g.setFont(italic);
						RenderingHints hints = g2d.getRenderingHints();
						g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
								RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
						g2d.drawString("No Cameras Found.", 10, 20);
						g2d.setRenderingHints(hints);
					}
				} catch (Exception e) {

				}
			}
		};
//        table.setTableHeader(null);
		DefaultTableCellRenderer header = new DefaultTableCellRenderer();
		header.setBackground(new Color(239, 240, 241));
		header.setFont(header.getFont().deriveFont(Font.BOLD));

		table.setModel(tm);
		table.setAutoCreateRowSorter(false);
		table.getColumnModel().getColumn(0).setMinWidth(0);
		table.getColumnModel().getColumn(0).setPreferredWidth(0);
		table.getColumnModel().getColumn(0).setMaxWidth(0);
		table.getColumnModel().getColumn(0).setHeaderRenderer(header);

		int tW = table.getWidth();
		int nameWidth = Math.round(0.30f * tW);
//        table.getColumnModel().getColumn(1).setMinWidth(nameWidth);
		table.getColumnModel().getColumn(1).setPreferredWidth(nameWidth);
//        table.getColumnModel().getColumn(1).setMaxWidth(nameWidth);
		table.getColumnModel().getColumn(1).setHeaderRenderer(header);

		int urlWidth = Math.round(0.70f * tW);
//        table.getColumnModel().getColumn(2).setMinWidth(urlWidth);
		table.getColumnModel().getColumn(2).setPreferredWidth(urlWidth);
//        table.getColumnModel().getColumn(2).setMaxWidth(urlWidth);
		table.getColumnModel().getColumn(2).setHeaderRenderer(header);

		table.getColumnModel().getColumn(3).setMinWidth(0);
		table.getColumnModel().getColumn(3).setPreferredWidth(0);
		table.getColumnModel().getColumn(3).setMaxWidth(0);
		table.getColumnModel().getColumn(3).setHeaderRenderer(header);

		table.getColumnModel().getColumn(4).setMinWidth(0);
		table.getColumnModel().getColumn(4).setPreferredWidth(0);
		table.getColumnModel().getColumn(4).setMaxWidth(0);
		table.getColumnModel().getColumn(4).setHeaderRenderer(header);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// double click to edit
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					try {

						new Thread() {
							@Override
							public void run() {
								try {
									this.sleep(1000);
									System.out.println("1 sec Delay showIPCamera");
									showIPCamera();
								} catch (InterruptedException e) {
									e.printStackTrace();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}.start();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		JScrollPane pane = new JScrollPane(table);
		add(pane, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnNew = new JButton("Add");
		btnNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showInsertForm();
			}
		});
		buttons.add(btnNew);

		JButton btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showEditForm();
			}
		});
		buttons.add(btnEdit);

		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmDelete();
			}
		});
		buttons.add(btnDelete);

		/*JButton btnStrat = new JButton("Start");
		btnStrat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					new Thread() {
						@Override
						public void run() {
							try {
								this.sleep(1000);
								System.out.println("1 sec Delay showIPCamera");
								showIPCamera();
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}.start();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		buttons.add(btnStrat);

		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new Thread() {
						@Override
						public void run() {
							try {
								this.sleep(1000);
								System.out.println("1 sec Delay stopIPCamera");

								stopIPCamera();
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}.start();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		buttons.add(btnStop);*/

		toolbar = new JPanel(new BorderLayout());
		toolbar.add(buttons, BorderLayout.WEST);

		// search bar
		/*
		 * JPanel searches = new JPanel(new FlowLayout(FlowLayout.LEFT));
		 * searches.add(new JLabel("Search")); txtSearch = new JTextField();
		 * txtSearch.addKeyListener(new KeyAdapter() {
		 * 
		 * @Override public void keyReleased(KeyEvent keyEvent) {
		 * filterSearch(txtSearch.getText()); } }); txtSearch.setColumns(30);
		 * searches.add(txtSearch);
		 * 
		 * toolbar.add(searches, BorderLayout.CENTER);
		 */
		add(toolbar, BorderLayout.NORTH);
	}

	private JTextField txtSearch;

	public void loadAllData() {
		/*
		 * if(!txtSearch.getText().isEmpty()){ filterSearch(txtSearch.getText()); } else
		 * { loadData(null); }
		 */
		loadData(null);
	}

	public void showIPCamera() throws Exception {
		
		String id = validateSelectedID();
		String camera_url = validateSelectedCameraURL();
		if (id.trim().isEmpty()) {
			return;
		}
//        OpenCVFrameGrabber frameGrabber = new OpenCVFrameGrabber("rtsp://182.65.121.30:554");
//        frameGrabber.setFormat("mjpeg");
//        frameGrabber.start();
		// rtsp://admin:admin123@192.168.1.10:554/channel=1/subtype=0
		
		streamGrabber = new FFmpegFrameGrabber(camera_url);
//        streamGrabber.setFormat("h264");
		streamGrabber.setFrameRate(100);
		streamGrabber.setImageWidth(getWidth());
		streamGrabber.setImageHeight(getHeight());

		try {
			streamGrabber.start();
		} catch (FrameGrabber.Exception e) {
			e.printStackTrace();
		}
		IplImage iPimg = streamGrabber.grab();
		canvasFrame = new CanvasFrame("Camera");
		canvasFrame.setCanvasSize(iPimg.width(), iPimg.height());
		canvasFrame.setLocationRelativeTo(null);
//		canvasFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		while (canvasFrame.isVisible() && (iPimg = streamGrabber.grab()) != null) {
			System.out.println("canvasFrame.isVisible() "+canvasFrame.isVisible());
			canvasFrame.showImage(iPimg);
		}
		streamGrabber.stop();
		canvasFrame.dispose();
	}
	
	public void stopIPCamera() throws Exception {
		System.out.println("stopIPCamera ");
		streamGrabber.stop();
		canvasFrame.dispose();
	}

	public void loadData(dbRow filter) {
		setData(Cameras.loadData(filter));
	}

	private void setData(dbList tdata) {
		DefaultTableModel tm = (DefaultTableModel) table.getModel();
		tm.setRowCount(0);
		Map<String, String> d;
		Map<String, String> dt;

		for (int i : tdata.keySet()) {
			d = tdata.get(i);
			tm.addRow(new Object[] { d.get("id"), d.get("name"), d.get("url"), d.get("camera_id"),
					d.get("stream_name") });
			System.out.println("Stream Name : " + d.get("stream_name") + "####" + d.get("url"));
		}
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter("/home/rigpa/Desktop/inputs"));
			for (int i : tdata.keySet()) {
				dt = tdata.get(i);
				pw.write(dt.get("stream_name") + "####" + dt.get("url") + "\n");
			}
			pw.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		if ((tdata != null)) {
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
							new Thread() {
								@Override
								public void run() {
									try {
										this.sleep(1000);
										System.out.println("1 sec Delay");
										Utils.startVideoStream();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

								}
							}.start();
							new Thread() {
								@Override
								public void run() {
									try {
										this.sleep(5000);
										System.out.println("10 sec Delay");
										Utils.getPIDVideoStream();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

								}
							}.start();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					} else {
						new Thread() {
							@Override
							public void run() {
								try {
									this.sleep(1000);
									System.out.println("1 sec Delay");
									Utils.startVideoStream();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

							}
						}.start();
					}
				}
			}
		}

	}

	public void filterSearch(String search) {
		setData(Cameras.searchByName(search));
	}

	private String validateSelectedID() {
		int selected = table.getSelectedRow();
		if (selected < 0) {
			Utils.alert("No Camera Selected.");
			return "";
		}

		return table.getValueAt(selected, 0).toString();
	}

	private String validateSelectedCameraID() {
		int selected = table.getSelectedRow();
		if (selected < 0) {
			return "";
		}
		return table.getValueAt(selected, 3).toString();
	}
	
	private String validateSelectedCameraURL() {
		int selected = table.getSelectedRow();
		if (selected < 0) {
			return "";
		}
		return table.getValueAt(selected, 2).toString();
	}

	private void showInsertForm() {
		CameraForm form = new CameraForm(Launcher.getFrame(), "Add Camera");
		form.setVisible(true);

		// load cameras from DB
		loadAllData();
	}

	private void showEditForm() {
		String id = validateSelectedID();
		String camera_id = validateSelectedCameraID();
		if (id.trim().isEmpty()) {
			return;
		}

		CameraForm form = new CameraForm(Launcher.getFrame(), "Edit Camera");
		form.loadData(id, camera_id);
		form.setVisible(true);

		// load cameras from DB
		loadAllData();
	}

	private void confirmDelete() {
		String id = validateSelectedID();
		String camera_id = validateSelectedCameraID();
		if (id.trim().isEmpty()) {
			return;
		}

		String name = table.getValueAt(table.getSelectedRow(), 1).toString();
		if (Utils.confirm(String.format("Are you sure want to delete %s ?", name))) {
			try {
				System.out.println("Delete Camera Post API started");
				URL url = new URL(AppInfo.BASE_URL + AppInfo.DELETE_CAMERA);
				Map<String, String> params = new ConcurrentHashMap<String, String>();
				params.put("camera_id", camera_id);
				StringBuilder postData = new StringBuilder();

				byte[] postDataBytes = Utils.setPostDataBytes(params, postData);
				params.clear();
				String response = Utils.getResponse(postDataBytes, url);
				System.out.println("Delete Camera Post API stopped");
				System.out.println(response);
				if (new JSONObject(response).get("status").equals("success")) {
					System.out.println("status - success");
					if (Cameras.delete(id)) {
						Utils.alert("Data deleted");
						loadAllData();
					} else {
						Utils.alert("Data not deleted");
					}
				} else {
					System.out.println("status - error");
					Utils.alert("Data not deleted");
				}
			} catch (Exception e) {
				System.out.println("Error " + e.getMessage());
				Utils.alert("Data not deleted");
			}
		}
	}
}
