package com.facecheck.client;

import com.facecheck.db.dbList;
import com.facecheck.db.dbRow;
import com.facecheck.tools.AppInfo;
import com.facecheck.tools.Utils;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.Frame;
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
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import java.awt.GridLayout;
import javax.swing.border.LineBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

/**
 *
 * @author user
 */
public class CameraList extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	public JTable table;
	public JTable logtable;
	private String PID = "";
	public FFmpegFrameGrabber streamGrabber;
	public FFmpegFrameGrabber streamGrabber1;
	public CanvasFrame canvasFrame;
	public JPanel camera_preview;
	public JLabel lblNewLabel;
	public JButton btnStart;
	public JButton btnStop;
	public JButton btnStartAll;
	public JButton btnStopAll;
	public JLabel serviceStatusLbl;
	
	public CameraList() {
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		setMinimumSize(new Dimension(1200, 750));
		setLocationRelativeTo(null);

		initComponents();
		
		initStreamProcess();
		
		Runnable helloRunnable = new Runnable() {
		    public void run() {
			        // System.out.println("Checking Inernet Connection in every 5 secs");
			        initStreamProcess();
		      	}
		};

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(helloRunnable, 0, 5, TimeUnit.SECONDS);
	}	
	
	private void initStreamProcess() {
		if(Utils.checkInternetAvailable()) {
        	dbList tdata = Cameras.loadData(null);
    		Map<String, String> d;
    		int restartServiceCount = 0;
    		for (int i : tdata.keySet()) {
    			d = tdata.get(i);
    			try {
					if(checkRTSPUrlAvailable(d.get("url"))){
						if(cameraStoppedBySystem(d.get("id"))) {
							restartServiceCount++;
							Cameras.updateCameraStatusByID(d.get("camera_id"), "Running","system");
						}
					} else {
						Cameras.updateCameraStatusByID(d.get("camera_id"), "Stopped","system");
					}
				} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
					Cameras.updateCameraStatusByID(d.get("camera_id"), "Stopped","system");
				}
    		}
    		
    		if(restartServiceCount>0) {
				btnStartAll.setEnabled(false);
				cameraStartServiceProcess();
				btnStopAll.setEnabled(true);
				serviceStatusLbl.setText("Satus : Running");
    		}
    		
        } else {
        	
        	dbList tdata = Cameras.loadData(null);
    		Map<String, String> d;
    		for (int i : tdata.keySet()) {
    			d = tdata.get(i);
    			Cameras.updateCameraStatusByID(d.get("camera_id"), "Stopped","system");
    		}
    		
        	btnStopAll.setEnabled(false);
			cameraStopServiceProcess();
			btnStartAll.setEnabled(true);
			serviceStatusLbl.setText("Satus : Stopped");
        }

		loadCamerasFromDB();
	}

	private void initComponents() {
		String[] fields = "id, Name, URL(RTSP), Camera ID, Stream Name, Status".split(", ");
		DefaultTableModel tm = new DefaultTableModel(null, fields) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		
		DefaultTableCellRenderer header = new DefaultTableCellRenderer();
		header.setBackground(new Color(239, 240, 241));
		header.setFont(header.getFont().deriveFont(Font.BOLD));

		String[] logfields = "id, Time, Message, Process, Type".split(", ");
		DefaultTableModel logtm = new DefaultTableModel(null, logfields) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		DefaultTableCellRenderer logheader = new DefaultTableCellRenderer();
		logheader.setBackground(new Color(239, 240, 241));
		logheader.setFont(header.getFont().deriveFont(Font.BOLD));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(6, 6, 438, 266);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// System.out.println("Tab: " + tabbedPane.getSelectedIndex());
				if (tabbedPane.getSelectedIndex() == 1) {
					loadLogData();
				}
			}
		});

		contentPane.setLayout(new BorderLayout());
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel services = new JPanel();
		tabbedPane.addTab("Cameras", null, services, null);
		services.setLayout(new GridLayout(0, 2, 0, 0));

		JPanel panel = new JPanel();
		services.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JLabel lblNewLabel_1 = new JLabel(" ");
		panel.add(lblNewLabel_1);
		Dimension dim = new Dimension(20, 1);

		JPanel btnHolder = new JPanel();
		FlowLayout flowLayout = (FlowLayout) btnHolder.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.add(btnHolder);

		JPanel buttons = new JPanel();
		btnHolder.add(buttons);
		buttons.setMaximumSize(new Dimension(300, 100));
		JButton btnNew = new JButton("Add");
		btnNew.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showInsertForm();
			}
		});
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(btnNew);

		buttons.add(Box.createRigidArea(new Dimension(15, 0)));
		JButton btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showEditForm();
			}
		});
		buttons.add(btnEdit);

		buttons.add(Box.createRigidArea(new Dimension(15, 0)));
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmDelete();
			}
		});
		buttons.add(btnDelete);

		buttons.add(Box.createRigidArea(new Dimension(15, 0)));
		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startCameraByIdProcess("user");
			}
		});
		buttons.add(btnStart);

		buttons.add(Box.createRigidArea(new Dimension(15, 0)));
		btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCameraByIdProcess("user");
			}
		});
		buttons.add(btnStop);

		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		panel.add(tabbedPane_1);
		table = new JTable() {

			private static final long serialVersionUID = 1L;

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
		table.setModel(tm);
		table.setIntercellSpacing(new Dimension(dim));

		int height = table.getRowHeight();
		table.setRowHeight(height + 10);
		table.setAutoCreateRowSorter(false);
		
		table.getColumnModel().getColumn(0).setMinWidth(0);
		table.getColumnModel().getColumn(0).setPreferredWidth(0);
		table.getColumnModel().getColumn(0).setMaxWidth(0);
		table.getColumnModel().getColumn(0).setHeaderRenderer(header);

		table.getColumnModel().getColumn(1).setPreferredWidth(getWidth());
		table.getColumnModel().getColumn(1).setHeaderRenderer(header);

		table.getColumnModel().getColumn(2).setMinWidth(0);
		table.getColumnModel().getColumn(2).setPreferredWidth(0);
		table.getColumnModel().getColumn(2).setMaxWidth(0);
		table.getColumnModel().getColumn(2).setHeaderRenderer(header);

		table.getColumnModel().getColumn(3).setMinWidth(0);
		table.getColumnModel().getColumn(3).setPreferredWidth(0);
		table.getColumnModel().getColumn(3).setMaxWidth(0);
		table.getColumnModel().getColumn(3).setHeaderRenderer(header);

		table.getColumnModel().getColumn(4).setMinWidth(0);
		table.getColumnModel().getColumn(4).setPreferredWidth(0);
		table.getColumnModel().getColumn(4).setMaxWidth(0);
		table.getColumnModel().getColumn(4).setHeaderRenderer(header);

		table.getColumnModel().getColumn(5).setPreferredWidth(getWidth());
		table.getColumnModel().getColumn(5).setHeaderRenderer(header);

		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);

		final TableColumnModel columnModel = table.getColumnModel();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// table row mouse click listener
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 1) {
					// System.out.println("Single Click");
					displayStartStopBtn();
				}
				if (mouseEvent.getClickCount() == 2) {
					try {
						// System.out.println("Double Click");
						new Thread() {
							@Override
							public void run() {
								try {
									Thread.sleep(300);
									showIPCamera();
								} catch (InterruptedException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						}.start();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JScrollPane pane = new JScrollPane(table);
		tabbedPane_1.addTab("Cameras", null, pane, null);
		pane.setSize(new Dimension(100, 300));
		pane.setAlignmentY(Component.TOP_ALIGNMENT);
		pane.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel btn_holder1 = new JPanel();
		panel.add(btn_holder1);
		btn_holder1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JPanel buttons1 = new JPanel();
		btn_holder1.add(buttons1);
		buttons1.setMaximumSize(new Dimension(300, 100));
		buttons1.setSize(new Dimension(100, 100));
		buttons1.add(Box.createRigidArea(new Dimension(5, 0)));

		btnStartAll = new JButton("Start All");
		btnStartAll.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnStartAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnStartAll.setEnabled(false);
				startAllCameraProcess("user");
				btnStopAll.setEnabled(true);
				serviceStatusLbl.setText("Satus : Running");
			}
		});
		buttons1.setLayout(new BoxLayout(buttons1, BoxLayout.X_AXIS));
		buttons1.add(btnStartAll);
		buttons1.add(Box.createRigidArea(new Dimension(10, 0)));

		btnStopAll = new JButton("Stop All");
		btnStopAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnStopAll.setEnabled(false);
				stopAllCameraProcess("user");
				btnStartAll.setEnabled(true);
				serviceStatusLbl.setText("Satus : Stopped");
			}
		});
		buttons1.add(btnStopAll);

		JTabbedPane tabbedPane_2 = new JTabbedPane(JTabbedPane.TOP);
		panel.add(tabbedPane_2);

		JPanel panel_4 = new JPanel();
		tabbedPane_2.addTab("Service", null, panel_4, null);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.Y_AXIS));

		JPanel status_holder = new JPanel();
		panel_4.add(status_holder);
		status_holder.setLayout(new GridLayout(0, 1, 0, 0));

		serviceStatusLbl = new JLabel(" Status : Running");
		serviceStatusLbl.setHorizontalAlignment(SwingConstants.CENTER);
		status_holder.add(serviceStatusLbl);

		camera_preview = new JPanel();
		camera_preview.setBorder(new LineBorder(new Color(0, 0, 0)));
		services.add(camera_preview);
		camera_preview.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 194, 114, 0 };
		gbl_panel_1.rowHeights = new int[] { 15, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		lblNewLabel = new JLabel("Camera Preview");

		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		panel_1.add(lblNewLabel, gbc);
		lblNewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		camera_preview.add(panel_1);

		JPanel logs = new JPanel();
		tabbedPane.addTab("Logs", null, logs, null);
		logs.setLayout(new BoxLayout(logs, BoxLayout.Y_AXIS));

		JLabel lblNewLabel_2 = new JLabel(" ");
		logs.add(lblNewLabel_2);

		logtable = new JTable() {

			private static final long serialVersionUID = 1L;

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

		logtable.setModel(logtm);
		Dimension logdim = new Dimension(20, 1);
		logtable.setIntercellSpacing(new Dimension(logdim));

		int logheight = logtable.getRowHeight();
		logtable.setRowHeight(logheight + 10);
		logtable.setAutoCreateRowSorter(false);
		
		logtable.getColumnModel().getColumn(0).setMinWidth(0);
		logtable.getColumnModel().getColumn(0).setPreferredWidth(0);
		logtable.getColumnModel().getColumn(0).setMaxWidth(0);
		logtable.getColumnModel().getColumn(0).setHeaderRenderer(logheader);

		logtable.getColumnModel().getColumn(1).setPreferredWidth(getWidth());
		logtable.getColumnModel().getColumn(1).setHeaderRenderer(logheader);
		
		logtable.getColumnModel().getColumn(2).setPreferredWidth(getWidth());
		logtable.getColumnModel().getColumn(2).setHeaderRenderer(logheader);

		logtable.getColumnModel().getColumn(3).setMinWidth(0);
		logtable.getColumnModel().getColumn(3).setPreferredWidth(0);
		logtable.getColumnModel().getColumn(3).setMaxWidth(0);
		logtable.getColumnModel().getColumn(3).setHeaderRenderer(logheader);

		logtable.getColumnModel().getColumn(4).setMinWidth(0);
		logtable.getColumnModel().getColumn(4).setPreferredWidth(0);
		logtable.getColumnModel().getColumn(4).setMaxWidth(0);
		logtable.getColumnModel().getColumn(4).setHeaderRenderer(logheader);
		
		logtable.setColumnSelectionAllowed(false);
		logtable.setRowSelectionAllowed(true);

		final TableColumnModel logcolumnModel = logtable.getColumnModel();
		for (int column = 0; column < logtable.getColumnCount(); column++) {
			int width = 15; // Min width
			for (int row = 0; row < logtable.getRowCount(); row++) {
				TableCellRenderer renderer = logtable.getCellRenderer(row, column);
				Component comp = logtable.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > 300)
				width = 300;
			logcolumnModel.getColumn(column).setPreferredWidth(width);
		}

		logtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane logPane = new JScrollPane(logtable);
		logPane.setAlignmentY(Component.TOP_ALIGNMENT);
		logPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		logs.add(logPane);
	}

	public void cameraStartServiceProcess() {
		dbRow data = Cameras.getPID("1");
		// System.out.println("PID data : " + data);
		if (data == null) {
			return;
		}
		for (String field : data.keySet()) {
			if (field.equalsIgnoreCase("value")) {
				PID = data.get("value");
				// System.out.println("PIDvalue : " + PID);
				if (PID != "") {
					try {
						Utils.stopVideoStream(PID);
						new Thread() {
							@Override
							public void run() {
								try {
									Thread.sleep(1000);
									// System.out.println("1 sec Delay");
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
									Thread.sleep(2000);
									// System.out.println("2 sec Delay");
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
								Thread.sleep(1000);
								// System.out.println("1 sec Delay");
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

	public void cameraStopServiceProcess() {
		dbRow data = Cameras.getPID("1");
		// System.out.println("PID data : " + data);
		if (data == null) {
			return;
		}
		for (String field : data.keySet()) {
			if (field.equalsIgnoreCase("value")) {
				PID = data.get("value");
				// System.out.println("PIDvalue : " + PID);
				if (PID != "") {
					try {
						Utils.stopVideoStream(PID);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public void startAllCameraProcess(String updated_by) {

		dbList tdata = Cameras.loadData(null);
		Map<String, String> d;
		for (int i : tdata.keySet()) {
			d = tdata.get(i);
			Cameras.updateCameraStatusByID(d.get("camera_id"), "Running", updated_by);
		}
		cameraStartServiceProcess();
		loadCamerasFromDB();
	}

	public void stopAllCameraProcess(String updated_by) {
		dbList tdata = Cameras.loadData(null);
		Map<String, String> d;
		for (int i : tdata.keySet()) {
			d = tdata.get(i);
			Cameras.updateCameraStatusByID(d.get("camera_id"), "Stopped", updated_by);
		}
		cameraStopServiceProcess();
		loadCamerasFromDB();
	}
	
	public void startCameraByIdProcess(String updated_by) {

		String id = validateSelectedID();
		String camera_id = validateSelectedCameraID();
		if (id.trim().isEmpty()) {
			return;
		}

		if (Cameras.updateCameraStatusByID(camera_id, "Running",updated_by)) {
			try {
				cameraStartServiceProcess();
				loadCamerasFromDB();
			} catch (Exception e) {
				System.out.println("Error " + e.getMessage());
			}
		}

	}

	public void stopCameraByIdProcess(String updated_by) {
		String id = validateSelectedID();
		String camera_id = validateSelectedCameraID();
		if (id.trim().isEmpty()) {
			return;
		}

		if (Cameras.updateCameraStatusByID(camera_id, "Stopped",updated_by)) {
			try {
				cameraStartServiceProcess();
				loadCamerasFromDB();
			} catch (Exception e) {
				System.out.println("Error " + e.getMessage());
			}
		}
	}

	public void displayStartStopBtn() {
		String id = validateSelectedID();
		String camStatus = "";
		dbRow data = Cameras.fetchCameraStatus(id);
		// System.out.println("Status data : " + data);
		if (data == null) {
			return;
		}
		for (String field : data.keySet()) {
			if (field.equalsIgnoreCase("status")) {
				camStatus = data.get("status");
				// System.out.println("camStatus : " + camStatus);
				if (camStatus.equalsIgnoreCase("Running")) {
					btnStart.setEnabled(false);
					btnStop.setEnabled(true);
				} else if (camStatus.equalsIgnoreCase("Stopped")) {
					btnStart.setEnabled(true);
					btnStop.setEnabled(false);
				}
			}
		}
	}
	
	public boolean cameraStoppedBySystem(String id) {
		dbRow data = Cameras.fetchCameraStatus(id);
		// System.out.println("Status data : " + data);
		if (data == null) {
			return false;
		} else {
			for (String field : data.keySet()) {
				if (field.equalsIgnoreCase("status")) {
					// System.out.println("camStatus : " + data.get("status"));
					if (data.get("status").equalsIgnoreCase("Stopped") && data.get("last_update_by").equalsIgnoreCase("system")) {
						return true;
					} else {
						return false;
					}
				}
			}	
		}
		return false;
	}
	
	public boolean checkRTSPUrlAvailable(String url) throws com.googlecode.javacv.FrameGrabber.Exception {
		streamGrabber1 = new FFmpegFrameGrabber(url);
  		streamGrabber1.setFrameRate(100);
  		streamGrabber1.setImageWidth(getWidth());
  		streamGrabber1.setImageHeight(getHeight());

  		try {
  			streamGrabber1.start();
  			IplImage iPimg = streamGrabber1.grab();
	  		if((iPimg = streamGrabber1.grab()) != null) {
	  			System.out.println(url+ " Streaming");
	  			return true;
	  		} else {
	  			System.out.println(url+ " Not Streaming");
	  		}
  		} catch (FrameGrabber.Exception e) {
  			System.out.println(url+ " not Streaming ");	
  		} finally {
  	  		streamGrabber1.stop();
  		}
  		return false;
	}

	public void showIPCamera() throws Exception {

		String id = validateSelectedID();
		String camera_url = validateSelectedCameraURL();
		if (id.trim().isEmpty()) {
			return;
		}

		streamGrabber = new FFmpegFrameGrabber(camera_url);
		streamGrabber.setFrameRate(50);
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
		while (canvasFrame.isVisible() && (iPimg = streamGrabber.grab()) != null) {
			// System.out.println("canvasFrame.isVisible() " + canvasFrame.isVisible());
			canvasFrame.showImage(iPimg);
		}
		streamGrabber.stop();
		canvasFrame.dispose();
	}

	public void stopIPCamera() throws Exception {
		// System.out.println("stopIPCamera ");
		streamGrabber.stop();
		canvasFrame.dispose();
	}
	
	public void loadCamerasFromDB() {
		dbList tdata = Cameras.loadData(null);
		DefaultTableModel tm = (DefaultTableModel) table.getModel();
		tm.setRowCount(0);
		Map<String, String> d;
		for (int i : tdata.keySet()) {
			d = tdata.get(i);
			tm.addRow(new Object[] { d.get("id"), d.get("name"), d.get("url"), d.get("camera_id"), d.get("stream_name"),
					d.get("status") });
		}
	}

	public void loadLogData() {
		dbList tdata = Cameras.loadLogData();
		DefaultTableModel tm = (DefaultTableModel) logtable.getModel();
		tm.setRowCount(0);
		Map<String, String> d;
		for (int i : tdata.keySet()) {
			d = tdata.get(i);
			tm.addRow(new Object[] { d.get("id"), d.get("log_time"), d.get("message"), d.get("process_name"),
					d.get("type") });
		}
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

		loadCamerasFromDB();
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

		loadCamerasFromDB();
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
				// System.out.println("Delete Camera Post API started");
				URL url = new URL(AppInfo.BASE_URL + AppInfo.DELETE_CAMERA);
				Map<String, String> params = new ConcurrentHashMap<String, String>();
				params.put("camera_id", camera_id);
				StringBuilder postData = new StringBuilder();

				byte[] postDataBytes = Utils.setPostDataBytes(params, postData);
				params.clear();
				String response = Utils.getResponse(postDataBytes, url, true);
				// System.out.println("Delete Camera Post API stopped");
				System.out.println(response);
				if (new JSONObject(response).get("status").equals("success")) {
					// System.out.println("status - success");
					if (Cameras.delete(id)) {
						Utils.alert("Data deleted");
						loadCamerasFromDB();
					} else {
						Utils.alert("Data not deleted");
					}
				} else {
					// System.out.println("status - error");
					Utils.alert("Data not deleted");
				}
			} catch (Exception e) {
				// System.out.println("Error " + e.getMessage());
				Utils.alert("Data not deleted");
			}
		}
	}
}