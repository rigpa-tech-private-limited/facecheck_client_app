package com.facecheck.client;

import com.facecheck.db.dbRow;
import com.facecheck.tools.AppInfo;
import com.facecheck.tools.Utils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import net.miginfocom.swing.MigLayout;
import com.facecheck.tools.Utils;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

/**
 *
 * @author user
 */
public class CameraForm extends JDialog {

    private String ObjectKeyID = "";
    private String ObjectKeyCameraID = "";

    public CameraForm(JFrame parent, String title) {
        super(parent);
        setTitle(title);
        setModal(true);
        this.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        initComponents();
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    private Map<String, JTextField> entries;
    private JPanel form;

    private void initComponents() {
        Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        ((JComponent) getContentPane()).setBorder(padding);

        entries = new HashMap<String, JTextField>();
        form = new JPanel(new MigLayout("insets 5"));
        addFormElement("Name", "name", new JTextField());
        addFormElement("URL(RTSP)", "url", new JTextField());
//        addFormElement("Type", "handphone", new JTextField());
//        addFormElement("Status", "email", new JTextField());

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CameraForm.this.save();
            }

        });
        form.add(btnSave, "span, right, push, gapy 10, wrap");

        getContentPane().add(form, BorderLayout.CENTER);
    }

    private void addFormElement(String label, String field, JTextField el) {
        form.add(new JLabel(label));
        form.add(el, "w 200, wrap");
        entries.put(field, el);
    }

    public void save() {
        dbRow values = getEntries();
        Utils.stdout(values);
        if (!validate(values)) {
            return;
        }
        if (!ObjectKeyID.equals("")) {
            values.put("id", ObjectKeyID);
            values.put("camera_id", ObjectKeyCameraID);
            System.out.println("DataValues" + values);
            System.out.println("Name : " + values.get("name"));
            System.out.println("URL : " + values.get("url"));
            
            try {
                System.out.println("Edit Camera Post API started");
                URL url = new URL(AppInfo.BASE_URL + AppInfo.EDIT_CAMERA);
                Map<String, String> params = new ConcurrentHashMap<String, String>();
                params.put("name", values.get("name"));
                params.put("camera_id", values.get("camera_id"));
                StringBuilder postData = new StringBuilder();

                byte[] postDataBytes = Utils.setPostDataBytes(params, postData);
                params.clear();
                String response = Utils.getResponse(postDataBytes, url);
                System.out.println("Edit Camera Post API stopped");
                System.out.println(response);
                if (new JSONObject(response).get("status").equals("success")) {
                    System.out.println("status - success");
                    JSONObject obj = new JSONObject(response);
                    String streamName = obj.getString("video_stream_name");
                    values.put("stream_name", streamName);
                    System.out.println("stream_name : " + values.get("stream_name"));
                    String cameraID = obj.getString("camera_id");
                    values.put("camera_id", cameraID);
                    System.out.println("camera_id : " + values.get("camera_id"));
                    int saved = Launcher.getDatabaseConnection().save("cameras", "id", values);
                    if (saved > 0) {
                        setVisible(false);
                        Utils.alert("Data saved");
                    } else {
                        Utils.alert("Data not saved");
                    }
                } else {
                    System.out.println("status - error");
                    Utils.alert("Data not saved");
                }
            } catch (Exception e) {
                System.out.println("Error " + e.getMessage());
                Utils.alert("Data not saved");
            }
        } else {
            System.out.println("DataValues" + values);
            System.out.println("Name : " + values.get("name"));
            System.out.println("URL : " + values.get("url"));

            try {
                System.out.println("Add Camera Post API started");
                URL url = new URL(AppInfo.BASE_URL + AppInfo.ADD_CAMERA);
                Map<String, String> params = new ConcurrentHashMap<String, String>();
                params.put("name", values.get("name"));
                params.put("url", values.get("url"));
                StringBuilder postData = new StringBuilder();

                byte[] postDataBytes = Utils.setPostDataBytes(params, postData);
                params.clear();
                String response = Utils.getResponse(postDataBytes, url);
                System.out.println("Add Camera Post API stopped");
                System.out.println(response);
                if (new JSONObject(response).get("status").equals("success")) {
                    System.out.println("status - success");
                    JSONObject obj = new JSONObject(response);
                    String streamName = obj.getString("video_stream_name");
                    values.put("stream_name", streamName);
                    System.out.println("stream_name : " + values.get("stream_name"));
                    String cameraID = obj.getString("camera_id");
                    values.put("camera_id", cameraID);
                    System.out.println("camera_id : " + values.get("camera_id"));
                    int saved = Launcher.getDatabaseConnection().save("cameras", "id", values);
                    if (saved > 0) {
                        setVisible(false);
                        Utils.alert("Data saved");
                    } else {
                        Utils.alert("Data not saved");
                    }
                } else {
                    System.out.println("status - error");
                    Utils.alert("Data not saved");
                }
            } catch (Exception e) {
                System.out.println("Error " + e.getMessage());
                Utils.alert("Data not saved");
            }

        }

    }

    private boolean validate(dbRow values) {
        for (String s : values.keySet()) {
            if (values.get(s).trim().isEmpty()) {
                Utils.alert("Fill all fields.");
                return false;
            }
        }
        return true;
    }

    private dbRow getEntries() {
        dbRow values = new dbRow();
        for (String name : entries.keySet()) {
            values.put(name, entries.get(name).getText());
        }

        return values;
    }

    public void loadData(String id, String camera_id) {
        dbRow data = Cameras.getMember(id);
        if (data == null) {
            setVisible(false);
            return;
        }

        ObjectKeyID = id;
        ObjectKeyCameraID = camera_id;
        for (String field : data.keySet()) {
            if (entries.containsKey(field)) {
                entries.get(field).setText(data.get(field));
            }
        }
    }
}
