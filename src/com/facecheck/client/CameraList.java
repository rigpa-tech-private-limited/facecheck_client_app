package com.facecheck.client;

import com.facecheck.db.dbList;
import com.facecheck.db.dbRow;
import com.facecheck.tools.AppInfo;
import com.facecheck.tools.Utils;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
                        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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
                        //showEditForm();
                        //showIPCamera();
                        executeShellCommand();
                    } catch (Exception ex) {
                        Logger.getLogger(CameraList.class.getName()).log(Level.SEVERE, null, ex);
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

        toolbar = new JPanel(new BorderLayout());
        toolbar.add(buttons, BorderLayout.WEST);

        // search bar
        /*JPanel searches = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searches.add(new JLabel("Search"));
        txtSearch = new JTextField();
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                filterSearch(txtSearch.getText());
            }
        });
        txtSearch.setColumns(30);
        searches.add(txtSearch);
        
        toolbar.add(searches, BorderLayout.CENTER);*/
        add(toolbar, BorderLayout.NORTH);
    }
    private JTextField txtSearch;

    public void loadAllData() {
        /*if(!txtSearch.getText().isEmpty()){
            filterSearch(txtSearch.getText());
        } else {
            loadData(null);
        }*/
        loadData(null);
    }

    public void executeShellCommand() throws InterruptedException {
        String[] command = {"./kinesis_video_gstreamer_sample_multistream_app"};

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(new File(System.getProperty("user.home")));

        try {
            System.out.println("processBuilder has started :( ");
            Process process = processBuilder.start();

//            BufferedReader reader = new BufferedReader (new InputStreamReader(process.getInputStream()));
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
                System.exit(0);
            } else {
                System.out.println("Something abnormal has haapened :( ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//		try {
//                        System.out.println("executeShellCommand has started :( ");
//			Process process = Runtime.getRuntime().exec("traceroute google.com"); 
//			
//			StringBuilder output = new StringBuilder(); 
//			
//			BufferedReader reader = new BufferedReader(new InputStreamReader (process.getInputStream()));
//			
//			String line;
//			
//			while((line = reader.readLine()) != null) {
//				output.append(line + "\n");
//			}
//			
//			int exitVal = process.waitFor();
//			if (exitVal == 0) {
//				System.out.println("Success");
//				System.out.println(output);
//				System.exit(0);
//			} else {
//				System.out.println("Something abnormal has haapened :( ");
//			}
//				
//		} catch (IOException | InterruptedException e) {
//		}
    }

    public void showIPCamera() throws Exception {
        OpenCVFrameGrabber frameGrabber = new OpenCVFrameGrabber("http://172.20.10.2:8080");
        frameGrabber.setFormat("mjpeg");
        frameGrabber.start();
        IplImage iPimg = frameGrabber.grab();
        CanvasFrame canvasFrame = new CanvasFrame("Camera");
        canvasFrame.setCanvasSize(iPimg.width(), iPimg.height());

        while (canvasFrame.isVisible() && (iPimg = frameGrabber.grab()) != null) {
            canvasFrame.showImage(iPimg);
        }
        frameGrabber.stop();
        canvasFrame.dispose();
        System.exit(0);
    }

    public void loadData(dbRow filter) {
        setData(Cameras.loadData(filter));
    }

    private void setData(dbList data) {
        DefaultTableModel tm = (DefaultTableModel) table.getModel();
        tm.setRowCount(0);
        Map<String, String> d;
        for (int i : data.keySet()) {
            d = data.get(i);
            tm.addRow(new Object[]{
                d.get("id"),
                d.get("name"),
                d.get("url"),
                d.get("camera_id"),
                d.get("stream_name")
            });
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
